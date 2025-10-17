package com.hanainplan.domain.portfolio.controller;

import com.hanainplan.domain.portfolio.dto.IrpPortfolioResponse;
import com.hanainplan.domain.portfolio.entity.IrpHolding;
import com.hanainplan.domain.portfolio.service.IrpPortfolioService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/irp/portfolio")
@RequiredArgsConstructor
@Slf4j
public class IrpPortfolioController {

    private final IrpPortfolioService irpPortfolioService;

    /**
     * 고객의 IRP 포트폴리오 조회 (슬리브별 분리)
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<IrpPortfolioResponse> getIrpPortfolio(@PathVariable Long customerId) {
        log.info("IRP 포트폴리오 조회 요청 - 고객 ID: {}", customerId);

        try {
            IrpPortfolioResponse response = irpPortfolioService.getIrpPortfolio(customerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("IRP 포트폴리오 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("IRP 포트폴리오 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 IRP 계좌의 포트폴리오 조회
     */
    @GetMapping("/{customerId}/account/{irpAccountNumber}")
    public ResponseEntity<IrpPortfolioResponse> getIrpPortfolioByAccount(
            @PathVariable Long customerId,
            @PathVariable String irpAccountNumber) {
        
        log.info("IRP 계좌 포트폴리오 조회 요청 - 고객 ID: {}, 계좌번호: {}", customerId, irpAccountNumber);

        try {
            IrpPortfolioResponse response = irpPortfolioService.getIrpPortfolioByAccount(customerId, irpAccountNumber);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("IRP 계좌 포트폴리오 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("IRP 계좌 포트폴리오 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고객의 총 IRP 자산 가치 조회
     */
    @GetMapping("/{customerId}/total-value")
    public ResponseEntity<Double> getTotalIrpValue(@PathVariable Long customerId) {
        log.debug("고객 ID {} 총 IRP 자산 가치 조회", customerId);

        try {
            Double totalValue = irpPortfolioService.getTotalIrpValue(customerId);
            return ResponseEntity.ok(totalValue);
        } catch (Exception e) {
            log.error("총 IRP 자산 가치 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고객의 현금 잔액 조회
     */
    @GetMapping("/{customerId}/cash")
    public ResponseEntity<Double> getCashBalance(@PathVariable Long customerId) {
        log.debug("고객 ID {} 현금 잔액 조회", customerId);

        try {
            Double cashBalance = irpPortfolioService.getCashBalance(customerId);
            return ResponseEntity.ok(cashBalance);
        } catch (Exception e) {
            log.error("현금 잔액 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고객의 예금 총액 조회
     */
    @GetMapping("/{customerId}/deposit")
    public ResponseEntity<Double> getDepositTotal(@PathVariable Long customerId) {
        log.debug("고객 ID {} 예금 총액 조회", customerId);

        try {
            Double depositTotal = irpPortfolioService.getDepositTotal(customerId);
            return ResponseEntity.ok(depositTotal);
        } catch (Exception e) {
            log.error("예금 총액 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고객의 펀드 총액 조회
     */
    @GetMapping("/{customerId}/fund")
    public ResponseEntity<Double> getFundTotal(@PathVariable Long customerId) {
        log.debug("고객 ID {} 펀드 총액 조회", customerId);

        try {
            Double fundTotal = irpPortfolioService.getFundTotal(customerId);
            return ResponseEntity.ok(fundTotal);
        } catch (Exception e) {
            log.error("펀드 총액 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 고객의 펀드 비중 조회
     */
    @GetMapping("/{customerId}/fund-weight")
    public ResponseEntity<Double> getFundWeight(@PathVariable Long customerId) {
        log.debug("고객 ID {} 펀드 비중 조회", customerId);

        try {
            Double fundWeight = irpPortfolioService.getFundWeight(customerId);
            return ResponseEntity.ok(fundWeight);
        } catch (Exception e) {
            log.error("펀드 비중 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 펀드 비중 70% 상한 초과 여부 확인
     */
    @GetMapping("/{customerId}/fund-weight/exceeded")
    public ResponseEntity<Boolean> isFundWeightExceeded(@PathVariable Long customerId) {
        log.debug("고객 ID {} 펀드 비중 상한 초과 여부 확인", customerId);

        try {
            Boolean isExceeded = irpPortfolioService.isFundWeightExceeded(customerId);
            return ResponseEntity.ok(isExceeded);
        } catch (Exception e) {
            log.error("펀드 비중 상한 초과 여부 확인 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 만기된 예금 조회
     */
    @GetMapping("/{customerId}/matured-deposits")
    public ResponseEntity<List<IrpHolding>> getMaturedDeposits(@PathVariable Long customerId) {
        log.debug("고객 ID {} 만기된 예금 조회", customerId);

        try {
            List<IrpHolding> maturedDeposits = irpPortfolioService.getMaturedDeposits(customerId);
            return ResponseEntity.ok(maturedDeposits);
        } catch (Exception e) {
            log.error("만기된 예금 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 포트폴리오 요약 정보 조회
     */
    @GetMapping("/{customerId}/summary")
    public ResponseEntity<IrpPortfolioService.PortfolioSummary> getPortfolioSummary(@PathVariable Long customerId) {
        log.debug("고객 ID {} 포트폴리오 요약 조회", customerId);

        try {
            IrpPortfolioService.PortfolioSummary summary = irpPortfolioService.getPortfolioSummary(customerId);
            return ResponseEntity.ok(summary);
        } catch (Exception e) {
            log.error("포트폴리오 요약 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 특정 자산의 보유 정보 조회
     */
    @GetMapping("/{customerId}/asset/{assetCode}")
    public ResponseEntity<IrpHolding> getHoldingByAssetCode(
            @PathVariable Long customerId,
            @PathVariable String assetCode) {
        
        log.debug("고객 ID {} 자산코드 {} 보유 정보 조회", customerId, assetCode);

        try {
            IrpHolding holding = irpPortfolioService.getHoldingByAssetCode(customerId, assetCode);
            if (holding == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(holding);
        } catch (Exception e) {
            log.error("자산 보유 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * 동기화가 필요한 보유 자산 조회 (관리자용)
     */
    @GetMapping("/sync-needed")
    public ResponseEntity<List<IrpHolding>> getHoldingsNeedingSync() {
        log.debug("동기화가 필요한 보유 자산 조회");

        try {
            List<IrpHolding> holdings = irpPortfolioService.getHoldingsNeedingSync();
            return ResponseEntity.ok(holdings);
        } catch (Exception e) {
            log.error("동기화가 필요한 보유 자산 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
