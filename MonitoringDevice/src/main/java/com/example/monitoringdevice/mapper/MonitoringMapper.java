package com.example.monitoringdevice.mapper;

import com.example.monitoringdevice.dto.MonitoringDto;
import com.example.monitoringdevice.entity.MonitoringEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel="spring",uses = {DeviceMapper.class})
public interface MonitoringMapper {

    MonitoringDto monitoringEntityToMonitoringDto(MonitoringEntity monitoringEntity);
    List<MonitoringDto>monitoringEntityToMonitoringDto(List<MonitoringEntity>monitoringEntity);

    MonitoringEntity monitoringDtoToMonitoringEntity (MonitoringDto monitoringDto);
    List<MonitoringEntity>monitoringDtoToMonitoringEntity(List<MonitoringDto>monitoringDto);

}
