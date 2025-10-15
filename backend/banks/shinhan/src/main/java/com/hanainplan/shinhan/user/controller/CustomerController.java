package com.hanainplan.shinhan.user.controller;

import com.hanainplan.shinhan.user.dto.CustomerRequestDto;
import com.hanainplan.shinhan.user.dto.CustomerResponseDto;
import com.hanainplan.shinhan.user.service.CustomerService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shinhan/customers")
@CrossOrigin(origins = "*")
public class CustomerController {

    @Autowired
    private CustomerService customerService;

    @PostMapping
    public ResponseEntity<CustomerResponseDto> createCustomer(@Valid @RequestBody CustomerRequestDto request) {
        try {
            CustomerResponseDto response = customerService.createCustomer(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(null);
        }
    }

    @GetMapping("/ci/{ci}")
    public ResponseEntity<CustomerResponseDto> getCustomerByCi(@PathVariable String ci) {
        Optional<CustomerResponseDto> customer = customerService.getCustomerByCi(ci);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CustomerResponseDto> getCustomerById(@PathVariable Long userId) {
        Optional<CustomerResponseDto> customer = customerService.getCustomerById(userId);
        return customer.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<CustomerResponseDto>> getAllCustomers() {
        List<CustomerResponseDto> customers = customerService.getAllCustomers();
        return ResponseEntity.ok(customers);
    }

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