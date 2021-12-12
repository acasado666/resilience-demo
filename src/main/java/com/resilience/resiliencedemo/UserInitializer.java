package com.resilience.resiliencedemo;

import com.resilience.resiliencedemo.model.User;
import com.resilience.resiliencedemo.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UserInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {

        log.info("Starting book initialization ...");

        User user = new User();
        user.setId(1L);
        user.setNickName("walking-smart");
        user.setName("Jonathan");
        user.setLastName("Kaleve");

        userRepository.save(user);

        User user1 = new User();
        user1.setId(2L);
        user1.setNickName("master-commander");
        user1.setName("Rafael");
        user1.setLastName("Kansy");

        userRepository.save(user1);

        log.info("... finished book initialization");
//
//        User user2 = new User();
//        user2.setId(3L);
//        user2.setNickName("always-cold");
//        user2.setName("Antonio");
//        user2.setLastName("Casado");
//
//        userRepository.save(user2);

    }
}
