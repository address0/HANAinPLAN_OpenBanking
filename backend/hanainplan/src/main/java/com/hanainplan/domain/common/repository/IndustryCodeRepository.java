package com.hanainplan.domain.common.repository;

import com.hanainplan.domain.common.entity.IndustryCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IndustryCodeRepository extends JpaRepository<IndustryCode, String> {

    @Query("SELECT ic FROM IndustryCode ic WHERE " +
           "(LOWER(ic.industryName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(ic.industryCode) LIKE LOWER(CONCAT('%', :keyword, '%'))) " +
           "ORDER BY ic.industryName")
    List<IndustryCode> findByIndustryNameContainingIgnoreCaseOrIndustryCodeContainingIgnoreCase(@Param("keyword") String keyword);
}