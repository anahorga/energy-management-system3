package com.example.deviceservice.mapper;

import com.example.deviceservice.dto.DeviceDto;
import com.example.deviceservice.entity.DeviceEntity;
import org.mapstruct.*;

import java.util.List;

@Mapper(componentModel="spring",uses = {UserMapper.class})
public interface DeviceMapper {


    DeviceDto deviceEntityToDeviceDto(DeviceEntity deviceEntity);
    List<DeviceDto> deviceEntityToDeviceDto(List<DeviceEntity> deviceEntity);

    DeviceEntity deviceDtoToDeviceEntity( DeviceDto deviceDto);
    List<DeviceEntity> deviceDtoToDeviceEntity(List<DeviceDto> deviceDto);


    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", ignore = true)
    void updateEntityFromDto(DeviceDto dto, @MappingTarget DeviceEntity entity);
}
