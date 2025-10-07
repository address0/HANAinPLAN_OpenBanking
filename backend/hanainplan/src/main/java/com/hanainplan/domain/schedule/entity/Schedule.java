package com.hanainplan.domain.schedule.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * 일정 엔터티
 * - 상담사의 일정 관리
 * - 고객 상담, 회의 등 일정 저장
 */
@Entity
@Table(name = "tb_schedule")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Schedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    // 상담사 ID (외래키 제약조건 제거)
    @Column(name = "consultant_id", nullable = false)
    private Long consultantId;

    // 일정 제목
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    // 일정 설명
    @Column(name = "description", length = 1000)
    private String description;

    // 일정 유형
    @Enumerated(EnumType.STRING)
    @Column(name = "schedule_type", nullable = false)
    private ScheduleType scheduleType;

    // 고객명 (상담인 경우)
    @Column(name = "client_name", length = 50)
    private String clientName;

    // 고객 ID (상담인 경우)
    @Column(name = "client_id")
    private Long clientId;

    // 시작 시간
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    // 종료 시간
    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // 종일 일정 여부
    @Column(name = "is_all_day")
    @Builder.Default
    private Boolean isAllDay = false;

    // 일정 상태 (예정, 진행중, 완료, 취소)
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Builder.Default
    private ScheduleStatus status = ScheduleStatus.SCHEDULED;

    // 위치 정보
    @Column(name = "location", length = 200)
    private String location;

    // 메모
    @Column(name = "memo", length = 500)
    private String memo;

    // 생성 시간
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    // 수정 시간
    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    /**
     * 일정 상태 enum
     */
    public enum ScheduleStatus {
        SCHEDULED("예정"),
        IN_PROGRESS("진행중"),
        COMPLETED("완료"),
        CANCELLED("취소");

        private final String description;

        ScheduleStatus(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    /**
     * 일정 시간 유효성 검증
     */
    public boolean isValidTimeRange() {
        return startTime != null && endTime != null && startTime.isBefore(endTime);
    }

    /**
     * 일정이 현재 진행 중인지 확인
     */
    public boolean isInProgress() {
        LocalDateTime now = LocalDateTime.now();
        return startTime.isBefore(now) && endTime.isAfter(now);
    }

    /**
     * 일정이 완료되었는지 확인
     */
    public boolean isPast() {
        return endTime.isBefore(LocalDateTime.now());
    }

    /**
     * 일정이 예정되었는지 확인
     */
    public boolean isUpcoming() {
        return startTime.isAfter(LocalDateTime.now());
    }

    /**
     * 업데이트 시간 갱신
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

