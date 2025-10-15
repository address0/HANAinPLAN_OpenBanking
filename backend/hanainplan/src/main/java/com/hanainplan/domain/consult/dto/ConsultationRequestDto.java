package com.hanainplan.domain.consult.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

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
    private String consultationType;

    @NotNull(message = "예약 시간은 필수입니다.")
    private LocalDateTime reservationDatetime;

    private String detail;
}