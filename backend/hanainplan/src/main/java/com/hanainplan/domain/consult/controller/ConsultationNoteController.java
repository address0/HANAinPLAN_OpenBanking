package com.hanainplan.domain.consult.controller;

import com.hanainplan.domain.consult.dto.ConsultationNoteDto;
import com.hanainplan.domain.consult.dto.SaveConsultationNoteRequest;
import com.hanainplan.domain.consult.service.ConsultationNoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/consultation")
@RequiredArgsConstructor
@Slf4j
public class ConsultationNoteController {

    private final ConsultationNoteService consultationNoteService;

    @PostMapping("/notes")
    public ResponseEntity<ConsultationNoteDto> saveNote(@RequestBody SaveConsultationNoteRequest request) {
        try {
            log.info("상담 메모 저장 요청: {}", request);
            ConsultationNoteDto savedNote = consultationNoteService.saveOrUpdateNote(request);
            return ResponseEntity.ok(savedNote);
        } catch (Exception e) {
            log.error("상담 메모 저장 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{consultId}/notes")
    public ResponseEntity<List<ConsultationNoteDto>> getNotes(@PathVariable String consultId) {
        try {
            log.info("상담 메모 조회 요청: consultId={}", consultId);
            List<ConsultationNoteDto> notes = consultationNoteService.getNotesByConsultId(consultId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("상담 메모 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{consultId}/notes/user/{userId}/type/{noteType}")
    public ResponseEntity<ConsultationNoteDto> getUserNote(
            @PathVariable String consultId,
            @PathVariable Long userId,
            @PathVariable String noteType) {
        try {
            log.info("사용자 메모 조회 요청: consultId={}, userId={}, noteType={}", consultId, userId, noteType);
            ConsultationNoteDto note = consultationNoteService.getNoteByConsultIdAndUserIdAndType(consultId, userId, noteType);
            return ResponseEntity.ok(note);
        } catch (Exception e) {
            log.error("사용자 메모 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{consultId}/notes/shared")
    public ResponseEntity<List<ConsultationNoteDto>> getSharedNotes(@PathVariable String consultId) {
        try {
            log.info("공유 메모 조회 요청: consultId={}", consultId);
            List<ConsultationNoteDto> notes = consultationNoteService.getSharedNotesByConsultId(consultId);
            return ResponseEntity.ok(notes);
        } catch (Exception e) {
            log.error("공유 메모 조회 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}