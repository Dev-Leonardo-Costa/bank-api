package com.leonardo.bank_api.transaction.service.metrics;

import io.micrometer.core.instrument.MeterRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TransferMetricsServiceImpl implements TransferMetricsService {

    private final MeterRegistry meterRegistry;

    @Override
    public void incrementTransferSuccess() {
        meterRegistry
                .counter("bank.transfer.success.total")
                .increment();
    }

    @Override
    public void incrementTransferFailed() {
        meterRegistry
                .counter("bank.transfer.failed.total")
                .increment();
    }
}
