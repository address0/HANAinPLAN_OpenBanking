-- Kookmin Bank Database Initialization
CREATE DATABASE IF NOT EXISTS kookmin_bank;
USE kookmin_bank;

-- Create tables for Kookmin Bank
CREATE TABLE IF NOT EXISTS kookmin_customers (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    ci VARCHAR(64) UNIQUE NOT NULL,
    name VARCHAR(100) NOT NULL,
    gender VARCHAR(1),
    birth_date VARCHAR(8),
    phone VARCHAR(20),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kookmin_accounts (
    account_number VARCHAR(20) PRIMARY KEY,
    account_type INT NOT NULL,
    balance DECIMAL(15,2),
    opening_date DATE,
    customer_ci VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS kookmin_transactions (
    transaction_id VARCHAR(50) PRIMARY KEY,
    account_number VARCHAR(20) NOT NULL,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    reference_number VARCHAR(50),
    FOREIGN KEY (account_number) REFERENCES kookmin_accounts(account_number)
);

-- Insert sample data
INSERT INTO kookmin_customers (ci, name, gender, birth_date, phone) VALUES
('KOOKMIN123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567', '정수현', 'M', '19951225', '010-5555-6666'),
('KOOKMIN789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234', '최유진', 'F', '19880703', '010-7777-8888');

INSERT INTO kookmin_accounts (account_number, account_type, balance, opening_date, customer_ci) VALUES
('1231234567890123', 1, 2000000.00, '2024-01-01', 'KOOKMIN123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567'),
('1241234567890123', 2, 1000000.00, '2024-01-02', 'KOOKMIN789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234');
