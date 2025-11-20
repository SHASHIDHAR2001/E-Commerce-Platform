package com.ecommerce.inventoryservice.controller;

import com.ecommerce.inventoryservice.dto.ProductRequest;
import com.ecommerce.inventoryservice.dto.ProductResponse;
import com.ecommerce.inventoryservice.dto.ReserveStockRequest;
import com.ecommerce.inventoryservice.dto.StockUpdateRequest;
import com.ecommerce.inventoryservice.service.InventoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/inventory")
@Tag(name = "Inventory Management", description = "APIs for managing products and inventory")
public class InventoryController {

    private static final Logger logger = LoggerFactory.getLogger(InventoryController.class);

    @Autowired
    private InventoryService inventoryService;

    @PostMapping("/products")
    @Operation(summary = "Create a new product", description = "Add a new product to the inventory")
    public ResponseEntity<ProductResponse> createProduct(@Valid @RequestBody ProductRequest request) {
        logger.info("REST request to create product: {}", request.getSku());
        ProductResponse response = inventoryService.createProduct(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/products/{id}")
    @Operation(summary = "Get product by ID", description = "Retrieve product details by product ID")
    public ResponseEntity<ProductResponse> getProductById(@PathVariable Long id) {
        logger.info("REST request to get product: {}", id);
        ProductResponse response = inventoryService.getProductById(id);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products")
    @Operation(summary = "Get all products", description = "Retrieve all products in the inventory")
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        logger.info("REST request to get all products");
        List<ProductResponse> response = inventoryService.getAllProducts();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/products/active")
    @Operation(summary = "Get active products", description = "Retrieve all active products")
    public ResponseEntity<List<ProductResponse>> getActiveProducts() {
        logger.info("REST request to get active products");
        List<ProductResponse> response = inventoryService.getActiveProducts();
        return ResponseEntity.ok(response);
    }

    @PutMapping("/products/{id}")
    @Operation(summary = "Update product", description = "Update an existing product")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id, 
            @Valid @RequestBody ProductRequest request) {
        logger.info("REST request to update product: {}", id);
        ProductResponse response = inventoryService.updateProduct(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/products/{id}")
    @Operation(summary = "Delete product", description = "Delete a product from inventory")
    public ResponseEntity<Map<String, String>> deleteProduct(@PathVariable Long id) {
        logger.info("REST request to delete product: {}", id);
        inventoryService.deleteProduct(id);
        Map<String, String> response = new HashMap<>();
        response.put("message", "Product deleted successfully");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/{id}/add-stock")
    @Operation(summary = "Add stock", description = "Add stock quantity to a product")
    public ResponseEntity<ProductResponse> addStock(
            @PathVariable Long id, 
            @Valid @RequestBody StockUpdateRequest request) {
        logger.info("REST request to add stock to product: {}", id);
        ProductResponse response = inventoryService.addStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/{id}/reduce-stock")
    @Operation(summary = "Reduce stock", description = "Reduce stock quantity from a product")
    public ResponseEntity<ProductResponse> reduceStock(
            @PathVariable Long id, 
            @Valid @RequestBody StockUpdateRequest request) {
        logger.info("REST request to reduce stock from product: {}", id);
        ProductResponse response = inventoryService.reduceStock(id, request.getQuantity());
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reserve-stock")
    public ResponseEntity<String> reserveStock(@RequestBody ReserveStockRequest request) {

        inventoryService.reduceStock(request.getProductId(), request.getQuantity());

        return ResponseEntity.ok("Stock Reserved Successfully");
    }


    @GetMapping("/products/{id}/check-stock")
    @Operation(summary = "Check stock availability", description = "Check if sufficient stock is available")
    public ResponseEntity<Map<String, Boolean>> checkStock(
            @PathVariable Long id, 
            @RequestParam Integer quantity) {
        logger.info("REST request to check stock for product: {}", id);
        boolean available = inventoryService.checkStock(id, quantity);
        Map<String, Boolean> response = new HashMap<>();
        response.put("available", available);
        return ResponseEntity.ok(response);
    }
}
