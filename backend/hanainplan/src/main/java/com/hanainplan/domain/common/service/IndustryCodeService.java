package com.hanainplan.domain.common.service;

import com.hanainplan.domain.common.entity.IndustryCode;
import com.hanainplan.domain.common.repository.IndustryCodeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class IndustryCodeService {

    private final IndustryCodeRepository industryCodeRepository;

    public List<IndustryCode> getAllIndustries() {
        log.debug("모든 업종코드 조회");
        return industryCodeRepository.findAll();
    }

    public List<IndustryCode> searchIndustriesByKeyword(String keyword) {
        log.debug("키워드로 업종코드 검색: {}", keyword);

        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllIndustries();
        }

        return industryCodeRepository.findByIndustryNameContainingIgnoreCaseOrIndustryCodeContainingIgnoreCase(keyword.trim());
    }

    public Optional<IndustryCode> getIndustryByCode(String industryCode) {
        log.debug("업종코드로 조회: {}", industryCode);
        return industryCodeRepository.findById(industryCode);
    }

}