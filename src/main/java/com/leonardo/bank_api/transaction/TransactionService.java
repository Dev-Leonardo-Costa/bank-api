package com.leonardo.bank_api.transaction;

import com.leonardo.bank_api.shared.enums.TransactionType;
import com.leonardo.bank_api.transaction.dto.request.DepositRequest;
import com.leonardo.bank_api.transaction.dto.request.TransferRequest;
import com.leonardo.bank_api.transaction.dto.request.WithdrawRequest;
import com.leonardo.bank_api.transaction.dto.response.StatementTransactionResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionReceiptResponse;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface TransactionService {

    TransactionResponse deposit(Long accountId, DepositRequest request);

    TransactionResponse transfer(Long sourceAccountId, TransferRequest request);

    TransactionResponse withdraw(Long accountId, WithdrawRequest request);

    Page<TransactionResponse> getStatement(
            Long accountId,
            TransactionType type,
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    Page<StatementTransactionResponse> getStatement(Long accountId, Pageable pageable);

    TransactionReceiptResponse getReceipt(Long transactionId);
}