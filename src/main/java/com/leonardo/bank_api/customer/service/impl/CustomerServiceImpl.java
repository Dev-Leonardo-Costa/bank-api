package com.leonardo.bank_api.customer.service.impl;

import com.leonardo.bank_api.common.exception.DuplicateResourceException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.customer.dto.request.CreateCustomerRequest;
import com.leonardo.bank_api.customer.dto.response.CustomerResponse;
import com.leonardo.bank_api.customer.entity.Customer;
import com.leonardo.bank_api.customer.mapper.CustomerMapper;
import com.leonardo.bank_api.customer.repository.CustomerRepository;
import com.leonardo.bank_api.customer.service.CustomerService;
import com.sun.jdi.request.DuplicateRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private final CustomerRepository customerRepository;

    private final PasswordEncoder passwordEncoder;
    private final CustomerMapper customerMapper;

    @Override
    public CustomerResponse createCustomer(CreateCustomerRequest request) {

        validateEmailEndCpf(request);

        Customer customer = customerMapper.toEntity(request);
        customer.setPassword(passwordEncoder.encode(request.password()));
        customerRepository.save(customer);
        return customerMapper.toResponse(customer);
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
        } else if (customerRepository.findByCpf(request.cpf()).isPresent()) {
            throw new DuplicateResourceException("CPF já cadastrado");
        }
    }

}
