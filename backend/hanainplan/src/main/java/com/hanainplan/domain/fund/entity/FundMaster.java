package com.hanainplan.domain.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 모펀드 기본 정보 (하나인플랜)
 * - 하나은행 FundMaster와 동일한 구조
 */
@Entity
@Table(name = "fund_master")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class FundMaster {

    @Id
    @Column(name = "fund_cd", length = 16)
    private String fundCd; // 펀드 코드

    @Column(name = "fund_name", nullable = false)
    private String fundName; // 공식 펀드명

    @Column(name = "fund_gb", nullable = false)
    private Integer fundGb; // 공모/사모 등 코드 (1: 공모, 2: 사모)

    @Column(name = "asset_type", length = 50)
    private String assetType; // 주식/채권혼합/재간접 등

    @Column(name = "risk_grade", length = 16)
    private String riskGrade; // VERY_LOW ~ VERY_HIGH

    @Column(name = "currency", length = 8, nullable = false)
    @Builder.Default
    private String currency = "KRW";

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "synced_at")
    private LocalDateTime syncedAt; // 마지막 동기화 시간

    // 연관관계: 모펀드 1 : N 클래스
    @OneToMany(mappedBy = "fundMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FundClass> fundClasses = new ArrayList<>();

    /**
     * 펀드 유형 설명 반환
     */
    public String getFundGbDescription() {
        return switch (fundGb) {
            case 1 -> "공모";
            case 2 -> "사모";
            case 3 -> "전문투자자";
            default -> "알 수 없음";
        };
    }

    /**
     * 클래스 추가 헬퍼 메서드
     */
    public void addFundClass(FundClass fundClass) {
        fundClasses.add(fundClass);
        fundClass.setFundMaster(this);
    }

    /**
     * 동기화 시간 업데이트
     */
    public void updateSyncTime() {
        this.syncedAt = LocalDateTime.now();
    }
}

