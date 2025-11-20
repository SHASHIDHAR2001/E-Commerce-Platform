# E-Commerce Order Management System

## Overview

This is a production-ready microservices-based Order Management System built with Spring Boot that handles product inventory and order processing. The system uses asynchronous message processing for scalability and implements pessimistic locking to prevent race conditions during concurrent inventory operations. The architecture follows REST API design principles with comprehensive error handling and validation.

**Current Status**: ✅ Fully Functional - Both services are running and tested successfully
- **Inventory Service**: Running on port 8000 with REST APIs and database integration
- **Order Service**: Running on port 8080 with REST APIs and database integration
- **Database**: PostgreSQL (NeonDB) connected and tables created
- **APIs Tested**: Product creation and order placement working correctly

## User Preferences

Preferred communication style: Simple, everyday language.

## System Architecture

### Microservices Design

**Problem**: Need to build a scalable e-commerce system that can handle inventory and orders independently.

**Solution**: Two independent microservices architecture:
- **Inventory Service** (Port 8000): Manages products and stock levels
- **Order Service** (Port 8080): Handles order placement and status tracking

**Rationale**: Separation allows independent scaling, deployment, and development of inventory vs order functionality. Each service can have its own database and scaling strategy.

### Technology Choices

**Backend Framework**: Spring Boot 3.2.0 with Java 17
- Provides robust dependency injection, transaction management, and production-ready features
- Excellent ecosystem for microservices (Spring Cloud compatibility)
- Built-in support for REST APIs, validation, and ORM

**ORM**: Hibernate/JPA
- Database-agnostic persistence layer
- Automatic schema generation and migration support
- Built-in support for pessimistic locking

### Concurrency Control

**Problem**: Prevent overselling when multiple orders try to reduce stock simultaneously.

**Solution**: Pessimistic locking on inventory updates
- Locks database rows during stock reduction operations
- Ensures sequential processing of concurrent stock updates

**Alternatives Considered**: Optimistic locking
**Pros**: Guarantees data consistency under high concurrency
**Cons**: May reduce throughput compared to optimistic locking in low-contention scenarios

### Asynchronous Processing

**Message Queue**: RabbitMQ
- Decouples order processing from inventory updates
- Enables retry mechanisms for failed operations
- Provides message persistence and delivery guarantees

**Use Case**: Order confirmation triggers async inventory reduction messages

### API Design

**Documentation**: Swagger/OpenAPI 3.0
- Interactive API testing via Swagger UI
- Auto-generated API documentation from code annotations
- Standardized API contract definition

**REST Principles**:
- Resource-based URLs (`/api/inventory/products`, `/api/orders`)
- HTTP methods for CRUD operations
- Meaningful HTTP status codes for different error scenarios
- JSON request/response format

### Order State Management

**State Transitions**: Pending → Confirmed → Shipped → Delivered
- Validates state transitions to prevent invalid status updates
- Business logic enforces sequential progression through order lifecycle

### Error Handling Strategy

**Approach**: Centralized exception handling with specific HTTP status codes
- 400 Bad Request: Validation errors
- 404 Not Found: Resource not found
- 409 Conflict: Business rule violations (e.g., insufficient stock)
- 500 Internal Server Error: Unexpected errors

**Logging**: SLF4J/Logback for structured logging across all operations

### Data Validation

**Input Validation**: Bean Validation (JSR-380)
- Validates request payloads before processing
- Returns detailed error messages with field-level information
- Prevents invalid data from entering the system

## External Dependencies

### Database
- **PostgreSQL** via NeonDB (cloud-hosted)
- Relational database for ACID compliance
- Supports pessimistic locking for concurrency control
- Each microservice has independent database schema

### Message Queue
- **RabbitMQ**: Message broker for async communication between services
- Handles order confirmation events and inventory update messages
- Provides message persistence and delivery acknowledgments

### Build & Dependencies
- **Maven**: Dependency management and build automation
- Spring Boot Starter dependencies for web, data JPA, validation, and AMQP

### API Testing
- **Postman Collection**: Pre-configured API requests for testing
- Includes examples for all inventory and order operations

### Infrastructure Requirements
- Java 17+ runtime environment
- PostgreSQL database instance
- RabbitMQ server for message queuing
- Ports 8000 (Inventory) and 8080 (Order) available