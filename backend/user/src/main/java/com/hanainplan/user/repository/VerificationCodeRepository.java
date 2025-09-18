package com.hanainplan.user.repository;

import com.hanainplan.user.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

/**
 * 인증번호 Repository
 */
@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    /**
     * 전화번호로 가장 최근 인증번호 조회 (만료되지 않은 것만)
     */
    @Query("SELECT v FROM VerificationCode v WHERE v.phoneNumber = :phoneNumber AND v.expiresAt > :now ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationCode> findLatestByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);

    /**
     * 전화번호와 인증번호로 조회
     */
    Optional<VerificationCode> findByPhoneNumberAndCode(String phoneNumber, String code);

    /**
     * 만료된 인증번호 삭제
     */
    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    /**
     * 특정 전화번호의 기존 인증번호 모두 만료 처리
     */
    @Modifying
    @Query("UPDATE VerificationCode v SET v.expiresAt = :now WHERE v.phoneNumber = :phoneNumber AND v.expiresAt > :now")
    void expireExistingCodes(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);
}
