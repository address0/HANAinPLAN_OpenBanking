package com.hanainplan.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PersonalInfoDto {
    private String name;
    private String residentNumber;
    private String gender;
    private String birthDate;
    private String phoneNumber;
    private String email;
    private AddressDto address;
    private String occupation;
    private String maritalStatus;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AddressDto {
    private String zipCode;
    private String address1;
    private String address2;
}

