package com.hanainplan.hanhwa.customer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CustomerRequestDto {

    @NotBlank(message = "CI는 필수입니다.")
    @Size(max = 64, message = "CI는 64자를 초과할 수 없습니다.")
    private String ci;

    @NotBlank(message = "이름은 필수입니다.")
    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다.")
    private String name;

    @Pattern(regexp = "M|F", message = "성별은 M 또는 F여야 합니다.")
    private String gender;

    @Pattern(regexp = "\\d{8}", message = "출생연월일은 8자리 숫자여야 합니다.")
    private String birthDate;

    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    private String phone;

    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
    private String email;

    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다.")
    private String address;

    @Size(max = 100, message = "직업은 100자를 초과할 수 없습니다.")
    private String occupation;

    @Pattern(regexp = "SINGLE|MARRIED|DIVORCED|WIDOWED", message = "혼인상태는 SINGLE, MARRIED, DIVORCED, WIDOWED 중 하나여야 합니다.")
    private String maritalStatus;

    @Pattern(regexp = "INDIVIDUAL|CORPORATE", message = "고객유형은 INDIVIDUAL 또는 CORPORATE여야 합니다.")
    private String customerType;

    @Pattern(regexp = "LOW|MEDIUM|HIGH", message = "위험등급은 LOW, MEDIUM, HIGH 중 하나여야 합니다.")
    private String riskLevel;

    private Boolean isActive;
}
