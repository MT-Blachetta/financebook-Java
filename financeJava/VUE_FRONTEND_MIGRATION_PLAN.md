# Vue.js Frontend Migration Plan

## Overview

This document outlines the complete strategy for migrating the FinanceBook frontend from **React + TypeScript** to **Vue 3 + TypeScript**, ensuring compatibility with the Java Spring Boot backend on port 8000.

---

## Technology Stack Comparison

### Current (React)

| Technology | Purpose |
|-----------|---------|
| React 18 | UI Framework |
| TypeScript 5.5 | Type Safety |
| Vite 5.2 | Build Tool |
| React Router 6 | Routing |
| TanStack React Query 5 | Data Fetching & Caching |
| React Context API | Global State |
| Styled-Components 6 | CSS-in-JS |
| Axios 1.6 | HTTP Client |
| Recharts 3 | Charts/Visualization |
| date-fns 3.6 | Date Formatting |

### Target (Vue.js)

| Technology | Purpose | Replaces |
|-----------|---------|----------|
| Vue 3 | UI Framework | React 18 |
| TypeScript 5.5 | Type Safety | (same) |
| Vite 5.2 | Build Tool | (same) |
| Vue Router 4 | Routing | React Router |
| Pinia 2 | State Management | Context API + React Query |
| Vue SFC Styles / CSS Modules | Styling | Styled-Components |
| Axios 1.6 | HTTP Client | (same) |
| Chart.js 4 or ECharts 5 | Charts | Recharts |
| date-fns 3.6 | Date Formatting | (same) |
| VueUse | Utility Composables | React hooks |

---

## Project Structure (Vue.js)

```
financeJava/frontend/
├── public/
│   └── favicon.ico
├── src/
│   ├── api/
│   │   ├── client.ts              # Axios instance with interceptors
│   │   ├── auth.api.ts            # Auth endpoints
│   │   ├── payment.api.ts         # Payment item endpoints
│   │   ├── category.api.ts        # Category endpoints
│   │   ├── recipient.api.ts       # Recipient endpoints
│   │   └── types.ts               # API request/response types
│   ├── assets/
│   │   ├── arrow-left.svg
│   │   ├── arrow-right.svg
│   │   ├── down.svg
│   │   ├── up.svg
│   │   └── periodic-icon.svg
│   ├── components/
│   │   ├── NavigationBar.vue
│   │   ├── NavigationDrawer.vue
│   │   ├── PaymentItemForm.vue
│   │   ├── ConfirmationDialog.vue
│   │   ├── LoadingSpinner.vue
│   │   └── common/
│   │       ├── Button.vue
│   │       ├── Input.vue
│   │       └── Card.vue
│   ├── composables/
│   │   ├── useAuth.ts             # Auth composable
│   │   ├── usePayments.ts         # Payment data fetching
│   │   ├── useCategories.ts       # Category data fetching
│   │   └── useNotification.ts     # Toast notifications
│   ├── router/
│   │   ├── index.ts               # Route configuration
│   │   └── guards.ts              # Auth guards
│   ├── stores/
│   │   ├── auth.ts                # Pinia auth store
│   │   ├── payments.ts            # Pinia payments store
│   │   ├── categories.ts          # Pinia categories store
│   │   └── ui.ts                  # UI state (drawer, etc.)
│   ├── types/
│   │   ├── models.ts              # Domain models (User, PaymentItem, etc.)
│   │   └── enums.ts               # Enums and constants
│   ├── styles/
│   │   ├── variables.css          # CSS custom properties
│   │   ├── reset.css              # CSS reset
│   │   └── global.css             # Global styles
│   ├── utils/
│   │   ├── format.ts              # Date formatting helpers
│   │   ├── validation.ts          # Form validation
│   │   └── constants.ts           # Text limits, etc.
│   ├── views/
│   │   ├── LoginView.vue
│   │   ├── SummaryView.vue
│   │   ├── AddPaymentView.vue
│   │   ├── EditPaymentView.vue
│   │   ├── AddSuccessView.vue
│   │   ├── CategoryTypesView.vue
│   │   ├── CategoriesView.vue
│   │   ├── StatisticsView.vue
│   │   └── NotFoundView.vue
│   ├── App.vue
│   ├── main.ts
│   └── vite-env.d.ts
├── index.html
├── package.json
├── tsconfig.json
├── tsconfig.app.json
├── tsconfig.node.json
├── vite.config.ts
└── README.md
```

---

## Migration Strategy

### Phase 1: Project Initialization ✅

**Goal:** Set up Vue.js project with TypeScript and Vite.

**Steps:**
1. Create Vue 3 project with Vite and TypeScript
2. Install core dependencies
3. Configure Vite proxy to Java backend (port 8000)
4. Set up TypeScript configuration
5. Create folder structure

