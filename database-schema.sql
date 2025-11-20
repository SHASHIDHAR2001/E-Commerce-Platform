-- =================================================================
-- E-Commerce Order Management System - Database Schema (PostgreSQL)
-- =================================================================

-- ========================================
-- INVENTORY SERVICE DATABASE SCHEMA
-- ========================================

-- Products Table
-- Stores product information with inventory tracking
CREATE TABLE IF NOT EXISTS products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL CHECK (price > 0),
    stock_quantity INTEGER NOT NULL DEFAULT 0 CHECK (stock_quantity >= 0),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

-- Index for faster lookups by SKU
CREATE INDEX IF NOT EXISTS idx_products_sku ON products(sku);

-- Index for faster lookups by active status
CREATE INDEX IF NOT EXISTS idx_products_active ON products(active);

-- ========================================
-- ORDER SERVICE DATABASE SCHEMA
-- ========================================

-- Orders Table
-- Stores customer order information
CREATE TABLE IF NOT EXISTS orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    total_amount NUMERIC(10,2) NOT NULL DEFAULT 0 CHECK (total_amount >= 0),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Order Items Table
-- Stores individual items within an order
CREATE TABLE IF NOT EXISTS order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL,
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL CHECK (quantity > 0),
    price NUMERIC(10,2) NOT NULL CHECK (price >= 0),
    subtotal NUMERIC(10,2) NOT NULL CHECK (subtotal >= 0),
    CONSTRAINT fk_order_items_order 
        FOREIGN KEY (order_id) 
        REFERENCES orders(id) 
        ON DELETE CASCADE
);

-- Indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_customer_email ON orders(customer_email);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);
CREATE INDEX IF NOT EXISTS idx_order_items_order_id ON order_items(order_id);
CREATE INDEX IF NOT EXISTS idx_order_items_product_id ON order_items(product_id);

-- ========================================
-- SAMPLE DATA FOR TESTING
-- ========================================

-- Insert sample products
INSERT INTO products (sku, name, description, price, stock_quantity, active) VALUES
('LAPTOP-001', 'MacBook Pro 16"', '16-inch MacBook Pro with M3 chip, 16GB RAM, 512GB SSD', 2499.99, 50, true),
('LAPTOP-002', 'Dell XPS 15', '15.6-inch Dell XPS with Intel i7, 32GB RAM, 1TB SSD', 1899.99, 75, true),
('MOUSE-001', 'Logitech MX Master 3', 'Ergonomic wireless mouse with USB-C charging', 99.99, 200, true),
('KEYBOARD-001', 'Keychron K2', 'Mechanical keyboard with RGB backlight', 79.99, 150, true),
('MONITOR-001', 'LG UltraWide 34"', '34-inch UltraWide monitor with 3440x1440 resolution', 599.99, 30, true)
ON CONFLICT (sku) DO NOTHING;

-- ========================================
-- DATABASE FUNCTIONS AND TRIGGERS
-- ========================================

-- Function to automatically update the updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

-- Trigger for products table
DROP TRIGGER IF EXISTS update_products_updated_at ON products;
CREATE TRIGGER update_products_updated_at
    BEFORE UPDATE ON products
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- Trigger for orders table
DROP TRIGGER IF EXISTS update_orders_updated_at ON orders;
CREATE TRIGGER update_orders_updated_at
    BEFORE UPDATE ON orders
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ========================================
-- USEFUL QUERIES FOR MONITORING
-- ========================================

-- View to get order summary with item counts
CREATE OR REPLACE VIEW order_summary AS
SELECT 
    o.id,
    o.customer_name,
    o.customer_email,
    o.status,
    o.total_amount,
    COUNT(oi.id) as item_count,
    SUM(oi.quantity) as total_quantity,
    o.created_at,
    o.updated_at
FROM orders o
LEFT JOIN order_items oi ON o.id = oi.order_id
GROUP BY o.id, o.customer_name, o.customer_email, o.status, o.total_amount, o.created_at, o.updated_at;

-- View to get low stock products
CREATE OR REPLACE VIEW low_stock_products AS
SELECT 
    id,
    sku,
    name,
    price,
    stock_quantity,
    created_at,
    updated_at
FROM products
WHERE stock_quantity < 10 AND active = true
ORDER BY stock_quantity ASC;

-- ========================================
-- PERFORMANCE OPTIMIZATION
-- ========================================

-- Analyze tables to update statistics for query planner
ANALYZE products;
ANALYZE orders;
ANALYZE order_items;

-- ========================================
-- CLEANUP QUERIES (USE WITH CAUTION)
-- ========================================

-- Uncomment below to reset all data (WARNING: This will delete all data)
-- DELETE FROM order_items;
-- DELETE FROM orders;
-- DELETE FROM products;
-- ALTER SEQUENCE products_id_seq RESTART WITH 1;
-- ALTER SEQUENCE orders_id_seq RESTART WITH 1;
-- ALTER SEQUENCE order_items_id_seq RESTART WITH 1;
