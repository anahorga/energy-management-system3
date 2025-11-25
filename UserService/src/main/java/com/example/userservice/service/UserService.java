package com.example.userservice.service;


import com.example.userservice.dto.UserDto;
import com.example.userservice.entity.UserEntity;
import com.example.userservice.exceptions.InvalidUserException;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.mapper.UserMapper;
import com.example.userservice.repository.UserRepository;
import com.example.userservice.validator.UserValidator;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
   private final UserRepository userRepository;
   private final UserMapper userMapper;
   private final UserValidator userValidator;

   public List<UserDto> findAllUsers(){
       return userMapper.userEntityToUserDto(userRepository.findAll());
   }

   public UserDto saveUser(UserDto userDto)
   {
       UserEntity userEntity = userMapper.userDtoToUserEntity(userDto);

       String errs = userValidator.validate(userEntity);
       if (!errs.isEmpty()) {
           throw new InvalidUserException(errs);
       }
       return userMapper.userEntityToUserDto(userRepository.save(userEntity));
   }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        userRepository.deleteById(id);
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto userDto)
    {
        UserEntity userEntity=userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User with id " + id + " not found"));

        userMapper.updateEntityFromDto(userDto, userEntity);
        String errs = userValidator.validate(userEntity);
        if (!errs.isEmpty()) {
            throw new InvalidUserException(errs);
        }
        return userMapper.userEntityToUserDto(userEntity);
    }
}
