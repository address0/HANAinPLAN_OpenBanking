package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 이율보증형 보험 상품 코드 Repository
 */
@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, String> {

    /**
     * 보험사 코드로 보험 상품 조회
     */
    List<InsuranceProduct> findByInsurerCode(String insurerCode);

    /**
     * 보험사명으로 보험 상품 조회
     */
    List<InsuranceProduct> findByInsurerName(String insurerName);

    /**
     * 상품명으로 검색
     */
    List<InsuranceProduct> findByNameContaining(String keyword);

    /**
     * 보험사 코드와 상품명으로 검색
     */
    List<InsuranceProduct> findByInsurerCodeAndNameContaining(String insurerCode, String keyword);
}



