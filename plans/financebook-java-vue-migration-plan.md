# FinanceBook Migration Plan: Python/React → Java/Vue.js

## Executive Summary

This document outlines the comprehensive migration strategy for transforming the **FinanceBook** private finance management application from:
- **Backend**: Python/FastAPI → Java/Spring Boot
- **Frontend**: React/TypeScript → Vue.js/TypeScript
- **Database**: PostgreSQL (unchanged)
- **Docker**: PostgreSQL (reused)

The migration maintains 100% feature parity, identical business logic, and equivalent user experience while modernizing to Java enterprise standards and Vue.js simplicity.

---

## Table of Contents

1. [Project Overview](#project-overview)
2. [Technology Mapping](#technology-mapping)
3. [Architecture Decisions](#architecture-decisions)
4. [Database Schema](#database-schema)
5. [Backend Migration Strategy](#backend-migration-strategy)
6. [Frontend Migration Strategy](#frontend-migration-strategy)
7. [Testing Strategy](#testing-strategy)
8. [Deployment Plan](#deployment-plan)
9. [Timeline and Phases](#timeline-and-phases)

---

## 1. Project Overview

### Current Architecture (financebookNeo-example)

**Backend (Python/FastAPI)**
- FastAPI web framework with async support
- SQLModel (Pydantic + SQLAlchemy) for ORM
- bcrypt for password hashing
- python-jose for JWT token generation
- Jinja2 for admin panel server-side rendering
- Uvicorn ASGI server
- NumPy for regression calculations
- Custom AST-based formula evaluator

**Frontend (React/TypeScript)**
- React 18 with TypeScript
- TanStack React Query for state management
- React Router for routing
- Styled Components for CSS-in-JS
- Recharts for data visualization
- Axios for HTTP requests
- Vite for build tooling

**Key Features**
- Multi-user authentication with JWT (30-min expiry)
- Admin panel with session-based auth
- Payment item CRUD with categories, recipients, invoices
- Transaction fee engine (table-based regression or formula-based)
- Hierarchical category system with unlimited depth
- CSV import/export
- Statistics with charts (balance over time, pie charts)
- File uploads (category icons, invoice documents)
- Per-user data isolation (multi-tenancy)
- Dark theme UI

### Target Architecture (financeJava)

**Backend (Java/Spring Boot)**
- Spring Boot 3.x (latest LTS)
- Spring Data JPA with Hibernate
- Spring Security with JWT
- BCryptPasswordEncoder
- Thymeleaf for admin panel
- Apache Commons Math for regression
- Expression evaluator (spring-expression or custom parser)
- Jackson for JSON serialization
- Embedded Tomcat server

**Frontend (Vue.js/TypeScript)**
- Vue 3 with Composition API and TypeScript
- Pinia for state management
- Vue Router for routing
- Native CSS/SCSS (no CSS-in-JS)
- Chart.js or ECharts for visualization
- Axios for HTTP requests
- Vite for build tooling

---

## 2. Technology Mapping

### Backend Component Mapping

| Python/FastAPI | Java/Spring Boot | Notes |
|----------------|------------------|-------|
| FastAPI | Spring Web MVC + REST | Controllers with `@RestController` |
| SQLModel | Spring Data JPA + Hibernate | Entity classes with `@Entity` |
| Pydantic schemas | DTO classes | Record classes or POJOs with validation |
| `get_session` dependency | `@Autowired` repositories | Dependency injection |
| `@app.post("/route")` | `@PostMapping("/route")` | Annotation-based routing |
| bcrypt (passlib) | BCryptPasswordEncoder | Built into Spring Security |
| python-jose JWT | java-jwt (auth0) or jjwt | JWT token generation/validation |
| Jinja2 templates | Thymeleaf templates | Server-side rendering |
| NumPy polyfit | Apache Commons Math | Polynomial regression |
| AST expression eval | Spring Expression Language (SpEL) or custom | Safe formula evaluation |
| Uvicorn | Embedded Tomcat | Application server |
| python-dotenv | application.properties/yml | Configuration management |

### Frontend Component Mapping

| React/TypeScript | Vue.js/TypeScript | Notes |
|------------------|-------------------|-------|
| React components | Vue SFC (Single File Components) | `.vue` files with `<script setup>` |
| React Query (TanStack) | Pinia stores + composables | State management |
| React Router | Vue Router | Client-side routing |
| Styled Components | Scoped CSS in SFC or SCSS | No CSS-in-JS needed |
| Recharts | Chart.js or ECharts | Data visualization |
| `useState`, `useEffect` | `ref`, `reactive`, `computed`, `watch` | Reactivity system |
| Context API | Provide/Inject or Pinia | Global state |
| Props & callbacks | Props & emits | Component communication |
| `useNavigate` hook | `useRouter` hook | Programmatic navigation |
| Axios | Axios | HTTP client (same library) |

---

## 3. Architecture Decisions

### 3.1 Backend Architecture

**Layered Architecture**
```
┌─────────────────────────────────────┐
│   Controllers (REST + Web MVC)      │  ← HTTP endpoints
├─────────────────────────────────────┤
│   DTOs (Request/Response objects)   │  ← Data transfer
├─────────────────────────────────────┤
│   Services (Business Logic)         │  ← Core logic
├─────────────────────────────────────┤
│   Repositories (Data Access)        │  ← Database queries
├─────────────────────────────────────┤
│   Entities (JPA Models)             │  ← Database tables
└─────────────────────────────────────┘
```

**Package Structure**
```
com.financebook
├── config/                 # Spring configuration classes
│   ├── SecurityConfig.java
│   ├── JwtConfig.java
│   └── WebConfig.java
├── controller/             # REST API controllers
│   ├── AuthController.java
│   ├── PaymentItemController.java
│   ├── CategoryController.java
│   ├── RecipientController.java
│   └── FileUploadController.java
├── controller.admin/       # Admin panel controllers
│   ├── AdminWebController.java
│   └── AdminApiController.java
├── dto/                    # Data Transfer Objects
│   ├── request/
│   │   ├── UserCreateRequest.java
│   │   ├── PaymentItemCreateRequest.java
│   │   └── ...
│   └── response/
│       ├── UserReadResponse.java
│       ├── PaymentItemReadResponse.java
│       └── ...
├── entity/                 # JPA entities
│   ├── User.java
│   ├── PaymentItem.java
│   ├── Category.java
│   ├── CategoryType.java
│   ├── Recipient.java
│   ├── PaymentItemCategoryLink.java
│   ├── TransactionFeePlan.java
│   └── TransactionFeeRecord.java
├── repository/             # Spring Data repositories
│   ├── UserRepository.java
│   ├── PaymentItemRepository.java
│   ├── CategoryRepository.java
│   └── ...
├── service/                # Business logic services
│   ├── UserService.java
│   ├── AuthService.java
│   ├── PaymentItemService.java
│   ├── CategoryService.java
│   ├── RecipientService.java
│   ├── FeeEngineService.java
│   ├── FileStorageService.java
│   └── CsvImportExportService.java
├── security/               # Security components
│   ├── JwtTokenProvider.java
│   ├── JwtAuthenticationFilter.java
│   ├── UserDetailsServiceImpl.java
│   └── AdminSessionManager.java
├── exception/              # Custom exceptions
│   ├── GlobalExceptionHandler.java
│   ├── ResourceNotFoundException.java
│   └── UnauthorizedException.java
├── util/                   # Utility classes
│   ├── FormulaEvaluator.java
│   ├── RegressionCalculator.java
│   └── NameNormalizer.java
└── FinanceBookApplication.java  # Main class
```

**Key Decisions**

1. **Build Tool**: Maven (industry standard, simpler than Gradle for this project)
2. **Java Version**: Java 17 LTS (current LTS, modern features like records)
3. **Spring Boot Version**: 3.2.x (latest stable)
4. **Database Migration**: Flyway (versioned SQL migrations, better for team collaboration)
5. **Validation**: Jakarta Bean Validation (JSR 380) with Hibernate Validator
6. **API Documentation**: Springdoc OpenAPI 3 (Swagger UI)
7. **Logging**: SLF4J with Logback (Spring Boot default)
8. **Testing**: JUnit 5 + Mockito + Spring Boot Test

### 3.2 Frontend Architecture

**Component Architecture**
```
src/
├── main.ts                 # Application entry point
├── App.vue                 # Root component
├── router/
│   └── index.ts           # Route definitions
├── stores/                 # Pinia stores
│   ├── auth.ts            # Authentication state
│   ├── payments.ts        # Payment items
│   └── categories.ts      # Categories & types
├── services/               # API services
│   ├── api.ts             # Axios instance
│   ├── authService.ts     # Auth endpoints
│   ├── paymentService.ts  # Payment CRUD
│   ├── categoryService.ts # Category endpoints
│   └── recipientService.ts
├── views/                  # Page components
│   ├── LoginPage.vue
│   ├── SummaryPage.vue
│   ├── AddItemPage.vue
│   ├── EditItemPage.vue
│   ├── AddSuccessPage.vue
│   ├── CategoryManagerPage.vue
│   ├── CategoryEditPage.vue
│   ├── StatisticsPage.vue
│   └── NotFoundPage.vue
├── components/             # Reusable components
│   ├── NavigationBar.vue
│   ├── PaymentItemForm.vue
│   ├── ConfirmationDialog.vue
│   ├── PaginationFooter.vue
│   └── ...
├── composables/            # Composition functions
│   ├── useAuth.ts
│   ├── usePagination.ts
│   └── useFileUpload.ts
├── types/                  # TypeScript interfaces
│   └── index.ts
├── styles/                 # Global styles
│   └── global.css
└── assets/                 # Static assets (icons, images)
```

**Key Decisions**

1. **Vue Version**: Vue 3 with Composition API (modern, TypeScript-friendly)
2. **State Management**: Pinia (official Vue state management, simpler than Vuex)
3. **Styling**: Scoped CSS in SFCs + global SCSS (simpler than CSS-in-JS)
4. **Charts**: Chart.js with vue-chartjs (lightweight, good Vue support)
5. **Forms**: Native HTML5 validation + custom validation logic
6. **Date Handling**: date-fns (same as React version)
7. **HTTP Client**: Axios (same as React version)
8. **Build Tool**: Vite (same as React version)

---

## 4. Database Schema

**No changes to database structure** - PostgreSQL schema remains identical. All tables, columns, relationships, and constraints are preserved.

### Core Tables

```sql
-- User table
CREATE TABLE user (
    id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    hashed_password VARCHAR(255) NOT NULL,
    surname VARCHAR(100) NOT NULL,
    prename VARCHAR(100) NOT NULL,
    birth_date DATE,
    phone VARCHAR(30),
    road VARCHAR(200),
    house_number VARCHAR(20),
    region VARCHAR(100),
    postal VARCHAR(20),
    city VARCHAR(100),
    state VARCHAR(100),
    is_admin BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Payment Item table
CREATE TABLE paymentitem (
    id SERIAL PRIMARY KEY,
    amount DECIMAL(10,2) NOT NULL,
    date TIMESTAMP NOT NULL,
    periodic BOOLEAN DEFAULT FALSE,
    description VARCHAR(1000),
    invoice_path VARCHAR(255),
    product_image_path VARCHAR(255),
    recipient_id INTEGER REFERENCES recipient(id),
    standard_category_id INTEGER REFERENCES category(id),
    user_id INTEGER REFERENCES user(id) NOT NULL
);

-- Category Type table
CREATE TABLE categorytype (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description VARCHAR(1000),
    user_id INTEGER REFERENCES user(id) NOT NULL
);

-- Category table (hierarchical)
CREATE TABLE category (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    type_id INTEGER REFERENCES categorytype(id) NOT NULL,
    parent_id INTEGER REFERENCES category(id),
    icon_file VARCHAR(255),
    user_id INTEGER REFERENCES user(id) NOT NULL
);

-- Recipient table
CREATE TABLE recipient (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    address VARCHAR(500),
    user_id INTEGER REFERENCES user(id) NOT NULL
);

-- Many-to-many link table
CREATE TABLE paymentitemcategorylink (
    payment_item_id INTEGER REFERENCES paymentitem(id) ON DELETE CASCADE,
    category_id INTEGER REFERENCES category(id),
    PRIMARY KEY (payment_item_id, category_id)
);

-- Transaction Fee Plan table
CREATE TABLE transactionfeeplan (
    id SERIAL PRIMARY KEY,
    user_id INTEGER REFERENCES user(id) UNIQUE NOT NULL,
    mode VARCHAR(20) DEFAULT 'table',
    formula_text TEXT,
    amount_table_json TEXT DEFAULT '[0]',
    interval_data_json TEXT DEFAULT '{}',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Transaction Fee Record table
CREATE TABLE transactionfeerecord (
    id SERIAL PRIMARY KEY,
    payment_item_id INTEGER REFERENCES paymentitem(id) NOT NULL,
    user_id INTEGER REFERENCES user(id) NOT NULL,
    fee_amount DECIMAL(10,2) NOT NULL,
    original_amount DECIMAL(10,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### Flyway Migration Strategy

Create versioned migrations in `src/main/resources/db/migration/`:

- `V1__initial_schema.sql` - Create all tables
- `V2__create_admin_user.sql` - Insert default admin user
- `V3__create_standard_category_type.sql` - Insert standard category type
- `V4__add_indexes.sql` - Performance indexes

---

## 5. Backend Migration Strategy

### 5.1 Core Components Translation

#### Authentication System

**Python (FastAPI)**
```python
# auth.py
def hash_password(plain_password: str) -> str:
    password_bytes = plain_password.encode("utf-8")
    salt = bcrypt.gensalt()
    hashed = bcrypt.hashpw(password_bytes, salt)
    return hashed.decode("utf-8")

def create_access_token(data: dict) -> str:
    to_encode = data.copy()
    expire = datetime.utcnow() + timedelta(minutes=30)
    to_encode.update({"exp": expire})
    return jwt.encode(to_encode, SECRET_KEY, algorithm=ALGORITHM)
```

**Java (Spring Boot)**
```java
// AuthService.java
@Service
public class AuthService {
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    
    public String hashPassword(String plainPassword) {
        return passwordEncoder.encode(plainPassword);
    }
    
    public boolean verifyPassword(String plain, String hashed) {
        return passwordEncoder.matches(plain, hashed);
    }
    
    public String createAccessToken(String username) {
        return jwtTokenProvider.generateToken(username);
    }
}

// JwtTokenProvider.java
@Component
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;
    
    @Value("${jwt.expiration:1800000}") // 30 min
    private long validityInMilliseconds;
    
    public String generateToken(String username) {
        Claims claims = Jwts.claims().setSubject(username);
        Date now = new Date();
        Date validity = new Date(now.getTime() + validityInMilliseconds);
        
        return Jwts.builder()
            .setClaims(claims)
            .setIssuedAt(now)
            .setExpiration(validity)
            .signWith(SignatureAlgorithm.HS256, secretKey)
            .compact();
    }
}
```

#### Fee Engine Service

**Python (FastAPI)**
```python
# fee_engine.py
def compute_regression_coefficients(points: list[dict], max_fee: float) -> list[float]:
    if len(points) == 0:
        return [0.0, max_fee]
    # ... numpy polyfit logic
    xs = np.array([p["freq"] for p in points])
    ys = np.array([p["fee"] for p in points])
    degree = min(len(points) - 1, 5)
    coeffs = np.polyfit(xs, ys, degree)
    return list(reversed(coeffs.tolist()))
```

**Java (Spring Boot)**
```java
// RegressionCalculator.java
@Component
public class RegressionCalculator {
    public double[] computeRegressionCoefficients(
            List<Map<String, Double>> points, double maxFee) {
        
        if (points.isEmpty()) {
            return new double[]{0.0, maxFee};
        }
        
        // Use Apache Commons Math
        double[] xs = points.stream()
            .mapToDouble(p -> p.get("freq")).toArray();
        double[] ys = points.stream()
            .mapToDouble(p -> p.get("fee")).toArray();
        
        int degree = Math.min(points.size() - 1, 5);
        PolynomialFitter fitter = new PolynomialFitter(
            new LeastSquaresBuilder().build());
        
        for (int i = 0; i < xs.length; i++) {
            fitter.addObservedPoint(xs[i], ys[i]);
        }
        
        return fitter.fit(degree);
    }
}
```

### 5.2 Entity Relationships

**JPA Annotations for Entities**

```java
// User.java
@Entity
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, length = 50, nullable = false)
    private String username;
    
    @Column(nullable = false)
    private String hashedPassword;
    
    @Column(length = 100, nullable = false)
    private String surname;
    
    @Column(length = 100, nullable = false)
    private String prename;
    
    private LocalDate birthDate;
    
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isAdmin = false;
    
    @Column(columnDefinition = "BOOLEAN DEFAULT TRUE")
    private Boolean isActive = true;
    
    @Column(updatable = false)
    @CreatedDate
    private LocalDateTime createdAt;
    
    // Getters, setters, constructors
}

// PaymentItem.java
@Entity
@Table(name = "paymentitem")
public class PaymentItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;
    
    @Column(nullable = false)
    private LocalDateTime date;
    
    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean periodic = false;
    
    @Column(length = 1000)
    private String description;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id")
    private Recipient recipient;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "standard_category_id")
    private Category standardCategory;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @ManyToMany
    @JoinTable(
        name = "paymentitemcategorylink",
        joinColumns = @JoinColumn(name = "payment_item_id"),
        inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();
    
    // Transient field for fee information
    @Transient
    private BigDecimal transactionFee;
}

// Category.java (self-referencing tree)
@Entity
@Table(name = "category")
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(length = 255, nullable = false)
    private String name;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "type_id", nullable = false)
    private CategoryType type;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> children = new ArrayList<>();
    
    @Column(length = 255)
    private String iconFile;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
}
```

### 5.3 Repository Layer

```java
// PaymentItemRepository.java
@Repository
public interface PaymentItemRepository extends JpaRepository<PaymentItem, Long> {
    
    // Find all payment items for a specific user
    List<PaymentItem> findByUserIdOrderByDateDesc(Long userId);
    
    // Find expenses only (amount < 0)
    @Query("SELECT p FROM PaymentItem p WHERE p.user.id = :userId AND p.amount < 0")
    List<PaymentItem> findExpensesByUserId(@Param("userId") Long userId);
    
    // Find incomes only (amount >= 0)
    @Query("SELECT p FROM PaymentItem p WHERE p.user.id = :userId AND p.amount >= 0")
    List<PaymentItem> findIncomesByUserId(@Param("userId") Long userId);
    
    // Find by category (including descendants)
    @Query("SELECT DISTINCT p FROM PaymentItem p " +
           "JOIN p.categories c " +
           "WHERE p.user.id = :userId AND c.id IN :categoryIds")
    List<PaymentItem> findByCategoryIds(
        @Param("userId") Long userId, 
        @Param("categoryIds") Set<Long> categoryIds);
}

// CategoryRepository.java
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    List<Category> findByUserIdAndTypeId(Long userId, Long typeId);
    
    List<Category> findByParentId(Long parentId);
    
    // Find all root categories (parent_id is null)
    List<Category> findByUserIdAndParentIdIsNull(Long userId);
    
    Optional<Category> findByUserIdAndName(Long userId, String name);
}
```

### 5.4 Service Layer Pattern

```java
// PaymentItemService.java
@Service
@Transactional
public class PaymentItemService {
    
    private final PaymentItemRepository paymentItemRepository;
    private final CategoryRepository categoryRepository;
    private final RecipientRepository recipientRepository;
    private final FeeEngineService feeEngineService;
    
    public PaymentItemReadResponse createPaymentItem(
            PaymentItemCreateRequest request, Long userId) {
        
        // 1. Validate recipient ownership
        if (request.getRecipientId() != null) {
            Recipient recipient = recipientRepository
                .findById(request.getRecipientId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient not found"));
            
            if (!recipient.getUser().getId().equals(userId)) {
                throw new UnauthorizedException("Recipient does not belong to you");
            }
        }
        
        // 2. Validate and fetch categories
        Set<Category> categories = new HashSet<>();
        if (request.getCategoryIds() != null && !request.getCategoryIds().isEmpty()) {
            // Validate one category per type
            Map<Long, Category> typeMap = new HashMap<>();
            for (Long catId : request.getCategoryIds()) {
                Category cat = categoryRepository.findById(catId)
                    .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
                
                if (!cat.getUser().getId().equals(userId)) {
                    throw new UnauthorizedException("Category does not belong to you");
                }
                
                if (typeMap.containsKey(cat.getType().getId())) {
                    throw new ValidationException("Only one category per type allowed");
                }
                
                typeMap.put(cat.getType().getId(), cat);
                categories.add(cat);
            }
        }
        
        // 3. Create payment item
        PaymentItem item = new PaymentItem();
        item.setAmount(request.getAmount());
        item.setDate(request.getDate());
        item.setPeriodic(request.getPeriodic());
        item.setDescription(request.getDescription());
        item.setCategories(categories);
        item.setUser(userRepository.findById(userId).orElseThrow());
        
        PaymentItem savedItem = paymentItemRepository.save(item);
        
        // 4. Compute and apply fee
        BigDecimal fee = feeEngineService.createFeeRecord(savedItem, userId);
        
        // 5. Build response
        return buildResponse(savedItem, fee);
    }
}
```

---

## 6. Frontend Migration Strategy

### 6.1 Component Structure Comparison

**React Component**
```tsx
// LoginPage.tsx
import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

export const LoginPage: React.FC = () => {
  const [username, setUsername] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [rememberMe, setRememberMe] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();
  
  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    try {
      await login(username, password, rememberMe);
      navigate('/');
    } catch (err) {
      setError('Invalid credentials');
    }
  };
  
  return (
    <form onSubmit={handleSubmit}>
      {/* JSX markup */}
    </form>
  );
};
```

**Vue Component**
```vue
<!-- LoginPage.vue -->
<script setup lang="ts">
import { ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const username = ref('');
const password = ref('');
const error = ref('');
const rememberMe = ref(false);
const authStore = useAuthStore();
const router = useRouter();

const handleSubmit = async () => {
  try {
    await authStore.login(username.value, password.value, rememberMe.value);
    router.push('/');
  } catch (err) {
    error.value = 'Invalid credentials';
  }
};
</script>

<template>
  <form @submit.prevent="handleSubmit">
    <!-- Template markup -->
  </form>
</template>

<style scoped>
/* Component-scoped styles */
</style>
```

### 6.2 State Management

**React Query → Pinia**

**React (TanStack Query)**
```tsx
// hooks.ts
export const usePaymentItems = (filters?: FilterParams) => {
  return useQuery({
    queryKey: ['paymentItems', filters],
    queryFn: () => api.getPaymentItems(filters),
  });
};
```

**Vue (Pinia Store)**
```typescript
// stores/payments.ts
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { paymentService } from '@/services/paymentService';

export const usePaymentStore = defineStore('payments', () => {
  const items = ref<PaymentItem[]>([]);
  const loading = ref(false);
  const error = ref<string | null>(null);
  
  const expenses = computed(() => 
    items.value.filter(item => item.amount < 0)
  );
  
  const incomes = computed(() => 
    items.value.filter(item => item.amount >= 0)
  );
  
  async function fetchPaymentItems(filters?: FilterParams) {
    loading.value = true;
    error.value = null;
    try {
      items.value = await paymentService.getAll(filters);
    } catch (err: any) {
      error.value = err.message;
    } finally {
      loading.value = false;
    }
  }
  
  async function createPaymentItem(data: PaymentItemCreate) {
    const newItem = await paymentService.create(data);
    items.value.push(newItem);
    return newItem;
  }
  
  return {
    items,
    expenses,
    incomes,
    loading,
    error,
    fetchPaymentItems,
    createPaymentItem,
  };
});
```

### 6.3 Routing

**React Router**
```tsx
// App.tsx
<Routes>
  <Route path="/login" element={<LoginPage />} />
  <Route path="/" element={<ProtectedRoute><SummaryPage /></ProtectedRoute>} />
  <Route path="/add" element={<ProtectedRoute><AddItemPage /></ProtectedRoute>} />
  <Route path="/edit/:id" element={<ProtectedRoute><EditItemPage /></ProtectedRoute>} />
  <Route path="/statistics" element={<ProtectedRoute><StatisticsPage /></ProtectedRoute>} />
  <Route path="*" element={<NotFoundPage />} />
</Routes>
```

**Vue Router**
```typescript
// router/index.ts
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const routes = [
  { path: '/login', component: () => import('@/views/LoginPage.vue') },
  { 
    path: '/', 
    component: () => import('@/views/SummaryPage.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/add', 
    component: () => import('@/views/AddItemPage.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/edit/:id', 
    component: () => import('@/views/EditItemPage.vue'),
    meta: { requiresAuth: true }
  },
  { 
    path: '/statistics', 
    component: () => import('@/views/StatisticsPage.vue'),
    meta: { requiresAuth: true }
  },
  { path: '/:pathMatch(.*)*', component: () => import('@/views/NotFoundPage.vue') }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

// Navigation guard
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore();
  
  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login');
  } else {
    next();
  }
});

export default router;
```

### 6.4 Charts Migration

**Recharts → Chart.js**

**React (Recharts)**
```tsx
<AreaChart data={balanceData}>
  <CartesianGrid strokeDasharray="3 3" />
  <XAxis dataKey="date" />
  <YAxis />
  <Tooltip />
  <Area type="monotone" dataKey="balance" stroke="#8884d8" fill="#8884d8" />
</AreaChart>
```

**Vue (Chart.js with vue-chartjs)**
```vue
<script setup lang="ts">
import { Line } from 'vue-chartjs';
import { computed } from 'vue';

const props = defineProps<{ balanceData: Array<{ date: string; balance: number }> }>();

const chartData = computed(() => ({
  labels: props.balanceData.map(d => d.date),
  datasets: [{
    label: 'Balance',
    data: props.balanceData.map(d => d.balance),
    borderColor: '#8884d8',
    backgroundColor: 'rgba(136, 132, 216, 0.2)',
    fill: true,
  }]
}));

const chartOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: true }
  }
};
</script>

