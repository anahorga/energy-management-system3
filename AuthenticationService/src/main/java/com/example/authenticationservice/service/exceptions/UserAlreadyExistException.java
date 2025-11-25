package com.example.authenticationservice.service.exceptions;

public class UserAlreadyExistException extends RuntimeException{
    public UserAlreadyExistException(){
        super("User already exists!");
    }
}
