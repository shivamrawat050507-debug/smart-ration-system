CREATE DATABASE IF NOT EXISTS smart_ration_db;
USE smart_ration_db;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    ration_card_number VARCHAR(50) NOT NULL UNIQUE,
    phone VARCHAR(15) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS shopkeepers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE IF NOT EXISTS inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    rice INT NOT NULL,
    wheat INT NOT NULL,
    sugar INT NOT NULL,
    updated_at DATETIME NOT NULL
);

CREATE TABLE IF NOT EXISTS bookings (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    booking_date DATE NOT NULL,
    booking_time TIME NOT NULL,
    collected BIT(1) NOT NULL DEFAULT b'0',
    user_id BIGINT NOT NULL,
    CONSTRAINT fk_booking_user
        FOREIGN KEY (user_id) REFERENCES users(id)
        ON DELETE CASCADE
);

INSERT INTO shopkeepers (name, username, password)
SELECT 'Default Shopkeeper', 'shopkeeper1', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy'
WHERE NOT EXISTS (
    SELECT 1 FROM shopkeepers WHERE username = 'shopkeeper1'
);

INSERT INTO inventory (rice, wheat, sugar, updated_at)
SELECT 100, 80, 50, NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM inventory
);
