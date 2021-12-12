package com.resilience.resiliencedemo.model;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

@Data
@Entity
public class User {

    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false, unique = true)
    private String nickName;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String lastName;

}