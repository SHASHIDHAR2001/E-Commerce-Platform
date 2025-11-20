# 🚀 E-Commerce Order Management System – Microservices (Java + Spring Boot + AWS)

A fully scalable and production-ready **Order Management System (OMS)** designed using microservices architecture.  
Built as part of the *E-Commerce Platform Case Study* and deployed on **AWS EC2** with PostgreSQL + RabbitMQ.

---

## 📌 Live Swagger APIs (Hosted on AWS)

| Service | Swagger URL |
|--------|-------------|
| **Order Service (8081)** | http://54.253.150.76:8081/swagger-ui/index.html |
| **Inventory Service (8082)** | http://54.253.150.76:8082/swagger-ui/index.html |

Both APIs come with **full Swagger documentation** and are easily testable.

---

# 🏗️ Architecture Overview

This system consists of two independent microservices:

### 1. **Inventory Service** (Port 8082)
- Product management with CRUD operations
- Stock control with pessimistic locking to prevent race conditions
- Real-time inventory tracking and validation

### 2. **Order Service** (Port 8081)
- Order placement with automated inventory validation
- Order status management with state transition validation
- Integration with Inventory Service for stock verification

                         ┌──────────────────────────┐
                         │     Swagger API UI       │
                         │ (Interactive API Testing)│
                         └──────────────┬───────────┘
                                        │ REST APIs
                                        ▼
            ┌───────────────────────────────────────────────────────┐
            │                          AWS EC2                      │
            │                                                       │
            │   ┌─────────────────────────────┐    ┌────────────┐   │
            │   │  Order Service (8081)       │    │ RabbitMQ   │   │
            │   │  REST + Async Events        │    │ 5672/15672 │   │
            │   └─────────────────────────────┘    └────────────┘   │
            │                ▲ REST Call                            │
            │                │                                      │
            │   ┌─────────────────────────────┐                     │
            │   │ Inventory Service (8082)    │                     │
            │   │ Stock Validation            │                     │
            │   └─────────────────────────────┘                     │
            └──────────────────────┬─────────────────────────────── ┘
                                   │
                          JDBC Connection
                                   ▼
                      AWS RDS PostgreSQL (Production)



---

# 🧰 Tech Stack

| Component          | Technology                  |
|-------------------|-----------------------------|
| **Runtime**        | Java 17+                    |
| **Framework**      | Spring Boot 3.2.x           |
| **Database**       | AWS RDS PostgreSQL          |
| **Message Broker** | RabbitMQ (Async Events)     |
| **Deployment**     | AWS EC2 (Amazon Linux 2023) |
| **ORM**            | JPA / Hibernate             |
| **Documentation**  | Swagger / OpenAPI           |
| **Build Tool**     | Maven                       |
| **Logging**        | SLF4J + Logback             |

---

# 📦 Microservices Included

## ✅ 1. Order Service (8081)
Handles:
- Place orders
- Validate stock from Inventory Service
- Calculate total amount
- Update order status
- Publish events to RabbitMQ
- Send async email/SMS notifications

## ✅ 2. Inventory Service (8082)
Handles:
- Product CRUD
- Stock add/remove
- Concurrency safety with **PESSIMISTIC_WRITE** locking
- Consumes RabbitMQ events
- Prevents overselling

---

# 📨 RabbitMQ Usage (Async & Scalable)

### Why RabbitMQ?
- Prevents API slowdown during traffic spikes
- Handles queue-based load distribution
- Decouples services
- Ensures guaranteed message delivery

### Used For:
1. Order confirmation email/SMS using Brevo API
2. Order status notifications
3. Background inventory updates

---

# 🗄️ Database Schema

The complete schema is available at:  
`database-schema.sql`

## 📋 Includes
- `products` table
- `orders` table
- `order_items` table
- triggers
- indexes
- sample data
- monitoring views

# ⚡ Scalability Decisions

### 1️⃣ Async Order Processing
Using RabbitMQ:
- Order request returns fast
- Heavy tasks run in background
- Can handle sudden traffic spikes

### 2️⃣ Database Locking (No Overselling)
- Inventory updates use **PESSIMISTIC_WRITE** lock
- Ensures stock integrity

### 3️⃣ Stateless Microservices
Can scale horizontally:
- docker-compose up --scale order-service=5

### 4️⃣ Database Scaling
- Read replicas
- Partitioning in high-volume tables
- Proper indexes included

### 5️⃣ Retry Safe
Idempotent consumers avoid double-updates.

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

## 🚀 Scaling the System in a Production Environment

To ensure the Order Management System can handle real-world traffic spikes and large-scale operations, the following production-grade scaling strategies can be applied:

### 1. Horizontal Scaling (Microservices-Level)
- Deploy multiple instances of `order-service` and `inventory-service`.
- Use a Load Balancer (AWS ALB/Nginx) to distribute traffic.
- Stateless microservices ensure easy scaling.
- Auto-scaling policies (CPU/Memory based) on AWS EC2/EKS.

### 2. Database Optimization
- Use **AWS RDS with Multi-AZ** for failover and high availability.
- Add **read replicas** for read-heavy operations (catalog browsing, reporting).
- Add **indexes** on commonly queried fields
- Use **partitioning** for large tables (orders grow rapidly).
- Enable **connection pooling** (HikariCP).

### 3. Caching Layer (Redis)
- Cache product reads to reduce DB queries.
- Cache frequently accessed order summaries.
- Useful for rate limiting as well.

### 4. Queue-Based Load Management
- Use RabbitMQ (or migrate to Kafka for higher throughput).
- Offload heavy operations:
    - Email/SMS notifications.
    - Inventory updates.
    - Order status workflows.
- Messages ensure smooth processing even during peak loads.

### 5. Circuit Breakers & Timeouts (Resilience4j)
- Prevent system overload when one service fails.
- Auto retries, fallbacks, and throttling protect upstream microservices.

### 6. API Rate Limiting
- Prevent malicious or accidental traffic bursts.
- Implement Bucket4j/Redis-based throttling.
- Helps prevent DB starvation.

### 7. Distributed Tracing & Monitoring
- Use OpenTelemetry + Jaeger/Zipkin to trace requests across microservices.
- Integrate CloudWatch/Grafana for metrics:
    - Request latency
    - Queue backlog
    - DB slow queries
    - Error spikes

### 8. Containerization & Orchestration
- Use Docker for environment consistency.
- Deploy using Kubernetes (EKS) for:
    - Auto-healing
    - Rolling deployments
    - Auto-scaling
    - Secret management

### 9. CI/CD Automation
- GitHub Actions / Jenkins for:
    - Automated builds
    - Unit tests
    - Security scans (Snyk/OWASP)
    - Blue-green deployments

### 🔹 Summary
By combining **horizontal scaling**, **database optimization**, **queue-based async processing**, and **cloud-native orchestration**, the system becomes fully capable of handling high production traffic such as festive season sales, flash deals, or global user spikes.


# 🛠️ Local Setup Guide

# 🛒 E-Commerce Platform Setup Guide

This guide provides the necessary steps to clone the repository and start the required services for the E-Commerce Platform microservices project.

## 📥 Clone the Repository
Start by cloning the project from GitHub and navigating into the directory:

#### 5. Clone and Build
```bash
# Clone repository
git clone https://github.com/SHASHIDHAR2001/E-Commerce-Platform
cd E-Commerce-Platform

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

