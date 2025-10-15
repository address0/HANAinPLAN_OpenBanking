package com.hanainplan.shinhan.product.controller;

import com.hanainplan.shinhan.product.dto.FinancialProductRequestDto;
import com.hanainplan.shinhan.product.dto.FinancialProductResponseDto;
import com.hanainplan.shinhan.product.service.FinancialProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/shinhan/products")
@CrossOrigin(origins = "*")
public class FinancialProductController {

    @Autowired
    private FinancialProductService financialProductService;

    @PostMapping
    public ResponseEntity<FinancialProductResponseDto> createProduct(@Valid @RequestBody FinancialProductRequestDto request) {
        try {
            FinancialProductResponseDto response = financialProductService.createProduct(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{productId}")
    public ResponseEntity<FinancialProductResponseDto> getProductById(@PathVariable Long productId) {
        Optional<FinancialProductResponseDto> product = financialProductService.getProductById(productId);
        return product.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/code/{productCode}")
    public ResponseEntity<FinancialProductResponseDto> getProductByCode(@PathVariable String productCode) {
        Optional<FinancialProductResponseDto> product = financialProductService.getProductByCode(productCode);
        return product.map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping
    public ResponseEntity<List<FinancialProductResponseDto>> getAllProducts() {
        List<FinancialProductResponseDto> products = financialProductService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/active")
    public ResponseEntity<List<FinancialProductResponseDto>> getActiveProducts() {
        List<FinancialProductResponseDto> products = financialProductService.getActiveProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/search")
    public ResponseEntity<List<FinancialProductResponseDto>> searchProductsByName(@RequestParam String name) {
        List<FinancialProductResponseDto> products = financialProductService.searchProductsByName(name);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<List<FinancialProductResponseDto>> getProductsByCategory(@PathVariable String category) {
        List<FinancialProductResponseDto> products = financialProductService.getProductsByCategory(category);
        return ResponseEntity.ok(products);
    }

    @PutMapping("/{productId}")
    public ResponseEntity<FinancialProductResponseDto> updateProduct(
            @PathVariable Long productId, 
            @Valid @RequestBody FinancialProductRequestDto request) {
        try {
            FinancialProductResponseDto response = financialProductService.updateProduct(productId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @PatchMapping("/{productId}/toggle-status")
    public ResponseEntity<FinancialProductResponseDto> toggleProductStatus(@PathVariable Long productId) {
        try {
            FinancialProductResponseDto response = financialProductService.toggleProductStatus(productId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @DeleteMapping("/{productId}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long productId) {
        try {
            financialProductService.deleteProduct(productId);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}