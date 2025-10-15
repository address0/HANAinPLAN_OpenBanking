package com.hanainplan.domain.common.repository;

import com.hanainplan.domain.common.entity.DiseaseCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiseaseCodeRepository extends JpaRepository<DiseaseCode, String> {

    @Query("SELECT d FROM DiseaseCode d WHERE d.diseaseName LIKE %:keyword% ORDER BY d.diseaseName")
    List<DiseaseCode> findByDiseaseNameContainingIgnoreCase(@Param("keyword") String keyword);

    List<DiseaseCode> findByDiseaseCategoryOrderByDiseaseName(String category);

    List<DiseaseCode> findByRiskLevelOrderByDiseaseName(String riskLevel);

    List<DiseaseCode> findByIsInsurableOrderByDiseaseName(String isInsurable);

    @Query("SELECT d FROM DiseaseCode d WHERE d.diseaseName LIKE %:keyword% OR d.diseaseCategory LIKE %:keyword% ORDER BY d.diseaseName")
    List<DiseaseCode> findByDiseaseNameOrCategoryContaining(@Param("keyword") String keyword);

    @Query("SELECT DISTINCT d.diseaseCategory FROM DiseaseCode d ORDER BY d.diseaseCategory")
    List<String> findAllCategories();
}