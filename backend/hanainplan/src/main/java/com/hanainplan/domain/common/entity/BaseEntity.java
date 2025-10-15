package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class BaseEntity extends BaseTimeEntity {

    @Column(name = "CREATED_BY", length = 20, updatable = false)
    private String createdBy;

    @Column(name = "UPDATED_BY", length = 20)
    private String updatedBy;

    @PrePersist
    public void prePersist() {
    }

    @PreUpdate
    public void preUpdate() {
    }
}