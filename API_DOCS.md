# API Documentation - React Commerce Backend

## Table of Contents
- [Overview](#overview)
- [Project Structure](#project-structure)
- [Technology Stack](#technology-stack)
- [Response Format](#response-format)
- [Authentication](#authentication)
- [Base URL](#base-url)
- [Common Patterns](#common-patterns)
- [Error Handling](#error-handling)
- [API Endpoints](#api-endpoints)
  - [Authentication](#authentication-endpoints)
  - [Products](#products-endpoints)
  - [Orders](#orders-endpoints)
  - [Vouchers](#vouchers-endpoints)
  - [Referrals](#referrals-endpoints)
  - [Customers](#customers-endpoints)
  - [Analytics](#analytics-endpoints)
- [Data Models](#data-models)
- [Status Codes](#status-codes)
- [Implementation Guide](#implementation-guide)

---

## Overview

This document describes the REST API specification for the React Commerce application backend built with **Java Spring Boot 3.5.x**. The API supports an e-commerce platform with multi-role support (Customer, Seller, Admin).

### Key Features
- JWT-based authentication with access tokens
- Role-based access control (RBAC) via `@PreAuthorize`
- Product management (CRUD operations)
- Order processing and tracking
- Voucher and referral system
- Customer management for sellers
- Analytics and reporting dashboard
- Centralized exception handling
- Consistent API response envelope

---

## Project Structure

Based on the Java Spring Boot Starter Template, the recommended structure:

```
src/
  main/
    java/com/commerce/app/
      config/              # Security, OpenAPI, WebSocket, Cache, Async
        SecurityConfig.java
        OpenAPIConfig.java
        WebSocketConfig.java
      controller/          # REST controllers
        AuthController.java
        ProductController.java
        OrderController.java
        VoucherController.java
        ReferralController.java
        CustomerController.java
        AnalyticsController.java
      dto/                 # Request/Response DTOs
        request/
          auth/
            LoginRequest.java
            RegisterRequest.java
          product/
            CreateProductRequest.java
            UpdateProductRequest.java
          order/
            CreateOrderRequest.java
          voucher/
            CreateVoucherRequest.java
        response/
          auth/
            LoginResponse.java
          product/
            ProductResponse.java
            ProductListResponse.java
          order/
            OrderResponse.java
          common/
            ApiResponse.java
            StatusResponse.java
            PageResponse.java
      entity/              # JPA entities
        BaseEntity.java    # Auditing fields (createdAt, updatedAt)
        User.java
        Product.java
        Order.java
        OrderItem.java
        Voucher.java
        ReferralCode.java
        Address.java
      enums/               # Enums
        UserRole.java      # CUSTOMER, SELLER, ADMIN
        OrderStatus.java   # PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED
        VoucherType.java   # PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING
        PaymentMethod.java # CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CASH_ON_DELIVERY
        PaymentStatus.java # PENDING, PAID, FAILED, REFUNDED
      exception/           # Custom exceptions
        GlobalExceptionHandler.java
        ResourceNotFoundException.java
        UnauthorizedException.java
        ValidationException.java
      repository/          # Spring Data JPA repositories
        UserRepository.java
        ProductRepository.java
        OrderRepository.java
        VoucherRepository.java
        ReferralCodeRepository.java
      security/            # JWT and Security
        JwtProvider.java
        JwtFilter.java
        JwtAuthenticationEntryPoint.java
      service/             # Business logic
        AuthService.java
        UserService.java
        ProductService.java
        OrderService.java
        VoucherService.java
        ReferralService.java
        CustomerService.java
        AnalyticsService.java
      utils/               # Utilities
        ResponseUtil.java  # Helper for building ApiResponse
    resources/
      application.properties
      .env.example
```

---

## Technology Stack

### Backend Stack
- **Language**: Java 21
- **Framework**: Spring Boot 3.5.7
- **Modules**: Web, Security, Validation, Data JPA, WebSocket, Retry, Cache
- **Database**: PostgreSQL 14+
- **ORM**: Spring Data JPA
- **Authentication**: Spring Security + JJWT (io.jsonwebtoken)
- **API Documentation**: springdoc-openapi-starter-webmvc-ui
- **Validation**: Jakarta Bean Validation
- **Build Tool**: Maven 3.9+
- **Environment Config**: spring-dotenv

### Key Dependencies
```xml
<!-- pom.xml -->
<dependencies>
    <!-- Spring Boot Starters -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>

    <!-- Database -->
    <dependency>
        <groupId>org.postgresql</groupId>
        <artifactId>postgresql</artifactId>
    </dependency>

    <!-- JWT -->
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-impl</artifactId>
        <version>0.12.3</version>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-jackson</artifactId>
        <version>0.12.3</version>
    </dependency>

    <!-- OpenAPI Documentation -->
    <dependency>
        <groupId>org.springdoc</groupId>
        <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        <version>2.3.0</version>
    </dependency>

    <!-- Environment Variables -->
    <dependency>
        <groupId>me.paulschwarz</groupId>
        <artifactId>spring-dotenv</artifactId>
        <version>4.0.0</version>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
    </dependency>
</dependencies>
```

---

## Response Format

All API responses follow a **consistent envelope structure** for success and error cases using `ResponseUtil`.

### Success Response Structure (Single Data)

```json
{
  "status": {
    "code": 200,
    "description": "Success message"
  },
  "data": {
    // Single object response payload
  }
}
```

**Generated by**: `ResponseUtil.buildSingleResponse(HttpStatus, String message, T data)`

### Success Response with Pagination

```json
{
  "status": {
    "code": 200,
    "description": "Success message"
  },
  "data": [
    // Array of items
  ],
  "paging": {
    "page": 1,
    "rowsPerPage": 20,
    "totalRows": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Generated by**: `ResponseUtil.buildPagedResponse(HttpStatus, String message, Page<T> page)`

**Note**:
- Page numbering starts from **1** (not 0)
- `paging` object is separate from `data` array
- Use `Page<T>` from Spring Data for automatic pagination

### Error Response Structure

```json
{
  "status": {
    "code": 400,
    "description": "Bad Request - Validation failed"
  },
  "errors": [
    "Price must be greater than 0",
    "Product name is required"
  ]
}
```

**Generated by**: `ResponseUtil.buildErrorResponse(HttpStatus, String message, List<String> errors)`

### Common Status Descriptions

| HTTP Code | Description Example |
|-----------|---------------------|
| 200 | OK |
| 201 | Created |
| 204 | No Content |
| 400 | Bad Request - Validation failed |
| 401 | Unauthorized - Invalid or missing token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource does not exist |
| 409 | Conflict - Resource already exists |
| 500 | Internal Server Error |

---

## Authentication

### Authentication Flow

The API uses **JWT (JSON Web Token)** for authentication following the starter template pattern.

#### 1. User Registration
```
POST /api/v1/auth/register
Response: { "status": {...}, "data": { "accessToken": "...", "tokenType": "Bearer", "role": "ROLE_CUSTOMER" } }
```

#### 2. User Login
```
POST /api/v1/auth/login
Response: { "status": {...}, "data": { "accessToken": "...", "tokenType": "Bearer", "role": "ROLE_SELLER" } }
```

#### 3. Making Authenticated Requests
Include the JWT token in the Authorization header:
```
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

#### 4. Token Configuration
Configure in `.env`:
```
JWT_SECRET=your-256-bit-secret-key-here
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=1209600000
JWT_ISSUER=react-commerce
```

### User Roles (Spring Security)
- **ROLE_CUSTOMER**: Browse products, place orders, manage cart/wishlist
- **ROLE_SELLER**: Manage products, view orders, access analytics
- **ROLE_ADMIN**: Full system access (optional)

### Authorization Annotations
```java
@PreAuthorize("hasRole('SELLER')")
@PreAuthorize("hasAnyRole('SELLER', 'ADMIN')")
@PreAuthorize("hasRole('CUSTOMER')")
```

---

## Base URL

```
Development: http://localhost:9999/api/v1
Production: https://api.yourcommerce.com/api/v1
```

All endpoints are versioned and prefixed with `/api/v1`.

---

## Common Patterns

### Pagination

All list endpoints support pagination using query parameters:

```
GET /api/v1/products?page=0&size=20
```

**Parameters:**
- `page`: Page number (0-indexed for request, default: 0)
- `size`: Items per page (default: 20, max: 100)

**Response Structure:**
```json
{
  "status": {
    "code": 200,
    "description": "Products retrieved successfully"
  },
  "data": [
    // Array of items
  ],
  "paging": {
    "page": 1,
    "rowsPerPage": 20,
    "totalRows": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Key Points:**
- Request uses **0-indexed** page numbers (Spring Data convention)
- Response uses **1-indexed** page numbers (converted by ResponseUtil)
- `data` contains the array of items
- `paging` object is separate and contains metadata

### Filtering

Endpoints support multiple filter parameters:

```
GET /api/v1/products?category=electronics&minPrice=100&maxPrice=500
```

### Sorting

```
GET /api/v1/products?sortBy=price&sortDir=asc
```

**Parameters:**
- `sortBy`: Field name to sort by
- `sortDir`: Sort direction (`asc` or `desc`)

### Search

```
GET /api/v1/products?search=laptop
```

---

## Error Handling

### GlobalExceptionHandler

All exceptions are handled centrally in `GlobalExceptionHandler.java` and return consistent error responses.

### Error Response Format

```json
{
  "status": {
    "code": 400,
    "description": "Bad Request - Validation failed"
  },
  "errors": [
    "Email is required",
    "Password must be at least 8 characters"
  ]
}
```

### Common Error Scenarios

| Scenario | HTTP Code | Response Example |
|----------|-----------|------------------|
| Validation failure | 400 | `{ "status": {...}, "errors": ["Price must be greater than 0"] }` |
| Authentication missing | 401 | `{ "status": { "code": 401, "description": "Unauthorized - Invalid or missing token" }, "errors": ["You must provide a valid access token"] }` |
| Invalid credentials | 401 | `{ "status": {...}, "errors": ["Invalid username or password"] }` |
| Insufficient permissions | 403 | `{ "status": { "code": 403, "description": "Forbidden - Insufficient permissions" }, "errors": ["You don't have permission to access this resource"] }` |
| Resource not found | 404 | `{ "status": { "code": 404, "description": "Not Found - Resource does not exist" }, "errors": ["Product with ID 123 not found"] }` |
| Duplicate resource | 409 | `{ "status": { "code": 409, "description": "Conflict - Resource already exists" }, "errors": ["Email already registered"] }` |
| Server error | 500 | `{ "status": { "code": 500, "description": "Internal Server Error" }, "errors": ["An unexpected error occurred"] }` |

---

## API Endpoints

### Authentication Endpoints

#### 1. User Registration

**POST** `/api/v1/auth/register`

Register a new user account.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "firstName": "John",
  "lastName": "Doe",
  "role": "CUSTOMER"
}
```

**Validation Rules:**
- `email`: Valid email format, unique, required
- `password`: Min 8 characters, must contain uppercase, lowercase, and number
- `firstName`: Required, 2-50 characters
- `lastName`: Required, 2-50 characters
- `role`: Enum [CUSTOMER, SELLER]

**Response:** `201 Created`
```json
{
  "status": {
    "code": 201,
    "description": "Created"
  },
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "role": "ROLE_CUSTOMER",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "CUSTOMER"
    }
  }
}
```

**Error Responses:**
- `400`: Validation error
```json
{
  "status": {
    "code": 400,
    "description": "Bad Request - Validation failed"
  },
  "errors": [
    "Password must contain at least one uppercase letter",
    "Email format is invalid"
  ]
}
```
- `409`: Email already registered
```json
{
  "status": {
    "code": 409,
    "description": "Conflict - Resource already exists"
  },
  "errors": ["Email already registered"]
}
```

---

#### 2. User Login

**POST** `/api/v1/auth/login`

Authenticate user and receive JWT token.

**Request Body:**
```json
{
  "email": "john.doe@example.com",
  "password": "SecurePassword123!",
  "role": "CUSTOMER"
}
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
    "tokenType": "Bearer",
    "role": "ROLE_CUSTOMER",
    "user": {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "email": "john.doe@example.com",
      "firstName": "John",
      "lastName": "Doe",
      "role": "CUSTOMER"
    }
  }
}
```

**Error Responses:**
- `401`: Invalid credentials
```json
{
  "status": {
    "code": 401,
    "description": "Unauthorized - Invalid credentials"
  },
  "errors": ["Invalid email or password"]
}
```
- `403`: Role mismatch
```json
{
  "status": {
    "code": 403,
    "description": "Forbidden - Role mismatch"
  },
  "errors": ["User role does not match requested role"]
}
```

---

#### 3. Get Current User

**GET** `/api/v1/auth/me`

Get authenticated user's profile.

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "email": "john.doe@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "role": "CUSTOMER",
    "phone": "+1234567890",
    "createdAt": "2025-01-15T10:30:45.123Z",
    "updatedAt": "2025-01-15T10:30:45.123Z"
  }
}
```

**Error Responses:**
- `401`: Unauthorized
```json
{
  "status": {
    "code": 401,
    "description": "Unauthorized - Invalid or missing token"
  },
  "errors": ["You must provide a valid access token"]
}
```

---

#### 4. Logout

**POST** `/api/v1/auth/logout`

Invalidate JWT token (implement token blacklist in Redis/database).

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** `204 No Content`

---

### Products Endpoints

#### 1. Get All Products

**GET** `/api/v1/products`

Retrieve paginated list of products with optional filtering.

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search in name/description
- `category`: Filter by category
- `brand`: Filter by brand
- `sellerId`: Filter by seller UUID
- `minPrice`: Minimum price
- `maxPrice`: Maximum price
- `inStock`: Boolean filter for stock availability
- `sortBy`: Sort field (price, name, createdAt)
- `sortDir`: Sort direction (asc, desc)

**Example:**
```
GET /api/v1/products?category=electronics&minPrice=100&maxPrice=500&page=0&size=20&sortBy=price&sortDir=asc
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "Products retrieved successfully"
  },
  "data": [
    {
      "id": "550e8400-e29b-41d4-a716-446655440000",
      "name": "Wireless Headphones",
      "description": "Premium noise-cancelling headphones",
      "price": 299.99,
      "compareAtPrice": 399.99,
      "category": "electronics",
      "brand": "AudioTech",
      "stock": 45,
      "images": [
        "https://storage.example.com/products/img1.jpg",
        "https://storage.example.com/products/img2.jpg"
      ],
      "seller": {
        "id": "seller-uuid",
        "name": "Premium Electronics Store"
      },
      "rating": 4.5,
      "reviewCount": 128,
      "createdAt": "2025-01-10T08:00:00Z",
      "updatedAt": "2025-01-15T10:30:00Z"
    }
  ],
  "paging": {
    "page": 1,
    "rowsPerPage": 20,
    "totalRows": 150,
    "totalPages": 8,
    "hasNext": true,
    "hasPrevious": false
  }
}
```

**Public Access:** No authentication required

---

#### 2. Get Product by ID

**GET** `/api/v1/products/{id}`

Retrieve single product details.

**Path Parameters:**
- `id`: Product UUID

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Wireless Headphones",
    "description": "Premium noise-cancelling headphones with 30-hour battery life",
    "price": 299.99,
    "compareAtPrice": 399.99,
    "category": "electronics",
    "brand": "AudioTech",
    "stock": 45,
    "images": [
      "https://storage.example.com/products/img1.jpg",
      "https://storage.example.com/products/img2.jpg"
    ],
    "seller": {
      "id": "seller-uuid",
      "name": "Premium Electronics Store",
      "email": "seller@example.com"
    },
    "rating": 4.5,
    "reviewCount": 128,
    "specifications": {
      "color": "Black",
      "weight": "250g",
      "connectivity": "Bluetooth 5.0"
    },
    "createdAt": "2025-01-10T08:00:00Z",
    "updatedAt": "2025-01-15T10:30:00Z"
  }
}
```

**Error Responses:**
- `404`: Product not found
```json
{
  "status": {
    "code": 404,
    "description": "Not Found - Resource does not exist"
  },
  "errors": ["Product with ID 550e8400-e29b-41d4-a716-446655440000 not found"]
}
```

**Public Access:** No authentication required

---

#### 3. Create Product

**POST** `/api/v1/products`

Create a new product (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse with precision tracking",
  "price": 49.99,
  "compareAtPrice": 79.99,
  "category": "electronics",
  "brand": "TechGear",
  "stock": 100,
  "images": [
    "https://storage.example.com/products/mouse1.jpg",
    "https://storage.example.com/products/mouse2.jpg"
  ],
  "specifications": {
    "color": "Silver",
    "connectivity": "2.4GHz Wireless",
    "battery": "AA"
  }
}
```

**Validation Rules:**
- `name`: Required, 3-200 characters
- `description`: Optional, max 2000 characters
- `price`: Required, > 0
- `compareAtPrice`: Optional, must be >= price
- `category`: Required
- `brand`: Required
- `stock`: Required, >= 0
- `images`: Optional, max 10 URLs
- `specifications`: Optional, JSON object

**Response:** `201 Created`
```json
{
  "status": {
    "code": 201,
    "description": "Created"
  },
  "data": {
    "id": "new-product-uuid",
    "name": "Wireless Mouse",
    "description": "Ergonomic wireless mouse with precision tracking",
    "price": 49.99,
    "compareAtPrice": 79.99,
    "category": "electronics",
    "brand": "TechGear",
    "stock": 100,
    "images": [
      "https://storage.example.com/products/mouse1.jpg",
      "https://storage.example.com/products/mouse2.jpg"
    ],
    "seller": {
      "id": "current-seller-uuid",
      "name": "Current Seller Name"
    },
    "rating": 0,
    "reviewCount": 0,
    "specifications": {
      "color": "Silver",
      "connectivity": "2.4GHz Wireless",
      "battery": "AA"
    },
    "createdAt": "2025-01-15T11:00:00Z",
    "updatedAt": "2025-01-15T11:00:00Z"
  }
}
```

**Error Responses:**
- `400`: Validation error
- `401`: Unauthorized
- `403`: Not a seller account

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

#### 4. Update Product

**PUT** `/api/v1/products/{id}`

Update existing product (Seller can only update their own products).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Product UUID

**Request Body:** Same as Create Product

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "product-uuid",
    "name": "Wireless Mouse - Updated",
    "description": "Updated description",
    "price": 54.99,
    "compareAtPrice": 79.99,
    "category": "electronics",
    "brand": "TechGear",
    "stock": 85,
    "images": [
      "https://storage.example.com/products/mouse1-new.jpg"
    ],
    "seller": {
      "id": "seller-uuid",
      "name": "Current Seller Name"
    },
    "rating": 4.2,
    "reviewCount": 15,
    "specifications": {
      "color": "Silver",
      "connectivity": "2.4GHz Wireless",
      "battery": "AA"
    },
    "createdAt": "2025-01-15T11:00:00Z",
    "updatedAt": "2025-01-15T12:30:00Z"
  }
}
```

**Error Responses:**
- `400`: Validation error
- `401`: Unauthorized
- `403`: Not the product owner
- `404`: Product not found

**Required Role:** `@PreAuthorize("hasRole('SELLER')")` + ownership check

---

#### 5. Delete Product

**DELETE** `/api/v1/products/{id}`

Delete a product (Seller can only delete their own products).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Product UUID

**Response:** `204 No Content`

**Error Responses:**
- `401`: Unauthorized
- `403`: Not the product owner
- `404`: Product not found
- `409`: Cannot delete product with active orders
```json
{
  "status": {
    "code": 409,
    "description": "Conflict - Cannot delete resource"
  },
  "errors": ["Cannot delete product with active orders"]
}
```

**Required Role:** `@PreAuthorize("hasRole('SELLER')")` + ownership check

---

#### 6. Get Categories

**GET** `/api/v1/products/categories`

Get list of all unique product categories.

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "categories": [
      "electronics",
      "fashion",
      "home-garden",
      "sports",
      "books",
      "toys"
    ]
  }
}
```

**Public Access:** No authentication required

---

#### 7. Get Brands

**GET** `/api/v1/products/brands`

Get list of all unique product brands.

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "brands": [
      "AudioTech",
      "TechGear",
      "FashionCo",
      "SportsPro",
      "HomeEssentials"
    ]
  }
}
```

**Public Access:** No authentication required

---

### Orders Endpoints

#### 1. Get All Orders

**GET** `/api/v1/orders`

Get orders based on user role:
- **Customer**: Gets their own orders
- **Seller**: Gets orders containing their products

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `status`: Filter by status (PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED)
- `startDate`: Filter from date (ISO 8601)
- `endDate`: Filter to date (ISO 8601)
- `sortBy`: Sort field (createdAt, total)
- `sortDir`: Sort direction (asc, desc)

**Example:**
```
GET /api/v1/orders?status=PROCESSING&page=0&size=20&sortBy=createdAt&sortDir=desc
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "content": [
      {
        "id": "order-uuid",
        "orderNumber": "ORD-2025-001234",
        "customerId": "customer-uuid",
        "customerName": "John Doe",
        "customerEmail": "john@example.com",
        "status": "PROCESSING",
        "items": [
          {
            "id": "item-uuid",
            "productId": "product-uuid",
            "productName": "Wireless Headphones",
            "productImage": "https://storage.example.com/products/img1.jpg",
            "quantity": 2,
            "price": 299.99,
            "subtotal": 599.98
          }
        ],
        "subtotal": 599.98,
        "discount": 50.00,
        "shipping": 10.00,
        "tax": 55.00,
        "total": 614.98,
        "shippingAddress": {
          "fullName": "John Doe",
          "phone": "+1234567890",
          "address": "123 Main St",
          "city": "New York",
          "state": "NY",
          "zipCode": "10001",
          "country": "USA"
        },
        "paymentMethod": "CREDIT_CARD",
        "paymentStatus": "PAID",
        "voucherCode": "SAVE50",
        "referralCode": null,
        "notes": "Please deliver before 5 PM",
        "createdAt": "2025-01-15T10:00:00Z",
        "updatedAt": "2025-01-15T11:30:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 45,
    "totalPages": 3,
    "first": true,
    "last": false
  }
}
```

**Error Responses:**
- `401`: Unauthorized

**Required Role:** `@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")`

---

#### 2. Get Order by ID

**GET** `/api/v1/orders/{id}`

Get single order details.

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Order UUID

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "order-uuid",
    "orderNumber": "ORD-2025-001234",
    "customerId": "customer-uuid",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "status": "PROCESSING",
    "items": [
      {
        "id": "item-uuid",
        "productId": "product-uuid",
        "productName": "Wireless Headphones",
        "productImage": "https://storage.example.com/products/img1.jpg",
        "sellerId": "seller-uuid",
        "sellerName": "Premium Electronics Store",
        "quantity": 2,
        "price": 299.99,
        "subtotal": 599.98
      }
    ],
    "subtotal": 599.98,
    "discount": 50.00,
    "shipping": 10.00,
    "tax": 55.00,
    "total": 614.98,
    "shippingAddress": {
      "fullName": "John Doe",
      "phone": "+1234567890",
      "address": "123 Main St",
      "city": "New York",
      "state": "NY",
      "zipCode": "10001",
      "country": "USA"
    },
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PAID",
    "voucherCode": "SAVE50",
    "referralCode": null,
    "notes": "Please deliver before 5 PM",
    "statusHistory": [
      {
        "status": "PENDING",
        "timestamp": "2025-01-15T10:00:00Z"
      },
      {
        "status": "PROCESSING",
        "timestamp": "2025-01-15T11:30:00Z"
      }
    ],
    "createdAt": "2025-01-15T10:00:00Z",
    "updatedAt": "2025-01-15T11:30:00Z"
  }
}
```

**Error Responses:**
- `401`: Unauthorized
- `403`: Not authorized to view this order
- `404`: Order not found

**Required Role:** `@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")` + ownership check

---

#### 3. Create Order

**POST** `/api/v1/orders`

Create a new order (checkout).

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "items": [
    {
      "productId": "product-uuid",
      "quantity": 2
    },
    {
      "productId": "another-product-uuid",
      "quantity": 1
    }
  ],
  "shippingAddress": {
    "fullName": "John Doe",
    "phone": "+1234567890",
    "address": "123 Main St",
    "city": "New York",
    "state": "NY",
    "zipCode": "10001",
    "country": "USA"
  },
  "paymentMethod": "CREDIT_CARD",
  "voucherCode": "SAVE50",
  "referralCode": "REF123",
  "notes": "Please deliver before 5 PM"
}
```

**Validation Rules:**
- `items`: Required, min 1 item
- `items[].productId`: Valid product UUID
- `items[].quantity`: > 0, <= available stock
- `shippingAddress`: All fields required
- `paymentMethod`: Enum [CREDIT_CARD, DEBIT_CARD, BANK_TRANSFER, CASH_ON_DELIVERY]
- `voucherCode`: Optional, must be valid and applicable
- `referralCode`: Optional, must be valid

**Response:** `201 Created`
```json
{
  "status": {
    "code": 201,
    "description": "Created"
  },
  "data": {
    "id": "new-order-uuid",
    "orderNumber": "ORD-2025-001235",
    "customerId": "customer-uuid",
    "customerName": "John Doe",
    "customerEmail": "john@example.com",
    "status": "PENDING",
    "items": [...],
    "subtotal": 599.98,
    "discount": 50.00,
    "shipping": 10.00,
    "tax": 55.00,
    "total": 614.98,
    "shippingAddress": {...},
    "paymentMethod": "CREDIT_CARD",
    "paymentStatus": "PENDING",
    "voucherCode": "SAVE50",
    "referralCode": "REF123",
    "notes": "Please deliver before 5 PM",
    "createdAt": "2025-01-15T12:00:00Z",
    "updatedAt": "2025-01-15T12:00:00Z"
  }
}
```

**Error Responses:**
- `400`: Validation error (invalid voucher, insufficient stock, etc.)
```json
{
  "status": {
    "code": 400,
    "description": "Bad Request - Validation failed"
  },
  "errors": [
    "Insufficient stock for product: Wireless Headphones",
    "Voucher code has expired"
  ]
}
```
- `401`: Unauthorized
- `404`: Product not found

**Required Role:** `@PreAuthorize("hasRole('CUSTOMER')")`

---

#### 4. Update Order Status

**PATCH** `/api/v1/orders/{id}/status`

Update order status (Seller only for their products' orders).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Order UUID

**Request Body:**
```json
{
  "status": "SHIPPED"
}
```

**Valid Status Transitions:**
- PENDING → PROCESSING or CANCELLED
- PROCESSING → SHIPPED or CANCELLED
- SHIPPED → DELIVERED
- DELIVERED → (final state)
- CANCELLED → (final state)

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "order-uuid",
    "orderNumber": "ORD-2025-001234",
    "status": "SHIPPED",
    "updatedAt": "2025-01-15T14:00:00Z"
  }
}
```

**Error Responses:**
- `400`: Invalid status transition
```json
{
  "status": {
    "code": 400,
    "description": "Bad Request - Invalid operation"
  },
  "errors": ["Cannot transition from DELIVERED to SHIPPED"]
}
```
- `401`: Unauthorized
- `403`: Not authorized to update this order
- `404`: Order not found

**Required Role:** `@PreAuthorize("hasRole('SELLER')")` + ownership check

---

#### 5. Cancel Order

**POST** `/api/v1/orders/{id}/cancel`

Cancel an order.
- **Customer**: Can cancel PENDING orders only
- **Seller**: Can cancel PENDING and PROCESSING orders

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Order UUID

**Request Body:**
```json
{
  "reason": "Changed my mind"
}
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "order-uuid",
    "orderNumber": "ORD-2025-001234",
    "status": "CANCELLED",
    "cancelReason": "Changed my mind",
    "cancelledAt": "2025-01-15T14:30:00Z"
  }
}
```

**Error Responses:**
- `400`: Order cannot be cancelled
```json
{
  "status": {
    "code": 400,
    "description": "Bad Request - Invalid operation"
  },
  "errors": ["Cannot cancel order that has already been shipped"]
}
```
- `401`: Unauthorized
- `403`: Not authorized to cancel this order
- `404`: Order not found

**Required Role:** `@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")` + status check

---

### Vouchers Endpoints

#### 1. Get Vouchers

**GET** `/api/v1/vouchers`

Get vouchers:
- **Seller**: Gets their own vouchers
- **Customer**: Gets available vouchers

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `active`: Filter active vouchers (boolean)
- `type`: Filter by type (PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING)

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "content": [
      {
        "id": "voucher-uuid",
        "code": "SAVE50",
        "name": "50% Off Electronics",
        "description": "Get 50% off on all electronics",
        "type": "PERCENTAGE",
        "value": 50,
        "minPurchase": 100.00,
        "maxDiscount": 200.00,
        "usageLimit": 100,
        "usageCount": 45,
        "userUsageLimit": 1,
        "startDate": "2025-01-01T00:00:00Z",
        "endDate": "2025-12-31T23:59:59Z",
        "isActive": true,
        "sellerId": "seller-uuid",
        "sellerName": "Premium Electronics Store",
        "createdAt": "2024-12-20T10:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 12,
    "totalPages": 1,
    "first": true,
    "last": true
  }
}
```

**Required Role:** `@PreAuthorize("hasAnyRole('CUSTOMER', 'SELLER')")`

---

#### 2. Create Voucher

**POST** `/api/v1/vouchers`

Create a new voucher (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "code": "NEWUSER20",
  "name": "New User Discount",
  "description": "20% off for new users",
  "type": "PERCENTAGE",
  "value": 20,
  "minPurchase": 50.00,
  "maxDiscount": 100.00,
  "usageLimit": 500,
  "userUsageLimit": 1,
  "startDate": "2025-01-20T00:00:00Z",
  "endDate": "2025-02-28T23:59:59Z",
  "isActive": true
}
```

**Validation Rules:**
- `code`: Required, 4-20 uppercase characters, unique
- `name`: Required, 3-100 characters
- `type`: Enum [PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING]
- `value`: Required, > 0; for PERCENTAGE must be <= 100
- `minPurchase`: Optional, >= 0
- `maxDiscount`: Optional (for PERCENTAGE), > 0
- `usageLimit`: Optional, > 0
- `userUsageLimit`: Optional, > 0
- `startDate`: Required, valid date
- `endDate`: Required, must be after startDate

**Response:** `201 Created`
```json
{
  "status": {
    "code": 201,
    "description": "Created"
  },
  "data": {
    "id": "new-voucher-uuid",
    "code": "NEWUSER20",
    "name": "New User Discount",
    "description": "20% off for new users",
    "type": "PERCENTAGE",
    "value": 20,
    "minPurchase": 50.00,
    "maxDiscount": 100.00,
    "usageLimit": 500,
    "usageCount": 0,
    "userUsageLimit": 1,
    "startDate": "2025-01-20T00:00:00Z",
    "endDate": "2025-02-28T23:59:59Z",
    "isActive": true,
    "sellerId": "seller-uuid",
    "sellerName": "Current Seller Name",
    "createdAt": "2025-01-15T15:00:00Z"
  }
}
```

**Error Responses:**
- `400`: Validation error
- `401`: Unauthorized
- `403`: Not a seller account
- `409`: Voucher code already exists

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

#### 3. Update Voucher

**PUT** `/api/v1/vouchers/{id}`

Update existing voucher (Seller can only update own vouchers).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Voucher UUID

**Request Body:** Same as Create Voucher

**Response:** `200 OK` (Same structure as Create response)

**Error Responses:**
- `400`: Validation error
- `401`: Unauthorized
- `403`: Not the voucher owner
- `404`: Voucher not found
- `409`: Voucher code already exists (if code changed)

**Required Role:** `@PreAuthorize("hasRole('SELLER')")` + ownership check

---

#### 4. Delete Voucher

**DELETE** `/api/v1/vouchers/{id}`

Delete a voucher (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Voucher UUID

**Response:** `204 No Content`

**Error Responses:**
- `401`: Unauthorized
- `403`: Not the voucher owner
- `404`: Voucher not found

**Required Role:** `@PreAuthorize("hasRole('SELLER')")` + ownership check

---

#### 5. Validate Voucher

**POST** `/api/v1/vouchers/validate`

Validate voucher code for checkout (Customer only).

**Headers:**
```
Authorization: Bearer {token}
```

**Request Body:**
```json
{
  "code": "SAVE50",
  "cartTotal": 350.00
}
```

**Response (Valid):** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "isValid": true,
    "message": "Voucher applied successfully",
    "voucher": {
      "id": "voucher-uuid",
      "code": "SAVE50",
      "type": "PERCENTAGE",
      "value": 50
    },
    "discountAmount": 175.00,
    "finalTotal": 175.00
  }
}
```

**Response (Invalid):** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "isValid": false,
    "message": "Voucher has expired",
    "discountAmount": 0
  }
}
```

**Validation Checks:**
- Code exists
- Voucher is active
- Current date between start and end date
- Cart total >= minimum purchase
- Usage limit not exceeded
- User usage limit not exceeded

**Required Role:** `@PreAuthorize("hasRole('CUSTOMER')")`

---

### Referrals Endpoints

#### 1. Get Referral Codes

**GET** `/api/v1/referrals`

Get customer's own referral codes.

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "referralCodes": [
      {
        "id": "referral-uuid",
        "code": "JOHN-REF-ABC123",
        "userId": "customer-uuid",
        "userName": "John Doe",
        "usageCount": 5,
        "rewardAmount": 5.00,
        "totalEarnings": 25.00,
        "isActive": true,
        "createdAt": "2025-01-10T10:00:00Z"
      }
    ]
  }
}
```

**Required Role:** `@PreAuthorize("hasRole('CUSTOMER')")`

---

#### 2. Generate Referral Code

**POST** `/api/v1/referrals/generate`

Generate a new referral code for the user.

**Headers:**
```
Authorization: Bearer {token}
```

**Response:** `201 Created`
```json
{
  "status": {
    "code": 201,
    "description": "Created"
  },
  "data": {
    "id": "new-referral-uuid",
    "code": "JOHN-REF-XYZ789",
    "userId": "customer-uuid",
    "userName": "John Doe",
    "usageCount": 0,
    "rewardAmount": 5.00,
    "totalEarnings": 0,
    "isActive": true,
    "createdAt": "2025-01-15T16:00:00Z"
  }
}
```

**Error Responses:**
- `401`: Unauthorized
- `409`: User already has an active referral code
```json
{
  "status": {
    "code": 409,
    "description": "Conflict - Resource already exists"
  },
  "errors": ["User already has an active referral code"]
}
```

**Required Role:** `@PreAuthorize("hasRole('CUSTOMER')")`

---

#### 3. Validate Referral Code

**POST** `/api/v1/referrals/validate`

Validate referral code for new user signup.

**Request Body:**
```json
{
  "code": "JOHN-REF-ABC123"
}
```

**Response (Valid):** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "isValid": true,
    "message": "Referral code is valid",
    "referralCode": {
      "code": "JOHN-REF-ABC123",
      "referrerName": "John Doe",
      "rewardAmount": 5.00
    }
  }
}
```

**Response (Invalid):** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "isValid": false,
    "message": "Referral code not found or inactive"
  }
}
```

**Public Access:** No authentication required

---

### Customers Endpoints

#### 1. Get All Customers

**GET** `/api/v1/customers`

Get list of customers who purchased from seller (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `page`: Page number (default: 0)
- `size`: Page size (default: 20)
- `search`: Search by name or email
- `sortBy`: Sort field (totalSpent, lastOrderDate)
- `sortDir`: Sort direction (asc, desc)

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "content": [
      {
        "id": "customer-uuid",
        "email": "john@example.com",
        "firstName": "John",
        "lastName": "Doe",
        "totalOrders": 15,
        "totalSpent": 1250.00,
        "lastOrderDate": "2025-01-15T10:00:00Z",
        "createdAt": "2024-06-01T08:00:00Z"
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 87,
    "totalPages": 5,
    "first": true,
    "last": false
  }
}
```

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

#### 2. Get Customer by ID

**GET** `/api/v1/customers/{id}`

Get customer details with order history (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Path Parameters:**
- `id`: Customer UUID

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "id": "customer-uuid",
    "email": "john@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "phone": "+1234567890",
    "totalOrders": 15,
    "totalSpent": 1250.00,
    "averageOrderValue": 83.33,
    "lastOrderDate": "2025-01-15T10:00:00Z",
    "recentOrders": [
      {
        "id": "order-uuid",
        "orderNumber": "ORD-2025-001234",
        "total": 614.98,
        "status": "DELIVERED",
        "createdAt": "2025-01-15T10:00:00Z"
      }
    ],
    "addresses": [
      {
        "id": "address-uuid",
        "fullName": "John Doe",
        "phone": "+1234567890",
        "address": "123 Main St",
        "city": "New York",
        "state": "NY",
        "zipCode": "10001",
        "country": "USA",
        "isDefault": true
      }
    ],
    "createdAt": "2024-06-01T08:00:00Z"
  }
}
```

**Error Responses:**
- `401`: Unauthorized
- `403`: Not authorized to view this customer
- `404`: Customer not found

**Required Role:** `@PreAuthorize("hasRole('SELLER')")` + relationship check

---

### Analytics Endpoints

#### 1. Get Revenue Analytics

**GET** `/api/v1/analytics/revenue`

Get revenue analytics (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `startDate`: Start date (ISO format, default: 30 days ago)
- `endDate`: End date (ISO format, default: today)
- `granularity`: Data granularity [DAY, WEEK, MONTH] (default: DAY)

**Example:**
```
GET /api/v1/analytics/revenue?startDate=2025-01-01&endDate=2025-01-31&granularity=DAY
```

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "totalRevenue": 15750.00,
    "totalOrders": 125,
    "averageOrderValue": 126.00,
    "growthRate": 12.5,
    "data": [
      {
        "date": "2025-01-01",
        "revenue": 450.00,
        "orders": 5
      },
      {
        "date": "2025-01-02",
        "revenue": 680.00,
        "orders": 7
      }
    ]
  }
}
```

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

#### 2. Get Orders Analytics

**GET** `/api/v1/analytics/orders`

Get orders analytics by status (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `startDate`: Start date (default: 30 days ago)
- `endDate`: End date (default: today)

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "totalOrders": 125,
    "byStatus": {
      "PENDING": 15,
      "PROCESSING": 25,
      "SHIPPED": 30,
      "DELIVERED": 50,
      "CANCELLED": 5
    },
    "completionRate": 95.8,
    "averageProcessingTime": 2.5
  }
}
```

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

#### 3. Get Products Analytics

**GET** `/api/v1/analytics/products`

Get top-performing products analytics (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `startDate`: Start date (default: 30 days ago)
- `endDate`: End date (default: today)
- `limit`: Number of top products (default: 10)

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "topProducts": [
      {
        "productId": "product-uuid",
        "productName": "Wireless Headphones",
        "totalSales": 3500.00,
        "unitsSold": 45,
        "averageRating": 4.5,
        "revenue": 13475.55
      }
    ],
    "lowStockProducts": [
      {
        "productId": "product-uuid",
        "productName": "Gaming Mouse",
        "currentStock": 5,
        "reorderPoint": 10
      }
    ]
  }
}
```

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

#### 4. Get Customers Analytics

**GET** `/api/v1/analytics/customers`

Get customer analytics (Seller only).

**Headers:**
```
Authorization: Bearer {token}
```

**Query Parameters:**
- `startDate`: Start date (default: 30 days ago)
- `endDate`: End date (default: today)

**Response:** `200 OK`
```json
{
  "status": {
    "code": 200,
    "description": "OK"
  },
  "data": {
    "totalCustomers": 87,
    "newCustomers": 12,
    "returningCustomers": 45,
    "retentionRate": 51.7,
    "topCustomers": [
      {
        "customerId": "customer-uuid",
        "customerName": "John Doe",
        "totalOrders": 15,
        "totalSpent": 1250.00
      }
    ]
  }
}
```

**Required Role:** `@PreAuthorize("hasRole('SELLER')")`

---

## Data Models

### Entity Models (JPA)

#### BaseEntity
```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
```

#### User
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role; // CUSTOMER, SELLER, ADMIN

    private String phone;
    private String avatar;
}
```

#### Product
```java
@Entity
@Table(name = "products")
public class Product extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private BigDecimal price;

    private BigDecimal compareAtPrice;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private String brand;

    @Column(nullable = false)
    private Integer stock;

    @ElementCollection
    private List<String> images;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    private Double rating = 0.0;
    private Integer reviewCount = 0;

    @Type(JsonBinaryType.class)
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> specifications;
}
```

#### Order
```java
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String orderNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status; // PENDING, PROCESSING, SHIPPED, DELIVERED, CANCELLED

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Column(nullable = false)
    private BigDecimal subtotal;

    private BigDecimal discount = BigDecimal.ZERO;
    private BigDecimal shipping = BigDecimal.ZERO;
    private BigDecimal tax = BigDecimal.ZERO;

    @Column(nullable = false)
    private BigDecimal total;

    @Embedded
    private Address shippingAddress;

    @Enumerated(EnumType.STRING)
    private PaymentMethod paymentMethod;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private String voucherCode;
    private String referralCode;
    private String notes;
    private String cancelReason;
    private LocalDateTime cancelledAt;
}
```

#### OrderItem
```java
@Entity
@Table(name = "order_items")
public class OrderItem {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private BigDecimal price;

