package com.example.bankapp.dto.request;

import com.example.bankapp.entity.Account;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull(message = "Account type is required")
    private Account.AccountType accountType;
}