package com.hanainplan.domain.consult.entity;

import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.entity.Consultant;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 엔터티
 * - 고객과 컨설턴트 간의 상담 정보 관리
 * - 예약, 진행, 완료 상태 관리
 */
@Entity
@Table(name = "tb_consult")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consult {

    @Id
    @Column(name = "consult_id", length = 20)
    private String consultId;

    @Column(name = "consult_type", length = 20, nullable = false)
    private String consultType;

    @Column(name = "reservation_datetime", nullable = false)
    private LocalDateTime reservationDatetime;

    @Column(name = "consult_datetime")
    private LocalDateTime consultDatetime;

    @Column(name = "consult_status", length = 20, nullable = false)
    @Builder.Default
    private String consultStatus = "예약신청";

    @Column(name = "qr_code", length = 255)
    private String qrCode;

    @Column(name = "consult_url", length = 500)
    private String consultUrl;

    @Column(name = "detail", columnDefinition = "TEXT")
    private String detail;

    @Column(name = "consult_result", length = 20)
    private String consultResult;

    @Column(name = "branch_code", length = 20)
    private String branchCode;

    @Column(name = "customer_id", length = 20, nullable = false)
    private String customerId;

    @Column(name = "consultant_id", length = 20, nullable = false)
    private String consultantId;

    @Column(name = "notification_sent_10min")
    @Builder.Default
    private Boolean notificationSent10min = false;

    @Column(name = "notification_sent_ontime")
    @Builder.Default
    private Boolean notificationSentOntime = false;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "branch_code", referencedColumnName = "branch_code", insertable = false, updatable = false)
//    private BranchCode branch;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "customer_id", referencedColumnName = "customer_id", insertable = false, updatable = false)
//    private Customer customer;
//
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "consultant_id", referencedColumnName = "consultant_id", insertable = false, updatable = false)
//    private Consultant consultant;

    /**
     * 상담 유형 enum 정의
     */
    public enum ConsultType {
        ONLINE("온라인"), OFFLINE("오프라인"), VIDEO("화상"), PHONE("전화");

        private final String description;

        ConsultType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static ConsultType fromValue(String value) {
            for (ConsultType type : values()) {
                if (type.description.equals(value)) {
                    return type;
                }
            }
            return ONLINE; // 기본값
        }
    }

    /**
     * 상담 상태 enum 정의
     */
    public enum ConsultStatus {
        REQUESTED("예약신청"), CONFIRMED("예약확정"), IN_PROGRESS("상담중"), 
        COMPLETED("상담완료"), CANCELLED("취소"), NO_SHOW("노쇼");

        private final String value;

        ConsultStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static ConsultStatus fromValue(String value) {
            for (ConsultStatus status : values()) {
                if (status.value.equals(value)) {
                    return status;
                }
            }
            return REQUESTED; // 기본값
        }
    }

    /**
     * 상담 완료 여부 확인
     */
    public boolean isCompleted() {
        return "상담완료".equals(this.consultStatus);
    }

    /**
     * 상담 진행 중 여부 확인
     */
    public boolean isInProgress() {
        return "상담중".equals(this.consultStatus);
    }

    /**
     * 화상 상담 여부 확인
     */
    public boolean isVideoConsult() {
        return "화상".equals(this.consultType);
    }

    /**
     * 상담 시작
     */
    public void startConsult() {
        this.consultStatus = "상담중";
        this.consultDatetime = LocalDateTime.now();
    }

    /**
     * 상담 완료
     */
    public void completeConsult(String result, String detail) {
        this.consultStatus = "상담완료";
        this.consultResult = result;
        this.detail = detail;
    }

    /**
     * 상담 취소
     */
    public void cancelConsult() {
        this.consultStatus = "취소";
    }

    /**
     * 10분 전 알림 발송 완료 표시
     */
    public void markNotification10minSent() {
        this.notificationSent10min = true;
    }

    /**
     * 정각 알림 발송 완료 표시
     */
    public void markNotificationOntimeSent() {
        this.notificationSentOntime = true;
    }
}
