package com.leonardo.bank_api.customer.service;

import com.leonardo.bank_api.customer.dto.request.CreateCustomerRequest;
import com.leonardo.bank_api.customer.dto.response.CustomerResponse;

public interface CustomerService {

    CustomerResponse createCustomer(CreateCustomerRequest request);

    CustomerResponse getCustomerById(Long id);
}
