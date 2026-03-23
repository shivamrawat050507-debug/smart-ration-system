# Smart Ration Distribution System

## 1. Product Goal

The Smart Ration Distribution System digitizes key Public Distribution System (PDS) operations to reduce queues, improve transparency, prevent duplicate claims, and make ration distribution easier for citizens, shopkeepers, and administrators.

This design assumes:

- Mock data only for Aadhaar, ration card, OTP, and notifications
- Multi-role web app: citizen, shopkeeper, admin
- Future scalability across multiple ration shops and districts

## 2. High-Level Architecture

### Core Components

1. React frontend
   - Citizen portal
   - Shopkeeper portal
   - Admin dashboard
2. Spring Boot backend
   - REST APIs
   - Authentication and authorization
   - Booking, token, quota, stock, and analytics modules
3. MySQL database
   - Stores users, shops, bookings, stocks, claims, and audit data
4. Supporting services
   - OTP service abstraction
   - Notification service abstraction
   - QR/token generation service
   - Fraud detection rules engine

### Suggested Deployment Shape

- Frontend: React SPA served separately or via Nginx
- Backend: Spring Boot monolith with modular package structure
- Database: MySQL
- Optional cache later: Redis for OTP/session/slot locking

### Why Start with a Modular Monolith

For a student or early-stage project, a modular monolith is better than microservices because it is:

- Faster to build
- Easier to test locally
- Easier to deploy
- Still scalable if modules are cleanly separated

Suggested backend modules:

- auth
- users
- shops
- inventory
- quotas
- booking
- token
- distribution
- notifications
- analytics
- fraud
- admin

## 3. User Roles and Permissions

### Citizen

- Register with mock Aadhaar/ration card
- Log in with password or OTP
- View eligibility and monthly quota
- Book a slot
- View/download QR token
- View booking history and claim status

### Shopkeeper

- Log in
- View daily slot bookings
- Verify user token via QR
- Mark ration as collected
- Update stock
- View low-stock alerts

### Admin

- Manage users
- Manage shopkeepers
- Manage ration shops
- Monitor stock and distributions
- View fraud alerts and duplicate claims
- View dashboards and reports

## 4. Core Domain Flow

### Citizen Booking Flow

1. User registers using mock Aadhaar/ration card details
2. System validates whether card exists in mock registry
3. User logs in
4. User checks monthly entitlement and remaining balance
5. User selects shop and available slot
6. System reserves slot if capacity remains
7. System generates booking and QR token
8. Notification is sent
9. User visits shop and presents QR token
10. Shopkeeper scans token and confirms distribution
11. Stock is reduced and monthly quota usage is updated

### Fraud Prevention Flow

1. Token scanned
2. Backend verifies:
   - token authenticity
   - booking status
   - slot date validity
   - whether quota already claimed
3. If duplicate or suspicious claim occurs, flag for admin review

## 5. Recommended Backend Package Structure

```text
src/main/java/com/smartration
  ├─ auth
  ├─ common
  ├─ config
  ├─ users
  ├─ shops
  ├─ inventory
  ├─ quota
  ├─ booking
  ├─ token
  ├─ distribution
  ├─ notification
  ├─ analytics
  ├─ fraud
  └─ admin
```

Each module should ideally contain:

- controller
- service
- repository
- entity
- dto
- mapper

## 6. Database Schema

### 6.1 `users`

