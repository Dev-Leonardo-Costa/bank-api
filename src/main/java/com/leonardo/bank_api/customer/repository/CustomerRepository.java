package com.leonardo.bank_api.customer.repository;

import com.leonardo.bank_api.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCpf(String cpf);

    Optional<Customer> findByEmail(String email);
}
