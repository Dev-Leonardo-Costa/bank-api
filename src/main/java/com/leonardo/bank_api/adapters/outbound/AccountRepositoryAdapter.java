package com.leonardo.bank_api.adapters.outbound;

import com.leonardo.bank_api.ports.AccountRepositoryPort;
import com.leonardo.bank_api.domain.Account;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Optional;

@Repository
public class AccountRepositoryAdapter implements AccountRepositoryPort {

    @PersistenceContext
    private EntityManager em;

    @Override
    public Account save(Account account) {
        if (account.getId() == null) {
            em.persist(account);
            return account;
        } else {
            return em.merge(account);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        Account a = em.find(Account.class, id);
        return Optional.ofNullable(a);
    }
}
