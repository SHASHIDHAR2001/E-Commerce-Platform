package com.ecommerce.orderservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Service
public class OrderMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);

    @Autowired
    private EmailService emailService;

    @Autowired
    private SmsService smsService;

    @RabbitListener(queues = "${rabbitmq.queue.order}")
    public void consumeOrderMessage(Map<String, Object> orderEvent) {
        logger.info("Received order event from queue: {}", orderEvent);

        Long orderId = ((Number) orderEvent.get("orderId")).longValue();
        String customerEmail = (String) orderEvent.get("customerEmail");
        String status = (String) orderEvent.get("status");
        BigDecimal totalAmount = new BigDecimal(orderEvent.get("totalAmount").toString());

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> items = (List<Map<String, Object>>) orderEvent.get("items");

        String customerPhone = (String) orderEvent.get("customerPhone");

        // send email async
        emailService.sendOrderConfirmationEmail(
                customerEmail,
                orderId,
                totalAmount,
                status,
                items
        );
        logger.info(customerPhone);

        smsService.sendOrderConfirmationSms(
                customerPhone,
                orderId,
                totalAmount,
                status
        );

        logger.info("Order processing (email + SMS) completed for orderId={}", orderId);
    }
}



//package com.ecommerce.orderservice.service;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.amqp.rabbit.annotation.RabbitListener;
//import org.springframework.stereotype.Service;
//
//import java.util.Map;
//
//@Service
//public class OrderMessageConsumer {
//
//    private static final Logger logger = LoggerFactory.getLogger(OrderMessageConsumer.class);
//
//    @RabbitListener(queues = "${rabbitmq.queue.order}")
//    public void consumeOrderMessage(Map<String, Object> orderEvent) {
//        logger.info("Received order event from queue");
//        logger.info("Order ID: {}", orderEvent.get("orderId"));
//        logger.info("Customer Email: {}", orderEvent.get("customerEmail"));
//        logger.info("Status: {}", orderEvent.get("status"));
//        logger.info("Total Amount: {}", orderEvent.get("totalAmount"));
//        logger.info("Order processing completed successfully");
//    }
//}

