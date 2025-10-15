package com.hanainplan.hana.fund.service;

import com.hanainplan.hana.fund.dto.FundClassDetailDto;
import com.hanainplan.hana.fund.entity.FundClass;
import com.hanainplan.hana.fund.entity.FundNav;
import com.hanainplan.hana.fund.repository.FundClassRepository;
import com.hanainplan.hana.fund.repository.FundNavRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class FundClassService {

    private final FundClassRepository fundClassRepository;
    private final FundNavRepository fundNavRepository;

    public List<FundClassDetailDto> getAllOnSaleFundClasses() {
        log.info("판매중인 펀드 클래스 목록 조회");

        List<FundClass> fundClasses = fundClassRepository.findAllOnSaleWithDetails();

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return FundClassDetailDto.fromEntity(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public Optional<FundClassDetailDto> getFundClassByCode(String childFundCd) {
        log.info("펀드 클래스 상세 조회 - childFundCd: {}", childFundCd);

        Optional<FundClass> fundClassOpt = fundClassRepository.findByChildFundCdWithDetails(childFundCd);

        if (fundClassOpt.isEmpty()) {
            return Optional.empty();
        }

        FundClass fundClass = fundClassOpt.get();
        FundNav latestNav = fundNavRepository.findLatestByChildFundCd(childFundCd)
                .orElse(null);

        return Optional.of(FundClassDetailDto.fromEntity(fundClass, latestNav));
    }

    public List<FundClassDetailDto> getFundClassesByMasterCode(String fundCd) {
        log.info("모펀드의 클래스 목록 조회 - fundCd: {}", fundCd);

        List<FundClass> fundClasses = fundClassRepository.findByFundMasterFundCd(fundCd);

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return FundClassDetailDto.fromEntity(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public List<FundClassDetailDto> getFundClassesByAssetType(String assetType) {
        log.info("자산 유형별 펀드 클래스 조회 - assetType: {}", assetType);

        List<FundClass> fundClasses = fundClassRepository.findAllOnSaleWithDetails().stream()
                .filter(fc -> fc.getFundMaster() != null && 
                             assetType.equals(fc.getFundMaster().getAssetType()))
                .collect(Collectors.toList());

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return FundClassDetailDto.fromEntity(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public List<FundClassDetailDto> getFundClassesByClassCode(String classCode) {
        log.info("클래스 코드별 조회 - classCode: {}", classCode);

        List<FundClass> fundClasses = fundClassRepository.findByClassCodeOrderByChildFundCdAsc(classCode);

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return FundClassDetailDto.fromEntity(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public List<FundClassDetailDto> getFundClassesByMaxAmount(int maxAmount) {
        log.info("최소 투자금액 {}원 이하 펀드 조회", maxAmount);

        List<FundClass> fundClasses = fundClassRepository.findAllOnSaleWithDetails().stream()
                .filter(fc -> fc.getFundRules() != null && 
                             fc.getFundRules().getMinInitialAmount() != null &&
                             fc.getFundRules().getMinInitialAmount().intValue() <= maxAmount)
                .collect(Collectors.toList());

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return FundClassDetailDto.fromEntity(fc, latestNav);
                })
                .collect(Collectors.toList());
    }
}