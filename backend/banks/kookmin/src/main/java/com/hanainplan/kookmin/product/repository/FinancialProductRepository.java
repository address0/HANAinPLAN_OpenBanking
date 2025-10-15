package com.hanainplan.kookmin.product.repository;

import com.hanainplan.kookmin.product.entity.FinancialProduct;
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

    boolean existsByProductCode(String productCode);

    List<FinancialProduct> findByDepositType(String depositType);

    List<FinancialProduct> findByIsActiveTrue();

    @Query("SELECT p FROM FinancialProduct p WHERE p.depositType = :depositType AND p.isActive = true")
    List<FinancialProduct> findByDepositTypeAndActive(@Param("depositType") String depositType);

    @Query("SELECT p FROM FinancialProduct p WHERE p.startDate <= :date AND p.endDate >= :date AND p.isActive = true")
    List<FinancialProduct> findActiveProductsByDate(@Param("date") LocalDate date);

    @Query("SELECT p FROM FinancialProduct p WHERE p.subscriptionAmount <= :maxAmount AND p.isActive = true")
    List<FinancialProduct> findByMaxSubscriptionAmount(@Param("maxAmount") String maxAmount);

    @Query("SELECT p FROM FinancialProduct p WHERE p.productName LIKE %:keyword% AND p.isActive = true")
    List<FinancialProduct> findByProductNameContaining(@Param("keyword") String keyword);

    List<FinancialProduct> findByProductNameContainingIgnoreCase(String productName);

    List<FinancialProduct> findByProductCategory(String productCategory);
}