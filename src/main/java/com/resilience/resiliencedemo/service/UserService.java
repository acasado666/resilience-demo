package com.resilience.resiliencedemo.service;

import com.resilience.resiliencedemo.controller.UserNotFoundException;
import com.resilience.resiliencedemo.controller.UserRequest;
import com.resilience.resiliencedemo.model.User;
import com.resilience.resiliencedemo.repository.AlternativeUserRepository;
import com.resilience.resiliencedemo.repository.UserRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Slf4j
@Service
public class UserService {

    RestTemplate restTemplate = new RestTemplate();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    AlternativeUserRepository alternativeUserRepository;

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User getUserById(Long id) {
        Optional<User> requestedUser = userRepository.findById(id);
        if (requestedUser.isEmpty()) {
            throw new UserNotFoundException(String.format("User with id: '%s' not found", id));
        }
        return requestedUser.get();
    }

    public User getUserByIdCache(Long id) {
        return alternativeUserRepository.getById(id);
    }

    @Transactional
    public User updateUser(Long id, UserRequest userToUpdateRequest) {
        Optional<User> userFromDatabase = userRepository.findById(id);

        if (userFromDatabase.isEmpty()) {
            throw new UserNotFoundException(String.format("Book with id: '%s' not found", id));
        }
        User userToUpdate = userFromDatabase.get();

        userToUpdate.setNickName(userToUpdateRequest.getNickName());
        userToUpdate.setName(userToUpdateRequest.getName());
        userToUpdate.setLastName(userToUpdateRequest.getLastName());

        return userToUpdate;
    }

    public void deleteUserById(Long id) {
        userRepository.deleteById(id);
    }

    @Bulkhead(name = "userBulkheadService", fallbackMethod = "userBulkheadFallback")
    public User externalServiceBulkhead() {
        log.info("tryng to log other service");
        User result = restTemplate.getForObject("http://localhost:9090/serviceA", User.class);
        log.info("backendA : result {}");
        return result;
    }

    public User userBulkheadFallback(Throwable t) {
        User user = getFallbackUser();
        user.setNickName("UserBulkhead Fallback response");
        log.info("userBulkheadFallback");
        return user;
    }


    @Retry(name = "userRetryService", fallbackMethod = "userRetryFallback")
    public User externalServiceRetry(Long id) {
        log.info("trying to log other service");
        User user = restTemplate.getForObject("http://localhost:9090/serviceA/" + id, User.class);
        log.info("backendA : result {}", user);
        return user;
    }

    public User userRetryFallback(Long id, Throwable t) {
        User user = alternativeUserRepository.simulateRetry(id);
        log.info("fallbackCircuitBreaker");
        return user;
    }

    @CircuitBreaker(name = "userCircuitBreakerCService", fallbackMethod = "userCircuitBreakerFallback")
    public User externalServiceCircuitBreaker(Long id) {
        log.info("trying to log other service");
        User result = restTemplate.getForObject("http://localhost:9090/serviceA/" +id, User.class);
        log.info("backendA : result {}", result);
        return result;
    }

    public User userCircuitBreakerFallback(Long id, Throwable t) {
        User user = alternativeUserRepository.simulateCircuitBreaker(id);
        log.info("fallbackCircuitBreaker");
        return user;
    }

    @RateLimiter(name = "userRateLimiterService", fallbackMethod = "userRateLimiterFallback")
    public String ratelimiter() {
        return "Time limiter SUCCESS";
    }

    public String userRateLimiterFallback(Throwable t) {
        String result = restTemplate.getForObject("http://localhost:9091/serviceB", String.class);
        log.info("fallbackRetry : result {}", result);
        return "Rate limiter SUCCESS fallback";
    }

    @TimeLimiter(name = "userTimeLimiterService")
    public CompletableFuture<String> futureSuccess() {
        return CompletableFuture.completedFuture("Timelimiter response");
    }

    public Long createNewUser(UserRequest userRequest) {

        User user = new User();
        user.setNickName(userRequest.getNickName());
        user.setName(userRequest.getName());
        user.setLastName(userRequest.getLastName());

        user = userRepository.save(user);

        return user.getId();
    }

    private User getFallbackUser() {
        User user = new User();
        user.setId(-1L);
        user.setNickName("");
        user.setName("");
        user.setLastName("");
        return user;
    }
}