Stores citizen accounts.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| full_name | VARCHAR(120) | |
| mobile | VARCHAR(15) UNIQUE | |
| email | VARCHAR(120) NULL | |
| password_hash | VARCHAR(255) NULL | Nullable if OTP-only login |
| aadhaar_no | VARCHAR(20) UNIQUE | Mock only |
| ration_card_no | VARCHAR(30) UNIQUE | |
| family_size | INT | |
| category | VARCHAR(20) | AAY / PHH / APL etc. |
| district | VARCHAR(80) | |
| address | VARCHAR(255) | |
| preferred_shop_id | BIGINT FK | Nullable |
| status | VARCHAR(20) | ACTIVE / BLOCKED |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### 6.2 `shopkeepers`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| full_name | VARCHAR(120) | |
| mobile | VARCHAR(15) UNIQUE | |
| email | VARCHAR(120) NULL | |
| password_hash | VARCHAR(255) | |
| shop_id | BIGINT FK UNIQUE | One active shopkeeper per shop initially |
| status | VARCHAR(20) | ACTIVE / INACTIVE |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### 6.3 `admins`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| full_name | VARCHAR(120) | |
| email | VARCHAR(120) UNIQUE | |
| password_hash | VARCHAR(255) | |
| role | VARCHAR(30) | SUPER_ADMIN / DISTRICT_ADMIN |
| status | VARCHAR(20) | |
| created_at | DATETIME | |

### 6.4 `ration_shops`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| shop_code | VARCHAR(30) UNIQUE | |
| shop_name | VARCHAR(120) | |
| dealer_name | VARCHAR(120) | |
| district | VARCHAR(80) | |
| address | VARCHAR(255) | |
| latitude | DECIMAL(10,7) NULL | Optional |
| longitude | DECIMAL(10,7) NULL | Optional |
| daily_capacity | INT | Total people per day |
| slot_capacity | INT | People per slot |
| opening_time | TIME | |
| closing_time | TIME | |
| active | BOOLEAN | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

### 6.5 `ration_items`

Master table for items.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| item_code | VARCHAR(20) UNIQUE | e.g. RICE, WHEAT, SUGAR |
| item_name | VARCHAR(80) | |
| unit | VARCHAR(20) | KG / LTR / PACKET |
| active | BOOLEAN | |

### 6.6 `quota_policies`

Defines entitlement per category.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| category | VARCHAR(20) | |
| item_id | BIGINT FK | |
| quantity_per_member | DECIMAL(10,2) NULL | |
| flat_quantity | DECIMAL(10,2) NULL | |
| effective_from | DATE | |
| effective_to | DATE NULL | |

Use either `quantity_per_member` or `flat_quantity`.

### 6.7 `monthly_user_quotas`

Snapshot for each month so policy changes do not break history.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK | |
| year_month | VARCHAR(7) | Format `YYYY-MM` |
| item_id | BIGINT FK | |
| entitled_qty | DECIMAL(10,2) | |
| claimed_qty | DECIMAL(10,2) | |
| remaining_qty | DECIMAL(10,2) | |
| status | VARCHAR(20) | OPEN / CLOSED |
| created_at | DATETIME | |

Unique key: `user_id + year_month + item_id`

### 6.8 `shop_inventory`

Current stock at each shop.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| shop_id | BIGINT FK | |
| item_id | BIGINT FK | |
| available_qty | DECIMAL(10,2) | |
| reorder_level | DECIMAL(10,2) | |
| last_updated_at | DATETIME | |

Unique key: `shop_id + item_id`

### 6.9 `inventory_transactions`

Tracks stock changes.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| shop_id | BIGINT FK | |
| item_id | BIGINT FK | |
| txn_type | VARCHAR(20) | INWARD / DISTRIBUTION / ADJUSTMENT |
| quantity | DECIMAL(10,2) | |
| reference_type | VARCHAR(30) | BOOKING / MANUAL / RESTOCK |
| reference_id | BIGINT NULL | |
| remarks | VARCHAR(255) NULL | |
| created_by_role | VARCHAR(20) | SHOPKEEPER / ADMIN / SYSTEM |
| created_by_id | BIGINT | |
| created_at | DATETIME | |

### 6.10 `slot_templates`

Defines recurring slot timings per shop.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| shop_id | BIGINT FK | |
| start_time | TIME | |
| end_time | TIME | |
| max_tokens | INT | |
| active | BOOLEAN | |

### 6.11 `slot_availability`

