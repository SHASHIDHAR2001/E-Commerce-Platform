package com.ecommerce.orderservice.dto;

public class ReserveStockRequest {

    private Long productId;
    private Integer quantity;

    public ReserveStockRequest() {}

    public ReserveStockRequest(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() { return productId; }
    public Integer getQuantity() { return quantity; }

    public void setProductId(Long productId) { this.productId = productId; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}
