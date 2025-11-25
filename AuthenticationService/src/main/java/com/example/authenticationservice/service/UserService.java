package com.example.authenticationservice.service;

import com.example.authenticationservice.dto.LoginRequest;
import com.example.authenticationservice.dto.RegisterRequest;
import com.example.authenticationservice.dto.UserDto;
import com.example.authenticationservice.dto.UserEvent;
import com.example.authenticationservice.entity.UserEntity;
import com.example.authenticationservice.entity.UserRole;
import com.example.authenticationservice.mapper.UserMapper;
import com.example.authenticationservice.rabbit.RabbitMQConfig;
import com.example.authenticationservice.repository.UserRepository;
import com.example.authenticationservice.service.exceptions.InvalidUserException;
import com.example.authenticationservice.service.exceptions.UserAlreadyExistException;
import com.example.authenticationservice.service.exceptions.UserNotAuthorizedException;
import com.example.authenticationservice.service.exceptions.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder encoder;
    private final UserValidator userValidator;
    private final RabbitTemplate rabbitTemplate;

    public Long register(RegisterRequest registerRequest) {
        UserEntity user = UserEntity.builder()
                .username(registerRequest.username())
                .password(encoder.encode(registerRequest.password()))
                .userRole(UserRole.USER)
                .build();


        String errs = userValidator.validate(user);
        if (!errs.isEmpty()) {
            throw new InvalidUserException(errs);
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException();
        }
        UserEntity savedUser = userRepository.save(user);
        try {
            UserEvent event = UserEvent.builder()
                    .id(savedUser.getId())
                    .firstName(registerRequest.firstName())
                    .lastName(registerRequest.lastName())
                    .email(registerRequest.email())
                    .address(registerRequest.address())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.insert", event);

        } catch (Exception e) {
            System.err.println("Error sending RabbitMQ message: " + e.getMessage());
        }

        return savedUser.getId();
    }

    public UserDto login(LoginRequest loginRequest) {
        UserEntity user = userRepository.findByUsername(loginRequest.username());
        if (user != null && encoder.matches(loginRequest.password(), user.getPassword())) {
            userRepository.save(user);
            return userMapper.userEntityToUserDto(user);
        }
        return null;
    }
    public List<UserDto> getRegisteredUsers() {

        return userMapper.userEntityToUserDto(userRepository.findAll());
    }
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException("User with id " + id + " not found");
        }
        UserEvent event = UserEvent.builder()
                .id(id)
                .build();
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.delete", event);
        userRepository.deleteById(id);
    }
    public UserDto adminRegister(RegisterRequest registerRequest) {
        UserEntity user = UserEntity.builder()
                .username(registerRequest.username())
                .password(encoder.encode(registerRequest.password()))
                .userRole(registerRequest.userRole())
                .build();

        String errs = userValidator.validate(user);
        if (!errs.isEmpty()) {
            throw new InvalidUserException(errs);
        }
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new UserAlreadyExistException();
        }

        UserEntity savedUser = userRepository.save(user);
        try {
            UserEvent event = UserEvent.builder()
                    .id(savedUser.getId())
                    .firstName(registerRequest.firstName())
                    .lastName(registerRequest.lastName())
                    .email(registerRequest.email())
                    .address(registerRequest.address())
                    .build();

            rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE_NAME, "user.insert", event);

        } catch (Exception e) {
            System.err.println("Error sending RabbitMQ message: " + e.getMessage());
        }
        return userMapper.userEntityToUserDto(savedUser);
    }

}