Stores date-wise capacity for booking.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| shop_id | BIGINT FK | |
| slot_template_id | BIGINT FK | |
| slot_date | DATE | |
| booked_count | INT | |
| max_count | INT | |
| status | VARCHAR(20) | OPEN / FULL / CLOSED |

Unique key: `shop_id + slot_template_id + slot_date`

### 6.12 `bookings`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| booking_no | VARCHAR(40) UNIQUE | |
| user_id | BIGINT FK | |
| shop_id | BIGINT FK | |
| slot_availability_id | BIGINT FK | |
| booking_date | DATE | |
| slot_start_time | TIME | |
| slot_end_time | TIME | |
| status | VARCHAR(20) | BOOKED / CANCELLED / COMPLETED / EXPIRED |
| token_id | BIGINT FK NULL | |
| notification_sent | BOOLEAN | |
| created_at | DATETIME | |
| updated_at | DATETIME | |

Constraint idea:

- One active booking per user per distribution cycle, if business rules require it

### 6.13 `digital_tokens`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| booking_id | BIGINT FK UNIQUE | |
| qr_code_value | VARCHAR(255) UNIQUE | Signed token or UUID |
| otp_code | VARCHAR(10) NULL | Optional backup verification |
| expires_at | DATETIME | |
| scanned_at | DATETIME NULL | |
| status | VARCHAR(20) | ACTIVE / USED / EXPIRED / CANCELLED |
| created_at | DATETIME | |

### 6.14 `distribution_records`

Proof of actual ration issue.

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| booking_id | BIGINT FK UNIQUE | |
| user_id | BIGINT FK | |
| shop_id | BIGINT FK | |
| distributed_at | DATETIME | |
| verified_by_shopkeeper_id | BIGINT FK | |
| remarks | VARCHAR(255) NULL | |

### 6.15 `distribution_record_items`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| distribution_record_id | BIGINT FK | |
| item_id | BIGINT FK | |
| quantity | DECIMAL(10,2) | |

### 6.16 `otp_requests`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| mobile | VARCHAR(15) | |
| purpose | VARCHAR(30) | LOGIN / REGISTER / VERIFY |
| otp_code | VARCHAR(10) | Mock only |
| expires_at | DATETIME | |
| verified | BOOLEAN | |
| created_at | DATETIME | |

### 6.17 `notifications`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| user_id | BIGINT FK NULL | |
| recipient_mobile | VARCHAR(15) NULL | |
| recipient_email | VARCHAR(120) NULL | |
| channel | VARCHAR(20) | SMS / EMAIL / IN_APP |
| template_code | VARCHAR(50) | |
| payload_json | JSON | |
| status | VARCHAR(20) | PENDING / SENT / FAILED |
| created_at | DATETIME | |
| sent_at | DATETIME NULL | |

### 6.18 `fraud_alerts`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| alert_type | VARCHAR(40) | DUPLICATE_CLAIM / TOKEN_REUSE / STOCK_ANOMALY |
| severity | VARCHAR(20) | LOW / MEDIUM / HIGH |
| user_id | BIGINT FK NULL | |
| shop_id | BIGINT FK NULL | |
| booking_id | BIGINT FK NULL | |
| description | VARCHAR(255) | |
| status | VARCHAR(20) | OPEN / REVIEWED / RESOLVED |
| created_at | DATETIME | |
| resolved_at | DATETIME NULL | |

### 6.19 `audit_logs`

| Column | Type | Notes |
|---|---|---|
| id | BIGINT PK | |
| actor_role | VARCHAR(20) | |
| actor_id | BIGINT | |
| action | VARCHAR(80) | |
| entity_type | VARCHAR(50) | |
| entity_id | BIGINT | |
| old_value_json | JSON NULL | |
| new_value_json | JSON NULL | |
| created_at | DATETIME | |

## 7. Key Entity Relationships

