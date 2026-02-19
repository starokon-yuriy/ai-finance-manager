#!/bin/bash

# Complete Start Script for AI Finance Manager
# This script starts both backend and frontend

echo "🚀 Starting AI Finance Manager..."
echo "=================================="
echo ""

# Get the directory where the script is located
SCRIPT_DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
cd "$SCRIPT_DIR"

# Colors
GREEN='\033[0;32m'
BLUE='\033[0;34m'
YELLOW='\033[1;33m'
RED='\033[0;31m'
NC='\033[0m' # No Color

# Check if backend is already running
echo "🔍 Checking if backend is already running..."
if lsof -Pi :8080 -sTCP:LISTEN -t >/dev/null ; then
    echo -e "${YELLOW}⚠️  Backend is already running on port 8080${NC}"
    read -p "Do you want to stop it and restart? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Stopping existing backend..."
        kill $(lsof -t -i:8080)
        sleep 2
    else
        echo -e "${BLUE}Keeping existing backend running${NC}"
        BACKEND_RUNNING=true
    fi
fi

# Check if frontend is already running
echo "🔍 Checking if frontend is already running..."
if lsof -Pi :3000 -sTCP:LISTEN -t >/dev/null ; then
    echo -e "${YELLOW}⚠️  Frontend is already running on port 3000${NC}"
    read -p "Do you want to stop it and restart? (y/n) " -n 1 -r
    echo
    if [[ $REPLY =~ ^[Yy]$ ]]; then
        echo "Stopping existing frontend..."
        kill $(lsof -t -i:3000)
        sleep 2
    else
        echo -e "${BLUE}Keeping existing frontend running${NC}"
        FRONTEND_RUNNING=true
    fi
fi

# Start Backend if not already running
if [ "$BACKEND_RUNNING" != true ]; then
    echo ""
    echo -e "${BLUE}📦 Starting Backend (Spring Boot)...${NC}"
    echo "This will run in the background and log to backend.log"

    # Start backend in background
    nohup ./mvnw spring-boot:run > backend.log 2>&1 &
    BACKEND_PID=$!

    echo "Backend PID: $BACKEND_PID"
    echo "Waiting for backend to start..."

    # Wait for backend to be ready (max 60 seconds)
    for i in {1..60}; do
        if curl -s http://localhost:8080/api/v1/finance/categories?type=INCOMES > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Backend is ready!${NC}"
            BACKEND_STARTED=true
            break
        fi
        echo -n "."
        sleep 1
    done

    if [ "$BACKEND_STARTED" != true ]; then
        echo -e "${RED}❌ Backend failed to start within 60 seconds${NC}"
        echo "Check backend.log for errors:"
        tail -20 backend.log
        exit 1
    fi
else
    echo -e "${GREEN}✅ Backend is already running${NC}"
fi

# Start Frontend if not already running
if [ "$FRONTEND_RUNNING" != true ]; then
    echo ""
    echo -e "${BLUE}🎨 Starting Frontend (React)...${NC}"
    echo "This will run in the background and log to frontend.log"

    # Check if node_modules exists
    if [ ! -d "frontend/node_modules" ]; then
        echo "📦 Installing frontend dependencies..."
        cd frontend
        npm install
        cd ..
    fi

    # Start frontend in background
    cd frontend
    nohup npm start > ../frontend.log 2>&1 &
    FRONTEND_PID=$!
    cd ..

    echo "Frontend PID: $FRONTEND_PID"
    echo "Waiting for frontend to start..."

    # Wait for frontend to be ready (max 60 seconds)
    for i in {1..60}; do
        if curl -s http://localhost:3000 > /dev/null 2>&1; then
            echo -e "${GREEN}✅ Frontend is ready!${NC}"
            FRONTEND_STARTED=true
            break
        fi
        echo -n "."
        sleep 1
    done

    if [ "$FRONTEND_STARTED" != true ]; then
        echo -e "${RED}❌ Frontend failed to start within 60 seconds${NC}"
        echo "Check frontend.log for errors:"
        tail -20 frontend.log
        exit 1
    fi
else
    echo -e "${GREEN}✅ Frontend is already running${NC}"
fi

echo ""
echo "=================================="
echo -e "${GREEN}✨ AI Finance Manager is running!${NC}"
echo ""
echo "📝 URLs:"
echo "   Frontend: http://localhost:3000"
echo "   Backend:  http://localhost:8080"
echo ""
echo "📋 Logs:"
echo "   Backend:  tail -f backend.log"
echo "   Frontend: tail -f frontend.log"
echo ""
echo "🧪 Test Integration:"
echo "   ./test-integration.sh"
echo ""
echo "🛑 To stop:"
echo "   pkill -f spring-boot:run"
echo "   pkill -f react-scripts"
echo ""
echo "🌐 Opening browser..."
sleep 2
open http://localhost:3000 2>/dev/null || xdg-open http://localhost:3000 2>/dev/null || echo "Please open http://localhost:3000 in your browser"

