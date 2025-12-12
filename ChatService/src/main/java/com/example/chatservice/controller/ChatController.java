package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatMessage;
import com.example.chatservice.service.AiService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final AiService aiService; // Injectăm serviciul AI

    // 1. PĂSTRĂM REGULILE TALE
    private static final Map<String, String> RULES = new HashMap<>();
    static {
        RULES.put("hello", "Hello! I am RobBot. How can I assist you today?");
        RULES.put("buna", "Salut! Sunt RobBot. Cu ce te pot ajuta?");
        RULES.put("consumption", "You can verify your energy consumption on the dashboard charts.");
        RULES.put("device", "Your smart devices are listed in the main dashboard.");
        RULES.put("login", "Please login using your secure credentials.");
        RULES.put("register", "New accounts can be created on the registration page.");
        RULES.put("admin", "If you need human assistance, please switch to the Admin tab.");
        RULES.put("hour", "Hourly stats are available by selecting a specific device.");
        RULES.put("thank", "You are welcome! Let me know if you need anything else.");
        RULES.put("bye", "Goodbye! Have a great day!");
    }

    @MessageMapping("/chat/bot")
    public void processBotMessage(@Payload ChatMessage message) {
        String destination = "/topic/bot." + message.getSenderId();

        // Echo la mesajul utilizatorului
        messagingTemplate.convertAndSend(destination, message);

        if ("System".equals(message.getSenderId()) || "RobBot".equals(message.getSenderId())) {
            return;
        }

        // 2. LOGICĂ HIBRIDĂ: Întâi căutăm în reguli
        String responseContent = null;
        String lowerContent = message.getContent().toLowerCase();

        for (Map.Entry<String, String> entry : RULES.entrySet()) {
            if (lowerContent.contains(entry.getKey())) {
                responseContent = entry.getValue();
                break;
            }
        }

        // 3. Dacă am găsit o regulă, răspundem imediat (fără AI)
        if (responseContent != null) {
            sendRobBotResponse(destination, responseContent);
        } else {
            // 4. Dacă NU am găsit regulă, apelăm AI-ul (asincron)
            new Thread(() -> {
                String aiResponse = aiService.generateResponse(message.getContent());
                sendRobBotResponse(destination, aiResponse);
            }).start();
        }
    }

    private void sendRobBotResponse(String destination, String content) {
        // Simulăm un mic delay pentru naturalețe (opțional)
        try { Thread.sleep(300); } catch (InterruptedException e) {}

        ChatMessage response = ChatMessage.builder()
                .senderId("RobBot")
                .content(content)
                .isAdmin(false)
                .build();
        messagingTemplate.convertAndSend(destination, response);
    }

    @MessageMapping("/chat/admin")
    public void processAdminMessage(@Payload ChatMessage message) {
        messagingTemplate.convertAndSend("/topic/admin", message);
    }
}