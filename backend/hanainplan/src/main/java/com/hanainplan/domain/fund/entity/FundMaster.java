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
    private String fundCd;

    @Column(name = "fund_name", nullable = false)
    private String fundName;

    @Column(name = "fund_gb", nullable = false)
    private Integer fundGb;

    @Column(name = "asset_type", length = 50)
    private String assetType;

    @Column(name = "risk_grade", length = 16)
    private String riskGrade;

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
    private LocalDateTime syncedAt;

    @OneToMany(mappedBy = "fundMaster", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FundClass> fundClasses = new ArrayList<>();

    public String getFundGbDescription() {
        return switch (fundGb) {
            case 1 -> "공모";
            case 2 -> "사모";
            case 3 -> "전문투자자";
            default -> "알 수 없음";
        };
    }

    public void addFundClass(FundClass fundClass) {
        fundClasses.add(fundClass);
        fundClass.setFundMaster(this);
    }

    public void updateSyncTime() {
        this.syncedAt = LocalDateTime.now();
    }
}