- One user belongs to one preferred ration shop, but can optionally book another if business rules allow
- One ration shop has many slot templates
- One slot template creates many daily slot availability records
- One booking belongs to one user and one shop
- One booking has one digital token
- One completed booking has one distribution record
- One distribution record has many distribution record items
- One shop has many inventory entries and inventory transactions

## 8. Backend API Design

Recommended base path:

`/api/v1`

### 8.1 Authentication APIs

#### `POST /auth/register`

Registers a citizen.

Request:

```json
{
  "fullName": "Ravi Kumar",
  "mobile": "9876543210",
  "email": "ravi@example.com",
  "aadhaarNo": "123412341234",
  "rationCardNo": "RC100200",
  "password": "Test@123",
  "district": "Hyderabad",
  "address": "House 12, Ward 5"
}
```

Response:

```json
{
  "message": "Registration successful",
  "userId": 101
}
```

#### `POST /auth/login/password`

#### `POST /auth/login/send-otp`

#### `POST /auth/login/verify-otp`

#### `POST /auth/logout`

#### `GET /auth/me`

Returns current profile and role.

### 8.2 Citizen APIs

#### `GET /users/me/profile`

#### `GET /users/me/eligibility`

Response idea:

```json
{
  "category": "PHH",
  "familySize": 4,
  "shop": {
    "id": 3,
    "name": "Ration Shop - Ward 12"
  },
  "quota": [
    {
      "item": "Rice",
      "entitledQty": 20,
      "claimedQty": 5,
      "remainingQty": 15,
      "unit": "KG"
    }
  ]
}
```

#### `GET /users/me/bookings`

#### `GET /users/me/bookings/{bookingId}`

#### `GET /users/me/token/{bookingId}`

#### `GET /users/me/notifications`

### 8.3 Slot Booking APIs

#### `GET /shops`

Filters:

- district
- pincode
- nearLat
- nearLng

#### `GET /shops/{shopId}/slots?date=2026-03-25`

Response:

```json
{
  "shopId": 3,
  "date": "2026-03-25",
  "slots": [
    {
      "slotAvailabilityId": 55,
      "startTime": "10:00",
      "endTime": "10:30",
      "available": 8,
      "max": 15,
      "status": "OPEN"
    }
  ]
}
```

#### `POST /bookings`

Request:

```json
{
  "shopId": 3,
  "slotAvailabilityId": 55
}
```

Business validations:

- user must be active
- quota must still be available
- slot must be open
- user must not have an overlapping active booking
- shop inventory should be sufficient for expected issue

#### `PUT /bookings/{bookingId}/cancel`

### 8.4 Token Verification APIs

#### `POST /tokens/verify`

Request:

```json
{
  "qrCodeValue": "7d6d78d2-b9a3-4cb1-8ef3-1f8fc6d5a111"
}
```

Response:

```json
{
  "valid": true,
  "bookingId": 890,
  "user": {
    "name": "Ravi Kumar",
    "rationCardNo": "RC100200"
  },
  "shop": {
    "id": 3,
    "name": "Ration Shop - Ward 12"
  },
  "status": "BOOKED"
}
```

#### `POST /distribution/confirm`

Request:

```json
{
  "bookingId": 890,
  "items": [
    {
      "itemId": 1,
      "quantity": 10
    },
    {
      "itemId": 2,
      "quantity": 2
    }
  ]
}
```

Effects:

- marks token as used
- marks booking as completed
- creates distribution record
- reduces stock
- updates monthly user quota
- creates audit log

### 8.5 Shopkeeper APIs

#### `POST /shopkeeper/login`

Or use shared auth with role-based access.

#### `GET /shopkeeper/bookings/today`

#### `GET /shopkeeper/bookings?date=2026-03-25`

#### `GET /shopkeeper/inventory`

#### `PUT /shopkeeper/inventory/{itemId}`

#### `GET /shopkeeper/alerts/low-stock`

### 8.6 Admin APIs

#### `GET /admin/dashboard/summary`

Metrics:

- total users
- total shops
- total bookings today
- completed distributions
- low stock shops
- fraud alerts open

