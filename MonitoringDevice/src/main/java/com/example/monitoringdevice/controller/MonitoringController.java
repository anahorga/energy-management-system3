package com.example.monitoringdevice.controller;

import com.example.monitoringdevice.dto.DeviceDto;
import com.example.monitoringdevice.exceptions.DeviceNotFoundException;
import com.example.monitoringdevice.service.MonitoringService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/monitoring")
public class MonitoringController {

    private final MonitoringService monitoringService;

    @PostMapping("/device")
    public ResponseEntity<?> saveDevice(@RequestBody DeviceDto device) {
        try {
            return ResponseEntity.ok(monitoringService.saveDevice(device));
        } catch (DuplicateKeyException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        }
    }
    @DeleteMapping("/device/{id}")
    public ResponseEntity<?> deleteDevice(@PathVariable Long id){
        try {
            monitoringService.deleteDevice(id);
            return ResponseEntity.ok().build();
        }catch(DeviceNotFoundException e){
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> getAll(
            @RequestParam("deviceId") Long deviceId,
            @RequestParam("day") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate day
    ) {
        try {
            // Trimitem parametrii direct la service
            return ResponseEntity.ok(monitoringService.getAllByDeviceIdAndTimestamp(deviceId, day));
        } catch (Exception e) {
            return ResponseEntity
                    .status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
