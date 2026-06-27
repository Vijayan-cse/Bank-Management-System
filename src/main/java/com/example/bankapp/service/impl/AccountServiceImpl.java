package com.example.bankapp.service.impl;

import com.example.bankapp.dto.request.AccountRequest;
import com.example.bankapp.dto.request.TransactionRequest;
import com.example.bankapp.dto.response.AccountResponse;
import com.example.bankapp.dto.response.TransactionResponse;
import com.example.bankapp.entity.Account;
import com.example.bankapp.entity.Transaction;
import com.example.bankapp.entity.User;
import com.example.bankapp.exception.InsufficientBalanceException;
import com.example.bankapp.exception.ResourceNotFoundException;
import com.example.bankapp.exception.UnauthorizedException;
import com.example.bankapp.repository.AccountRepository;
import com.example.bankapp.repository.TransactionRepository;
import com.example.bankapp.repository.UserRepository;
import com.example.bankapp.service.AccountService;
import lombok.RequiredArgsConstructor;
import com.example.bankapp.exception.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final UserRepository userRepository;

    @Override
    public AccountResponse createAccount(AccountRequest request, String username) {
        User user = getUser(username);

        Account account = Account.builder()
                .accountNumber(generateAccountNumber())
                .accountType(request.getAccountType())
                .balance(BigDecimal.ZERO)
                .status(Account.AccountStatus.ACTIVE)
                .user(user)
                .build();

        return mapToAccountResponse(accountRepository.save(account));
    }

    @Override
    public List<AccountResponse> getUserAccounts(String username) {
        User user = getUser(username);
        return accountRepository.findByUserId(user.getId())
                .stream()
                .map(this::mapToAccountResponse)
                .collect(Collectors.toList());
    }

    @Override
    public AccountResponse getAccountByNumber(String accountNumber, String username) {
        Account account = getAndValidateAccount(accountNumber, username);
        return mapToAccountResponse(account);
    }

    @Override
    @Transactional
    public TransactionResponse deposit(TransactionRequest request, String username) {
        Account account = getAndValidateAccount(request.getAccountNumber(), username);

        account.setBalance(account.getBalance().add(request.getAmount()));
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.DEPOSIT)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .description(request.getDescription())
                .account(account)
                .build();

        return mapToTransactionResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse withdraw(TransactionRequest request, String username) {
        Account account = getAndValidateAccount(request.getAccountNumber(), username);

        if (account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transaction");
        }

        // Minimum balance check
        BigDecimal balanceAfterWithdrawal = account.getBalance().subtract(request.getAmount());
        if (balanceAfterWithdrawal.compareTo(Account.MINIMUM_BALANCE) < 0) {
            throw new BadRequestException("Withdrawal denied. Minimum balance of ₹"
                    + Account.MINIMUM_BALANCE + " must be maintained in SAVINGS account");
        }

        account.setBalance(balanceAfterWithdrawal);
        accountRepository.save(account);

        Transaction transaction = Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.WITHDRAWAL)
                .amount(request.getAmount())
                .balanceAfter(account.getBalance())
                .description(request.getDescription())
                .account(account)
                .build();

        return mapToTransactionResponse(transactionRepository.save(transaction));
    }

    @Override
    @Transactional
    public TransactionResponse transfer(TransactionRequest request, String username) {
        Account sourceAccount = getAndValidateAccount(request.getAccountNumber(), username);
        Account targetAccount = accountRepository
                .findByAccountNumber(request.getTargetAccountNumber())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "accountNumber", request.getTargetAccountNumber()));

        if (sourceAccount.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for this transaction");
        }

        sourceAccount.setBalance(sourceAccount.getBalance().subtract(request.getAmount()));
        targetAccount.setBalance(targetAccount.getBalance().add(request.getAmount()));

        accountRepository.save(sourceAccount);
        accountRepository.save(targetAccount);

        String txnId = UUID.randomUUID().toString();

        transactionRepository.save(Transaction.builder()
                .transactionId(txnId)
                .type(Transaction.TransactionType.TRANSFER_OUT)
                .amount(request.getAmount())
                .balanceAfter(sourceAccount.getBalance())
                .description(request.getDescription())
                .referenceAccountNumber(request.getTargetAccountNumber())
                .account(sourceAccount)
                .build());

        Transaction creditTxn = transactionRepository.save(Transaction.builder()
                .transactionId(UUID.randomUUID().toString())
                .type(Transaction.TransactionType.TRANSFER_IN)
                .amount(request.getAmount())
                .balanceAfter(targetAccount.getBalance())
                .description(request.getDescription())
                .referenceAccountNumber(request.getAccountNumber())
                .account(targetAccount)
                .build());

        return mapToTransactionResponse(creditTxn);
    }

    @Override
    public List<TransactionResponse> getTransactionHistory(String accountNumber, String username) {
        Account account = getAndValidateAccount(accountNumber, username);
        return transactionRepository.findByAccountIdOrderByCreatedAtDesc(account.getId())
                .stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    @Override
public Page<TransactionResponse> getTransactionHistoryPaginated(
        String accountNumber, String username, Pageable pageable) {
    Account account = getAndValidateAccount(accountNumber, username);
    return transactionRepository
            .findByAccountIdOrderByCreatedAtDesc(account.getId(), pageable)
            .map(this::mapToTransactionResponse);
}

    // ── Helpers ──────────────────────────────────────────────

    private User getUser(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User", "username", username));
    }

    private Account getAndValidateAccount(String accountNumber, String username) {
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Account", "accountNumber", accountNumber));

        if (!account.getUser().getUsername().equals(username)) {
            throw new UnauthorizedException("Unauthorized access to account");
        }

        return account;
    }

    private String generateAccountNumber() {
        String number;
        do {
            number = "ACC" + (long) (Math.random() * 9_000_000_000L + 1_000_000_000L);
        } while (accountRepository.existsByAccountNumber(number));
        return number;
    }

    private AccountResponse mapToAccountResponse(Account account) {
        return AccountResponse.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType().name())
                .balance(account.getBalance())
                .status(account.getStatus().name())
                .createdAt(account.getCreatedAt())
                .ownerUsername(account.getUser().getUsername())
                .build();
    }

    private TransactionResponse mapToTransactionResponse(Transaction txn) {
        return TransactionResponse.builder()
                .transactionId(txn.getTransactionId())
                .type(txn.getType().name())
                .amount(txn.getAmount())
                .balanceAfter(txn.getBalanceAfter())
                .description(txn.getDescription())
                .referenceAccountNumber(txn.getReferenceAccountNumber())
                .createdAt(txn.getCreatedAt())
                .build();
    }
}