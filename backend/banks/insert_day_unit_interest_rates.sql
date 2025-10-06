-- =============================================================================
-- 일 단위 정기예금 금리 데이터 추가
-- 퇴직 임박 고객을 위한 단기 상품 (90일, 180일)
-- =============================================================================

-- ========== 하나은행 일 단위 금리 ==========
USE hana_bank;

-- 90일 (3개월) 정기예금 금리
INSERT INTO hana_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('HANA_DEPOSIT_001', 'BASIC', '90일', 0.0280, FALSE, '2025-01-01'),
('HANA_DEPOSIT_001', 'PREFERENTIAL', '90일', 0.0300, FALSE, '2025-01-01');

-- 180일 (6개월) 정기예금 금리
INSERT INTO hana_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('HANA_DEPOSIT_001', 'BASIC', '180일', 0.0285, FALSE, '2025-01-01'),
('HANA_DEPOSIT_001', 'PREFERENTIAL', '180일', 0.0305, FALSE, '2025-01-01');

-- IRP 연계 일 단위 정기예금 금리 (기본금리 + IRP 혜택)
INSERT INTO hana_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('IRP_DEPOSIT_001', 'BASIC', '90일', 0.0290, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'PREFERENTIAL', '90일', 0.0310, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'BASIC', '180일', 0.0295, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'PREFERENTIAL', '180일', 0.0315, TRUE, '2025-01-01');

SELECT '✅ 하나은행 일 단위 금리 데이터 추가 완료' AS status;


-- ========== 국민은행 일 단위 금리 ==========
USE kookmin_bank;

-- 90일 (3개월) 정기예금 금리 (하나보다 약간 높은 금리)
INSERT INTO kookmin_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('KB_DEPOSIT_001', 'BASIC', '90일', 0.0290, FALSE, '2025-01-01'),
('KB_DEPOSIT_001', 'PREFERENTIAL', '90일', 0.0310, FALSE, '2025-01-01');

-- 180일 (6개월) 정기예금 금리
INSERT INTO kookmin_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('KB_DEPOSIT_001', 'BASIC', '180일', 0.0295, FALSE, '2025-01-01'),
('KB_DEPOSIT_001', 'PREFERENTIAL', '180일', 0.0315, FALSE, '2025-01-01');

-- IRP 연계 일 단위 정기예금 금리
INSERT INTO kookmin_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('IRP_DEPOSIT_001', 'BASIC', '90일', 0.0300, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'PREFERENTIAL', '90일', 0.0320, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'BASIC', '180일', 0.0305, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'PREFERENTIAL', '180일', 0.0325, TRUE, '2025-01-01');

SELECT '✅ 국민은행 일 단위 금리 데이터 추가 완료' AS status;


-- ========== 신한은행 일 단위 금리 ==========
USE shinhan_bank;

-- 90일 (3개월) 정기예금 금리 (중간 수준)
INSERT INTO shinhan_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('SH_DEPOSIT_001', 'BASIC', '90일', 0.0285, FALSE, '2025-01-01'),
('SH_DEPOSIT_001', 'PREFERENTIAL', '90일', 0.0305, FALSE, '2025-01-01');

-- 180일 (6개월) 정기예금 금리
INSERT INTO shinhan_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('SH_DEPOSIT_001', 'BASIC', '180일', 0.0290, FALSE, '2025-01-01'),
('SH_DEPOSIT_001', 'PREFERENTIAL', '180일', 0.0310, FALSE, '2025-01-01');

-- IRP 연계 일 단위 정기예금 금리
INSERT INTO shinhan_interest_rates (product_code, interest_type, maturity_period, interest_rate, is_irp, effective_date) VALUES
('IRP_DEPOSIT_001', 'BASIC', '90일', 0.0295, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'PREFERENTIAL', '90일', 0.0315, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'BASIC', '180일', 0.0300, TRUE, '2025-01-01'),
('IRP_DEPOSIT_001', 'PREFERENTIAL', '180일', 0.0320, TRUE, '2025-01-01');

SELECT '✅ 신한은행 일 단위 금리 데이터 추가 완료' AS status;


-- ========== 확인 쿼리 ==========

-- 하나은행 일 단위 금리 확인
USE hana_bank;
SELECT '하나은행 일 단위 금리' AS bank, product_code, interest_type, maturity_period, interest_rate, is_irp 
FROM hana_interest_rates 
WHERE maturity_period IN ('90일', '180일') 
ORDER BY is_irp, maturity_period, interest_type;

-- 국민은행 일 단위 금리 확인
USE kookmin_bank;
SELECT '국민은행 일 단위 금리' AS bank, product_code, interest_type, maturity_period, interest_rate, is_irp 
FROM kookmin_interest_rates 
WHERE maturity_period IN ('90일', '180일') 
ORDER BY is_irp, maturity_period, interest_type;

-- 신한은행 일 단위 금리 확인
USE shinhan_bank;
SELECT '신한은행 일 단위 금리' AS bank, product_code, interest_type, maturity_period, interest_rate, is_irp 
FROM shinhan_interest_rates 
WHERE maturity_period IN ('90일', '180일') 
ORDER BY is_irp, maturity_period, interest_type;


-- ========== 금리 비교 ==========
SELECT 
    '금리 비교 (90일)' AS comparison,
    'HANA' AS bank_code,
    (SELECT interest_rate FROM hana_bank.hana_interest_rates WHERE maturity_period = '90일' AND interest_type = 'BASIC' AND is_irp = TRUE LIMIT 1) AS irp_rate
UNION ALL
SELECT 
    '금리 비교 (90일)',
    'KOOKMIN',
    (SELECT interest_rate FROM kookmin_bank.kookmin_interest_rates WHERE maturity_period = '90일' AND interest_type = 'BASIC' AND is_irp = TRUE LIMIT 1)
UNION ALL
SELECT 
    '금리 비교 (90일)',
    'SHINHAN',
    (SELECT interest_rate FROM shinhan_bank.shinhan_interest_rates WHERE maturity_period = '90일' AND interest_type = 'BASIC' AND is_irp = TRUE LIMIT 1)
ORDER BY irp_rate DESC;

SELECT 
    '금리 비교 (180일)' AS comparison,
    'HANA' AS bank_code,
    (SELECT interest_rate FROM hana_bank.hana_interest_rates WHERE maturity_period = '180일' AND interest_type = 'BASIC' AND is_irp = TRUE LIMIT 1) AS irp_rate
UNION ALL
SELECT 
    '금리 비교 (180일)',
    'KOOKMIN',
    (SELECT interest_rate FROM kookmin_bank.kookmin_interest_rates WHERE maturity_period = '180일' AND interest_type = 'BASIC' AND is_irp = TRUE LIMIT 1)
UNION ALL
SELECT 
    '금리 비교 (180일)',
    'SHINHAN',
    (SELECT interest_rate FROM shinhan_bank.shinhan_interest_rates WHERE maturity_period = '180일' AND interest_type = 'BASIC' AND is_irp = TRUE LIMIT 1)
ORDER BY irp_rate DESC;





