# Smart Digital Ration Distribution System

State, city, and depot-aware ration distribution prototype built with React, Spring Boot, and MySQL.

## What This Version Demonstrates

- Centralized dashboard for beneficiary, dealer, and admin personas
- State -> city -> depot -> ration card hierarchy
- Dynamic family-size based entitlement preview
- Depot stock monitoring with partial distribution and pending balance handling
- City-wise stock requirement insights for administration
- Fraud controls such as QR validation, depot restriction, and monthly claim limits

## Demo Logins

- Beneficiary: `HR-GRG-4421` / `password123`
- Dealer: `DLR-GRG-014` / `password123`
- Admin: `ADMIN001` / `admin123`

## Key API Added

- `GET /api/dashboard/overview/{userId}` returns the state/city/depot overview used by the new frontend

## Local Connection Setup

1. Start MySQL and make sure `smart_ration_db` is available.
2. Run the Spring Boot backend on `http://localhost:8080`.
3. Copy `smart-ration-frontend/.env.example` to `smart-ration-frontend/.env` if you want to override the API URL.
4. Run the React frontend on `http://localhost:5173`.

The frontend now uses `VITE_API_BASE_URL` and the backend CORS policy allows local Vite development from both `localhost:5173` and `127.0.0.1:5173`.

## Project Structure

- [Backend](C:/Users/Administrator/OneDrive/Documents/Smart-ration-System/smart-ration-backend)
- [Frontend](C:/Users/Administrator/OneDrive/Documents/Smart-ration-System/smart-ration-frontend)
