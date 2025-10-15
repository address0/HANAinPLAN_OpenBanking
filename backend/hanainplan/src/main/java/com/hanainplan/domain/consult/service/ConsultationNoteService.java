package com.hanainplan.domain.consult.service;

import com.hanainplan.domain.consult.dto.ConsultationNoteDto;
import com.hanainplan.domain.consult.dto.SaveConsultationNoteRequest;
import com.hanainplan.domain.consult.entity.ConsultationNote;
import com.hanainplan.domain.consult.repository.ConsultationNoteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ConsultationNoteService {

    private final ConsultationNoteRepository consultationNoteRepository;

    public ConsultationNoteDto saveOrUpdateNote(SaveConsultationNoteRequest request) {
        log.info("상담 메모 저장/업데이트 요청: consultId={}, userId={}, noteType={}", 
                request.getConsultId(), request.getUserId(), request.getNoteType());

        Optional<ConsultationNote> existingNote = consultationNoteRepository
                .findByConsultIdAndUserIdAndNoteType(
                        request.getConsultId(), 
                        request.getUserId(), 
                        request.getNoteType()
                );

        ConsultationNote note;
        if (existingNote.isPresent()) {
            note = existingNote.get();
            note.updateContent(request.getContent());
            log.info("기존 메모 업데이트: noteId={}", note.getNoteId());
        } else {
            note = ConsultationNote.builder()
                    .consultId(request.getConsultId())
                    .userId(request.getUserId())
                    .noteType(request.getNoteType())
                    .content(request.getContent())
                    .createdDate(LocalDateTime.now())
                    .updatedDate(LocalDateTime.now())
                    .build();
            log.info("새 메모 생성");
        }

        ConsultationNote savedNote = consultationNoteRepository.save(note);
        return convertToDto(savedNote);
    }

    @Transactional(readOnly = true)
    public List<ConsultationNoteDto> getNotesByConsultId(String consultId) {
        log.info("상담 메모 조회: consultId={}", consultId);

        List<ConsultationNote> notes = consultationNoteRepository.findByConsultId(consultId);
        return notes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ConsultationNoteDto getSharedNoteByConsultId(String consultId) {
        log.info("공유 메모 조회: consultId={}", consultId);

        Optional<ConsultationNote> sharedNote = consultationNoteRepository
                .findByConsultIdAndNoteType(consultId, "SHARED");

        return sharedNote.map(this::convertToDto).orElse(null);
    }

    @Transactional(readOnly = true)
    public ConsultationNoteDto getNoteByConsultIdAndUserIdAndType(String consultId, Long userId, String noteType) {
        log.info("사용자 메모 조회: consultId={}, userId={}, noteType={}", consultId, userId, noteType);

        Optional<ConsultationNote> note = consultationNoteRepository
                .findByConsultIdAndUserIdAndNoteType(consultId, userId, noteType);

        return note.map(this::convertToDto).orElse(null);
    }

    @Transactional(readOnly = true)
    public List<ConsultationNoteDto> getSharedNotesByConsultId(String consultId) {
        log.info("공유 메모 조회: consultId={}", consultId);

        List<ConsultationNote> notes = consultationNoteRepository.findSharedNotesByConsultId(consultId);
        return notes.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private ConsultationNoteDto convertToDto(ConsultationNote note) {
        return ConsultationNoteDto.builder()
                .noteId(note.getNoteId())
                .consultId(note.getConsultId())
                .userId(note.getUserId())
                .noteType(note.getNoteType())
                .content(note.getContent())
                .createdDate(note.getCreatedDate())
                .updatedDate(note.getUpdatedDate())
                .build();
    }
}