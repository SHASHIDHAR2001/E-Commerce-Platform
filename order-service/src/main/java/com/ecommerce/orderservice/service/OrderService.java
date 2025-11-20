package com.ecommerce.orderservice.service;

import com.ecommerce.orderservice.dto.OrderItemRequest;
import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.ReserveStockRequest;
import com.ecommerce.orderservice.exception.InsufficientStockException;
import com.ecommerce.orderservice.exception.InvalidOrderStatusException;
import com.ecommerce.orderservice.exception.OrderNotFoundException;
import com.ecommerce.orderservice.exception.ProductNotFoundException;
import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private static final Logger logger = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${rabbitmq.exchange.order}")
    private String orderExchange;

    @Value("${inventory.url}")
    private String inventoryUrl;

    @Value("${rabbitmq.routing.key.order}")
    private String orderRoutingKey;

    @Value("${inventory.service.url}")
    private String inventoryServiceUrl;

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        logger.info("Creating order for customer: {}", request.getCustomerEmail());

        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setShippingAddress(request.getShippingAddress());
        order.setStatus(OrderStatus.PENDING);

        for (OrderItemRequest itemRequest : request.getItems()) {
            Map<String, Object> product = getProductDetails(itemRequest.getProductId());
            
            if (product == null) {
                throw new ProductNotFoundException("Product not found with ID: " + itemRequest.getProductId());
            }

            Integer availableStock = (Integer) product.get("stockQuantity");
            if (availableStock < itemRequest.getQuantity()) {
                throw new InsufficientStockException(
                    "Insufficient stock for product: " + product.get("name") + 
                    ". Available: " + availableStock + ", Requested: " + itemRequest.getQuantity()
                );
            }

            OrderItem orderItem = new OrderItem();
            orderItem.setProductId(itemRequest.getProductId());
            orderItem.setProductName((String) product.get("name"));
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setPrice(new BigDecimal(product.get("price").toString()));
            orderItem.calculateSubtotal();
            
            order.addOrderItem(orderItem);
            ReserveStockRequest reserve = new ReserveStockRequest(
                    itemRequest.getProductId(),
                    itemRequest.getQuantity()
            );

            restTemplate.postForObject(
                    inventoryUrl + "/api/inventory/reserve-stock",
                    reserve,
                    String.class
            );
        }

        order.calculateTotalAmount();
        Order savedOrder = orderRepository.save(order);

        publishOrderEvent(savedOrder);

        logger.info("Order created successfully with ID: {}", savedOrder.getId());
        return OrderResponse.fromEntity(savedOrder);
    }

    public OrderResponse getOrderById(Long id) {
        logger.info("Fetching order with ID: {}", id);
        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));
        return OrderResponse.fromEntity(order);
    }

    public List<OrderResponse> getAllOrders() {
        logger.info("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByStatus(OrderStatus status) {
        logger.info("Fetching orders with status: {}", status);
        return orderRepository.findByStatus(status).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<OrderResponse> getOrdersByEmail(String email) {
        logger.info("Fetching orders for email: {}", email);
        return orderRepository.findByCustomerEmail(email).stream()
                .map(OrderResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderResponse updateOrderStatus(Long id, OrderStatus newStatus) {
        logger.info("Updating order {} status to: {}", id, newStatus);

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with ID: " + id));

        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);

        logger.info("Order status updated successfully for order ID: {}", id);
        return OrderResponse.fromEntity(updatedOrder);
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        if (currentStatus == OrderStatus.DELIVERED || currentStatus == OrderStatus.CANCELLED) {
            throw new InvalidOrderStatusException(
                "Cannot update status for orders in " + currentStatus + " state"
            );
        }

        int currentOrder = getStatusOrder(currentStatus);
        int newOrder = getStatusOrder(newStatus);

        if (newStatus != OrderStatus.CANCELLED && newOrder < currentOrder) {
            throw new InvalidOrderStatusException(
                "Invalid status transition from " + currentStatus + " to " + newStatus
            );
        }
    }

    private int getStatusOrder(OrderStatus status) {
        return switch (status) {
            case PENDING -> 1;
            case CONFIRMED -> 2;
            case SHIPPED -> 3;
            case DELIVERED -> 4;
            case CANCELLED -> 5;
        };
    }

    private Map<String, Object> getProductDetails(Long productId) {
        try {
            String url = inventoryServiceUrl + "/products/" + productId;
            logger.info("Fetching product details from: {}", url);
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            logger.error("Error fetching product details: ", e);
            return null;
        }
    }

    private void publishOrderEvent(Order order) {
        try {
            Map<String, Object> orderEvent = new HashMap<>();
            orderEvent.put("orderId", order.getId());
            orderEvent.put("customerEmail", order.getCustomerEmail());
            orderEvent.put("status", order.getStatus().toString());
            orderEvent.put("totalAmount", order.getTotalAmount());

            orderEvent.put("customerPhone", order.getCustomerPhone());

            List<Map<String, Object>> items = order.getOrderItems().stream()
                    .map(item -> {
                        Map<String, Object> itemMap = new HashMap<>();
                        itemMap.put("productId", item.getProductId());
                        itemMap.put("quantity", item.getQuantity());
                        itemMap.put("price", item.getPrice());
                        itemMap.put("subtotal", item.getSubtotal());
                        return itemMap;
                    })
                    .collect(Collectors.toList());

            orderEvent.put("items", items);

            rabbitTemplate.convertAndSend(orderExchange, orderRoutingKey, orderEvent);
            logger.info("Order event published for order ID: {}", order.getId());
        } catch (Exception e) {
            logger.error("Error publishing order event: ", e);
        }
    }

}
