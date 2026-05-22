# SYOS - Synex Outlet Store (Assignment 2)

## Overview

SYOS is a multi-user, multi-tier, client-server web application for grocery store management. It was refactored from a single-user CLI application (Assignment 1) into a web application using Java Servlets, JSP, and Apache Tomcat with MySQL as the database tier.

The system supports concurrent access from multiple employees and online customers simultaneously, with a custom request queue preventing race conditions on shared stock data.

## Architecture

```
Client Tier          Server Tier (Tomcat)              Database Tier
─────────────       ──────────────────────────         ─────────────
 Browser      ──►   Servlets (Controllers)       ──►   MySQL
 HTML/JS             Service Layer (Business)           syos_db
 Polling              ├── Interface
                      ├── Implementation
                      └── Synchronized Wrapper
                     Repository Layer (Data Access)
                     Domain Models (Entities)
                     Request Queue (Concurrency)
```

## Tech Stack

- Java 21
- Apache Tomcat 11
- MySQL
- JSP / Servlets (Jakarta EE)
- JUnit 4
- JavaScript (polling for real-time updates)

## Features

**Employee (In-Store)**
- Point of Sale with bill creation, item addition, and cash checkout
- Inventory management (shelf, online, warehouse views)
- Stock management (add to warehouse, restock shelves)
- Reports (daily sales, reshelf, reorder, stock, bill) with channel filtering

**Customer (Online)**
- Public product browsing without login
- Customer registration and login
- Online shopping with cart and checkout
- Order history

**Concurrency**
- Request queue using Producer-Consumer pattern (BlockingQueue + Thread)
- Synchronized service wrappers (Decorator pattern)
- Real-time stock updates via JavaScript polling
- Queue logging for monitoring task processing

## Project Structure

```
com.cb011999.cccp/
├── database/              DatabaseConnection (Singleton)
├── domain/
│   ├── enums/             BillStatus, StoreType, TransactionType
│   └── model/             Bill, Item, StockBatch, User, Employee, OnlineCustomer
├── factory/               RepositoryFactory
├── observer/              StockSubject, StockObserver implementations
├── repository/            Repository interfaces
│   └── impl/              Database and InMemory implementations
├── service/               Service interfaces
│   ├── impl/              Service implementations (business logic)
│   ├── concurrency/       Synchronized service wrappers
│   └── report/model/      Report DTOs
├── strategy/              PaymentStrategy, CashPayment
├── test/service/          JUnit test suites
└── web/
    ├── concurrency/       RequestQueue, Task
    ├── Servlets           (Login, POS, Shop, Stock, Inventory, Reports, etc.)
    └── JSP pages          (in WEB-INF/views/)
```

## Setup

### Prerequisites
- Java 21
- Apache Tomcat 11
- MySQL Server
- Eclipse IDE (or any Java IDE with Dynamic Web Project support)

### Database Setup
1. Start MySQL server
2. Create the database:
   ```sql
   CREATE DATABASE syos_db;
   ```
3. The application auto-creates tables on first run via `DatabaseConnection.initializeDatabase()`

### Seed Data
Run the SQL seed script to populate items and stock:
```sql
source scripts/seed_items_and_stock.sql
```

### Create an Employee (via Postman)
```
POST http://localhost:8080/syos/api/employees
Content-Type: application/x-www-form-urlencoded

apiKey=SYOS-ADMIN-2026
name=John Silva
contact=0771234567
employeeNumber=E001
role=Cashier
password=password123
```

### Running
1. Import as Dynamic Web Project in Eclipse
2. Add MySQL connector JAR to `WEB-INF/lib/`
3. Configure Tomcat 11 as the server runtime
4. Run on Server
5. Visit `http://localhost:8080/syos/`

## Design Patterns

| Pattern | Usage |
|---------|-------|
| Singleton | DatabaseConnection, RequestQueue |
| Factory | RepositoryFactory |
| Strategy | PaymentStrategy / CashPayment |
| Observer | StockSubject / StockObserver |
| Builder | Bill.BillBuilder |
| Repository | All data access interfaces + implementations |
| Decorator | Synchronized service wrappers |
| Producer-Consumer | RequestQueue with BlockingQueue |
| MVC | Servlets + JSPs + Domain Models |
| Front Controller | Each servlet handles multiple actions |
| PRG | Post-Redirect-Get after form submissions |

## Testing

Run all tests via JUnit 4 in Eclipse. Test suites include:

- `DatabaseConnectionTest` - Singleton, connection management
- `InventoryServiceTest` - FEFO/FIFO logic, restocking, thresholds
- `PointOfSaleServiceTest` - Checkout flow, stock validation
- `UserServiceTest` - Registration, login flows
- `ReportServiceTest` - Report generation, filtering
- `TaskTest` - Task execution, result handling, error handling
- `RequestQueueTest` - Queue submission, ordering, concurrent access
- `SynchronizedInventoryServiceTest` - Concurrent stock operations
- `SynchronizedPointOfSaleServiceTest` - Concurrent checkout safety

## API Endpoints

| Endpoint | Method | Description |
|----------|--------|-------------|
| `/` | GET | Public homepage (browse products) |
| `/login` | GET/POST | Customer login |
| `/register` | GET/POST | Customer registration |
| `/staff` | GET/POST | Employee login |
| `/dashboard` | GET | User dashboard |
| `/pos` | GET/POST | Point of Sale |
| `/shop` | GET/POST | Online shop |
| `/orders` | GET | Order history |
| `/inventory` | GET | Inventory views |
| `/stock` | GET/POST | Stock management |
| `/reports` | GET | Reports with filters |
| `/api/stock` | GET | Stock data as JSON (for polling) |
| `/api/employees` | POST | Employee management (API key required) |
| `/api/queue-status` | GET | Request queue status |
| `/logout` | GET | Logout |
