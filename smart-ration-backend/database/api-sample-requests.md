# Smart Ration Backend API Sample Requests

## 1. Register User

**POST** `/api/users/register`

```json
{
  "name": "Ravi Kumar",
  "rationCardNumber": "RC10001",
  "phone": "9876543210",
  "password": "user123"
}
```

## 2. Login User

**POST** `/api/users/login`

```json
{
  "rationCardNumber": "RC10001",
  "password": "user123"
}
```

## 3. Get User Details

**GET** `/api/users/1`

## 4. Book Ration Slot

**POST** `/api/bookings`

```json
{
  "userId": 1,
  "date": "2026-03-25",
  "time": "10:30:00"
}
```

## 5. Get All Bookings For A User

**GET** `/api/bookings/user/1`

## 6. Shopkeeper Login

**POST** `/api/shopkeepers/login`

```json
{
  "username": "shopkeeper1",
  "password": "password123"
}
```

## 7. View Daily Bookings

**GET** `/api/shopkeepers/bookings?date=2026-03-25`

## 8. Verify User / Mark Ration As Collected

**PATCH** `/api/shopkeepers/bookings/1/verify`

## 9. Add Inventory Stock

**POST** `/api/inventory`

```json
{
  "rice": 100,
  "wheat": 80,
  "sugar": 50
}
```

## 10. Update Inventory Stock

**PUT** `/api/inventory/1`

```json
{
  "rice": 120,
  "wheat": 90,
  "sugar": 60
}
```

## 11. Get Current Stock

**GET** `/api/inventory`
