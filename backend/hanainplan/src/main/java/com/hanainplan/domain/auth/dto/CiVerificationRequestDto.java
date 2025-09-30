package com.hanainplan.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CI 검증 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiVerificationRequestDto {

    @NotBlank(message = "이름은 필수입니다")
    @Size(min = 2, max = 50, message = "이름은 2자 이상 50자 이하여야 합니다")
    private String name;

    @NotBlank(message = "주민번호는 필수입니다")
    @Size(min = 13, max = 13, message = "주민번호는 13자리여야 합니다")
    private String residentNumber;

    // 8자리 생년월일 (주민번호에서 자동 추출)
    private String birthDate;

    // 성별 (주민번호에서 자동 추출)
    private String gender;
}
