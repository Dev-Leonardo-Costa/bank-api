package com.leonardo.bank_api.account.service;

import com.leonardo.bank_api.account.dto.AccountResponse;
import com.leonardo.bank_api.account.dto.CreateAccountRequest;

public interface AccountService {

    AccountResponse createAccount();

    AccountResponse getAccountById(Long id);
}