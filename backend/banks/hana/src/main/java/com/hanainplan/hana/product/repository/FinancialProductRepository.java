package com.hanainplan.hana.product.repository;

import com.hanainplan.hana.product.entity.FinancialProduct;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FinancialProductRepository extends JpaRepository<FinancialProduct, Long> {

    Optional<FinancialProduct> findByProductCode(String productCode);
    
    boolean existsByProductCode(String productCode);

    List<FinancialProduct> findByDepositType(String depositType);

    // isActive 필드가 삭제되어 관련 메서드들 제거됨
    
    List<FinancialProduct> findByProductNameContainingIgnoreCase(String productName);
    
    List<FinancialProduct> findByProductCategory(String productCategory);
}
