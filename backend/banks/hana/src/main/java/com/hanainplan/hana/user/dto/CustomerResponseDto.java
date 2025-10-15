package com.hanainplan.hana.user.dto;

import com.hanainplan.hana.user.entity.Customer;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class CustomerResponseDto {

    private Long userId;
    private String ci;
    private String name;
    private String gender;
    private String birthDate;
    private String phone;
    private LocalDateTime createdAt;

    public CustomerResponseDto() {}

    public CustomerResponseDto(Customer customer) {
        this.userId = customer.getUserId();
        this.ci = customer.getCi();
        this.name = customer.getName();
        this.gender = customer.getGender();
        this.birthDate = customer.getBirthDate();
        this.phone = customer.getPhone();
        this.createdAt = customer.getCreatedAt();
    }

    public static CustomerResponseDto from(Customer customer) {
        return new CustomerResponseDto(customer);
    }
}