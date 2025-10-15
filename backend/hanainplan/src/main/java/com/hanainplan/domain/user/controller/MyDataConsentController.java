package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.MyDataConsentRequestDto;
import com.hanainplan.domain.user.dto.MyDataConsentResponseDto;
import com.hanainplan.domain.user.service.MyDataConsentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/mydata")
@Tag(name = "마이데이터", description = "마이데이터 수집 동의 및 은행사별 계좌 정보 조회 API")
@CrossOrigin(origins = "*")
@Slf4j
public class MyDataConsentController {

    @Autowired
    private MyDataConsentService myDataConsentService;

    @PostMapping("/consent")
    @Operation(summary = "마이데이터 수집 동의", description = "CI를 기반으로 각 은행사에서 고객 여부를 확인하고 계좌 정보를 조회합니다.")
    public ResponseEntity<MyDataConsentResponseDto> processMyDataConsent(
            @Valid @RequestBody MyDataConsentRequestDto request) {
        try {
            MyDataConsentResponseDto response = myDataConsentService.processMyDataConsent(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}