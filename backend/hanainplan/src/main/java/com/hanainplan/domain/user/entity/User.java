package com.hanainplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "tb_user")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_type", nullable = false)
    private UserType userType;

    @Column(name = "user_name", length = 50, nullable = false)
    private String userName;

    @Column(name = "social_number", length = 13, nullable = false, unique = true)
    private String socialNumber;

    @Column(name = "phone_number", length = 13, nullable = false, unique = true)
    private String phoneNumber;

    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", length = 1, nullable = false)
    private Gender gender;

    @Column(name = "email", length = 100)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "kakao_id", length = 100, unique = true)
    private String kakaoId;

    @Column(name = "ci", length = 100)
    private String ci;

    @Enumerated(EnumType.STRING)
    @Column(name = "login_type", nullable = false)
    @Builder.Default
    private LoginType loginType = LoginType.PASSWORD;

    @Column(name = "is_phone_verified")
    @Builder.Default
    private Boolean isPhoneVerified = false;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "created_date", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdDate = LocalDateTime.now();

    @Column(name = "updated_date")
    @Builder.Default
    private LocalDateTime updatedDate = LocalDateTime.now();

    @Column(name = "last_login_date")
    private LocalDateTime lastLoginDate;

    public enum UserType {
        GENERAL("일반고객"), COUNSELOR("상담원");

        private final String description;

        UserType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum Gender {
        M("남성"), F("여성");

        private final String description;

        Gender(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public enum LoginType {
        PASSWORD("비밀번호"), KAKAO("카카오");

        private final String description;

        LoginType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }

    public static LocalDate extractBirthDateFromSocialNumber(String socialNumber) {
        if (socialNumber == null || socialNumber.length() != 13) {
            throw new IllegalArgumentException("주민번호 형식이 올바르지 않습니다.");
        }

        String birthPart = socialNumber.substring(0, 6);
        String genderPart = socialNumber.substring(6, 7);

        int year = Integer.parseInt(birthPart.substring(0, 2));
        int month = Integer.parseInt(birthPart.substring(2, 4));
        int day = Integer.parseInt(birthPart.substring(4, 6));

        int genderCode = Integer.parseInt(genderPart);
        if (genderCode == 1 || genderCode == 2) {
            year += 1900;
        } else if (genderCode == 3 || genderCode == 4) {
            year += 2000;
        } else {
            throw new IllegalArgumentException("유효하지 않은 주민번호입니다.");
        }

        return LocalDate.of(year, month, day);
    }

    public static Gender extractGenderFromSocialNumber(String socialNumber) {
        if (socialNumber == null || socialNumber.length() != 13) {
            throw new IllegalArgumentException("주민번호 형식이 올바르지 않습니다.");
        }

        int genderCode = Integer.parseInt(socialNumber.substring(6, 7));
        return (genderCode % 2 == 1) ? Gender.M : Gender.F;
    }

    public boolean isAdult() {
        return birthDate != null && birthDate.isBefore(LocalDate.now().minusYears(18));
    }

    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}