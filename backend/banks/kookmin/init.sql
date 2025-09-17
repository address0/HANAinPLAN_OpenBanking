-- Kookmin Bank Database Initialization
CREATE DATABASE IF NOT EXISTS kookmin_bank;
USE kookmin_bank;

-- Create tables for Kookmin Bank
CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_number VARCHAR(20) UNIQUE NOT NULL,
    user_id VARCHAR(50) NOT NULL,
    balance DECIMAL(15,2) DEFAULT 0.00,
    account_type ENUM('SAVINGS', 'CHECKING', 'DEPOSIT') DEFAULT 'SAVINGS',
    status ENUM('ACTIVE', 'INACTIVE', 'SUSPENDED') DEFAULT 'ACTIVE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    transaction_type ENUM('DEPOSIT', 'WITHDRAWAL', 'TRANSFER_IN', 'TRANSFER_OUT') NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    description TEXT,
    reference_number VARCHAR(50),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (account_id) REFERENCES accounts(id)
);

-- Insert sample data
INSERT INTO accounts (account_number, user_id, balance, account_type) VALUES
('KOOKMIN001', 'user001', 2000000.00, 'SAVINGS'),
('KOOKMIN002', 'user002', 1000000.00, 'CHECKING'),
('KOOKMIN003', 'user003', 5000000.00, 'DEPOSIT');
