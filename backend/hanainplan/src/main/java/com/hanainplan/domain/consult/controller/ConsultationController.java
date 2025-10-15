package com.hanainplan.domain.consult.controller;

import com.hanainplan.domain.consult.dto.ConsultationRequestDto;
import com.hanainplan.domain.consult.dto.ConsultationResponseDto;
import com.hanainplan.domain.consult.service.ConsultService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consultations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Consultation", description = "상담 관리 API")
@CrossOrigin(origins = "*")
public class ConsultationController {

    private final ConsultService consultService;

    @PostMapping
    @Operation(summary = "상담 신청", description = "고객이 특정 상담사에게 예약 상담을 신청합니다.")
    public ResponseEntity<?> createConsultation(@Valid @RequestBody ConsultationRequestDto request) {
        try {
            log.info("POST /api/consultations - customerId: {}, consultantId: {}", 
                    request.getCustomerId(), request.getConsultantId());

            ConsultationResponseDto response = consultService.createConsultation(request);

            return ResponseEntity.status(HttpStatus.CREATED).body(Map.of(
                "success", true,
                "consultation", response,
                "message", "상담 신청이 완료되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("상담 신청 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("상담 신청 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 신청 중 오류가 발생했습니다."
            ));
        }
    }

    @GetMapping("/customer/{customerId}")
    @Operation(summary = "고객 상담 목록 조회", description = "특정 고객의 모든 상담 내역을 조회합니다.")
    public ResponseEntity<List<ConsultationResponseDto>> getCustomerConsultations(
            @Parameter(description = "고객 ID", required = true)
            @PathVariable Long customerId
    ) {
        log.info("GET /api/consultations/customer/{} - customerId: {}", customerId, customerId);

        List<ConsultationResponseDto> consultations = consultService.getCustomerConsultations(customerId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/consultant/{consultantId}")
    @Operation(summary = "상담사 상담 목록 조회", description = "특정 상담사의 모든 상담 내역을 조회합니다.")
    public ResponseEntity<List<ConsultationResponseDto>> getConsultantConsultations(
            @Parameter(description = "상담사 ID", required = true)
            @PathVariable Long consultantId
    ) {
        log.info("GET /api/consultations/consultant/{} - consultantId: {}", consultantId, consultantId);

        List<ConsultationResponseDto> consultations = consultService.getConsultantConsultations(consultantId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/consultant/{consultantId}/today")
    @Operation(summary = "오늘 상담 조회", description = "상담사의 오늘 상담 목록을 조회합니다.")
    public ResponseEntity<List<ConsultationResponseDto>> getTodayConsultations(
            @Parameter(description = "상담사 ID", required = true)
            @PathVariable Long consultantId
    ) {
        log.info("GET /api/consultations/consultant/{}/today - consultantId: {}", consultantId, consultantId);

        List<ConsultationResponseDto> consultations = consultService.getTodayConsultations(consultantId);
        return ResponseEntity.ok(consultations);
    }

    @GetMapping("/consultant/{consultantId}/requests")
    @Operation(summary = "상담 요청 내역 조회", description = "상담사에게 들어온 예약 신청 목록을 조회합니다.")
    public ResponseEntity<List<ConsultationResponseDto>> getConsultationRequests(
            @Parameter(description = "상담사 ID", required = true)
            @PathVariable Long consultantId
    ) {
        log.info("GET /api/consultations/consultant/{}/requests - consultantId: {}", consultantId, consultantId);

        List<ConsultationResponseDto> requests = consultService.getConsultationRequests(consultantId);
        return ResponseEntity.ok(requests);
    }

    @PatchMapping("/{consultId}/status")
    @Operation(summary = "상담 상태 변경", description = "상담의 상태를 변경합니다.")
    public ResponseEntity<?> updateConsultationStatus(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable String consultId,

            @Parameter(description = "변경할 상태", required = true)
            @RequestParam String status
    ) {
        try {
            log.info("PATCH /api/consultations/{}/status - consultId: {}, status: {}", 
                    consultId, consultId, status);

            ConsultationResponseDto response = consultService.updateConsultationStatus(consultId, status);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "consultation", response,
                "message", "상담 상태가 변경되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("상담 상태 변경 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("상담 상태 변경 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 상태 변경 중 오류가 발생했습니다."
            ));
        }
    }

    @GetMapping("/{consultId}/details")
    @Operation(summary = "상담 상세 정보 조회", description = "화상 상담 입장을 위한 상담 상세 정보를 조회합니다.")
    public ResponseEntity<?> getConsultationDetails(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable String consultId
    ) {
        try {
            log.info("GET /api/consultations/{}/details - consultId: {}", consultId, consultId);

            ConsultationResponseDto response = consultService.getConsultationDetails(consultId);

            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("상담 상세 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("상담 상세 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 상세 정보 조회 중 오류가 발생했습니다."
            ));
        }
    }

    @PostMapping("/{consultId}/cancel")
    @Operation(summary = "상담 취소", description = "고객이 예약한 상담을 취소합니다.")
    public ResponseEntity<?> cancelConsultation(
            @Parameter(description = "상담 ID", required = true)
            @PathVariable String consultId,

            @Parameter(description = "고객 ID", required = true)
            @RequestParam Long customerId
    ) {
        try {
            log.info("POST /api/consultations/{}/cancel - consultId: {}, customerId: {}", 
                    consultId, consultId, customerId);

            ConsultationResponseDto response = consultService.cancelConsultation(consultId, customerId);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "consultation", response,
                "message", "상담이 취소되었습니다."
            ));
        } catch (IllegalArgumentException e) {
            log.error("상담 취소 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", e.getMessage()
            ));
        } catch (Exception e) {
            log.error("상담 취소 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "상담 취소 중 오류가 발생했습니다."
            ));
        }
    }
}