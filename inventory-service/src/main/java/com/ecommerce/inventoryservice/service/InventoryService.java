package com.ecommerce.inventoryservice.service;

import com.ecommerce.inventoryservice.dto.ProductRequest;
import com.ecommerce.inventoryservice.dto.ProductResponse;
import com.ecommerce.inventoryservice.exception.DuplicateProductException;
import com.ecommerce.inventoryservice.exception.InsufficientStockException;
import com.ecommerce.inventoryservice.exception.ProductNotFoundException;
import com.ecommerce.inventoryservice.model.Product;
import com.ecommerce.inventoryservice.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class InventoryService {

    private static final Logger logger = LoggerFactory.getLogger(InventoryService.class);

    @Autowired
    private ProductRepository productRepository;

    @Transactional
    public ProductResponse createProduct(ProductRequest request) {
        logger.info("Creating new product with SKU: {}", request.getSku());
        
        if (productRepository.findBySku(request.getSku()).isPresent()) {
            throw new DuplicateProductException("Product with SKU " + request.getSku() + " already exists");
        }

        Product product = new Product();
        product.setSku(request.getSku());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive() != null ? request.getActive() : true);

        Product savedProduct = productRepository.save(product);
        logger.info("Product created successfully with ID: {}", savedProduct.getId());
        
        return ProductResponse.fromEntity(savedProduct);
    }

    public ProductResponse getProductById(Long id) {
        logger.info("Fetching product with ID: {}", id);
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        return ProductResponse.fromEntity(product);
    }

    public List<ProductResponse> getAllProducts() {
        logger.info("Fetching all products");
        return productRepository.findAll().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<ProductResponse> getActiveProducts() {
        logger.info("Fetching all active products");
        return productRepository.findByActiveTrue().stream()
                .map(ProductResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        logger.info("Updating product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));

        if (!product.getSku().equals(request.getSku())) {
            if (productRepository.findBySku(request.getSku()).isPresent()) {
                throw new DuplicateProductException("Product with SKU " + request.getSku() + " already exists");
            }
            product.setSku(request.getSku());
        }

        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setActive(request.getActive());

        Product updatedProduct = productRepository.save(product);
        logger.info("Product updated successfully with ID: {}", updatedProduct.getId());
        
        return ProductResponse.fromEntity(updatedProduct);
    }

    @Transactional
    public void deleteProduct(Long id) {
        logger.info("Deleting product with ID: {}", id);
        
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + id));
        
        productRepository.delete(product);
        logger.info("Product deleted successfully with ID: {}", id);
    }

    @Transactional
    public ProductResponse addStock(Long productId, Integer quantity) {
        logger.info("Adding {} units to product ID: {}", quantity, productId);
        
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        product.setStockQuantity(product.getStockQuantity() + quantity);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Stock added successfully. New quantity: {}", updatedProduct.getStockQuantity());
        return ProductResponse.fromEntity(updatedProduct);
    }

    @Transactional
    public ProductResponse reduceStock(Long productId, Integer quantity) {
        logger.info("Reducing {} units from product ID: {}", quantity, productId);
        
        Product product = productRepository.findByIdWithLock(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException(
                "Insufficient stock for product: " + product.getName() + 
                ". Available: " + product.getStockQuantity() + ", Requested: " + quantity
            );
        }

        product.setStockQuantity(product.getStockQuantity() - quantity);
        Product updatedProduct = productRepository.save(product);
        
        logger.info("Stock reduced successfully. New quantity: {}", updatedProduct.getStockQuantity());
        return ProductResponse.fromEntity(updatedProduct);
    }

    public boolean checkStock(Long productId, Integer quantity) {
        logger.info("Checking stock for product ID: {} with quantity: {}", productId, quantity);
        
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with ID: " + productId));

        boolean hasStock = product.getStockQuantity() >= quantity;
        logger.info("Stock check result: {}", hasStock);
        return hasStock;
    }
}
