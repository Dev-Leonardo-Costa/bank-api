package com.leonardo.bank_api.transaction.controller;

import com.leonardo.bank_api.transaction.dto.request.DepositRequest;
import com.leonardo.bank_api.transaction.dto.request.TransferRequest;
import com.leonardo.bank_api.transaction.dto.request.WithdrawRequest;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import com.leonardo.bank_api.transaction.service.TransactionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/transactions")
@RequiredArgsConstructor
public class TransactionController {

    private final TransactionService transactionService;

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<TransactionResponse> deposit(
            @PathVariable Long accountId,
            @Valid @RequestBody DepositRequest request
    ) {

        return ResponseEntity.ok(transactionService.deposit(accountId, request));
    }

    @PostMapping("/accounts/{sourceAccountId}/transfer")
    public ResponseEntity<TransactionResponse> transfer(
            @PathVariable Long sourceAccountId,
            @Valid @RequestBody TransferRequest request
    ) {

        return ResponseEntity.ok(transactionService.transfer(sourceAccountId, request));
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(
            @PathVariable Long accountId,
            @Valid @RequestBody WithdrawRequest request
    ) {

        return ResponseEntity.ok(
                transactionService.withdraw(accountId, request)
        );
    }
}