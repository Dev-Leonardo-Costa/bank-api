package com.leonardo.bank_api.adapters.outbound.mapper;

import com.leonardo.bank_api.adapters.outbound.entity.AccountEntity;
import com.leonardo.bank_api.domain.Account;

public class AccountMapper {

    public static Account toDomain(AccountEntity e) {
        if (e == null) return null;
        return new Account(e.getId(), e.getCustomerId(), e.getBalance());
    }

    public static AccountEntity toEntity(Account d) {
        if (d == null) return null;
        return new AccountEntity(d.getId(), d.getCustomerId(), d.getBalance());
    }
}
