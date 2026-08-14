package com.leonardo.bank_api.transaction.service;

import com.leonardo.bank_api.transaction.dto.request.DepositRequest;
import com.leonardo.bank_api.transaction.dto.request.TransferRequest;
import com.leonardo.bank_api.transaction.dto.request.WithdrawRequest;
import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;

public interface TransactionService {

    TransactionResponse deposit(Long accountId, DepositRequest request);

    TransactionResponse transfer(Long sourceAccountId, TransferRequest request);

    TransactionResponse withdraw(Long accountId, WithdrawRequest request);
}