#!/bin/bash

# Integration Test Script for AI Finance Manager
# This script tests all API endpoints to verify BE-FE integration

echo "🧪 Testing AI Finance Manager API Integration..."
echo "================================================"
echo ""

BASE_URL="http://localhost:8080/api/v1/finance"
DATE_FROM="2026-02-01"
DATE_TO="2026-02-28"

# Colors for output
GREEN='\033[0;32m'
RED='\033[0;31m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# Test 1: Get Income Categories
echo "1️⃣  Testing GET /categories?type=INCOMES"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/categories?type=INCOMES")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ SUCCESS${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY" | jq '.[0:2]' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ FAILED${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 2: Get Expense Categories
echo "2️⃣  Testing GET /categories?type=EXPENSES"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/categories?type=EXPENSES")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ SUCCESS${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY" | jq '.[0:2]' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ FAILED${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 3: Get Income Transactions
echo "3️⃣  Testing GET /transactions?type=INCOMES&dateFrom=$DATE_FROM&dateTo=$DATE_TO"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions?type=INCOMES&dateFrom=$DATE_FROM&dateTo=$DATE_TO")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ SUCCESS${NC} - Status: $HTTP_CODE"
    echo "Response preview:"
    echo "$BODY" | jq '.categorySummaries[0] | {category: .category.description, total: .categoryTotal, count: (.transactions | length)}' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ FAILED${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 4: Get Expense Transactions
echo "4️⃣  Testing GET /transactions?type=EXPENSES&dateFrom=$DATE_FROM&dateTo=$DATE_TO"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions?type=EXPENSES&dateFrom=$DATE_FROM&dateTo=$DATE_TO")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ SUCCESS${NC} - Status: $HTTP_CODE"
    echo "Response preview:"
    echo "$BODY" | jq '.categorySummaries[0] | {category: .category.description, total: .categoryTotal, count: (.transactions | length)}' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ FAILED${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 5: Create Transaction (Income)
echo "5️⃣  Testing POST /transactions (Create Income)"
PAYLOAD='{
  "amount": 1000.00,
  "transactionDate": "2026-02-17",
  "categoryId": 1,
  "comment": "Integration test transaction"
}'

RESPONSE=$(curl -s -w "\n%{http_code}" -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 201 ]; then
    echo -e "${GREEN}✅ SUCCESS${NC} - Status: $HTTP_CODE"
    echo "Created transaction:"
    echo "$BODY" | jq '{id: .idTransaction, amount: .amount, category: .category.description}' 2>/dev/null || echo "$BODY"
else
    echo -e "${RED}❌ FAILED${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

# Test 6: Export Transactions to CSV
echo "6️⃣  Testing GET /transactions/export?dateFrom=$DATE_FROM&dateTo=$DATE_TO"
RESPONSE=$(curl -s -w "\n%{http_code}" "$BASE_URL/transactions/export?dateFrom=$DATE_FROM&dateTo=$DATE_TO")
HTTP_CODE=$(echo "$RESPONSE" | tail -n1)
BODY=$(echo "$RESPONSE" | head -n-1)

if [ "$HTTP_CODE" -eq 200 ]; then
    echo -e "${GREEN}✅ SUCCESS${NC} - Status: $HTTP_CODE"
    echo "CSV Content (first 3 lines):"
    echo "$BODY" | head -n 3
else
    echo -e "${RED}❌ FAILED${NC} - Status: $HTTP_CODE"
    echo "Response: $BODY"
fi
echo ""

echo "================================================"
echo "✨ Integration tests completed!"
echo ""
echo "📝 Notes:"
echo "   - If tests failed with connection errors, ensure backend is running on port 8080"
echo "   - Start backend: ./mvnw spring-boot:run"
echo "   - Start frontend: cd frontend && npm start"

