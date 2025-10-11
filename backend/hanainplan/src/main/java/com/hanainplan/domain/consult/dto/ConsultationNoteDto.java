package com.hanainplan.domain.consult.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 상담 메모 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationNoteDto {

    private Long noteId;
    private String consultId;
    private Long userId;
    private String noteType; // "PERSONAL" 또는 "SHARED"
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}
