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
    transaction_type VARCHAR(50) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    transaction_datetime TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    description TEXT,
    reference_number VARCHAR(50),
    transaction_category VARCHAR(50),
    branch_name VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (account_number) REFERENCES hana_accounts(account_number)
);

-- 예금 가입 정보 테이블
CREATE TABLE IF NOT EXISTS hana_deposit_subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '가입 ID',
    customer_ci VARCHAR(64) NOT NULL COMMENT '고객 CI',
    account_number VARCHAR(20) NOT NULL COMMENT '계좌번호',
    status VARCHAR(20) NOT NULL COMMENT '상태 (ACTIVE, MATURED, CLOSED)',
    subscription_date DATE NOT NULL COMMENT '가입일',
    maturity_date DATE COMMENT '만기일',
    contract_period INT COMMENT '계약기간 (product_type=2: 일단위, 그외: 개월단위)',
    product_type INT NOT NULL DEFAULT 0 COMMENT '상품유형 (0:일반, 1:디폴트옵션, 2:일단위)',
    bank_name VARCHAR(50) NOT NULL DEFAULT '하나은행' COMMENT '은행명',
    bank_code VARCHAR(10) NOT NULL DEFAULT 'HANA' COMMENT '은행 코드',
    deposit_code VARCHAR(20) NOT NULL COMMENT '예금 상품 코드',
    rate DECIMAL(5,4) COMMENT '적용 금리',
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '현재 잔액',
    unpaid_interest DECIMAL(15,2) DEFAULT 0 COMMENT '미지급 이자',
    last_interest_calculation_date DATE COMMENT '최종 이자 계산일',
    next_interest_payment_date DATE COMMENT '다음 이자 지급일',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_customer_ci (customer_ci),
    INDEX idx_account_number (account_number),
    INDEX idx_deposit_code (deposit_code),
    INDEX idx_status (status),
    INDEX idx_subscription_date (subscription_date),
    INDEX idx_maturity_date (maturity_date),
    INDEX idx_product_type (product_type),
    FOREIGN KEY (customer_ci) REFERENCES hana_customers(ci),
    FOREIGN KEY (account_number) REFERENCES hana_accounts(account_number)
) COMMENT='하나은행 예금 가입 정보';

-- 금리 정보 테이블
CREATE TABLE IF NOT EXISTS hana_interest_rates (
    interest_rate_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '금리조건pk',
    product_code VARCHAR(20) NOT NULL COMMENT '상품코드',
    interest_type VARCHAR(20) NOT NULL COMMENT '금리종류(BASIC/PREFERENTIAL/AFTER_MATURITY/EARLY_WITHDRAWAL)',
    maturity_period VARCHAR(50) COMMENT '만기기간',
    interest_rate DECIMAL(5,4) NOT NULL COMMENT '금리(%)',
    is_irp BOOLEAN DEFAULT FALSE COMMENT 'IRP여부',
    effective_date DATE NOT NULL COMMENT '적용일자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO hana_customers (ci, name, gender, birth_date, phone) VALUES
('ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567890', '홍길동', 'M', '19900101', '010-1234-5678'),
('XYZ789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ', '김영희', 'F', '19920515', '010-9876-5432');

INSERT INTO hana_accounts (account_number, account_type, balance, opening_date, customer_ci) VALUES
('1101234567890123', 1, 1000000.00, '2024-01-01', 'ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567890'),
('1111234567890123', 2, 500000.00, '2024-01-02', 'XYZ789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ');

-- 하나은행 정기예금 금리 데이터 (일반 정기예금)
INSERT INTO hana_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('HANA_DEPOSIT_001', 'BASIC', '6개월', 0.0207, FALSE, '2025-01-01'),
('HANA_DEPOSIT_001', 'BASIC', '1년', 0.0240, FALSE, '2025-01-01'),
('HANA_DEPOSIT_001', 'BASIC', '2년', 0.0200, FALSE, '2025-01-01'),
('HANA_DEPOSIT_001', 'BASIC', '3년', 0.0210, FALSE, '2025-01-01'),
('HANA_DEPOSIT_001', 'BASIC', '5년', 0.0202, FALSE, '2025-01-01');

-- 하나은행 정기예금 금리 데이터 (디폴트옵션)
INSERT INTO hana_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('HANA_DEPOSIT_002', 'BASIC', '3년', 0.0220, TRUE, '2025-01-01');
