package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.InsuranceProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface InsuranceProductRepository extends JpaRepository<InsuranceProduct, String> {

    List<InsuranceProduct> findByInsurerCode(String insurerCode);

    List<InsuranceProduct> findByInsurerName(String insurerName);

    List<InsuranceProduct> findByNameContaining(String keyword);

    List<InsuranceProduct> findByInsurerCodeAndNameContaining(String insurerCode, String keyword);
}