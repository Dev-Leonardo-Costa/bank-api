package com.leonardo.bank_api.pix.scheduler;

import com.leonardo.bank_api.pix.dto.request.PixTransferRequest;
import com.leonardo.bank_api.pix.entity.PixSchedule;
import com.leonardo.bank_api.pix.repository.PixScheduleRepository;
import com.leonardo.bank_api.pix.service.PixService;
import com.leonardo.bank_api.shared.enums.PixScheduleStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class PixScheduleProcessor {

    private final PixScheduleRepository pixScheduleRepository;
    private final PixService pixService;

    @Scheduled(fixedDelay = 60000)
    public void processScheduledPix() {

        List<PixSchedule> schedules =
                pixScheduleRepository
                        .findAllByStatusAndScheduledAtLessThanEqual(
                                PixScheduleStatus.SCHEDULED,
                                LocalDateTime.now()
                        );

        log.info(
                "Encontrados {} PIX agendados para processamento",
                schedules.size()
        );

        schedules.forEach(this::process);
    }

    private void process(PixSchedule schedule) {

        try {

            log.info("Processando PIX agendado id={}", schedule.getId());

            schedule.setStatus(PixScheduleStatus.PROCESSING);

            pixScheduleRepository.save(schedule);

            pixService.executeScheduledPix(
                    schedule.getSourceAccount().getId(),
                    schedule.getPixKey(),
                    schedule.getAmount()
            );

            schedule.setStatus(PixScheduleStatus.COMPLETED);

            pixScheduleRepository.save(schedule);

            log.info("PIX agendado id={} executado com sucesso", schedule.getId());

        } catch (Exception ex) {
            schedule.setStatus(PixScheduleStatus.FAILED);

            pixScheduleRepository.save(schedule);

            log.error(
                    "Falha ao executar PIX agendado id={}: {}",
                    schedule.getId(),
                    ex.getMessage(),
                    ex
            );
        }

        pixScheduleRepository.save(schedule);
    }
}