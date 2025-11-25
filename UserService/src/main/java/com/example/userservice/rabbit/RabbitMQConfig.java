package com.example.userservice.rabbit;

import org.springframework.amqp.core.*;

import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String EXCHANGE_NAME = "energy_sync_exchange";

    public static final String USER_SERVICE_QUEUE = "q.user-service.sync";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue userServiceQueue() {
        return new Queue(USER_SERVICE_QUEUE);
    }

    @Bean
    public Binding bindingUserEvents(Queue userServiceQueue, TopicExchange exchange) {
        return BindingBuilder.bind(userServiceQueue).to(exchange).with("user.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
}