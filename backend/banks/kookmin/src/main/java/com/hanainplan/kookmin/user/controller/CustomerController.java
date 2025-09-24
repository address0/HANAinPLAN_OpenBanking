package com.hanainplan.kookmin.user.controller;

import com.hanainplan.kookmin.user.dto.CustomerRequestDto;
import com.hanainplan.kookmin.user.dto.CustomerResponseDto;
import com.hanainplan.kookmin.user.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/kookmin/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    /**
     * 고객 생성
     */
    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto request) {
        try {
            CustomerResponseDto response = customerService.createCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null); // 또는 에러 메시지를 포함한 응답
        }
    }

    /**
     * 고객 조회 (CI)
     */
    @GetMapping("/ci/{ci}")
    public ResponseEntity<CustomerResponseDto> getCustomerByCi(@PathVariable String ci) {
        Optional<CustomerResponseDto> customer = customerService.getCustomerByCi(ci);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 고객 조회 (ID)
     */
    @GetMapping("/{userId}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long userId) {
        Optional<CustomerResponseDto> customer = customerService.getCustomerById(userId);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * 모든 고객 조회
     */
    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

    /**
     * 고객 수정
     */
    @PutMapping("/{userId}")
    public ResponseEntity<CustomerResponseDto> updateCustomer(
            @PathVariable Long userId,
            @Valid @RequestBody CustomerRequestDto request) {
        try {
            CustomerResponseDto response = customerService.updateCustomer(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    /**
     * 고객 삭제
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteCustomer(@PathVariable Long userId) {
        try {
            customerService.deleteCustomer(userId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
