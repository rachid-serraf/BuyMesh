# Order Service

Manages shopping carts, checkout, order creation, order lifecycle, and order statistics.

## Overview

The Order Service is the source of truth for carts and orders. It validates products
through the Product Service, creates orders per seller, and keeps stock in sync by
publishing stock events to Kafka. All requests reach the service through the API
Gateway, where JWT authentication is enforced.

## Features

- Shopping cart management
- Buy Now orders
- Cart checkout grouped by seller
- Order status lifecycle and cancellation
- Buyer order history and seller order dashboard
- Buyer and seller statistics
- Product validation through OpenFeign
- Stock reservation and release through Kafka
- Role-based access (`CLIENT`, `SELLER`) and ownership checks

## Technology Stack

- Spring Boot 3.x (Servlet)
- Spring Security + JWT
- Spring Data MongoDB
- Spring Kafka
- Spring Cloud OpenFeign
- Netflix Eureka client
- Java 11

## Architecture

```
com.buy01.order
├── cart/        # Cart, checkout, and Product Service client
├── common/      # Shared enums (Role, Status, Product)
├── order/       # Orders, statistics, messaging, and persistence
└── security/    # JWT filter, JWT utilities, and security config
```

### Domain Model

```
Order
├── id: String
├── checkoutId: String (groups orders from the same checkout)
├── buyerId: String
├── sellerId: String
├── status: Status (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED)
├── items: List<OrderItem>
├── total: BigDecimal
├── address: DeliveryAddress
├── createdAt: Instant
└── updatedAt: Instant

Cart
├── id: String
├── userId: UUID (unique)
├── items: List<CartItem>
├── createdAt: Instant
└── updatedAt: Instant
```

## API Endpoints

All endpoints except `/orders/test` and `/actuator/health` require a valid JWT.

### Cart

| Method | Endpoint               | Description                     | Access |
| ------ | ---------------------- | ------------------------------- | ------ |
| GET    | `/cart/items`          | Get the current user's cart     | Protected |
| POST   | `/cart/items`          | Add a product to the cart       | Protected |
| DELETE | `/cart/items/{productId}` | Remove a product from the cart | Protected |
| POST   | `/cart/checkout`       | Check out the cart              | Protected |

### Orders

| Method | Endpoint                    | Description                              | Access |
| ------ | --------------------------- | ---------------------------------------- | ------ |
| POST   | `/orders`                   | Create an order directly                 | Protected |
| POST   | `/orders/buy`               | Buy a single product now                 | Protected |
| GET    | `/orders/me`                | List the buyer's orders (paginated)      | Protected |
| GET    | `/orders/seller`            | List orders containing the seller's items | SELLER |
| GET    | `/orders/{orderId}`         | Get order details                        | Buyer/Seller |
| PATCH  | `/orders/{orderId}/status`  | Advance order status                     | SELLER |
| POST   | `/orders/{orderId}/cancel`  | Cancel a pending or confirmed order      | Buyer/Seller |
| POST   | `/orders/{orderId}/redo`    | Re-create a cancelled or delivered order | Buyer |
| DELETE | `/orders/{orderId}`         | Delete a cancelled or delivered order    | Buyer |

### Statistics

| Method | Endpoint              | Description                      | Access |
| ------ | --------------------- | -------------------------------- | ------ |
| GET    | `/orders/stats/me`    | Buyer statistics                 | CLIENT |
| GET    | `/orders/stats/seller`| Seller statistics                | SELLER |

### Query Parameters

`GET /orders/me` and `GET /orders/seller` accept:

| Parameter | Description                                  |
| --------- | -------------------------------------------- |
| `search`  | Search by product/order text                 |
| `status`  | Filter by `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED` |
| `from`    | Start date (`YYYY-MM-DD`)                    |
| `to`      | End date (`YYYY-MM-DD`)                      |
| `page`    | Page index (default `0`)                     |
| `size`    | Page size (default `20`, max `50`)           |

## Order Lifecycle

```
PENDING ──> CONFIRMED ──> SHIPPED ──> DELIVERED
   │             │
   └─────────────┴──> CANCELLED ──> redo ──> PENDING (new order)
```

- Only sellers can advance the status (`PENDING → CONFIRMED → SHIPPED → DELIVERED`).
- Buyers and sellers can cancel `PENDING` or `CONFIRMED` orders.
- Buyers can redo `CANCELLED` or `DELIVERED` orders, creating a new `PENDING` order.
- Buyers can delete `CANCELLED` or `DELIVERED` orders.

See [Order & Checkout Flow](../../docs/ORDER_FLOW.md) for the complete flow.

