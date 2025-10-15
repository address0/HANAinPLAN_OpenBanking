package com.hanainplan.shinhan.user.controller;

import com.hanainplan.shinhan.user.dto.CustomerAccountResponseDto;
import com.hanainplan.shinhan.user.service.CustomerAccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/shinhan/customer-accounts")
@CrossOrigin(origins = "*")
public class CustomerAccountController {

    @Autowired
    private CustomerAccountService customerAccountService;

    @GetMapping("/ci/{ci}")
    public ResponseEntity<CustomerAccountResponseDto> getCustomerAccountInfoByCi(@PathVariable String ci) {
        CustomerAccountResponseDto response = customerAccountService.getCustomerAccountInfoByCi(ci);
        return ResponseEntity.ok(response);
    }
}