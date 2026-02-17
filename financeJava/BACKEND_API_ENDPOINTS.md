# FinanceBook Backend API Endpoints

**Base URL**: `http://localhost:8080`  
**Authentication**: JWT Bearer Token (except /auth/login and /auth/register)

---

## 🔐 Authentication Endpoints

### POST /auth/login
Authenticate user and receive JWT token.

**Request Body**:
```json
{
  "username": "admin",
  "password": "admin"
}
```

**Response**:
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "tokenType": "bearer"
}
```

**Status Codes**:
- `200 OK` - Authentication successful
- `401 Unauthorized` - Invalid credentials
- `403 Forbidden` - Account deactivated

---

### POST /auth/register
Register a new user account.

**Request Body**:
```json
{
  "username": "newuser",
  "password": "password123",
  "surname": "Doe",
  "prename": "John",
  "birthDate": "1990-01-15",
  "phone": "+49 123 456789",
  "road": "Main Street",
  "houseNumber": "42",
  "postal": "12345",
  "city": "Berlin",
  "state": "Berlin"
}
```

**Response**: UserReadResponse (see GET /auth/me)

**Status Codes**:
- `200 OK` - Registration successful
- `400 Bad Request` - Validation errors or duplicate username

**Notes**:
- Automatically creates "standard" category type and "UNCLASSIFIED" category for new user
- Password must be at least 6 characters
- Username is normalized (whitespace collapsed)

---

### GET /auth/me
Get current authenticated user's profile.

**Headers**: `Authorization: Bearer <token>`

**Response**:
```json
{
  "id": 1,
  "username": "admin",
  "surname": "Administrator",
  "prename": "System",
  "birthDate": null,
  "phone": null,
  "road": null,
  "houseNumber": null,
  "region": null,
  "postal": null,
  "city": null,
  "state": null,
  "isAdmin": true,
  "isActive": true,
  "createdAt": "2026-02-16T18:00:00"
}
```

---

### PUT /auth/me
Update current user's profile.

**Headers**: `Authorization: Bearer <token>`

**Request Body** (all fields optional):
```json
{
  "surname": "NewSurname",
  "prename": "NewPrename",
  "password": "newpassword123",
  "phone": "+49 987 654321",
  "city": "Munich"
}
```

**Response**: UserReadResponse

---

## 💰 Payment Item Endpoints

### POST /payment-items
Create a new payment item.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "amount": 150.50,
  "date": "2026-02-16T14:30:00",
  "periodic": false,
  "description": "Grocery shopping",
  "recipientId": 5,
  "categoryIds": [10, 12]
}
```

**Response**:
```json
{
  "id": 123,
  "amount": 150.49,
  "date": "2026-02-16T14:30:00",
  "periodic": false,
  "description": "Grocery shopping",
  "recipient": {
    "id": 5,
    "name": "Supermarket",
    "address": "Main St 10"
  },
  "categories": [...],
  "standardCategory": {...},
  "transactionFee": 0.01
}
```

**Notes**:
- Transaction fee is automatically computed and applied
- If no categoryIds provided, "UNCLASSIFIED" is assigned
- Only one category per type allowed
- Negative amount = expense, positive = income

---

### GET /payment-items
List all payment items with optional filtering.

**Headers**: `Authorization: Bearer <token>`

**Query Parameters**:
- `expenseOnly` (boolean, optional) - Filter for expenses only (amount < 0)
- `incomeOnly` (boolean, optional) - Filter for incomes only (amount >= 0)
- `categoryIds` (array of longs, optional) - Filter by categories (OR logic, includes descendants)

**Examples**:
```
GET /payment-items
GET /payment-items?expenseOnly=true
GET /payment-items?categoryIds=10&categoryIds=12
```

**Response**: Array of PaymentItemReadResponse

**Notes**:
- Category filtering automatically expands to include all descendant categories
- Results ordered by date descending

---

### GET /payment-items/{itemId}
Get a single payment item by ID.

**Headers**: `Authorization: Bearer <token>`

**Response**: PaymentItemReadResponse

**Status Codes**:
- `200 OK` - Item found
- `404 Not Found` - Item doesn't exist
- `403 Forbidden` - Item belongs to another user

---

### PUT /payment-items/{itemId}
Update an existing payment item.

**Headers**: `Authorization: Bearer <token>`

**Request Body** (all fields optional):
```json
{
  "amount": 175.00,
  "description": "Updated description",
  "categoryIds": [11]
}
```

**Response**: PaymentItemReadResponse

**Notes**:
- If amount is changed, transaction fee is recomputed
- Category links are replaced with new selection

---

### DELETE /payment-items/{itemId}
Delete a payment item.

**Headers**: `Authorization: Bearer <token>`

**Response**: `204 No Content`

**Notes**:
- Associated invoice file is deleted from disk
- Transaction fee is refunded
- Category links are removed automatically

---

## 📁 Category Endpoints

