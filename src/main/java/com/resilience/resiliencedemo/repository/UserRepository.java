package com.resilience.resiliencedemo.repository;

import com.resilience.resiliencedemo.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
}
