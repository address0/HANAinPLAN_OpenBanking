package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.ConsultantDto;
import com.hanainplan.domain.user.entity.Consultant;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.ConsultantRepository;
import com.hanainplan.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/consultants")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultant", description = "상담사 관리 API")
@CrossOrigin(origins = "*")
public class ConsultantController {

    private final ConsultantRepository consultantRepository;
    private final UserRepository userRepository;

    @GetMapping
    @Operation(summary = "상담사 목록 조회", description = "모든 활성화된 상담사 목록을 조회합니다.")
    public ResponseEntity<List<ConsultantDto>> getAllConsultants() {
        log.info("GET /api/consultants - 전체 상담사 조회");

        List<Consultant> consultants = consultantRepository.findAll();
        List<ConsultantDto> consultantDtos = consultants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        return ResponseEntity.ok(consultantDtos);
    }

    @GetMapping("/available-at-time")
    @Operation(summary = "특정 시간대 가능한 상담사 조회", 
               description = "특정 시간대에 일정이 없는 상담사 목록을 조회합니다.")
    public ResponseEntity<List<ConsultantDto>> getAvailableConsultantsAtTime(
            @Parameter(description = "시작 시간 (ISO 8601 형식)", required = true, example = "2025-10-08T10:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,

            @Parameter(description = "종료 시간 (ISO 8601 형식)", required = true, example = "2025-10-08T11:00:00")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime
    ) {
        log.info("GET /api/consultants/available-at-time - startTime: {}, endTime: {}", startTime, endTime);

        if (startTime.isAfter(endTime) || startTime.isEqual(endTime)) {
            log.warn("잘못된 시간 범위: startTime={}, endTime={}", startTime, endTime);
            throw new IllegalArgumentException("시작 시간은 종료 시간보다 이전이어야 합니다.");
        }

        List<Consultant> availableConsultants = consultantRepository.findAvailableConsultantsAtTime(
                startTime, endTime
        );

        List<ConsultantDto> consultantDtos = availableConsultants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("가능한 상담사 수: {}", consultantDtos.size());
        return ResponseEntity.ok(consultantDtos);
    }

    @GetMapping("/available")
    @Operation(summary = "현재 상담 가능한 상담사 조회", 
               description = "현재 온라인 상태이고 상담 가능한 상담사 목록을 조회합니다.")
    public ResponseEntity<List<ConsultantDto>> getAvailableConsultants() {
        log.info("GET /api/consultants/available - 상담 가능한 상담사 조회");

        List<Consultant> availableConsultants = consultantRepository.findAvailableConsultants();
        List<ConsultantDto> consultantDtos = availableConsultants.stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        log.info("상담 가능한 상담사 수: {}", consultantDtos.size());
        return ResponseEntity.ok(consultantDtos);
    }

    @GetMapping("/{consultantId}")
    @Operation(summary = "상담사 상세 조회", description = "특정 상담사의 상세 정보를 조회합니다.")
    public ResponseEntity<ConsultantDto> getConsultant(
            @Parameter(description = "상담사 ID", required = true)
            @PathVariable Long consultantId
    ) {
        log.info("GET /api/consultants/{} - consultantId: {}", consultantId, consultantId);

        Consultant consultant = consultantRepository.findById(consultantId)
                .orElseThrow(() -> new IllegalArgumentException("상담사를 찾을 수 없습니다. ID: " + consultantId));

        ConsultantDto consultantDto = convertToDto(consultant);
        return ResponseEntity.ok(consultantDto);
    }

    private ConsultantDto convertToDto(Consultant consultant) {
        ConsultantDto dto = ConsultantDto.fromEntity(consultant);

        userRepository.findById(consultant.getConsultantId()).ifPresent(user -> {
            dto.setUserName(user.getUserName());
            dto.setPhoneNumber(user.getPhoneNumber());
            dto.setEmail(user.getEmail());
        });

        return dto;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("잘못된 요청: {}", e.getMessage());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception e) {
        log.error("서버 오류: {}", e.getMessage(), e);
        return ResponseEntity.status(500).body("서버 오류가 발생했습니다: " + e.getMessage());
    }
}