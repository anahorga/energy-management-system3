package com.example.monitoringdevice.service;


import com.example.monitoringdevice.dto.MonitoringDto;
import com.example.monitoringdevice.entity.MonitoringEntity;
import com.example.monitoringdevice.exceptions.DeviceNotFoundException;
import com.example.monitoringdevice.mapper.MonitoringMapper;
import com.example.monitoringdevice.rabbit.RabbitConfig;
import com.example.monitoringdevice.repository.DeviceRepository;
import com.example.monitoringdevice.repository.MonitoringRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonitoringListener {

    private final ObjectMapper objectMapper;

    private final DeviceRepository deviceRepository;

    private final MonitoringRepository monitoringRepository;

    private final MonitoringMapper monitoringMapper;

    @RabbitListener(queues = RabbitConfig.DEVICE_MEASUREMENTS_QUEUE)
    public void handleMessage(MonitoringDto dto) {
        try {

           // System.out.println("[MONITORING] Am primit mesaj:");
           // System.out.println("  timestamp = " + dto.getTimestamp());
            //System.out.println("  consumption = " + dto.getConsumption());
            //System.out.println("  device.id = " + (dto.getDevice() != null ? dto.getDevice().getId() : null));
            MonitoringEntity monitoringEntity=monitoringMapper.monitoringDtoToMonitoringEntity(dto);

            if(!deviceRepository.existsById(monitoringEntity.getDevice().getId()))
                throw new DeviceNotFoundException("Device with id "+monitoringEntity.getDevice().getId()+"not found");
            LocalDateTime time=monitoringEntity.getTimestamp().withMinute(0).withSecond(0).withNano(0);//cast all minutes to 00:00

            monitoringEntity.setTimestamp(time);
            Optional<MonitoringEntity> existing =
                    Optional.ofNullable(monitoringRepository.findByDeviceIdAndTimestamp(
                            monitoringEntity.getDevice().getId(),
                            monitoringEntity.getTimestamp()
                    ));

            if (existing.isPresent()) {
                MonitoringEntity found = existing.get();
                Double aux = monitoringEntity.getConsumption();
                aux += found.getConsumption();
                aux = BigDecimal.valueOf(aux).setScale(2, RoundingMode.HALF_UP).doubleValue();
                found.setConsumption(aux);
                monitoringRepository.save(found);

            } else {
                monitoringRepository.save(monitoringEntity);
            }

        } catch (Exception e) {
            System.err.println("[MONITORING] Eroare la parsarea mesajului JSON: " );
            e.printStackTrace();
        }
    }
}
