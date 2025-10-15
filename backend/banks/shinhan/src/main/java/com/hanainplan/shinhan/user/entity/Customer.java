package com.hanainplan.shinhan.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "shinhan_customers")
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
    private String ci;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "gender", length = 1)
    private String gender;

    @Column(name = "birth_date", length = 8)
    private String birthDate;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "has_irp_account")
    private Boolean hasIrpAccount;

    @Column(name = "irp_account_number", length = 50)
    private String irpAccountNumber;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

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

            if (now.getMonthValue() < birthDateObj.getMonthValue() ||
                (now.getMonthValue() == birthDateObj.getMonthValue() && now.getDayOfMonth() < birthDateObj.getDayOfMonth())) {
                age--;
            }

            return age;

        } catch (Exception e) {
            return 0;
        }
    }

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