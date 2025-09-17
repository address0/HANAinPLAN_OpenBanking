package com.hanainplan.domain.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 사용자 엔터티
 * - 하나인플랜 서비스 사용자 정보 관리
 * - 로그인 및 인증에 사용
 */
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

    @Column(name = "social_number", length = 8, nullable = false, unique = true)
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
    private String kakaoId; // 카카오 OAuth ID

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

    /**
     * 사용자 타입 enum 정의
     */
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

    /**
     * 성별 enum 정의
     */
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

    /**
     * 로그인 타입 enum 정의
     */
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

    /**
     * 주민번호에서 생년월일과 성별 추출
     */
    public static LocalDate extractBirthDateFromSocialNumber(String socialNumber) {
        if (socialNumber == null || socialNumber.length() != 8) {
            throw new IllegalArgumentException("주민번호 형식이 올바르지 않습니다.");
        }
        
        String birthPart = socialNumber.substring(0, 6);
        String genderPart = socialNumber.substring(7, 8);
        
        int year = Integer.parseInt(birthPart.substring(0, 2));
        int month = Integer.parseInt(birthPart.substring(2, 4));
        int day = Integer.parseInt(birthPart.substring(4, 6));
        
        // 성별 코드로 세기 판단
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

    /**
     * 주민번호에서 성별 추출
     */
    public static Gender extractGenderFromSocialNumber(String socialNumber) {
        if (socialNumber == null || socialNumber.length() != 8) {
            throw new IllegalArgumentException("주민번호 형식이 올바르지 않습니다.");
        }
        
        int genderCode = Integer.parseInt(socialNumber.substring(7, 8));
        return (genderCode % 2 == 1) ? Gender.M : Gender.F;
    }

    /**
     * 성인 여부 확인
     */
    public boolean isAdult() {
        return birthDate != null && birthDate.isBefore(LocalDate.now().minusYears(18));
    }

    /**
     * 최근 로그인 업데이트
     */
    public void updateLastLogin() {
        this.lastLoginDate = LocalDateTime.now();
        this.updatedDate = LocalDateTime.now();
    }

    /**
     * 업데이트 시간 갱신
     */
    @PreUpdate
    public void preUpdate() {
        this.updatedDate = LocalDateTime.now();
    }
}
