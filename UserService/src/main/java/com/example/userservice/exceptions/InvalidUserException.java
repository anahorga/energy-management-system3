package com.example.userservice.exceptions;


public class InvalidUserException extends RuntimeException{
    public InvalidUserException(String errs){
        super(errs);
    }
}
