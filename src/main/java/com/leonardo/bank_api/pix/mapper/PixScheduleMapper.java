package com.leonardo.bank_api.pix.mapper;


import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.pix.dto.request.CreatePixScheduleRequest;
import com.leonardo.bank_api.pix.dto.response.PixScheduleResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.pix.entity.PixSchedule;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PixScheduleMapper {

    @Mapping(source = "sourceAccount.id", target = "sourceAccountId")
    PixScheduleResponse toResponse(PixSchedule pixSchedule);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "sourceAccount", source = "sourceAccount")
    @Mapping(target = "pixKey", source = "destinationPixKey.keyValue")
    @Mapping(target = "status", constant = "SCHEDULED")
    @Mapping(target = "createdAt", ignore = true)
    PixSchedule toEntity(CreatePixScheduleRequest request, Account sourceAccount, PixKey destinationPixKey);

}
