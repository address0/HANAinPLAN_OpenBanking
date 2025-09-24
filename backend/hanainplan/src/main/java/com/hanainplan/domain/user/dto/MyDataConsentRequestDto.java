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
public class MyDataConsentRequestDto {
    
    @NotBlank(message = "전화번호는 필수입니다")
    private String phoneNumber;
    
    @NotBlank(message = "주민등록번호는 필수입니다")
    private String socialNumber;
    
    @NotBlank(message = "이름은 필수입니다")
    private String name;
    
    private boolean consentToMyDataCollection;
    
    // 주민등록번호에서 추출된 정보
    private String birthDate; // YYYYMMDD 형식
    private String gender;    // M 또는 F
}
