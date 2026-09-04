package com.leonardo.bank_api.account.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountMetricsServiceImpl implements AccountMetricsService {

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementAccountCreated() {
        meterRegistry
                .counter("bank.account.created.total")
                .increment();
    }
}
