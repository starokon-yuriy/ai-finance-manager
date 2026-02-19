# Quick Start Guide - AI Finance Manager

A full-stack personal finance management application with Spring Boot backend and React frontend.

## 📋 Prerequisites

### Backend Requirements
- **Java 21** or higher
- **Maven 3.6+** (or use included Maven wrapper `./mvnw`)

### Frontend Requirements
- **Node.js 18** (matches CI environment — higher versions may cause lock file drift)
- **npm 10+** (comes with Node.js 18)

### Optional Tools
- **jq** - for pretty-printing JSON responses: `brew install jq`
- **SQLite** - for database inspection: `brew install sqlite3`

---

## 🚀 Quick Start (Automated)

### Start Both Backend & Frontend
```bash
./start-all.sh
```

This script will:
1. Check and kill any existing processes on ports 8080 and 3000
2. Start Spring Boot backend on **http://localhost:8080**
3. Start React frontend on **http://localhost:3000**
4. Wait for backend to be ready (health check)
5. Install frontend dependencies if needed
6. Open browser automatically

**Logs:**
- Backend: `backend.log`
- Frontend: `startup.log`

### Start Frontend Only
```bash
./start-frontend.sh
```

---

## 💻 Manual Setup

### Backend Setup

#### Option 1: Run with Maven Wrapper (Recommended)
```bash
./mvnw spring-boot:run
```

#### Option 2: Build and Run JAR
```bash
./mvnw clean package -DskipTests
java -jar target/ai-finance-manager-0.0.1-SNAPSHOT.jar
```

#### Option 3: Run from IDE
- Import as Maven project in IntelliJ IDEA / Eclipse / VS Code
- Run `AiFinanceManagerApplication` main class

**Backend URL:** http://localhost:8080  
**API Base URL:** http://localhost:8080/api/v1/finance

### Frontend Setup

```bash
# Navigate to frontend directory
cd frontend

# Install dependencies (first time only)
npm install

# Start development server
npm start
```

**Frontend URL:** http://localhost:3000  
**Proxy Configuration:** Requests to `/api/*` are proxied to `http://localhost:8080`

---

## 🏗️ Technology Stack

### Backend
| Technology | Version | Purpose |
|------------|---------|---------|
| Java | 21 | Programming language |
| Spring Boot | 4.0.2 | Application framework |
| Spring Data JPA | 4.0.2 | Data access layer |
| Hibernate | (via Spring Boot) | ORM framework |
| SQLite | 3.45.1.0 | Embedded database |
| Flyway | (via Spring Boot) | Database migrations |
| MapStruct | 1.6.3 | Bean mapping |
| Lombok | (via Spring Boot) | Reduce boilerplate |
| JaCoCo | 0.8.11 | Code coverage |

### Frontend
| Technology | Version | Purpose |
|------------|---------|---------|
| React | 18.3.1 | UI framework |
| React Scripts | 5.0.1 | Build tooling |
| Axios | 1.13.5 | HTTP client |
| Cypress | 13.17.0 | E2E testing |
| Jest | (via React Scripts) | Unit testing |
| @testing-library/react | 13.4.0 | Component testing |
| @testing-library/jest-dom | 5.17.0 | DOM matchers |
| @testing-library/user-event | 14.6.1 | User interaction testing |

---

## 🔌 Ports & Endpoints

### Backend Port: **8080**
- Base URL: `http://localhost:8080`
- API Prefix: `/api/v1/finance`

#### Endpoints:
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/categories?type={INCOMES\|EXPENSES}` | Get categories by type |
| GET | `/transactions?type={type}&dateFrom={date}&dateTo={date}` | Get transactions by type and date range |
| POST | `/transactions` | Create new transaction |
| GET | `/transactions/export?dateFrom={date}&dateTo={date}` | Export transactions to CSV |

### Frontend Port: **3000**
- URL: `http://localhost:3000`
- Development server with hot reload

---

## 📁 Project Structure

```
ai-finance-manager/
├── src/main/
│   ├── java/com/ys/ai/aifinancemanager/
│   │   ├── api/          # REST Controllers
│   │   ├── application/  # Services, Mappers, DTOs
│   │   └── domain/       # Entities, Repositories
│   └── resources/
│       ├── application.yaml          # Main config
│       └── db/migration/             # Flyway migrations
│           ├── V1__Create_initial_schema.sql
│           └── V2__Insert_initial_data.sql
├── src/test/
│   ├── java/             # Unit & Integration tests
│   └── resources/
│       └── application-test.yaml     # Test config
├── frontend/
│   ├── src/
│   │   ├── App.js                    # Main React component
│   │   ├── services/apiService.js    # API client
│   │   └── ...
│   ├── cypress/e2e/                  # E2E tests
│   ├── package.json
│   └── cypress.config.js
├── pom.xml               # Maven dependencies
├── start-all.sh          # Start both BE & FE
├── start-frontend.sh     # Start FE only
├── test-api.sh           # Quick API tests
└── test-integration.sh   # Full integration tests
```

