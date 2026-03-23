# Upgrade Notes

This project was upgraded in place from a basic Spring Boot + React ration system into a more production-ready system with JWT security, role-based access, admin APIs, and payment-aware orders.

## What Changed

### Backend

- Added JWT authentication with Spring Security
- Added BCrypt password encoding
- Added role-based authorization
- Public endpoints:
  - `POST /api/auth/register`
  - `POST /api/auth/login`
- Secured all other `/api/**` endpoints
- Added admin-only APIs under `/api/admin/**`
- Added product CRUD for admins
- Added payment simulation API
- Added `paymentStatus` and upgraded order flow
- Moved API responses toward DTO-based output

### Frontend

- Added JWT login handling
- Stores JWT token in `localStorage`
- Sends token in Axios `Authorization` header
- Added protected routes
- Added admin-only pages
- Updated dashboard and order flow to use secured backend APIs

## Default Admin

- Ration Card Number: `ADMIN001`
- Password: `admin123`

## Important Backend Files

- `smart-ration-backend/src/main/java/com/smartration/backend/config/SecurityConfig.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/security/JwtAuthenticationFilter.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/security/JwtService.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/controller/AuthController.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/controller/AdminController.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/controller/PaymentController.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/controller/ProductController.java`
- `smart-ration-backend/src/main/java/com/smartration/backend/controller/OrderController.java`

## Important Frontend Files

- `smart-ration-frontend/src/context/AuthContext.jsx`
- `smart-ration-frontend/src/services/api.js`
- `smart-ration-frontend/src/services/authService.js`
- `smart-ration-frontend/src/services/orderService.js`
- `smart-ration-frontend/src/services/paymentService.js`
- `smart-ration-frontend/src/services/productService.js`
- `smart-ration-frontend/src/components/ProtectedRoute.jsx`
- `smart-ration-frontend/src/pages/LoginPage.jsx`
- `smart-ration-frontend/src/pages/DashboardPage.jsx`
- `smart-ration-frontend/src/pages/OrderPage.jsx`
- `smart-ration-frontend/src/pages/AdminProductsPage.jsx`
- `smart-ration-frontend/src/pages/AdminOrdersPage.jsx`

## How To Run

### Backend

```powershell
cd c:\Users\Administrator\OneDrive\Documents\Smart-ration-System\smart-ration-backend
mvn spring-boot:run
```

### Frontend

```powershell
cd c:\Users\Administrator\OneDrive\Documents\Smart-ration-System\smart-ration-frontend
npm install
npm run dev
```

## Authentication Flow

1. User logs in using `/api/auth/login`
2. Backend returns a JWT token
3. Frontend stores token in `localStorage`
4. Axios automatically sends `Authorization: Bearer <token>`
5. Backend JWT filter validates token on secured requests

## Role Rules

- `ROLE_USER`
  - can access normal user APIs
  - can browse products
  - can place orders
  - can make payments

- `ROLE_ADMIN`
  - can access admin product management
  - can access admin order management

## Order and Payment Flow

1. User logs in
2. User selects products
3. Frontend calls `POST /api/orders`
4. Order is created with:
   - `status = PENDING_PAYMENT`
   - `paymentStatus = PENDING`
5. Frontend calls `POST /api/payments/{orderId}`
6. Backend marks payment as successful
7. Order becomes `CONFIRMED`
8. Product stock is reduced after payment

## APIs Added or Updated

### Auth

- `POST /api/auth/register`
- `POST /api/auth/login`

### User

- `GET /api/users/{id}`

### Products

- `GET /api/products`

### Orders

- `POST /api/orders`
- `GET /api/orders/user/{id}`

### Payments

- `POST /api/payments/{orderId}`

### Admin

- `GET /api/admin/products`
- `POST /api/admin/products`
- `PUT /api/admin/products/{id}`
- `DELETE /api/admin/products/{id}`
- `GET /api/admin/orders`

## Compatibility Note

To avoid breaking older database rows, legacy order status values like `PLACED` are still accepted in the enum.

## Verification Completed

- Backend compile passed
- Frontend build passed
- JWT login verified
- Anonymous secured access blocked
- Role-based authorization verified
- Admin API access verified
- Order and payment flow verified
