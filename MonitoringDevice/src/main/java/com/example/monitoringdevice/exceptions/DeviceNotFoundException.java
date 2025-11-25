package com.example.monitoringdevice.exceptions;

public class DeviceNotFoundException extends RuntimeException{
    public DeviceNotFoundException(String errs){
        super(errs);
    }
}
