package com.hanainplan.hana.customer.controller;

import com.hanainplan.hana.customer.dto.CustomerRequestDto;
import com.hanainplan.hana.customer.dto.CustomerResponseDto;
import com.hanainplan.hana.customer.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/hana/customers")
@RequiredArgsConstructor
@Tag(name = "HANA 고객 관리", description = "HANA 보험사의 고객 CRUD API")
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping
    @Operation(summary = "고객 생성", description = "새로운 고객을 생성합니다.")
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto requestDto) {
        try {
            CustomerResponseDto response = customerService.createCustomer(requestDto);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ci/{ci}")
    @Operation(summary = "CI로 고객 조회", description = "CI로 고객을 조회합니다.")
    public ResponseEntity<CustomerResponseDto> getCustomerByCi(@PathVariable String ci) {
        try {
            CustomerResponseDto response = customerService.getCustomerByCi(ci);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/{customerId}")
    @Operation(summary = "고객 ID로 조회", description = "고객 ID로 고객을 조회합니다.")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long customerId) {
        try {
            CustomerResponseDto response = customerService.getCustomerById(customerId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping
    @Operation(summary = "전체 고객 목록 조회", description = "모든 고객 목록을 조회합니다.")
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        List<CustomerResponseDto> responses = customerService.getAllCustomers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/active")
    @Operation(summary = "활성화된 고객 목록 조회", description = "활성화된 고객 목록을 조회합니다.")
    public ResponseEntity<List<CustomerResponseDto>> getActiveCustomers() {
        List<CustomerResponseDto> responses = customerService.getActiveCustomers();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/type/{customerType}")
    @Operation(summary = "고객 유형별 조회", description = "특정 고객 유형의 고객 목록을 조회합니다.")
    public ResponseEntity<List<CustomerResponseDto>> getCustomersByType(@PathVariable String customerType) {
        List<CustomerResponseDto> responses = customerService.getCustomersByType(customerType);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/risk-level/{riskLevel}")
    @Operation(summary = "위험등급별 고객 조회", description = "특정 위험등급의 고객 목록을 조회합니다.")
    public ResponseEntity<List<CustomerResponseDto>> getCustomersByRiskLevel(@PathVariable String riskLevel) {
        List<CustomerResponseDto> responses = customerService.getCustomersByRiskLevel(riskLevel);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/gender/{gender}")
    @Operation(summary = "성별 고객 조회", description = "특정 성별의 고객 목록을 조회합니다.")
    public ResponseEntity<List<CustomerResponseDto>> getCustomersByGender(@PathVariable String gender) {
        List<CustomerResponseDto> responses = customerService.getCustomersByGender(gender);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search/name")
    @Operation(summary = "고객명 검색", description = "고객명 키워드로 고객을 검색합니다.")
    public ResponseEntity<List<CustomerResponseDto>> searchCustomersByName(@RequestParam String keyword) {
        List<CustomerResponseDto> responses = customerService.searchCustomersByName(keyword);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search/occupation")
    @Operation(summary = "직업별 고객 검색", description = "직업 키워드로 고객을 검색합니다.")
    public ResponseEntity<List<CustomerResponseDto>> searchCustomersByOccupation(@RequestParam String keyword) {
        List<CustomerResponseDto> responses = customerService.searchCustomersByOccupation(keyword);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/search/address")
    @Operation(summary = "주소별 고객 검색", description = "주소 키워드로 고객을 검색합니다.")
    public ResponseEntity<List<CustomerResponseDto>> searchCustomersByAddress(@RequestParam String keyword) {
        List<CustomerResponseDto> responses = customerService.searchCustomersByAddress(keyword);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/ci/{ci}")
    @Operation(summary = "고객 정보 수정", description = "기존 고객 정보를 수정합니다.")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable String ci,
            @Valid @RequestBody CustomerRequestDto requestDto) {
        try {
            CustomerResponseDto response = customerService.updateCustomer(ci, requestDto);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PutMapping("/ci/{ci}/toggle-status")
    @Operation(summary = "고객 상태 변경", description = "고객의 활성화/비활성화 상태를 변경합니다.")
    public ResponseEntity<CustomerResponseDto> toggleCustomerStatus(@PathVariable String ci) {
        try {
            CustomerResponseDto response = customerService.toggleCustomerStatus(ci);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/ci/{ci}")
    @Operation(summary = "고객 삭제", description = "고객을 삭제합니다.")
    public ResponseEntity<Void> deleteCustomer(@PathVariable String ci) {
        try {
            customerService.deleteCustomer(ci);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
