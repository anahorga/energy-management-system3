package com.example.chatservice.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Service
public class AiService {

    private final RestClient restClient;

    @Value("${ai.model}")
    private String model;

    public AiService(@Value("${ai.api.url}") String baseUrl,
                     @Value("${ai.api.key}") String apiKey) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("Authorization", "Bearer " + apiKey)
                .defaultHeader("Content-Type", "application/json")
                .build();
    }

    public String generateResponse(String userMessage) {
        try {
            var requestBody = Map.of(
                    "model", model,
                    "messages", List.of(
                            Map.of("role", "system", "content", "You are a helpful energy management assistant named RobBot."),
                            Map.of("role", "user", "content", userMessage)
                    )
            );

            // Groq răspunde cu o listă de 'choices'
            GroqResponse response = restClient.post()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response != null && !response.choices().isEmpty()) {
                // Extragem mesajul din prima opțiune
                return response.choices().get(0).message().content();
            }

            return "I received an empty response from Groq.";

        } catch (Exception e) {
            e.printStackTrace();
            return "My AI brain is offline. Error: " + e.getMessage();
        }
    }

    // --- Structura JSON pentru Groq / OpenAI ---
    // { "choices": [ { "message": { "role": "assistant", "content": "..." } } ] }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record GroqResponse(List<Choice> choices) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record Choice(AiMessage message) {}

    @JsonIgnoreProperties(ignoreUnknown = true)
    record AiMessage(String role, String content) {}
}