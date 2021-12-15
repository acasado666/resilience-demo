package com.resilience.resiliencedemo.service;

import com.resilience.resiliencedemo.model.User;
import com.resilience.resiliencedemo.repository.UserRepository;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Autowired
    UserService userService;

    @Mock
    private UserRepository userRepository;


    private User createUser(Long id, String nickName, String name, String lastName) {
        User user = new User();
        user.setNickName(nickName);
        user.setName(name);
        user.setLastName(lastName);
        user.setId(id);
        return user;
    }
}