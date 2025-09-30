package com.hanainplan.kookmin.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "kookmin_customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "ci", unique = true, nullable = false, length = 64)
    private String ci; // CI 값

    @Column(name = "name", nullable = false, length = 100)
    private String name; // 이름

    @Column(name = "gender", length = 1)
    private String gender; // 성별 (M/F)

    @Column(name = "birth_date", length = 8)
    private String birthDate; // 출생연월일 (YYYYMMDD)

    @Column(name = "phone", length = 20)
    private String phone; // 전화번호

    @Column(name = "has_irp_account")
    private Boolean hasIrpAccount; // IRP 계좌 보유 여부 (default: false)

    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber; // IRP 계좌번호 (보유한 경우)

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    /**
     * 만 나이 계산
     * @return 만 나이
     */
    public int getAge() {
        if (birthDate == null || birthDate.length() != 8) {
            return 0;
        }

        try {
            int birthYear = Integer.parseInt(birthDate.substring(0, 4));
            int birthMonth = Integer.parseInt(birthDate.substring(4, 6));
            int birthDay = Integer.parseInt(birthDate.substring(6, 8));

            LocalDate birthDateObj = LocalDate.of(birthYear, birthMonth, birthDay);
            LocalDate now = LocalDate.now();

            int age = now.getYear() - birthDateObj.getYear();

            // 생일이 지나지 않았으면 1살 빼기
            if (now.getMonthValue() < birthDateObj.getMonthValue() ||
                (now.getMonthValue() == birthDateObj.getMonthValue() && now.getDayOfMonth() < birthDateObj.getDayOfMonth())) {
                age--;
            }

            return age;

        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * 55세 이상 여부 확인
     * @return 55세 이상이면 true
     */
    public boolean isAge55OrOlder() {
        return getAge() >= 55;
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (hasIrpAccount == null) {
            hasIrpAccount = false;
        }
    }
}
