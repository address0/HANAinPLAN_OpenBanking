package com.hanainplan.domain.user.controller;

import com.hanainplan.domain.user.dto.IrpAccountCreateRequest;
import com.hanainplan.domain.user.dto.IrpAccountCreateResponse;
import com.hanainplan.domain.user.service.IrpAccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/irp-account")
@RequiredArgsConstructor
@Slf4j
public class IrpAccountController {

    private final IrpAccountService irpAccountService;

    /**
     * IRP 계좌 개설
     */
    @PostMapping("/create")
    public ResponseEntity<IrpAccountCreateResponse> createIrpAccount(@RequestBody IrpAccountCreateRequest request) {
        log.info("IRP 계좌 개설 요청 - 고객 ID: {}", request.getCustomerId());

        try {
            IrpAccountCreateResponse response = irpAccountService.createIrpAccount(request);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("IRP 계좌 개설 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("IRP 계좌 개설 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * IRP 계좌 해지
     */
    @DeleteMapping("/{customerId}")
    public ResponseEntity<IrpAccountCreateResponse> closeIrpAccount(@PathVariable Long customerId) {
        log.info("IRP 계좌 해지 요청 - 고객 ID: {}", customerId);

        try {
            IrpAccountCreateResponse response = irpAccountService.closeIrpAccount(customerId);
            
            if (response.getSuccess()) {
                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.badRequest().body(response);
            }
        } catch (IllegalArgumentException e) {
            log.error("IRP 계좌 해지 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("IRP 계좌 해지 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * IRP 계좌 정보 조회
     */
    @GetMapping("/{customerId}")
    public ResponseEntity<IrpAccountCreateResponse> getIrpAccount(@PathVariable Long customerId) {
        log.debug("IRP 계좌 정보 조회 요청 - 고객 ID: {}", customerId);

        try {
            IrpAccountCreateResponse response = irpAccountService.getIrpAccount(customerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            log.error("IRP 계좌 정보 조회 실패: {}", e.getMessage());
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            log.error("IRP 계좌 정보 조회 중 오류 발생", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
