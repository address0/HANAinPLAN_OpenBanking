package com.hanainplan.user.dto;

import com.hanainplan.user.entity.User;
import java.time.LocalDateTime;

public class UserResponseDto {

    private Long id;
    private String name;
    private String phone;
    private String birthDate;
    private String gender;
    private String ci;

    public UserResponseDto() {}

    public UserResponseDto(User user) {
        this.id = user.getId();
        this.name = user.getName();
        this.phone = user.getPhone();
        this.birthDate = user.getBirthDate();
        this.gender = user.getGender();
        this.ci = user.getCi();
    }

    public static UserResponseDto from(User user) {
        return new UserResponseDto(user);
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
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

    public String getCi() {
        return ci;
    }

    public void setCi(String ci) {
        this.ci = ci;
    }
}