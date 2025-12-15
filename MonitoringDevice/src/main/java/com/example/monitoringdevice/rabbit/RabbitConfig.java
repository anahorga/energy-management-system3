package com.example.monitoringdevice.rabbit;

import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {


    @Value("${app.monitoring.queue}")
    private String monitoringQueueName;

    // Definim coada dinamic
    @Bean
    public Queue monitoringQueue() {
        return new Queue(monitoringQueueName, false);
    }
    public static final String EXCHANGE_NAME = "energy_sync_exchange";
    public static final String MONITORING_SYNC_QUEUE = "q.monitoring-service.sync";

    @Bean
    public TopicExchange exchange() {
        return new TopicExchange(EXCHANGE_NAME);
    }

    @Bean
    public Queue monitoringSyncQueue() {
        return new Queue(MONITORING_SYNC_QUEUE);
    }

    @Bean
    public Binding bindingDeviceEvents(Queue monitoringSyncQueue, TopicExchange exchange) {
        return BindingBuilder.bind(monitoringSyncQueue).to(exchange).with("device.#");
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }
    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate template = new RabbitTemplate(connectionFactory);
        template.setMessageConverter(jsonMessageConverter());
        return template;
    }

}