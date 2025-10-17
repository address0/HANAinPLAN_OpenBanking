package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.RiskProfileRequest;
import com.hanainplan.domain.user.dto.RiskProfileResponse;
import com.hanainplan.domain.user.entity.RiskProfileAnswer;
import com.hanainplan.domain.user.service.RiskProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/risk-profile")
@RequiredArgsConstructor
@Slf4j
public class RiskProfileController {

    private final RiskProfileService riskProfileService;

    @PostMapping("/evaluate")
    public ResponseEntity<RiskProfileResponse> evaluateRiskProfile(
            @RequestBody RiskProfileRequest request) {
        
        log.info("리스크 프로파일 평가 요청 - 고객 ID: {}", request.getCustomerId());
        
        try {
            RiskProfileResponse response = riskProfileService.evaluateRiskProfile(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리스크 프로파일 평가 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("리스크 프로파일 평가 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{customerId}")
    public ResponseEntity<RiskProfileResponse> getRiskProfile(
            @PathVariable Long customerId) {
        
        log.info("리스크 프로파일 조회 요청 - 고객 ID: {}", customerId);
        
        try {
            RiskProfileResponse response = riskProfileService.getRiskProfile(customerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("리스크 프로파일 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("리스크 프로파일 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{customerId}/answers")
    public ResponseEntity<List<RiskProfileAnswer>> getRiskProfileAnswers(
            @PathVariable Long customerId) {
        
        log.info("리스크 프로파일 답변 이력 조회 요청 - 고객 ID: {}", customerId);
        
        try {
            List<RiskProfileAnswer> answers = riskProfileService.getRiskProfileAnswers(customerId);
            return ResponseEntity.ok(answers);
        } catch (Exception e) {
            log.error("리스크 프로파일 답변 이력 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/questions")
    public ResponseEntity<List<String>> getQuestions() {
        log.info("리스크 프로파일 질문 목록 조회 요청");
        
        try {
            List<String> questions = riskProfileService.getQuestionTexts();
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            log.error("질문 목록 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/questions/{questionNumber}/options")
    public ResponseEntity<List<String>> getAnswerOptions(
            @PathVariable Integer questionNumber) {
        
        log.info("질문 {}번 답변 옵션 조회 요청", questionNumber);
        
        try {
            List<String> options = riskProfileService.getAnswerOptions(questionNumber);
            return ResponseEntity.ok(options);
        } catch (IllegalArgumentException e) {
            log.error("답변 옵션 조회 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("답변 옵션 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @PostMapping("/questions/{questionNumber}/answers/{answerScore}")
    public ResponseEntity<RiskProfileRequest.QuestionAnswer> createQuestionAnswer(
            @PathVariable Integer questionNumber,
            @PathVariable Integer answerScore) {
        
        log.info("질문 답변 생성 요청 - 질문: {}, 답변: {}", questionNumber, answerScore);
        
        try {
            RiskProfileRequest.QuestionAnswer answer = 
                riskProfileService.createQuestionAnswer(questionNumber, answerScore);
            return ResponseEntity.ok(answer);
        } catch (IllegalArgumentException e) {
            log.error("질문 답변 생성 실패: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            log.error("질문 답변 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