<template>
  <Line :data="chartData" :options="chartOptions" />
</template>
```

---

## 7. Testing Strategy

### 7.1 Backend Testing

**Unit Tests**
```java
// UserServiceTest.java
@SpringBootTest
public class UserServiceTest {
    
    @MockBean
    private UserRepository userRepository;
    
    @Autowired
    private UserService userService;
    
    @Test
    void testCreateUser_Success() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setSurname("Test");
        request.setPrename("User");
        
        when(userRepository.save(any())).thenAnswer(i -> i.getArgument(0));
        
        UserReadResponse response = userService.createUser(request);
        
        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository).save(any(User.class));
    }
    
    @Test
    void testCreateUser_DuplicateUsername_ThrowsException() {
        // Test duplicate username validation
    }
}

// FeeEngineServiceTest.java
@SpringBootTest
public class FeeEngineServiceTest {
    
    @Autowired
    private FeeEngineService feeEngineService;
    
    @Test
    void testComputeRegressionCoefficients_ZeroPoints() {
        double[] coeffs = feeEngineService.computeRegressionCoefficients(
            Collections.emptyList(), 0.1);
        
        assertArrayEquals(new double[]{0.0, 0.1}, coeffs);
    }
    
    @Test
    void testComputeFee_FormulaMode() {
        // Test formula-based fee calculation
    }
    
