package com.ecommerce.inventoryservice.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    @Value("${rabbitmq.queue.inventory}")
    private String inventoryQueue;

    @Value("${rabbitmq.exchange.inventory}")
    private String inventoryExchange;

    @Value("${rabbitmq.routing.key.inventory}")
    private String inventoryRoutingKey;

    @Bean
    public Queue inventoryQueue() {
        return new Queue(inventoryQueue, true);
    }

    @Bean
    public TopicExchange inventoryExchange() {
        return new TopicExchange(inventoryExchange);
    }

    @Bean
    public Binding inventoryBinding() {
        return BindingBuilder
                .bind(inventoryQueue())
                .to(inventoryExchange())
                .with(inventoryRoutingKey);
    }

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(messageConverter());
        return rabbitTemplate;
    }
}
