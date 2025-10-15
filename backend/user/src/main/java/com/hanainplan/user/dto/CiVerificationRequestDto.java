package com.hanainplan.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class CiVerificationRequestDto {

    @NotBlank(message = "이름은 필수입니다.")
    @Size(min = 2, max = 50, message = "이름은 2-50자 사이여야 합니다.")
    private String name;

    @NotBlank(message = "생년월일은 필수입니다.")
    @Pattern(regexp = "^(19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12][0-9]|3[01])$", 
             message = "생년월일은 YYYYMMDD 형식이어야 합니다.")
    private String birthDate;

    @NotBlank(message = "성별은 필수입니다.")
    @Pattern(regexp = "^[MF]$", message = "성별은 M 또는 F여야 합니다.")
    private String gender;

    @NotBlank(message = "주민번호는 필수입니다.")
    @Pattern(regexp = "^\\d{13}$", message = "주민번호는 13자리 숫자여야 합니다.")
    private String residentNumber;

    public CiVerificationRequestDto() {}

    public CiVerificationRequestDto(String name, String birthDate, String gender, String residentNumber) {
        this.name = name;
        this.birthDate = birthDate;
        this.gender = gender;
        this.residentNumber = residentNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBirthDate() {
        return birthDate;
    }

    public void setBirthDate(String birthDate) {
        this.birthDate = birthDate;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getResidentNumber() {
        return residentNumber;
    }

    public void setResidentNumber(String residentNumber) {
        this.residentNumber = residentNumber;
    }
}