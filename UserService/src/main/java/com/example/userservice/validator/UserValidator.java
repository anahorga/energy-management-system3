package com.example.userservice.validator;

import com.example.userservice.entity.UserEntity;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class UserValidator {

    public String validate(UserEntity user){
        StringBuilder errors = new StringBuilder();
        errors
                .append(validateName(user))
                .append(validateEmail(user))
                .append(validateAddress(user));
        if(!errors.isEmpty()){
            return errors.toString();
        }
        return "";
    }


    private String validateName(UserEntity user){
        if(user.getFirstName()==null || user.getFirstName().isEmpty()){
            return "First name is required! ";
        }
        if(user.getLastName()==null || user.getLastName().isEmpty()){
            return "Last name is required! ";
        }
        return "";
    }
    private String validateAddress(UserEntity user){
        if(user.getAddress()==null || user.getAddress().isEmpty()){
            return "Address is required! ";
        }

        return "";
    }

    private String validateEmail(UserEntity user){
        if(user.getEmail()==null || user.getEmail().isEmpty()){
            return "Email is required! ";
        }
        Pattern pattern = Pattern.compile("[a-zA-z][a-zA-Z0-9._-]*@[a-zA-z]+\\.[a-zA-Z.]+");
        Matcher matcher = pattern.matcher(user.getEmail());
        if(!matcher.matches()){
            return "Invalid email! ";
        }
        return "";
    }
}
