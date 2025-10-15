package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.DepositSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositSubscriptionRepository extends JpaRepository<DepositSubscription, Long> {

    List<DepositSubscription> findByUserId(Long userId);

    List<DepositSubscription> findByUserIdOrderBySubscriptionDateDesc(Long userId);

    List<DepositSubscription> findByCustomerCi(String customerCi);

    Optional<DepositSubscription> findByAccountNumber(String accountNumber);

    List<DepositSubscription> findByUserIdAndStatus(Long userId, String status);

    List<DepositSubscription> findByCustomerCiAndStatus(String customerCi, String status);

    List<DepositSubscription> findByBankCode(String bankCode);

    List<DepositSubscription> findByUserIdAndBankCode(Long userId, String bankCode);

    List<DepositSubscription> findByDepositCode(String depositCode);

    List<DepositSubscription> findByStatus(String status);

    List<DepositSubscription> findByMaturityDateBetween(LocalDate startDate, LocalDate endDate);

    List<DepositSubscription> findByProductType(Integer productType);

    List<DepositSubscription> findByUserIdAndProductType(Long userId, Integer productType);

    @Query("SELECT ds FROM DepositSubscription ds WHERE ds.maturityDate = CURRENT_DATE AND ds.status = 'ACTIVE'")
    List<DepositSubscription> findMaturingToday();

    @Query("SELECT ds FROM DepositSubscription ds WHERE ds.nextInterestPaymentDate <= CURRENT_DATE AND ds.status = 'ACTIVE'")
    List<DepositSubscription> findReadyForInterestPayment();

    @Query("SELECT ds.bankCode, ds.bankName, COUNT(ds), SUM(ds.currentBalance) " +
           "FROM DepositSubscription ds WHERE ds.status = 'ACTIVE' GROUP BY ds.bankCode, ds.bankName")
    List<Object[]> getSubscriptionStatisticsByBank();

    @Query("SELECT SUM(ds.currentBalance) FROM DepositSubscription ds WHERE ds.userId = :userId AND ds.status = 'ACTIVE'")
    BigDecimal getTotalBalanceByUserId(@Param("userId") Long userId);

    List<DepositSubscription> findByMaturityDateAndStatus(LocalDate maturityDate, String status);
}