    @Column(nullable = false)
    private BigDecimal subtotal;
}
```

#### Voucher
```java
@Entity
@Table(name = "vouchers")
public class Voucher extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VoucherType type; // PERCENTAGE, FIXED_AMOUNT, FREE_SHIPPING

    @Column(nullable = false)
    private BigDecimal value;

    private BigDecimal minPurchase;
    private BigDecimal maxDiscount;
    private Integer usageLimit;
    private Integer usageCount = 0;
    private Integer userUsageLimit;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean isActive = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;
}
```

#### ReferralCode
```java
@Entity
@Table(name = "referral_codes")
public class ReferralCode extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private Integer usageCount = 0;
    private BigDecimal rewardAmount = BigDecimal.valueOf(5.00);
    private BigDecimal totalEarnings = BigDecimal.ZERO;

    @Column(nullable = false)
    private Boolean isActive = true;
}
```

#### Address (Embeddable)
```java
@Embeddable
public class Address {
    private String fullName;
    private String phone;
    private String address;
    private String city;
    private String state;
    private String zipCode;
    private String country;
}
```

---

## Status Codes

### Success Codes
- **200 OK**: Request successful
- **201 Created**: Resource created successfully
- **204 No Content**: Request successful, no content to return

### Client Error Codes
- **400 Bad Request**: Invalid request data or validation error
- **401 Unauthorized**: Authentication required or token invalid
- **403 Forbidden**: Authenticated but not authorized for this resource
- **404 Not Found**: Resource not found
- **409 Conflict**: Resource conflict (duplicate, constraint violation)

### Server Error Codes
- **500 Internal Server Error**: Unexpected server error
- **503 Service Unavailable**: Service temporarily unavailable

---

## Implementation Guide

### 1. Environment Configuration

Create `.env` file in project root:

```bash
# Server
SERVER_PORT=9999

