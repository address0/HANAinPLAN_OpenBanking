package com.hanainplan.domain.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 이율보증형 보험 상품 코드 엔티티
 */
@Entity
@Table(name = "tb_insurance_product")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InsuranceProduct {

    @Id
    @Column(name = "insurance_code", length = 20)
    private String insuranceCode; // 보험 상품 코드 (PK)

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 상품명

    @Column(name = "insurer_code", nullable = false, length = 10)
    private String insurerCode; // 보험사 코드

    @Column(name = "insurer_name", nullable = false, length = 50)
    private String insurerName; // 보험사명

    @Column(name = "description", columnDefinition = "TEXT")
    private String description; // 상품 설명

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}



