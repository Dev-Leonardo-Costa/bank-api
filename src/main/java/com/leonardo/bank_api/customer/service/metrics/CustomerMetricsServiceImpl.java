package com.leonardo.bank_api.customer.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomerMetricsServiceImpl implements CustomerMetricsService {

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementCustomerCreated() {
        meterRegistry
                .counter("bank.customer.created.total")
                .increment();
    }
}
