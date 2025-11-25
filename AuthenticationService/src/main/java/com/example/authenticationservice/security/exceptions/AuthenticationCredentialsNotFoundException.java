package com.example.authenticationservice.security.exceptions;

public class AuthenticationCredentialsNotFoundException extends RuntimeException{
    public AuthenticationCredentialsNotFoundException(final String message){
        super(message);
    }
}
