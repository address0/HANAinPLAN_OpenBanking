package com.hanainplan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyDataConsentRequestDto {

    @NotBlank(message = "전화번호는 필수입니다.")
    private String phoneNumber;

    @NotBlank(message = "주민등록번호는 필수입니다.")
    private String socialNumber;

    @NotBlank(message = "이름은 필수입니다.")
    private String name;

    private boolean consentToMyDataCollection;

    private String birthDate;
    private String gender;

    private Long userId;
}