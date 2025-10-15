package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.IrpAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface IrpAccountRepository extends JpaRepository<IrpAccount, Long> {

    List<IrpAccount> findByCustomerCiOrderByCreatedDateDesc(String customerCi);

    Optional<IrpAccount> findByCustomerCiAndBankCode(String customerCi, String bankCode);

    Optional<IrpAccount> findByAccountNumber(String accountNumber);

    Optional<IrpAccount> findByCustomerCiAndAccountNumber(String customerCi, String accountNumber);

    List<IrpAccount> findByAccountStatusOrderByCreatedDateDesc(String accountStatus);

    List<IrpAccount> findByBankCodeAndAccountStatusOrderByCreatedDateDesc(String bankCode, String accountStatus);

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.syncStatus = 'PENDING' OR ia.lastSyncDate < :threshold")
    List<IrpAccount> findAccountsNeedingSync(@Param("threshold") LocalDateTime threshold);

    List<IrpAccount> findBySyncStatusOrderByLastSyncDateDesc(String syncStatus);

    @Query("SELECT ia FROM IrpAccount ia WHERE ia.maturityDate <= CURRENT_DATE AND ia.accountStatus = 'ACTIVE'")
    List<IrpAccount> findMaturedAccounts();

    List<IrpAccount> findByIsAutoDepositTrue();

    List<IrpAccount> findByDepositDayAndIsAutoDepositTrue(Integer depositDay);

    @Query("SELECT ia.bankCode, COUNT(ia), SUM(ia.currentBalance) FROM IrpAccount ia WHERE ia.accountStatus = 'ACTIVE' GROUP BY ia.bankCode")
    List<Object[]> getIrpStatisticsByBank();

    @Query("SELECT COUNT(ia) > 0 FROM IrpAccount ia WHERE ia.customerCi = :customerCi")
    boolean existsByCustomerCi(@Param("customerCi") String customerCi);

    @Query("SELECT COUNT(ia) FROM IrpAccount ia WHERE ia.customerCi = :customerCi AND ia.accountStatus = 'ACTIVE'")
    long countActiveAccountsByCustomerCi(@Param("customerCi") String customerCi);

    @Query("SELECT COUNT(ia) FROM IrpAccount ia WHERE ia.customerId = :customerId AND ia.accountStatus = 'ACTIVE'")
    long countActiveAccountsByCustomerId(@Param("customerId") Long customerId);

    @Query("SELECT COUNT(ia) > 0 FROM IrpAccount ia WHERE ia.customerId = :customerId")
    boolean existsByCustomerId(@Param("customerId") Long customerId);

    Optional<IrpAccount> findByCustomerIdAndAccountStatus(Long customerId, String accountStatus);
}