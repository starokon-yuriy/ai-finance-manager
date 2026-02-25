# AI Finance Manager

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.2-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![React](https://img.shields.io/badge/React-18.2-blue.svg)](https://reactjs.org/)
[![Recharts](https://img.shields.io/badge/Recharts-3.7-8884d8.svg)](https://recharts.org/)
[![SQLite](https://img.shields.io/badge/SQLite-3.45.1.0-blue.svg)](https://www.sqlite.org/)
[![MCP](https://img.shields.io/badge/MCP-1.0.0-blueviolet.svg)](https://modelcontextprotocol.io/)
[![Spring AI](https://img.shields.io/badge/Spring%20AI-1.0.0-6db33f.svg)](https://docs.spring.io/spring-ai/reference/)
[![Datawrapper](https://img.shields.io/badge/Datawrapper-MCP-00b1d2.svg)](https://github.com/palewire/datawrapper-mcp)
[![License](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

A full-stack personal finance management application built with a Spring Boot REST API backend and React frontend. Track your income and expenses, categorize transactions, visualize balance charts with Recharts and Datawrapper, export financial data to CSV, and integrate with AI agents via the **Model Context Protocol (MCP)**.

## 📑 Table of Contents

- [Features](#-features)
- [Architecture Overview](#-architecture-overview)
- [Technology Stack](#-technology-stack)
- [Quick Start](#-quick-start)
- [Project Structure](#-project-structure)
- [Database Schema](#-database-schema)
- [API Documentation](#-api-documentation)
- [Frontend Architecture](#-frontend-architecture)
- [MCP Integration](#-mcp-integration)
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
- 📉 **Balance Chart**: Interactive income vs. expense time-series chart (Recharts) with optional Datawrapper embed
- 🔄 **Real-time Synchronization**: Instant updates between frontend and backend

### Visualization & MCP
- 📊 **Recharts AreaChart**: Interactive local chart with tooltips, summary cards, and zero-line reference
- 🌐 **Datawrapper Integration**: Auto-create, update, and publish line charts via the Datawrapper API (direct REST or MCP stdio)
- 🔌 **Model Context Protocol (MCP)**: Structured `McpContext` schema for AI agent workflows — transferable financial context
- 🤖 **Spring AI MCP Client**: Uses the Spring AI MCP framework (`McpSyncClient`) with stdio transport to communicate with the `datawrapper-mcp` Python server
- 📝 **Agent Interaction Logging**: Every MCP/agent interaction is logged with timestamps, context snapshots, and accept/reject status


### User Experience
- 📱 **Modern React Interface**: Responsive, intuitive web application
- 🎨 **Beautiful UI Design**: Glassmorphism effects with gradient backgrounds
- ⚡ **Fast Performance**: Optimized queries and efficient data handling
- 🌐 **RESTful API**: Clean, well-documented API endpoints
- ✅ **Input Validation**: Both client-side and server-side validation

### Technical Features
- 🗄️ **SQLite Database**: Lightweight embedded database with Flyway migrations
- 🔒 **Type Safety**: Strong typing with Java entities and TypeScript-ready structure
- 🧪 **Comprehensive Testing**: 148 backend tests covering controllers, services, mappers, validation, MCP client (unit + integration), and context
- 📦 **Easy Deployment**: Single JAR file deployment or containerization ready
- 🔄 **CI/CD Ready**: GitHub Actions workflows for automated testing and deployment

---

## 🏛️ Architecture Overview

AI Finance Manager follows a **three-tier architecture** with an additional **MCP layer** for AI agent integration:

```
┌──────────────────────────────────────────────────────────────────────────┐
│                          AI AGENT TIER (optional)                        │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐                  │
│  │ Claude Code   │  │   Cursor      │  │ GitHub       │                  │
│  │ (CLI)         │  │   (IDE)       │  │ Copilot      │                  │
│  └──────┬───────┘  └──────┬───────┘  └──────┬───────┘                  │
│         │ MCP stdio       │ MCP stdio        │ MCP stdio               │
│         ▼                 ▼                  ▼                          │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │          datawrapper-mcp (Python stdio server)               │      │
│  └──────────────────────────────────────────────────────────────┘      │
└──────────────────────────┬─────────────────────────────────────────────┘
                           │ Datawrapper REST API
┌──────────────────────────▼─────────────────────────────────────────────┐
│                        CLIENT TIER                                      │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │              React Frontend (Port 3000)                      │      │
│  │  • App.js — Main component with tabs and state               │      │
│  │  • BalanceChart.js — Recharts AreaChart + Datawrapper iframe │      │
│  │  • apiService.js — Centralized API client                    │      │
│  └──────────────────────────────┬───────────────────────────────┘      │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │ HTTP/REST (Proxy)
┌─────────────────────────────────▼──────────────────────────────────────┐
│                      APPLICATION TIER                                   │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │         Spring Boot Backend (Port 8080)                      │      │
│  │                                                              │      │
│  │  ┌────────────────────────────────────────────────────────┐  │      │
│  │  │  MCP Layer                                             │  │      │
│  │  │  • McpDatawrapperClient — Spring AI MCP client          │  │      │
│  │  │  • McpClientConfiguration — MCP bean config             │  │      │
│  │  │  • AgentLogService — interaction audit trail           │  │      │
│  │  │  • McpContext — structured domain schema               │  │      │
│  │  └────────────────────────────────────────────────────────┘  │      │
│  │  ┌────────────────────────────────────────────────────────┐  │      │
│  │  │  API Layer (Controllers)                               │  │      │
│  │  │  • TransactionController — REST endpoints              │  │      │
│  │  └────────────────────────────────────────────────────────┘  │      │
│  │  ┌────────────────────────────────────────────────────────┐  │      │
│  │  │  Application Layer (Services & Mappers)                │  │      │
│  │  │  • TransactionService — CRUD & query operations        │  │      │
│  │  │  • BalanceChartService — time-series aggregation        │  │      │
│  │  │  • DatawrapperService — chart CRUD via MCP client       │  │      │
│  │  │  • CsvExportService — CSV generation                   │  │      │
│  │  │  • CategoryMapper / TransactionMapper (MapStruct)       │  │      │
│  │  │  • ValidationUtils — input validation                  │  │      │
│  │  └────────────────────────────────────────────────────────┘  │      │
│  │  ┌────────────────────────────────────────────────────────┐  │      │
│  │  │  Domain Layer (Entities & Repositories)                │  │      │
│  │  │  • Transaction / Category entities                     │  │      │
│  │  │  • Spring Data JPA Repositories                        │  │      │
│  │  └────────────────────────────────────────────────────────┘  │      │
│  │  ┌────────────────────────────────────────────────────────┐  │      │
│  │  │  Config Layer                                          │  │      │
│  │  │  • CorsConfig — CORS for React dev server              │  │      │
│  │  │  • FlywayConfig — database migrations                  │  │      │
│  │  │  • MapperConfiguration — MapStruct config              │  │      │
│  │  └────────────────────────────────────────────────────────┘  │      │
│  └──────────────────────────────────────────────────────────────┘      │
└─────────────────────────────────┬──────────────────────────────────────┘
                                  │ JPA / JDBC
┌─────────────────────────────────▼──────────────────────────────────────┐
│                         DATA TIER                                       │
│  ┌──────────────────────────────────────────────────────────────┐      │
│  │  SQLite (finance_manager.db)                                 │      │
│  │  • CATEGORY table    • TRANSACTIONS table                    │      │
│  │  • Managed by Flyway migrations                              │      │
│  └──────────────────────────────────────────────────────────────┘      │
└────────────────────────────────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Layered Architecture**: Clear separation between API, application, domain, MCP, and config layers
2. **DTO Pattern**: Data Transfer Objects for decoupling API contracts from domain entities
3. **MapStruct**: Automatic mapping between entities and DTOs
4. **Repository Pattern**: Spring Data JPA repositories for data access abstraction
5. **Service Layer**: Business logic encapsulated in service classes
6. **RESTful API**: Stateless HTTP communication following REST principles
7. **Proxy Configuration**: Frontend proxies API calls to backend for simplified development
8. **MCP Integration**: Structured context schema for AI agent interoperability
9. **Spring AI MCP Client**: Uses the Spring AI `McpSyncClient` with `StdioClientTransport` to communicate with `datawrapper-mcp` via JSON-RPC 2.0 over stdin/stdout
10. **Graceful Degradation**: Datawrapper/MCP features are optional — the app works fully without them

### Data Flow: Adding a Transaction

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

### Data Flow: Balance Chart

```
1. User clicks "Apply" on Balance tab
   ↓
2. Frontend: fetchBalanceChartData()
   GET /api/v1/finance/balance/chart?dateFrom=...&dateTo=...
   ↓
3. TransactionController.getBalanceChartData()
   ↓
4. BalanceChartServiceImpl.getBalanceChartData()
   ├── TransactionRepository: fetch INCOMES transactions
   ├── TransactionRepository: fetch EXPENSES transactions
   ├── buildBalanceTimeSeries(): aggregate by day (≤31 days) or month (>31 days)
   └── DatawrapperService: create/update & publish chart via Spring AI McpSyncClient (optional)
   ↓
5. BalanceChartDataResponse
   { chartEmbedUrl, balanceData[], totalIncome, totalExpense, netBalance }
   ↓
6. Frontend: BalanceChart component renders
   ├── Recharts AreaChart (interactive, local)
   └── Datawrapper iframe embed (if URL available)
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
| **Jackson (tools.jackson)** | Latest (via Spring Boot) | JSON serialization / MCP JSON-RPC | [Jackson](https://github.com/FasterXML/jackson) |
| **Spring AI MCP** | 1.0.0 | MCP client framework (McpSyncClient, StdioClientTransport) | [Spring AI](https://docs.spring.io/spring-ai/reference/) |
| **JaCoCo** | 0.8.11 | Code coverage | [JaCoCo](https://www.jacoco.org/) |
| **Maven** | 3.6+ | Build tool | [Apache Maven](https://maven.apache.org/) |

### Frontend Technologies

| Technology | Version | Purpose | Documentation |
|------------|---------|---------|---------------|
| **React** | 18.2 | UI library | [React](https://reactjs.org/) |
| **React Scripts** | 5.0.1 | Build tooling (CRA) | [Create React App](https://create-react-app.dev/) |
| **Recharts** | 3.7 | Interactive charts (AreaChart, tooltips, legends) | [Recharts](https://recharts.org/) |
| **Axios** | 1.6+ | HTTP client | [Axios](https://axios-http.com/) |
| **Jest** | (via React Scripts) | Unit testing | [Jest](https://jestjs.io/) |
| **@testing-library/react** | 13.4.0 | Component testing | [Testing Library](https://testing-library.com/) |
| **@testing-library/jest-dom** | 5.16.5 | DOM matchers | [jest-dom](https://github.com/testing-library/jest-dom) |
| **@testing-library/user-event** | 14.4.3 | User interaction testing | [user-event](https://testing-library.com/docs/user-event/intro/) |
| **Cypress** | 13.6+ | E2E testing | [Cypress](https://www.cypress.io/) |
| **Node.js** | 18+ | Runtime | [Node.js](https://nodejs.org/) |

### MCP & Visualization Technologies

| Technology | Version | Purpose | Documentation |
|------------|---------|---------|---------------|
| **MCP (Model Context Protocol)** | 1.0.0 | AI agent context exchange standard | [MCP Spec](https://modelcontextprotocol.io/) |
| **Spring AI MCP** | 1.0.0 | Java MCP client framework (McpSyncClient, StdioClientTransport) | [Spring AI](https://docs.spring.io/spring-ai/reference/) |
| **datawrapper-mcp** | Latest | Python MCP server for Datawrapper API | [GitHub](https://github.com/palewire/datawrapper-mcp) |
| **Datawrapper API** | v3 | Chart creation, publishing, and embedding | [Datawrapper](https://www.datawrapper.de/) |
| **JSON-RPC 2.0** | 2.0 | Protocol for MCP stdio communication | [JSON-RPC](https://www.jsonrpc.org/) |

### Architecture Patterns

- **Layered Architecture**: Separation of concerns (API, Application, Domain, MCP, Config)
- **Repository Pattern**: Data access abstraction
- **DTO Pattern**: API-Domain decoupling
- **Service Layer Pattern**: Business logic encapsulation
- **RESTful API**: HTTP-based stateless communication
- **Component-Based UI**: React component architecture
- **MCP Context Schema**: Structured, transferable state for AI agents
- **Spring AI MCP Client**: McpSyncClient with StdioClientTransport for stdio JSON-RPC communication

---

## 📋 Prerequisites

**Backend:**
- Java 21 or higher
- Maven 3.6 or higher

**Frontend:**
- Node.js 16 or higher
- npm (comes with Node.js)

**MCP / Datawrapper (optional):**
- Python 3.10+ with `uv` package manager
- Datawrapper access token ([get one here](https://www.datawrapper.de/))

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

### Manual Setup

**Terminal 1 — Backend:**
```bash
./mvnw clean install
./mvnw spring-boot:run
```
Backend available at: **http://localhost:8080**

**Terminal 2 — Frontend:**
```bash
cd frontend
npm install
npm start
```
Frontend available at: **http://localhost:3000**

### Test Integration
```bash
# Run automated API tests
./test-integration.sh
```

---

## 📁 Project Structure

### Backend Structure

```
src/main/java/com/ys/ai/aifinancemanager/
├── AiFinanceManagerApplication.java           # Main Spring Boot application
│
├── api/                                        # ═══ API LAYER ═══
│   └── controller/
│       └── TransactionController.java         # REST API endpoints
│           • POST /api/v1/finance/transactions
│           • GET  /api/v1/finance/transactions
│           • GET  /api/v1/finance/categories
│           • GET  /api/v1/finance/transactions/export
│           • GET  /api/v1/finance/balance/chart
│
├── application/                                # ═══ APPLICATION LAYER ═══
│   ├── dto/                                   # Data Transfer Objects
│   │   ├── BalanceChartDataResponse.java      # Chart data + totals + embed URL
│   │   ├── CategoryDto.java                   # Category DTO
│   │   ├── CreateTransactionRequest.java      # Create transaction payload
│   │   ├── TransactionDto.java                # Transaction DTO
│   │   ├── TransactionExportResponse.java     # CSV export data
│   │   ├── TransactionsByCategoryDto.java     # Grouped transactions DTO
│   │   └── TransactionsByTypeResponse.java    # Response with category summaries
│   │
│   ├── mapper/                                # MapStruct Mappers
│   │   ├── CategoryMapper.java               # Category entity ↔ DTO
│   │   └── TransactionMapper.java            # Transaction entity ↔ DTO
│   │
│   ├── service/                               # Business Logic
│   │   ├── BalanceChartService.java           # Balance chart interface
│   │   ├── BalanceChartServiceImpl.java       # Time-series aggregation + Datawrapper
│   │   ├── CsvExportService.java             # CSV export interface
│   │   ├── CsvExportServiceImpl.java         # CSV generation logic
│   │   ├── DatawrapperService.java           # Datawrapper interface
│   │   ├── DatawrapperServiceImpl.java       # Chart CRUD via MCP stdio client
│   │   ├── TransactionService.java           # Transaction service interface
│   │   └── TransactionServiceImpl.java       # Transaction CRUD & queries
│   │
│   └── validation/                            # Input Validation
│       └── ValidationUtils.java              # Date range, request, export validation
│
├── config/                                     # ═══ CONFIG LAYER ═══
│   ├── CorsConfig.java                       # CORS configuration for React dev server
│   └── FlywayConfig.java                     # Flyway database migration config
│
├── configuration/                              # ═══ FRAMEWORK CONFIG ═══
│   └── MapperConfiguration.java              # MapStruct configuration
│
├── domain/                                     # ═══ DOMAIN LAYER ═══
│   ├── entity/
│   │   ├── Category.java                     # @Entity — CATEGORY table
│   │   ├── CategoryType.java                 # Enum: INCOMES, EXPENSES
│   │   └── Transaction.java                  # @Entity — TRANSACTIONS table
│   │
│   └── repository/
│       ├── CategoryRepository.java           # JPA repository for categories
│       └── TransactionRepository.java        # JPA repository with date/type queries
│
└── mcp/                                        # ═══ MCP LAYER ═══
    ├── client/
    │   ├── McpClientConfiguration.java       # Spring AI MCP client config:
    │   │                                      #   Creates McpSyncClient bean with StdioClientTransport
    │   │                                      #   Configures datawrapper-mcp process spawning
    │   │                                      #   Handles DATAWRAPPER_ACCESS_TOKEN resolution
    │   └── McpDatawrapperClient.java         # MCP client wrapper:
    │                                          #   Delegates to McpSyncClient for tool calls
    │                                          #   create_chart / update_chart / publish_chart
    │                                          #   Parses chart IDs and embed URLs from responses
    │
    ├── model/
    │   └── McpContext.java                   # Context schema:
    │                                          #   QueryContext, FinancialSummary,
    │                                          #   ChartContext, WorkflowMetadata
    │
    └── service/
        └── AgentLogService.java              # Logs all agent interactions to agent_log.txt
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
│   │   • State management (tabs, transactions, filters, chart)
│   │   • Transaction modal
│   │   • Three tabs: Income, Expense, Balance
│   │
│   ├── App.css                               # Styles (glassmorphism, chart containers)
│   ├── App.test.js                           # Unit tests for App component
│   ├── index.js                              # React entry point
│   │
│   ├── components/
│   │   └── BalanceChart.js                   # Recharts AreaChart component
│   │       • Income / Expense / Balance area chart
│   │       • Custom tooltip with dollar formatting
│   │       • Summary cards (total income, expense, net)
│   │       • Optional Datawrapper iframe embed
│   │       • Zero-line reference
│   │
│   └── services/
│       └── apiService.js                     # Centralized API client
│           • checkConnection()
│           • getCategories(type)
│           • getTransactions(type, dateFrom, dateTo)
│           • createTransaction(transaction)
│           • exportTransactionsToCsv(dateFrom, dateTo)
│           • getBalanceChartData(dateFrom, dateTo)
│
├── cypress/                                   # E2E Testing
│   ├── e2e/
│   │   ├── income.cy.js
│   │   ├── expense.cy.js
│   │   ├── balance.cy.js
│   │   └── integration.cy.js
│   └── support/
│       ├── commands.js
│       └── e2e.js
│
├── package.json                              # Dependencies & scripts
└── cypress.config.js                         # Cypress configuration
```

### MCP Configuration Files

```
ai-finance-manager/
├── mcp/
│   ├── mcp-config.json                       # MCP server config for AI agents (datawrapper-mcp)
│   └── README.md                             # Detailed MCP integration documentation
│
├── src/main/java/.../mcp/client/
│   └── McpClientConfiguration.java           # Spring @Configuration for McpSyncClient bean
│                                              #   StdioClientTransport, process spawning
│
├── src/main/resources/
│   └── application.yaml                      # MCP client config:
│                                              #   mcp.client.enabled, mcp.client.command
│                                              #   datawrapper.access-token
```

### Test Structure

```
src/test/java/com/ys/ai/aifinancemanager/
├── AiFinanceManagerApplicationTests.java      # Context load test (1 test)
├── api/controller/
│   └── TransactionControllerTest.java         # Controller unit tests (23 tests)
├── application/
│   ├── mapper/
│   │   ├── CategoryMapperTest.java            # Mapper tests (8 tests)
│   │   └── TransactionMapperTest.java         # Mapper tests (12 tests)
│   ├── service/
│   │   ├── BalanceChartServiceImplTest.java    # Chart generation tests (6 tests)
│   │   ├── CsvExportServiceImplTest.java      # CSV export tests (11 tests)
│   │   └── TransactionServiceImplTest.java    # Service unit tests (29 tests)
│   └── validation/
│       └── ValidationUtilsTest.java           # Validation tests (18 tests)
└── mcp/
    ├── client/
    │   ├── McpClientConfigurationTest.java    # MCP config tests (2 tests)
    │   ├── McpDatawrapperClientIntegrationTest.java  # MCP integration tests (3 tests)
    │   ├── McpDatawrapperClientTest.java      # MCP client unit tests (32 tests)
    │   └── McpToolDiscoveryTest.java          # MCP tool discovery test (1 test, skipped)
    ├── model/
    │   └── McpContextRoundTripTest.java       # Context round-trip tests (2 tests)
    └── service/
        (empty — McpSafetyValidatorTest removed with McpSafetyValidator)

Total: 148 backend tests (1 skipped)
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
                    │ 1:N
                    ▼
┌─────────────────────────────────────────┐
│         TRANSACTIONS                     │
├──────────────────┬──────────────────────┤
│ ID_TRANSACTION   │ INTEGER (PK)        │
│ AMOUNT           │ DECIMAL(10,2)       │
│ TRANSACTION_DATE │ DATE                │
│ ID_CATEGORY (FK) │ INTEGER             │
│ COMMENT          │ VARCHAR(255)        │
└──────────────────┴──────────────────────┘
```

### Table: CATEGORY

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

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| `ID_TRANSACTION` | INTEGER | PRIMARY KEY, AUTOINCREMENT | Unique transaction identifier |
| `AMOUNT` | DECIMAL(10,2) | NOT NULL | Transaction amount (positive) |
| `TRANSACTION_DATE` | DATE | NOT NULL | Date of transaction (ISO 8601) |
| `ID_CATEGORY` | INTEGER | FOREIGN KEY → CATEGORY(ID_CATEGORY) | Category reference |
| `COMMENT` | VARCHAR(255) | NULL | Optional description |

### Database Operations

```bash
# View database
sqlite3 finance_manager.db
.tables
SELECT * FROM CATEGORY;
SELECT * FROM TRANSACTIONS;
.exit

# Reset database (Flyway recreates from migrations)
rm finance_manager.db
./mvnw spring-boot:run
```

---

## 📡 API Documentation

### Base URL
`http://localhost:8080/api/v1/finance`

### Endpoints Overview

| Method | Endpoint | Description | Status Codes |
|--------|----------|-------------|--------------|
| POST | `/transactions` | Create new transaction | 201, 400 |
| GET | `/transactions` | Get transactions by type and date | 200, 400 |
| GET | `/categories` | Get categories by type | 200, 400 |
| GET | `/transactions/export` | Export transactions to CSV | 200, 400 |
| GET | `/balance/chart` | Get balance chart data | 200, 400 |


---

### 1. Add Transaction

**POST** `/api/v1/finance/transactions`

```json
// Request
{
  "amount": 100.50,
  "transactionDate": "2026-02-16",
  "transactionType": "EXPENSES",
  "categoryId": 2,
  "comment": "Grocery shopping"
}

// Response: 201 Created
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

All parameters required. `type`: `INCOMES` or `EXPENSES`.

```json
// Response: 200 OK
{
  "categorySummaries": [
    {
      "category": { "idCategory": 1, "description": "Salary", "type": "INCOMES" },
      "transactions": [
        {
          "idTransaction": 1,
          "amount": 5000.00,
          "transactionDate": "2026-02-01",
          "transactionType": "INCOMES",
          "category": { "idCategory": 1, "description": "Salary", "type": "INCOMES" },
          "comment": "Monthly salary"
        }
      ],
      "categoryTotal": 5000.00
    }
  ],
  "totalAmount": 6000.00
}
```

### 3. Get Categories

**GET** `/api/v1/finance/categories?type={type}`

```json
// Response: 200 OK
[
  { "idCategory": 1, "description": "Salary", "type": "INCOMES" },
  { "idCategory": 8, "description": "Other Income", "type": "INCOMES" }
]
```

### 4. Export Transactions to CSV

**GET** `/api/v1/finance/transactions/export?dateFrom={dateFrom}&dateTo={dateTo}`

Returns `text/csv` with Content-Disposition header.

```csv
Transaction ID,Transaction Date,Amount,Category Description,Category Type,Comment
1,2026-02-01,5000.00,Salary,INCOMES,Monthly salary
2,2026-02-05,250.50,Food & Groceries,EXPENSES,Supermarket shopping
```

### 5. Get Balance Chart Data

**GET** `/api/v1/finance/balance/chart?dateFrom={dateFrom}&dateTo={dateTo}`

Returns time-series data with optional Datawrapper embed URL.

```json
// Response: 200 OK
{
  "chartEmbedUrl": "https://datawrapper.dwcdn.net/abc123/",
  "balanceData": [
    { "date": "2026-01-01", "income": 1000.00, "expense": 200.00, "balance": 800.00 },
    { "date": "2026-01-02", "income": 0.00,    "expense": 150.00, "balance": 650.00 },
    { "date": "2026-01-03", "income": 500.00,  "expense": 0.00,   "balance": 1150.00 }
  ],
  "totalIncome": 1500.00,
  "totalExpense": 350.00,
  "netBalance": 1150.00
}
```

**Aggregation logic:**
- **≤ 31 days** → daily data points (`YYYY-MM-DD`)
- **> 31 days** → monthly data points (`YYYY-MM`)


---

## 🎨 Frontend Architecture

### Component Structure

```
App.js (Main Component)
├── State Management
│   ├── activeTab (income/expense/balance)
│   ├── categories, incomeTransactions, expenseTransactions
│   ├── date filters (for each tab)
│   ├── chartData, chartLoading, chartError
│   └── modal state
│
├── Tab 1: Income
│   ├── Date range filter + Apply button
│   ├── Add Income button → opens modal
│   ├── Transaction list (grouped by category)
│   └── Total summary
│
├── Tab 2: Expense
│   ├── Date range filter + Apply button
│   ├── Add Expense button → opens modal
│   ├── Transaction list (grouped by category)
│   └── Total summary
│
├── Tab 3: Balance
│   ├── Date range filter + Apply button
│   ├── CSV Download button
│   ├── BalanceChart component
│   │   ├── Recharts AreaChart (Income, Expense, Balance areas)
│   │   ├── Custom tooltip with dollar formatting
│   │   ├── Summary cards (Total Income, Total Expense, Net Balance)
│   │   └── Datawrapper iframe embed (if chartEmbedUrl present)
│   └── Transaction list toggle (Incomes / Expenses)
│
└── Add Transaction Modal
    ├── Amount, Date, Type, Category dropdown, Comment
    └── Add button → createTransaction()
```

### API Service Layer

**File**: `frontend/src/services/apiService.js`

Centralized API client using Axios:

```javascript
const API_BASE_URL = '/api/v1/finance';

export default {
  checkConnection: async () => {...},
  getCategories: async (type) => {...},
  getTransactions: async (type, dateFrom, dateTo) => {...},
  createTransaction: async (transaction) => {...},
  exportTransactionsToCsv: async (dateFrom, dateTo) => {...},
  getBalanceChartData: async (dateFrom, dateTo) => {...},
}
```

---

## 🔌 MCP Integration

The MCP (Model Context Protocol) integration adds structured, transferable context between tooling and AI agents. For the full MCP documentation, see **[mcp/README.md](mcp/README.md)**.

### Overview

The MCP layer serves two purposes:

1. **Structured Context** — A domain-specific `McpContext` schema carries financial data state, chart metadata, and workflow tracking between agents and tools.
2. **Datawrapper Visualization** — The backend orchestrates chart creation through `McpDatawrapperClient` using the Spring AI `McpSyncClient` with stdio transport.

### Two Paths to a Chart

```
PATH 1: Automatic (Backend → Spring AI McpSyncClient → datawrapper-mcp → Datawrapper API)
═════════════════════════════════════════════════════════════════════════════
User clicks "Apply" on Balance tab
    → React calls GET /api/v1/finance/balance/chart
    → BalanceChartServiceImpl aggregates transactions
    → DatawrapperServiceImpl calls McpDatawrapperClient
    → McpDatawrapperClient delegates to Spring AI McpSyncClient (stdio transport)
    → McpSyncClient sends JSON-RPC to datawrapper-mcp process
    → Returns embedUrl + balanceData
    → React renders Recharts chart + optional Datawrapper iframe

PATH 2: Agent-driven (AI Agent → datawrapper-mcp → Datawrapper API)
═══════════════════════════════════════════════════════════════════
You tell Copilot/Claude: "create a chart of my expenses"
    → Agent calls GET /api/v1/finance/balance/chart (to get data)
    → Agent calls datawrapper-mcp tool: create_chart (via its own MCP connection)
    → Agent tells you the URL
```

### MCP Components

| Component | Package | Responsibility |
|-----------|---------|---------------|
| `McpClientConfiguration` | `mcp.client` | Spring `@Configuration` — creates `McpSyncClient` bean with `StdioClientTransport`, configures `datawrapper-mcp` process spawning and access token resolution |
| `McpDatawrapperClient` | `mcp.client` | MCP client wrapper — delegates tool calls (`create_chart`, `update_chart`, `publish_chart`) to `McpSyncClient`, parses chart IDs and embed URLs from responses |
| `McpContext` | `mcp.model` | Domain schema — QueryContext, FinancialSummary, ChartContext, WorkflowMetadata |
| `AgentLogService` | `mcp.service` | Logs all agent interactions to `agent_log.txt` with timestamps, context snapshots, and status |

### McpContext Schema

```
McpContext
├── schemaVersion: String          # "1.0.0"
├── domain: String                 # "finance-manager"
├── state: String                  # initialized → data_loaded → validated → chart_created → published
│
├── QueryContext
│   ├── dateFrom / dateTo: LocalDate
│   ├── transactionCount: int
│   └── categoryTypes: List<String>
│
├── FinancialSummary
│   ├── totalIncome / totalExpense / netBalance: BigDecimal
│   ├── dataPointCount: int
│   └── categoryBreakdown: Map<String, BigDecimal>
│
├── ChartContext
│   ├── chartId, chartType, embedUrl, lastUpdated
│   └── published: boolean
│
└── WorkflowMetadata
    ├── agentId, iterationCount, lastAction, lastResult
    └── verificationSteps: List<String>
```


### MCP Client Configuration

The MCP client uses the **Spring AI MCP framework** (`McpSyncClient` with `StdioClientTransport`). Configuration is in `application.yaml`:

```yaml
mcp:
  client:
    enabled: true                    # Set to false to disable MCP/Datawrapper
    command: /path/to/datawrapper-mcp
```

The `McpClientConfiguration` class creates a `McpSyncClient` bean that:
1. Spawns the `datawrapper-mcp` process as a subprocess
2. Configures `StdioClientTransport` for JSON-RPC communication over stdin/stdout
3. Passes `DATAWRAPPER_ACCESS_TOKEN` as an environment variable to the child process
4. Initializes the MCP handshake automatically

When disabled, the app works fully — chart creation is simply skipped and the frontend still renders the local Recharts chart.

---

## 🧪 Testing

### Backend Testing (148 tests)

| Test Class | Tests | Coverage Area |
|------------|-------|---------------|
| `McpDatawrapperClientTest` | 32 | MCP client unit tests: disabled state, tool calls, response parsing, error handling, edge cases |
| `TransactionServiceImplTest` | 29 | Transaction CRUD, queries, export, validation |
| `TransactionControllerTest` | 23 | REST endpoint unit tests (all 5 endpoints) |
| `ValidationUtilsTest` | 18 | Date range, request, export validation |
| `TransactionMapperTest` | 12 | Transaction entity ↔ DTO mapping |
| `CsvExportServiceImplTest` | 11 | CSV generation, escaping, edge cases |
| `CategoryMapperTest` | 8 | Category entity ↔ DTO mapping |
| `BalanceChartServiceImplTest` | 6 | Chart data generation, aggregation, Datawrapper integration |
| `McpDatawrapperClientIntegrationTest` | 3 | MCP client integration: end-to-end flow with mocked McpSyncClient |
| `McpClientConfigurationTest` | 2 | MCP configuration: enabled/disabled McpSyncClient bean creation |
| `McpContextRoundTripTest` | 2 | Context construction and mutation |
| `McpToolDiscoveryTest` | 1 | MCP tool discovery (skipped — requires live datawrapper-mcp server) |
| `AiFinanceManagerApplicationTests` | 1 | Spring context load |

#### Running Backend Tests

```bash
# Run all tests
./mvnw test

# Run specific test class
./mvnw test -Dtest=TransactionControllerTest

# Run with coverage report
./mvnw clean test jacoco:report
# View report: target/site/jacoco/index.html
```

### Frontend Testing

```bash
cd frontend

# Unit tests (Jest + React Testing Library)
npm test                              # Watch mode
npm test -- --watchAll=false          # Single run
npm run test:coverage                 # With coverage

# E2E tests (Cypress)
npm run cypress:open                  # Interactive mode
npm run cypress:run                   # Headless mode
```

### Integration Testing

```bash
./test-integration.sh
./test-api.sh
```

---

## ⚙️ Configuration

### Backend Configuration

**File**: `src/main/resources/application.yaml`

```yaml
server:
  port: 8080

spring:
  application:
    name: ai-finance-manager
  datasource:
    url: jdbc:sqlite:finance_manager.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    database-platform: org.hibernate.community.dialect.SQLiteDialect
    hibernate:
      ddl-auto: none        # Managed by Flyway
    show-sql: true

# Datawrapper MCP integration
# Set DATAWRAPPER_ACCESS_TOKEN env variable or configure below
datawrapper:
  access-token: ${DATAWRAPPER_ACCESS_TOKEN:}

# MCP Client configuration
# Uses Spring AI MCP framework (McpSyncClient with StdioClientTransport)
mcp:
  client:
    enabled: ${MCP_CLIENT_ENABLED:false}
    command: ${MCP_CLIENT_COMMAND:/usr/local/bin/datawrapper-mcp}
```

### Frontend Configuration

**File**: `frontend/package.json`

```json
{
  "proxy": "http://localhost:8080"
}
```

All requests to `/api/*` are automatically proxied from port 3000 to port 8080.

### MCP Server Configuration (for AI Agents)

**File**: `mcp/mcp-config.json`

```json
{
  "mcpServers": {
    "datawrapper": {
      "command": "uvx",
      "args": ["datawrapper-mcp"],
      "env": {
        "DATAWRAPPER_ACCESS_TOKEN": "<your-token>"
      }
    }
  }
}
```

See [mcp/README.md](mcp/README.md) for Claude Code, VS Code + Copilot, and Cursor configuration.

---

## 💻 Development Workflow

### Development Cycle

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
```

### Adding New Features

#### Backend: New Endpoint
1. Create DTO in `application/dto/`
2. Add method in service interface
3. Implement in service class
4. Add endpoint in `TransactionController`
5. Write unit tests

#### Frontend: New Component
1. Create component in `src/components/`
2. Update `App.js` with new state/handlers
3. Update `apiService.js` if new API call needed
4. Write unit tests and Cypress E2E tests

---

## 🚀 Deployment

### Building for Production

```bash
# Backend JAR
./mvnw clean package -DskipTests
java -jar target/ai-finance-manager-0.0.1-SNAPSHOT.jar

# Frontend build
cd frontend
npm run build
```

### Deployment Options

**Option 1 — Single JAR with Embedded Frontend:**
1. `cd frontend && npm run build`
2. Copy `build/` contents to `src/main/resources/static/`
3. `./mvnw clean package`
4. `java -jar target/*.jar` → http://localhost:8080

**Option 2 — Separate Deployment:**
- Backend JAR on server (port 8080)
- Frontend static files on Nginx/CDN

**Option 3 — Docker:**
```dockerfile
FROM eclipse-temurin:21-jre
COPY target/*.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Environment Variables

```bash
SERVER_PORT=8080
SPRING_DATASOURCE_URL=jdbc:sqlite:/path/to/finance_manager.db
DATAWRAPPER_ACCESS_TOKEN=your_token_here    # Optional
MCP_CLIENT_ENABLED=true                     # Optional
MCP_CLIENT_COMMAND=/path/to/datawrapper-mcp # Optional
```

---

## 🐛 Troubleshooting

### Common Issues

```bash
# Port already in use
lsof -ti:8080 | xargs kill -9
lsof -ti:3000 | xargs kill -9

# Database locked
rm finance_manager.db
./mvnw spring-boot:run

# Maven wrapper permission
chmod +x mvnw

# Node modules issues
cd frontend && rm -rf node_modules package-lock.json && npm install

# Java version check
java -version
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes
4. Run tests: `./mvnw test && cd frontend && npm test`
5. Commit: `git commit -am 'Add amazing feature'`
6. Push: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Code Style
- **Backend**: Java conventions, Lombok annotations
- **Frontend**: ES6+, functional components with hooks
- **Tests**: Write tests for new features
- **Coverage**: Maintain 70%+ coverage

---

## 📚 Additional Documentation

- **[mcp/README.md](mcp/README.md)** — Full MCP integration documentation (concepts, datawrapper-mcp, prompt examples)
- **[QUICKSTART.md](QUICKSTART.md)** — Quick start guide

---

## 📄 License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.

---

**Built with ❤️ using Spring Boot, Spring AI MCP, React, Recharts, and Datawrapper**