    @Test
    void testComputeFee_TableMode_ThreePoints() {
        // Test table-based regression with 3 data points
    }
}
```

**Integration Tests**
```java
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class AuthControllerIntegrationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    void testLogin_Success() throws Exception {
        LoginRequest request = new LoginRequest("admin", "admin");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.access_token").exists())
            .andExpect(jsonPath("$.token_type").value("bearer"));
    }
    
    @Test
    void testGetProfile_WithValidToken() throws Exception {
        String token = obtainJwtToken("admin", "admin");
        
        mockMvc.perform(get("/auth/me")
                .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.username").value("admin"));
    }
}
```

### 7.2 Frontend Testing

**Vitest + Vue Test Utils**

```typescript
// LoginPage.spec.ts
import { describe, it, expect, beforeEach, vi } from 'vitest';
import { mount } from '@vue/test-utils';
import { createPinia, setActivePinia } from 'pinia';
import LoginPage from '@/views/LoginPage.vue';
import { useAuthStore } from '@/stores/auth';

describe('LoginPage', () => {
  beforeEach(() => {
    setActivePinia(createPinia());
  });
  
  it('renders login form', () => {
    const wrapper = mount(LoginPage);
    expect(wrapper.find('input[type="text"]').exists()).toBe(true);
    expect(wrapper.find('input[type="password"]').exists()).toBe(true);
    expect(wrapper.find('button[type="submit"]').exists()).toBe(true);
  });
  
  it('calls login on form submit', async () => {
    const wrapper = mount(LoginPage);
    const authStore = useAuthStore();
    const loginSpy = vi.spyOn(authStore, 'login');
    
    await wrapper.find('input[type="text"]').setValue('testuser');
    await wrapper.find('input[type="password"]').setValue('password');
    await wrapper.find('form').trigger('submit');
    
    expect(loginSpy).toHaveBeenCalledWith('testuser', 'password', false);
  });
});

