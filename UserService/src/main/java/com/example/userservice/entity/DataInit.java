package com.example.userservice.entity;

import com.example.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class DataInit implements CommandLineRunner {

    private final UserRepository userRepository;

    @Override
    public void run(String... args) {
        if (!userRepository.existsById(1L)) {
            userRepository.save(UserEntity.builder()
                    .id(1L)
                            .firstName("Admin")
                            .lastName("Admin")
                            .address("Aleea Moldoveanu")
                            .email("admin@admin.com")
                    .build());
        }
    }
}


