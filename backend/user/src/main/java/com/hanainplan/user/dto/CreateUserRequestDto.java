package com.hanainplan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateUserRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 100, message = "이름은 2-100자 사이여야 합니다.")
    private String name;

    @Pattern(regexp = "^01[0-9]-\\d{4}-\\d{4}$", message = "전화번호는 010-XXXX-XXXX 형식이어야 합니다.")
    private String phone;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])$", 
             message = "생년월일은 YYYYMMDD 형식이어야 합니다.")
    private String birthDate;

    @NotBlank(message = "성별은 필수입니다.")
    @Pattern(regexp = "^(M|F|MALE|FEMALE|남|여|남성|여성)$", 
             message = "성별은 M(남성) 또는 F(여성)이어야 합니다.")
    private String gender;

    @NotBlank(message = "주민번호는 필수입니다.")
    @Pattern(regexp = "^\\d{13}$", message = "주민번호는 13자리 숫자여야 합니다.")
    private String residentNumber;

    public CreateUserRequestDto() {}

    public CreateUserRequestDto(String name, String phone, 
                               String birthDate, String gender, String residentNumber) {
        this.name = name;
        this.phone = phone;
        this.birthDate = birthDate;
        this.gender = gender;
        this.residentNumber = residentNumber;
    }

}