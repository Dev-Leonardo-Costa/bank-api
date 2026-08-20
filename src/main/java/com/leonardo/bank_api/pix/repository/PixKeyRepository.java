package com.leonardo.bank_api.pix.repository;

import com.leonardo.bank_api.pix.entity.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PixKeyRepository extends JpaRepository<PixKey, Long> {

    Optional<PixKey> findByKeyValue(String keyValue);

    boolean existsByKeyValue(String keyValue);

    List<PixKey> findAllByAccountCustomerEmail(String email);
}