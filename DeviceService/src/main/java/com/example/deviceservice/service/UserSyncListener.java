package com.example.deviceservice.service;


import com.example.deviceservice.rabbit.RabbitMQConfig;
import com.example.deviceservice.dto.UserDto;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserSyncListener {

    private final DeviceService deviceService;

    @RabbitListener(queues = RabbitMQConfig.DEVICE_SERVICE_QUEUE)
    public void consumeUserEvent(UserDto event, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String key) {
        System.out.println(" [DeviceService] Received event: " + key + " for User ID: " + event.getId());

        try {
            if ("user.insert".equals(key)) {

                deviceService.saveUser(event);
                System.out.println(" [DeviceService] User ID synced successfully.");
            }
            else if ("user.delete".equals(key)) {
                deviceService.deleteUser(event.getId());
                System.out.println(" [DeviceService] User ID deleted successfully.");
            }
        } catch (Exception e) {
            System.err.println(" [DeviceService] Failed to sync user: " + e.getMessage());
        }
    }
}