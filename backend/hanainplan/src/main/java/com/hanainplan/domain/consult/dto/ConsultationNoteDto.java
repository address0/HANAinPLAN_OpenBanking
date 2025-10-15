package com.hanainplan.domain.consult.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationNoteDto {

    private Long noteId;
    private String consultId;
    private Long userId;
    private String noteType;
    private String content;
    private LocalDateTime createdDate;
    private LocalDateTime updatedDate;
}