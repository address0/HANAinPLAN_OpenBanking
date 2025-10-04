package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.InterestRate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InterestRateRepository extends JpaRepository<InterestRate, Long> {

    /**
     * 모든 금리 정보 조회
     */
    List<InterestRate> findAll();

    /**
     * 은행 코드로 금리 조회
     */
    List<InterestRate> findByBankCode(String bankCode);

    /**
     * 은행 코드와 만기 기간으로 금리 조회
     */
    List<InterestRate> findByBankCodeAndMaturityPeriod(String bankCode, String maturityPeriod);

    /**
     * 만기 기간으로 금리 조회
     */
    List<InterestRate> findByMaturityPeriod(String maturityPeriod);

    /**
     * 금리 종류로 금리 조회
     */
    List<InterestRate> findByInterestType(String interestType);
}



