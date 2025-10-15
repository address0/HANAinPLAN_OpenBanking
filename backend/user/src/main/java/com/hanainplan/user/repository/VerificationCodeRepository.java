package com.hanainplan.user.repository;

import com.hanainplan.user.entity.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    @Query("SELECT v FROM VerificationCode v WHERE v.phoneNumber = :phoneNumber AND v.expiresAt > :now ORDER BY v.createdAt DESC LIMIT 1")
    Optional<VerificationCode> findLatestByPhoneNumber(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);

    Optional<VerificationCode> findByPhoneNumberAndCode(String phoneNumber, String code);

    @Modifying
    @Query("DELETE FROM VerificationCode v WHERE v.expiresAt < :now")
    void deleteExpiredCodes(@Param("now") LocalDateTime now);

    @Modifying
    @Query("UPDATE VerificationCode v SET v.expiresAt = :now WHERE v.phoneNumber = :phoneNumber AND v.expiresAt > :now")
    void expireExistingCodes(@Param("phoneNumber") String phoneNumber, @Param("now") LocalDateTime now);
}