### POST /categories
Create a new category.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "name": "Food",
  "typeId": 1,
  "parentId": null,
  "iconFile": "food-icon.png"
}
```

**Response**: CategoryReadResponse

---

### GET /categories
List all categories for the current user.

**Headers**: `Authorization: Bearer <token>`

**Response**: Array of CategoryReadResponse

---

### GET /categories/{categoryId}
Get a single category by ID.

**Headers**: `Authorization: Bearer <token>`

**Response**: CategoryReadResponse

---

### GET /categories/{categoryId}/tree
Get category with its full tree structure.

**Headers**: `Authorization: Bearer <token>`

**Response**: CategoryReadResponse (with nested children)

---

### GET /categories/{categoryId}/descendants
Get all descendant categories (recursive).

**Headers**: `Authorization: Bearer <token>`

**Response**: Array of CategoryReadResponse

---

### GET /categories/by-type/{typeId}
Get all categories of a specific type.

**Headers**: `Authorization: Bearer <token>`

**Response**: Array of CategoryReadResponse

---

### PUT /categories/{categoryId}
Update an existing category.

**Headers**: `Authorization: Bearer <token>`

**Request Body** (all fields optional):
```json
{
  "name": "Updated Name",
  "parentId": 5,
  "iconFile": "new-icon.png"
}
```

**Response**: CategoryReadResponse

---

## 🏷️ Category Type Endpoints

### POST /category-types
Create a new category type.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "name": "Payment Method",
  "description": "How the payment was made"
}
```

**Response**: CategoryTypeReadResponse

---

### GET /category-types
List all category types for the current user.

**Headers**: `Authorization: Bearer <token>`

**Response**: Array of CategoryTypeReadResponse

---

## 👤 Recipient Endpoints

### POST /recipients
Create a new recipient.

**Headers**: `Authorization: Bearer <token>`

**Request Body**:
```json
{
  "name": "Coffee Shop",
  "address": "Downtown St. 15"
}
```

**Response**: RecipientReadResponse

**Notes**:
- Name is normalized (whitespace collapsed)
- Name must be unique per user

---

### GET /recipients
List all recipients for the current user.

**Headers**: `Authorization: Bearer <token>`

**Response**: Array of RecipientReadResponse

---

### GET /recipients/{recipientId}
Get a single recipient by ID.

**Headers**: `Authorization: Bearer <token>`

**Response**: RecipientReadResponse

---

### PUT /recipients/{recipientId}
Update an existing recipient.

**Headers**: `Authorization: Bearer <token>`

**Request Body** (all fields optional):
```json
{
  "name": "Updated Name",
  "address": "New Address 123"
}
```

**Response**: RecipientReadResponse

---

## 📎 File Upload Endpoints

### POST /uploadicon
Upload a category icon file.

**Headers**: `Authorization: Bearer <token>`

**Request**: `multipart/form-data` with `file` field

**Allowed Types**: PNG, JPEG, GIF, BMP, SVG

**Response**:
```json
{
  "filename": "icon.png"
}
```

---

### GET /download_static/{filename}
Download a category icon file.

**Response**: Icon file (image)

---

### POST /upload-invoice/{paymentItemId}
Upload an invoice file for a payment item.

**Headers**: `Authorization: Bearer <token>`

**Request**: `multipart/form-data` with `file` field

**Allowed Types**: PDF, DOCX, DOC, JPEG, PNG, GIF, BMP, TIFF

**Max Size**: 25MB

**Response**:
```json
{
  "message": "Invoice uploaded successfully",
  "filename": "123_abc123.pdf",
  "payment_item_id": 123
}
```

**Notes**:
- Replaces existing invoice if one exists
- Filename is unique (paymentItemId_UUID.ext)

---

### GET /download-invoice/{paymentItemId}
Download the invoice file for a payment item.

**Headers**: `Authorization: Bearer <token>`

**Response**: Invoice file (PDF/image/document)

---

### DELETE /invoice/{paymentItemId}
Delete the invoice file for a payment item.

**Headers**: `Authorization: Bearer <token>`

**Response**:
```json
{
  "message": "Invoice deleted successfully"
}
```

---

## 🔧 API Documentation

**Swagger UI**: `http://localhost:8080/docs`  
**OpenAPI JSON**: `http://localhost:8080/api-docs`

---

## 🛡️ Security

### Authentication
All endpoints except `/auth/login` and `/auth/register` require JWT authentication.

**Header Format**:
```
Authorization: Bearer <your-jwt-token>
```

### JWT Token Details
- **Algorithm**: HS256
- **Expiration**: 30 minutes (1800000 ms)
- **Claim**: `sub` = username

### Multi-User Isolation
- All data queries are scoped to the authenticated user
- Cross-entity ownership validation enforced
- Users cannot see or modify other users' data

---

## ⚠️ Error Responses

All errors return a consistent format:

```json
{
  "status": 400,
  "message": "Validation error message",
  "timestamp": "2026-02-16T18:00:00"
}
```

**Common Status Codes**:
- `400 Bad Request` - Validation errors
- `401 Unauthorized` - Invalid or missing JWT token
- `403 Forbidden` - Insufficient permissions or account deactivated
- `404 Not Found` - Resource doesn't exist
- `500 Internal Server Error` - Server error

---

## 📊 Implementation Status

✅ **Authentication**: 4/4 endpoints  
✅ **Payment Items**: 5/5 endpoints  
✅ **Categories**: 7/7 endpoints  
✅ **Category Types**: 2/2 endpoints  
✅ **Recipients**: 4/4 endpoints  
✅ **File Uploads**: 5/5 endpoints  
⏳ **Admin API**: 0/4 endpoints (pending)  
⏳ **CSV Import/Export**: 0/2 endpoints (pending)

**Total Implemented**: 27/29 core endpoints (93%)

**Missing**:
- CSV import/export (2 endpoints)
- Admin user management API (4 endpoints)
- Admin web panel (Thymeleaf templates)

---

**Last Updated**: 2026-02-16  
**API Version**: 0.1.0
