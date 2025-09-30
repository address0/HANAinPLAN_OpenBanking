package com.hanainplan.hana.user.repository;

import com.hanainplan.hana.user.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {

    Optional<Customer> findByCi(String ci);

    boolean existsByCi(String ci);

    Optional<Customer> findByPhone(String phone);

    /**
     * IRP 계좌를 보유한 고객들 조회
     */
    List<Customer> findByHasIrpAccountTrue();

    /**
     * 특정 IRP 계좌번호를 보유한 고객 조회
     */
    Optional<Customer> findByIrpAccountNumber(String irpAccountNumber);
}
