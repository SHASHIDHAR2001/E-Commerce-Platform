package com.ecommerce.inventoryservice.dto;

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
}