// stores/auth.spec.ts
describe('Auth Store', () => {
  it('sets user on successful login', async () => {
    const authStore = useAuthStore();
    
    vi.spyOn(authService, 'login').mockResolvedValue({
      access_token: 'fake-token',
      token_type: 'bearer'
    });
    
    await authStore.login('user', 'pass', false);
    
    expect(authStore.isAuthenticated).toBe(true);
    expect(localStorage.getItem('token')).toBe('fake-token');
  });
});
```

---

## 8. Deployment Plan

### 8.1 Docker Configuration

**Backend Dockerfile**
```dockerfile
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

COPY target/financebook-0.1.0.jar app.jar

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=prod
ENV DATABASE_URL=jdbc:postgresql://db:5432/financebook

ENTRYPOINT ["java", "-jar", "app.jar"]
```

**Frontend Dockerfile**
```dockerfile
FROM node:18-alpine as build

WORKDIR /app

COPY package*.json ./
RUN npm ci

COPY . .
RUN npm run build

FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
```

**docker-compose.yml**
```yaml
version: '3.8'

services:
  db:
    image: postgres:15
    environment:
      POSTGRES_USER: yourself
      POSTGRES_PASSWORD: secretPassword
      POSTGRES_DB: financebook
    volumes:
      - postgres_data:/var/lib/postgresql/data
    ports:
      - "5432:5432"
    networks:
      - financebook-network

  backend:
    build: ./backend
    ports:
      - "8080:8080"
    environment:
      DATABASE_URL: jdbc:postgresql://db:5432/financebook
      DATABASE_USERNAME: yourself
      DATABASE_PASSWORD: secretPassword
      JWT_SECRET_KEY: ${JWT_SECRET_KEY}
      ADMIN_DEFAULT_PASSWORD: ${ADMIN_DEFAULT_PASSWORD}
    depends_on:
      - db
    networks:
      - financebook-network

  frontend:
    build: ./frontend
    ports:
      - "80:80"
    depends_on:
      - backend
    networks:
      - financebook-network

