package com.hanainplan.domain.banking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTransferResponseDto {
    
    private boolean success;
    private String message;
    private Long autoTransferId;
    private String transferName;
    private String transferStatus;
    private LocalDateTime createdAt;
    private String failureReason;
    
    // 성공 응답 생성
    public static AutoTransferResponseDto success(String message, Long autoTransferId, String transferName, String transferStatus) {
        return AutoTransferResponseDto.builder()
                .success(true)
                .message(message)
                .autoTransferId(autoTransferId)
                .transferName(transferName)
                .transferStatus(transferStatus)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    // 실패 응답 생성
    public static AutoTransferResponseDto failure(String message, String failureReason) {
        return AutoTransferResponseDto.builder()
                .success(false)
                .message(message)
                .failureReason(failureReason)
                .createdAt(LocalDateTime.now())
                .build();
    }
}
