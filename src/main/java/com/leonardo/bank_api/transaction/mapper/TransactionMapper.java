package com.leonardo.bank_api.transaction.mapper;

import com.leonardo.bank_api.transaction.dto.response.TransactionResponse;
import com.leonardo.bank_api.transaction.entity.Transaction;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransactionMapper {

    @Mapping(source = "sourceAccount.id", target = "sourceAccountId")
    @Mapping(source = "destinationAccount.id", target = "destinationAccountId")
    TransactionResponse toResponse(Transaction transaction);
}