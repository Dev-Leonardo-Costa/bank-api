package com.leonardo.bank_api.account.repository;

import com.leonardo.bank_api.account.entity.Account;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AccountRepository extends JpaRepository<Account, Long> {

    Optional<Account> findByNumber(String number);

    List<Account> findByCustomerId(Long customerId);

    boolean existsByNumber(String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT a
            FROM Account a
            JOIN FETCH a.customer
            WHERE a.id = :id
            """)
    Optional<Account> findByIdForUpdate(@Param("id") Long id);
}