---

## 🗄️ Database

### Type: **SQLite**
- **Location:** `finance_manager.db` (created in project root)
- **Dialect:** `org.hibernate.community.dialect.SQLiteDialect`
- **Migration Tool:** Flyway

### Database Schema

#### Tables:
1. **CATEGORY**
   - `id_category` (INTEGER, PK)
   - `description` (TEXT)
   - `type` (TEXT) - INCOMES or EXPENSES

2. **TRANSACTION**
   - `id_transaction` (INTEGER, PK)
   - `amount` (REAL)
   - `transaction_date` (TEXT)
   - `transaction_type` (TEXT)
   - `id_category` (INTEGER, FK)
   - `comment` (TEXT)

### View Database:
```bash
sqlite3 finance_manager.db

# Inside SQLite shell:
.tables
.schema CATEGORY
.schema TRANSACTION
SELECT * FROM CATEGORY;
SELECT * FROM "TRANSACTION";
.exit
```

### Reset Database:
```bash
rm finance_manager.db
./mvnw spring-boot:run  # Will recreate with migrations
```

---

## 🧪 Testing

### Backend Tests

#### Run All Tests
```bash
./mvnw test
```

#### Run Unit Tests Only
```bash
./mvnw test -Dtest="*Test"
```

#### Run Integration Tests Only
```bash
./mvnw test -Dtest="*IntegrationTest"
```

#### Generate Coverage Report
```bash
./mvnw clean test jacoco:report
# Report: target/site/jacoco/index.html
```

#### Quick API Test Script
```bash
./test-api.sh
```
Tests all endpoints with sample data.

#### Full Integration Test Script
```bash
./test-integration.sh
```
Comprehensive test suite with colored output and HTTP status validation.

### Frontend Tests

#### Run Unit Tests
```bash
cd frontend
npm test                    # Interactive watch mode
npm test -- --watchAll=false  # Single run
```

#### Run with Coverage
```bash
npm run test:coverage
# Report: frontend/coverage/lcov-report/index.html
```

#### Run E2E Tests (Cypress)
```bash
# Interactive mode
npm run cypress:open

# Headless mode
npm run cypress:run
```

#### Full Test Suite Script
```bash
cd frontend
./run-tests.sh --all        # Unit + E2E
./run-tests.sh --unit-only  # Unit tests only
./run-tests.sh --e2e-only   # E2E tests only
./run-tests.sh --coverage   # With coverage report
```

**Test Coverage Goals:**
- Backend: 70%+ (lines, branches, functions)
- Frontend: 70%+ (lines, branches, functions, statements)

---

## 📊 Sample API Calls

### 1. List All INCOMES Categories
```bash
curl "http://localhost:8080/api/v1/finance/categories?type=INCOMES" | jq
```

**Response:**
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

### 2. List All EXPENSES Categories
```bash
curl "http://localhost:8080/api/v1/finance/categories?type=EXPENSES" | jq
```

**Response:**
```json
[
  {
    "idCategory": 2,
    "description": "Food & Groceries",
    "type": "EXPENSES"
  },
  {
    "idCategory": 3,
    "description": "Transportation",
    "type": "EXPENSES"
  }
]
```

### 3. Add an Expense Transaction
```bash
curl -X POST http://localhost:8080/api/v1/finance/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 150.00,
    "transactionDate": "2026-02-16",
    "transactionType": "EXPENSES",
    "categoryId": 2,
    "comment": "Weekly groceries"
  }' | jq
```

**Response:**
```json
{
  "idTransaction": 7,
  "amount": 150.00,
  "transactionDate": "2026-02-16",
  "transactionType": "EXPENSES",
  "category": {
    "idCategory": 2,
    "description": "Food & Groceries",
    "type": "EXPENSES"
  },
  "comment": "Weekly groceries"
}
```

### 4. Add an Income Transaction
```bash
curl -X POST http://localhost:8080/api/v1/finance/transactions \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 3000.00,
    "transactionDate": "2026-02-15",
    "transactionType": "INCOMES",
    "categoryId": 1,
    "comment": "Monthly salary payment"
  }' | jq
```

### 5. Get INCOMES Transactions for February 2026
```bash
curl "http://localhost:8080/api/v1/finance/transactions?type=INCOMES&dateFrom=2026-02-01&dateTo=2026-02-28" | jq
```

**Response:**
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
    }
  ],
  "totalAmount": 5000.00
}
```

### 6. Get EXPENSES Transactions for February 2026
```bash
curl "http://localhost:8080/api/v1/finance/transactions?type=EXPENSES&dateFrom=2026-02-01&dateTo=2026-02-28" | jq
```

### 7. Export Transactions to CSV
```bash
curl "http://localhost:8080/api/v1/finance/transactions/export?dateFrom=2026-02-01&dateTo=2026-02-28" \
  -o transactions.csv
