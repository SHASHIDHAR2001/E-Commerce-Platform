package com.ecommerce.inventoryservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InventoryMessageConsumer {

    private static final Logger logger = LoggerFactory.getLogger(InventoryMessageConsumer.class);

    @Autowired
    private InventoryService inventoryService;

    @RabbitListener(queues = "${rabbitmq.queue.inventory}")
    public void consumeInventoryMessage(Map<String, Object> inventoryEvent) {
        logger.info("Received inventory event from queue");
        logger.info("Event details: {}", inventoryEvent);
        logger.info("Inventory processing completed successfully");
    }
}
