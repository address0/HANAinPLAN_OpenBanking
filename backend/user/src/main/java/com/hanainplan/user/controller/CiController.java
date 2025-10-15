package com.hanainplan.user.controller;

import com.hanainplan.user.dto.*;
import com.hanainplan.user.service.CiConversionService;
import com.hanainplan.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/ci")
@Tag(name = "CI 관리", description = "실명인증 CI 변환 및 검증 API")
public class CiController {

    @Autowired
    private CiConversionService ciConversionService;

    @Autowired
    private UserService userService;

    @PostMapping("/convert")
    @Operation(
        summary = "CI 변환", 
        description = "주민번호를 CI로 변환합니다. 실명인증을 위한 일방향 변환을 수행합니다.",
        operationId = "convertToCi"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200", 
            description = "CI 변환 성공",
            content = @Content(
                schema = @Schema(implementation = CiConversionResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = "{\"success\": true, \"message\": \"CI 변환이 성공적으로 완료되었습니다.\", \"ci\": \"ABC123DEF456GHI789\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400", 
            description = "입력 데이터 오류",
            content = @Content(
                schema = @Schema(implementation = CiConversionResponseDto.class),
                examples = @ExampleObject(
                    name = "오류 예시",
                    value = "{\"success\": false, \"message\": \"입력 데이터 오류: 이름은 필수입니다.\", \"ci\": null, \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<CiConversionResponseDto> convertToCi(
        @Valid 
        @RequestBody
        @io.swagger.v3.oas.annotations.media.Schema(implementation = CiConversionRequestDto.class)
        CiConversionRequestDto request) {
        try {
            String ci = ciConversionService.convertToCi(
                request.getName(),
                request.getBirthDate(),
                request.getGender(),
                request.getResidentNumber()
            );

            return ResponseEntity.ok(CiConversionResponseDto.success(ci));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(CiConversionResponseDto.error("입력 데이터 오류: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(CiConversionResponseDto.error("서버 오류: " + e.getMessage()));
        }
    }

    @PostMapping("/verify")
    @Operation(
        summary = "CI 검증",
        description = "입력된 개인정보를 CI로 변환하여 실명인증 사용자 데이터에 존재하는지 검증합니다.",
        operationId = "verifyCi"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "CI 검증 성공",
            content = @Content(
                schema = @Schema(implementation = CiVerificationResponseDto.class),
                examples = @ExampleObject(
                    name = "성공 예시",
                    value = "{\"success\": true, \"message\": \"실명인증 사용자가 확인되었습니다.\", \"verified\": true, \"ci\": \"ABC123DEF456GHI789\", \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "입력 데이터 오류",
            content = @Content(
                schema = @Schema(implementation = CiVerificationResponseDto.class),
                examples = @ExampleObject(
                    name = "오류 예시",
                    value = "{\"success\": false, \"message\": \"입력 데이터 오류: 이름은 필수입니다.\", \"verified\": false, \"ci\": null, \"timestamp\": \"2024-01-15T10:30:00\"}"
                )
            )
        )
    })
    public ResponseEntity<CiVerificationResponseDto> verifyCi(@Valid @RequestBody CiVerificationRequestDto request) {
        try {
            String ci = ciConversionService.convertToCi(
                request.getName(),
                request.getBirthDate(),
                request.getGender(),
                request.getResidentNumber()
            );

            boolean userExists = userService.getUserByCi(ci).isPresent();

            String message = userExists ? "실명인증 사용자가 확인되었습니다." : "실명인증 사용자를 찾을 수 없습니다.";

            return ResponseEntity.ok(CiVerificationResponseDto.success(userExists, message, ci));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                .body(CiVerificationResponseDto.error("입력 데이터 오류: " + e.getMessage()));

        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                .body(CiVerificationResponseDto.error("서버 오류: " + e.getMessage()));
        }
    }

    @GetMapping("/test")
    @Operation(summary = "CI 변환 테스트", description = "개발용 CI 변환 테스트 API")
    public ResponseEntity<CiConversionResponseDto> testCiConversion(
            @RequestParam String name,
            @RequestParam String birthDate,
            @RequestParam String gender,
            @RequestParam String residentNumber) {

        try {
            String ci = ciConversionService.convertToCi(name, birthDate, gender, residentNumber);
            return ResponseEntity.ok(CiConversionResponseDto.success(ci));

        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(CiConversionResponseDto.error("테스트 오류: " + e.getMessage()));
        }
    }
}