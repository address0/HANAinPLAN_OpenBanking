package com.hanainplan.domain.auth.controller;

import com.hanainplan.domain.auth.dto.CiVerificationRequestDto;
import com.hanainplan.domain.auth.dto.CiVerificationResponseDto;
import com.hanainplan.domain.auth.dto.LoginRequestDto;
import com.hanainplan.domain.auth.dto.LoginResponseDto;
import com.hanainplan.domain.auth.dto.KakaoUserInfoDto;
import com.hanainplan.domain.auth.service.AuthService;
import com.hanainplan.domain.auth.service.KakaoAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "인증 API", description = "로그인, 로그아웃 등 인증 관련 API")
public class AuthController {

    private final AuthService authService;
    private final KakaoAuthService kakaoAuthService;

    @Autowired
    public AuthController(AuthService authService, KakaoAuthService kakaoAuthService) {
        this.authService = authService;
        this.kakaoAuthService = kakaoAuthService;
    }

    @PostMapping("/login")
    @Operation(summary = "로그인", description = "전화번호와 비밀번호를 이용한 로그인")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "로그인 성공/실패", 
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<LoginResponseDto> login(
        @Parameter(description = "로그인 요청 정보", required = true)
        @Valid @RequestBody LoginRequestDto loginRequest
    ) {
        try {
            LoginResponseDto response = authService.loginWithPhoneAndPassword(loginRequest);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LoginResponseDto errorResponse = new LoginResponseDto(false, "로그인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/kakao/url")
    @Operation(summary = "카카오 로그인 URL 조회", description = "카카오 OAuth 인증 URL을 반환합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "카카오 로그인 URL 조회 성공"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<?> getKakaoLoginUrl() {
        try {
            String authUrl = kakaoAuthService.getKakaoAuthUrl();
            return ResponseEntity.ok()
                .body(new java.util.HashMap<String, Object>() {{
                    put("success", true);
                    put("url", authUrl);
                }});
        } catch (Exception e) {
            return ResponseEntity.ok()
                .body(new java.util.HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "카카오 로그인 URL 생성에 실패했습니다.");
                }});
        }
    }

    @GetMapping("/kakao/callback")
    @Operation(summary = "카카오 OAuth 콜백 처리", description = "카카오 OAuth 인증 후 콜백을 처리합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "카카오 로그인 성공/실패"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 인증 코드"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<?> handleKakaoCallback(
        @Parameter(description = "카카오 OAuth 인증 코드", required = true)
        @RequestParam String code
    ) {
        try {
            KakaoUserInfoDto kakaoUserInfo = kakaoAuthService.handleKakaoCallback(code);

            if (kakaoUserInfo.isSuccess()) {
                return ResponseEntity.ok()
                    .body(new java.util.HashMap<String, Object>() {{
                        put("success", true);
                        put("message", "카카오 로그인 성공");
                        put("id", kakaoUserInfo.getId());
                        put("nickname", kakaoUserInfo.getNickname());
                        put("email", kakaoUserInfo.getEmail());
                        put("profileImage", kakaoUserInfo.getProfileImage());
                    }});
            } else {
                return ResponseEntity.ok()
                    .body(new java.util.HashMap<String, Object>() {{
                        put("success", false);
                        put("message", kakaoUserInfo.getMessage());
                    }});
            }
        } catch (Exception e) {
            return ResponseEntity.ok()
                .body(new java.util.HashMap<String, Object>() {{
                    put("success", false);
                    put("message", "카카오 로그인 처리 중 오류가 발생했습니다.");
                }});
        }
    }

    @PostMapping("/kakao-login")
    @Operation(summary = "카카오 로그인", description = "카카오 계정을 이용한 로그인")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "카카오 로그인 성공/실패", 
            content = @Content(schema = @Schema(implementation = LoginResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<LoginResponseDto> kakaoLogin(
        @Parameter(description = "카카오 사용자 ID", required = true)
        @RequestParam String kakaoId
    ) {
        try {
            LoginResponseDto response = authService.loginWithKakao(kakaoId);
            return ResponseEntity.ok(response);

        } catch (Exception e) {
            LoginResponseDto errorResponse = new LoginResponseDto(false, "카카오 로그인 처리 중 오류가 발생했습니다.");
            return ResponseEntity.ok(errorResponse);
        }
    }

    @GetMapping("/check-user")
    @Operation(summary = "사용자 존재 여부 확인", description = "전화번호로 사용자 존재 여부를 확인합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "확인 완료"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터"
        )
    })
    public ResponseEntity<Boolean> checkUserExists(
        @Parameter(description = "전화번호", required = true, example = "01012345678")
        @RequestParam String phoneNumber
    ) {
        try {
            boolean exists = authService.isUserExistsByPhoneNumber(phoneNumber);
            return ResponseEntity.ok(exists);

        } catch (Exception e) {
            return ResponseEntity.ok(false);
        }
    }

    @PostMapping("/logout")
    @Operation(summary = "로그아웃", description = "사용자 로그아웃 처리")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "로그아웃 성공"
        )
    })
    public ResponseEntity<LoginResponseDto> logout() {
        LoginResponseDto response = new LoginResponseDto(true, "로그아웃이 완료되었습니다.");
        return ResponseEntity.ok(response);
    }

    @PostMapping("/ci/verify")
    @Operation(summary = "CI 검증", description = "이름과 주민번호를 이용한 CI 실명인증")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "CI 검증 성공/실패",
            content = @Content(schema = @Schema(implementation = CiVerificationResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400",
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "500",
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<CiVerificationResponseDto> verifyCi(
        @Parameter(description = "CI 검증 요청 정보", required = true)
        @Valid @RequestBody CiVerificationRequestDto request
    ) {
        try {
            CiVerificationResponseDto response = authService.verifyCi(request);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            CiVerificationResponseDto errorResponse = CiVerificationResponseDto.failure("CI 검증 처리 중 오류가 발생했습니다.", "SYSTEM_ERROR");
            return ResponseEntity.ok(errorResponse);
        }
    }
}