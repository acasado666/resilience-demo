package com.resilience.resiliencedemo.repository;

import com.resilience.resiliencedemo.model.User;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
public class SimpleUserRepository implements AlternativeUserRepository{

    @Override
    @Cacheable("users")
    public User getById(Long id) {
        simulateSlowService();

        User user = getUser();
        return user;
    }

    @Override
    public User simulateCircuitBreaker(Long id) {
        User user = getUser();
        user.setId(id);
        user.setNickName("Some one from circuit breaker");
        return user;
    }

    @Override
    public User simulateRetry(Long id) {
        User user = getUser();
        user.setId(id);
        user.setNickName("Some one from Retry");
        return user;
    }

    private void simulateSlowService() {
        try {
            long time = 3000L;
            Thread.sleep(time);
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private User getUser() {
        User user = new User();
        user.setId(100L);
        user.setNickName("Some one cached");
        user.setName("Alexander");
        user.setLastName("Magno");
        return user;
    }
}
