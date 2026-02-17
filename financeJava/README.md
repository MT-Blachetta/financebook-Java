# FinanceBook (Java Version)

Professional personal finance management application featuring a robust Spring Boot backend and a modern React frontend.

## Overview

FinanceBook is a comprehensive tool for tracking expenses, incomes, and managing financial categories. It supports multi-user environments with strict data isolation, transaction fee calculations, and detailed statistical analysis.

This version is a reimplementation of the original Python-based FinanceBook, leveraging the enterprise capabilities of **Java 17** and **Spring Boot**, paired with a **React** frontend for a responsive user experience.

## 🏗 Architecture

### Backend (`financeJava/backend`)
-   **Framework**: Spring Boot 3.2.2
-   **Language**: Java 17
-   **Database**: PostgreSQL 15+
-   **ORM**: Hibernate / Spring Data JPA
-   **Security**: Spring Security with JWT Authentication
-   **Build Tool**: Maven

### Frontend (`financeJava/frontend`)
-   **Framework**: React 18
-   **Build Tool**: Vite
-   **Styling**: Styled Components
-   **State/Data Fetching**: React Query, Axios
-   **Routing**: React Router

---

## Prerequisites

Before starting, ensure you have the following installed:

1.  **Java Development Kit (JDK) 17** or higher.
2.  **Node.js v18** or higher (with npm).
3.  **PostgreSQL 15** or higher.
4.  **Maven 3.8+** (optional, wrapper provided).

---

## Configuration

### Backend Configuration
The backend is configured via `src/main/resources/application.yml`. Key environment variables:

| Variable | Description | Default |
|----------|-------------|---------|
| `DATABASE_URL` | PostgreSQL Connection URL | `jdbc:postgresql://localhost:5432/financebook` |
| `DATABASE_USERNAME` | Database User | `yourself` |
| `DATABASE_PASSWORD` | Database Password | `secretPassword` |
| `JWT_SECRET_KEY` | Secret for signing tokens | *(dev default provided)* |
| `ADMIN_DEFAULT_PASSWORD` | Initial password for 'admin' user | `admin` |

### Frontend Configuration
The frontend uses a Vite proxy to communicate with the backend.
-   **Proxy Target**: `http://localhost:8000` (Defined in `vite.config.ts`)
-   **Port**: 5173 (Default)

---

## Installation & Running (Development)

### 1. Database Setup
Create a PostgreSQL database named `financebook`.

```bash
# Example using psql
createdb financebook
```

### 2. Backend Setup
Navigate to the backend directory and run the application.

```bash
cd financeJava/backend

# Run with Maven (Using wrapper if available, or installed maven)
mvn spring-boot:run
```
*The application will start on port **8000**. On first run, it will automatically create the database schema and a default admin user.*

### 3. Frontend Setup
Open a new terminal, navigate to the frontend directory, install dependencies, and start the dev server.

```bash
cd financeJava/frontend

# Install dependencies
npm install

# Start development server
npm run dev
```
*The frontend will be accessible at `http://localhost:5173`.*

---

## Deployment (Production)

### 1. Build Backend
Package the Spring Boot application into a JAR file.

```bash
cd financeJava/backend
mvn clean package -DskipTests
```
The executable JAR will be located at `target/financebook-backend-0.1.0.jar`.

### 2. Build Frontend
Build the static assets for the frontend.

```bash
cd financeJava/frontend
npm run build
```
The static files will be generated in the `dist` directory.

### 3. Running in Production
You can run the JAR file directly. For a complete deployment, you should serve the frontend static files using a web server like Nginx, using it as a reverse proxy to forward API requests to the Java backend.

**Example execution:**
```bash
export DATABASE_URL=jdbc:postgresql://localhost:5432/financebook_prod
export DATABASE_USERNAME=prod_user
export DATABASE_PASSWORD=prod_pass
java -jar financebook-backend-0.1.0.jar
```

---

## User Manual

### Login
-   **URL**: `http://localhost:5173/login`
-   **Default Admin Credentials**:
    -   Username: `admin`
    -   Password: `admin` (or valid configured password)

### Dashboard (Summary)
The home page displays a list of all payment items sorted by date.
-   **Filter**: Use the filters to view Expenses, Incomes, or fees.
-   **Categories**: Filter by specific categories using the dropdown.

### Adding Payments
Click the **"ADD"** button in the navigation bar.
-   **Description**: Name of the transaction.
-   **Amount**: Value (positive for income, negative for expense).
-   **Date**: Date of transaction.
-   **Category**: Assign a category for classification.
-   **Recipient**: (Optional) Who received/paid the money.

### Managing Categories
Navigate to the "Categories" section (if available in menu) or manage them on the fly when adding payments. Categories are hierarchical.

### Statistics
Click the **"STATISTICS"** link to view visual breakdowns of your finances:
-   **Balance History**: Line chart of running balance.
-   **Income/Expense**: Pie charts by category.

---

## Troubleshooting

**Q: "Login failed" error?**
A: Ensure the backend is running on port 8000. Check the browser console network tab. If using the default admin, ensure the database initialized correctly.

**Q: Categories not showing icons?**
A: Ensure the `icons` directory is correctly pointed to in `application.yml` and contains the SVG files.

**Q: Connection refused to localhost:8000?**
A: The backend is not running or crashed. Check the terminal output where you ran `mvn spring-boot:run`.
