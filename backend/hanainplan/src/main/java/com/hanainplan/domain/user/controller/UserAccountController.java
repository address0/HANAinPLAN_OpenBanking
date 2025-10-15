package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.CustomerAccountInfoDto;
import com.hanainplan.domain.user.dto.MyDataConsentRequestDto;
import com.hanainplan.domain.user.dto.MyDataConsentResponseDto;
import com.hanainplan.domain.user.dto.SaveAccountsRequest;
import com.hanainplan.domain.user.service.UserAccountTransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/account")
@Tag(name = "사용자 계좌 통합", description = "사용자 정보와 계좌 정보를 함께 처리하는 API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class UserAccountController {

    private final UserAccountTransactionService userAccountTransactionService;

    @PutMapping("/{userId}/additional-info")
    @Operation(summary = "사용자 추가 정보 업데이트", description = "사용자 추가 정보 입력 시 계좌 정보도 함께 업데이트합니다")
    public ResponseEntity<String> updateUserWithAccountInfo(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "추가 정보") @RequestBody String additionalInfo) {

        log.info("사용자 추가 정보 업데이트 API 호출 - 사용자 ID: {}, 추가 정보: {}", userId, additionalInfo);

        try {
            userAccountTransactionService.updateUserWithAccountInfo(userId, additionalInfo);
            return ResponseEntity.ok("사용자 추가 정보가 성공적으로 업데이트되었습니다.");
        } catch (Exception e) {
            log.error("사용자 추가 정보 업데이트 실패", e);
            return ResponseEntity.internalServerError().body("사용자 추가 정보 업데이트 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/{userId}/save-accounts")
    @Operation(summary = "계좌 정보 직접 저장", description = "프론트엔드에서 받은 계좌 정보를 직접 저장합니다")
    public ResponseEntity<String> saveAccounts(
            @Parameter(description = "사용자 ID") @PathVariable Long userId,
            @Parameter(description = "계좌 정보") @RequestBody SaveAccountsRequest request) {

        log.info("계좌 정보 직접 저장 API 호출 - 사용자 ID: {}", userId);

        try {
            userAccountTransactionService.saveAccountsDirectly(userId, request.getBankAccountInfo());
            return ResponseEntity.ok("계좌 정보가 성공적으로 저장되었습니다.");
        } catch (Exception e) {
            log.error("계좌 정보 저장 실패", e);
            return ResponseEntity.internalServerError().body("계좌 정보 저장 중 오류가 발생했습니다.");
        }
    }
}