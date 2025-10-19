package com.hanainplan.hana.irp.controller;

import com.hanainplan.hana.irp.dto.IrpAccountBalanceResponseDto;
import com.hanainplan.hana.irp.dto.IrpDepositHoldingsResponseDto;
import com.hanainplan.hana.irp.dto.IrpFundHoldingsResponseDto;
import com.hanainplan.hana.irp.service.IrpAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hana/irp")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hana IRP Account", description = "하나은행 IRP 계좌 관리 API")
public class IrpAccountController {

    private final IrpAccountService irpAccountService;

    @GetMapping("/{accountNumber}/balance")
    @Operation(summary = "IRP 계좌 잔액 조회", description = "IRP 계좌의 현금 잔액을 조회합니다")
    public ResponseEntity<IrpAccountBalanceResponseDto> getAccountBalance(
            @Parameter(description = "IRP 계좌번호", required = true)
            @PathVariable String accountNumber
    ) {
        log.info("GET /api/hana/irp/{}/balance - IRP 계좌 잔액 조회", accountNumber);

        IrpAccountBalanceResponseDto response = irpAccountService.getAccountBalance(accountNumber);

        log.info("IRP 계좌 잔액 조회 완료 - 계좌번호: {}, 잔액: {}원", 
                accountNumber, response.getCashBalance());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{accountNumber}/deposits")
    @Operation(summary = "IRP 예금 보유 조회", description = "IRP 계좌의 예금 상품 보유 내역을 조회합니다")
    public ResponseEntity<List<IrpDepositHoldingsResponseDto>> getDepositHoldings(
            @Parameter(description = "IRP 계좌번호", required = true)
            @PathVariable String accountNumber
    ) {
        log.info("GET /api/hana/irp/{}/deposits - IRP 예금 보유 조회", accountNumber);

        List<IrpDepositHoldingsResponseDto> holdings = irpAccountService.getDepositHoldings(accountNumber);

        log.info("IRP 예금 보유 조회 완료 - 계좌번호: {}, 예금 상품 수: {}개", 
                accountNumber, holdings.size());
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/{accountNumber}/funds")
    @Operation(summary = "IRP 펀드 보유 조회", description = "IRP 계좌의 펀드 보유 내역을 조회합니다")
    public ResponseEntity<List<IrpFundHoldingsResponseDto>> getFundHoldings(
            @Parameter(description = "IRP 계좌번호", required = true)
            @PathVariable String accountNumber
    ) {
        log.info("GET /api/hana/irp/{}/funds - IRP 펀드 보유 조회", accountNumber);

        List<IrpFundHoldingsResponseDto> holdings = irpAccountService.getFundHoldings(accountNumber);

        log.info("IRP 펀드 보유 조회 완료 - 계좌번호: {}, 펀드 상품 수: {}개", 
                accountNumber, holdings.size());
        return ResponseEntity.ok(holdings);
    }

    @GetMapping("/{accountNumber}/portfolio")
    @Operation(summary = "IRP 포트폴리오 전체 조회", description = "IRP 계좌의 전체 포트폴리오를 조회합니다")
    public ResponseEntity<com.hanainplan.hana.irp.dto.IrpPortfolioResponseDto> getPortfolio(
            @Parameter(description = "IRP 계좌번호", required = true)
            @PathVariable String accountNumber
    ) {
        log.info("GET /api/hana/irp/{}/portfolio - IRP 포트폴리오 전체 조회", accountNumber);

        com.hanainplan.hana.irp.dto.IrpPortfolioResponseDto portfolio = 
                irpAccountService.getPortfolio(accountNumber);

        log.info("IRP 포트폴리오 조회 완료 - 계좌번호: {}, 총 자산: {}원", 
                accountNumber, portfolio.getTotalValue());
        return ResponseEntity.ok(portfolio);
    }
}




