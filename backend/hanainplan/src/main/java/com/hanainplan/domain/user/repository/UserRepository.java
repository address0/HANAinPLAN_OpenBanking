package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 사용자 Repository
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * 주민번호로 사용자 조회
     */
    Optional<User> findBySocialNumber(String socialNumber);

    /**
     * 전화번호로 사용자 조회
     */
    Optional<User> findByPhoneNumber(String phoneNumber);

    /**
     * 카카오 ID로 사용자 조회
     */
    Optional<User> findByKakaoId(String kakaoId);

    /**
     * 이메일로 사용자 조회
     */
    Optional<User> findByEmail(String email);

    /**
     * 주민번호 존재 여부 확인
     */
    boolean existsBySocialNumber(String socialNumber);

    /**
     * 전화번호 존재 여부 확인
     */
    boolean existsByPhoneNumber(String phoneNumber);

    /**
     * 카카오 ID 존재 여부 확인
     */
    boolean existsByKakaoId(String kakaoId);

    /**
     * 이메일 존재 여부 확인
     */
    boolean existsByEmail(String email);
}
