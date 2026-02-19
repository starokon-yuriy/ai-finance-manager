#!/bin/bash

echo "=== Testing Personal Finance Manager API ==="
echo ""

BASE_URL="http://localhost:8080/api/v1/finance"

echo "1. Get all INCOMES categories"
curl -s -X GET "$BASE_URL/categories?type=INCOMES" | jq '.' || echo "Failed to get INCOMES categories"
echo ""
echo ""

echo "2. Get all EXPENSES categories"
curl -s -X GET "$BASE_URL/categories?type=EXPENSES" | jq '.' || echo "Failed to get EXPENSES categories"
echo ""
echo ""

echo "3. Add a new expense transaction"
curl -s -X POST "$BASE_URL/transactions" \
  -H "Content-Type: application/json" \
  -d '{
    "amount": 75.50,
    "transactionDate": "2026-02-16",
    "transactionType": "EXPENSES",
    "categoryId": 2,
    "comment": "Grocery shopping"
  }' | jq '.' || echo "Failed to add transaction"
echo ""
echo ""

echo "4. Get INCOMES transactions for February 2026 grouped by category"
curl -s -X GET "$BASE_URL/transactions?type=INCOMES&dateFrom=2026-02-01&dateTo=2026-02-28" | jq '.' || echo "Failed to get INCOMES transactions"
echo ""
echo ""

echo "5. Get EXPENSES transactions for February 2026 grouped by category"
curl -s -X GET "$BASE_URL/transactions?type=EXPENSES&dateFrom=2026-02-01&dateTo=2026-02-28" | jq '.' || echo "Failed to get EXPENSES transactions"
echo ""
echo ""

echo "6. Export transactions to CSV for February 2026"
curl -s -X GET "$BASE_URL/transactions/export?dateFrom=2026-02-01&dateTo=2026-02-28" -o transactions.csv && echo "CSV file saved to transactions.csv" || echo "Failed to export CSV"
echo ""
echo "CSV Content Preview:"
head -10 transactions.csv 2>/dev/null || echo "No CSV file found"
echo ""
echo ""

echo "=== Test completed ==="

