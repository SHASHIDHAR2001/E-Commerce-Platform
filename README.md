# E-Commerce Order Management System - Microservices

A scalable microservices-based Order Management System built with Spring Boot, featuring asynchronous processing, comprehensive API documentation, and production-ready error handling.

## Architecture Overview

This system consists of two independent microservices:

### 1. **Inventory Service** (Port 8000)
- Product management with CRUD operations
- Stock control with pessimistic locking to prevent race conditions
- Real-time inventory tracking and validation

### 2. **Order Service** (Port 8080)
- Order placement with automated inventory validation
- Order status management with state transition validation
- Integration with Inventory Service for stock verification

## Technology Stack

- **Framework**: Spring Boot 3.2.0
- **Language**: Java 17
- **Database**: PostgreSQL (NeonDB)
- **Message Queue**: RabbitMQ (async processing)
- **API Documentation**: Swagger/OpenAPI 3.0
- **Build Tool**: Maven
- **ORM**: Hibernate/JPA

## Key Features

### Functional Requirements
✅ Order placement with inventory validation  
✅ Automatic stock reduction on order confirmation  
✅ Order status tracking (Pending → Confirmed → Shipped → Delivered)  
✅ Product inventory management  
✅ Race condition handling with pessimistic locking  
✅ RESTful API design  

### Non-Functional Requirements
✅ Comprehensive error handling with meaningful HTTP status codes  
✅ Logging framework (SLF4J/Logback) for all operations  
✅ Swagger UI for interactive API testing  
✅ Async message processing with RabbitMQ  
✅ Database transactions for data consistency  
✅ Input validation with detailed error messages  

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+
- PostgreSQL database
- RabbitMQ (optional, for async features)

### Environment Variables
The following environment variables are required:
```bash
# Database Configuration
PGHOST=your-database-host
PGPORT=5432
PGDATABASE=your-database-name
PGUSER=your-database-user
PGPASSWORD=your-database-password

# RabbitMQ Configuration (Optional)
RABBITMQ_HOST=localhost
RABBITMQ_PORT=5672
RABBITMQ_USERNAME=guest
RABBITMQ_PASSWORD=guest
```

### Build and Run

#### Build Both Services
```bash
# Build Inventory Service
cd inventory-service
mvn clean package -DskipTests

# Build Order Service
cd ../order-service
mvn clean package -DskipTests
```

#### Run Services
```bash
# Run Inventory Service (Terminal 1)
cd inventory-service
java -jar target/inventory-service-1.0.0.jar

# Run Order Service (Terminal 2)
cd order-service
java -jar target/order-service-1.0.0.jar
```

## API Documentation

### Inventory Service APIs

**Base URL**: `http://localhost:8000/api/inventory`

#### Product Management

**Create Product**
```http
POST /products
Content-Type: application/json

{
  "sku": "PROD-001",
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with USB receiver",
  "price": 29.99,
  "stockQuantity": 100,
  "active": true
}
```

**Get All Products**
```http
GET /products
```

**Get Product by ID**
```http
GET /products/{id}
```

**Update Product**
```http
PUT /products/{id}
Content-Type: application/json

{
  "sku": "PROD-001",
  "name": "Wireless Mouse Pro",
  "description": "Updated description",
  "price": 34.99,
  "stockQuantity": 150,
  "active": true
}
```

**Delete Product**
```http
DELETE /products/{id}
```

#### Stock Management

**Add Stock**
```http
POST /products/{id}/add-stock
Content-Type: application/json

{
  "quantity": 50
}
```

**Reduce Stock**
```http
POST /products/{id}/reduce-stock
Content-Type: application/json

{
  "quantity": 10
}
```

**Check Stock Availability**
```http
GET /products/{id}/check-stock?quantity=5
```

### Order Service APIs

**Base URL**: `http://localhost:8080/api/orders`

#### Order Management

**Create Order**
```http
POST /
Content-Type: application/json

{
  "customerName": "John Doe",
  "customerEmail": "john.doe@example.com",
  "shippingAddress": "123 Main St, New York, NY 10001",
  "items": [
    {
      "productId": 1,
      "quantity": 2
    },
    {
      "productId": 2,
      "quantity": 1
    }
  ]
}
```

**Get All Orders**
```http
GET /
```

**Get Order by ID**
```http
GET /{id}
```

**Get Orders by Status**
```http
GET /status/{status}

# Valid statuses: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED
```

**Get Orders by Customer Email**
```http
GET /customer/{email}
```

**Update Order Status**
```http
PUT /{id}/status
Content-Type: application/json

{
  "status": "SHIPPED"
}
```

## Swagger UI

Access interactive API documentation:

- **Inventory Service**: http://localhost:8000/swagger-ui.html
- **Order Service**: http://localhost:8080/swagger-ui.html

