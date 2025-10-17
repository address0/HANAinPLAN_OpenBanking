package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCustomerId(Long customerId);

    boolean existsByCustomerId(Long customerId);
    
    List<Customer> findByHasIrpAccountTrue();
}