package com.hanainplan.hana.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CustomerRequestDto {

    @NotBlank(message = "CI는 필수입니다.")
    @Pattern(regexp = "^[A-Za-z0-9+/=]{20,64}$", message = "CI 형식이 올바르지 않습니다.")
    private String ci;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다.")
    private String name;

    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F여야 합니다.")
    private String gender;

    @Pattern(regexp = "^\\d{8}$", message = "출생연월일은 YYYYMMDD 형식이어야 합니다.")
    private String birthDate;

    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "전화번호 형식이 올바르지 않습니다. (예: 010-1234-5678)")
    private String phone;
}