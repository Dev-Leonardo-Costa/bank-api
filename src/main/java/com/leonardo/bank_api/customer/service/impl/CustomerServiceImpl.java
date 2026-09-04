package com.leonardo.bank_api.customer.service.impl;

import com.leonardo.bank_api.common.exception.DuplicateResourceException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.customer.dto.request.CreateCustomerRequest;
import com.leonardo.bank_api.customer.dto.response.CustomerResponse;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.customer.mapper.CustomerMapper;
import com.leonardo.bank_api.customer.repository.CustomerRepository;
import com.leonardo.bank_api.customer.service.CustomerService;
import com.leonardo.bank_api.customer.service.metrics.CustomerMetricsServiceImpl;
import com.leonardo.bank_api.security.entity.RoleEntity;
import com.leonardo.bank_api.security.entity.UserEntity;
import com.leonardo.bank_api.security.repository.RoleRepository;
import com.leonardo.bank_api.security.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    private final CustomerMetricsServiceImpl customerMetricsService;

    @Transactional
    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        validateEmailEndCpf(request);

        Customer customer = customerMapper.toEntity(request);

        Customer savedCustomer = customerRepository.save(customer);

        RoleEntity roleCustomer = roleRepository.findByName("ROLE_CUSTOMER")
                .orElseThrow(() ->
                        new ResourceNotFoundException("Role ROLE_CUSTOMER não encontrada")
                );

        UserEntity user = UserEntity.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .enabled(true)
                .roles(Set.of(roleCustomer))
                .build();

        userRepository.save(user);

        customerMetricsService.incrementCustomerCreated();

        return customerMapper.toResponse(savedCustomer);
    }

    @Override
    public CustomerResponse getCustomerById(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente não encontrado"));
        return customerMapper.toResponse(customer);
    }

    private void validateEmailEndCpf(CreateCustomerRequest request) {
        if (customerRepository.findByEmail(request.email()).isPresent() ) {
            throw new DuplicateResourceException("E-mail já cadastrado");
        }

        if (customerRepository.findByCpf(request.cpf()).isPresent()) {
            throw new DuplicateResourceException("CPF já cadastrado");
        }
    }

}
