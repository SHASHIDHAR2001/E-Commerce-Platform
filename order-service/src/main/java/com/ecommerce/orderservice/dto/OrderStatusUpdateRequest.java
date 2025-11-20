package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.OrderStatus;
import jakarta.validation.constraints.NotNull;

public class OrderStatusUpdateRequest {

    @NotNull(message = "Order status is required")
    private OrderStatus status;

    public OrderStatusUpdateRequest() {}

    public OrderStatusUpdateRequest(OrderStatus status) {
        this.status = status;
    }

    // ====== GETTERS & SETTERS ======

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }
}
