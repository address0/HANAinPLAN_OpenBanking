package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.CustomerDiseaseDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 고객 질병 상세 정보 Repository
 */
@Repository
public interface CustomerDiseaseDetailRepository extends JpaRepository<CustomerDiseaseDetail, Long> {

    /**
     * 고객 ID로 질병 상세 정보 목록 조회
     */
    List<CustomerDiseaseDetail> findByCustomerId(Long customerId);

    /**
     * 고객 ID와 질병 코드로 질병 상세 정보 조회
     */
    List<CustomerDiseaseDetail> findByCustomerIdAndDiseaseCode(Long customerId, String diseaseCode);

    /**
     * 고객 ID로 질병 상세 정보 삭제
     */
    void deleteByCustomerId(Long customerId);

    /**
     * 고객 ID로 질병 상세 정보 개수 조회
     */
    long countByCustomerId(Long customerId);
}
