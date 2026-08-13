package com.leonardo.bank_api.account.service.impl;

import com.leonardo.bank_api.account.dto.AccountResponse;
import com.leonardo.bank_api.account.dto.CreateAccountRequest;
import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.mapper.AccountMapper;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.account.service.AccountService;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.customer.repository.CustomerRepository;
import com.leonardo.bank_api.shared.enums.AccountStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private static final String DEFAULT_AGENCY = "0001";

    private final AccountRepository accountRepository;
    private final CustomerRepository customerRepository;
    private final AccountMapper accountMapper;

    @Transactional
    @Override
    public AccountResponse createAccount() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Customer customer = customerRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Cliente não encontrado")
                );

        Account account = Account.builder()
                .number(generateAccountNumber())
                .agency(DEFAULT_AGENCY)
                .balance(BigDecimal.ZERO)
                .status(AccountStatus.ACTIVE)
                .customer(customer)
                .build();

        Account savedAccount = accountRepository.save(account);

        return accountMapper.toResponse(savedAccount);
    }

    @Override
    public AccountResponse getAccountById(Long id) {

        Account account = accountRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Conta não encontrada")
                );

        return accountMapper.toResponse(account);
    }

    private String generateAccountNumber() {

        SecureRandom random = new SecureRandom();

        String number;

        do {
            number = String.format(
                    "%08d",
                    random.nextInt(100_000_000)
            );
        } while (accountRepository.existsByNumber(number));

        return number;
    }
}