**Dependencies:**
```json
{
  "dependencies": {
    "vue": "^3.4.0",
    "vue-router": "^4.3.0",
    "pinia": "^2.1.0",
    "axios": "^1.6.8",
    "chart.js": "^4.4.0",
    "vue-chartjs": "^5.3.0",
    "date-fns": "^3.6.0",
    "@vueuse/core": "^10.9.0"
  },
  "devDependencies": {
    "@vitejs/plugin-vue": "^5.0.0",
    "@vue/tsconfig": "^0.5.0",
    "typescript": "^5.5.0",
    "vite": "^5.2.0",
    "vue-tsc": "^2.0.0"
  }
}
```

---

### Phase 2: Core Infrastructure

**Goal:** Establish foundational services and utilities.

#### 2.1 TypeScript Types

Create `src/types/models.ts` matching Java backend DTOs:

```typescript
// Match Java UserReadResponse
export interface User {
  id: number;
  username: string;
  surname: string;
  prename: string;
  birthDate?: string;
  phone?: string;
  road?: string;
  houseNumber?: string;
  region?: string;
  postal?: string;
  city?: string;
  state?: string;
  isAdmin: boolean;
  isActive: boolean;
  createdAt: string;
}

// Match Java PaymentItemReadResponse
export interface PaymentItem {
  id: number;
  amount: number;
  date: string;
  periodic: boolean;
  description?: string;
  recipientId?: number;
  recipient?: Recipient;
  categories?: Category[];
  standardCategoryId?: number;
  standardCategory?: Category;
  invoicePath?: string;
  transactionFee?: number;
}

// Match Java CategoryReadResponse
export interface Category {
  id: number;
  name: string;
  typeId: number;
  parentId?: number;
  iconFile?: string;
  children?: Category[];
}

// Match Java CategoryTypeReadResponse
export interface CategoryType {
  id: number;
  name: string;
  description?: string;
}

// Match Java RecipientReadResponse
export interface Recipient {
  id: number;
  name: string;
  address?: string;
}

// Form data types
export interface PaymentItemFormData {
  amount: number;
  date: string;
  periodic: boolean;
  description?: string;
  recipientId?: number;
  categoryIds?: number[];
  standardCategoryId?: number;
}
```

#### 2.2 Axios Client

Create `src/api/client.ts`:

```typescript
import axios from 'axios';
import type { AxiosInstance } from 'axios';
import { useAuthStore } from '@/stores/auth';

const client: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor: Add JWT token
client.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('financebook_token') 
      || sessionStorage.getItem('financebook_token');
    
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor: Handle 401
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      const authStore = useAuthStore();
      authStore.logout();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default client;
```

#### 2.3 API Services

Create `src/api/auth.api.ts`:

```typescript
import client from './client';
import type { User } from '@/types/models';

export interface LoginRequest {
  username: string;
  password: string;
}

export interface JwtResponse {
  accessToken: string;
  tokenType: string;
}

export const authApi = {
  async login(credentials: LoginRequest): Promise<JwtResponse> {
    const response = await client.post<JwtResponse>('/auth/login', credentials);
    return response.data;
  },

  async getCurrentUser(): Promise<User> {
    const response = await client.get<User>('/auth/me');
    return response.data;
  },

  async register(userData: any): Promise<void> {
    await client.post('/auth/register', userData);
  },
};
```

---

### Phase 3: State Management with Pinia

#### 3.1 Auth Store

Create `src/stores/auth.ts`:

```typescript
import { defineStore } from 'pinia';
import { ref, computed } from 'vue';
import { authApi } from '@/api/auth.api';
import type { User } from '@/types/models';

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string | null>(null);
  const user = ref<User | null>(null);
  const isLoading = ref(false);

  const isAuthenticated = computed(() => !!token.value);

  async function login(username: string, password: string, rememberMe: boolean) {
    isLoading.value = true;
    try {
      const response = await authApi.login({ username, password });
      token.value = response.accessToken;
      
      // Store token
      if (rememberMe) {
        localStorage.setItem('financebook_token', response.accessToken);
        sessionStorage.removeItem('financebook_token');
      } else {
        sessionStorage.setItem('financebook_token', response.accessToken);
        localStorage.removeItem('financebook_token');
      }

      // Fetch user profile
      user.value = await authApi.getCurrentUser();
    } finally {
      isLoading.value = false;
    }
  }

  function logout() {
    token.value = null;
    user.value = null;
    localStorage.removeItem('financebook_token');
    sessionStorage.removeItem('financebook_token');
  }

  async function validateToken(): Promise<boolean> {
    const storedToken = localStorage.getItem('financebook_token') 
      || sessionStorage.getItem('financebook_token');
    
    if (!storedToken) return false;

    try {
      token.value = storedToken;
      user.value = await authApi.getCurrentUser();
      return true;
    } catch {
      logout();
      return false;
    }
  }

  return {
    token,
    user,
    isLoading,
    isAuthenticated,
    login,
    logout,
    validateToken,
  };
});
```

