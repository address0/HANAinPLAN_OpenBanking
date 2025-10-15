package com.hanainplan.domain.consult.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tb_consultation_note")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultationNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "note_id")
    private Long noteId;

    @Column(name = "consult_id", length = 20, nullable = false)
    private String consultId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "note_type", length = 20, nullable = false)
    private String noteType;

    @Column(name = "content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now();

    public enum NoteType {
        PERSONAL("개인 메모"), SHARED("공유 메모");

        private final String description;

        NoteType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }

        public static NoteType fromValue(String value) {
            for (NoteType type : values()) {
                if (type.name().equals(value)) {
                    return type;
                }
            }
            return PERSONAL;
        }
    }

    public void updateContent(String newContent) {
        this.content = newContent;
        this.updatedDate = LocalDateTime.now();
    }
}