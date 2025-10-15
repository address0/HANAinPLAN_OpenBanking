package com.hanainplan.domain.user.repository;

import com.hanainplan.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findBySocialNumber(String socialNumber);

    Optional<User> findByPhoneNumber(String phoneNumber);

    Optional<User> findByKakaoId(String kakaoId);

    Optional<User> findByEmail(String email);

    boolean existsBySocialNumber(String socialNumber);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByKakaoId(String kakaoId);

    boolean existsByEmail(String email);
}