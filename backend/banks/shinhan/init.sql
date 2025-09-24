-- Shinhan Bank Database Initialization
CREATE DATABASE IF NOT EXISTS shinhan_bank;
USE shinhan_bank;

-- Create tables for Shinhan Bank
CREATE TABLE IF NOT EXISTS shinhan_customers (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ci VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(1),
    birth_date VARCHAR(8),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shinhan_accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    account_type INT NOT NULL,
    balance DECIMAL(15,2),
    opening_date DATE,
    customer_ci VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS shinhan_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    reference_number VARCHAR(50),
    FOREIGN KEY (account_number) REFERENCES shinhan_accounts(account_number)
);

-- Insert sample data
INSERT INTO shinhan_customers (ci, name, gender, birth_date, phone) VALUES
('SHINHAN123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567', '박민수', 'M', '19850320', '010-1111-2222'),
('SHINHAN789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234', '이지은', 'F', '19920810', '010-3333-4444');

INSERT INTO shinhan_accounts (account_number, account_type, balance, opening_date, customer_ci) VALUES
('4561234567890123', 1, 1500000.00, '2024-01-01', 'SHINHAN123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567'),
('4571234567890123', 2, 750000.00, '2024-01-02', 'SHINHAN789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234');
