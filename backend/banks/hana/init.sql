-- HANA Bank Database Initialization
CREATE DATABASE IF NOT EXISTS hana_bank;
USE hana_bank;

-- Create tables for HANA Bank
CREATE TABLE IF NOT EXISTS hana_customers (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ci VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(1),
    birth_date VARCHAR(8),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hana_accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    account_type INT NOT NULL,
    balance DECIMAL(15,2),
    opening_date DATE,
    customer_ci VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS hana_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    reference_number VARCHAR(50),
    FOREIGN KEY (account_number) REFERENCES hana_accounts(account_number)
);

-- Insert sample data
INSERT INTO hana_customers (ci, name, gender, birth_date, phone) VALUES
('ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567890', '홍길동', 'M', '19900101', '010-1234-5678'),
('XYZ789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ', '김영희', 'F', '19920515', '010-9876-5432');

INSERT INTO hana_accounts (account_number, account_type, balance, opening_date, customer_ci) VALUES
('1101234567890123', 1, 1000000.00, '2024-01-01', 'ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567890'),
('1111234567890123', 2, 500000.00, '2024-01-02', 'XYZ789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ');
