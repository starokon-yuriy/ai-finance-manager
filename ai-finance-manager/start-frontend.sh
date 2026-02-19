#!/bin/bash

# Quick script to start the React frontend

echo "=== Starting AI Finance Manager Frontend ==="
echo ""

cd "$(dirname "$0")/frontend"

# Check if Node.js is installed
if ! command -v node &> /dev/null
then
    echo "❌ Node.js is not installed!"
    echo ""
    echo "Please install Node.js first:"
    echo "  brew install node"
    echo ""
    exit 1
fi

# Check if node_modules exists
if [ ! -d "node_modules" ]; then
    echo "📦 Installing dependencies..."
    npm install
    echo ""
fi

echo "🚀 Starting React development server..."
echo ""
echo "The app will open automatically at http://localhost:3000"
echo "Make sure your Spring Boot backend is running on port 8080!"
echo ""

npm start

