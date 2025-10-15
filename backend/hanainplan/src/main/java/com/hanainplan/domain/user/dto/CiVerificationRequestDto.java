package com.hanainplan.domain.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CiVerificationRequestDto {

    @NotBlank(message = "휴대폰 번호는 필수입니다")
    @Pattern(regexp = "^010-\\d{4}-\\d{4}$", message = "휴대폰 번호 형식이 올바르지 않습니다")
    private String phoneNumber;

    @NotBlank(message = "주민번호는 필수입니다")
    @Pattern(regexp = "^\\d{13}$", message = "주민번호는 13자리 숫자여야 합니다")
    private String socialNumber;

    @NotBlank(message = "이름은 필수입니다")
    private String name;
}