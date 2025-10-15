package com.hanainplan.domain.common.controller;

import com.hanainplan.domain.common.entity.DiseaseCode;
import com.hanainplan.domain.common.service.DiseaseCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/diseases")
@RequiredArgsConstructor
@Slf4j
public class DiseaseCodeController {

    private final DiseaseCodeService diseaseCodeService;

    @GetMapping
    public ResponseEntity<List<DiseaseCode>> getAllDiseases() {
        log.info("GET /api/diseases - 모든 질병 조회 요청");

        try {
            List<DiseaseCode> diseases = diseaseCodeService.getAllDiseases();
            log.info("질병 데이터 {}개 반환", diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("질병 데이터 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<DiseaseCode>> searchDiseases(@RequestParam String keyword) {
        log.info("GET /api/diseases/search?keyword={} - 질병 검색 요청", keyword);

        try {
            List<DiseaseCode> diseases = diseaseCodeService.searchDiseases(keyword);
            log.info("검색 결과 {}개 반환", diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("질병 검색 중 오류 발생: keyword={}", keyword, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/{diseaseCode}")
    public ResponseEntity<DiseaseCode> getDiseaseByCode(@PathVariable String diseaseCode) {
        log.info("GET /api/diseases/{} - 질병 상세 조회 요청", diseaseCode);

        try {
            Optional<DiseaseCode> disease = diseaseCodeService.getDiseaseByCode(diseaseCode);
            if (disease.isPresent()) {
                log.info("질병 코드 {} 조회 성공", diseaseCode);
                return ResponseEntity.ok(disease.get());
            } else {
                log.warn("질병 코드 {}를 찾을 수 없음", diseaseCode);
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("질병 상세 조회 중 오류 발생: diseaseCode={}", diseaseCode, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<DiseaseCode>> getDiseasesByCategory(@PathVariable String category) {
        log.info("GET /api/diseases/category/{} - 분류별 질병 조회 요청", category);

        try {
            List<DiseaseCode> diseases = diseaseCodeService.getDiseasesByCategory(category);
            log.info("분류 '{}' 질병 {}개 반환", category, diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("분류별 질병 조회 중 오류 발생: category={}", category, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/risk/{riskLevel}")
    public ResponseEntity<List<DiseaseCode>> getDiseasesByRiskLevel(@PathVariable String riskLevel) {
        log.info("GET /api/diseases/risk/{} - 위험등급별 질병 조회 요청", riskLevel);

        try {
            List<DiseaseCode> diseases = diseaseCodeService.getDiseasesByRiskLevel(riskLevel);
            log.info("위험등급 '{}' 질병 {}개 반환", riskLevel, diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("위험등급별 질병 조회 중 오류 발생: riskLevel={}", riskLevel, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/insurable")
    public ResponseEntity<List<DiseaseCode>> getInsurableDiseases() {
        log.info("GET /api/diseases/insurable - 보험 가입 가능한 질병 조회 요청");

        try {
            List<DiseaseCode> diseases = diseaseCodeService.getInsurableDiseases();
            log.info("보험 가입 가능한 질병 {}개 반환", diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("보험 가입 가능한 질병 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/uninsurable")
    public ResponseEntity<List<DiseaseCode>> getUninsurableDiseases() {
        log.info("GET /api/diseases/uninsurable - 보험 가입 불가능한 질병 조회 요청");

        try {
            List<DiseaseCode> diseases = diseaseCodeService.getUninsurableDiseases();
            log.info("보험 가입 불가능한 질병 {}개 반환", diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("보험 가입 불가능한 질병 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/categories")
    public ResponseEntity<List<String>> getAllCategories() {
        log.info("GET /api/diseases/categories - 모든 질병 분류 조회 요청");

        try {
            List<String> categories = diseaseCodeService.getAllCategories();
            log.info("질병 분류 {}개 반환", categories.size());
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            log.error("질병 분류 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/high-risk")
    public ResponseEntity<List<DiseaseCode>> getHighRiskDiseases() {
        log.info("GET /api/diseases/high-risk - 고위험 질병 조회 요청");

        try {
            List<DiseaseCode> diseases = diseaseCodeService.getHighRiskDiseases();
            log.info("고위험 질병 {}개 반환", diseases.size());
            return ResponseEntity.ok(diseases);
        } catch (Exception e) {
            log.error("고위험 질병 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}