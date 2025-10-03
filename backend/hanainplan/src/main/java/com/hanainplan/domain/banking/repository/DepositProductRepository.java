package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.DepositProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 예금 상품 코드 Repository
 */
@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, String> {

    /**
     * 은행 코드로 예금 상품 조회
     */
    List<DepositProduct> findByBankCode(String bankCode);

    /**
     * 은행명으로 예금 상품 조회
     */
    List<DepositProduct> findByBankName(String bankName);

    /**
     * 상품명으로 검색
     */
    List<DepositProduct> findByNameContaining(String keyword);

    /**
     * 은행 코드와 상품명으로 검색
     */
    List<DepositProduct> findByBankCodeAndNameContaining(String bankCode, String keyword);
}



