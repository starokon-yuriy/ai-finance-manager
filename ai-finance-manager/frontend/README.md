# AI Finance Manager - React Frontend

This is the React frontend for the AI Finance Manager application.

## Prerequisites

Before you begin, ensure you have the following installed:
- Node.js (v16 or higher)
- npm (comes with Node.js)

## Installation Steps

### 1. Install Node.js (if not already installed)

Since you have Homebrew installed, run:

```bash
brew install node
```

Verify the installation:

```bash
node --version
npm --version
```

### 2. Install Dependencies

Navigate to the frontend directory and install the required packages:

```bash
cd frontend
npm install
```

This will install:
- React 18
- React DOM
- React Scripts (Create React App tooling)
- Axios (for API calls)
- Web Vitals (for performance monitoring)

## Running the Application

### Start the Backend (Spring Boot)

First, make sure your Spring Boot backend is running on port 8080.

From the project root directory:

```bash
./mvnw spring-boot:run
```

Or if using Maven:

```bash
mvn spring-boot:run
```

### Start the Frontend (React)

In a separate terminal, navigate to the frontend directory and start the development server:

```bash
cd frontend
npm start
```

The React application will automatically open in your browser at:
```
http://localhost:3000
```

## Project Structure

```
frontend/
├── public/
│   ├── index.html          # HTML template
│   └── manifest.json       # PWA manifest
├── src/
│   ├── App.js              # Main App component (Hello World)
│   ├── App.css             # App styling
│   ├── index.js            # Entry point
│   ├── index.css           # Global styles
│   ├── reportWebVitals.js  # Performance monitoring
│   ├── App.test.js         # Tests
│   └── setupTests.js       # Test configuration
├── package.json            # Dependencies and scripts
└── .gitignore             # Git ignore rules
```

## Features

### Current Implementation (Hello World)

The current implementation includes:
- ✅ Beautiful gradient UI with Hello World message
- ✅ Backend connection testing
- ✅ API status display
- ✅ Responsive design
- ✅ Modern glassmorphism styling
- ✅ CORS configuration for backend communication

### API Integration

The frontend is configured to communicate with the Spring Boot backend:
- **Backend URL**: `http://localhost:8080`
- **API Endpoint**: `/api/transactions`
- **Proxy**: Configured in `package.json` for seamless API calls

## Available Scripts

In the frontend directory, you can run:

### `npm start`

Runs the app in development mode.
Open [http://localhost:3000](http://localhost:3000) to view it in your browser.

The page will reload when you make changes.

### `npm test`

Launches the test runner in interactive watch mode.

### `npm run build`

Builds the app for production to the `build` folder.
It correctly bundles React in production mode and optimizes the build for best performance.

### `npm run eject`

**Note: this is a one-way operation. Once you `eject`, you can't go back!**

## Testing the Setup

1. Start the Spring Boot backend (port 8080)
2. Start the React frontend (port 3000)
3. Open http://localhost:3000 in your browser
4. You should see:
   - A "Hello World!" greeting
   - Backend status indicator
   - Tech stack information
   - A test button to check backend connectivity

## CORS Configuration

The backend has been configured to allow requests from the React frontend:
- **Allowed Origin**: `http://localhost:3000`
- **Allowed Methods**: GET, POST, PUT, DELETE, OPTIONS
- **Configuration File**: `src/main/java/com/ys/ai/aifinancemanager/config/CorsConfig.java`

## Troubleshooting

### Port Already in Use

If port 3000 is already in use:
- You'll be prompted to use another port (usually 3001)
- Or kill the process using port 3000:
  ```bash
  lsof -ti:3000 | xargs kill -9
  ```

### Backend Connection Issues

If the frontend can't connect to the backend:
1. Ensure Spring Boot is running on port 8080
2. Check the console for CORS errors
3. Verify the proxy configuration in `package.json`
4. Check that CORS is properly configured in the backend

### Module Not Found Errors

If you get module not found errors:
```bash
cd frontend
rm -rf node_modules package-lock.json
npm install
```

## Next Steps

Now that you have a working Hello World application, you can:

1. **Build the UI Components**: Create components for transactions, categories, etc.
2. **Add Routing**: Install React Router for navigation
3. **State Management**: Consider Redux or Context API for complex state
4. **API Service Layer**: Create a dedicated service for API calls
5. **Form Handling**: Add forms for creating/editing transactions
6. **Data Visualization**: Add charts for financial insights
7. **Authentication**: Implement user authentication if needed

## Quick Start with Setup Script

Alternatively, you can use the provided setup script:

```bash
chmod +x setup-frontend.sh
./setup-frontend.sh
```

This script will:
1. Check and install Node.js if needed
2. Create the frontend directory structure
3. Install all dependencies
4. Provide instructions for running the app

## Learn More

- [React Documentation](https://react.dev/)
- [Create React App Documentation](https://create-react-app.dev/)
- [Spring Boot CORS](https://spring.io/guides/gs/rest-service-cors/)
- [Axios Documentation](https://axios-http.com/)

