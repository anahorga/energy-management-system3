package com.example.monitoringdevice.mapper;

import com.example.monitoringdevice.dto.DeviceDto;
import com.example.monitoringdevice.entity.DeviceEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel="spring")
public interface DeviceMapper {

    DeviceDto deviceEntityToDeviceDto(DeviceEntity deviceEntity);
    List<DeviceDto> deviceEntityToDeviceDto(List<DeviceEntity>deviceEntity);

    List<DeviceEntity>deviceDtoToDeviceEntity(List<DeviceDto>deviceDto);
    DeviceEntity deviceDtoToDeviceEntity(DeviceDto deviceDto);
}
