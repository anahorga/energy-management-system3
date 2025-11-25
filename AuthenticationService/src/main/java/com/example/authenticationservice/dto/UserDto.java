package com.example.authenticationservice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import com.example.authenticationservice.entity.UserRole;

@Builder
public record UserDto(Long id, String username, UserRole userRole) {
}