# Database
DB_HOST=localhost
DB_PORT=5432
DB_NAME=commerce_db
DB_USERNAME=postgres
DB_PASSWORD=postgres

# JWT
JWT_SECRET=your-super-secret-256-bit-key-change-this-in-production
JWT_EXPIRATION=86400000
JWT_REFRESH_EXPIRATION=1209600000
JWT_ISSUER=react-commerce

# Admin Seeder
ADMIN_USERNAME=admin@commerce.com
ADMIN_PASSWORD=Admin@123
```

### 2. Application Properties

```properties
# application.properties
spring.application.name=react-commerce

# Database
spring.datasource.url=jdbc:postgresql://${DB_HOST}:${DB_PORT}/${DB_NAME}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# JWT
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION}
jwt.refresh-expiration=${JWT_REFRESH_EXPIRATION}
jwt.issuer=${JWT_ISSUER}

# OpenAPI
springdoc.api-docs.path=/v3/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method
```

### 3. ResponseUtil Helper

```java
package com.github.jutionck.utils;

import com.github.jutionck.dto.response.ApiResponse;
import com.github.jutionck.dto.response.ErrorResponse;
import com.github.jutionck.dto.response.PagingResponse;
import com.github.jutionck.dto.response.StatusResponse;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

public class ResponseUtil {

