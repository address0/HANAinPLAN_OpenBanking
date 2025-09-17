package com.hanainplan.domain.product.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 금융상품 엔터티
 * - 보험, 연금, 투자 등 다양한 금융상품 정보 관리
 * - 상품 추천 및 가입에 사용
 */
@Entity
@Table(name = "tb_financial_products")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FinancialProduct {

    @Id
    @Column(name = "product_id", length = 20)
    private String productId;

    @Column(name = "product_name", length = 200, nullable = false)
    private String productName;

    @Column(name = "product_type", length = 20, nullable = false)
    private String productType;

    @Column(name = "provider", length = 100, nullable = false)
    private String provider;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "status", length = 20, nullable = false)
    @Builder.Default
    private String status = "판매중";

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    /**
     * 상품 유형 enum 정의
     */
    public enum ProductType {
        INSURANCE("보험"), PENSION("연금"), INVESTMENT("투자"), LOAN("대출"), DEPOSIT("예적금");

        private final String description;

        ProductType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static ProductType fromValue(String value) {
            for (ProductType type : values()) {
                if (type.description.equals(value)) {
                    return type;
                }
            }
            return INSURANCE; // 기본값
        }
    }

    /**
     * 상품 상태 enum 정의
     */
    public enum ProductStatus {
        ACTIVE("판매중"), SUSPENDED("판매중지"), DISCONTINUED("단종");

        private final String value;

        ProductStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ProductStatus fromValue(String value) {
            for (ProductStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return ACTIVE; // 기본값
        }
    }

    /**
     * 판매 중인 상품 여부 확인
     */
    public boolean isActive() {
        return "판매중".equals(this.status);
    }

    /**
     * 보험 상품 여부 확인
     */
    public boolean isInsuranceProduct() {
        return "보험".equals(this.productType);
    }

    /**
     * 연금 상품 여부 확인
     */
    public boolean isPensionProduct() {
        return "연금".equals(this.productType);
    }

    /**
     * 상품 상태 업데이트
     */
    public void updateStatus(String newStatus) {
        if (newStatus != null && !newStatus.trim().isEmpty()) {
            this.status = newStatus;
        }
    }
}
