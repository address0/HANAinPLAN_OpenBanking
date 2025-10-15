package com.hanainplan.domain.banking.controller;

import com.hanainplan.domain.banking.dto.AccountDto;
import com.hanainplan.domain.banking.service.MyDataAccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/banking/mydata")
@Tag(name = "마이데이터 계좌", description = "마이데이터에서 가져온 계좌 정보 저장 API")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class MyDataAccountController {

    private final MyDataAccountService myDataAccountService;

    @PostMapping("/accounts/save")
    @Operation(summary = "마이데이터 계좌 정보 저장", description = "마이데이터에서 가져온 계좌 정보를 hanainplan 계좌 테이블에 저장합니다")
    public ResponseEntity<List<AccountDto>> saveMyDataAccounts(
            @Parameter(description = "사용자 ID") @RequestParam Long userId,
            @Parameter(description = "고객 CI") @RequestParam String customerCi,
            @Parameter(description = "계좌 정보 목록") @RequestBody List<MyDataAccountService.MyDataAccountInfo> accountInfos) {

        log.info("마이데이터 계좌 정보 저장 API 호출 - 사용자 ID: {}, CI: {}, 계좌 수: {}", 
                userId, customerCi, accountInfos.size());

        try {
            List<AccountDto> savedAccounts = myDataAccountService.saveMyDataAccounts(userId, customerCi, accountInfos);
            return ResponseEntity.ok(savedAccounts);
        } catch (Exception e) {
            log.error("마이데이터 계좌 정보 저장 실패", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}