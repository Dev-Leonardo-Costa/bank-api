package com.leonardo.bank_api.account.mapper;

import com.leonardo.bank_api.account.dto.AccountResponse;
import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.customer.entity.Customer;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "customer.id", target = "customerId")
    AccountResponse toResponse(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "number", source = "accountNumber")
    @Mapping(target = "agency", source = "agency")
    @Mapping(target = "balance", constant = "0")
    @Mapping(target = "status", constant = "ACTIVE")
    @Mapping(target = "customer", source = "customer")
    @Mapping(target = "createdAt", ignore = true)
    Account toEntity(Customer customer, String accountNumber, String agency);
}