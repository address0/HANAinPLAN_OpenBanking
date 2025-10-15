package com.hanainplan.domain.user.dto;

import com.hanainplan.domain.user.entity.Consultant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConsultantDto {

    private Long consultantId;
    private String employeeId;
    private String department;
    private String position;
    private String branchCode;
    private String branchName;
    private String branchAddress;
    private BigDecimal branchLatitude;
    private BigDecimal branchLongitude;

    private String licenseType;
    private String licenseNumber;
    private LocalDate licenseIssueDate;
    private LocalDate licenseExpiryDate;

    private String specialization;

    private LocalDate hireDate;
    private String workStatus;

    private Integer maxDailyConsultations;
    private BigDecimal consultationRating;
    private Integer totalConsultations;
    private String consultationStatus;

    private String officePhone;
    private String extension;
    private String workEmail;

    private String userName;
    private String phoneNumber;
    private String email;

    private String experienceYears;
    private Integer experienceYearsInt;

    public static ConsultantDto fromEntity(Consultant consultant) {
        if (consultant == null) {
            return null;
        }

        int years = consultant.calculateExperienceYears();

        return ConsultantDto.builder()
                .consultantId(consultant.getConsultantId())
                .employeeId(consultant.getEmployeeId())
                .department(consultant.getDepartment())
                .position(convertPositionToKorean(consultant.getPosition()))
                .branchCode(consultant.getBranchCode())
                .branchName(consultant.getBranchName())
                .branchAddress(consultant.getBranchAddress())
                .branchLatitude(consultant.getBranchLatitude())
                .branchLongitude(consultant.getBranchLongitude())
                .licenseType(consultant.getLicenseType())
                .licenseNumber(consultant.getLicenseNumber())
                .licenseIssueDate(consultant.getLicenseIssueDate())
                .licenseExpiryDate(consultant.getLicenseExpiryDate())
                .specialization(convertSpecializationToKorean(consultant.getSpecialization()))
                .hireDate(consultant.getHireDate())
                .workStatus(consultant.getWorkStatus() != null ? consultant.getWorkStatus().getDescription() : null)
                .maxDailyConsultations(consultant.getMaxDailyConsultations())
                .consultationRating(consultant.getConsultationRating())
                .totalConsultations(consultant.getTotalConsultations())
                .consultationStatus(consultant.getConsultationStatus() != null ? consultant.getConsultationStatus().getDescription() : null)
                .officePhone(consultant.getOfficePhone())
                .extension(consultant.getExtension())
                .workEmail(consultant.getWorkEmail())
                .experienceYears(years > 0 ? years + "년" : "신입")
                .experienceYearsInt(years)
                .build();
    }

    private static String convertPositionToKorean(String position) {
        if (position == null) return null;

        switch (position.toUpperCase()) {
            case "SENIOR": return "대리";
            case "MANAGER": return "과장";
            case "DIRECTOR": return "부장";
            case "DEPUTY_MANAGER": return "차장";
            case "ASSISTANT": return "주임";
            case "CONSULTANT": return "상담사";
            default: return position;
        }
    }

    private static String convertSpecializationToKorean(String specialization) {
        if (specialization == null || specialization.isEmpty()) {
            return null;
        }

        try {
            String[] specialties = specialization.replaceAll("[\\[\\]\"]", "").split(",");
            StringBuilder result = new StringBuilder("[");

            for (int i = 0; i < specialties.length; i++) {
                String specialty = specialties[i].trim();
                String koreanSpecialty = convertSpecialtyToKorean(specialty);

                if (i > 0) result.append(",");
                result.append("\"").append(koreanSpecialty).append("\"");
            }

            result.append("]");
            return result.toString();
        } catch (Exception e) {
            return specialization;
        }
    }

    private static String convertSpecialtyToKorean(String specialty) {
        if (specialty == null) return null;

        switch (specialty.toUpperCase()) {
            case "PENSION": return "퇴직연금";
            case "INVESTMENT": return "투자상품";
            case "INSURANCE": return "보험상품";
            case "LOAN": return "대출상품";
            case "DEPOSIT": return "예금상품";
            case "FUND": return "펀드상품";
            case "ASSET_MANAGEMENT": return "자산관리";
            case "RETIREMENT_PLANNING": return "노후설계";
            case "TAX_PLANNING": return "세무설계";
            case "ESTATE_PLANNING": return "상속설계";
            case "GENERAL": return "일반상담";
            default: return specialty;
        }
    }

}