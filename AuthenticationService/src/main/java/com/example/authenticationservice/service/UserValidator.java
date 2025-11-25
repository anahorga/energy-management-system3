package com.example.authenticationservice.service;

import com.example.authenticationservice.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    public String validate(UserEntity user){
        StringBuilder errors = new StringBuilder();
        errors
                .append(validateUsername(user))
                .append(validatePassword(user));
        if(!errors.isEmpty()){
            return errors.toString();
        }
        return "";
    }
    private String validateUsername(UserEntity user){
        if(user.getUsername()==null || user.getUsername().isEmpty())
            return "Username is required! ";
        if(user.getUsername().length() < 4)
            return "Invalid username. Shoud be at least 4 characters! ";
        return "";
    }

    private String validatePassword(UserEntity user){
        if(user.getPassword()==null || user.getPassword().isEmpty())
            return "Password is required! ";

        if(user.getPassword().length() < 4)
            return "Invalid password. Shoud be at least 4 characters! ";

        return "";
    }

}