## Request/Response Models

### Add Cart Item

```json
{
  "productId": "product-id",
  "quantity": 2
}
```

### Checkout

```json
{
  "address": {
    "fullName": "Ada Lovelace",
    "phone": "+1 555 0100",
    "line1": "1 Main St",
    "line2": null,
    "city": "Springfield",
    "state": "IL",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

### Buy Now

```json
{
  "productId": "product-id",
  "quantity": 1,
  "address": {
    "fullName": "Ada Lovelace",
    "phone": "+1 555 0100",
    "line1": "1 Main St",
    "city": "Springfield",
    "postalCode": "62701",
    "country": "USA"
  }
}
```

### Update Order Status

```json
{
  "status": "CONFIRMED"
}
```

### Checkout Response

```json
{
  "valid": true,
  "issues": [],
  "cart": { "...": "..." },
  "orders": [
    {
      "id": "order-id",
      "checkoutId": "shared-checkout-id",
      "sellerId": "seller-user-id",
      "status": "PENDING",
      "total": 99.99,
      "items": []
    }
  ]
}
```

## Kafka Events

The Order Service publishes to `order-stock-events` when stock must change:

```json
{
  "orderId": "order-id",
  "action": "RESERVE",
  "items": [
    { "productId": "product-id", "quantity": 2 }
  ]
}
```

Actions:

- `RESERVE` – when a new order is created
- `RELEASE` – when an order is cancelled

The Product Service consumes these events and adjusts inventory.

## Configuration

| Property                  | Description                              | Default                  |
| ------------------------- | ---------------------------------------- | ------------------------ |
| `ORDER_MONGO_HOST`        | MongoDB host                             | `localhost`              |
| `ORDER_MONGO_LOCAL_PORT`  | MongoDB port                             | `27020`                  |
| `ORDER_MONGO_DB_NAME`     | Database name                            | `mongo-order`            |
| `ORDER_MONGO_DB_USER`     | MongoDB username                         | `orderuser`              |
| `ORDER_MONGO_DB_PWD`      | MongoDB password                         | `123456`                 |
| `EUREKA_URL`              | Eureka server URL                        | `http://localhost:8761/eureka` |
| `KAFKA_BROKER`            | Kafka bootstrap servers                  | `localhost:9092`         |
| `JWT_PUBLIC_KEY`          | Public key for verifying gateway tokens  | -                        |
| `JWT_PRIVATE_KEY`         | Private key for signing tokens           | -                        |
| `INTERNAL_API_KEY`        | Shared key for Product Service calls     | `local-dev-internal-key` |
| `ORDER_SERVICE_PORT`      | Host port exposed by Docker Compose      | `.env` value             |

## Running Locally

```bash
cd backend/order-service
./mvnw spring-boot:run
```

Requires MongoDB, Eureka, and Kafka to be running. The service starts on port `8084`.
When using Docker Compose, the host port is defined by `ORDER_SERVICE_PORT`.

### Environment Variables

```bash
export ORDER_MONGO_HOST=localhost
export ORDER_MONGO_LOCAL_PORT=27020
export ORDER_MONGO_DB_NAME=mongo-order
export ORDER_MONGO_DB_USER=orderuser
export ORDER_MONGO_DB_PWD=123456
export EUREKA_URL=http://localhost:8761/eureka
export KAFKA_BROKER=localhost:9092
export JWT_PUBLIC_KEY=your-public-key
export JWT_PRIVATE_KEY=your-private-key
export INTERNAL_API_KEY=local-dev-internal-key
```

## Testing

```bash
# Get the cart
curl http://localhost:8084/cart/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN"

# Add an item to the cart
curl -X POST http://localhost:8084/cart/items \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": "product-id", "quantity": 2}'

# Check out the cart
curl -X POST http://localhost:8084/cart/checkout \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"address": {"fullName": "Ada Lovelace", "phone": "+1 555 0100", "line1": "1 Main St", "city": "Springfield", "postalCode": "62701", "country": "USA"}}'

# Buy a product now
curl -X POST http://localhost:8084/orders/buy \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"productId": "product-id", "quantity": 1, "address": {"fullName": "Ada Lovelace", "phone": "+1 555 0100", "line1": "1 Main St", "city": "Springfield", "postalCode": "62701", "country": "USA"}}'
```

## Docker

```bash
docker compose up -d order-service
```

## Ports

| Service       | Port  |
| ------------- | ----- |
| Order Service | 8084  |
| MongoDB       | 27020 |
| Kafka         | 9092  |
| Eureka        | 8761  |
