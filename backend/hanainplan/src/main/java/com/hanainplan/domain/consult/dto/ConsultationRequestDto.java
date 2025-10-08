package com.hanainplan.domain.consult.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

/**
 * 상담 신청 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationRequestDto {

    @NotNull(message = "고객 ID는 필수입니다.")
    private Long customerId;
    
    @NotNull(message = "상담사 ID는 필수입니다.")
    private Long consultantId;
    
    @NotNull(message = "상담 유형은 필수입니다.")
    private String consultationType; // 일반, 상품가입, 자산관리
    
    @NotNull(message = "예약 시간은 필수입니다.")
    private LocalDateTime reservationDatetime;
    
    private String detail; // 상담 요청 상세 내용
}

