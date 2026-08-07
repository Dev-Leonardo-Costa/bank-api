package com.leonardo.bank_api.adapters.outbound;

import com.leonardo.bank_api.adapters.outbound.entity.AccountEntity;
import com.leonardo.bank_api.adapters.outbound.mapper.AccountMapper;
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
        AccountEntity entity = AccountMapper.toEntity(account);
        if (entity.getId() == null) {
            em.persist(entity);
            return AccountMapper.toDomain(entity);
        } else {
            AccountEntity merged = em.merge(entity);
            return AccountMapper.toDomain(merged);
        }
    }

    @Override
    public Optional<Account> findById(Long id) {
        AccountEntity e = em.find(AccountEntity.class, id);
        return Optional.ofNullable(AccountMapper.toDomain(e));
    }
}
