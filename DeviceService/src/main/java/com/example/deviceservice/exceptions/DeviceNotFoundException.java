package com.example.deviceservice.exceptions;

public class DeviceNotFoundException extends RuntimeException{
    public DeviceNotFoundException(String errs){
        super(errs);
    }
}
