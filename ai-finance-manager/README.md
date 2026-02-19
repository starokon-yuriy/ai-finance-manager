# AI Finance Manager

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2.0-blue.svg)](https://reactjs.org/)
[![SQLite](https://img.shields.io/badge/SQLite-3.45.1.0-blue.svg)](https://www.sqlite.org/)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A full-stack personal finance management application built with a Spring Boot REST API backend and React frontend. Track your income and expenses, categorize transactions, and export financial data with an elegant, modern interface.

## 📑 Table of Contents

- [Features](#-features)
- [Architecture Overview](#-architecture-overview)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [API Documentation](#-api-documentation)
- [Frontend Architecture](#-frontend-architecture)
- [Testing](#-testing)
- [Configuration](#-configuration)
- [Development Workflow](#-development-workflow)
- [Deployment](#-deployment)
- [Contributing](#-contributing)
- [License](#-license)

---

## ✨ Features

### Core Functionality
- 💰 **Transaction Management**: Add, view, and categorize income and expense transactions
- 📊 **Date Range Filtering**: View transactions for specific time periods
- 🏷️ **Category-Based Organization**: Pre-configured categories for incomes and expenses
- 📈 **Automatic Summaries**: Calculate totals by category and overall totals
- 📥 **CSV Export**: Download transaction data for external analysis
- 🔄 **Real-time Synchronization**: Instant updates between frontend and backend

### User Experience
- 📱 **Modern React Interface**: Responsive, intuitive web application
- 🎨 **Beautiful UI Design**: Glassmorphism effects with gradient backgrounds
- ⚡ **Fast Performance**: Optimized queries and efficient data handling
- 🌐 **RESTful API**: Clean, well-documented API endpoints
- ✅ **Input Validation**: Both client-side and server-side validation

### Technical Features
- 🗄️ **SQLite Database**: Lightweight embedded database with Flyway migrations
- 🔒 **Type Safety**: Strong typing with Java entities and TypeScript-ready structure
- 🧪 **Comprehensive Testing**: Unit tests, integration tests, and E2E tests
- 📦 **Easy Deployment**: Single JAR file deployment or containerization ready
- 🔄 **CI/CD Ready**: GitHub Actions workflows for automated testing and deployment

---

## 🏛️ Architecture Overview

AI Finance Manager follows a **three-tier architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────────────────┐
│                        CLIENT TIER                              │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │              React Frontend (Port 3000)                   │  │
│  │  • App.js - Main component with tabs and state           │  │
│  │  • apiService.js - Centralized API client                │  │
│  │  • Date filtering & transaction forms                    │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │ HTTP/REST (Proxy)
                         │ JSON over HTTP
┌────────────────────────▼────────────────────────────────────────┐
│                     APPLICATION TIER                            │
│  ┌──────────────────────────────────────────────────────────┐  │
│  │         Spring Boot Backend (Port 8080)                   │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  API Layer (Controllers)                           │  │  │
│  │  │  • TransactionController                           │  │  │
│  │  │  • REST endpoints with @RestController            │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Application Layer (Services & Mappers)           │  │  │
│  │  │  • TransactionService / TransactionServiceImpl    │  │  │
│  │  │  • CategoryMapper / TransactionMapper (MapStruct) │  │  │
│  │  │  • DTOs & Validation                              │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  │  ┌────────────────────────────────────────────────────┐  │  │
│  │  │  Domain Layer (Entities & Repositories)           │  │  │
│  │  │  • Transaction / Category entities               │  │  │
│  │  │  • Spring Data JPA Repositories                   │  │  │
│  │  └────────────────────────────────────────────────────┘  │  │
│  └──────────────────────────────────────────────────────────┘  │
└────────────────────────┬────────────────────────────────────────┘
                         │ JPA/Hibernate
                         │ JDBC
┌────────────────────────▼────────────────────────────────────────┐
│                       DATA TIER                                 │
│  ┌─────────────────────────────────────���────────────────────┐  │
│  │              SQLite Database                              │  │
│  │  • finance_manager.db (file-based)                       │  │
│  │  • Tables: CATEGORY, TRANSACTIONS                        │  │
│  │  • Managed by Flyway migrations                          │  │
│  └──────────────────────────────────────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Layered Architecture**: Clear separation between API, application, and domain layers
2. **DTO Pattern**: Data Transfer Objects for decoupling API contracts from domain entities
3. **MapStruct**: Automatic mapping between entities and DTOs
4. **Repository Pattern**: Spring Data JPA repositories for data access abstraction
5. **Service Layer**: Business logic encapsulated in service classes
6. **RESTful API**: Stateless HTTP communication following REST principles
7. **Proxy Configuration**: Frontend proxies API calls to backend for simplified development

### Data Flow Example: Adding a Transaction

```
1. User fills form in React UI
   ↓
2. Frontend validates input
   ↓
3. apiService.createTransaction() → POST /api/v1/finance/transactions
   ↓
4. TransactionController.addTransaction() receives request
   ↓
5. ValidationUtils validates request payload
   ↓
6. TransactionService.addTransaction() processes business logic
   ↓
7. CategoryRepository.findById() verifies category exists
   ↓
8. Transaction entity created and saved via TransactionRepository
   ↓
9. TransactionMapper converts entity to DTO
   ↓
10. Controller returns 201 Created with TransactionDto
    ↓
11. Frontend updates UI with new transaction
```

---

## 🚀 Quick Start

### Automated Start (Recommended)
```bash
# Start both backend and frontend
./start-all.sh
```

This will:
1. Start Spring Boot backend on port 8080
2. Start React frontend on port 3000
3. Open browser automatically at http://localhost:3000

### Test Integration
```bash
# Run automated API tests
./test-integration.sh
```

---

## 🛠️ Technology Stack

### Backend Technologies

| Technology | Version | Purpose | Documentation |
|------------|---------|---------|---------------|
| **Java** | 21 | Core programming language | [Oracle Java 21](https://openjdk.java.net/) |
| **Spring Boot** | 4.0.2 | Application framework | [Spring Boot Docs](https://spring.io/projects/spring-boot) |
| **Spring Data JPA** | 4.0.2 | Data persistence layer | [Spring Data JPA](https://spring.io/projects/spring-data-jpa) |
| **Hibernate** | 6.x (via Spring Boot) | ORM implementation | [Hibernate ORM](https://hibernate.org/) |
| **SQLite** | 3.45.1.0 | Embedded database | [SQLite](https://www.sqlite.org/) |
| **Hibernate Community Dialects** | Latest | SQLite support for Hibernate | [Community Dialects](https://github.com/hibernate/hibernate-orm) |
| **Flyway** | Latest (via Spring Boot) | Database migrations | [Flyway](https://flywaydb.org/) |
| **MapStruct** | 1.6.3 | Object mapping | [MapStruct](https://mapstruct.org/) |
| **Lombok** | Latest (via Spring Boot) | Code generation | [Project Lombok](https://projectlombok.org/) |
| **JaCoCo** | 0.8.11 | Code coverage | [JaCoCo](https://www.jacoco.org/) |
| **Maven** | 3.6+ | Build tool | [Apache Maven](https://maven.apache.org/) |

### Frontend Technologies

| Technology | Version | Purpose | Documentation |
|------------|---------|---------|---------------|
| **React** | 18.2.0 | UI library | [React](https://reactjs.org/) |
| **React Scripts** | 5.0.1 | Build tooling (CRA) | [Create React App](https://create-react-app.dev/) |
| **Axios** | 1.6.0 | HTTP client | [Axios](https://axios-http.com/) |
| **Jest** | (via React Scripts) | Unit testing | [Jest](https://jestjs.io/) |
| **React Testing Library** | 13.4.0 | Component testing | [Testing Library](https://testing-library.com/) |
| **Cypress** | 13.6.0 | E2E testing | [Cypress](https://www.cypress.io/) |
| **CSS3** | - | Styling | - |
| **npm** | 8+ | Package manager | [npm](https://www.npmjs.com/) |

### Development & DevOps Tools

| Tool | Purpose |
|------|---------|
| **Git** | Version control |
| **GitHub Actions** | CI/CD pipeline |
| **IntelliJ IDEA** | Recommended IDE (backend) |
| **VS Code** | Recommended IDE (frontend) |
| **Postman/cURL** | API testing |
| **SQLite Browser** | Database inspection |

### Architecture Patterns

- **Layered Architecture**: Separation of concerns (API, Application, Domain)
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: API-Domain decoupling
- **Service Layer Pattern**: Business logic encapsulation
- **RESTful API**: HTTP-based stateless communication
- **Component-Based UI**: React component architecture

---

## 📋 Prerequisites

**Backend:**
- Java 21 or higher
- Maven 3.6 or higher

**Frontend:**
- Node.js 16 or higher
- npm (comes with Node.js)

## 💻 Manual Setup

### Backend

```bash
# Build the project
./mvnw clean install

# Run the application
./mvnw spring-boot:run
```

Backend will be available at: **http://localhost:8080**

### Frontend

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm start
```

Frontend will be available at: **http://localhost:3000**

The backend will start on `http://localhost:8080`

### Frontend Setup

**Quick Start:**

See [FRONTEND_SETUP.md](FRONTEND_SETUP.md) for detailed instructions.

```bash
# Install Node.js (if not already installed)
brew install node

# Navigate to frontend directory
cd frontend

# Install dependencies
npm install

# Start the development server
npm start
```

The frontend will automatically open at `http://localhost:3000`

### Running Both Together

**Terminal 1 - Backend:**
```bash
./mvnw spring-boot:run
```

**Terminal 2 - Frontend:**
```bash
cd frontend
npm start
```

Then open your browser to `http://localhost:3000` to see the React UI!

## 🔗 Backend-Frontend Integration

The application features a fully integrated frontend and backend architecture:

### Integration Architecture

- **Frontend** (React on port 3000) ↔ **Proxy** → **Backend** (Spring Boot on port 8080)
- All API calls use the centralized `apiService.js` module
- Proxy configuration in `frontend/package.json` forwards `/api/*` requests to backend
- Real-time data synchronization between UI and database

### API Integration Points

| Frontend Feature | Backend Endpoint | Method | Description |
|-----------------|------------------|--------|-------------|
| Category Dropdown | `/api/v1/finance/categories` | GET | Loads categories for transaction form |
| Add Income/Expense | `/api/v1/finance/transactions` | POST | Creates new transaction |
| Income Tab | `/api/v1/finance/transactions` | GET | Fetches incomes with date filter |
| Expense Tab | `/api/v1/finance/transactions` | GET | Fetches expenses with date filter |
| Balance Tab | `/api/v1/finance/transactions` | GET | Fetches both incomes and expenses |
| Download CSV | `/api/v1/finance/transactions/export` | GET | Exports transactions to CSV |

### Data Flow Examples

**Creating a Transaction:**
1. User fills form in UI modal
2. Frontend validates input
3. `apiService.createTransaction()` called
4. POST request to `/api/v1/finance/transactions`
5. Backend validates, saves to database
6. Returns created transaction
7. UI updates with new data

**Viewing Transactions:**
1. User selects date range
2. Clicks "Apply" button
3. `apiService.getTransactions()` called
4. GET request with type and date parameters
5. Backend queries database, groups by category
6. Returns categorized summaries with totals
7. UI displays grouped data

### Integration Documentation

- **[INTEGRATION_COMPLETE.md](INTEGRATION_COMPLETE.md)** - Detailed integration guide
- **[INTEGRATION_SUMMARY.md](INTEGRATION_SUMMARY.md)** - Quick reference and testing guide
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and data flow diagrams

### API Base URL
**Base URL**: `http://localhost:8080/api/v1/finance`

All endpoints are prefixed with `/api/v1/finance`.

### Endpoints Overview

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| GET | `/categories` | Get categories by type | 200, 400 |
| GET | `/transactions` | Get transactions by type and date | 200, 400 |
| POST | `/transactions` | Create new transaction | 201, 400 |
| GET | `/transactions/export` | Export transactions to CSV | 200, 400 |

---

## API Endpoints Details

### 1. Add Transaction

**POST** `/api/v1/finance/transactions`

Create a new transaction (income or expense).

**Request Body:**
```json
{
  "amount": 100.50,
  "transactionDate": "2026-02-16",
  "transactionType": "EXPENSES",
  "categoryId": 2,
  "comment": "Grocery shopping"
}
```

**Response:** `201 Created`
```json
{
  "idTransaction": 7,
  "amount": 100.50,
  "transactionDate": "2026-02-16",
  "transactionType": "EXPENSES",
  "category": {
    "idCategory": 2,
    "description": "Food & Groceries",
    "type": "EXPENSES"
  },
  "comment": "Grocery shopping"
}
```

### 2. Get Transactions by Type and Date Range

**GET** `/api/v1/finance/transactions?type={type}&dateFrom={dateFrom}&dateTo={dateTo}`

Retrieve transactions filtered by type and date range, grouped by categories with totals.

**Parameters (all required):**
- `type` - Transaction type (INCOMES or EXPENSES)
- `dateFrom` - Start date in ISO format (YYYY-MM-DD)
- `dateTo` - End date in ISO format (YYYY-MM-DD)

**Example:**
```
GET /api/v1/finance/transactions?type=INCOMES&dateFrom=2026-02-01&dateTo=2026-02-28
GET /api/v1/finance/transactions?type=EXPENSES&dateFrom=2026-02-01&dateTo=2026-02-28
```

**Response:** `200 OK`
```json
{
  "categorySummaries": [
    {
      "category": {
        "idCategory": 1,
        "description": "Salary",
        "type": "INCOMES"
      },
      "transactions": [
        {
          "idTransaction": 1,
          "amount": 5000.00,
          "transactionDate": "2026-02-01",
          "transactionType": "INCOMES",
          "category": {
            "idCategory": 1,
            "description": "Salary",
            "type": "INCOMES"
          },
          "comment": "Monthly salary"
        }
      ],
      "categoryTotal": 5000.00
    },
    {
      "category": {
        "idCategory": 8,
        "description": "Other Income",
        "type": "INCOMES"
      },
      "transactions": [
        {
          "idTransaction": 6,
          "amount": 1000.00,
          "transactionDate": "2026-02-15",
          "transactionType": "INCOMES",
          "category": {
            "idCategory": 8,
            "description": "Other Income",
            "type": "INCOMES"
          },
          "comment": "Stock dividend"
        }
      ],
      "categoryTotal": 1000.00
    }
  ],
  "totalAmount": 6000.00
}
```

### 3. Get All Categories

**GET** `/api/v1/finance/categories?type={type}`

Retrieve all available categories filtered by type.

**Parameters:**
- `type` - Category type (INCOMES or EXPENSES)

**Example:**
```
GET /api/v1/finance/categories?type=INCOMES
GET /api/v1/finance/categories?type=EXPENSES
```

**Response:** `200 OK`
```json
[
  {
    "idCategory": 1,
    "description": "Salary",
    "type": "INCOMES"
  },
  {
    "idCategory": 8,
    "description": "Other Income",
    "type": "INCOMES"
  }
]
```

### 4. Export Transactions to CSV

**GET** `/api/v1/finance/transactions/export?dateFrom={dateFrom}&dateTo={dateTo}`

Export transactions to a CSV file for a specified date range.

**Parameters (both required):**
- `dateFrom` - Start date in ISO format (YYYY-MM-DD)
- `dateTo` - End date in ISO format (YYYY-MM-DD)

**Example:**
```
GET /api/v1/finance/transactions/export?dateFrom=2026-02-01&dateTo=2026-02-28
```

**Response:** `200 OK`
- Content-Type: `text/csv`
- Content-Disposition: `attachment; filename="transactions_2026-02-01_2026-02-28.csv"`

**CSV Format:**
```csv
Transaction ID,Amount,Transaction Date,Transaction Type,Category,Comment
1,5000.00,2026-02-01,INCOMES,Salary,Monthly salary
2,250.50,2026-02-05,EXPENSES,Food & Groceries,Supermarket shopping
3,50.00,2026-02-07,EXPENSES,Transportation,Gas station
```

---

## 📁 Project Structure

### Backend Structure

```
src/main/java/com/ys/ai/aifinancemanager/
├── AiFinanceManagerApplication.java           # Main Spring Boot application
│
├── api/                                        # API/Presentation Layer
│   └── controller/
│       └── TransactionController.java         # REST API endpoints
│           • @RestController
│           • Handles HTTP requests/responses
│           • Endpoint: /api/v1/finance
│
├── application/                                # Application Layer
│   ├── dto/                                   # Data Transfer Objects
│   │   ├── CategoryDto.java                  # Category DTO
│   │   ├── TransactionDto.java               # Transaction DTO
│   │   ├── CreateTransactionRequest.java     # Create transaction payload
│   │   ├── TransactionsByCategoryDto.java    # Grouped transactions DTO
│   │   ├── TransactionsByTypeResponse.java   # Response with summaries
│   │   └── TransactionExportResponse.java    # CSV export data
│   │
│   ├── mapper/                                # MapStruct Mappers
│   │   ├── CategoryMapper.java               # Category entity ↔ DTO
│   │   └── TransactionMapper.java            # Transaction entity ↔ DTO
│   │
│   ├── service/                               # Business Logic
│   │   ├── TransactionService.java           # Service interface
│   │   ├── TransactionServiceImpl.java       # Service implementation
│   │   ├── CsvExportService.java             # CSV export interface
│   │   └── CsvExportServiceImpl.java         # CSV generation logic
│   │
│   └── validation/                            # Input Validation
│       └── ValidationUtils.java              # Validation helper methods
│
└── domain/                                     # Domain Layer
    ├── entity/                                # JPA Entities
    │   ├── Category.java                     # Category entity
    │   │   • @Entity @Table(name="CATEGORY")
    │   │   • Fields: idCategory, description, type
    │   │
    │   ├── Transaction.java                  # Transaction entity
    │   │   • @Entity @Table(name="TRANSACTIONS")
    │   │   • Fields: idTransaction, amount, transactionDate, category, comment
    │   │   • @ManyToOne relationship with Category
    │   │
    │   └── CategoryType.java                 # Enum: INCOMES, EXPENSES
    │
    └── repository/                            # Data Access Layer
        ├── CategoryRepository.java           # Spring Data JPA repository
        │   • findByType(CategoryType type)
        │
        └── TransactionRepository.java        # Spring Data JPA repository
            • Custom queries for filtering by date and category type

src/main/resources/
├── application.yaml                          # Application configuration
└── db/migration/                             # Flyway migrations
    ├── V1__Create_initial_schema.sql         # Database schema
    └── V2__Insert_initial_data.sql           # Initial data

src/test/java/                                # Test Classes
├── controller/                               # Controller tests
├── service/                                  # Service tests
├── mapper/                                   # Mapper tests
└── validation/                               # Validation tests
```

### Frontend Structure

```
frontend/
├── public/
│   ├── index.html                            # HTML template
│   └── manifest.json                         # PWA manifest
│
├── src/
│   ├── App.js                                # Main React component
│   │   • State management (tabs, transactions, filters)
│   │   • Transaction modal
│   │   • Three tabs: Income, Expense, Balance
│   │
│   ├── App.css                               # Main styles (glassmorphism)
│   ├── index.js                              # React entry point
│   │
│   └── services/
│       └── apiService.js                     # Centralized API client
│           • getCategories(type)
│           • getTransactions(type, dateFrom, dateTo)
│           • createTransaction(transaction)
│           • exportTransactionsToCsv(dateFrom, dateTo)
│
├── cypress/                                   # E2E Testing
│   ├── e2e/
│   │   ├── income.cy.js                      # Income tab tests
│   │   ├── expense.cy.js                     # Expense tab tests
│   │   ├── balance.cy.js                     # Balance tab tests
│   │   └── integration.cy.js                 # Full integration tests
│   │
│   └── support/
│       ├── commands.js                       # Custom Cypress commands
│       └── e2e.js                            # Support file
│
├── package.json                              # Dependencies & scripts
└── cypress.config.js                         # Cypress configuration
```

---

## 🗄️ Database Schema

### Technology
- **Database**: SQLite 3.45.1.0
- **Location**: `finance_manager.db` (project root)
- **Dialect**: `org.hibernate.community.dialect.SQLiteDialect`
- **Migrations**: Managed by Flyway

### Schema Overview

```sql
┌─────────────────────────────────────────┐
│           CATEGORY                       │
├──────────────────┬──────────────────────┤
│ ID_CATEGORY (PK) │ INTEGER             │
│ DESCRIPTION      │ VARCHAR(255) UNIQUE │
│ TYPE             │ VARCHAR(20)         │
│                  │ CHECK: INCOMES/     │
│                  │        EXPENSES     │
└──────────────────┴──────────────────────┘
                    │
                    │ 1
                    │
                    │ N
                    ▼
┌─────────────────────────────────────────┐
│         TRANSACTIONS                     │
├──────────────────┬──────────────────────┤
│ ID_TRANSACTION   │ INTEGER (PK)        │
│ AMOUNT           │ DECIMAL(10,2)       │
│ TRANSACTION_DATE │ DATE                │
│ ID_CATEGORY (FK) │ INTEGER             │
│ COMMENT          │ VARCHAR(255)        │
└──────────────────┴────────────────────��─┘
```

### Table: CATEGORY

Stores transaction categories (incomes and expenses).

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `ID_CATEGORY` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique category identifier |
| `DESCRIPTION` | VARCHAR(255) | NOT NULL, UNIQUE | Category name |
| `TYPE` | VARCHAR(20) | NOT NULL, CHECK IN ('INCOMES', 'EXPENSES') | Category type |

**Pre-populated Categories:**

| ID | Description | Type |
|----|-------------|------|
| 1 | Salary | INCOMES |
| 2 | Food & Groceries | EXPENSES |
| 3 | Transportation | EXPENSES |
| 4 | Entertainment | EXPENSES |
| 5 | Utilities | EXPENSES |
| 6 | Healthcare | EXPENSES |
| 7 | Investment | EXPENSES |
| 8 | Other Income | INCOMES |
| 9 | Other Expenses | EXPENSES |

### Table: TRANSACTIONS

Stores all financial transactions.

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `ID_TRANSACTION` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique transaction identifier |
| `AMOUNT` | DECIMAL(10,2) | NOT NULL | Transaction amount (positive for both incomes/expenses) |
| `TRANSACTION_DATE` | DATE | NOT NULL | Date of transaction (ISO 8601 format) |
| `ID_CATEGORY` | INTEGER | FOREIGN KEY → CATEGORY(ID_CATEGORY) | Reference to category |
| `COMMENT` | VARCHAR(255) | NULL | Optional transaction description |

**Relationships:**
- `TRANSACTIONS.ID_CATEGORY` → `CATEGORY.ID_CATEGORY` (Many-to-One)

### Entity-Relationship Diagram

```
CATEGORY (1) ────────< (N) TRANSACTIONS
   ↑                           ↑
   │                           │
   │                           │
CategoryType enum      Transaction type determined
(INCOMES/EXPENSES)      by category relationship
```

### Sample Data

The database is pre-populated with sample transactions:

```sql
-- Sample Income
ID: 1, Amount: 5000.00, Date: 2026-02-01, Category: Salary, Comment: "Monthly salary"
ID: 6, Amount: 1000.00, Date: 2026-02-15, Category: Other Income, Comment: "Stock dividend"

-- Sample Expenses
ID: 2, Amount: 250.50, Date: 2026-02-05, Category: Food & Groceries, Comment: "Supermarket shopping"
ID: 3, Amount: 50.00, Date: 2026-02-07, Category: Transportation, Comment: "Gas station"
ID: 4, Amount: 100.00, Date: 2026-02-10, Category: Entertainment, Comment: "Cinema tickets"
ID: 5, Amount: 150.00, Date: 2026-02-12, Category: Utilities, Comment: "Electricity bill"
```

### Database Operations

#### View Database
```bash
sqlite3 finance_manager.db

.tables                              # List all tables
.schema CATEGORY                     # View CATEGORY table schema
.schema TRANSACTIONS                 # View TRANSACTIONS table schema
SELECT * FROM CATEGORY;              # View all categories
SELECT * FROM TRANSACTIONS;          # View all transactions
.exit                                # Exit SQLite shell
```

#### Reset Database
```bash
# Delete database file
rm finance_manager.db

# Restart application (Flyway will recreate from migrations)
./mvnw spring-boot:run
```

#### Backup Database
```bash
# Create backup
cp finance_manager.db finance_manager_backup_$(date +%Y%m%d).db

# Restore from backup
cp finance_manager_backup_20260219.db finance_manager.db
```

---

## 📡 API Documentation

---

## ⚙️ Configuration

### Backend Configuration

**File**: `src/main/resources/application.yaml`

```yaml
server:
  port: 8080                                   # Backend port

spring:
  application:
    name: ai-finance-manager

  datasource:
    url: jdbc:sqlite:finance_manager.db        # SQLite database file
    driver-class-name: org.sqlite.JDBC

  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: none                           # Managed by Flyway
    show-sql: true                             # Log SQL queries
    properties:
      hibernate:
        format_sql: true                       # Pretty-print SQL

  flyway:
    enabled: true                              # Enable database migrations
    locations: classpath:db/migration          # Migration scripts location

logging:
  level:
    org.flywaydb: INFO
    com.ys.ai.aifinancemanager: DEBUG         # Application logging level
```

### Test Configuration

**File**: `src/test/resources/application-test.yaml`

```yaml
spring:
  datasource:
    url: jdbc:sqlite::memory:                  # In-memory database for tests
    driver-class-name: org.sqlite.JDBC

  jpa:
    show-sql: false                            # Reduce test log noise
    hibernate:
      ddl-auto: none

  flyway:
    enabled: true                              # Run migrations in tests
    locations: classpath:db/migration
```

### Frontend Configuration

**File**: `frontend/package.json`

```json
{
  "proxy": "http://localhost:8080",           // Proxy API calls to backend
  "scripts": {
    "start": "react-scripts start",           // Start on port 3000
    "build": "react-scripts build",
    "test": "react-scripts test",
    "test:coverage": "react-scripts test --coverage --watchAll=false",
    "cypress:open": "cypress open",           // Interactive E2E tests
    "cypress:run": "cypress run"              // Headless E2E tests
  }
}
```

**Proxy Configuration**: All requests to `/api/*` are automatically proxied from port 3000 to port 8080.

---

## 🎨 Frontend Architecture

### Component Structure

The frontend is built as a single-page application (SPA) with React:

```
App.js (Main Component)
├── State Management
│   ├── activeTab (income/expense/balance)
│   ├── categories (all categories)
│   ├── incomeTransactions
│   ├── expenseTransactions
│   ├── date filters (for each tab)
│   └── modal state
│
├── Tab 1: Income
│   ├── Date range filter
│   ├── Apply button → fetchIncomeTransactions()
│   ├── Add Income button → opens modal
│   ├── Transaction list (grouped by category)
│   └── Total summary
│
├── Tab 2: Expense
│   ├── Date range filter
│   ├── Apply button → fetchExpenseTransactions()
│   ├── Add Expense button → opens modal
│   ├── Transaction list (grouped by category)
│   └── Total summary
│
├── Tab 3: Balance
│   ├── Date range filter
│   ├── Toggle: Incomes / Expenses
│   ├── Apply button → fetchBalanceTransactions()
│   ├── CSV Download button
│   ├── Transaction list (grouped by category)
│   └── Total summary
│
└── Add Transaction Modal
    ├── Amount input (validated)
    ├── Date selector
    ├── Transaction type (INCOMES/EXPENSES)
    ├── Category dropdown (filtered by type)
    ├── Comment textarea
    └── Add button → createTransaction()
```

### API Service Layer

**File**: `frontend/src/services/apiService.js`

Centralized API client using Axios:

```javascript
const API_BASE_URL = '/api/v1/finance';

export default {
  // Get categories by type (INCOMES or EXPENSES)
  getCategories: async (type) => {...}

  // Get transactions by type and date range
  getTransactions: async (type, dateFrom, dateTo) => {...}

  // Create new transaction
  createTransaction: async (transaction) => {...}

  // Export transactions to CSV
  exportTransactionsToCsv: async (dateFrom, dateTo) => {...}

  // Test backend connection
  checkConnection: async () => {...}
}
```

### State Management

React hooks for state management:
- `useState` - Component state (transactions, categories, filters, modal)
- `useEffect` - Side effects (initial data loading)
- Props drilling (no Redux needed for this size application)

### UI/UX Features

1. **Glassmorphism Design**
   - Semi-transparent backgrounds
   - Backdrop blur effects
   - Gradient overlays
   - Modern card-based layout

2. **Responsive Date Filters**
   - Default: Current month (start date to today)
   - Custom range selection
   - Apply button triggers data fetch

3. **Real-time Updates**
   - Transaction added → Modal closes → List refreshes automatically
   - No manual page refresh needed

4. **Input Validation**
   - Client-side validation before API call
   - Amount must be positive number
   - Date cannot be empty
   - Category must be selected

5. **Error Handling**
   - Try-catch blocks for all API calls
   - Console logging for debugging
   - User-friendly error messages (can be enhanced)

---

## 🧪 Testing

### Backend Testing

#### Test Structure
```
src/test/java/com/ys/ai/aifinancemanager/
├── AiFinanceManagerApplicationTests.java    # Context load test
├── controller/
│   ├── TransactionControllerTest.java       # Unit tests (@WebMvcTest)
│   └── TransactionControllerIntegrationTest.java  # Integration tests
├── service/
│   └── TransactionServiceImplTest.java      # Service unit tests
├── mapper/
│   ├── CategoryMapperTest.java
│   └── TransactionMapperTest.java
└── validation/
    └── ValidationUtilsTest.java
```

#### Running Backend Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TransactionControllerTest

# Run with coverage report
./mvnw clean test jacoco:report
# View report: target/site/jacoco/index.html

# Run integration tests only
./mvnw test -Dtest="*IntegrationTest"

# Run unit tests only
./mvnw test -Dtest="*Test" -Dtest.exclude="*IntegrationTest"
```

#### Test Coverage Goals
- **Line Coverage**: 70%+
- **Branch Coverage**: 70%+
- **Method Coverage**: 70%+

### Frontend Testing

#### Test Structure
```
frontend/
├── src/
│   ├── App.test.js                          # Unit tests for App component
│   ├── services/
│   │   └── apiService.test.js               # API service tests
│   └── setupTests.js                        # Jest configuration
│
└── cypress/
    ├── e2e/
    │   ├── income.cy.js                     # Income tab E2E tests
    │   ├── expense.cy.js                    # Expense tab E2E tests
    │   ├── balance.cy.js                    # Balance tab E2E tests
    │   └── integration.cy.js                # Full integration tests
    └── support/
        ├── commands.js                      # Custom commands
        └── e2e.js                           # Support file
```

#### Running Frontend Tests

```bash
cd frontend

# Unit tests (Jest + React Testing Library)
npm test                                      # Watch mode
npm test -- --watchAll=false                  # Single run
npm run test:coverage                         # With coverage

# E2E tests (Cypress)
npm run cypress:open                          # Interactive mode
npm run cypress:run                           # Headless mode

# Full test suite
./run-tests.sh --all                          # Unit + E2E
./run-tests.sh --unit-only                    # Unit tests only
./run-tests.sh --e2e-only                     # E2E tests only
./run-tests.sh --coverage                     # With coverage
```

#### Test Coverage Goals
- **Line Coverage**: 70%+
- **Statement Coverage**: 70%+
- **Branch Coverage**: 70%+
- **Function Coverage**: 70%+

### Integration Testing

```bash
# Full API integration tests
./test-integration.sh

# Quick API smoke tests
./test-api.sh
```

**Test Scripts Output:**
- ✅ Green checkmarks for passing tests
- ❌ Red X for failing tests
- HTTP status codes validation
- Response body validation

---

## 💻 Development Workflow

### 1. Initial Setup

```bash
# Clone repository
git clone <repository-url>
cd ai-finance-manager

# Backend setup
./mvnw clean install

# Frontend setup
cd frontend
npm install
cd ..
```

### 2. Start Development Environment

```bash
# Option A: Automated (Recommended)
./start-all.sh

# Option B: Manual (Two terminals)
# Terminal 1 - Backend
./mvnw spring-boot:run

# Terminal 2 - Frontend
cd frontend && npm start
```

### 3. Development Cycle

```
1. Make code changes
   ├── Backend: src/main/java/**/*.java
   └── Frontend: frontend/src/**/*.{js,css}

2. Hot reload (automatic)
   ├── Backend: Spring Boot DevTools
   └── Frontend: React Fast Refresh

3. Run tests
   ├── ./mvnw test
   └── cd frontend && npm test

4. Verify integration
   └── ./test-integration.sh

5. Check coverage
   ├── ./mvnw jacoco:report
   └── npm run test:coverage

6. Commit changes
   └── git commit -am "Your message"
```

### 4. Adding New Features

#### Backend: Add New Endpoint

1. Create DTO in `application/dto/`
2. Add method in `TransactionService`
3. Implement in `TransactionServiceImpl`
4. Add endpoint in `TransactionController`
5. Write unit tests
6. Write integration tests

#### Frontend: Add New Component

1. Create component in `src/components/` (if needed)
2. Update `App.js` with new state/handlers
3. Add CSS in `App.css`
4. Update `apiService.js` if new API call needed
5. Write unit tests
6. Write Cypress E2E tests

### 5. Database Changes

1. Create new migration: `V{X}__Description.sql`
2. Place in `src/main/resources/db/migration/`
3. Restart application (Flyway runs automatically)
4. Update entities if needed
5. Run tests to verify

### 6. Code Quality

```bash
# Backend
./mvnw clean compile                          # Check compilation
./mvnw test                                   # Run tests
./mvnw jacoco:report                          # Coverage report

# Frontend
cd frontend
npm run build                                 # Check build
npm test -- --watchAll=false                  # Run tests
npm run test:coverage                         # Coverage report
```

---

## 🚀 Deployment

### Building for Production

#### Backend JAR

```bash
# Build executable JAR
./mvnw clean package -DskipTests

# JAR location
target/ai-finance-manager-0.0.1-SNAPSHOT.jar

# Run JAR
java -jar target/ai-finance-manager-0.0.1-SNAPSHOT.jar
```

#### Frontend Build

```bash
cd frontend

# Create production build
npm run build

# Build output location
build/

# Serve static files (example with serve)
npx serve -s build -p 3000
```

### Deployment Options

#### Option 1: Single JAR with Embedded Frontend

1. Build frontend: `cd frontend && npm run build`
2. Copy `build/` contents to `src/main/resources/static/`
3. Build backend: `./mvnw clean package`
4. Run: `java -jar target/*.jar`
5. Access: `http://localhost:8080`

#### Option 2: Separate Deployment

- **Backend**: Deploy JAR to server (port 8080)
- **Frontend**: Deploy to Nginx/Apache/CDN
- Update frontend `apiService.js` with backend URL

#### Option 3: Docker

```dockerfile
# Backend Dockerfile
FROM eclipse-temurin:21-jre
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

# Frontend Dockerfile
FROM node:18 AS build
WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

FROM nginx:alpine
COPY --from=build /app/build /usr/share/nginx/html
EXPOSE 80
```

#### Option 4: Cloud Platforms

- **Heroku**: Use Procfile with Maven plugin
- **AWS**: Elastic Beanstalk or EC2
- **Azure**: App Service
- **Google Cloud**: App Engine or Cloud Run

### Environment Variables

```bash
# Backend
SPRING_DATASOURCE_URL=jdbc:sqlite:/path/to/finance_manager.db
SERVER_PORT=8080

# Frontend
REACT_APP_API_BASE_URL=http://your-backend-url:8080/api/v1/finance
```

---

## 🔄 CI/CD Pipeline

### GitHub Actions Workflows

Located in `.github/workflows/`:

#### 1. **ci.yml** - Main CI Pipeline
- Triggers: Push/PR to main/develop
- Jobs:
  - Build & test backend (Maven)
  - Build & test frontend (npm)
  - Generate coverage reports
  - Upload artifacts

#### 2. **pr-checks.yml** - Pull Request Validation
- Runs on all PRs
- Validates code quality
- Runs full test suite
- Checks build success

#### 3. **dependency-check.yml** - Security Scanning
- Scheduled runs (weekly)
- Checks for vulnerable dependencies
- Generates security reports

#### 4. **release.yml** - Release Automation
- Triggers: Tag push (v*.*.*)
- Creates GitHub release
- Builds and publishes artifacts
- Generates changelog

### Running CI Locally

```bash
# Validate CI configuration
./validate-ci.sh

# Simulate CI build
./mvnw clean install
cd frontend && npm ci && npm run build && npm test -- --watchAll=false
```

---

## 📚 Additional Documentation

- **[QUICKSTART.md](QUICKSTART.md)** - Quick start guide with detailed setup instructions
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - Detailed system architecture and data flow diagrams
- **[FRONTEND_SETUP.md](FRONTEND_SETUP.md)** - Comprehensive frontend setup guide
- **[frontend/TESTING.md](frontend/TESTING.md)** - Testing documentation and best practices
- **[CI_SETUP_SUMMARY.md](CI_SETUP_SUMMARY.md)** - CI/CD pipeline documentation
- **[INTEGRATION_COMPLETE.md](INTEGRATION_COMPLETE.md)** - Backend-Frontend integration guide

---

## 🤝 Contributing

### Getting Started

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `./mvnw test && cd frontend && npm test`
5. Commit: `git commit -am 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Code Style

- **Backend**: Follow Java conventions, use Lombok annotations
- **Frontend**: Use ES6+ features, functional components with hooks
- **Tests**: Write tests for new features
- **Comments**: Document complex logic

### Pull Request Guidelines

- Clear description of changes
- Reference related issues
- All tests passing
- Code coverage maintained (70%+)
- No merge conflicts

---

## 🐛 Troubleshooting

### Common Issues

#### Port Already in Use
```bash
# Kill process on port 8080 (backend)
lsof -ti:8080 | xargs kill -9

# Kill process on port 3000 (frontend)
lsof -ti:3000 | xargs kill -9
```

#### Database Locked
```bash
# Close all connections and remove database
rm finance_manager.db
./mvnw spring-boot:run  # Recreates from migrations
```

#### Maven Wrapper Permission Denied
```bash
chmod +x mvnw
```

#### Node Modules Issues
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

#### Java Version Mismatch
```bash
# Check version
java -version

# Set JAVA_HOME (macOS)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👨‍💻 Authors

- **Your Name** - Initial work

---

## 🙏 Acknowledgments

- Spring Boot team for the excellent framework
- React team for the amazing UI library
- MapStruct for simplifying object mapping
- SQLite for the lightweight database solution
- All open-source contributors

---

## 📞 Support

For questions, issues, or suggestions:
- Open an issue on GitHub
- Check existing documentation
- Review the troubleshooting section

---

**Built with ❤️ using Spring Boot and React**