volumes:
  postgres_data:

networks:
  financebook-network:
    driver: bridge
```

### 8.2 CI/CD Pipeline (GitHub Actions Example)

```yaml
name: Build and Test

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  backend:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 17
      uses: actions/setup-java@v3
      with:
        java-version: '17'
        distribution: 'temurin'
    
    - name: Build with Maven
      run: mvn clean package -DskipTests
      working-directory: ./backend
    
    - name: Run tests
      run: mvn test
      working-directory: ./backend
    
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: backend-jar
        path: backend/target/*.jar

  frontend:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Setup Node.js
      uses: actions/setup-node@v3
      with:
        node-version: '18'
    
    - name: Install dependencies
      run: npm ci
      working-directory: ./frontend
    
    - name: Run tests
      run: npm test
      working-directory: ./frontend
    
    - name: Build
      run: npm run build
      working-directory: ./frontend
    
    - name: Upload artifact
      uses: actions/upload-artifact@v3
      with:
        name: frontend-dist
        path: frontend/dist
```

---

## 9. Timeline and Phases

### Estimated Timeline

| Phase | Duration | Deliverables |
|-------|----------|--------------|
| Phase 1: Analysis & Setup | 1-2 days | Project structure, skeletal apps |
| Phase 2-4: Backend Core | 3-4 days | Entities, repositories, auth |
| Phase 5-7: Backend Features | 4-5 days | Services, controllers, admin panel |
| Phase 8-10: Frontend Core | 3-4 days | Setup, routing, auth, layout |
| Phase 11-14: Frontend Features | 5-6 days | All pages, components, charts |
| Phase 15-16: Testing | 3-4 days | Unit, integration, E2E tests |
| Phase 17: Refactoring | 2-3 days | Code quality, optimization |
| Phase 18: Documentation | 1-2 days | Docs, deployment guide |
| **Total** | **22-30 days** | Complete application |

### Critical Path Items

1. **Database entities and relationships** - Foundation for everything
2. **Authentication system** - Security must work correctly
3. **Fee engine service** - Complex business logic requires careful porting
4. **Category tree operations** - Recursive queries need optimization
5. **Multi-user data isolation** - Security critical feature

### Risk Mitigation

1. **Complex regression logic**: Port NumPy logic carefully to Apache Commons Math, write comprehensive tests
2. **Formula evaluator**: Use Spring Expression Language or implement custom AST parser with whitelist
3. **Performance**: Add database indexes for frequently queried columns
4. **Data migration**: Create Flyway migrations for schema versioning
5. **Session management**: Properly implement both JWT and cookie-based auth

---

## 10. Key Differences and Adaptations

### 10.1 Language Paradigm Differences

| Aspect | Python | Java |
|--------|--------|------|
| Type system | Dynamic, gradual (type hints) | Static, strong |
| Null handling | None, Optional | null, Optional<T> |
| Collections | list, dict, set | List<T>, Map<K,V>, Set<T> |
| Error handling | try/except | try/catch (checked exceptions) |
| Async | async/await | CompletableFuture, @Async |
| Decorators | @decorator | @Annotation |
| Dependency injection | Parameters with = Depends() | @Autowired, constructor injection |

### 10.2 Framework Differences

| Feature | FastAPI | Spring Boot |
|---------|---------|-------------|
| Route definition | `@app.get("/path")` | `@GetMapping("/path")` |
| Validation | Pydantic models | Jakarta Bean Validation |
| ORM | SQLAlchemy/SQLModel | Hibernate/JPA |
| DI | Function parameters | Constructor/field injection |
| Config | .env + os.getenv | application.yml + @Value |
| Templates | Jinja2 | Thymeleaf |
| Static files | StaticFiles mount | ResourceHandler config |

### 10.3 Testing Approaches

| Type | Python/FastAPI | Java/Spring Boot |
|------|----------------|------------------|
| Unit tests | pytest | JUnit 5 |
| Mocking | pytest-mock, unittest.mock | Mockito |
| Integration | TestClient | MockMvc, WebTestClient |
| DB tests | SQLite in-memory | H2 in-memory or Testcontainers |
| Coverage | pytest-cov | JaCoCo |

---

## 11. Success Criteria

The migration will be considered successful when:

1. ✅ **100% feature parity** with Python/React version
2. ✅ **All 93 tests pass** (equivalent to Python test suite)
3. ✅ **API contract maintained** - frontend can switch backends seamlessly
4. ✅ **Performance benchmarks met** - response times <= Python version
5. ✅ **Security audit passed** - no vulnerabilities introduced
6. ✅ **Code quality** - SonarQube score >= 80%
7. ✅ **Documentation complete** - README, API docs, deployment guide
8. ✅ **Zero data loss** - existing PostgreSQL data works with new backend
9. ✅ **Multi-user isolation verified** - users cannot access each other's data
10. ✅ **Admin panel functional** - all admin features working

---

## Appendix A: Technology Stack Summary

### Backend Stack
- **Java 17 LTS**
- **Spring Boot 3.2.x**
  - Spring Web MVC
  - Spring Data JPA
  - Spring Security
- **Hibernate ORM**
- **PostgreSQL 15**
- **Flyway** (migrations)
- **JWT** (java-jwt or jjwt)
- **BCrypt** (Spring Security)
- **Thymeleaf** (templates)
- **Apache Commons Math** (regression)
- **Lombok** (boilerplate reduction)
- **Maven** (build tool)
- **JUnit 5 + Mockito** (testing)

### Frontend Stack
- **Vue 3** (Composition API)
- **TypeScript 5**
- **Pinia** (state management)
- **Vue Router 4**
- **Axios**
- **Chart.js** or **ECharts** (charts)
- **date-fns** (date utilities)
- **Vite** (build tool)
- **Vitest + Vue Test Utils** (testing)

### Infrastructure
- **PostgreSQL 15** (database)
- **Docker** (containerization)
- **Docker Compose** (orchestration)
- **Nginx** (frontend serving, reverse proxy)

---

## Next Steps

1. **Review and approve this plan** with stakeholders
2. **Set up development environment** (Java 17, Maven, Node.js, Docker)
3. **Create financeJava directory** with backend and frontend subdirectories
4. **Initialize Spring Boot project** using Spring Initializr
5. **Initialize Vue.js project** using `npm create vue@latest`
6. **Begin Phase 1 implementation** following the todo list

This concludes the comprehensive migration plan. The implementation can proceed systematically through the 18 phases outlined in the todo list.