```

**CSV Content:**
```csv
Transaction ID,Amount,Transaction Date,Transaction Type,Category,Comment
1,5000.00,2026-02-01,INCOMES,Salary,Monthly salary
2,250.50,2026-02-05,EXPENSES,Food & Groceries,Supermarket shopping
3,50.00,2026-02-07,EXPENSES,Transportation,Gas station
```

---

## 🛑 Stopping the Application

### Stop Backend
```bash
# Find and kill process on port 8080
lsof -ti:8080 | xargs kill
# Or force kill
lsof -ti:8080 | xargs kill -9
```

### Stop Frontend
```bash
# Press Ctrl+C in terminal, or:
lsof -ti:3000 | xargs kill
```

### Stop Both
```bash
# Kill both processes
lsof -ti:8080,3000 | xargs kill
```

---

## 🔧 Configuration

### Backend Configuration (`src/main/resources/application.yaml`)
```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:sqlite:finance_manager.db
    driver-class-name: org.sqlite.JDBC
  jpa:
    show-sql: true              # Log SQL queries
    hibernate:
      ddl-auto: none            # Managed by Flyway
  flyway:
    enabled: true               # Database migrations
```

### Test Configuration (`src/test/resources/application-test.yaml`)
```yaml
spring:
  datasource:
    url: jdbc:sqlite::memory:   # In-memory DB for tests
  jpa:
    show-sql: false             # Reduce test logs
```

### Frontend Configuration (`frontend/package.json`)
```json
{
  "proxy": "http://localhost:8080",  // Proxy API calls to backend
  "scripts": {
    "start": "react-scripts start",  // Port 3000
    "build": "react-scripts build",
    "test": "react-scripts test"
  }
}
```

---

## 📝 Common Issues & Solutions

### Issue 1: Port 8080 Already in Use
```bash
# Kill existing process
lsof -ti:8080 | xargs kill -9

# Or use different port (edit application.yaml)
server:
  port: 8081
```

### Issue 2: Port 3000 Already in Use
```bash
# Kill existing process
lsof -ti:3000 | xargs kill

# Or set PORT environment variable
PORT=3001 npm start
```

### Issue 3: Maven Wrapper Permission Denied
```bash
chmod +x mvnw
```

### Issue 4: Node Modules Missing
```bash
cd frontend
npm install
```

### Issue 5: Database Locked
```bash
# Close all connections and restart
rm finance_manager.db
./mvnw spring-boot:run
```

### Issue 6: Java Version Mismatch
```bash
# Check Java version
java -version  # Should be 21+

# Set JAVA_HOME (macOS with brew)
export JAVA_HOME=$(/usr/libexec/java_home -v 21)
```

### Issue 7: Cannot Resolve Dependencies
```bash
# Clean and rebuild
./mvnw clean install -U  # -U forces update
```

---

## 🚢 CI/CD Pipeline

GitHub Actions workflows are configured in `.github/workflows/`:

### Workflows:
1. **ci.yml** - Main CI pipeline
   - Runs on push/PR to main/develop
   - Backend: build, test, coverage
   - Frontend: build, test, coverage

2. **pr-checks.yml** - PR validation
   - Code quality checks
   - Test execution
   - Build verification

3. **dependency-check.yml** - Security scanning
   - Dependency vulnerability checks
   - Scheduled runs

4. **release.yml** - Release automation
   - Version tagging
   - Artifact publishing

### Run CI Validation Locally:
```bash
./validate-ci.sh
```

---

## 🎯 Development Workflow

### 1. Start Development Environment
```bash
./start-all.sh
```

### 2. Make Changes
- Backend: Edit Java files in `src/main/java/`
- Frontend: Edit React components in `frontend/src/`
- Hot reload is enabled for both

### 3. Run Tests
```bash
# Backend tests
./mvnw test

# Frontend tests
cd frontend && npm test
```

### 4. Verify Integration
```bash
./test-integration.sh
```

### 5. Check Coverage
```bash
# Backend
./mvnw jacoco:report
open target/site/jacoco/index.html

# Frontend
cd frontend && npm run test:coverage
open coverage/lcov-report/index.html
```

---

## 📚 Additional Documentation

- **[README.md](README.md)** - Project overview and features
- **[ARCHITECTURE.md](ARCHITECTURE.md)** - System architecture and data flow
- **[FRONTEND_SETUP.md](FRONTEND_SETUP.md)** - Detailed frontend setup
- **[frontend/TESTING.md](frontend/TESTING.md)** - Testing documentation
- **[CI_SETUP_SUMMARY.md](CI_SETUP_SUMMARY.md)** - CI/CD setup details

---

## 🎉 Next Steps

1. ✅ Start the application with `./start-all.sh`
2. ✅ Open http://localhost:3000 in your browser
3. ✅ Try adding income and expense transactions
4. ✅ Run the test scripts to verify everything works
5. ✅ Explore the API with `./test-api.sh`
6. ✅ Review code coverage reports
7. ✅ Read [ARCHITECTURE.md](ARCHITECTURE.md) to understand the system design

Happy coding! 🚀

