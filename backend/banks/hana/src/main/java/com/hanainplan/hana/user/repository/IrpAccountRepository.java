package com.hanainplan.hana.user.repository;

import com.hanainplan.hana.user.entity.IrpAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface IrpAccountRepository extends JpaRepository<IrpAccount, Long> {

    /**
     * 사용자 CI로 IRP 계좌 조회
     */
    Optional<IrpAccount> findByCustomerCi(String customerCi);

    /**
     * 계좌번호로 IRP 계좌 조회
     */
    Optional<IrpAccount> findByAccountNumber(String accountNumber);

    /**
     * 사용자 CI와 계좌상태로 조회
     */
    Optional<IrpAccount> findByCustomerCiAndAccountStatus(String customerCi, String accountStatus);

    /**
     * 사용자 CI, 은행코드, 계좌상태로 조회
     */
    Optional<IrpAccount> findByCustomerCiAndBankCodeAndAccountStatus(String customerCi, String bankCode, String accountStatus);

    /**
     * 계좌상태로 조회
     */
    List<IrpAccount> findByAccountStatus(String accountStatus);

    /**
     * 은행코드와 계좌상태로 조회
     */
    List<IrpAccount> findByBankCodeAndAccountStatus(String bankCode, String accountStatus);

    /**
     * 활성화된 IRP 계좌 목록 조회
     */
    List<IrpAccount> findByAccountStatusOrderByCreatedAtDesc(String accountStatus);

    /**
     * 특정 날짜에 개설된 IRP 계좌 조회
     */
    List<IrpAccount> findByOpenDate(LocalDate openDate);

    /**
     * 만기도래된 IRP 계좌 조회
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.maturityDate <= CURRENT_DATE AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findMaturedAccounts();

    /**
     * 자동납입이 설정된 활성 계좌 조회
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.isAutoDeposit = true AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findActiveAutoDepositAccounts();

    /**
     * 특정 납입일에 자동납입이 설정된 계좌 조회
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.isAutoDeposit = true AND ia.depositDay = :day AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findAutoDepositAccountsByDay(@Param("day") Integer day);

    /**
     * 특정 기간 내 납입이 필요한 계좌 조회
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.isAutoDeposit = true AND ia.accountStatus = 'ACTIVE' AND ia.lastContributionDate <= :cutoffDate")
    List<IrpAccount> findAccountsNeedingContribution(@Param("cutoffDate") LocalDate cutoffDate);

    /**
     * 특정 시간 이후 업데이트된 IRP 계좌 조회
     */
    @Query("SELECT ia FROM IrpAccount ia WHERE ia.updatedAt > :sinceDateTime")
    List<IrpAccount> findByUpdatedAtAfter(@Param("sinceDateTime") java.time.LocalDateTime sinceDateTime);
}

