package com.hanainplan.domain.fund.service;

import com.hanainplan.domain.fund.dto.FundClassDetailDto;
import com.hanainplan.domain.fund.entity.FundClass;
import com.hanainplan.domain.fund.entity.FundNav;
import com.hanainplan.domain.fund.repository.FundClassRepository;
import com.hanainplan.domain.fund.repository.FundNavRepository;
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

        List<FundClass> fundClasses = fundClassRepository.findBySaleStatusOrderByChildFundCdAsc("ON");

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return toDetailDto(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public Optional<FundClassDetailDto> getFundClassByCode(String childFundCd) {
        log.info("펀드 클래스 상세 조회 - childFundCd: {}", childFundCd);

        Optional<FundClass> fundClassOpt = fundClassRepository.findById(childFundCd);

        if (fundClassOpt.isEmpty()) {
            return Optional.empty();
        }

        FundClass fundClass = fundClassOpt.get();
        FundNav latestNav = fundNavRepository.findLatestByChildFundCd(childFundCd)
                .orElse(null);

        return Optional.of(toDetailDto(fundClass, latestNav));
    }

    public List<FundClassDetailDto> getFundClassesByMasterCode(String fundCd) {
        log.info("모펀드의 클래스 목록 조회 - fundCd: {}", fundCd);

        List<FundClass> fundClasses = fundClassRepository.findByFundMasterFundCd(fundCd);

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return toDetailDto(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public List<FundClassDetailDto> getFundClassesByAssetType(String assetType) {
        log.info("자산 유형별 펀드 클래스 조회 - assetType: {}", assetType);

        List<FundClass> fundClasses = fundClassRepository.findByAssetType(assetType);

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return toDetailDto(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public List<FundClassDetailDto> getFundClassesByClassCode(String classCode) {
        log.info("클래스 코드별 조회 - classCode: {}", classCode);

        List<FundClass> fundClasses = fundClassRepository.findByClassCode(classCode);

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return toDetailDto(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    public List<FundClassDetailDto> getFundClassesByMaxAmount(int maxAmount) {
        log.info("최소 투자금액 {}원 이하 펀드 조회", maxAmount);

        List<FundClass> fundClasses = fundClassRepository.findBySaleStatusOrderByChildFundCdAsc("ON").stream()
                .filter(fc -> fc.getFundRules() != null && 
                             fc.getFundRules().getMinInitialAmount() != null &&
                             fc.getFundRules().getMinInitialAmount().intValue() <= maxAmount)
                .collect(Collectors.toList());

        return fundClasses.stream()
                .map(fc -> {
                    FundNav latestNav = fundNavRepository.findLatestByChildFundCd(fc.getChildFundCd())
                            .orElse(null);
                    return toDetailDto(fc, latestNav);
                })
                .collect(Collectors.toList());
    }

    private FundClassDetailDto toDetailDto(FundClass entity, FundNav latestNav) {
        FundClassDetailDto.FundClassDetailDtoBuilder builder = FundClassDetailDto.builder()
                .childFundCd(entity.getChildFundCd())
                .classCode(entity.getClassCode())
                .loadType(entity.getLoadType())
                .taxCategory(entity.getTaxCategory())
                .saleStatus(entity.getSaleStatus())
                .sourceUrl(entity.getSourceUrl());

        if (entity.getFundMaster() != null) {
            builder.fundMaster(FundClassDetailDto.FundMasterDto.builder()
                    .fundCd(entity.getFundMaster().getFundCd())
                    .fundName(entity.getFundMaster().getFundName())
                    .fundGb(entity.getFundMaster().getFundGb())
                    .assetType(entity.getFundMaster().getAssetType())
                    .riskGrade(entity.getFundMaster().getRiskGrade())
                    .currency(entity.getFundMaster().getCurrency())
                    .isActive(entity.getFundMaster().getIsActive())
                    .build());
        }

        if (entity.getFundRules() != null) {
            builder.rules(FundClassDetailDto.FundRulesDto.builder()
                    .cutoffTime(entity.getFundRules().getCutoffTime())
                    .navPublishTime(entity.getFundRules().getNavPublishTime())
                    .buySettleDays(entity.getFundRules().getBuySettleDays())
                    .redeemSettleDays(entity.getFundRules().getRedeemSettleDays())
                    .unitType(entity.getFundRules().getUnitType())
                    .minInitialAmount(entity.getFundRules().getMinInitialAmount())
                    .minAdditional(entity.getFundRules().getMinAdditional())
                    .incrementAmount(entity.getFundRules().getIncrementAmount())
                    .allowSip(entity.getFundRules().getAllowSip())
                    .allowSwitch(entity.getFundRules().getAllowSwitch())
                    .redemptionFeeRate(entity.getFundRules().getRedemptionFeeRate())
                    .redemptionFeeDays(entity.getFundRules().getRedemptionFeeDays())
                    .build());
        }

        if (entity.getFundFees() != null) {
            builder.fees(FundClassDetailDto.FundFeesDto.builder()
                    .mgmtFeeBps(entity.getFundFees().getMgmtFeeBps())
                    .salesFeeBps(entity.getFundFees().getSalesFeeBps())
                    .trusteeFeeBps(entity.getFundFees().getTrusteeFeeBps())
                    .adminFeeBps(entity.getFundFees().getAdminFeeBps())
                    .frontLoadPct(entity.getFundFees().getFrontLoadPct())
                    .totalFeeBps(entity.getFundFees().getTotalFeeBps())
                    .mgmtFeePercent(entity.getFundFees().getMgmtFeePercent())
                    .salesFeePercent(entity.getFundFees().getSalesFeePercent())
                    .trusteeFeePercent(entity.getFundFees().getTrusteeFeePercent())
                    .adminFeePercent(entity.getFundFees().getAdminFeePercent())
                    .totalFeePercent(entity.getFundFees().getTotalFeePercent())
                    .build());
        }

        if (latestNav != null) {
            builder.latestNav(latestNav.getNav())
                   .latestNavDate(latestNav.getNavDate());
        }

        return builder.build();
    }
}