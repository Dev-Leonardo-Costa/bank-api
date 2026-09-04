package com.leonardo.bank_api.auth.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthMetricsServiceImpl implements AuthMetricsService {

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementAuthFailure() {
        meterRegistry
                .counter("bank.auth.failed.total")
                .increment();
    }

    @Override
    public void incrementAuthSuccess() {
        meterRegistry
                .counter("bank.auth.success.total")
                .increment();
    }
}
