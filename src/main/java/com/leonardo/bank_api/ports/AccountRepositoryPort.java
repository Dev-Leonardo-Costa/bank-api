package com.leonardo.bank_api.ports;

import com.leonardo.bank_api.domain.Account;
import java.util.Optional;

public interface AccountRepositoryPort {
    Account save(Account account);
    Optional<Account> findById(Long id);
}
