package com.example.monitoringdevice.service;

import com.example.monitoringdevice.dto.DeviceDto;
import com.example.monitoringdevice.dto.MonitoringDto;
import com.example.monitoringdevice.entity.DeviceEntity;
import com.example.monitoringdevice.entity.MonitoringEntity;
import com.example.monitoringdevice.exceptions.DeviceNotFoundException;
import com.example.monitoringdevice.mapper.DeviceMapper;
import com.example.monitoringdevice.mapper.MonitoringMapper;
import com.example.monitoringdevice.repository.DeviceRepository;
import com.example.monitoringdevice.repository.MonitoringRepository;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MonitoringService {

        public final DeviceMapper deviceMapper;
    private  final DeviceRepository deviceRepository;
    private final MonitoringRepository monitoringRepository;
    private final MonitoringMapper monitoringMapper;

    public DeviceDto saveDevice(DeviceDto deviceDto)
    {
        DeviceEntity deviceEntity = deviceMapper.deviceDtoToDeviceEntity(deviceDto);

        if(deviceEntity.getId()==null)
            throw new IllegalArgumentException("Device id must be provided (no auto-generation).");
        if(deviceRepository.existsById(deviceEntity.getId()))
            throw new DuplicateKeyException("Device with id " + deviceEntity.getId() + " already exists");

        return deviceMapper.deviceEntityToDeviceDto(deviceRepository.save(deviceEntity));
    }
    public void deleteDevice(Long id) {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException("Device with id " + id + " not found");
        }
        deviceRepository.deleteById(id);
    }
    @Transactional(readOnly = true)
    public List<MonitoringDto> getAllByDeviceIdAndTimestamp(Long id, LocalDate day)
    {
        if (!deviceRepository.existsById(id)) {
            throw new DeviceNotFoundException("Device with id " + id + " not found");
        }

        LocalDateTime start = day.atStartOfDay();
        LocalDateTime end = day.plusDays(1).atStartOfDay();

        List<MonitoringEntity> monitoring=monitoringRepository.findByDeviceIdAndTimestampBetweenOrderByTimestampAsc(id,start,end);
        if(monitoring.isEmpty())
            throw new RuntimeException("No monitoring found for selected date");
        else
            return monitoringMapper.monitoringEntityToMonitoringDto(monitoring);

    }

    public void updateDevice(DeviceDto deviceDTO) {
        DeviceEntity existingDevice = deviceRepository.findById(deviceDTO.getId())
                .orElseThrow(() -> new RuntimeException("Device doesn't exist"));

        existingDevice.setConsumption(deviceDTO.getConsumption());
        existingDevice.setUserId(deviceDTO.getUserId());

        deviceRepository.save(existingDevice);
    }
}
