package com.example.bankapp.service;

import com.example.bankapp.dto.request.AccountRequest;
import com.example.bankapp.dto.request.TransactionRequest;
import com.example.bankapp.dto.response.AccountResponse;
import com.example.bankapp.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface AccountService {
    AccountResponse createAccount(AccountRequest request, String username);
    List<AccountResponse> getUserAccounts(String username);
    AccountResponse getAccountByNumber(String accountNumber, String username);
    TransactionResponse deposit(TransactionRequest request, String username);
    TransactionResponse withdraw(TransactionRequest request, String username);
    TransactionResponse transfer(TransactionRequest request, String username);
    List<TransactionResponse> getTransactionHistory(String accountNumber, String username);
    Page<TransactionResponse> getTransactionHistoryPaginated(
    String accountNumber, String username, Pageable pageable);
    
}