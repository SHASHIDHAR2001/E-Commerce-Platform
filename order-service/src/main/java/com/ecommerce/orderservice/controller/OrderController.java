package com.ecommerce.orderservice.controller;

import com.ecommerce.orderservice.dto.OrderRequest;
import com.ecommerce.orderservice.dto.OrderResponse;
import com.ecommerce.orderservice.dto.OrderStatusUpdateRequest;
import com.ecommerce.orderservice.model.OrderStatus;
import com.ecommerce.orderservice.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Order Management", description = "APIs for managing orders")
public class OrderController {

    private static final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @PostMapping
    @Operation(summary = "Create a new order", description = "Place a new order with inventory validation")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        logger.info("REST request to create order for: {}", request.getCustomerEmail());
        OrderResponse response = orderService.createOrder(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get order by ID", description = "Retrieve order details by order ID")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        logger.info("REST request to get order: {}", id);
        OrderResponse response = orderService.getOrderById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "Get all orders", description = "Retrieve all orders")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        logger.info("REST request to get all orders");
        List<OrderResponse> response = orderService.getAllOrders();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/status/{status}")
    @Operation(summary = "Get orders by status", description = "Retrieve orders by their status")
    public ResponseEntity<List<OrderResponse>> getOrdersByStatus(@PathVariable OrderStatus status) {
        logger.info("REST request to get orders by status: {}", status);
        List<OrderResponse> response = orderService.getOrdersByStatus(status);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/customer/{email}")
    @Operation(summary = "Get orders by customer email", description = "Retrieve orders for a specific customer")
    public ResponseEntity<List<OrderResponse>> getOrdersByEmail(@PathVariable String email) {
        logger.info("REST request to get orders for email: {}", email);
        List<OrderResponse> response = orderService.getOrdersByEmail(email);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "Update order status", description = "Update the status of an existing order")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Long id, 
            @Valid @RequestBody OrderStatusUpdateRequest request) {
        logger.info("REST request to update order {} status to: {}", id, request.getStatus());
        OrderResponse response = orderService.updateOrderStatus(id, request.getStatus());
        return ResponseEntity.ok(response);
    }
}
