package com.leonardo.bank_api.transaction.service.metrics;

public interface TransferMetricsService {
    void incrementTransferSuccess();
    void incrementTransferFailed();
}
