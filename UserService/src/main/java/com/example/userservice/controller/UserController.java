package com.example.userservice.controller;

import com.example.userservice.dto.UserDto;
import com.example.userservice.exceptions.InvalidUserException;
import com.example.userservice.exceptions.UserNotFoundException;
import com.example.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    @GetMapping
    public ResponseEntity<List<UserDto>> findAllMembers() {
        List<UserDto> users = userService.findAllUsers();
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> saveUser(@RequestBody UserDto user){
        try {
            return ResponseEntity.ok(userService.saveUser(user));
        }catch (InvalidUserException e){
            return ResponseEntity
                    .badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok().build();
        }catch(UserNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PutMapping("/{id}")
   public ResponseEntity<?> update(@PathVariable Long id,@RequestBody  UserDto user) {
        try {
            userService.updateUser(id, user);
            return ResponseEntity.ok(userService.updateUser(id, user));
        } catch (UserNotFoundException | InvalidUserException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
     }

}