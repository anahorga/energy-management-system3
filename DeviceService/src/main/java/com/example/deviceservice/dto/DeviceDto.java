package com.example.deviceservice.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DeviceDto {

    private Long id;

    private String name;
    private Double consumption;

    private UserDto user;

}
