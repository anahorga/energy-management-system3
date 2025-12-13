package com.example.deviceservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MonitoringDeviceDto {

    private Long id;

    private Double consumption;

    private Long userId;
}
