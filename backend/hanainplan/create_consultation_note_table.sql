-- 상담 메모 테이블 생성
CREATE TABLE IF NOT EXISTS tb_consultation_note (
    note_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    consult_id VARCHAR(20) NOT NULL,
    user_id BIGINT NOT NULL,
    note_type VARCHAR(20) NOT NULL, -- 'PERSONAL' 또는 'SHARED'
    content TEXT,
    created_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_date DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    INDEX idx_consult_id (consult_id),
    INDEX idx_user_id (user_id),
    INDEX idx_note_type (note_type),
    INDEX idx_consult_user_type (consult_id, user_id, note_type)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
