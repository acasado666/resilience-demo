package com.resilience.resiliencedemo.controller;

import com.resilience.resiliencedemo.model.User;
import com.resilience.resiliencedemo.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping
    public ResponseEntity<Void> createNewUser(@Valid @RequestBody UserRequest userRequest, UriComponentsBuilder uriComponentsBuilder) {
        Long primaryKey = userService.createNewUser(userRequest);

        UriComponents uriComponents = uriComponentsBuilder.path("/api/users/{id}").buildAndExpand(primaryKey);
        HttpHeaders headers = new HttpHeaders();
        headers.setLocation(uriComponents.toUri());

        return new ResponseEntity<Void>(headers, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    @GetMapping("/{id}")
    public ResponseEntity<User> getUserById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @GetMapping("/cache/{id}")
    public ResponseEntity<User> getUserByIdCache(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.getUserByIdCache(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(@PathVariable("id") Long id, @Valid @RequestBody UserRequest userRequest) {
        return ResponseEntity.ok(userService.updateUser(id, userRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable("id") Long id) {
        userService.deleteUserById(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/retry/{id}")
    public ResponseEntity<User> serviceRetry(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.externalServiceRetry(id));
    }

    @GetMapping("/circuitbreaker/{id}")
    public ResponseEntity<User> serviceCircuitBreaker(@PathVariable("id") Long id) {
        return ResponseEntity.ok(userService.externalServiceCircuitBreaker(id));
    }

    @GetMapping("/ratelimiter")
    public String serviceRateLimiter() {
        return userService.ratelimiter();
    }

    @GetMapping("/timelimiter")
    public CompletableFuture<String> futureSuccess(){
        return userService.futureSuccess();
    }

    @GetMapping("/bulkhead")
    public User externalServiceBulkhead() {
        return userService.externalServiceBulkhead();
    }
}
