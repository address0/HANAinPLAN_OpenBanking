package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.DepositProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DepositProductRepository extends JpaRepository<DepositProduct, String> {

    List<DepositProduct> findByBankCode(String bankCode);

    List<DepositProduct> findByBankName(String bankName);

    List<DepositProduct> findByNameContaining(String keyword);

    List<DepositProduct> findByBankCodeAndNameContaining(String bankCode, String keyword);
}