    /**
     * Build single object response
     * @param httpStatus HTTP status code
     * @param message Success message
     * @param data Single object data
     * @return ResponseEntity with ApiResponse wrapper
     */
    public static <T> ResponseEntity<ApiResponse<T>> buildSingleResponse(
            HttpStatus httpStatus,
            String message,
            T data) {
        StatusResponse status = StatusResponse.builder()
                .code(httpStatus.value())
                .description(message)
                .build();
        ApiResponse<T> response = ApiResponse.<T>builder()
                .status(status)
                .data(data)
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Build paginated list response with paging metadata
     * @param httpStatus HTTP status code
     * @param message Success message
     * @param page Spring Data Page object
     * @return ResponseEntity with ApiResponse wrapper containing data array and paging object
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> buildPagedResponse(
            HttpStatus httpStatus,
            String message,
            Page<T> page) {
        StatusResponse status = StatusResponse.builder()
                .code(httpStatus.value())
                .description(message)
                .build();
        PagingResponse paging = PagingResponse.builder()
                .page(page.getNumber() + 1)  // Convert from 0-indexed to 1-indexed
                .rowsPerPage(page.getSize())
                .totalRows(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();

        ApiResponse<List<T>> response = ApiResponse.<List<T>>builder()
                .status(status)
                .data(page.getContent())  // List of items
                .paging(paging)           // Pagination metadata
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }

    /**
     * Alias for buildPagedResponse
     */
    public static <T> ResponseEntity<ApiResponse<List<T>>> buildPageResponse(
            HttpStatus httpStatus,
            String message,
            Page<T> page) {
        return buildPagedResponse(httpStatus, message, page);
    }

    /**
     * Build error response with list of error messages
     * @param httpStatus HTTP status code
     * @param message Error description
     * @param errors List of error messages
     * @return ResponseEntity with ErrorResponse wrapper
     */
    public static ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus httpStatus,
            String message,
            List<String> errors) {

        StatusResponse status = StatusResponse.builder()
                .code(httpStatus.value())
                .description(message)
                .build();
        ErrorResponse response = ErrorResponse.builder()
                .status(status)
                .errors(errors)
                .build();

        return ResponseEntity.status(httpStatus).body(response);
    }
}
```

**Key Points**:
- `buildSingleResponse()`: For single object responses (e.g., create, update, get by ID)
- `buildPagedResponse()`: For paginated lists with separate `paging` metadata
- `buildErrorResponse()`: Uses `ErrorResponse` class instead of `ApiResponse`
- Page numbers are **1-indexed** (converted from Spring's 0-indexed)

### 4. Controller Example

```java
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    /**
     * Get paginated products list
     * Returns data array + paging metadata
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
        @RequestParam(defaultValue = "0") int page,  // 0-indexed for Spring Data
        @RequestParam(defaultValue = "20") int size,
        @RequestParam(required = false) String search,
        @RequestParam(required = false) String category
    ) {
        // Service returns Page<ProductResponse> from Spring Data
        Page<ProductResponse> productsPage = productService.getProducts(
            page, size, search, category
        );

        // ResponseUtil converts to 1-indexed pagination and adds paging metadata
        return ResponseUtil.buildPagedResponse(
            HttpStatus.OK,
            "Products retrieved successfully",
            productsPage
        );
    }

    /**
     * Get single product by ID
     * Returns single object
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(
        @PathVariable String id
    ) {
        ProductResponse product = productService.getProductById(id);
        return ResponseUtil.buildSingleResponse(
            HttpStatus.OK,
            "Product retrieved successfully",
            product
        );
    }

    /**
     * Create new product (Seller only)
     * Returns single object
     */
    @PostMapping
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
        @Valid @RequestBody CreateProductRequest request,
        @AuthenticationPrincipal User currentUser
    ) {
        ProductResponse product = productService.createProduct(request, currentUser);
        return ResponseUtil.buildSingleResponse(
            HttpStatus.CREATED,
            "Product created successfully",
            product
        );
    }
}
```

### 5. GlobalExceptionHandler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors (400 Bad Request)
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(
        MethodArgumentNotValidException ex
    ) {
        List<String> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> error.getField() + ": " + error.getDefaultMessage())
            .collect(Collectors.toList());

        return ResponseUtil.buildErrorResponse(
            HttpStatus.BAD_REQUEST,
            "Validation failed",
            errors
        );
    }

    /**
     * Handle resource not found (404 Not Found)
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        ResourceNotFoundException ex
    ) {
        return ResponseUtil.buildErrorResponse(
            HttpStatus.NOT_FOUND,
            "Resource not found",
            List.of(ex.getMessage())
        );
    }

    /**
     * Handle unauthorized access (401 Unauthorized)
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(
        UnauthorizedException ex
    ) {
        return ResponseUtil.buildErrorResponse(
            HttpStatus.UNAUTHORIZED,
            "Unauthorized",
            List.of(ex.getMessage())
        );
    }

    /**
     * Handle constraint violations (409 Conflict)
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleConflict(
        DataIntegrityViolationException ex
    ) {
        return ResponseUtil.buildErrorResponse(
            HttpStatus.CONFLICT,
            "Data conflict",
            List.of("Resource already exists or constraint violation")
        );
    }

    /**
     * Handle generic exceptions (500 Internal Server Error)
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex
    ) {
        return ResponseUtil.buildErrorResponse(
            HttpStatus.INTERNAL_SERVER_ERROR,
            "Internal server error",
            List.of("An unexpected error occurred")
        );
    }
}
```

### 6. Security Configuration

```java
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(Customizer.withDefaults())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/products/**").permitAll()
                .requestMatchers("/api/v1/referrals/validate").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )
            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class)
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint(jwtAuthenticationEntryPoint)
            );

        return http.build();
    }
}
```

---

## Additional Resources

- **Swagger UI**: http://localhost:9999/swagger-ui.html
- **OpenAPI JSON**: http://localhost:9999/v3/api-docs
- **Database Migrations**: Consider using Flyway for production
- **Caching**: Implement Redis for voucher validation, product catalog
- **File Upload**: Integrate AWS S3 or Cloudinary for product images
- **Email**: Use JavaMailSender for order confirmations
- **Payment Gateway**: Integrate Midtrans, Stripe, or PayPal
- **WebSocket**: Real-time order status updates for sellers/customers

---

**Last Updated**: January 15, 2025
**API Version**: 1.0.0
**Base Path**: `/api/v1`
