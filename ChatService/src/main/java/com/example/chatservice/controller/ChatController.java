package com.example.chatservice.controller;

import com.example.chatservice.dto.ChatMessage;
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

    // Reguli simple pentru Robot (min 10 reguli pot fi adăugate aici)
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

    /* --- TAB-UL 1: Discuția cu Robotul --- */
    @MessageMapping("/chat/bot")
    public void processBotMessage(@Payload ChatMessage message) throws InterruptedException {
        // 1. Broadcast mesajul utilizatorului (ca să apară în lista lui)
        messagingTemplate.convertAndSend("/topic/bot", message);

        // Dacă mesajul e trimis chiar de sistem, ne oprim
        if ("System".equals(message.getSenderId()) || "RobBot".equals(message.getSenderId())) {
            return;
        }

        // 2. Căutăm regula
        String responseContent = null;
        String lowerContent = message.getContent().toLowerCase();

        for (Map.Entry<String, String> entry : RULES.entrySet()) {
            if (lowerContent.contains(entry.getKey())) {
                responseContent = entry.getValue();
                break;
            }
        }

        // 3. Fallback (dacă nu găsim regulă) - Aici ar veni AI-ul
        if (responseContent == null) {
            responseContent = "I don't understand '" + message.getContent() + "'. Please try keywords like 'consumption' or 'device', or ask an Admin.";
        }

        // 4. Simulăm un mic delay
        Thread.sleep(500);

        // 5. Trimitem răspunsul robotului
        ChatMessage response = ChatMessage.builder()
                .senderId("RobBot")
                .content(responseContent)
                .isAdmin(false)
                .build();

        messagingTemplate.convertAndSend("/topic/bot", response);
    }

    /* --- TAB-UL 2: Discuția cu Adminul --- */
    @MessageMapping("/chat/admin")
    public void processAdminMessage(@Payload ChatMessage message) {
        // Aici doar dăm forward. Adminul și Userul vorbesc liber.
        messagingTemplate.convertAndSend("/topic/admin", message);
    }
}