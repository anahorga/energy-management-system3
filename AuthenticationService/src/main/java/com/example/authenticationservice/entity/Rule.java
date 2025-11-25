package com.example.authenticationservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class Rule {
    private String httpMethod;
    private String pathPattern;
    private Set<UserRole> allowed;
}
