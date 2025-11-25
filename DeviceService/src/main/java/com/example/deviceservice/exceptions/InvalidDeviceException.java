package com.example.deviceservice.exceptions;

public class InvalidDeviceException extends RuntimeException{
    public InvalidDeviceException(String errs){
        super(errs);
    }
}