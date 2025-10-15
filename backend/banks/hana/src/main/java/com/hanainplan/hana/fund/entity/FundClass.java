package com.hanainplan.hana.fund.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "fund_class")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundClass {

    @Id
    @Column(name = "child_fund_cd", length = 16)
    private String childFundCd;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fund_cd", nullable = false)
    private FundMaster fundMaster;

    @Column(name = "class_code", length = 8)
    private String classCode;

    @Column(name = "load_type", length = 16)
    private String loadType;

    @Column(name = "tax_category", length = 32)
    private String taxCategory;

    @Column(name = "sale_status", length = 16, nullable = false)
    @Builder.Default
    private String saleStatus = "ON";

    @Column(name = "source_url", length = 512)
    private String sourceUrl;

    @OneToOne(mappedBy = "fundClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private FundRules fundRules;

    @OneToOne(mappedBy = "fundClass", cascade = CascadeType.ALL, orphanRemoval = true)
    private FundFees fundFees;

    public boolean isOnSale() {
        return "ON".equals(saleStatus);
    }

    public String getDisplayName() {
        if (fundMaster == null) {
            return childFundCd;
        }
        String className = classCode != null ? " " + classCode + "클래스" : "";
        return fundMaster.getFundName() + className;
    }

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