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

-- 금리 정보 테이블
CREATE TABLE IF NOT EXISTS kookmin_interest_rates (
    interest_rate_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '금리조건pk',
    product_code VARCHAR(20) NOT NULL COMMENT '상품코드',
    interest_type VARCHAR(20) NOT NULL COMMENT '금리종류(BASIC/PREFERENTIAL/AFTER_MATURITY/EARLY_WITHDRAWAL)',
    maturity_period VARCHAR(50) COMMENT '만기기간',
    interest_rate DECIMAL(5,4) NOT NULL COMMENT '금리(%)',
    effective_date DATE NOT NULL COMMENT '적용일자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 예금 가입 정보 테이블
CREATE TABLE IF NOT EXISTS kookmin_product_subscriptions (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '가입pk',
    customer_ci VARCHAR(64) NOT NULL COMMENT '고객CI',
    product_code VARCHAR(20) NOT NULL COMMENT '상품코드',
    account_number VARCHAR(20) NOT NULL COMMENT '계좌번호',
    status VARCHAR(20) NOT NULL COMMENT '상태 (ACTIVE, INACTIVE, MATURED, CANCELLED)',
    subscription_date DATE NOT NULL COMMENT '가입일자',
    maturity_date DATE COMMENT '만기일자',
    contract_period INT COMMENT '약정기간 (개월)',
    maturity_period VARCHAR(50) COMMENT '만기기간 (예: "6개월", "12개월")',
    rate_type VARCHAR(10) COMMENT '고정/변동여부 (FIXED, VARIABLE)',
    base_rate DECIMAL(5,4) COMMENT '기준금리',
    preferential_rate DECIMAL(5,4) COMMENT '우대금리(선택)',
    final_applied_rate DECIMAL(5,4) COMMENT '최종적용금리',
    preferential_reason VARCHAR(200) COMMENT '우대금리 사유',
    interest_calculation_basis VARCHAR(20) COMMENT '이자계산방법 (단리/복리)',
    interest_payment_method VARCHAR(20) COMMENT '이자지급방법 (만기일시/매월)',
    interest_type VARCHAR(20) COMMENT '이자유형',
    contract_principal DECIMAL(15,2) COMMENT '약정원금',
    current_balance DECIMAL(15,2) COMMENT '현재잔액',
    unpaid_interest DECIMAL(15,2) COMMENT '미수이자',
    unpaid_tax DECIMAL(15,2) COMMENT '미납세금',
    last_interest_calculation_date DATE COMMENT '최종이자계산일',
    next_interest_payment_date DATE COMMENT '다음이자지급일',
    branch_name VARCHAR(100) COMMENT '지점명',
    monthly_payment_amount DECIMAL(15,2) COMMENT '월납입액',
    monthly_payment_day INT COMMENT '월납입일',
    total_installments INT COMMENT '총납입회차',
    completed_installments INT COMMENT '완료납입회차',
    missed_installments INT COMMENT '미납회차',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO kookmin_customers (ci, name, gender, birth_date, phone) VALUES
('KOOKMIN123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567', '정수현', 'M', '19951225', '010-5555-6666'),
('KOOKMIN789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234', '최유진', 'F', '19880703', '010-7777-8888');

INSERT INTO kookmin_accounts (account_number, account_type, balance, opening_date, customer_ci) VALUES
('1231234567890123', 1, 2000000.00, '2024-01-01', 'KOOKMIN123DEF456GHI789JKL012MNO345PQR678STU901VWX234YZ567'),
('1241234567890123', 2, 1000000.00, '2024-01-02', 'KOOKMIN789ABC123DEF456GHI789JKL012MNO345PQR678STU901VWX234');

-- 국민은행 정기예금 금리 데이터 (상품유형 0: 일반 정기예금)
INSERT INTO kookmin_interest_rates (product_code, interest_type, maturity_period, interest_rate, effective_date) VALUES
('KOOKMIN_DEPOSIT_001', 'BASIC', '6개월', 0.0203, '2025-01-01'),
('KOOKMIN_DEPOSIT_001', 'BASIC', '1년', 0.0230, '2025-01-01'),
('KOOKMIN_DEPOSIT_001', 'BASIC', '2년', 0.0187, '2025-01-01'),
('KOOKMIN_DEPOSIT_001', 'BASIC', '3년', 0.0197, '2025-01-01'),
('KOOKMIN_DEPOSIT_001', 'BASIC', '5년', 0.0182, '2025-01-01');

-- 국민은행 정기예금 금리 데이터 (상품유형 1: 디폴트옵션)
INSERT INTO kookmin_interest_rates (product_code, interest_type, maturity_period, interest_rate, effective_date) VALUES
('KOOKMIN_DEPOSIT_002', 'BASIC', '3년', 0.0207, '2025-01-01');
