package com.example.deviceservice.validator;


import com.example.deviceservice.entity.DeviceEntity;
import com.example.deviceservice.entity.UserEntity;
import org.springframework.stereotype.Component;



@Component
public class DeviceValidator {


    public String validate(DeviceEntity device){
        StringBuilder errors = new StringBuilder();
        errors
                .append(validateName(device))
                .append(validateUser(device.getUser()));

        if(!errors.isEmpty()){
            return errors.toString();
        }
        return "";
    }


    private String validateName(DeviceEntity device){

        if(device.getName()==null || device.getName().isEmpty()){
            return " Name is required! ";
        }
        return "";
    }
    private String validateUser(UserEntity user){
        if(user==null || user.getId()==null){
            return "User is required! ";
        }

        return "";
    }

}

