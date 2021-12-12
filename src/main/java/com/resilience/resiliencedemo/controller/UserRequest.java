package com.resilience.resiliencedemo.controller;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

@Data
public class UserRequest {

    @NotEmpty
    private String nickName;

    @NotEmpty
    @Size(max = 20)
    private String name;

    @NotEmpty
    private String lastName;
};