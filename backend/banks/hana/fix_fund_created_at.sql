-- 펀드 데이터의 created_at 필드 수정 스크립트
-- 문제: 기존 fund_master 레코드의 created_at이 null이어서 업데이트 시 오류 발생

-- 방법 1: 기존 펀드 데이터를 모두 삭제하고 새로 생성 (권장)
SET FOREIGN_KEY_CHECKS = 0;
TRUNCATE TABLE fund_nav;
TRUNCATE TABLE fund_fees;
TRUNCATE TABLE fund_rules;
TRUNCATE TABLE fund_class;
TRUNCATE TABLE fund_master;
SET FOREIGN_KEY_CHECKS = 1;

-- 방법 2: created_at이 null인 레코드만 현재 시간으로 업데이트 (기존 데이터 유지)
-- UPDATE fund_master 
-- SET created_at = NOW() 
-- WHERE created_at IS NULL;

-- 완료 후 하나은행 서버를 재시작하면 FundDataLoader가 펀드 데이터를 다시 생성합니다.


