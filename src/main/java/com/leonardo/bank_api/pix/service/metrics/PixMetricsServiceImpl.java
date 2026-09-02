package com.leonardo.bank_api.pix.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PixMetricsServiceImpl implements PixMetricsService {

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementPixSuccess() {
        meterRegistry
                .counter("bank.pix.success.total")
                .increment();
    }

    @Override
    public void incrementPixFailed() {
        meterRegistry
                .counter("bank.pix.failed.total")
                .increment();
    }

    @Override
    public void incrementPixScheduled() {
        meterRegistry
                .counter("bank.pix.scheduled.total")
                .increment();
    }

    @Override
    public void incrementPixCanceled() {
        meterRegistry
                .counter("bank.pix.canceled.total")
                .increment();
    }
}