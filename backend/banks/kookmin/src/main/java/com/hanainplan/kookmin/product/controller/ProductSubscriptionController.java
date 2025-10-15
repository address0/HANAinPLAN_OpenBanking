package com.hanainplan.kookmin.product.controller;

import com.hanainplan.kookmin.product.dto.ProductSubscriptionRequestDto;
import com.hanainplan.kookmin.product.dto.ProductSubscriptionResponseDto;
import com.hanainplan.kookmin.product.service.ProductSubscriptionService;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/kookmin/product")
@Tag(name = "Kookmin 은행 상품 가입 관리", description = "금융상품 및 IRP 상품 가입 API")
public class ProductSubscriptionController {

    @Autowired
    private ProductSubscriptionService productSubscriptionService;

    @PostMapping("/financial/subscribe")
    @Operation(summary = "금융상품 가입", description = "예금, 적금 등 금융상품에 가입합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "가입 성공",
            content = @Content(schema = @Schema(implementation = ProductSubscriptionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> subscribeToFinancialProduct(@Valid @RequestBody ProductSubscriptionRequestDto request) {
        try {
            ProductSubscriptionResponseDto response = productSubscriptionService.subscribeToFinancialProduct(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("가입 실패", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류", e.getMessage()));
        }
    }

    @PostMapping("/irp/subscribe")
    @Operation(summary = "IRP 상품 가입", description = "개인형퇴직연금(IRP) 상품에 가입합니다.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "가입 성공",
            content = @Content(schema = @Schema(implementation = ProductSubscriptionResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "잘못된 요청 데이터",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500", description = "서버 오류",
            content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<?> subscribeToIrpProduct(@Valid @RequestBody ProductSubscriptionRequestDto request) {
        try {
            ProductSubscriptionResponseDto response = productSubscriptionService.subscribeToIrpProduct(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("가입 실패", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류", e.getMessage()));
        }
    }

    @GetMapping("/subscriptions/customer/{customerCi}")
    @Operation(summary = "고객별 가입 목록 조회", description = "특정 고객의 모든 상품 가입 목록을 조회합니다.")
    public ResponseEntity<List<ProductSubscriptionResponseDto>> getSubscriptionsByCustomer(
            @Parameter(description = "고객 CI", required = true) @PathVariable String customerCi) {
        List<ProductSubscriptionResponseDto> subscriptions = productSubscriptionService.getSubscriptionsByCustomerCi(customerCi);
        return ResponseEntity.ok(subscriptions);
    }

    @GetMapping("/subscriptions/account/{accountNumber}")
    @Operation(summary = "계좌별 가입 조회", description = "특정 계좌의 상품 가입 정보를 조회합니다.")
    public ResponseEntity<?> getSubscriptionByAccount(
            @Parameter(description = "계좌번호", required = true) @PathVariable String accountNumber) {
        Optional<ProductSubscriptionResponseDto> subscription = productSubscriptionService.getSubscriptionByAccountNumber(accountNumber);
        if (subscription.isPresent()) {
            return ResponseEntity.ok(subscription.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/subscriptions/product/{productCode}")
    @Operation(summary = "상품별 가입 목록 조회", description = "특정 상품의 모든 가입 목록을 조회합니다.")
    public ResponseEntity<List<ProductSubscriptionResponseDto>> getSubscriptionsByProduct(
            @Parameter(description = "상품코드", required = true) @PathVariable String productCode) {
        List<ProductSubscriptionResponseDto> subscriptions = productSubscriptionService.getSubscriptionsByProductCode(productCode);
        return ResponseEntity.ok(subscriptions);
    }

    @PutMapping("/subscriptions/{subscriptionId}/status")
    @Operation(summary = "가입 상태 변경", description = "상품 가입 상태를 변경합니다.")
    public ResponseEntity<?> updateSubscriptionStatus(
            @Parameter(description = "가입 ID", required = true) @PathVariable Long subscriptionId,
            @Parameter(description = "새로운 상태", required = true) @RequestParam String status) {
        try {
            ProductSubscriptionResponseDto response = productSubscriptionService.updateSubscriptionStatus(subscriptionId, status);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("상태 변경 실패", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류", e.getMessage()));
        }
    }

    @DeleteMapping("/subscriptions/{subscriptionId}")
    @Operation(summary = "가입 해지", description = "상품 가입을 해지합니다.")
    public ResponseEntity<?> cancelSubscription(
            @Parameter(description = "가입 ID", required = true) @PathVariable Long subscriptionId) {
        try {
            productSubscriptionService.cancelSubscription(subscriptionId);
            return ResponseEntity.ok().body(new SuccessResponse("가입이 성공적으로 해지되었습니다."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("해지 실패", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new ErrorResponse("서버 오류", e.getMessage()));
        }
    }

    public static class ErrorResponse {
        private String error;
        private String message;

        public ErrorResponse(String error, String message) {
            this.error = error;
            this.message = message;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }
        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }

    public static class SuccessResponse {
        private String message;

        public SuccessResponse(String message) {
            this.message = message;
        }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}