---

### Phase 4: Router Configuration

Create `src/router/index.ts`:

```typescript
import { createRouter, createWebHistory } from 'vue-router';
import { useAuthStore } from '@/stores/auth';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
      meta: { requiresAuth: false },
    },
    {
      path: '/',
      name: 'summary',
      component: () => import('@/views/SummaryView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/add',
      name: 'add-payment',
      component: () => import('@/views/AddPaymentView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/payment/:id/edit',
      name: 'edit-payment',
      component: () => import('@/views/EditPaymentView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/add-success',
      name: 'add-success',
      component: () => import('@/views/AddSuccessView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/categories',
      name: 'categories',
      component: () => import('@/views/CategoriesView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/category-types',
      name: 'category-types',
      component: () => import('@/views/CategoryTypesView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/statistics',
      name: 'statistics',
      component: () => import('@/views/StatisticsView.vue'),
      meta: { requiresAuth: true },
    },
    {
      path: '/404',
      name: 'not-found',
      component: () => import('@/views/NotFoundView.vue'),
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/404',
    },
  ],
});

// Auth guard
router.beforeEach(async (to, from, next) => {
  const authStore = useAuthStore();

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    next('/login');
  } else if (to.name === 'login' && authStore.isAuthenticated) {
    next('/');
  } else {
    next();
  }
});

export default router;
```

---

### Phase 5: Component Migration

#### React to Vue Translation Guide

| React Pattern | Vue Equivalent |
|--------------|----------------|
| `useState()` | `ref()` or `reactive()` |
| `useEffect()` | `onMounted()`, `watch()`, `watchEffect()` |
| `useCallback()` | Define function in `<script setup>` |
| `useMemo()` | `computed()` |
| `useContext()` | Pinia store + `inject()` |
| `React.lazy()` | Route-level code splitting |
| Styled-components | `<style scoped>` in SFC |
| Props | `defineProps<T>()` |
| Events | `defineEmits<T>()` |

---

### Phase 6: Global Styles

Create `src/styles/global.css`:

```css
/* CSS Reset */
*,*::before,*::after {
  box-sizing: border-box;
  margin: 0;
}

html, body, #app {
  height: 100%;
}

body {
  line-height: 1.5;
  -webkit-font-smoothing: antialiased;
  background: var(--color-bg);
  color: var(--color-text-primary);
  font-family: 'Inter', ui-sans-serif, system-ui, sans-serif;
  font-size: 16px;
}

/* Design Tokens */
:root {
  --color-bg: #000000;
  --color-surface: #1c1c1c;
  --color-text-primary: #eaeaea;
  --color-text-secondary: #9e9e9e;
  --color-positive: #2ecc71;
  --color-negative: #e74c3c;
  --radius-lg: 0.75rem;
  --radius-md: 0.5rem;
  --spacing-xs: 0.25rem;
  --spacing-sm: 0.5rem;
  --spacing-md: 1rem;
  --spacing-lg: 1.5rem;
}
```

---

## Vite Configuration

Update `vite.config.ts`:

```typescript
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import path from 'path';

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': path.resolve(__dirname, './src'),
    },
  },
  server: {
    port: 5173,
    proxy: {
      '/api': {
        target: 'http://localhost:8000',
        changeOrigin: true,
        rewrite: (path) => path.replace(/^\/api/, ''),
      },
    },
  },
});
```

---

## Component Migration Examples

### Example: LoginPage → LoginView.vue

**React version** uses Context API and inline styles.
**Vue version** uses Pinia store and scoped styles.

### Example: NavigationBar

React uses styled-components and props.
Vue uses `<style scoped>` and emits.

---

## Testing Strategy

1. **Unit Tests:** Vitest for composables and utilities
2. **Component Tests:** Vitest + Vue Test Utils
3. **E2E Tests:** Playwright or Cypress
4. **Type Safety:** `vue-tsc --noEmit` in CI

---

## Deployment Checklist

- [ ] Build optimized bundle (`npm run build`)
- [ ] Test production build (`npm run preview`)
- [ ] Verify API proxy works in production
- [ ] Check responsive design on mobile
- [ ] Validate all routes work
- [ ] Test authentication flow
- [ ] Verify file uploads work
- [ ] Test chart rendering
- [ ] Check browser compatibility

---

## Next Steps

1. Initialize Vue project with `npm create vue@latest`
2. Install dependencies
3. Set up project structure
4. Configure Vite proxy
5. Implement auth store and login page
6. Port components one by one
7. Test each feature as it's migrated
8. Deploy and verify

---

**Document Status:** Draft v1.0
**Last Updated:** 2026-02-17
**Author:** Migration Team
