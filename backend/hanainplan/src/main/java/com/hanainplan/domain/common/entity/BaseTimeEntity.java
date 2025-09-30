package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 공통 시간 필드 BaseEntity
 * - 생성시간, 수정시간 자동 관리
 * - 다른 엔터티에서 상속받아 사용
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @CreatedDate
    @Column(name = "created_date", updatable = false)
    private LocalDateTime createdDate;

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }
}

