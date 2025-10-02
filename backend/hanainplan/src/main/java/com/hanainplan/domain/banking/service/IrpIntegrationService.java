package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.IrpAccountDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenRequestDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenResponseDto;
import com.hanainplan.domain.banking.dto.IrpAccountStatusResponseDto;
import com.hanainplan.domain.banking.entity.IrpAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * IRP 통합 관리 서비스
 * - 은행별 IRP 데이터를 HANAinPLAN에서 통합 관리
 */
public interface IrpIntegrationService {

    // ===== 계좌 관리 =====

    /**
     * 특정 고객의 모든 IRP 계좌 조회
     */
    List<IrpAccountDto> getCustomerIrpAccounts(String customerCi);

    /**
     * 특정 고객의 모든 IRP 계좌 조회 (고객 ID 기반)
     */
    List<IrpAccountDto> getCustomerIrpAccountsByCustomerId(Long customerId);

    /**
     * 특정 은행의 IRP 계좌 조회
     */
    List<IrpAccountDto> getBankIrpAccounts(String bankCode);

    /**
     * 모든 IRP 계좌 조회 (관리자용)
     */
    List<IrpAccountDto> getAllIrpAccounts();

    /**
     * IRP 계좌 상세 조회
     */
    IrpAccountDto getIrpAccountDetail(Long irpAccountId);

    /**
     * IRP 계좌 존재 여부 확인 (CI 기반)
     */
    boolean hasIrpAccount(String customerCi);

    /**
     * IRP 계좌 존재 여부 확인 (고객 ID 기반)
     */
    boolean hasIrpAccountByCustomerId(Long customerId);

    /**
     * IRP 계좌 상태 확인 (상세 정보 포함)
     */
    IrpAccountStatusResponseDto checkIrpAccountStatus(String customerCi);

    /**
     * IRP 계좌 상태 확인 (고객 ID 기반, 상세 정보 포함)
     */
    IrpAccountStatusResponseDto checkIrpAccountStatusByCustomerId(Long customerId);

    /**
     * IRP 계좌 개설
     */
    IrpAccountOpenResponseDto openIrpAccount(IrpAccountOpenRequestDto request) throws Exception;

    /**
     * 고객 IRP 계좌 정보 조회 (단일 계좌)
     */
    IrpAccountDto getCustomerIrpAccount(String customerCi);

    /**
     * 고객 IRP 계좌 정보 조회 (고객 ID 기반, 단일 계좌)
     */
    IrpAccountDto getCustomerIrpAccountByCustomerId(Long customerId);

    /**
     * 활성화된 IRP 계좌 수 조회 (CI 기반)
     */
    long getActiveIrpAccountCount(String customerCi);

    /**
     * 활성화된 IRP 계좌 수 조회 (고객 ID 기반)
     */
    long getActiveIrpAccountCountByCustomerId(Long customerId);

    // ===== 통계 및 분석 =====

    /**
     * 은행별 IRP 계좌 통계
     */
    Map<String, Object> getIrpAccountStatistics();

    /**
     * 고객별 IRP 포트폴리오 요약
     */
    Map<String, Object> getCustomerIrpPortfolio(String customerCi);

    /**
     * 고객별 IRP 포트폴리오 요약 (고객 ID 기반)
     */
    Map<String, Object> getCustomerIrpPortfolioByCustomerId(Long customerId);

    /**
     * 은행별 IRP 잔고 합계
     */
    Map<String, Object> getTotalIrpBalanceByBank();

    // ===== 배치 작업 =====

    /**
     * 주간 IRP 데이터 배치 동기화
     * TODO: OpenFeign으로 마이그레이션 필요
     */
    // void weeklyIrpDataSyncBatch();

    /**
     * 월별 IRP 통계 생성 배치 작업
     */
    void monthlyIrpStatisticsBatch();

    /**
     * 만기 도래 IRP 계좌 알림 배치 작업
     */
    void maturityNotificationBatch();
}
