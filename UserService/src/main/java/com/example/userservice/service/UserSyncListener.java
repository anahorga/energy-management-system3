package com.example.userservice.service;

import com.example.userservice.dto.UserDto;

import com.example.userservice.rabbit.RabbitMQConfig;
import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;



import org.springframework.amqp.support.AmqpHeaders;
import org.springframework.messaging.handler.annotation.Header;

@Component
@RequiredArgsConstructor
public class UserSyncListener {

    private final UserService userService;

    @RabbitListener(queues = RabbitMQConfig.USER_SERVICE_QUEUE)
    public void consumeMessage(UserDto event, @Header(AmqpHeaders.RECEIVED_ROUTING_KEY) String key) {
        System.out.println(" [UserService] Received event: " + key + " for ID: " + event.getId());

        try {
            if ("user.insert".equals(key)) {

                userService.saveUser(event);
                System.out.println(" [UserService] User synced successfully.");
            }
            else if ("user.delete".equals(key)) {
                userService.deleteUser(event.getId());
                System.out.println(" [UserService] User deleted successfully.");
            }
        } catch (Exception e) {
            System.err.println(" [UserService] Failed to process event: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
