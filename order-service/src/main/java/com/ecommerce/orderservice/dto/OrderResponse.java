package com.ecommerce.orderservice.dto;

import com.ecommerce.orderservice.model.Order;
import com.ecommerce.orderservice.model.OrderItem;
import com.ecommerce.orderservice.model.OrderStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class OrderResponse {

    private Long id;
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String shippingAddress;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public OrderResponse() {}

    public OrderResponse(Long id, String customerName, String customerPhone,
                         String customerEmail, String shippingAddress,
                         OrderStatus status, BigDecimal totalAmount,
                         List<OrderItemResponse> items,
                         LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.shippingAddress = shippingAddress;
        this.status = status;
        this.totalAmount = totalAmount;
        this.items = items;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public void setCustomerPhone(String customerPhone) {
        this.customerPhone = customerPhone;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(String shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public List<OrderItemResponse> getItems() {
        return items;
    }

    public void setItems(List<OrderItemResponse> items) {
        this.items = items;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ========================
    // STATIC MAPPER
    // ========================

    public static OrderResponse fromEntity(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setCustomerName(order.getCustomerName());
        response.setCustomerEmail(order.getCustomerEmail());
        response.setCustomerPhone(order.getCustomerPhone());
        response.setShippingAddress(order.getShippingAddress());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        response.setItems(
                order.getOrderItems().stream()
                        .map(OrderItemResponse::fromEntity)
                        .collect(Collectors.toList())
        );

        return response;
    }
}

// ========================
// INNER RESPONSE CLASS
// ========================
class OrderItemResponse {

    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal price;
    private BigDecimal subtotal;

    public OrderItemResponse() {}

    public OrderItemResponse(Long id, Long productId, String productName,
                             Integer quantity, BigDecimal price, BigDecimal subtotal) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.quantity = quantity;
        this.price = price;
        this.subtotal = subtotal;
    }

    // ===== GETTERS & SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    public static OrderItemResponse fromEntity(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProductId());
        response.setProductName(item.getProductName());
        response.setQuantity(item.getQuantity());
        response.setPrice(item.getPrice());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}
