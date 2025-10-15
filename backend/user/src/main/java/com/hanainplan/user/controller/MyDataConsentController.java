package com.hanainplan.user.controller;

import com.hanainplan.user.dto.MyDataConsentRequestDto;
import com.hanainplan.user.dto.MyDataConsentResponseDto;
import com.hanainplan.user.service.MyDataConsentService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user/mydata")
@CrossOrigin(origins = "*")
public class MyDataConsentController {

    @Autowired
    private MyDataConsentService myDataConsentService;

    @PostMapping("/consent")
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