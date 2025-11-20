//package com.ecommerce.orderservice.config;
//
//import org.springframework.amqp.core.Binding;
//import org.springframework.amqp.core.BindingBuilder;
//import org.springframework.amqp.core.Queue;
//import org.springframework.amqp.core.TopicExchange;
//import org.springframework.amqp.rabbit.connection.ConnectionFactory;
//import org.springframework.amqp.rabbit.core.RabbitTemplate;
//import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
//import org.springframework.amqp.support.converter.MessageConverter;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.context.annotation.Configuration;
//
//@Configuration
//public class RabbitMQConfig {
//
//    @Value("${rabbitmq.queue.order}")
//    private String orderQueue;
//
//    @Value("${rabbitmq.exchange.order}")
//    private String orderExchange;
//
//    @Value("${rabbitmq.routing.key.order}")
//    private String orderRoutingKey;
//
//    @Bean
//    public Queue orderQueue() {
//        return new Queue(orderQueue, true);
//    }
//
//    @Bean
//    public TopicExchange orderExchange() {
//        return new TopicExchange(orderExchange);
//    }
//
//    @Bean
//    public Binding orderBinding() {
//        return BindingBuilder
//                .bind(orderQueue())
//                .to(orderExchange())
//                .with(orderRoutingKey);
//    }
//
//    @Bean
//    public MessageConverter messageConverter() {
//        return new Jackson2JsonMessageConverter();
//    }
//
//    @Bean
//    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
//        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
//        rabbitTemplate.setMessageConverter(messageConverter());
//        return rabbitTemplate;
//    }
//}

package com.ecommerce.orderservice.config;

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

    @Value("${rabbitmq.queue.order}")
    private String orderQueue;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${rabbitmq.routing.key.order}")
    private String orderRoutingKey;

    @Bean
    public Queue orderQueue() {
        return new Queue(orderQueue, true); // durable queue
    }

    @Bean
    public TopicExchange orderExchange() {
        return new TopicExchange(orderExchange);
    }

    @Bean
    public Binding orderBinding() {
        return BindingBuilder
                .bind(orderQueue())
                .to(orderExchange())
                .with(orderRoutingKey);
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
