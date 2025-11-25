package com.example.authenticationservice.security.exceptions;

public class JwtAuthenticationException extends RuntimeException{
    public JwtAuthenticationException(final Exception ex){
        super(ex);
    }
}
