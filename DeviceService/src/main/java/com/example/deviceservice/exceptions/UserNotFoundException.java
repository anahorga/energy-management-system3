package com.example.deviceservice.exceptions;

public class UserNotFoundException extends RuntimeException{
    public UserNotFoundException(String errs){
        super(errs);
    }
}