#### `GET /admin/users`

#### `PUT /admin/users/{id}/status`

#### `GET /admin/shopkeepers`

#### `POST /admin/shopkeepers`

#### `GET /admin/shops`

#### `POST /admin/shops`

#### `PUT /admin/shops/{id}`

#### `GET /admin/inventory/overview`

#### `GET /admin/fraud-alerts`

#### `PUT /admin/fraud-alerts/{id}/resolve`

#### `GET /admin/analytics/distribution-trends`

#### `GET /admin/analytics/shop-performance`

## 9. Security and Authentication Design

### Recommended Approach

- Spring Security with JWT authentication
- Role-based access control using roles:
  - ROLE_USER
  - ROLE_SHOPKEEPER
  - ROLE_ADMIN
- BCrypt for password hashing
- Mock OTP provider for development

### OTP Flow

1. User enters mobile number
2. Backend creates OTP entry
3. Mock service logs or stores OTP
4. User submits OTP
5. Backend validates and issues JWT

For demo projects, OTP can be:

- stored in `otp_requests`
- shown in console logs
- optionally surfaced in development-only admin/debug panel

### QR Token Design

Use either:

1. UUID mapped to backend token record
2. Signed JWT-like QR payload with booking ID and expiry

Best option for this project:

- Store UUID in DB
- Generate QR image on backend or frontend using returned string
- Verify server-side on scan

## 10. Concurrency and Booking Rules

Slot booking must avoid overbooking.

### Recommended Strategy

- Use transaction boundaries in Spring service layer
- Lock `slot_availability` row during booking
- Re-check `booked_count < max_count`
- Increment atomically

Pseudo-flow:

1. Start transaction
2. Select slot row for update
3. Check capacity
4. Create booking
5. Increment booked count
6. Generate token
7. Commit

This is important to prevent race conditions when many users book the same slot.

## 11. Fraud Detection Rules

Start simple with rule-based detection:

- Same user claiming more than once in the same month
- Same token scanned multiple times
- Claims attempted at wrong shop
- Booking completed outside booked date/slot
- Stock deducted without matching distribution record
- Unusual inventory adjustments by shopkeeper

Later, show these in admin dashboard as flagged events.

## 12. Frontend UI Ideas

Use React with:

- React Router
- Axios
- Material UI, Ant Design, or Tailwind + component library
- React Query for API state
- Formik/React Hook Form for forms

### 12.1 Citizen Screens

#### Landing Page

- Project intro
- Benefits of smart ration booking
- Login and register CTA
- Optional FAQ

#### Registration Page

- Full name
- Mobile
- Aadhaar number
- Ration card number
- Password setup
- Address and district

#### Login Page

Two tabs:

- Password login
- OTP login

#### User Dashboard

Cards for:

- Monthly quota
- Remaining quota
- Next booking
- Preferred ration shop

Sections:

- Eligibility details
- Notifications
- Recent activity

#### Slot Booking Page

- Shop selector
- Date picker
- Slot cards with capacity indicator
- Confirm booking modal

#### Digital Token Page

- QR code
- Booking number
- Shop address
- Slot date/time
- Status badge

#### Booking History Page

- Upcoming bookings
- Completed collections
- Cancelled bookings

### 12.2 Shopkeeper Screens

#### Shopkeeper Dashboard

- Today’s bookings count
- Completed distributions
- Pending verifications
- Low stock alerts

#### Booking Verification Screen

- QR scan input or camera scanner
- Token status result
- User details
- Item issue confirmation form

#### Inventory Screen

- Current stock table
- Add stock / adjust stock action
- Low stock indicators

### 12.3 Admin Screens

#### Admin Dashboard

- Summary KPI cards
- Booking trend chart
- Distribution chart
- Low stock shops list
- Fraud alerts table

#### User Management

- Search by ration card/mobile
- View profile
- Block/unblock user

#### Shop Management

- Create/update shops
- Assign shopkeepers
- View shop stock and daily usage

