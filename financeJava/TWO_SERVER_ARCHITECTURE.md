# Two-Server Architecture

FinanceBook now uses a **two-server architecture** to separate concerns:

## Architecture Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      FinanceBook System                      │
├─────────────────────────────────────────────────────────────┤
│                                                              │
│  ┌──────────────────┐              ┌──────────────────────┐ │
│  │  Admin Panel     │              │   Java Backend API   │ │
│  │  (Python/FastAPI)│◄────────────►│   (Spring Boot)      │ │
│  │  Port: 8080      │   HTTP/REST  │   Port: 8000         │ │
│  └──────────────────┘              └──────────────────────┘ │
│         │                                     │              │
│         │                                     │              │
│         └─────────┬───────────────────────────┘              │
│                   ▼                                          │
│         ┌──────────────────┐                                 │
│         │   PostgreSQL     │                                 │
│         │   Port: 5432     │                                 │
│         └──────────────────┘                                 │
│                                                              │
└─────────────────────────────────────────────────────────────┘
```

## Server Responsibilities

### 1. Java Backend API (Port 8000)

**Location:** `financeJava/backend/`

**Responsibilities:**
- REST API endpoints for payment items, categories, recipients
- JWT-based authentication for API clients
- Business logic and data validation
- Transaction fee computation
- File upload handling (icons, invoices)
- Database operations via JPA/Hibernate

**Technology Stack:**
- Java 17
- Spring Boot 3.2
- Spring Security (JWT)
- PostgreSQL via JPA/Hibernate
- Flyway for migrations
- Swagger/OpenAPI documentation

**API Documentation:** http://localhost:8000/docs

**Starting the Java Backend:**
```bash
cd financeJava/backend
mvn spring-boot:run
```

### 2. Python Admin Panel (Port 8080)

**Location:** `financeJava/backend/adminpanel/`

**Responsibilities:**
- Admin authentication (session-based cookies)
- User management interface
- Dashboard with system statistics
- Transaction fee plan configuration
- HTML rendering via Jinja2 templates

**Technology Stack:**
- Python 3.10+
- FastAPI
- SQLModel (for direct database access)
- Jinja2 templates
- bcrypt for password hashing

**Admin Panel:** http://localhost:8080/admin/login

**Starting the Admin Panel:**
```bash
cd financeJava/backend/adminpanel
./run_admin.sh
```

Or manually:
```bash
cd financeJava/backend/adminpanel
python3 -m venv venv
source venv/bin/activate
pip install -r requirements.txt
python main.py
```

## Communication Between Servers

### Admin Panel → Java Backend

The admin panel queries statistics and manages data by:
1. **Direct database access** - Reads from PostgreSQL directly for user info and statistics
2. **Future REST API calls** - Can make HTTP requests to Java backend at `http://localhost:8000` for operations

### Shared Database

Both servers share the **same PostgreSQL database**:
- Connection: `postgresql://yourself:secretPassword@localhost:5432/financebook`
- Schema managed by Java backend (Flyway migrations)
- Admin panel uses read/write access for user management

## Default Credentials

**Admin Login:**
- Username: `admin`
- Password: `admin`
- URL: http://localhost:8080/admin/login

**API Authentication:**
- Use `/auth/login` endpoint on port 8000
- Receives JWT token for API access

## Environment Variables

### Java Backend (Port 8000)

```bash
DATABASE_URL=jdbc:postgresql://localhost:5432/financebook
DATABASE_USERNAME=yourself
DATABASE_PASSWORD=secretPassword
JWT_SECRET_KEY=your-jwt-secret-key
```

### Python Admin Panel (Port 8080)

```bash
DATABASE_URL=postgresql://yourself:secretPassword@localhost:5432/financebook
ADMIN_SESSION_SECRET=your-session-secret
ADMIN_PORT=8080
JAVA_BACKEND_URL=http://localhost:8000
```

## Starting Both Servers

**Terminal 1 - Java Backend:**
```bash
cd financeJava/backend
mvn spring-boot:run
```

**Terminal 2 - Admin Panel:**
```bash
cd financeJava/backend/adminpanel
./run_admin.sh
```

**Terminal 3 - PostgreSQL (if not running):**
```bash
# Using Docker
docker run -d \
  --name financebook-postgres \
  -e POSTGRES_DB=financebook \
  -e POSTGRES_USER=yourself \
  -e POSTGRES_PASSWORD=secretPassword \
  -p 5432:5432 \
  postgres:15
```

## Workflow

1. **Start PostgreSQL database** (port 5432)
2. **Start Java backend** (port 8000) - Creates schema via Flyway
3. **Start Python admin panel** (port 8080) - Connects to same database
4. **Access admin panel** at http://localhost:8080/admin/login
5. **API documentation** at http://localhost:8000/docs

## Benefits of Two-Server Architecture

✅ **Separation of Concerns** - Admin UI separate from business logic
✅ **Technology Choice** - Use Python for admin UI, Java for core API
✅ **Independent Scaling** - Scale each server independently
✅ **Development Flexibility** - Different teams can work on each server
✅ **Easier Migration** - Can replace admin panel without touching API

## Why This Architecture?

The original Python `financebookNeo-example` had both API and admin panel in one FastAPI server. We separated them to:

1. Keep the **proven admin UI** from the Python version
2. Build the **core API in Java** with Spring Boot
3. Allow **gradual migration** - admin panel can be ported to Java later
4. Provide **clear boundaries** between admin management and API operations
