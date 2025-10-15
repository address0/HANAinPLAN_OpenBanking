package com.hanainplan.hana.fund.controller;

import com.hanainplan.hana.fund.dto.FundProductResponseDto;
import com.hanainplan.hana.fund.entity.FundProduct;
import com.hanainplan.hana.fund.service.FundProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/hana/fund-products")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Hana Fund Products", description = "하나은행 펀드 상품 API")
public class FundProductController {

    private final FundProductService fundProductService;

    @GetMapping
    @Operation(summary = "펀드 상품 목록 조회", description = "모든 활성 펀드 상품을 조회합니다")
    public ResponseEntity<List<FundProductResponseDto>> getAllFundProducts() {
        log.info("GET /api/hana/fund-products - 펀드 상품 목록 조회");

        List<FundProduct> products = fundProductService.getAllActiveFundProducts();
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("펀드 상품 목록 조회 완료 - {}건", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{fundCode}")
    @Operation(summary = "펀드 상품 상세 조회", description = "펀드 코드로 상품 상세 정보를 조회합니다")
    public ResponseEntity<FundProductResponseDto> getFundProduct(
            @Parameter(description = "펀드 코드", required = true)
            @PathVariable String fundCode
    ) {
        log.info("GET /api/hana/fund-products/{} - 펀드 상품 상세 조회", fundCode);

        FundProduct product = fundProductService.getFundProductByCode(fundCode)
                .orElseThrow(() -> new IllegalArgumentException("펀드 상품을 찾을 수 없습니다: " + fundCode));

        FundProductResponseDto response = FundProductResponseDto.fromEntity(product);

        log.info("펀드 상품 상세 조회 완료 - fundCode: {}, fundName: {}", 
                response.getFundCode(), response.getFundName());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/type/{fundType}")
    @Operation(summary = "펀드 유형별 조회", description = "펀드 유형으로 상품을 조회합니다 (주식형, 채권형, 혼합형, MMF 등)")
    public ResponseEntity<List<FundProductResponseDto>> getFundProductsByType(
            @Parameter(description = "펀드 유형", required = true, example = "주식형")
            @PathVariable String fundType
    ) {
        log.info("GET /api/hana/fund-products/type/{} - 펀드 유형별 조회", fundType);

        List<FundProduct> products = fundProductService.getFundProductsByType(fundType);
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("펀드 유형별 조회 완료 - fundType: {}, 결과: {}건", fundType, response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/risk/{riskLevel}")
    @Operation(summary = "위험등급별 조회", description = "위험등급으로 펀드를 조회합니다 (1: 매우높음 ~ 5: 매우낮음)")
    public ResponseEntity<List<FundProductResponseDto>> getFundProductsByRiskLevel(
            @Parameter(description = "위험등급 (1~5)", required = true, example = "3")
            @PathVariable String riskLevel
    ) {
        log.info("GET /api/hana/fund-products/risk/{} - 위험등급별 조회", riskLevel);

        List<FundProduct> products = fundProductService.getFundProductsByRiskLevel(riskLevel);
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("위험등급별 조회 완료 - riskLevel: {}, 결과: {}건", riskLevel, response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/region/{investmentRegion}")
    @Operation(summary = "투자 지역별 조회", description = "투자 지역으로 펀드를 조회합니다 (국내, 해외, 글로벌)")
    public ResponseEntity<List<FundProductResponseDto>> getFundProductsByRegion(
            @Parameter(description = "투자 지역", required = true, example = "국내")
            @PathVariable String investmentRegion
    ) {
        log.info("GET /api/hana/fund-products/region/{} - 투자 지역별 조회", investmentRegion);

        List<FundProduct> products = fundProductService.getFundProductsByInvestmentRegion(investmentRegion);
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("투자 지역별 조회 완료 - region: {}, 결과: {}건", investmentRegion, response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/irp-eligible")
    @Operation(summary = "IRP 편입 가능 펀드 조회", description = "IRP 계좌로 투자 가능한 펀드를 조회합니다")
    public ResponseEntity<List<FundProductResponseDto>> getIrpEligibleFunds() {
        log.info("GET /api/hana/fund-products/irp-eligible - IRP 편입 가능 펀드 조회");

        List<FundProduct> products = fundProductService.getIrpEligibleFunds();
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("IRP 편입 가능 펀드 조회 완료 - 결과: {}건", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/top-performing")
    @Operation(summary = "수익률 상위 펀드 조회", description = "1년 수익률 기준 상위 펀드를 조회합니다")
    public ResponseEntity<List<FundProductResponseDto>> getTopPerformingFunds() {
        log.info("GET /api/hana/fund-products/top-performing - 수익률 상위 펀드 조회");

        List<FundProduct> products = fundProductService.getTopPerformingFunds();
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("수익률 상위 펀드 조회 완료 - 결과: {}건", response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search")
    @Operation(summary = "펀드명 검색", description = "펀드명으로 상품을 검색합니다")
    public ResponseEntity<List<FundProductResponseDto>> searchFundsByName(
            @Parameter(description = "검색 키워드", required = true, example = "코스피")
            @RequestParam String keyword
    ) {
        log.info("GET /api/hana/fund-products/search?keyword={} - 펀드명 검색", keyword);

        List<FundProduct> products = fundProductService.searchFundsByName(keyword);
        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("펀드명 검색 완료 - keyword: {}, 결과: {}건", keyword, response.size());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/filter")
    @Operation(summary = "복합 필터링 조회", description = "여러 조건으로 펀드를 필터링합니다")
    public ResponseEntity<List<FundProductResponseDto>> filterFunds(
            @Parameter(description = "펀드 유형", example = "주식형")
            @RequestParam(required = false) String fundType,

            @Parameter(description = "위험등급 (1~5)", example = "3")
            @RequestParam(required = false) String riskLevel,

            @Parameter(description = "투자 지역", example = "국내")
            @RequestParam(required = false) String investmentRegion,

            @Parameter(description = "IRP 편입 가능 여부", example = "true")
            @RequestParam(required = false) Boolean isIrpEligible
    ) {
        log.info("GET /api/hana/fund-products/filter - 복합 필터링 조회");

        List<FundProduct> products = fundProductService.searchFunds(
                fundType, riskLevel, investmentRegion, isIrpEligible);

        List<FundProductResponseDto> response = products.stream()
                .map(FundProductResponseDto::fromEntity)
                .collect(Collectors.toList());

        log.info("복합 필터링 조회 완료 - 결과: {}건", response.size());
        return ResponseEntity.ok(response);
    }
}