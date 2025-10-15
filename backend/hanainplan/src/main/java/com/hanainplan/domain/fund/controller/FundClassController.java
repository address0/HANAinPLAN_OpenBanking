package com.hanainplan.domain.fund.controller;

import com.hanainplan.domain.fund.dto.FundClassDetailDto;
import com.hanainplan.domain.fund.service.FundClassService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banking/fund-classes")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Fund Classes", description = "펀드 클래스 API")
@CrossOrigin(origins = "*")
public class FundClassController {

    private final FundClassService fundClassService;

    @GetMapping
    @Operation(summary = "판매중인 펀드 클래스 목록", description = "판매중인 모든 펀드 클래스를 상세 정보와 함께 조회합니다")
    public ResponseEntity<List<FundClassDetailDto>> getAllOnSaleFundClasses() {
        log.info("GET /api/banking/fund-classes - 판매중인 펀드 클래스 목록 조회");

        List<FundClassDetailDto> fundClasses = fundClassService.getAllOnSaleFundClasses();

        log.info("판매중인 펀드 클래스 목록 조회 완료 - {}건", fundClasses.size());
        return ResponseEntity.ok(fundClasses);
    }

    @GetMapping("/{childFundCd}")
    @Operation(summary = "펀드 클래스 상세 조회", description = "클래스 코드로 펀드 클래스 상세 정보를 조회합니다")
    public ResponseEntity<FundClassDetailDto> getFundClass(
            @Parameter(description = "클래스 펀드 코드", required = true, example = "51306P")
            @PathVariable String childFundCd
    ) {
        log.info("GET /api/banking/fund-classes/{} - 펀드 클래스 상세 조회", childFundCd);

        FundClassDetailDto fundClass = fundClassService.getFundClassByCode(childFundCd)
                .orElseThrow(() -> new IllegalArgumentException("펀드 클래스를 찾을 수 없습니다: " + childFundCd));

        log.info("펀드 클래스 상세 조회 완료 - childFundCd: {}", childFundCd);
        return ResponseEntity.ok(fundClass);
    }

    @GetMapping("/master/{fundCd}")
    @Operation(summary = "모펀드의 클래스 목록", description = "모펀드 코드로 해당 펀드의 모든 클래스를 조회합니다")
    public ResponseEntity<List<FundClassDetailDto>> getFundClassesByMaster(
            @Parameter(description = "모펀드 코드", required = true, example = "513061")
            @PathVariable String fundCd
    ) {
        log.info("GET /api/banking/fund-classes/master/{} - 모펀드의 클래스 목록 조회", fundCd);

        List<FundClassDetailDto> fundClasses = fundClassService.getFundClassesByMasterCode(fundCd);

        log.info("모펀드의 클래스 목록 조회 완료 - fundCd: {}, 결과: {}건", fundCd, fundClasses.size());
        return ResponseEntity.ok(fundClasses);
    }

    @GetMapping("/asset-type/{assetType}")
    @Operation(summary = "자산 유형별 조회", description = "자산 유형으로 펀드 클래스를 조회합니다 (채권혼합, 주식형 등)")
    public ResponseEntity<List<FundClassDetailDto>> getFundClassesByAssetType(
            @Parameter(description = "자산 유형", required = true, example = "채권혼합")
            @PathVariable String assetType
    ) {
        log.info("GET /api/banking/fund-classes/asset-type/{} - 자산 유형별 조회", assetType);

        List<FundClassDetailDto> fundClasses = fundClassService.getFundClassesByAssetType(assetType);

        log.info("자산 유형별 조회 완료 - assetType: {}, 결과: {}건", assetType, fundClasses.size());
        return ResponseEntity.ok(fundClasses);
    }

    @GetMapping("/class-code/{classCode}")
    @Operation(summary = "클래스 코드별 조회", description = "클래스 코드(A/C/P 등)로 펀드를 조회합니다")
    public ResponseEntity<List<FundClassDetailDto>> getFundClassesByClassCode(
            @Parameter(description = "클래스 코드", required = true, example = "P")
            @PathVariable String classCode
    ) {
        log.info("GET /api/banking/fund-classes/class-code/{} - 클래스 코드별 조회", classCode);

        List<FundClassDetailDto> fundClasses = fundClassService.getFundClassesByClassCode(classCode);

        log.info("클래스 코드별 조회 완료 - classCode: {}, 결과: {}건", classCode, fundClasses.size());
        return ResponseEntity.ok(fundClasses);
    }

    @GetMapping("/max-amount/{maxAmount}")
    @Operation(summary = "최소 투자금액 이하 펀드", description = "지정한 금액 이하로 투자 가능한 펀드를 조회합니다")
    public ResponseEntity<List<FundClassDetailDto>> getFundClassesByMaxAmount(
            @Parameter(description = "최대 금액", required = true, example = "50000")
            @PathVariable int maxAmount
    ) {
        log.info("GET /api/banking/fund-classes/max-amount/{} - 최소 투자금액 이하 펀드 조회", maxAmount);

        List<FundClassDetailDto> fundClasses = fundClassService.getFundClassesByMaxAmount(maxAmount);

        log.info("최소 투자금액 이하 펀드 조회 완료 - maxAmount: {}, 결과: {}건", maxAmount, fundClasses.size());
        return ResponseEntity.ok(fundClasses);
    }
}