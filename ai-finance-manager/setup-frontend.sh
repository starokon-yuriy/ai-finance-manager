#!/bin/bash

# Setup script for AI Finance Manager Frontend

echo "=== AI Finance Manager Frontend Setup ==="
echo ""

# Check if Node.js is installed
if ! command -v node &> /dev/null
then
    echo "Node.js is not installed. Installing via Homebrew..."
    brew install node
    echo "Node.js installed successfully!"
else
    echo "Node.js is already installed: $(node --version)"
fi

echo ""
echo "npm version: $(npm --version)"
echo ""

# Navigate to project root
cd "$(dirname "$0")"

# Create frontend directory if it doesn't exist
if [ ! -d "frontend" ]; then
    echo "Creating frontend directory..."
    mkdir -p frontend
fi

# Navigate to frontend directory
cd frontend

# Initialize React app if package.json doesn't exist
if [ ! -f "package.json" ]; then
    echo "Initializing React application..."
    npx create-react-app . --template minimal

    # If create-react-app fails, use manual setup
    if [ $? -ne 0 ]; then
        echo "Using manual React setup..."
        # package.json will be created by the script
    fi
fi

# Install dependencies
echo "Installing dependencies..."
npm install

echo ""
echo "=== Setup Complete ==="
echo "To start the frontend development server, run:"
echo "  cd frontend"
echo "  npm start"
echo ""
echo "The React app will run on http://localhost:3000"
echo "The Spring Boot backend should run on http://localhost:8080"

