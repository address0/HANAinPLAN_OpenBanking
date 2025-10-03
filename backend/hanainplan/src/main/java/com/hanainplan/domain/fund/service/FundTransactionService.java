package com.hanainplan.domain.fund.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanainplan.domain.banking.client.HanaBankClient;
import com.hanainplan.domain.fund.dto.FundTransactionDto;
import com.hanainplan.domain.fund.dto.FundTransactionStatsDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * 하나인플랜 펀드 거래 내역 서비스
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FundTransactionService {

    private final HanaBankClient hanaBankClient;
    private final ObjectMapper objectMapper;

    /**
     * 고객의 모든 거래 내역 조회
     */
    public List<FundTransactionDto> getCustomerTransactions(String customerCi) {
        log.info("고객 거래 내역 조회 - customerCi: {}", customerCi);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getCustomerTransactions(customerCi);
            
            List<FundTransactionDto> transactions = objectMapper.convertValue(
                    response, new TypeReference<List<FundTransactionDto>>() {});
            
            log.info("거래 내역 조회 완료 - {}건", transactions.size());
            return transactions;
            
        } catch (Exception e) {
            log.error("거래 내역 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 특정 가입의 거래 내역 조회
     */
    public List<FundTransactionDto> getSubscriptionTransactions(Long subscriptionId) {
        log.info("가입 거래 내역 조회 - subscriptionId: {}", subscriptionId);
        
        try {
            List<Map<String, Object>> response = hanaBankClient.getSubscriptionTransactions(subscriptionId);
            
            List<FundTransactionDto> transactions = objectMapper.convertValue(
                    response, new TypeReference<List<FundTransactionDto>>() {});
            
            log.info("거래 내역 조회 완료 - {}건", transactions.size());
            return transactions;
            
        } catch (Exception e) {
            log.error("거래 내역 조회 실패", e);
            return List.of();
        }
    }

    /**
     * 거래 통계 조회
     */
    public FundTransactionStatsDto getTransactionStats(String customerCi) {
        log.info("거래 통계 조회 - customerCi: {}", customerCi);
        
        try {
            Map<String, Object> response = hanaBankClient.getTransactionStats(customerCi);
            
            FundTransactionStatsDto stats = objectMapper.convertValue(
                    response, FundTransactionStatsDto.class);
            
            log.info("거래 통계 조회 완료 - 총 거래: {}건", stats.getTotalTransactionCount());
            return stats;
            
        } catch (Exception e) {
            log.error("거래 통계 조회 실패", e);
            return FundTransactionStatsDto.builder().build();
        }
    }
}

