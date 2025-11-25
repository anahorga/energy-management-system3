package com.example.userservice.dto;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;


@Data
public class UserDto {

    private Long id;

    @Pattern(regexp = "^[A-Za-z]+(?:[ -][A-Za-z]+)*$", message = "First name must be in the format Alex, Alex Pop or Alex-Pop ")
    @NotNull(message = "The first name must not be null")
    private String firstName;

    @Pattern(regexp = "^[A-Za-z]+(?:[ -][A-Za-z]+)*$", message = "Last name must be in the format Alex, Alex Pop or Alex-Pop")
    @NotNull(message = "The last name must not be null")
    private String lastName;

    @Pattern(regexp = "^[A-Za-z]+(?:[ -][A-Za-z]+)*$")
    @NotNull(message = "The address must not be null")
    private String address;

    @Email
    @NotNull(message = "The email must not be null")
    private String email;
}
