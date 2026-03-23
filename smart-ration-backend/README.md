# Smart Ration Distribution System Backend

This is a beginner-friendly Spring Boot backend project for a Smart Ration Distribution System.

## Tech Stack

- Java 17
- Spring Boot
- Maven
- Spring Data JPA / Hibernate
- MySQL
- Lombok

## Features

- User registration
- User login
- Get user details
- Book ration slot
- Get bookings for a user
- Shopkeeper login
- View daily bookings
- Verify ration collection
- Add inventory stock
- Update inventory stock
- Get current stock

## Project Structure

```text
smart-ration-backend/
|-- src/main/java/com/smartration/backend/
|   |-- config
|   |-- controller
|   |-- dto
|   |-- entity
|   |-- exception
|   |-- repository
|   |-- service
|   |   `-- impl
|   `-- SmartRationBackendApplication.java
|-- src/main/resources/
|   `-- application.properties
|-- database/
|   |-- schema.sql
|   `-- api-sample-requests.md
|-- pom.xml
`-- README.md
```

## Database Setup

1. Open MySQL.
2. Run the SQL script from [schema.sql](C:\Users\Administrator\OneDrive\Documents\Smart-ration-System\smart-ration-backend\database\schema.sql).
3. This will create the `smart_ration_db` database and required tables.

Example:

```sql
SOURCE c:/Users/Administrator/OneDrive/Documents/Smart-ration-System/smart-ration-backend/database/schema.sql;
```

## MySQL Configuration

Update your `application.properties` file with your MySQL username and password:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/smart_ration_db
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.jpa.hibernate.ddl-auto=update
```

## How To Run

1. Make sure Java 17 and Maven are installed.
2. Open terminal in the project folder:

```powershell
cd c:\Users\Administrator\OneDrive\Documents\Smart-ration-System\smart-ration-backend
```

3. Run the project:

```powershell
mvn spring-boot:run
```

4. The application will start on:

```text
http://localhost:8080
```

## Main API Endpoints

- `POST /api/users/register`
- `POST /api/users/login`
- `GET /api/users/{id}`
- `POST /api/bookings`
- `GET /api/bookings/user/{id}`
- `POST /api/shopkeepers/login`
- `GET /api/shopkeepers/bookings?date=YYYY-MM-DD`
- `PATCH /api/shopkeepers/bookings/{bookingId}/verify`
- `POST /api/inventory`
- `PUT /api/inventory/{id}`
- `GET /api/inventory`

## Default Shopkeeper Login

- Username: `shopkeeper1`
- Password: `password123`

## Sample Request Bodies

See [api-sample-requests.md](C:\Users\Administrator\OneDrive\Documents\Smart-ration-System\smart-ration-backend\database\api-sample-requests.md) for example request JSON.

## Notes

- Use Postman or Thunder Client to test the APIs.
- If you already created tables from SQL, `spring.jpa.hibernate.ddl-auto=update` will keep them updated.
- If Maven is not installed, install Maven first and then run the project.