#### Analytics

- Daily distributions
- Shopwise performance
- Item consumption trends
- Fraud alert trends

## 13. Suggested UI/UX Principles

- Keep citizen flow simple and mobile-first
- Use large buttons and clear status labels
- Show slot availability visually with green/yellow/red indicators
- Make booking confirmation prominent
- Keep QR token accessible offline if possible
- Use local language support later if needed

## 14. Step-by-Step Implementation Plan

### Phase 1: Project Setup

1. Initialize backend with Spring Boot
   - Spring Web
   - Spring Security
   - Spring Data JPA
   - MySQL Driver
   - Validation
   - Lombok
2. Initialize frontend with React
3. Configure MySQL database
4. Set up base folder structure
5. Create environment configs

### Phase 2: Core Database and Authentication

1. Create initial schema using Flyway or Liquibase
2. Seed master data:
   - ration items
   - sample shops
   - sample quota policies
   - sample users
3. Build registration API
4. Build password login
5. Build mock OTP login
6. Add JWT authentication and role guards
7. Create frontend login/register pages

### Phase 3: User Eligibility and Quota

1. Implement quota calculation logic
2. Create monthly quota snapshot generation
3. Build eligibility API
4. Show quota dashboard on frontend

### Phase 4: Slot Booking

1. Create slot template and slot availability tables
2. Build slot listing API
3. Build booking API with transaction locking
4. Generate booking number and token
5. Send mock notification
6. Create booking UI and confirmation screen

### Phase 5: QR Token and Distribution

1. Add QR generation
2. Build token verification API
3. Build distribution confirmation API
4. Update inventory and quota on successful collection
5. Create shopkeeper verification UI

### Phase 6: Inventory Management

1. Build inventory APIs
2. Track stock transactions
3. Show low stock alerts
4. Add shopkeeper inventory update screen

### Phase 7: Admin Dashboard

1. Build dashboard summary APIs
2. Build user/shop/shopkeeper management APIs
3. Build fraud alerts module
4. Create analytics charts on frontend

### Phase 8: Hardening and Polish

1. Add audit logging
2. Add form validation and error handling
3. Add pagination/filtering
4. Add test coverage
5. Improve responsiveness and UX
6. Add location-based shop suggestion if desired

## 15. Suggested Sprint Order

If you want to build this quickly, build in this order:

1. Auth + roles
2. Users + quota view
3. Shops + slot availability
4. Booking + QR token
5. Shopkeeper verification
6. Inventory updates
7. Admin dashboard
8. Fraud rules + notifications

## 16. Example Technology Choices

### Backend

- Java 17
- Spring Boot 3
- Spring Security
- Spring Data JPA
- MySQL 8
- Flyway
- MapStruct or manual mappers

### Frontend

- React 18
- React Router
- React Query
- Axios
- Material UI or Tailwind CSS
- QR code library such as `qrcode.react`

### Testing

- Backend: JUnit, Mockito, Testcontainers
- Frontend: React Testing Library

## 17. MVP Scope Recommendation

To avoid overbuilding, define an MVP:

- User registration and login
- View monthly quota
- Book slot
- Generate QR token
- Shopkeeper verifies and completes distribution
- Admin can view dashboard summary
- Inventory update with low-stock alerts

Leave these for later:

- Real SMS integration
- Real Aadhaar integration
- GIS-based distance calculation
- Advanced ML fraud detection
- Multilingual support

## 18. Future Enhancements

- Android app for shopkeepers
- Offline scan mode with later sync
- District-level analytics
- Beneficiary family-member management
- E-KYC integration
- Biometric verification simulation
- Notification center with WhatsApp/SMS integration

## 19. Recommended Next Build Step

Start by implementing:

1. backend auth module
2. user and ration shop schema
3. quota calculation
4. slot booking APIs
5. citizen dashboard UI

That sequence gives you a functional demo quickly and sets up the rest of the system cleanly.
