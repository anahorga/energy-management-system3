package com.example.deviceservice.entity;



import com.example.deviceservice.repository.UserRepository;
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
                    .build());
        }
    }
}

