package com.hanainplan.hana.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "hana_customers")
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

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
