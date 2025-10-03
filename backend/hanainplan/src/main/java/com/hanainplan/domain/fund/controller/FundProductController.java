package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.dto.FundProductDto;
import com.hanainplan.domain.fund.service.FundProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 하나인플랜 펀드 상품 컨트롤러
 */
@RestController
@RequestMapping("/api/banking/fund-products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fund Products", description = "펀드 상품 조회 API")
@CrossOrigin(origins = "*")
public class FundProductController {

    private final FundProductService fundProductService;

    /**
     * 모든 활성 펀드 상품 조회
     */
    @GetMapping
    @Operation(summary = "펀드 상품 목록 조회", description = "모든 활성 펀드 상품을 조회합니다")
    public ResponseEntity<List<FundProductDto>> getAllFundProducts() {
        log.info("GET /api/banking/fund-products - 펀드 상품 목록 조회");
        
        List<FundProductDto> products = fundProductService.getAllFundProducts();
        
        log.info("펀드 상품 목록 조회 완료 - {}건", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * 펀드 코드로 상품 상세 조회
     */
    @GetMapping("/{fundCode}")
    @Operation(summary = "펀드 상품 상세 조회", description = "펀드 코드로 상품 상세 정보를 조회합니다")
    public ResponseEntity<FundProductDto> getFundProduct(
            @Parameter(description = "펀드 코드", required = true, example = "HANA_STOCK_001")
            @PathVariable String fundCode
    ) {
        log.info("GET /api/banking/fund-products/{} - 펀드 상품 상세 조회", fundCode);
        
        FundProductDto product = fundProductService.getFundProductByCode(fundCode);
        
        log.info("펀드 상품 상세 조회 완료 - fundCode: {}, fundName: {}", 
                product.getFundCode(), product.getFundName());
        return ResponseEntity.ok(product);
    }

    /**
     * 펀드 유형별 조회
     */
    @GetMapping("/type/{fundType}")
    @Operation(summary = "펀드 유형별 조회", description = "펀드 유형으로 상품을 조회합니다 (주식형, 채권형, 혼합형, MMF 등)")
    public ResponseEntity<List<FundProductDto>> getFundProductsByType(
            @Parameter(description = "펀드 유형", required = true, example = "주식형")
            @PathVariable String fundType
    ) {
        log.info("GET /api/banking/fund-products/type/{} - 펀드 유형별 조회", fundType);
        
        List<FundProductDto> products = fundProductService.getFundProductsByType(fundType);
        
        log.info("펀드 유형별 조회 완료 - fundType: {}, 결과: {}건", fundType, products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * 위험등급별 조회
     */
    @GetMapping("/risk/{riskLevel}")
    @Operation(summary = "위험등급별 조회", description = "위험등급으로 펀드를 조회합니다 (1: 매우높음 ~ 5: 매우낮음)")
    public ResponseEntity<List<FundProductDto>> getFundProductsByRiskLevel(
            @Parameter(description = "위험등급 (1~5)", required = true, example = "3")
            @PathVariable String riskLevel
    ) {
        log.info("GET /api/banking/fund-products/risk/{} - 위험등급별 조회", riskLevel);
        
        List<FundProductDto> products = fundProductService.getFundProductsByRiskLevel(riskLevel);
        
        log.info("위험등급별 조회 완료 - riskLevel: {}, 결과: {}건", riskLevel, products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * IRP 편입 가능 펀드 조회
     */
    @GetMapping("/irp-eligible")
    @Operation(summary = "IRP 편입 가능 펀드 조회", description = "IRP 계좌로 투자 가능한 펀드를 조회합니다")
    public ResponseEntity<List<FundProductDto>> getIrpEligibleFunds() {
        log.info("GET /api/banking/fund-products/irp-eligible - IRP 편입 가능 펀드 조회");
        
        List<FundProductDto> products = fundProductService.getIrpEligibleFunds();
        
        log.info("IRP 편입 가능 펀드 조회 완료 - 결과: {}건", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * 수익률 상위 펀드 조회
     */
    @GetMapping("/top-performing")
    @Operation(summary = "수익률 상위 펀드 조회", description = "1년 수익률 기준 상위 펀드를 조회합니다")
    public ResponseEntity<List<FundProductDto>> getTopPerformingFunds() {
        log.info("GET /api/banking/fund-products/top-performing - 수익률 상위 펀드 조회");
        
        List<FundProductDto> products = fundProductService.getTopPerformingFunds();
        
        log.info("수익률 상위 펀드 조회 완료 - 결과: {}건", products.size());
        return ResponseEntity.ok(products);
    }

    /**
     * 복합 필터링 조회
     */
    @GetMapping("/filter")
    @Operation(summary = "복합 필터링 조회", description = "여러 조건으로 펀드를 필터링합니다")
    public ResponseEntity<List<FundProductDto>> filterFunds(
            @Parameter(description = "펀드 유형", example = "주식형")
            @RequestParam(required = false) String fundType,
            
            @Parameter(description = "위험등급 (1~5)", example = "3")
            @RequestParam(required = false) String riskLevel,
            
            @Parameter(description = "투자 지역", example = "국내")
            @RequestParam(required = false) String investmentRegion,
            
            @Parameter(description = "IRP 편입 가능 여부", example = "true")
            @RequestParam(required = false) Boolean isIrpEligible
    ) {
        log.info("GET /api/banking/fund-products/filter - 복합 필터링 조회");
        
        List<FundProductDto> products = fundProductService.filterFunds(
                fundType, riskLevel, investmentRegion, isIrpEligible);
        
        log.info("복합 필터링 조회 완료 - 결과: {}건", products.size());
        return ResponseEntity.ok(products);
    }
}

