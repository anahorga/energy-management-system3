package com.example.loadbalancerservice.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    // Coada de intrare (cea în care scrie simulatorul)
    @Bean
    public Queue incomingQueue() {
        return new Queue("device_measurements", false);
    }

    // Definim cele 4 cozi de ieșire ca să fim siguri că există
    @Bean public Queue q0() { return new Queue("q.monitoring.0", false); }
    @Bean public Queue q1() { return new Queue("q.monitoring.1", false); }
    @Bean public Queue q2() { return new Queue("q.monitoring.2", false); }
    @Bean public Queue q3() { return new Queue("q.monitoring.3", false); }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        return mapper;}

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }
}