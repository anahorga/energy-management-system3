package com.example.authenticationservice.dto;

import com.example.authenticationservice.entity.UserRole;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;

import jakarta.validation.constraints.Email;


@Builder
public record RegisterRequest(
        @NotBlank String username,
        @NotBlank String password,
        UserRole userRole,
        @NotBlank String firstName,
        @NotBlank String lastName,
        @NotBlank String address,
        @Email String email
) {
}
