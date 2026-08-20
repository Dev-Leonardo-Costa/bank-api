package com.leonardo.bank_api.pix.mapper;

import com.leonardo.bank_api.pix.dto.response.PixKeyResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PixMapper {

    @Mapping(source = "account.id", target = "accountId")
    PixKeyResponse toResponse(PixKey pixKey);
}