-- 상담 타입 컬럼 길이 수정 스크립트

-- 상담 테이블의 consult_type 컬럼 길이를 20으로 변경
ALTER TABLE tb_consult MODIFY COLUMN consult_type VARCHAR(20) NOT NULL;
