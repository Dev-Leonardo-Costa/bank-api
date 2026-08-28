package com.leonardo.bank_api.pix.service.impl;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.account.repository.AccountRepository;
import com.leonardo.bank_api.common.exception.BusinessException;
import com.leonardo.bank_api.common.exception.ForbiddenOperationException;
import com.leonardo.bank_api.common.exception.ResourceNotFoundException;
import com.leonardo.bank_api.pix.dto.request.CreatePixScheduleRequest;
import com.leonardo.bank_api.pix.dto.response.PixScheduleResponse;
import com.leonardo.bank_api.pix.entity.PixKey;
import com.leonardo.bank_api.pix.entity.PixSchedule;
import com.leonardo.bank_api.pix.mapper.PixScheduleMapper;
import com.leonardo.bank_api.pix.repository.PixKeyRepository;
import com.leonardo.bank_api.pix.repository.PixScheduleRepository;
import com.leonardo.bank_api.pix.service.PixScheduleService;
import com.leonardo.bank_api.shared.enums.PixScheduleStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PixScheduleServiceImpl implements PixScheduleService {

    private final PixScheduleRepository pixScheduleRepository;
    private final PixKeyRepository pixKeyRepository;
    private final AccountRepository accountRepository;
    private final PixScheduleMapper pixScheduleMapper;

    @Transactional
    @Override
    public PixScheduleResponse createSchedule(CreatePixScheduleRequest request) {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        Account sourceAccount = accountRepository
                .findById(request.sourceAccountId())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Conta de origem não encontrada"
                        )
                );

        if (!sourceAccount.getCustomer()
                .getEmail()
                .equalsIgnoreCase(email)) {

            throw new ForbiddenOperationException(
                    "Você não possui permissão para agendar PIX nesta conta"
            );
        }

        PixKey destinationPixKey = pixKeyRepository
                .findByKeyValue(request.pixKey())
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Chave PIX de destino não encontrada"
                        )
                );

        PixSchedule schedule = pixScheduleMapper.toEntity(request, sourceAccount, destinationPixKey);

        PixSchedule saved = pixScheduleRepository.save(schedule);

        return pixScheduleMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PixScheduleResponse> findMySchedules() {

        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        return pixScheduleRepository
                .findAllBySourceAccountCustomerEmail(email)
                .stream()
                .map(pixScheduleMapper::toResponse)
                .toList();
    }

    @Transactional
    @Override
    public PixScheduleResponse cancelSchedule(Long scheduleId) {
        String email = SecurityContextHolder.getContext()
                .getAuthentication()
                .getName();

        PixSchedule schedule = pixScheduleRepository
                .findById(scheduleId)
                .orElseThrow(() ->
                        new ResourceNotFoundException(
                                "Agendamento PIX não encontrado"
                        )
                );

        if (!schedule.getSourceAccount()
                .getCustomer()
                .getEmail()
                .equalsIgnoreCase(email)) {

            throw new ForbiddenOperationException(
                    "Você não possui permissão para cancelar este PIX agendado"
            );
        }

        if (schedule.getStatus() != PixScheduleStatus.SCHEDULED) {
            throw new BusinessException(
                    "Somente PIX com status SCHEDULED pode ser cancelado"
            );
        }

        schedule.setStatus(PixScheduleStatus.CANCELED);

        PixSchedule saved = pixScheduleRepository.save(schedule);

        return pixScheduleMapper.toResponse(saved);
    }
}