package com.leonardo.bank_api.pixschedule.unitario;

import com.leonardo.bank_api.account.entity.Account;
import com.leonardo.bank_api.pix.entity.PixSchedule;
import com.leonardo.bank_api.pix.repository.PixScheduleRepository;
import com.leonardo.bank_api.pix.scheduler.PixScheduleProcessor;
import com.leonardo.bank_api.pix.service.PixService;
import com.leonardo.bank_api.shared.enums.PixScheduleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PixScheduleProcessorTest {

    @Mock
    private PixScheduleRepository pixScheduleRepository;

    @Mock
    private PixService pixService;

    @InjectMocks
    private PixScheduleProcessor pixScheduleProcessor;

    private Account sourceAccount;
    private PixSchedule schedule;

    @BeforeEach
    void setUp() {

        sourceAccount = Account.builder()
                .id(5L)
                .balance(new BigDecimal("1000.00"))
                .build();

        schedule = PixSchedule.builder()
                .id(1L)
                .sourceAccount(sourceAccount)
                .pixKey("79849933003")
                .amount(new BigDecimal("100.00"))
                .scheduledAt(LocalDateTime.now().minusMinutes(1))
                .status(PixScheduleStatus.SCHEDULED)
                .build();
    }

    @Test
    void shouldProcessScheduledPixSuccessfully() {

        when(
                pixScheduleRepository
                        .findAllByStatusAndScheduledAtLessThanEqual(
                                eq(PixScheduleStatus.SCHEDULED),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(List.of(schedule));

        pixScheduleProcessor.processScheduledPix();

        verify(pixService).executeScheduledPix(
                5L,
                "79849933003",
                new BigDecimal("100.00")
        );

        assertThat(schedule.getStatus())
                .isEqualTo(PixScheduleStatus.COMPLETED);

        verify(pixScheduleRepository, times(3))
                .save(schedule);
    }

    @Test
    void shouldMarkScheduleAsFailedWhenPixExecutionFails() {

        when(
                pixScheduleRepository
                        .findAllByStatusAndScheduledAtLessThanEqual(
                                eq(PixScheduleStatus.SCHEDULED),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(List.of(schedule));

        doThrow(new RuntimeException("Erro ao executar PIX"))
                .when(pixService)
                .executeScheduledPix(
                        5L,
                        "79849933003",
                        new BigDecimal("100.00")
                );

        pixScheduleProcessor.processScheduledPix();

        assertThat(schedule.getStatus())
                .isEqualTo(PixScheduleStatus.FAILED);

        verify(pixService).executeScheduledPix(
                5L,
                "79849933003",
                new BigDecimal("100.00")
        );

        verify(pixScheduleRepository, times(3))
                .save(schedule);
    }

    @Test
    void shouldNotProcessWhenThereAreNoScheduledPix() {

        when(
                pixScheduleRepository
                        .findAllByStatusAndScheduledAtLessThanEqual(
                                eq(PixScheduleStatus.SCHEDULED),
                                any(LocalDateTime.class)
                        )
        ).thenReturn(List.of());

        pixScheduleProcessor.processScheduledPix();

        verifyNoInteractions(pixService);

        verify(pixScheduleRepository, never())
                .save(any(PixSchedule.class));
    }
}