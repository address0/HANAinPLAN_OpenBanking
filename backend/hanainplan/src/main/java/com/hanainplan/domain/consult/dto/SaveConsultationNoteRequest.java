package com.hanainplan.domain.consult.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaveConsultationNoteRequest {

    private String consultId;
    private Long userId;
    private String noteType;
    private String content;
}