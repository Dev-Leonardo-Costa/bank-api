package com.leonardo.bank_api.account.mapper;

import com.leonardo.bank_api.account.dto.AccountResponse;
import com.leonardo.bank_api.account.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface AccountMapper {

    @Mapping(source = "customer.id", target = "customerId")
    AccountResponse toResponse(Account account);
}