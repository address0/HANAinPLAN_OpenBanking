package com.hanainplan.domain.fund.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.fund.dto.FundClassDetailDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 하나인플랜 펀드 클래스 서비스
 * - 하나은행 API를 통해 실제 펀드 클래스 정보 조회
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundClassService {

    private final HanaBankClient hanaBankClient;
    private final ObjectMapper objectMapper;

    /**
     * 판매중인 펀드 클래스 목록 조회
     */
    public List<FundClassDetailDto> getAllOnSaleFundClasses() {
        log.info("판매중인 펀드 클래스 목록 조회 요청");
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getAllOnSaleFundClasses();
            List<FundClassDetailDto> fundClasses = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("판매중인 펀드 클래스 목록 조회 완료 - {}건", fundClasses.size());
            return fundClasses;
        } catch (Exception e) {
            log.error("펀드 클래스 목록 조회 실패", e);
            throw new RuntimeException("펀드 클래스 목록 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 클래스 코드로 상세 조회
     */
    public FundClassDetailDto getFundClassByCode(String childFundCd) {
        log.info("펀드 클래스 상세 조회 - childFundCd: {}", childFundCd);
        
        try {
            Map<String, Object> response = hanaBankClient.getFundClass(childFundCd);
            FundClassDetailDto fundClass = mapToDto(response);
            
            log.info("펀드 클래스 상세 조회 완료 - childFundCd: {}", childFundCd);
            return fundClass;
        } catch (Exception e) {
            log.error("펀드 클래스 상세 조회 실패 - childFundCd: {}", childFundCd, e);
            throw new RuntimeException("펀드 클래스 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 모펀드 코드로 클래스 목록 조회
     */
    public List<FundClassDetailDto> getFundClassesByMaster(String fundCd) {
        log.info("모펀드의 클래스 목록 조회 - fundCd: {}", fundCd);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getFundClassesByMaster(fundCd);
            List<FundClassDetailDto> fundClasses = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("모펀드의 클래스 목록 조회 완료 - fundCd: {}, 결과: {}건", fundCd, fundClasses.size());
            return fundClasses;
        } catch (Exception e) {
            log.error("모펀드의 클래스 목록 조회 실패 - fundCd: {}", fundCd, e);
            throw new RuntimeException("펀드 클래스 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 자산 유형별 조회
     */
    public List<FundClassDetailDto> getFundClassesByAssetType(String assetType) {
        log.info("자산 유형별 조회 - assetType: {}", assetType);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getFundClassesByAssetType(assetType);
            List<FundClassDetailDto> fundClasses = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("자산 유형별 조회 완료 - assetType: {}, 결과: {}건", assetType, fundClasses.size());
            return fundClasses;
        } catch (Exception e) {
            log.error("자산 유형별 조회 실패 - assetType: {}", assetType, e);
            throw new RuntimeException("펀드 클래스 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 클래스 코드별 조회 (A/C/P 등)
     */
    public List<FundClassDetailDto> getFundClassesByClassCode(String classCode) {
        log.info("클래스 코드별 조회 - classCode: {}", classCode);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getFundClassesByClassCode(classCode);
            List<FundClassDetailDto> fundClasses = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("클래스 코드별 조회 완료 - classCode: {}, 결과: {}건", classCode, fundClasses.size());
            return fundClasses;
        } catch (Exception e) {
            log.error("클래스 코드별 조회 실패 - classCode: {}", classCode, e);
            throw new RuntimeException("펀드 클래스 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 최소 투자금액 이하 펀드 조회
     */
    public List<FundClassDetailDto> getFundClassesByMaxAmount(int maxAmount) {
        log.info("최소 투자금액 {}원 이하 펀드 조회", maxAmount);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getFundClassesByMaxAmount(maxAmount);
            List<FundClassDetailDto> fundClasses = response.stream()
                    .map(this::mapToDto)
                    .collect(Collectors.toList());
            
            log.info("최소 투자금액 이하 펀드 조회 완료 - maxAmount: {}, 결과: {}건", maxAmount, fundClasses.size());
            return fundClasses;
        } catch (Exception e) {
            log.error("최소 투자금액 이하 펀드 조회 실패 - maxAmount: {}", maxAmount, e);
            throw new RuntimeException("펀드 클래스 조회에 실패했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * Map -> FundClassDetailDto 변환
     */
    private FundClassDetailDto mapToDto(Map<String, Object> map) {
        try {
            return objectMapper.convertValue(map, FundClassDetailDto.class);
        } catch (Exception e) {
            log.error("DTO 변환 실패", e);
            throw new RuntimeException("데이터 변환에 실패했습니다: " + e.getMessage(), e);
        }
    }
}

