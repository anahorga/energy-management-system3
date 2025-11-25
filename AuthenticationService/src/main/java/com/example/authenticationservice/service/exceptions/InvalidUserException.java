package com.example.authenticationservice.service.exceptions;

public class InvalidUserException extends RuntimeException{
    public InvalidUserException(String errs){
        super(errs);
    }
}
