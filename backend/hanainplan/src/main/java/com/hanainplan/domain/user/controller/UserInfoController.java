package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.UserInfoResponseDto;
import com.hanainplan.domain.user.dto.UserInfoUpdateRequestDto;
import com.hanainplan.domain.user.dto.PasswordChangeRequestDto;
import com.hanainplan.domain.user.service.UserInfoService;
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

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@Tag(name = "사용자 정보 API", description = "사용자 정보 조회, 수정, 탈퇴 관련 API")
public class UserInfoController {

    private final UserInfoService userInfoService;

    @Autowired
    public UserInfoController(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }

    @GetMapping("/info/{userId}")
    @Operation(summary = "사용자 정보 조회", description = "사용자 ID로 상세 정보를 조회합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "사용자 정보 조회 성공", 
            content = @Content(schema = @Schema(implementation = UserInfoResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<?> getUserInfo(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId
    ) {
        try {
            UserInfoResponseDto userInfo = userInfoService.getUserInfo(userId);
            return ResponseEntity.ok(userInfo);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자 정보 조회 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PutMapping("/info/{userId}")
    @Operation(summary = "사용자 정보 수정", description = "사용자 기본 정보 및 상세 정보를 수정합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "사용자 정보 수정 성공", 
            content = @Content(schema = @Schema(implementation = UserInfoResponseDto.class))
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<?> updateUserInfo(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId,

        @Parameter(description = "사용자 정보 수정 요청", required = true)
        @Valid @RequestBody UserInfoUpdateRequestDto updateRequest
    ) {
        try {
            UserInfoResponseDto updatedUserInfo = userInfoService.updateUserInfo(userId, updateRequest);
            return ResponseEntity.ok(updatedUserInfo);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "사용자 정보 수정 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @PutMapping("/password/{userId}")
    @Operation(summary = "비밀번호 변경", description = "사용자의 비밀번호를 변경합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "비밀번호 변경 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터 또는 현재 비밀번호 불일치"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<?> changePassword(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId,

        @Parameter(description = "비밀번호 변경 요청", required = true)
        @Valid @RequestBody PasswordChangeRequestDto passwordChangeRequest
    ) {
        try {
            boolean success = userInfoService.changePassword(userId, passwordChangeRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "비밀번호가 성공적으로 변경되었습니다." : "비밀번호 변경에 실패했습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "비밀번호 변경 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }

    @DeleteMapping("/account/{userId}")
    @Operation(summary = "계정 탈퇴", description = "사용자 계정을 탈퇴(비활성화)합니다")
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "계정 탈퇴 성공"
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "잘못된 요청 데이터 또는 비밀번호 불일치"
        ),
        @ApiResponse(
            responseCode = "404", 
            description = "사용자를 찾을 수 없음"
        ),
        @ApiResponse(
            responseCode = "500", 
            description = "서버 내부 오류"
        )
    })
    public ResponseEntity<?> deleteAccount(
        @Parameter(description = "사용자 ID", required = true, example = "1")
        @PathVariable Long userId,

        @Parameter(description = "계정 탈퇴 확인용 비밀번호", required = true)
        @RequestParam String password
    ) {
        try {
            boolean success = userInfoService.deleteAccount(userId, password);

            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("message", success ? "계정 탈퇴가 완료되었습니다." : "계정 탈퇴에 실패했습니다.");

            return ResponseEntity.ok(response);

        } catch (RuntimeException e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "계정 탈퇴 처리 중 오류가 발생했습니다.");
            return ResponseEntity.internalServerError().body(errorResponse);
        }
    }
}