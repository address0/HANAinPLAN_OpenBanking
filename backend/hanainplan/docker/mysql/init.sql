-- HANAinPLAN Database Initialization
CREATE DATABASE IF NOT EXISTS hanainplan;
USE hanainplan;

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS tb_user (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_type VARCHAR(20) NOT NULL,
    user_name VARCHAR(50) NOT NULL,
    social_number VARCHAR(13) NOT NULL UNIQUE,
    phone_number VARCHAR(13) NOT NULL UNIQUE,
    birth_date DATE NOT NULL,
    gender VARCHAR(1) NOT NULL,
    email VARCHAR(100),
    password VARCHAR(255),
    kakao_id VARCHAR(100) UNIQUE,
    ci VARCHAR(100),
    login_type VARCHAR(20) NOT NULL DEFAULT 'PASSWORD',
    is_phone_verified BOOLEAN DEFAULT FALSE,
    is_active BOOLEAN DEFAULT TRUE,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    last_login_date TIMESTAMP,
    profile_image_url VARCHAR(500)
);

-- 계좌 테이블
CREATE TABLE IF NOT EXISTS tb_banking_account (
    account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_ci VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(50) NOT NULL,
    account_type INT NOT NULL,
    account_status VARCHAR(20) NOT NULL,
    balance DECIMAL(15,2) NOT NULL,
    currency_code VARCHAR(3) NOT NULL,
    opened_date TIMESTAMP NOT NULL,
    expiry_date TIMESTAMP,
    interest_rate DECIMAL(5,4),
    minimum_balance DECIMAL(15,2),
    credit_limit DECIMAL(15,2),
    description VARCHAR(200),
    purpose VARCHAR(50),
    monthly_deposit_amount DECIMAL(15,2),
    deposit_period INT,
    interest_payment_method VARCHAR(10),
    account_password VARCHAR(255),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 거래내역 테이블
CREATE TABLE IF NOT EXISTS tb_banking_transaction (
    transaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    transaction_number VARCHAR(36) NOT NULL UNIQUE,
    from_account_id BIGINT,
    to_account_id BIGINT,
    transaction_type VARCHAR(20) NOT NULL,
    transaction_category VARCHAR(20) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    balance_after DECIMAL(15,2) NOT NULL,
    transaction_direction VARCHAR(10) NOT NULL,
    description VARCHAR(200),
    transaction_status VARCHAR(20) NOT NULL,
    transaction_date TIMESTAMP NOT NULL,
    processed_date TIMESTAMP,
    reference_number VARCHAR(50),
    memo VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 예금 상품 코드 테이블
CREATE TABLE IF NOT EXISTS tb_deposit_product (
    deposit_code VARCHAR(20) PRIMARY KEY COMMENT '예금 상품 코드',
    name VARCHAR(100) NOT NULL COMMENT '상품명',
    bank_code VARCHAR(10) NOT NULL COMMENT '은행 코드',
    bank_name VARCHAR(50) NOT NULL COMMENT '은행명',
    description TEXT COMMENT '상품 설명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bank_code (bank_code)
) COMMENT='예금 상품 코드 마스터';

-- 이율보증형 보험 상품 코드 테이블
CREATE TABLE IF NOT EXISTS tb_insurance_product (
    insurance_code VARCHAR(20) PRIMARY KEY COMMENT '보험 상품 코드',
    name VARCHAR(100) NOT NULL COMMENT '상품명',
    insurer_code VARCHAR(10) NOT NULL COMMENT '보험사 코드',
    insurer_name VARCHAR(50) NOT NULL COMMENT '보험사명',
    description TEXT COMMENT '상품 설명',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_insurer_code (insurer_code)
) COMMENT='이율보증형 보험 상품 코드 마스터';

-- 예금 가입 정보 테이블
CREATE TABLE IF NOT EXISTS tb_deposit_subscription (
    subscription_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '가입 ID',
    user_id BIGINT NOT NULL COMMENT 'HANAinPLAN 사용자 ID',
    customer_ci VARCHAR(100) NOT NULL COMMENT '고객 CI',
    account_number VARCHAR(20) NOT NULL COMMENT '계좌번호',
    status VARCHAR(20) NOT NULL COMMENT '상태 (ACTIVE, MATURED, CLOSED)',
    subscription_date DATE NOT NULL COMMENT '가입일',
    maturity_date DATE COMMENT '만기일',
    contract_period INT COMMENT '계약기간 (product_type=2: 일단위, 그외: 개월단위)',
    product_type INT NOT NULL DEFAULT 0 COMMENT '상품유형 (0:일반, 1:디폴트옵션, 2:일단위)',
    bank_name VARCHAR(50) NOT NULL COMMENT '은행명',
    bank_code VARCHAR(10) NOT NULL COMMENT '은행 코드',
    deposit_code VARCHAR(20) NOT NULL COMMENT '예금 상품 코드',
    rate DECIMAL(5,4) COMMENT '적용 금리',
    current_balance DECIMAL(15,2) NOT NULL DEFAULT 0 COMMENT '현재 잔액',
    unpaid_interest DECIMAL(15,2) DEFAULT 0 COMMENT '미지급 이자',
    last_interest_calculation_date DATE COMMENT '최종 이자 계산일',
    next_interest_payment_date DATE COMMENT '다음 이자 지급일',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_user_id (user_id),
    INDEX idx_customer_ci (customer_ci),
    INDEX idx_account_number (account_number),
    INDEX idx_bank_code (bank_code),
    INDEX idx_deposit_code (deposit_code),
    INDEX idx_status (status),
    INDEX idx_subscription_date (subscription_date),
    INDEX idx_maturity_date (maturity_date),
    INDEX idx_product_type (product_type),
    FOREIGN KEY (deposit_code) REFERENCES tb_deposit_product(deposit_code)
) COMMENT='예금 가입 정보';

-- IRP 계좌 테이블
CREATE TABLE IF NOT EXISTS tb_irp_account (
    irp_account_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_ci VARCHAR(100) NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    account_name VARCHAR(100),
    bank_code VARCHAR(10),
    bank_name VARCHAR(50),
    account_status VARCHAR(20) NOT NULL,
    total_balance DECIMAL(15,2),
    principal_amount DECIMAL(15,2),
    profit_amount DECIMAL(15,2),
    accumulated_contribution DECIMAL(15,2),
    annual_contribution_limit DECIMAL(15,2),
    remaining_contribution_limit DECIMAL(15,2),
    tax_benefit_amount DECIMAL(15,2),
    management_fee_rate DECIMAL(5,4),
    opened_date DATE,
    last_contribution_date DATE,
    retirement_date DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- ✅ IRP 거래내역은 tb_transaction 테이블에서 통합 관리
-- (account_type=6인 계좌의 거래내역으로 저장)

-- 자동이체 테이블
CREATE TABLE IF NOT EXISTS tb_auto_transfer (
    auto_transfer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    from_account_id BIGINT NOT NULL,
    to_account_id BIGINT NOT NULL,
    transfer_name VARCHAR(100),
    amount DECIMAL(15,2) NOT NULL,
    transfer_day INT NOT NULL,
    transfer_status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    next_transfer_date DATE,
    last_transfer_date DATE,
    total_transfer_count INT DEFAULT 0,
    failed_transfer_count INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 금융상품 테이블
CREATE TABLE IF NOT EXISTS tb_financial_products (
    product_id VARCHAR(20) PRIMARY KEY,
    product_name VARCHAR(200) NOT NULL,
    product_type VARCHAR(20) NOT NULL,
    provider VARCHAR(100) NOT NULL,
    created_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    status VARCHAR(20) NOT NULL DEFAULT '판매중',
    detail TEXT
);

-- 상담 테이블
CREATE TABLE IF NOT EXISTS tb_consult (
    consult_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    consultant_id BIGINT,
    consult_type VARCHAR(20) NOT NULL,
    consult_status VARCHAR(20) NOT NULL,
    title VARCHAR(200),
    content TEXT,
    scheduled_date TIMESTAMP,
    completed_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 상담사 테이블
CREATE TABLE IF NOT EXISTS tb_consultant (
    consultant_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    consultant_name VARCHAR(50) NOT NULL,
    department VARCHAR(50),
    position VARCHAR(50),
    specialization VARCHAR(100),
    phone_number VARCHAR(13),
    email VARCHAR(100),
    is_available BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 고객 테이블
CREATE TABLE IF NOT EXISTS tb_customer (
    customer_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    customer_name VARCHAR(50) NOT NULL,
    customer_grade VARCHAR(20),
    total_assets DECIMAL(15,2),
    credit_score INT,
    last_visit_date TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 통계 요약 테이블
CREATE TABLE IF NOT EXISTS tb_statistics_summary (
    summary_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    summary_date DATE NOT NULL,
    total_assets DECIMAL(15,2),
    total_deposits DECIMAL(15,2),
    total_investments DECIMAL(15,2),
    total_loans DECIMAL(15,2),
    monthly_income DECIMAL(15,2),
    monthly_expense DECIMAL(15,2),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 지점코드 테이블
CREATE TABLE IF NOT EXISTS tb_branch_code (
    branch_code VARCHAR(10) PRIMARY KEY,
    branch_name VARCHAR(100) NOT NULL,
    bank_code VARCHAR(10),
    bank_name VARCHAR(50),
    region VARCHAR(50),
    address VARCHAR(200),
    phone_number VARCHAR(20),
    operating_hours VARCHAR(100),
    is_active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 금리 정보 테이블
CREATE TABLE IF NOT EXISTS tb_interest_rate (
    interest_rate_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '금리 ID',
    bank_code VARCHAR(20) NOT NULL COMMENT '은행 코드 (HANA, KOOKMIN, SHINHAN)',
    bank_name VARCHAR(50) NOT NULL COMMENT '은행명',
    product_code VARCHAR(50) NOT NULL COMMENT '상품 코드',
    product_name VARCHAR(100) NOT NULL COMMENT '상품명',
    product_type INT NOT NULL COMMENT '상품 유형 (0:일반, 1:디폴트옵션)',
    maturity_period VARCHAR(20) NOT NULL COMMENT '만기 기간 (예: 6개월, 1년, 2년, 3년, 5년)',
    interest_type VARCHAR(20) NOT NULL DEFAULT 'BASIC' COMMENT '금리 종류 (BASIC, PREFERENTIAL)',
    interest_rate DECIMAL(5,4) NOT NULL COMMENT '금리 (%)',
    is_irp BOOLEAN DEFAULT FALSE COMMENT 'IRP 여부',
    effective_date DATE NOT NULL COMMENT '적용일자',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_bank_code (bank_code),
    INDEX idx_product_code (product_code),
    INDEX idx_maturity_period (maturity_period),
    INDEX idx_interest_type (interest_type)
) COMMENT='은행별 금리 정보';

-- 인덱스 생성
CREATE INDEX idx_user_account ON tb_banking_account(user_id);
CREATE INDEX idx_user_transaction ON tb_banking_transaction(from_account_id);
CREATE INDEX idx_transaction_date ON tb_banking_transaction(transaction_date);
CREATE INDEX idx_user_irp ON tb_irp_account(user_id);
CREATE INDEX idx_user_auto_transfer ON tb_auto_transfer(user_id);
CREATE INDEX idx_user_consult ON tb_consult(user_id);
CREATE INDEX idx_consult_status ON tb_consult(consult_status);

-- 금리 데이터 삽입
-- 하나은행 정기예금 금리
INSERT INTO tb_interest_rate (bank_code, bank_name, product_code, product_name, product_type, maturity_period, interest_type, interest_rate, is_irp, effective_date) VALUES
('HANA', '하나은행', 'HANA_DEPOSIT_001', '하나 정기예금', 0, '6개월', 'BASIC', 0.0207, FALSE, '2025-01-01'),
('HANA', '하나은행', 'HANA_DEPOSIT_001', '하나 정기예금', 0, '1년', 'BASIC', 0.0240, FALSE, '2025-01-01'),
('HANA', '하나은행', 'HANA_DEPOSIT_001', '하나 정기예금', 0, '2년', 'BASIC', 0.0200, FALSE, '2025-01-01'),
('HANA', '하나은행', 'HANA_DEPOSIT_001', '하나 정기예금', 0, '3년', 'BASIC', 0.0210, FALSE, '2025-01-01'),
('HANA', '하나은행', 'HANA_DEPOSIT_001', '하나 정기예금', 0, '5년', 'BASIC', 0.0202, FALSE, '2025-01-01'),
('HANA', '하나은행', 'HANA_DEPOSIT_002', '하나 디폴트옵션', 1, '3년', 'BASIC', 0.0220, TRUE, '2025-01-01');

-- 국민은행 정기예금 금리
INSERT INTO tb_interest_rate (bank_code, bank_name, product_code, product_name, product_type, maturity_period, interest_type, interest_rate, is_irp, effective_date) VALUES
('KOOKMIN', '국민은행', 'KOOKMIN_DEPOSIT_001', 'KB 정기예금', 0, '6개월', 'BASIC', 0.0203, FALSE, '2025-01-01'),
('KOOKMIN', '국민은행', 'KOOKMIN_DEPOSIT_001', 'KB 정기예금', 0, '1년', 'BASIC', 0.0230, FALSE, '2025-01-01'),
('KOOKMIN', '국민은행', 'KOOKMIN_DEPOSIT_001', 'KB 정기예금', 0, '2년', 'BASIC', 0.0187, FALSE, '2025-01-01'),
('KOOKMIN', '국민은행', 'KOOKMIN_DEPOSIT_001', 'KB 정기예금', 0, '3년', 'BASIC', 0.0197, FALSE, '2025-01-01'),
('KOOKMIN', '국민은행', 'KOOKMIN_DEPOSIT_001', 'KB 정기예금', 0, '5년', 'BASIC', 0.0182, FALSE, '2025-01-01'),
('KOOKMIN', '국민은행', 'KOOKMIN_DEPOSIT_002', 'KB 디폴트옵션', 1, '3년', 'BASIC', 0.0207, TRUE, '2025-01-01');

-- 신한은행 정기예금 금리
INSERT INTO tb_interest_rate (bank_code, bank_name, product_code, product_name, product_type, maturity_period, interest_type, interest_rate, is_irp, effective_date) VALUES
('SHINHAN', '신한은행', 'SHINHAN_DEPOSIT_001', '신한 정기예금', 0, '6개월', 'BASIC', 0.0198, FALSE, '2025-01-01'),
('SHINHAN', '신한은행', 'SHINHAN_DEPOSIT_001', '신한 정기예금', 0, '1년', 'BASIC', 0.0233, FALSE, '2025-01-01'),
('SHINHAN', '신한은행', 'SHINHAN_DEPOSIT_001', '신한 정기예금', 0, '2년', 'BASIC', 0.0197, FALSE, '2025-01-01'),
('SHINHAN', '신한은행', 'SHINHAN_DEPOSIT_001', '신한 정기예금', 0, '3년', 'BASIC', 0.0202, FALSE, '2025-01-01'),
('SHINHAN', '신한은행', 'SHINHAN_DEPOSIT_001', '신한 정기예금', 0, '5년', 'BASIC', 0.0205, FALSE, '2025-01-01'),
('SHINHAN', '신한은행', 'SHINHAN_DEPOSIT_002', '신한 디폴트옵션', 1, '3년', 'BASIC', 0.0212, TRUE, '2025-01-01');

-- 샘플 데이터 (선택사항)
-- INSERT INTO tb_user (user_name, social_number, phone_number, birth_date, gender, email, user_type) VALUES
-- ('홍길동', '9001011234567', '010-1234-5678', '1990-01-01', 'M', 'hong@example.com', 'CUSTOMER');

