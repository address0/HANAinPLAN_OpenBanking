package com.hanainplan.domain.common.repository;

import com.hanainplan.domain.common.entity.DiseaseCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 질병코드 Repository
 * - 질병 데이터 조회 및 검색 기능 제공
 */
@Repository
public interface DiseaseCodeRepository extends JpaRepository<DiseaseCode, String> {

    /**
     * 질병명으로 검색
     * @param keyword 검색 키워드
     * @return 검색된 질병 목록
     */
    @Query("SELECT d FROM DiseaseCode d WHERE d.diseaseName LIKE %:keyword% ORDER BY d.diseaseName")
    List<DiseaseCode> findByDiseaseNameContainingIgnoreCase(@Param("keyword") String keyword);

    /**
     * 질병 분류별 조회
     * @param category 질병 분류
     * @return 해당 분류의 질병 목록
     */
    List<DiseaseCode> findByDiseaseCategoryOrderByDiseaseName(String category);

    /**
     * 위험등급별 조회
     * @param riskLevel 위험등급 (상, 중, 하)
     * @return 해당 위험등급의 질병 목록
     */
    List<DiseaseCode> findByRiskLevelOrderByDiseaseName(String riskLevel);

    /**
     * 보험 가입 가능 여부별 조회
     * @param isInsurable 가입 가능 여부 (Y/N)
     * @return 해당 조건의 질병 목록
     */
    List<DiseaseCode> findByIsInsurableOrderByDiseaseName(String isInsurable);

    /**
     * 질병명 또는 분류로 통합 검색
     * @param keyword 검색 키워드
     * @return 검색된 질병 목록
     */
    @Query("SELECT d FROM DiseaseCode d WHERE d.diseaseName LIKE %:keyword% OR d.diseaseCategory LIKE %:keyword% ORDER BY d.diseaseName")
    List<DiseaseCode> findByDiseaseNameOrCategoryContaining(@Param("keyword") String keyword);

    /**
     * 모든 질병 분류 조회
     * @return 질병 분류 목록
     */
    @Query("SELECT DISTINCT d.diseaseCategory FROM DiseaseCode d ORDER BY d.diseaseCategory")
    List<String> findAllCategories();
}

