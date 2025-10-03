package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 펀드 클래스 (판매단위)
 * - 같은 모펀드라도 A/C/P 등 클래스별로 다른 보수·판매규칙
 */
@Entity
@Table(name = "fund_class")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundClass {

    @Id
    @Column(name = "child_fund_cd", length = 16)
    private String childFundCd; // 클래스 펀드 코드 (예: 51306P, 30810C)

    // 연관관계: 클래스 N : 1 모펀드
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_cd", nullable = false)
    private FundMaster fundMaster;

    @Column(name = "class_code", length = 8)
    private String classCode; // A/C/I/P 등 (표기용)

    @Column(name = "load_type", length = 16)
    private String loadType; // FRONT/BACK/NONE/UNKNOWN

    @Column(name = "tax_category", length = 32)
    private String taxCategory; // 배당소득/양도소득 등

    @Column(name = "sale_status", length = 16, nullable = false)
    @Builder.Default
    private String saleStatus = "ON"; // ON/OFF/PAUSE

    @Column(name = "source_url", length = 512)
    private String sourceUrl; // 펀드 상세페이지 URL

    // 연관관계: 클래스 1 : 1 거래규칙
    @OneToOne(mappedBy = "fundClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private FundRules fundRules;

    // 연관관계: 클래스 1 : 1 수수료
    @OneToOne(mappedBy = "fundClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private FundFees fundFees;

    /**
     * 판매 중 여부 확인
     */
    public boolean isOnSale() {
        return "ON".equals(saleStatus);
    }

    /**
     * 클래스 표시명 반환 (예: "미래에셋퇴직플랜20 P클래스")
     */
    public String getDisplayName() {
        if (fundMaster == null) {
            return childFundCd;
        }
        String className = classCode != null ? " " + classCode + "클래스" : "";
        return fundMaster.getFundName() + className;
    }

    /**
     * Load Type 설명 반환
     */
    public String getLoadTypeDescription() {
        return switch (loadType) {
            case "FRONT" -> "선취";
            case "BACK" -> "후취";
            case "NONE" -> "무부하";
            case "UNKNOWN" -> "미정";
            default -> loadType;
        };
    }
}

