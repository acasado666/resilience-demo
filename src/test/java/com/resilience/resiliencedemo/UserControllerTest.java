package com.resilience.resiliencedemo;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.resilience.resiliencedemo.controller.UserController;
import com.resilience.resiliencedemo.controller.UserNotFoundException;
import com.resilience.resiliencedemo.controller.UserRequest;
import com.resilience.resiliencedemo.model.User;
import com.resilience.resiliencedemo.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(SpringExtension.class)
@WebMvcTest(UserController.class)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserService userService;

    @Captor
    private ArgumentCaptor<UserRequest> userRequestArgumentCaptor;

    @Test
    public void postingANewUsersShouldCreateANewUser() throws Exception {

        UserRequest userRequest = new UserRequest();
        userRequest.setNickName("Duke");
        userRequest.setName("Antonio");
        userRequest.setLastName("Casado");

        when(userService.createNewUser(userRequestArgumentCaptor.capture())).thenReturn(1L);

        this.mockMvc
                .perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(header().string("Location", "http://localhost/api/users/1"));

        assertThat(userRequestArgumentCaptor.getValue().getNickName(), is("Duke"));
        assertThat(userRequestArgumentCaptor.getValue().getName(), is("Antonio"));
        assertThat(userRequestArgumentCaptor.getValue().getLastName(), is("Casado"));

    }

    @Test
    public void allUsersEndpointShouldReturnTwoUsers() throws Exception {

        when(userService.getAllUsers()).thenReturn(List.of(
                createUser(1L, "Java 11", "Duke", "1337"),
                createUser(2L, "Java EE 8", "Duke", "1338")));

        this.mockMvc
                .perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].nickName", is("Java 11")))
                .andExpect(jsonPath("$[0].name", is("Duke")))
                .andExpect(jsonPath("$[0].lastName", is("1337")))
                .andExpect(jsonPath("$[0].id", is(1)));

    }

    @Test
    public void getUserWithIdOneShouldReturnAUser() throws Exception {

        when(userService.getUserById(1L)).thenReturn(createUser(1L, "Java 11", "Duke", "1337"));

        this.mockMvc
                .perform(get("/api/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.nickName", is("Java 11")))
                .andExpect(jsonPath("$.name", is("Duke")))
                .andExpect(jsonPath("$.lastName", is("1337")))
                .andExpect(jsonPath("$.id", is(1)));

    }

    @Test
    public void getUserWithUnknownIdShouldReturn404() throws Exception {

        when(userService.getUserById(1L)).thenThrow(new UserNotFoundException("Book with id '1' not found"));

        this.mockMvc
                .perform(get("/api/users/1"))
                .andExpect(status().isNotFound());

    }

    @Test
    public void updateUserWithKnownIdShouldUpdateTheUser() throws Exception {

        UserRequest userRequest = new UserRequest();
        userRequest.setNickName("Duke");
        userRequest.setName("Antonio");
        userRequest.setLastName("Casado");

        when(userService.updateUser(eq(1L), userRequestArgumentCaptor.capture()))
                .thenReturn(createUser(1L, "Duke", "Antonio", "Casado"));

        this.mockMvc
                .perform(put("/api/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"))
                .andExpect(jsonPath("$.nickName", is("Duke")))
                .andExpect(jsonPath("$.name", is("Antonio")))
                .andExpect(jsonPath("$.lastName", is("Casado")))
                .andExpect(jsonPath("$.id", is(1)));

        assertThat(userRequestArgumentCaptor.getValue().getNickName(), is("Duke"));
        assertThat(userRequestArgumentCaptor.getValue().getName(), is("Antonio"));
        assertThat(userRequestArgumentCaptor.getValue().getLastName(), is("Casado"));

    }

    @Test
    public void updateUserWithUnknownIdShouldReturn404() throws Exception {

        UserRequest userRequest = new UserRequest();
        userRequest.setNickName("Duke");
        userRequest.setName("Antonio");
        userRequest.setLastName("Casado");

        when(userService.updateUser(eq(42L), userRequestArgumentCaptor.capture()))
                .thenThrow(new UserNotFoundException("The book with id '42' was not found"));

        this.mockMvc
                .perform(put("/api/users/42")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(userRequest)))
                .andExpect(status().isNotFound());

    }

    private User createUser(Long id, String nickName, String name, String lastName) {
        User user = new User();
        user.setNickName(nickName);
        user.setName(name);
        user.setLastName(lastName);
        user.setId(id);
        return user;
    }

}