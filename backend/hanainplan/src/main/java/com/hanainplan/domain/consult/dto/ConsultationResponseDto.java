package com.hanainplan.domain.consult.dto;

import com.hanainplan.domain.consult.entity.Consult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationResponseDto {

    private String consultId;
    private String consultType;
    private LocalDateTime reservationDatetime;
    private LocalDateTime consultDatetime;
    private String consultStatus;
    private String qrCode;
    private String consultUrl;
    private String detail;
    private String consultResult;
    private String branchCode;
    private String customerId;
    private String consultantId;

    private String customerName;
    private String consultantName;
    private String consultantDepartment;

    public static ConsultationResponseDto fromEntity(Consult consult) {
        if (consult == null) {
            return null;
        }

        return ConsultationResponseDto.builder()
                .consultId(consult.getConsultId())
                .consultType(consult.getConsultType())
                .reservationDatetime(consult.getReservationDatetime())
                .consultDatetime(consult.getConsultDatetime())
                .consultStatus(consult.getConsultStatus())
                .qrCode(consult.getQrCode())
                .consultUrl(consult.getConsultUrl())
                .detail(consult.getDetail())
                .consultResult(consult.getConsultResult())
                .branchCode(consult.getBranchCode())
                .customerId(consult.getCustomerId())
                .consultantId(consult.getConsultantId())
                .build();
    }
}