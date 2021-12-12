package com.resilience.resiliencedemo.service;

import com.resilience.resiliencedemo.controller.UserNotFoundException;
import com.resilience.resiliencedemo.controller.UserRequest;
import com.resilience.resiliencedemo.model.User;
import com.resilience.resiliencedemo.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public Long createNewUser(UserRequest userRequest) {

        User user = new User();
        user.setNickName(userRequest.getNickName());
        user.setName(userRequest.getName());
        user.setLastName(userRequest.getLastName());

        user = userRepository.save(user);

        return user.getId();
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    @CircuitBreaker(name = "tgService", fallbackMethod = "userCircuitBreakerFallback")
    @Retry(name = "tgServiceRetry", fallbackMethod = "userRetryFallback")
    public User getUserById(Long id) {
        Optional<User> requestedUser = userRepository.findById(id);

        if (requestedUser.isEmpty()) {
            throw new UserNotFoundException(String.format("User with id: '%s' not found", id));
        }

        return requestedUser.get();
    }

    @Cacheable(cacheNames = "cdpList", key = "#id")
    public User getUserByIdCache(Long id) {
        Optional<User> requestedUser = userRepository.findById(id);

        if (requestedUser.isEmpty()) {
            throw new UserNotFoundException(String.format("User with id: '%s' not found", id));
        }

        return requestedUser.get();
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

    /**
     * Circuit breaker activation in case of failure
     */
    public String userCircuitBreakerFallback(Throwable t) {
        return "Sorry ... Service not available!!!";
    }

    /**
     * Retry mechanism fallback
     */
    public String userRetryFallback(Throwable t) {
        return "Sorry ... Service not available after Retry mechanism!!!";
    }
}
