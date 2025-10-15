package com.hanainplan.domain.common.service;

import com.hanainplan.domain.common.entity.DiseaseCode;
import com.hanainplan.domain.common.repository.DiseaseCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class DiseaseCodeService {

    private final DiseaseCodeRepository diseaseCodeRepository;

    public List<DiseaseCode> getAllDiseases() {
        log.info("모든 질병 데이터 조회 시작");
        List<DiseaseCode> diseases = diseaseCodeRepository.findAll();
        log.info("질병 데이터 {}개 조회 완료", diseases.size());
        return diseases;
    }

    public Optional<DiseaseCode> getDiseaseByCode(String diseaseCode) {
        log.info("질병 코드로 조회: {}", diseaseCode);
        return diseaseCodeRepository.findById(diseaseCode);
    }

    public List<DiseaseCode> searchDiseases(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            log.warn("검색 키워드가 비어있음. 전체 목록 반환");
            return getAllDiseases();
        }

        log.info("질병 검색 시작: {}", keyword);
        List<DiseaseCode> diseases = diseaseCodeRepository.findByDiseaseNameOrCategoryContaining(keyword.trim());
        log.info("키워드 '{}' 검색 결과: {}개", keyword, diseases.size());
        return diseases;
    }

    public List<DiseaseCode> getDiseasesByCategory(String category) {
        log.info("질병 분류별 조회: {}", category);
        List<DiseaseCode> diseases = diseaseCodeRepository.findByDiseaseCategoryOrderByDiseaseName(category);
        log.info("분류 '{}' 질병 {}개 조회", category, diseases.size());
        return diseases;
    }

    public List<DiseaseCode> getDiseasesByRiskLevel(String riskLevel) {
        log.info("위험등급별 조회: {}", riskLevel);
        List<DiseaseCode> diseases = diseaseCodeRepository.findByRiskLevelOrderByDiseaseName(riskLevel);
        log.info("위험등급 '{}' 질병 {}개 조회", riskLevel, diseases.size());
        return diseases;
    }

    public List<DiseaseCode> getInsurableDiseases() {
        log.info("보험 가입 가능한 질병 조회");
        List<DiseaseCode> diseases = diseaseCodeRepository.findByIsInsurableOrderByDiseaseName("Y");
        log.info("보험 가입 가능한 질병 {}개 조회", diseases.size());
        return diseases;
    }

    public List<DiseaseCode> getUninsurableDiseases() {
        log.info("보험 가입 불가능한 질병 조회");
        List<DiseaseCode> diseases = diseaseCodeRepository.findByIsInsurableOrderByDiseaseName("N");
        log.info("보험 가입 불가능한 질병 {}개 조회", diseases.size());
        return diseases;
    }

    public List<String> getAllCategories() {
        log.info("모든 질병 분류 조회");
        List<String> categories = diseaseCodeRepository.findAllCategories();
        log.info("질병 분류 {}개 조회", categories.size());
        return categories;
    }

    public List<DiseaseCode> getHighRiskDiseases() {
        return getDiseasesByRiskLevel("상");
    }

    public List<DiseaseCode> getMediumRiskDiseases() {
        return getDiseasesByRiskLevel("중");
    }

    public List<DiseaseCode> getLowRiskDiseases() {
        return getDiseasesByRiskLevel("하");
    }
}