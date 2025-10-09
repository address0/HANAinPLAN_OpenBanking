-- 알림 테이블 생성 스크립트

-- 알림 테이블
CREATE TABLE IF NOT EXISTS notifications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL COMMENT '알림을 받을 사용자 ID',
    title VARCHAR(200) NOT NULL COMMENT '알림 제목',
    content TEXT COMMENT '알림 내용',
    type VARCHAR(20) NOT NULL COMMENT '알림 타입 (CONSULTATION, TRANSACTION, SYSTEM, PROMOTION, SCHEDULE, OTHER)',
    is_read BOOLEAN DEFAULT FALSE COMMENT '읽음 여부',
    read_at DATETIME NULL COMMENT '읽은 시간',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시간',
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시간',

    INDEX idx_user_id (user_id),
    INDEX idx_user_read (user_id, is_read),
    INDEX idx_created_at (created_at),
    INDEX idx_type (type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='사용자 알림 테이블';

-- 알림 타입 체크 제약조건 추가 (MySQL 8.0 이상에서만 가능)
-- ALTER TABLE notifications ADD CONSTRAINT chk_notification_type
-- CHECK (type IN ('CONSULTATION', 'TRANSACTION', 'SYSTEM', 'PROMOTION', 'SCHEDULE', 'OTHER'));

-- 샘플 데이터 (테스트용)
INSERT INTO notifications (user_id, title, content, type, is_read) VALUES
(1, '상담 일정 알림', '내일 오전 10시에 상담 일정이 있습니다.', 'CONSULTATION', FALSE),
(1, '거래 완료 알림', '예금 상품 구매가 완료되었습니다.', 'TRANSACTION', TRUE),
(2, '시스템 점검 안내', '금일 새벽 2시부터 4시까지 시스템 점검이 예정되어 있습니다.', 'SYSTEM', FALSE),
(2, '프로모션 정보', '새로운 예금 상품이 출시되었습니다. 확인해보세요!', 'PROMOTION', FALSE);
