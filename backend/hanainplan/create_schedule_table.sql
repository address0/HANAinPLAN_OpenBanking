-- 일정 관리 테이블 생성 스크립트

-- tb_schedule 테이블 생성
CREATE TABLE IF NOT EXISTS tb_schedule (
    schedule_id BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '일정 ID',
    consultant_id BIGINT NOT NULL COMMENT '상담사 ID',
    title VARCHAR(200) NOT NULL COMMENT '일정 제목',
    description VARCHAR(1000) COMMENT '일정 설명',
    schedule_type VARCHAR(20) NOT NULL COMMENT '일정 유형 (CONSULTATION, MEETING, OTHER)',
    client_name VARCHAR(50) COMMENT '고객명 (상담인 경우)',
    client_id BIGINT COMMENT '고객 ID (상담인 경우)',
    start_time DATETIME NOT NULL COMMENT '시작 시간',
    end_time DATETIME NOT NULL COMMENT '종료 시간',
    is_all_day BOOLEAN DEFAULT FALSE COMMENT '종일 일정 여부',
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED' COMMENT '일정 상태 (SCHEDULED, IN_PROGRESS, COMPLETED, CANCELLED)',
    location VARCHAR(200) COMMENT '위치',
    memo VARCHAR(500) COMMENT '메모',
    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',
    INDEX idx_consultant_id (consultant_id),
    INDEX idx_start_time (start_time),
    INDEX idx_schedule_type (schedule_type),
    INDEX idx_status (status),
    INDEX idx_client_id (client_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='상담사 일정 관리';

-- 샘플 데이터 삽입 (테스트용)
-- 상담사 ID는 실제 tb_user와 tb_consultant에 존재하는 ID를 사용해야 합니다.

-- 샘플 일정 1: 고객 상담
INSERT INTO tb_schedule (consultant_id, title, description, schedule_type, client_name, start_time, end_time, status)
VALUES (2, '김철수 고객 IRP 상담', 'IRP 상품 가입 상담 및 포트폴리오 분석', 'CONSULTATION', '김철수', 
        DATE_ADD(NOW(), INTERVAL 1 DAY), DATE_ADD(NOW(), INTERVAL 1 DAY) + INTERVAL 1 HOUR, 'SCHEDULED');

-- 샘플 일정 2: 팀 회의
INSERT INTO tb_schedule (consultant_id, title, description, schedule_type, start_time, end_time, status)
VALUES (2, '월간 실적 회의', '2025년 1월 실적 리뷰 및 2월 목표 설정', 'MEETING',
        DATE_ADD(NOW(), INTERVAL 2 DAY), DATE_ADD(NOW(), INTERVAL 2 DAY) + INTERVAL 2 HOUR, 'SCHEDULED');

-- 샘플 일정 3: 고객 상담
INSERT INTO tb_schedule (consultant_id, title, description, schedule_type, client_name, start_time, end_time, status)
VALUES (2, '이영희 고객 펀드 상담', '펀드 상품 문의 및 투자 전략 상담', 'CONSULTATION', '이영희',
        DATE_ADD(NOW(), INTERVAL 3 DAY), DATE_ADD(NOW(), INTERVAL 3 DAY) + INTERVAL 1 HOUR, 'SCHEDULED');

-- 샘플 일정 4: 기타
INSERT INTO tb_schedule (consultant_id, title, description, schedule_type, start_time, end_time, status, location)
VALUES (2, '금융 상품 교육', '신규 IRP 상품 교육 세미나', 'OTHER',
        DATE_ADD(NOW(), INTERVAL 5 DAY), DATE_ADD(NOW(), INTERVAL 5 DAY) + INTERVAL 3 HOUR, 'SCHEDULED', '본사 회의실');

-- 완료된 일정 (테스트용)
INSERT INTO tb_schedule (consultant_id, title, description, schedule_type, client_name, start_time, end_time, status)
VALUES (2, '박민수 고객 상담', '정기예금 만기 상담 완료', 'CONSULTATION', '박민수',
        DATE_SUB(NOW(), INTERVAL 2 DAY), DATE_SUB(NOW(), INTERVAL 2 DAY) + INTERVAL 1 HOUR, 'COMPLETED');

-- 데이터 확인
SELECT * FROM tb_schedule ORDER BY start_time DESC;

