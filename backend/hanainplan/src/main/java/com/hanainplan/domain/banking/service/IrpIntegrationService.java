package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.dto.IrpAccountDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenRequestDto;
import com.hanainplan.domain.banking.dto.IrpAccountOpenResponseDto;
import com.hanainplan.domain.banking.dto.IrpAccountStatusResponseDto;
import com.hanainplan.domain.banking.entity.IrpAccount;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public interface IrpIntegrationService {

    List<IrpAccountDto> getCustomerIrpAccounts(String customerCi);

    List<IrpAccountDto> getCustomerIrpAccountsByCustomerId(Long customerId);

    List<IrpAccountDto> getBankIrpAccounts(String bankCode);

    List<IrpAccountDto> getAllIrpAccounts();

    IrpAccountDto getIrpAccountDetail(Long irpAccountId);

    boolean hasIrpAccount(String customerCi);

    boolean hasIrpAccountByCustomerId(Long customerId);

    IrpAccountStatusResponseDto checkIrpAccountStatus(String customerCi);

    IrpAccountStatusResponseDto checkIrpAccountStatusByCustomerId(Long customerId);

    IrpAccountOpenResponseDto openIrpAccount(IrpAccountOpenRequestDto request) throws Exception;

    IrpAccountDto getCustomerIrpAccount(String customerCi);

    IrpAccountDto getCustomerIrpAccountByCustomerId(Long customerId);

    IrpAccountDto getCustomerIrpAccountByUserId(Long userId);

    long getActiveIrpAccountCount(String customerCi);

    long getActiveIrpAccountCountByCustomerId(Long customerId);

    Map<String, Object> getIrpAccountStatistics();

    Map<String, Object> getCustomerIrpPortfolio(String customerCi);

    Map<String, Object> getCustomerIrpPortfolioByCustomerId(Long customerId);

    Map<String, Object> getTotalIrpBalanceByBank();

    void monthlyIrpStatisticsBatch();

    void maturityNotificationBatch();
}