package com.hanainplan.hana.product.repository;

import com.hanainplan.hana.product.entity.DepositSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface DepositSubscriptionRepository extends JpaRepository<DepositSubscription, Long> {

    Optional<DepositSubscription> findByAccountNumber(String accountNumber);

    List<DepositSubscription> findByCustomerCi(String customerCi);

    List<DepositSubscription> findByStatus(String status);

    List<DepositSubscription> findByMaturityDateAndStatus(LocalDate maturityDate, String status);
}