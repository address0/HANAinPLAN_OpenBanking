package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.CustomerDiseaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CustomerDiseaseDetailRepository extends JpaRepository<CustomerDiseaseDetail, Long> {

    List<CustomerDiseaseDetail> findByCustomerId(Long customerId);

    List<CustomerDiseaseDetail> findByCustomerIdAndDiseaseCode(Long customerId, String diseaseCode);

    void deleteByCustomerId(Long customerId);

    long countByCustomerId(Long customerId);
}