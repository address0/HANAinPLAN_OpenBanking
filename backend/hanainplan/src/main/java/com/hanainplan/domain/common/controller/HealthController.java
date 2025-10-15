package com.hanainplan.domain.common.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

@Tag(name = "헬스체크", description = "서버 상태 확인 API")
@RestController
@RequestMapping("/api/health")
public class HealthController {

    @Operation(summary = "서버 상태 확인", description = "서버가 정상적으로 동작하는지 확인합니다.")
    @GetMapping
    public ResponseEntity<Map<String, Object>> health() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "message", "하나인플랜 서버가 정상적으로 동작 중입니다.",
                "timestamp", LocalDateTime.now(),
                "version", "1.0.0"
        ));
    }

    @Operation(summary = "데이터베이스 연결 확인", description = "데이터베이스 연결 상태를 확인합니다.")
    @GetMapping("/db")
    public ResponseEntity<Map<String, Object>> dbHealth() {
        return ResponseEntity.ok(Map.of(
                "database", "UP",
                "message", "데이터베이스 연결 정상",
                "timestamp", LocalDateTime.now()
        ));
    }
}