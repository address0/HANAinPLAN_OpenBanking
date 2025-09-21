package com.hanainplan.hana.customer.dto;

import com.hanainplan.hana.customer.entity.Customer;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CustomerResponseDto {
    private Long customerId;
    private String ci;
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private String email;
    private String address;
    private String occupation;
    private String maritalStatus;
    private String customerType;
    private String riskLevel;
    private Boolean isActive;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CustomerResponseDto fromEntity(Customer entity) {
        CustomerResponseDto dto = new CustomerResponseDto();
        dto.setCustomerId(entity.getCustomerId());
        dto.setCi(entity.getCi());
        dto.setName(entity.getName());
        dto.setGender(entity.getGender());
        dto.setBirthDate(entity.getBirthDate());
        dto.setPhone(entity.getPhone());
        dto.setEmail(entity.getEmail());
        dto.setAddress(entity.getAddress());
        dto.setOccupation(entity.getOccupation());
        dto.setMaritalStatus(entity.getMaritalStatus());
        dto.setCustomerType(entity.getCustomerType());
        dto.setRiskLevel(entity.getRiskLevel());
        dto.setIsActive(entity.getIsActive());
        dto.setCreatedAt(entity.getCreatedAt());
        dto.setUpdatedAt(entity.getUpdatedAt());
        return dto;
    }
}
