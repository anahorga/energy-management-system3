package com.example.chatservice.service;

import com.example.chatservice.dto.NotificationDto;
import com.example.chatservice.config.RabbitMQConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NotificationListener {

    private final SimpMessagingTemplate messagingTemplate;

    @RabbitListener(queues = RabbitMQConfig.NOTIFICATION_QUEUE)
    public void receiveNotification(NotificationDto notification) {
        System.out.println(" [ChatService] Received notification for User " + notification.getUserId() + ": " + notification.getMessage());

        // MODIFICARE AICI:
        // Înainte era: "/topic/bot." + notification.getUserId()
        // Acum trebuie să fie identic cu ce ai în React:
        messagingTemplate.convertAndSend(
                "/topic/user/" + notification.getUserId() + "/notifications",
                notification
        );
    }
}