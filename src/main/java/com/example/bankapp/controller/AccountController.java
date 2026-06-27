package com.example.bankapp.controller;

import com.example.bankapp.dto.request.AccountRequest;
import com.example.bankapp.dto.request.TransactionRequest;
import com.example.bankapp.dto.response.AccountResponse;
import com.example.bankapp.dto.response.ApiResponse;
import com.example.bankapp.dto.response.TransactionResponse;
import com.example.bankapp.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<ApiResponse<AccountResponse>> createAccount(
            @Valid @RequestBody AccountRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        AccountResponse response = accountService.createAccount(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account created successfully", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AccountResponse>>> getUserAccounts(
            @AuthenticationPrincipal UserDetails userDetails) {
        List<AccountResponse> accounts = accountService.getUserAccounts(userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Accounts fetched successfully", accounts));
    }

    @GetMapping("/{accountNumber}")
    public ResponseEntity<ApiResponse<AccountResponse>> getAccount(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        AccountResponse response = accountService.getAccountByNumber(
                accountNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Account fetched successfully", response));
    }

    @PostMapping("/deposit")
    public ResponseEntity<ApiResponse<TransactionResponse>> deposit(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = accountService.deposit(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Deposit successful", response));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<ApiResponse<TransactionResponse>> withdraw(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = accountService.withdraw(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Withdrawal successful", response));
    }

    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<TransactionResponse>> transfer(
            @Valid @RequestBody TransactionRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        TransactionResponse response = accountService.transfer(request, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transfer successful", response));
    }

    @GetMapping("/{accountNumber}/transactions")
    public ResponseEntity<ApiResponse<List<TransactionResponse>>> getTransactionHistory(
            @PathVariable String accountNumber,
            @AuthenticationPrincipal UserDetails userDetails) {
        List<TransactionResponse> transactions = accountService.getTransactionHistory(
                accountNumber, userDetails.getUsername());
        return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", transactions));
    }

    @GetMapping("/{accountNumber}/transactions/paged")
public ResponseEntity<ApiResponse<Map<String, Object>>> getPagedTransactions(
        @PathVariable String accountNumber,
        @AuthenticationPrincipal UserDetails userDetails,
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {

    Pageable pageable = PageRequest.of(page, size);
    Page<TransactionResponse> transactions = accountService
            .getTransactionHistoryPaginated(accountNumber, userDetails.getUsername(), pageable);

    Map<String, Object> response = new HashMap<>();
    response.put("transactions", transactions.getContent());
    response.put("currentPage", transactions.getNumber());
    response.put("totalItems", transactions.getTotalElements());
    response.put("totalPages", transactions.getTotalPages());

    return ResponseEntity.ok(ApiResponse.success("Transactions fetched successfully", response));
}
}