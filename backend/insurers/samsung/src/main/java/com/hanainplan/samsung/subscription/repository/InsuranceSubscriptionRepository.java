package com.hanainplan.samsung.subscription.repository;

import com.hanainplan.samsung.subscription.entity.InsuranceSubscription;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceSubscriptionRepository extends JpaRepository<InsuranceSubscription, Long> {
    
    Optional<InsuranceSubscription> findByPolicyNumber(String policyNumber);
    
    boolean existsByPolicyNumber(String policyNumber);
    
    List<InsuranceSubscription> findByCustomerCi(String customerCi);
    
    List<InsuranceSubscription> findByProductCode(String productCode);
    
    List<InsuranceSubscription> findByProductType(String productType);
    
    List<InsuranceSubscription> findBySubscriptionStatus(String subscriptionStatus);
    
    List<InsuranceSubscription> findByAgentCode(String agentCode);
    
    List<InsuranceSubscription> findByBranchCode(String branchCode);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.customerCi = :customerCi AND s.subscriptionStatus = :status")
    List<InsuranceSubscription> findByCustomerCiAndStatus(@Param("customerCi") String customerCi, @Param("status") String status);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.productCode = :productCode AND s.subscriptionStatus = :status")
    List<InsuranceSubscription> findByProductCodeAndStatus(@Param("productCode") String productCode, @Param("status") String status);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.subscriptionDate BETWEEN :startDate AND :endDate")
    List<InsuranceSubscription> findBySubscriptionDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.maturityDate BETWEEN :startDate AND :endDate")
    List<InsuranceSubscription> findByMaturityDateBetween(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.maturityDate < :currentDate AND s.subscriptionStatus = 'ACTIVE'")
    List<InsuranceSubscription> findExpiredSubscriptions(@Param("currentDate") LocalDate currentDate);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.medicalExamRequired = true AND s.medicalExamResult = 'PENDING'")
    List<InsuranceSubscription> findPendingMedicalExams();
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.paymentFrequency = :frequency AND s.subscriptionStatus = 'ACTIVE'")
    List<InsuranceSubscription> findByPaymentFrequencyAndActive(@Param("frequency") String frequency);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.riskAssessment = :riskLevel AND s.subscriptionStatus = 'ACTIVE'")
    List<InsuranceSubscription> findByRiskAssessmentAndActive(@Param("riskLevel") String riskLevel);
    
    @Query("SELECT s FROM InsuranceSubscription s WHERE s.automaticRenewal = true AND s.subscriptionStatus = 'ACTIVE'")
    List<InsuranceSubscription> findAutoRenewalSubscriptions();
    
    @Query("SELECT COUNT(s) FROM InsuranceSubscription s WHERE s.customerCi = :customerCi AND s.subscriptionStatus = 'ACTIVE'")
    Long countActiveSubscriptionsByCustomer(@Param("customerCi") String customerCi);
    
    @Query("SELECT SUM(s.premiumAmount) FROM InsuranceSubscription s WHERE s.customerCi = :customerCi AND s.subscriptionStatus = 'ACTIVE'")
    BigDecimal getTotalPremiumByCustomer(@Param("customerCi") String customerCi);
}
