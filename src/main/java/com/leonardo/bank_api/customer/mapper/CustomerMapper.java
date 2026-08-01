package com.leonardo.bank_api.customer.mapper;

import com.leonardo.bank_api.customer.dto.request.CreateCustomerRequest;
import com.leonardo.bank_api.customer.dto.response.CustomerResponse;
import com.leonardo.bank_api.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerMapper {

    Customer toEntity(CreateCustomerRequest request);

    @Mapping(target = "createdAt", expression = "java(customer.getCreatedAt().toLocalDate())")
    CustomerResponse toResponse(Customer customer);
}
