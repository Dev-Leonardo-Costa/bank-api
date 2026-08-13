package com.leonardo.bank_api.account.repository;

import com.leonardo.bank_api.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByNumber(String number);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByNumber(String number);
}