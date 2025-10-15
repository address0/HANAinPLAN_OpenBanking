package com.hanainplan.user.repository;

import com.hanainplan.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByCi(String ci);

    boolean existsByCi(String ci);
}