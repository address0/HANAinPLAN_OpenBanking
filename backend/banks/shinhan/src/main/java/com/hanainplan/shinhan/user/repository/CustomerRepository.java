package com.hanainplan.shinhan.user.repository;

import com.hanainplan.shinhan.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCi(String ci);

    boolean existsByCi(String ci);

    Optional<Customer> findByPhone(String phone);
}