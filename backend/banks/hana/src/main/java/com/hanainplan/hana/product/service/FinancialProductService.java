package com.hanainplan.hana.product.service;

import com.hanainplan.hana.product.dto.FinancialProductRequestDto;
import com.hanainplan.hana.product.dto.FinancialProductResponseDto;
import com.hanainplan.hana.product.entity.FinancialProduct;
import com.hanainplan.hana.product.repository.FinancialProductRepository;
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

    /**
     * 금융상품 생성
     */
    public FinancialProductResponseDto createProduct(FinancialProductRequestDto request) {
        // 상품코드 중복 확인
        if (financialProductRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("이미 존재하는 상품코드입니다: " + request.getProductCode());
        }

        FinancialProduct product = FinancialProduct.builder()
            .productCode(request.getProductCode())
            .productName(request.getProductName())
            .depositType(request.getDepositType())
            .minContractPeriod(request.getMinContractPeriod())
            .maxContractPeriod(request.getMaxContractPeriod())
            .contractPeriodUnit(request.getContractPeriodUnit())
            .subscriptionTarget(request.getSubscriptionTarget())
            .subscriptionAmount(request.getSubscriptionAmount())
            .productCategory(request.getProductCategory())
            .interestPayment(request.getInterestPayment())
            .taxBenefit(request.getTaxBenefit())
            .partialWithdrawal(request.getPartialWithdrawal())
            .cancellationPenalty(request.getCancellationPenalty())
            .description(request.getDescription())
            .build();

        FinancialProduct savedProduct = financialProductRepository.save(product);
        return FinancialProductResponseDto.from(savedProduct);
    }

    /**
     * 금융상품 조회 (ID)
     */
    @Transactional(readOnly = true)
    public Optional<FinancialProductResponseDto> getProductById(Long productId) {
        return financialProductRepository.findById(productId)
            .map(FinancialProductResponseDto::from);
    }

    /**
     * 금융상품 조회 (상품코드)
     */
    @Transactional(readOnly = true)
    public Optional<FinancialProductResponseDto> getProductByCode(String productCode) {
        return financialProductRepository.findByProductCode(productCode)
            .map(FinancialProductResponseDto::from);
    }

    /**
     * 모든 금융상품 조회
     */
    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> getAllProducts() {
        List<FinancialProduct> products = financialProductRepository.findAll();
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    // isActive 필드가 삭제되어 활성화된 상품 조회 기능 제거됨

    /**
     * 상품명으로 검색
     */
    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> searchProductsByName(String productName) {
        List<FinancialProduct> products = financialProductRepository.findByProductNameContainingIgnoreCase(productName);
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 상품유형으로 검색
     */
    @Transactional(readOnly = true)
    public List<FinancialProductResponseDto> getProductsByCategory(String productCategory) {
        List<FinancialProduct> products = financialProductRepository.findByProductCategory(productCategory);
        return products.stream()
            .map(FinancialProductResponseDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 금융상품 수정
     */
    public FinancialProductResponseDto updateProduct(Long productId, FinancialProductRequestDto request) {
        FinancialProduct product = financialProductRepository.findById(productId)
            .orElseThrow(() -> new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + productId));

        // 상품코드 변경 시 중복 확인
        if (!product.getProductCode().equals(request.getProductCode()) && 
            financialProductRepository.existsByProductCode(request.getProductCode())) {
            throw new IllegalArgumentException("이미 존재하는 상품코드입니다: " + request.getProductCode());
        }

        product.setProductCode(request.getProductCode());
        product.setProductName(request.getProductName());
        product.setDepositType(request.getDepositType());
        product.setMinContractPeriod(request.getMinContractPeriod());
        product.setMaxContractPeriod(request.getMaxContractPeriod());
        product.setContractPeriodUnit(request.getContractPeriodUnit());
        product.setSubscriptionTarget(request.getSubscriptionTarget());
        product.setSubscriptionAmount(request.getSubscriptionAmount());
        product.setProductCategory(request.getProductCategory());
        product.setInterestPayment(request.getInterestPayment());
        product.setTaxBenefit(request.getTaxBenefit());
        product.setPartialWithdrawal(request.getPartialWithdrawal());
        product.setCancellationPenalty(request.getCancellationPenalty());
        product.setDescription(request.getDescription());

        FinancialProduct updatedProduct = financialProductRepository.save(product);
        return FinancialProductResponseDto.from(updatedProduct);
    }

    // isActive 필드가 삭제되어 활성화/비활성화 기능 제거됨

    /**
     * 금융상품 삭제
     */
    public void deleteProduct(Long productId) {
        if (!financialProductRepository.existsById(productId)) {
            throw new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + productId);
        }
        financialProductRepository.deleteById(productId);
    }
}

