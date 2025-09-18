package com.hanainplan.hana.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "hana_financial_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @Column(name = "product_code", unique = true, nullable = false, length = 20)
    private String productCode; // 상품코드

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName; // 상품명

    @Column(name = "product_type", nullable = false)
    private Integer productType; // 상품종류 (1: 예금, 2: 적금, 3: 대출, 4: 펀드, 5: 보험)

    @Column(name = "interest_rate", precision = 5, scale = 4)
    private BigDecimal interestRate; // 금리

    @Column(name = "min_amount", precision = 15, scale = 2)
    private BigDecimal minAmount; // 최소금액

    @Column(name = "max_amount", precision = 15, scale = 2)
    private BigDecimal maxAmount; // 최대금액

    @Column(name = "term_months")
    private Integer termMonths; // 기간(개월)

    @Column(name = "is_active")
    private Boolean isActive; // 활성화여부

    @Column(name = "start_date")
    private LocalDate startDate; // 판매시작일

    @Column(name = "end_date")
    private LocalDate endDate; // 판매종료일

    @Column(name = "description", length = 500)
    private String description; // 상품설명

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
