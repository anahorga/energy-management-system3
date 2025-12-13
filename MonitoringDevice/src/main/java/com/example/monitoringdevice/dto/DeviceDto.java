package com.example.monitoringdevice.dto;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceDto {
    private Long id;

    private Double consumption;

    private Long userId;
}
