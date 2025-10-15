package com.hanainplan.domain.common.controller;

import com.hanainplan.domain.common.entity.IndustryCode;
import com.hanainplan.domain.common.service.IndustryCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/industries")
@RequiredArgsConstructor
public class IndustryCodeController {

    private final IndustryCodeService industryCodeService;

    @GetMapping
    public ResponseEntity<List<IndustryCode>> getAllIndustries() {
        log.info("모든 업종코드 조회 요청");

        List<IndustryCode> industries = industryCodeService.getAllIndustries();

        log.info("업종코드 조회 완료: {} 개", industries.size());
        return ResponseEntity.ok(industries);
    }

    @GetMapping("/search")
    public ResponseEntity<List<IndustryCode>> searchIndustries(
            @RequestParam(name = "keyword", required = false) String keyword) {
        log.info("업종코드 키워드 검색 요청: {}", keyword);

        List<IndustryCode> industries = industryCodeService.searchIndustriesByKeyword(keyword);

        log.info("업종코드 검색 완료: {} 개", industries.size());
        return ResponseEntity.ok(industries);
    }

}