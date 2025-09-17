package com.hanainplan.domain.banking.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_banking_auto_transfer")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AutoTransfer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "auto_transfer_id")
    private Long autoTransferId;
    
    // 출금 계좌 ID (외래키 제약조건 제거)
    @Column(name = "from_account_id", nullable = false)
    private Long fromAccountId;
    
    // 입금 계좌 ID (외래키 제약조건 제거)
    @Column(name = "to_account_id", nullable = false)
    private Long toAccountId;
    
    // 자동이체명
    @Column(name = "transfer_name", nullable = false, length = 100)
    private String transferName;
    
    // 이체 금액
    @Column(name = "amount", nullable = false, precision = 15, scale = 2)
    private BigDecimal amount;
    
    // 이체 주기
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_cycle", nullable = false)
    private TransferCycle transferCycle;
    
    // 이체일 (월별 이체일)
    @Column(name = "transfer_day")
    private Integer transferDay;
    
    // 이체 요일 (주별 이체일)
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_weekday")
    private Weekday transferWeekday;
    
    // 시작일
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;
    
    // 종료일
    @Column(name = "end_date")
    private LocalDate endDate;
    
    // 자동이체 상태
    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false)
    private TransferStatus transferStatus;
    
    // 상대방 계좌번호
    @Column(name = "counterpart_account_number", length = 20)
    private String counterpartAccountNumber;
    
    // 상대방 이름
    @Column(name = "counterpart_name", length = 50)
    private String counterpartName;
    
    // 상대방 은행 코드
    @Column(name = "counterpart_bank_code", length = 10)
    private String counterpartBankCode;
    
    // 이체 설명
    @Column(name = "description", length = 200)
    private String description;
    
    // 마지막 이체일
    @Column(name = "last_transfer_date")
    private LocalDate lastTransferDate;
    
    // 다음 이체일
    @Column(name = "next_transfer_date")
    private LocalDate nextTransferDate;
    
    // 이체 횟수
    @Column(name = "transfer_count", nullable = false)
    private Integer transferCount;
    
    // 최대 이체 횟수 (무제한: null)
    @Column(name = "max_transfer_count")
    private Integer maxTransferCount;
    
    // 이체 실패 횟수
    @Column(name = "failure_count", nullable = false)
    private Integer failureCount;
    
    // 최대 실패 허용 횟수
    @Column(name = "max_failure_count", nullable = false)
    private Integer maxFailureCount;
    
    // 생성일시
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    // 수정일시
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // 이체 주기 열거형
    public enum TransferCycle {
        DAILY("매일"),
        WEEKLY("매주"),
        MONTHLY("매월"),
        QUARTERLY("분기별"),
        YEARLY("연간");
        
        private final String description;
        
        TransferCycle(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 요일 열거형
    public enum Weekday {
        MONDAY("월요일"),
        TUESDAY("화요일"),
        WEDNESDAY("수요일"),
        THURSDAY("목요일"),
        FRIDAY("금요일"),
        SATURDAY("토요일"),
        SUNDAY("일요일");
        
        private final String description;
        
        Weekday(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 자동이체 상태 열거형
    public enum TransferStatus {
        ACTIVE("활성"),
        INACTIVE("비활성"),
        SUSPENDED("정지"),
        COMPLETED("완료"),
        CANCELLED("취소");
        
        private final String description;
        
        TransferStatus(String description) {
            this.description = description;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    // 다음 이체일 계산
    public void calculateNextTransferDate() {
        LocalDate today = LocalDate.now();
        LocalDate nextDate = null;
        
        switch (this.transferCycle) {
            case DAILY:
                nextDate = today.plusDays(1);
                break;
            case WEEKLY:
                if (this.transferWeekday != null) {
                    int daysUntilNext = (this.transferWeekday.ordinal() - today.getDayOfWeek().getValue() + 7) % 7;
                    if (daysUntilNext == 0) daysUntilNext = 7; // 같은 요일이면 다음 주
                    nextDate = today.plusDays(daysUntilNext);
                }
                break;
            case MONTHLY:
                if (this.transferDay != null) {
                    nextDate = today.withDayOfMonth(this.transferDay);
                    if (nextDate.isBefore(today) || nextDate.isEqual(today)) {
                        nextDate = nextDate.plusMonths(1);
                    }
                }
                break;
            case QUARTERLY:
                nextDate = today.plusMonths(3);
                break;
            case YEARLY:
                nextDate = today.plusYears(1);
                break;
        }
        
        this.nextTransferDate = nextDate;
    }
    
    // 이체 실행 가능 여부 확인
    public boolean canExecute() {
        return this.transferStatus == TransferStatus.ACTIVE &&
               this.nextTransferDate != null &&
               LocalDate.now().isEqual(this.nextTransferDate) &&
               (this.maxTransferCount == null || this.transferCount < this.maxTransferCount) &&
               this.failureCount < this.maxFailureCount;
    }
    
    // 이체 실행 후 상태 업데이트
    public void executeTransfer() {
        this.lastTransferDate = LocalDate.now();
        this.transferCount++;
        this.failureCount = 0; // 성공 시 실패 횟수 초기화
        calculateNextTransferDate();
        
        // 최대 이체 횟수 도달 시 완료 처리
        if (this.maxTransferCount != null && this.transferCount >= this.maxTransferCount) {
            this.transferStatus = TransferStatus.COMPLETED;
        }
    }
    
    // 이체 실패 처리
    public void failTransfer() {
        this.failureCount++;
        
        // 최대 실패 횟수 도달 시 정지 처리
        if (this.failureCount >= this.maxFailureCount) {
            this.transferStatus = TransferStatus.SUSPENDED;
        }
    }
}
