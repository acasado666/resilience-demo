package com.resilience.resiliencedemo.repository;

import com.resilience.resiliencedemo.model.User;

public interface AlternativeUserRepository {
    User getById(Long id);
    User simulateCircuitBreaker(Long id);
    User simulateRetry(Long id);
}
