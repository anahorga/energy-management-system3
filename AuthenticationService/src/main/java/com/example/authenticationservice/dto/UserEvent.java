package com.example.authenticationservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserEvent {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private String address;
}
