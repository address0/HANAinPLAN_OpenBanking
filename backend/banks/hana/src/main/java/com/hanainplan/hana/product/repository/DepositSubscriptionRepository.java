package com.hanainplan.hana.product.repository;

import com.hanainplan.hana.product.entity.DepositSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 정기예금 가입 정보 Repository
 */
@Repository
public interface DepositSubscriptionRepository extends JpaRepository<DepositSubscription, Long> {
    
    /**
     * 계좌번호로 예금 가입 내역 조회
     */
    Optional<DepositSubscription> findByAccountNumber(String accountNumber);
    
    /**
     * 고객 CI로 예금 가입 내역 조회
     */
    List<DepositSubscription> findByCustomerCi(String customerCi);
    
    /**
     * 상태로 예금 가입 내역 조회
     */
    List<DepositSubscription> findByStatus(String status);
    
    /**
     * 만기일과 상태로 예금 가입 내역 조회
     */
    List<DepositSubscription> findByMaturityDateAndStatus(LocalDate maturityDate, String status);
}
