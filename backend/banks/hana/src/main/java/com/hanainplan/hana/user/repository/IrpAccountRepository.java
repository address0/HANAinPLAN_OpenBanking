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

    Optional<IrpAccount> findByCustomerCi(String customerCi);

    Optional<IrpAccount> findByAccountNumber(String accountNumber);

    Optional<IrpAccount> findByCustomerCiAndAccountNumber(String customerCi, String accountNumber);

    Optional<IrpAccount> findByCustomerCiAndAccountStatus(String customerCi, String accountStatus);

    Optional<IrpAccount> findByCustomerCiAndBankCodeAndAccountStatus(String customerCi, String bankCode, String accountStatus);

    List<IrpAccount> findAllByCustomerCiAndBankCodeAndAccountStatus(String customerCi, String bankCode, String accountStatus);

    List<IrpAccount> findByAccountStatus(String accountStatus);

    List<IrpAccount> findByBankCodeAndAccountStatus(String bankCode, String accountStatus);

    List<IrpAccount> findByAccountStatusOrderByCreatedAtDesc(String accountStatus);

    List<IrpAccount> findByOpenDate(LocalDate openDate);

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.maturityDate <= CURRENT_DATE AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findMaturedAccounts();

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.isAutoDeposit = true AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findActiveAutoDepositAccounts();

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.isAutoDeposit = true AND ia.depositDay = :day AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findAutoDepositAccountsByDay(@Param("day") Integer day);

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.isAutoDeposit = true AND ia.accountStatus = 'ACTIVE' AND ia.lastContributionDate <= :cutoffDate")
    List<IrpAccount> findAccountsNeedingContribution(@Param("cutoffDate") LocalDate cutoffDate);

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.updatedAt > :sinceDateTime")
    List<IrpAccount> findByUpdatedAtAfter(@Param("sinceDateTime") java.time.LocalDateTime sinceDateTime);
}