package com.hanainplan.domain.common.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * 지점코드 엔터티
 * - 하나인플랜 서비스 지점 정보 관리
 * - 상담 예약 시 지점 선택에 사용
 */
@Entity
@Table(name = "branch_code")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BranchCode {

    @Id
    @Column(name = "branch_code", length = 20)
    private String branchCode;

    @Column(name = "branch_name", length = 100, nullable = false)
    private String branchName;

    @Column(name = "address", length = 200, nullable = false)
    private String address;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Column(name = "operating_hours", length = 100)
    private String operatingHours;

    @Column(name = "latitude", precision = 10, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 6)
    private BigDecimal longitude;

    @Column(name = "status", length = 10, nullable = false)
    @Builder.Default
    private String status = "운영";

    /**
     * 지점 운영 여부 확인
     */
    public boolean isOperating() {
        return "운영".equals(this.status);
    }

    /**
     * 위치 정보 존재 여부 확인
     */
    public boolean hasLocationInfo() {
        return latitude != null && longitude != null;
    }
}