## Database Schema

### Products Table (Inventory Service)
```sql
CREATE TABLE products (
    id BIGSERIAL PRIMARY KEY,
    sku VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    price NUMERIC(10,2) NOT NULL,
    stock_quantity INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    version BIGINT
);
```

### Orders Table (Order Service)
```sql
CREATE TABLE orders (
    id BIGSERIAL PRIMARY KEY,
    customer_name VARCHAR(255) NOT NULL,
    customer_email VARCHAR(255) NOT NULL,
    shipping_address VARCHAR(255) NOT NULL,
    status VARCHAR(50) NOT NULL CHECK (status IN ('PENDING','CONFIRMED','SHIPPED','DELIVERED','CANCELLED')),
    total_amount NUMERIC(10,2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE order_items (
    id BIGSERIAL PRIMARY KEY,
    order_id BIGINT NOT NULL REFERENCES orders(id),
    product_id BIGINT NOT NULL,
    product_name VARCHAR(255) NOT NULL,
    quantity INTEGER NOT NULL,
    price NUMERIC(10,2) NOT NULL,
    subtotal NUMERIC(10,2) NOT NULL
);
```

## Error Handling

The system implements comprehensive error handling with appropriate HTTP status codes:

| Status Code | Description |
|------------|-------------|
| 200 OK | Successful request |
| 201 Created | Resource created successfully |
| 400 Bad Request | Validation error or invalid request |
| 404 Not Found | Resource not found |
| 409 Conflict | Duplicate resource (e.g., SKU already exists) |
| 500 Internal Server Error | Unexpected server error |

### Error Response Format
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": {
    "customerName": "Customer name is required",
    "customerEmail": "Invalid email format"
  },
  "timestamp": "2025-11-19T06:00:00"
}
```

## AWS Deployment Guide

### Deployment Architecture

```
┌─────────────────────────────────────────────────────────┐
│                     AWS Cloud                            │
│                                                          │
│  ┌──────────────┐       ┌──────────────┐               │
│  │   EC2 (t2.micro)    │   RDS PostgreSQL │           │
│  │  ┌──────────┐       │                 │
│  │  │ Order    │       └──────────────┘               │
│  │  │ Service  │              │                        │
│  │  │ (8080)   │◄─────────────┘                        │
│  │  └──────────┘                                       │
│  │                                                      │
│  │  ┌──────────┐                                       │
│  │  │Inventory │                                       │
│  │  │ Service  │                                       │
│  │  │ (8000)   │                                       │
│  │  └──────────┘                                       │
│  │                                                      │
│  │  ┌──────────┐                                       │
│  │  │ RabbitMQ │                                       │
│  │  │ (Docker) │                                       │
│  │  └──────────┘                                       │
│  └──────────────┘                                       │
└─────────────────────────────────────────────────────────┘
```

### Step-by-Step Deployment

#### 1. Setup EC2 Instance (Free Tier)
```bash
# Launch EC2 t2.micro instance with Ubuntu 22.04
# Configure Security Group:
# - Port 22 (SSH)
# - Port 8000 (Inventory Service)
# - Port 8080 (Order Service)
# - Port 5672 (RabbitMQ)

# Connect to EC2
ssh -i your-key.pem ubuntu@your-ec2-ip
```

#### 2. Install Java and Maven
```bash
sudo apt update
sudo apt install openjdk-17-jdk maven git -y
java -version
mvn -version
```

#### 3. Install Docker and RabbitMQ
```bash
# Install Docker
sudo apt install docker.io -y
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ubuntu

# Run RabbitMQ
docker run -d --name rabbitmq \
  -p 5672:5672 \
  -p 15672:15672 \
  rabbitmq:3-management
```

#### 4. Setup RDS PostgreSQL (Free Tier)
```
# In AWS Console:
# 1. Create RDS PostgreSQL instance (db.t3.micro or db.t4g.micro for free tier)
# 2. Select "Free tier" template
# 3. Note down: endpoint, port, username, password, database name
# 4. Configure security group to allow connections from EC2
```

#### 5. Clone and Build
```bash
# Clone repository
git clone <your-repo-url>
cd order-management-system

# Set environment variables
export PGHOST=your-rds-endpoint
export PGPORT=5432
export PGDATABASE=your-database-name
export PGUSER=your-username
export PGPASSWORD=your-password
export RABBITMQ_HOST=localhost

