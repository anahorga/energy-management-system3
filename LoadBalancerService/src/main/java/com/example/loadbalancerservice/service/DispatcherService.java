package com.example.loadbalancerservice.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class DispatcherService {

    private final RabbitTemplate rabbitTemplate;
    private final ObjectMapper objectMapper;

    @RabbitListener(queues = "device_measurements")
    public void distributeMessage(Message messageRaw) {
        try {
            // Convertim byte[] la String
            String messageJson = new String(messageRaw.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [LB] Received JSON: " + messageJson);

            // Parsăm JSON-ul DOAR pentru a extrage device ID
            JsonNode root = objectMapper.readTree(messageJson);

            // Extragem ID-ul
            long deviceId;
            if (root.has("device") && root.get("device").has("id")) {
                deviceId = root.get("device").get("id").asLong();
            } else {
                System.err.println(" [LB] ERROR: JSON format incorrect. Expected 'device.id'. Received: " + messageJson);
                return;
            }

            // Algoritmul de Hashing (replicas = 4)
            int replicaIndex = (int) (deviceId % 4);
            String targetQueue = "q.monitoring." + replicaIndex;

            System.out.println(" [LB] Routing Device " + deviceId + " -> " + targetQueue);

            // IMPORTANT: Trimitem mesajul ORIGINAL, exact cum a venit
            // NU îl reconvertim - trimitem Message-ul direct
            rabbitTemplate.send(targetQueue, messageRaw);

            System.out.println(" [LB] Message successfully routed to " + targetQueue);

        } catch (Exception e) {
            System.err.println(" [LB] EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}