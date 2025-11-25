package com.example.monitoringdevice.service;


import com.example.monitoringdevice.dto.DeviceDto;
import com.example.monitoringdevice.rabbit.RabbitConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DeviceSyncListener {

    private final MonitoringService monitoringService;

    @RabbitListener(queues = RabbitConfig.MONITORING_SYNC_QUEUE)
    public void syncDevice(DeviceDto event, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String key) {
        System.out.println(" [Monitoring] Received event: " + key + " -> Device ID: " + event.getId());

        try {
            if ("device.insert".equals(key)) {

                monitoringService.saveDevice(event);
                System.out.println(" [Monitoring] Device synced successfully.");
            }
            else if ("device.delete".equals(key)) {
                monitoringService.deleteDevice(event.getId());
                System.out.println(" [Monitoring] Device deleted successfully.");
            }
        } catch (Exception e) {
            System.err.println(" [Monitoring] Failed to process sync event: " + e.getMessage());

        }
    }
}
