package com.hanainplan.shinhan.product.service;

import com.hanainplan.shinhan.product.dto.FinancialProductRequestDto;
import com.hanainplan.shinhan.product.dto.FinancialProductResponseDto;
import com.hanainplan.shinhan.product.entity.FinancialProduct;
import com.hanainplan.shinhan.product.repository.FinancialProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class FinancialProductService {

    @Autowired
    private FinancialProductRepository financialProductRepository;

    public FinancialProductResponseDto createProduct(FinancialProductRequestDto request) {
        if (financialProductRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("이미 존재하는 상품코드입니다: " + request.getProductCode());
        }

        FinancialProduct product = FinancialProduct.builder()
            .productCode(request.getProductCode())
            .productName(request.getProductName())
            .depositType(request.getDepositType())
            .subscriptionTarget(request.getSubscriptionTarget())
            .subscriptionAmount(request.getSubscriptionAmount())
            .productCategory(request.getProductCategory())
            .interestPayment(request.getInterestPayment())
            .taxBenefit(request.getTaxBenefit())
            .partialWithdrawal(request.getPartialWithdrawal())
            .depositorProtection(request.getDepositorProtection())
            .transactionMethod(request.getTransactionMethod())
            .precautions(request.getPrecautions())
            .contractCancellationRight(request.getContractCancellationRight())
            .cancellationPenalty(request.getCancellationPenalty())
            .paymentRestrictions(request.getPaymentRestrictions())
            .isActive(request.getIsActive() != null ? request.getIsActive() : true)
            .startDate(request.getStartDate())
            .endDate(request.getEndDate())
            .build();

        FinancialProduct savedProduct = financialProductRepository.save(product);
        return FinancialProductResponseDto.from(savedProduct);
    }

    @Transactional(readOnly = true)
    public Optional<FinancialProductResponseDto> getProductById(Long productId) {
        return financialProductRepository.findById(productId)
            .map(FinancialProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Optional<FinancialProductResponseDto> getProductByCode(String productCode) {
        return financialProductRepository.findByProductCode(productCode)
            .map(FinancialProductResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> getAllProducts() {
        List<FinancialProduct> products = financialProductRepository.findAll();
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> getActiveProducts() {
        List<FinancialProduct> products = financialProductRepository.findByIsActiveTrue();
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> searchProductsByName(String productName) {
        List<FinancialProduct> products = financialProductRepository.findByProductNameContainingIgnoreCase(productName);
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> getProductsByCategory(String productCategory) {
        List<FinancialProduct> products = financialProductRepository.findByProductCategory(productCategory);
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    public FinancialProductResponseDto updateProduct(Long productId, FinancialProductRequestDto request) {
        FinancialProduct product = financialProductRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + productId));

        if (!product.getProductCode().equals(request.getProductCode()) && 
            financialProductRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("이미 존재하는 상품코드입니다: " + request.getProductCode());
        }

        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setDepositType(request.getDepositType());
        product.setSubscriptionTarget(request.getSubscriptionTarget());
        product.setSubscriptionAmount(request.getSubscriptionAmount());
        product.setProductCategory(request.getProductCategory());
        product.setInterestPayment(request.getInterestPayment());
        product.setTaxBenefit(request.getTaxBenefit());
        product.setPartialWithdrawal(request.getPartialWithdrawal());
        product.setDepositorProtection(request.getDepositorProtection());
        product.setTransactionMethod(request.getTransactionMethod());
        product.setPrecautions(request.getPrecautions());
        product.setContractCancellationRight(request.getContractCancellationRight());
        product.setCancellationPenalty(request.getCancellationPenalty());
        product.setPaymentRestrictions(request.getPaymentRestrictions());
        product.setIsActive(request.getIsActive());
        product.setStartDate(request.getStartDate());
        product.setEndDate(request.getEndDate());

        FinancialProduct updatedProduct = financialProductRepository.save(product);
        return FinancialProductResponseDto.from(updatedProduct);
    }

    public FinancialProductResponseDto toggleProductStatus(Long productId) {
        FinancialProduct product = financialProductRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + productId));

        product.setIsActive(!product.getIsActive());
        FinancialProduct updatedProduct = financialProductRepository.save(product);
        return FinancialProductResponseDto.from(updatedProduct);
    }

    public void deleteProduct(Long productId) {
        if (!financialProductRepository.existsById(productId)) {
            throw new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + productId);
        }
        financialProductRepository.deleteById(productId);
    }
}