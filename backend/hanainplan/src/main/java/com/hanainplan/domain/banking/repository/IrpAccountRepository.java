package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.IrpAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * IRP 계좌 리포지토리
 */
@Repository
public interface IrpAccountRepository extends JpaRepository<IrpAccount, Long> {

    /**
     * 고객 CI로 IRP 계좌 조회
     */
    List<IrpAccount> findByCustomerCiOrderByCreatedDateDesc(String customerCi);

    /**
     * 고객 CI와 은행 코드로 IRP 계좌 조회
     */
    Optional<IrpAccount> findByCustomerCiAndBankCode(String customerCi, String bankCode);

    /**
     * 계좌번호로 IRP 계좌 조회
     */
    Optional<IrpAccount> findByAccountNumber(String accountNumber);

    /**
     * 고객 CI와 계좌번호로 IRP 계좌 조회
     */
    Optional<IrpAccount> findByCustomerCiAndAccountNumber(String customerCi, String accountNumber);

    /**
     * 활성화된 IRP 계좌 조회
     */
    List<IrpAccount> findByAccountStatusOrderByCreatedDateDesc(String accountStatus);

    /**
     * 특정 은행의 활성화된 IRP 계좌 조회
     */
    List<IrpAccount> findByBankCodeAndAccountStatusOrderByCreatedDateDesc(String bankCode, String accountStatus);


    /**
     * 동기화가 필요한 IRP 계좌 조회 (마지막 동기화 이후 일정 시간 경과)
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.syncStatus = 'PENDING' OR ia.lastSyncDate < :threshold")
    List<IrpAccount> findAccountsNeedingSync(@Param("threshold") LocalDateTime threshold);

    /**
     * 동기화 실패한 IRP 계좌 조회
     */
    List<IrpAccount> findBySyncStatusOrderByLastSyncDateDesc(String syncStatus);

    /**
     * 만기 도래한 IRP 계좌 조회
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.maturityDate <= CURRENT_DATE AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findMaturedAccounts();

    /**
     * 자동납입 설정된 IRP 계좌 조회
     */
    List<IrpAccount> findByIsAutoDepositTrue();

    /**
     * 특정 납입일의 자동납입 계좌 조회
     */
    List<IrpAccount> findByDepositDayAndIsAutoDepositTrue(Integer depositDay);

    /**
     * 은행별 IRP 계좌 통계 조회
     */
    @Query("SELECT ia.bankCode, COUNT(ia), SUM(ia.currentBalance) FROM IrpAccount ia WHERE ia.accountStatus = 'ACTIVE' GROUP BY ia.bankCode")
    List<Object[]> getIrpStatisticsByBank();

    /**
     * 특정 고객의 모든 은행 IRP 계좌 존재 여부 확인
     */
    @Query("SELECT COUNT(ia) > 0 FROM IrpAccount ia WHERE ia.customerCi = :customerCi")
    boolean existsByCustomerCi(@Param("customerCi") String customerCi);

    /**
     * 특정 고객의 활성화된 IRP 계좌 수 조회 (CI 기반)
     */
    @Query("SELECT COUNT(ia) FROM IrpAccount ia WHERE ia.customerCi = :customerCi AND ia.accountStatus = 'ACTIVE'")
    long countActiveAccountsByCustomerCi(@Param("customerCi") String customerCi);

    /**
     * 특정 고객의 활성화된 IRP 계좌 수 조회 (고객 ID 기반)
     */
    @Query("SELECT COUNT(ia) FROM IrpAccount ia WHERE ia.customerId = :customerId AND ia.accountStatus = 'ACTIVE'")
    long countActiveAccountsByCustomerId(@Param("customerId") Long customerId);

    /**
     * 특정 고객의 모든 은행 IRP 계좌 존재 여부 확인 (고객 ID 기반)
     */
    @Query("SELECT COUNT(ia) > 0 FROM IrpAccount ia WHERE ia.customerId = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    /**
     * 고객 ID와 계좌 상태로 IRP 계좌 조회
     */
    Optional<IrpAccount> findByCustomerIdAndAccountStatus(Long customerId, String accountStatus);
}
