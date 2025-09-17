package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 일반고객 Repository
 */
@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    /**
     * 사용자 ID로 고객 조회
     */
    Optional<Customer> findByCustomerId(Long customerId);

    /**
     * 사용자 ID로 고객 존재 여부 확인
     */
    boolean existsByCustomerId(Long customerId);
}
