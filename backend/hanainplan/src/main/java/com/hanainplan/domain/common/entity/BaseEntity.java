package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.Getter;

/**
 * 공통 필드 BaseEntity
 * - 시간 필드 + 생성자/수정자 정보
 * - 감사(Audit) 기능이 필요한 엔터티에서 상속
 */
@MappedSuperclass
@Getter
public abstract class BaseEntity extends BaseTimeEntity {

    @Column(name = "CREATED_BY", length = 20, updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 20)
    private String updatedBy;

    /**
     * 생성자 설정
     */
    @PrePersist
    public void prePersist() {
        // SecurityContext에서 현재 사용자 정보 가져와서 설정
        // 추후 Spring Security 연동 시 구현
    }

    /**
     * 수정자 설정
     */
    @PreUpdate
    public void preUpdate() {
        // SecurityContext에서 현재 사용자 정보 가져와서 설정
        // 추후 Spring Security 연동 시 구현
    }
}
