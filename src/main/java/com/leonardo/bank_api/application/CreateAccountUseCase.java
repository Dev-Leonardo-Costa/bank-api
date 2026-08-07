package com.leonardo.bank_api.application;

import com.leonardo.bank_api.domain.Account;
import com.leonardo.bank_api.ports.AccountRepositoryPort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateAccountUseCase {

    private final AccountRepositoryPort accountRepository;

    public CreateAccountUseCase(AccountRepositoryPort accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public Account execute(Long customerId) {
        Account account = new Account();
        account.setCustomerId(customerId);
        account.setBalance(java.math.BigDecimal.ZERO);
        return accountRepository.save(account);
    }
}
