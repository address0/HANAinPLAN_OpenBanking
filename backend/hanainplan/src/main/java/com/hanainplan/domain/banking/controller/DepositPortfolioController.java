package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.DepositPortfolioDto;
import com.hanainplan.domain.banking.service.DepositPortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/banking/portfolio")
@Tag(name = "정기예금 포트폴리오", description = "정기예금 가입 내역 조회 API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class DepositPortfolioController {

    private final DepositPortfolioService depositPortfolioService;

    @GetMapping("/user/{userId}")
    @Operation(summary = "사용자 정기예금 포트폴리오 조회", 
               description = "사용자의 모든 정기예금 가입 내역을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getUserDepositPortfolio(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {

        log.info("사용자 정기예금 포트폴리오 조회 - 사용자 ID: {}", userId);

        try {
            List<DepositPortfolioDto> portfolios = depositPortfolioService.getUserPortfolio(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("portfolioCount", portfolios.size());
            response.put("portfolios", portfolios);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("포트폴리오 조회 실패 - 사용자 ID: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "포트폴리오 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}/active")
    @Operation(summary = "사용자 활성 정기예금 포트폴리오 조회", 
               description = "사용자의 활성 상태인 정기예금만 조회합니다.")
    public ResponseEntity<Map<String, Object>> getActiveDepositPortfolio(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {

        log.info("사용자 활성 정기예금 포트폴리오 조회 - 사용자 ID: {}", userId);

        try {
            List<DepositPortfolioDto> portfolios = depositPortfolioService.getActivePortfolio(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("portfolioCount", portfolios.size());
            response.put("portfolios", portfolios);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("활성 포트폴리오 조회 실패 - 사용자 ID: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "활성 포트폴리오 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/user/{userId}/summary")
    @Operation(summary = "사용자 포트폴리오 요약 정보", 
               description = "사용자의 정기예금 포트폴리오 요약 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getPortfolioSummary(
            @Parameter(description = "사용자 ID") @PathVariable Long userId) {

        log.info("사용자 포트폴리오 요약 정보 조회 - 사용자 ID: {}", userId);

        try {
            Map<String, Object> summary = depositPortfolioService.getPortfolioSummary(userId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.putAll(summary);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("포트폴리오 요약 조회 실패 - 사용자 ID: {}", userId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "포트폴리오 요약 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/{portfolioId}")
    @Operation(summary = "포트폴리오 상세 조회", 
               description = "특정 포트폴리오의 상세 정보를 조회합니다.")
    public ResponseEntity<Map<String, Object>> getPortfolioDetail(
            @Parameter(description = "포트폴리오 ID") @PathVariable Long portfolioId) {

        log.info("포트폴리오 상세 조회 - 포트폴리오 ID: {}", portfolioId);

        try {
            DepositPortfolioDto portfolio = depositPortfolioService.getPortfolioDetail(portfolioId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("portfolio", portfolio);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("포트폴리오 상세 조회 실패 - 포트폴리오 ID: {}", portfolioId, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "포트폴리오 상세 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/irp/{irpAccountNumber}")
    @Operation(summary = "IRP 계좌 포트폴리오 조회", 
               description = "IRP 계좌번호로 해당 계좌에서 가입한 모든 금융상품을 조회합니다.")
    public ResponseEntity<Map<String, Object>> getIrpPortfolio(
            @Parameter(description = "IRP 계좌번호") @PathVariable String irpAccountNumber) {

        log.info("IRP 계좌 포트폴리오 조회 - IRP 계좌번호: {}", irpAccountNumber);

        try {
            List<DepositPortfolioDto> portfolios = depositPortfolioService.getPortfolioByIrpAccount(irpAccountNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("irpAccountNumber", irpAccountNumber);
            response.put("portfolioCount", portfolios.size());
            response.put("portfolios", portfolios);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("IRP 계좌 포트폴리오 조회 실패 - IRP 계좌번호: {}", irpAccountNumber, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "IRP 계좌 포트폴리오 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @GetMapping("/irp/{irpAccountNumber}/active")
    @Operation(summary = "IRP 계좌 활성 포트폴리오 조회", 
               description = "IRP 계좌번호로 운용중인 금융상품만 조회합니다.")
    public ResponseEntity<Map<String, Object>> getActiveIrpPortfolio(
            @Parameter(description = "IRP 계좌번호") @PathVariable String irpAccountNumber) {

        log.info("IRP 계좌 활성 포트폴리오 조회 - IRP 계좌번호: {}", irpAccountNumber);

        try {
            List<DepositPortfolioDto> portfolios = depositPortfolioService.getActivePortfolioByIrpAccount(irpAccountNumber);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("irpAccountNumber", irpAccountNumber);
            response.put("portfolioCount", portfolios.size());
            response.put("portfolios", portfolios);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("IRP 계좌 활성 포트폴리오 조회 실패 - IRP 계좌번호: {}", irpAccountNumber, e);
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "IRP 계좌 활성 포트폴리오 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}