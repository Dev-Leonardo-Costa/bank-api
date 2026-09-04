package com.leonardo.bank_api.auth.service.metrics;

public interface AuthMetricsService {
    void incrementAuthFailure();
    void incrementAuthSuccess();
}
