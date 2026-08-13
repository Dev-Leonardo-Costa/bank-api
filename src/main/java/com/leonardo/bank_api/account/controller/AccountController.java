package com.leonardo.bank_api.account.controller;

import com.leonardo.bank_api.account.dto.AccountResponse;
import com.leonardo.bank_api.account.dto.CreateAccountRequest;
import com.leonardo.bank_api.account.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @PostMapping
    public ResponseEntity<AccountResponse> createAccount() {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(accountService.createAccount());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id
    ) {

        return ResponseEntity.ok(
                accountService.getAccountById(id)
        );
    }
}