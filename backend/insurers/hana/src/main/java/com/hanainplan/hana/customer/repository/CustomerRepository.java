package com.hanainplan.hana.customer.repository;

import com.hanainplan.hana.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
    
    Optional<Customer> findByCi(String ci);
    
    Optional<Customer> findByPhone(String phone);
    
    Optional<Customer> findByEmail(String email);
    
    boolean existsByCi(String ci);
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
    
    List<Customer> findByIsActiveTrue();
    
    List<Customer> findByCustomerType(String customerType);
    
    List<Customer> findByRiskLevel(String riskLevel);
    
    List<Customer> findByMaritalStatus(String maritalStatus);
    
    @Query("SELECT c FROM Customer c WHERE c.name LIKE %:keyword% AND c.isActive = true")
    List<Customer> findByNameContaining(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Customer c WHERE c.occupation LIKE %:keyword% AND c.isActive = true")
    List<Customer> findByOccupationContaining(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Customer c WHERE c.address LIKE %:keyword% AND c.isActive = true")
    List<Customer> findByAddressContaining(@Param("keyword") String keyword);
    
    @Query("SELECT c FROM Customer c WHERE c.gender = :gender AND c.isActive = true")
    List<Customer> findByGenderAndActive(@Param("gender") String gender);
    
    @Query("SELECT c FROM Customer c WHERE c.customerType = :customerType AND c.isActive = true")
    List<Customer> findByCustomerTypeAndActive(@Param("customerType") String customerType);
    
    @Query("SELECT c FROM Customer c WHERE c.riskLevel = :riskLevel AND c.isActive = true")
    List<Customer> findByRiskLevelAndActive(@Param("riskLevel") String riskLevel);
}
