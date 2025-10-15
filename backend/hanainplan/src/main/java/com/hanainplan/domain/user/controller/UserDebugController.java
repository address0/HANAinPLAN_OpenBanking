package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Debug", description = "디버깅용 API")
@CrossOrigin(origins = "*")
public class UserDebugController {

    private final UserRepository userRepository;

    @GetMapping("/user/{userId}/email")
    @Operation(summary = "사용자 이메일 정보 확인", description = "특정 사용자의 이메일 정보를 확인합니다.")
    public ResponseEntity<?> getUserEmailInfo(@PathVariable Long userId) {
        try {
            log.info("사용자 이메일 정보 조회 요청 - userId: {}", userId);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다: " + userId));

            return ResponseEntity.ok(Map.of(
                "userId", user.getUserId(),
                "userName", user.getUserName(),
                "email", user.getEmail(),
                "phoneNumber", user.getPhoneNumber(),
                "userType", user.getUserType(),
                "hasEmail", user.getEmail() != null && !user.getEmail().isEmpty()
            ));

        } catch (Exception e) {
            log.error("사용자 이메일 정보 조회 실패 - userId: {}", userId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "사용자 이메일 정보 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }

    @GetMapping("/users/email-info")
    @Operation(summary = "모든 사용자 이메일 정보 확인", description = "모든 사용자의 이메일 정보를 확인합니다.")
    public ResponseEntity<?> getAllUsersEmailInfo() {
        try {
            log.info("모든 사용자 이메일 정보 조회 요청");

            var users = userRepository.findAll();

            var emailInfo = users.stream()
                    .map(user -> Map.of(
                        "userId", user.getUserId(),
                        "userName", user.getUserName(),
                        "email", user.getEmail() != null ? user.getEmail() : "없음",
                        "userType", user.getUserType(),
                        "hasEmail", user.getEmail() != null && !user.getEmail().isEmpty()
                    ))
                    .toList();

            return ResponseEntity.ok(Map.of(
                "totalUsers", users.size(),
                "usersWithEmail", users.stream().filter(u -> u.getEmail() != null && !u.getEmail().isEmpty()).count(),
                "usersWithoutEmail", users.stream().filter(u -> u.getEmail() == null || u.getEmail().isEmpty()).count(),
                "users", emailInfo
            ));

        } catch (Exception e) {
            log.error("모든 사용자 이메일 정보 조회 실패", e);
            return ResponseEntity.internalServerError().body(Map.of(
                "success", false,
                "message", "사용자 이메일 정보 조회 중 오류가 발생했습니다: " + e.getMessage()
            ));
        }
    }
}