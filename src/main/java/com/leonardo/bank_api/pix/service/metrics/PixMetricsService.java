package com.leonardo.bank_api.pix.service.metrics;

public interface PixMetricsService {

    void incrementPixSuccess();

    void incrementPixFailed();

    void incrementPixScheduled();

    void incrementPixCanceled();
}