package com.example.deviceservice.controller;


import com.example.deviceservice.dto.DeviceDto;
import com.example.deviceservice.dto.UserDto;
import com.example.deviceservice.exceptions.DeviceNotFoundException;
import com.example.deviceservice.exceptions.InvalidDeviceException;
import com.example.deviceservice.exceptions.UserNotFoundException;
import com.example.deviceservice.service.DeviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/devices")
public class DeviceController {

    private final DeviceService deviceService;


    @GetMapping
    public ResponseEntity<List<DeviceDto>> findAllDevices() {
        List<DeviceDto> devices = deviceService.findAll();
        return ResponseEntity.ok(devices);
    }


    @PostMapping
    public ResponseEntity<?> save(@RequestBody DeviceDto devicedto) {

       try{
           return ResponseEntity.ok(deviceService.saveDevice(devicedto));
       }
       catch( RuntimeException e)
       {
           return ResponseEntity
                   .badRequest()
                   .body(Map.of("error", e.getMessage()));
       }

    }
    @DeleteMapping("/user/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id){
        try {
            deviceService.deleteUser(id);
            return ResponseEntity.ok().build();
        }catch(UserNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @PostMapping("/user")
    public ResponseEntity<?> saveUser(@RequestBody UserDto user) {
        try {
            return ResponseEntity.ok(deviceService.saveUser(user));
        } catch (DuplicateKeyException|IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }


    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id,@RequestBody  DeviceDto device) {
        try {
            return ResponseEntity.ok(deviceService.updateDevice(device,id));
        } catch (DeviceNotFoundException | InvalidDeviceException|UserNotFoundException e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id){
        try {
            deviceService.deleteDevice(id);
            return ResponseEntity.ok().build();
        }catch(DeviceNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
    @GetMapping ("/{id}")
    public ResponseEntity<?> getDevicesByUserId(@PathVariable Long id){
        try {

            return ResponseEntity.ok(deviceService.findByUserId(id));
        }catch(UserNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

}
