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

    // Ascultă exact coada definită în simulator: "device_measurements"
    @RabbitListener(queues = "device_measurements")
    public void distributeMessage(Message messageRaw) {
        String messageJson = "";
        try {
            // 1. Convertim byte[] la String
            messageJson = new String(messageRaw.getBody(), StandardCharsets.UTF_8);
            System.out.println(" [LB] Received JSON: " + messageJson);

            // 2. Parsăm JSON-ul
            JsonNode root = objectMapper.readTree(messageJson);

            // 3. Extragem ID-ul conform structurii din Python script
            // Python trimite: "device": { "id": 123 }
            long deviceId;

            if (root.has("device") && root.get("device").has("id")) {
                deviceId = root.get("device").get("id").asLong();
            } else {
                System.err.println(" [LB] ERROR: JSON format incorrect. Expected 'device.id'. Received: " + messageJson);
                return; // Nu putem procesa fără ID
            }

            // 4. Algoritmul de Hashing (replicas = 4)
            int replicaIndex = (int) (deviceId % 4);
            String targetQueue = "q.monitoring." + replicaIndex;

            // 5. Trimitem mesajul mai departe la coada specifică
            System.out.println(" [LB] Routing Device " + deviceId + " -> " + targetQueue);
            rabbitTemplate.convertAndSend(targetQueue, messageJson);

        } catch (Exception e) {
            System.err.println(" [LB] EXCEPTION: " + e.getMessage());
            e.printStackTrace();
        }
    }
}