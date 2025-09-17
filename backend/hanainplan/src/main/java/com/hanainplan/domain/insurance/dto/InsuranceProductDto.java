package com.hanainplan.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceProductDto {
    private String id;
    private String name;
    private String category;
    private String description;
    private String coverage;
    private List<String> benefits;
    private List<String> exclusions;
    private Integer minAge;
    private Integer maxAge;
    private Long minPremium;
    private Long maxPremium;
    private Boolean isActive;
    private LocalDateTime createdAt;
}

