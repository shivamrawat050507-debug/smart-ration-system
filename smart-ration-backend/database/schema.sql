CREATE DATABASE IF NOT EXISTS smart_ration_db;
USE smart_ration_db;

CREATE TABLE IF NOT EXISTS states (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_code VARCHAR(20) NOT NULL UNIQUE,
    state_name VARCHAR(120) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS cities (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_id BIGINT NOT NULL,
    city_code VARCHAR(30) NOT NULL UNIQUE,
    city_name VARCHAR(120) NOT NULL,
    population INT NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_city_state FOREIGN KEY (state_id) REFERENCES states(id)
);

CREATE TABLE IF NOT EXISTS depots (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    city_id BIGINT NOT NULL,
    depot_code VARCHAR(40) NOT NULL UNIQUE,
    depot_name VARCHAR(150) NOT NULL,
    address VARCHAR(255) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_depot_city FOREIGN KEY (city_id) REFERENCES cities(id)
);

CREATE TABLE IF NOT EXISTS dealers (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    depot_id BIGINT NOT NULL UNIQUE,
    full_name VARCHAR(120) NOT NULL,
    mobile VARCHAR(15) NOT NULL UNIQUE,
    username VARCHAR(60) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    CONSTRAINT fk_dealer_depot FOREIGN KEY (depot_id) REFERENCES depots(id)
);

CREATE TABLE IF NOT EXISTS ration_cards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    depot_id BIGINT NOT NULL,
    city_id BIGINT NOT NULL,
    state_id BIGINT NOT NULL,
    ration_card_no VARCHAR(40) NOT NULL UNIQUE,
    qr_code_value VARCHAR(255) NOT NULL UNIQUE,
    head_of_family VARCHAR(120) NOT NULL,
    category VARCHAR(20) NOT NULL,
    mobile VARCHAR(15) NOT NULL UNIQUE,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_card_depot FOREIGN KEY (depot_id) REFERENCES depots(id),
    CONSTRAINT fk_card_city FOREIGN KEY (city_id) REFERENCES cities(id),
    CONSTRAINT fk_card_state FOREIGN KEY (state_id) REFERENCES states(id)
);

CREATE TABLE IF NOT EXISTS family_members (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ration_card_id BIGINT NOT NULL,
    member_name VARCHAR(120) NOT NULL,
    relation_type VARCHAR(40) NOT NULL,
    age INT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT fk_family_card FOREIGN KEY (ration_card_id) REFERENCES ration_cards(id)
);

CREATE TABLE IF NOT EXISTS ration_rules (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    state_id BIGINT NOT NULL,
    ration_category VARCHAR(20) NOT NULL,
    commodity_name VARCHAR(50) NOT NULL,
    unit VARCHAR(20) NOT NULL,
    quantity_per_person DECIMAL(10, 2) NOT NULL,
    effective_from DATE NOT NULL,
    effective_to DATE NULL,
    CONSTRAINT fk_rule_state FOREIGN KEY (state_id) REFERENCES states(id)
);

CREATE TABLE IF NOT EXISTS depot_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    depot_id BIGINT NOT NULL,
    commodity_name VARCHAR(50) NOT NULL,
    available_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    reserved_pending_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    monthly_required_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    last_updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uq_depot_commodity (depot_id, commodity_name),
    CONSTRAINT fk_stock_depot FOREIGN KEY (depot_id) REFERENCES depots(id)
);

CREATE TABLE IF NOT EXISTS monthly_entitlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ration_card_id BIGINT NOT NULL,
    entitlement_month CHAR(7) NOT NULL,
    commodity_name VARCHAR(50) NOT NULL,
    entitled_quantity DECIMAL(10, 2) NOT NULL,
    issued_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    pending_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    UNIQUE KEY uq_card_month_commodity (ration_card_id, entitlement_month, commodity_name),
    CONSTRAINT fk_entitlement_card FOREIGN KEY (ration_card_id) REFERENCES ration_cards(id)
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_no VARCHAR(40) NOT NULL UNIQUE,
    ration_card_id BIGINT NOT NULL,
    depot_id BIGINT NOT NULL,
    dealer_id BIGINT NOT NULL,
    distribution_month CHAR(7) NOT NULL,
    status VARCHAR(30) NOT NULL,
    verification_mode VARCHAR(30) NOT NULL,
    issued_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    remarks VARCHAR(255) NULL,
    CONSTRAINT fk_txn_card FOREIGN KEY (ration_card_id) REFERENCES ration_cards(id),
    CONSTRAINT fk_txn_depot FOREIGN KEY (depot_id) REFERENCES depots(id),
    CONSTRAINT fk_txn_dealer FOREIGN KEY (dealer_id) REFERENCES dealers(id)
);

CREATE TABLE IF NOT EXISTS transaction_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_id BIGINT NOT NULL,
    commodity_name VARCHAR(50) NOT NULL,
    entitled_quantity DECIMAL(10, 2) NOT NULL,
    issued_quantity DECIMAL(10, 2) NOT NULL,
    pending_quantity DECIMAL(10, 2) NOT NULL DEFAULT 0,
    CONSTRAINT fk_txn_item_transaction FOREIGN KEY (transaction_id) REFERENCES transactions(id)
);

INSERT INTO states (state_code, state_name)
SELECT 'HR', 'Haryana'
WHERE NOT EXISTS (SELECT 1 FROM states WHERE state_code = 'HR');

INSERT INTO cities (state_id, city_code, city_name, population)
SELECT s.id, 'GRG', 'Gurugram', 184500
FROM states s
WHERE s.state_code = 'HR'
  AND NOT EXISTS (SELECT 1 FROM cities WHERE city_code = 'GRG');

INSERT INTO depots (city_id, depot_code, depot_name, address)
SELECT c.id, 'GRG-D014', 'Sector 14 Fair Price Depot', 'Ward 14, Gurugram'
FROM cities c
WHERE c.city_code = 'GRG'
  AND NOT EXISTS (SELECT 1 FROM depots WHERE depot_code = 'GRG-D014');
