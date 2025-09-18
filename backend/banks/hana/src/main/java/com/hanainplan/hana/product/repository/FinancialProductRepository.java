package com.hanainplan.hana.product.repository;

import com.hanainplan.hana.product.entity.FinancialProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialProductRepository extends JpaRepository<FinancialProduct, Long> {

    Optional<FinancialProduct> findByProductCode(String productCode);

    List<FinancialProduct> findByProductType(Integer productType);

    List<FinancialProduct> findByIsActiveTrue();

    @Query("SELECT p FROM FinancialProduct p WHERE p.productType = :productType AND p.isActive = true")
    List<FinancialProduct> findByProductTypeAndActive(@Param("productType") Integer productType);

    @Query("SELECT p FROM FinancialProduct p WHERE p.startDate <= :date AND p.endDate >= :date AND p.isActive = true")
    List<FinancialProduct> findActiveProductsByDate(@Param("date") LocalDate date);
}
