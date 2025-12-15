package com.example.monitoringdevice.service;

import com.example.monitoringdevice.dto.MonitoringDto;
import com.example.monitoringdevice.dto.NotificationDto;
import com.example.monitoringdevice.entity.DeviceEntity;
import com.example.monitoringdevice.entity.MonitoringEntity;
import com.example.monitoringdevice.exceptions.DeviceNotFoundException;
import com.example.monitoringdevice.mapper.MonitoringMapper;
import com.example.monitoringdevice.rabbit.RabbitConfig;
import com.example.monitoringdevice.repository.DeviceRepository;
import com.example.monitoringdevice.repository.MonitoringRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MonitoringListener {

    private final DeviceRepository deviceRepository;
    private final MonitoringRepository monitoringRepository;
    private final MonitoringMapper monitoringMapper;
    private final RabbitTemplate rabbitTemplate;



    @RabbitListener(queues = "${app.monitoring.queue}")
    public void handleMessage(MonitoringDto dto) {
        try {
            MonitoringEntity monitoringEntity = monitoringMapper.monitoringDtoToMonitoringEntity(dto);
            Long deviceId = monitoringEntity.getDevice().getId();

            // <--- 2. Aducem Device-ul din DB pentru a vedea limita (consumption) și userId-ul
            DeviceEntity device = deviceRepository.findById(deviceId)
                    .orElseThrow(() -> new DeviceNotFoundException("Device with id " + deviceId + " not found"));

            // Setăm ora fixă (00:00)
            LocalDateTime time = monitoringEntity.getTimestamp().withMinute(0).withSecond(0).withNano(0);
            monitoringEntity.setTimestamp(time);

            Optional<MonitoringEntity> existing = Optional.ofNullable(
                    monitoringRepository.findByDeviceIdAndTimestamp(deviceId, time)
            );

            double currentTotalConsumption; // Variabilă pentru a ține valoarea finală a orei curente

            if (existing.isPresent()) {
                MonitoringEntity found = existing.get();
                Double aux = monitoringEntity.getConsumption();
                aux += found.getConsumption();

                // Rotunjire
                aux = BigDecimal.valueOf(aux).setScale(2, RoundingMode.HALF_UP).doubleValue();

                found.setConsumption(aux);
                monitoringRepository.save(found);

                currentTotalConsumption = aux; // Salvăm valoarea actualizată
            } else {
                monitoringRepository.save(monitoringEntity);
                currentTotalConsumption = monitoringEntity.getConsumption(); // Salvăm valoarea inițială
            }

            if (currentTotalConsumption > device.getConsumption()) {
                System.out.println("ALERT: Device " + deviceId + " exceeded limit! Current: " + currentTotalConsumption + ", Max: " + device.getConsumption());

                NotificationDto notification = NotificationDto.builder()
                        .message("Device-ul " + device.getId() + " a depășit consumul maxim! Valoare curentă: " + currentTotalConsumption + " kW")
                        .userId(device.getUserId())
                        .deviceId(device.getId())
                        .build();

                rabbitTemplate.convertAndSend(RabbitConfig.EXCHANGE_NAME, "overconsumption.notification", notification);
            }

        } catch (Exception e) {
            System.err.println("[MONITORING] Eroare la procesarea mesajului: ");
            e.printStackTrace();
        }
    }
}