# Build services
cd inventory-service && mvn clean package -DskipTests
cd ../order-service && mvn clean package -DskipTests
```

#### 6. Run Services with systemd

**Inventory Service systemd unit** (`/etc/systemd/system/inventory-service.service`):
```ini
[Unit]
Description=Inventory Service
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/order-management-system/inventory-service
Environment="PGHOST=your-rds-endpoint"
Environment="PGPORT=5432"
Environment="PGDATABASE=your-database"
Environment="PGUSER=your-user"
Environment="PGPASSWORD=your-password"
Environment="RABBITMQ_HOST=localhost"
ExecStart=/usr/bin/java -jar target/inventory-service-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Order Service systemd unit** (`/etc/systemd/system/order-service.service`):
```ini
[Unit]
Description=Order Service
After=network.target

[Service]
User=ubuntu
WorkingDirectory=/home/ubuntu/order-management-system/order-service
Environment="PGHOST=your-rds-endpoint"
Environment="PGPORT=5432"
Environment="PGDATABASE=your-database"
Environment="PGUSER=your-user"
Environment="PGPASSWORD=your-password"
Environment="RABBITMQ_HOST=localhost"
ExecStart=/usr/bin/java -jar target/order-service-1.0.0.jar
Restart=always
RestartSec=10

[Install]
WantedBy=multi-user.target
```

**Start services**:
```bash
sudo systemctl daemon-reload
sudo systemctl start inventory-service
sudo systemctl start order-service
sudo systemctl enable inventory-service
sudo systemctl enable order-service

# Check status
sudo systemctl status inventory-service
sudo systemctl status order-service
```

#### 7. Setup Nginx Reverse Proxy (Optional)
```bash
sudo apt install nginx -y

# Configure /etc/nginx/sites-available/default
server {
    listen 80;
    server_name your-domain.com;

    location /api/inventory {
        proxy_pass http://localhost:8000/api/inventory;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }

    location /api/orders {
        proxy_pass http://localhost:8080/api/orders;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
    }
}

sudo nginx -t
sudo systemctl restart nginx
```

### Cost Estimation (Free Tier)
- **EC2 t2.micro**: 750 hours/month free
- **RDS db.t3.micro**: 750 hours/month free
- **Data Transfer**: 15GB/month free
- **Total**: $0/month within free tier limits

## Testing

### Sample Testing Flow

1. **Create Products**
```bash
curl -X POST http://localhost:8000/api/inventory/products \
  -H "Content-Type: application/json" \
  -d '{
    "sku": "LAPTOP-001",
    "name": "MacBook Pro",
    "description": "16-inch MacBook Pro",
    "price": 2499.99,
    "stockQuantity": 50
  }'
```

2. **Create Order**
```bash
curl -X POST http://localhost:8080/api/orders \
  -H "Content-Type: application/json" \
  -d '{
    "customerName": "Jane Smith",
    "customerEmail": "jane@example.com",
    "shippingAddress": "456 Oak Ave, Boston, MA 02101",
    "items": [
      {"productId": 1, "quantity": 1}
    ]
  }'
```

3. **Update Order Status**
```bash
curl -X PUT http://localhost:8080/api/orders/1/status \
  -H "Content-Type: application/json" \
  -d '{"status": "SHIPPED"}'
```

## Project Structure

```
order-management-system/
├── inventory-service/
│   ├── src/main/java/com/ecommerce/inventoryservice/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access layer
│   │   ├── model/           # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   ├── config/          # Configuration classes
│   │   └── exception/       # Exception handlers
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── order-service/
│   ├── src/main/java/com/ecommerce/orderservice/
│   │   ├── controller/      # REST controllers
│   │   ├── service/         # Business logic
│   │   ├── repository/      # Data access layer
│   │   ├── model/           # JPA entities
│   │   ├── dto/             # Data transfer objects
│   │   ├── config/          # Configuration classes
│   │   └── exception/       # Exception handlers
│   ├── src/main/resources/
│   │   └── application.properties
│   └── pom.xml
├── docker-compose.yml       # RabbitMQ setup
└── README.md
```

## Troubleshooting

### Common Issues

**Database Connection Failed**
- Verify environment variables are set correctly
- Check PostgreSQL is running and accessible
- Ensure database user has proper permissions

**RabbitMQ Connection Warnings**
- Non-critical if you're not using async features
- Start RabbitMQ with Docker: `docker-compose up -d`

**Port Already in Use**
- Change ports in `application.properties`
- Kill existing process: `lsof -ti:8080 | xargs kill`

## Future Enhancements

- [ ] Implement rate limiting with bucket4j
- [ ] Add Redis caching layer
- [ ] Implement retry mechanism for failed orders
- [ ] Add API Gateway with Spring Cloud Gateway
- [ ] Implement circuit breaker pattern
- [ ] Add distributed tracing with Zipkin
- [ ] Implement database read replicas
- [ ] Add comprehensive unit and integration tests

## Contact

For questions or support:
- Email: ashutosh.t@sunking.com
- Phone: 9958762772

## License

This project is developed as a case study for backend developer assessment.
