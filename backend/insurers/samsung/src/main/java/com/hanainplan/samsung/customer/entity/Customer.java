package com.hanainplan.samsung.customer.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "samsung_customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "customer_id")
    private Long customerId; // 고객ID (PK)

    @Column(name = "ci", unique = true, nullable = false, length = 64)
    private String ci; // CI 값

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 이름

    @Column(name = "gender", length = 10)
    private String gender; // 성별 (M/F)

    @Column(name = "birth_date", length = 8)
    private String birthDate; // 출생연월일 (YYYYMMDD)

    @Column(name = "phone", length = 20)
    private String phone; // 전화번호

    @Column(name = "email", length = 100)
    private String email; // 이메일

    @Column(name = "address", length = 200)
    private String address; // 주소

    @Column(name = "occupation", length = 100)
    private String occupation; // 직업

    @Column(name = "marital_status", length = 20)
    private String maritalStatus; // 혼인상태 (SINGLE, MARRIED, DIVORCED, WIDOWED)

    @Column(name = "customer_type", length = 20)
    private String customerType; // 고객유형 (INDIVIDUAL, CORPORATE)

    @Column(name = "risk_level", length = 20)
    private String riskLevel; // 위험등급 (LOW, MEDIUM, HIGH)

    @Column(name = "is_active")
    private Boolean isActive; // 활성화여부

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
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
