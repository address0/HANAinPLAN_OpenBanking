package com.hanainplan.kookmin.product.service;

import com.hanainplan.kookmin.product.dto.ProductSubscriptionRequestDto;
import com.hanainplan.kookmin.product.dto.ProductSubscriptionResponseDto;
import com.hanainplan.kookmin.product.entity.FinancialProduct;
import com.hanainplan.kookmin.product.entity.IrpProduct;
import com.hanainplan.kookmin.product.entity.ProductSubscription;
import com.hanainplan.kookmin.product.repository.FinancialProductRepository;
import com.hanainplan.kookmin.product.repository.IrpProductRepository;
import com.hanainplan.kookmin.product.repository.ProductSubscriptionRepository;
import com.hanainplan.kookmin.user.entity.Customer;
import com.hanainplan.kookmin.user.repository.CustomerRepository;
import com.hanainplan.kookmin.account.entity.Account;
import com.hanainplan.kookmin.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProductSubscriptionService {

    @Autowired
    private ProductSubscriptionRepository productSubscriptionRepository;

    @Autowired
    private FinancialProductRepository financialProductRepository;

    @Autowired
    private IrpProductRepository irpProductRepository;

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountRepository accountRepository;

    /**
     * 금융상품 가입
     */
    public ProductSubscriptionResponseDto subscribeToFinancialProduct(ProductSubscriptionRequestDto request) {
        // 1. 고객 존재 확인
        Customer customer = customerRepository.findByCi(request.getCustomerCi())
            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerCi()));

        // 2. 계좌 존재 확인
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + request.getAccountNumber()));

        // 3. 계좌 소유자 확인
        if (!account.getCustomer().getCi().equals(request.getCustomerCi())) {
            throw new IllegalArgumentException("계좌 소유자가 일치하지 않습니다.");
        }

        // 4. 금융상품 존재 확인
        FinancialProduct product = financialProductRepository.findByProductCode(request.getProductCode())
            .orElseThrow(() -> new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + request.getProductCode()));

        // 5. 중복 가입 확인
        if (productSubscriptionRepository.existsByCustomerCiAndProductCode(request.getCustomerCi(), request.getProductCode())) {
            throw new IllegalArgumentException("이미 해당 상품에 가입되어 있습니다.");
        }

        // 6. 상품 가입 생성
        ProductSubscription subscription = ProductSubscription.builder()
            .customerCi(request.getCustomerCi())
            .productCode(request.getProductCode())
            .accountNumber(request.getAccountNumber())
            .status(request.getStatus())
            .subscriptionDate(request.getSubscriptionDate())
            .maturityDate(request.getMaturityDate())
            .contractPeriod(request.getContractPeriod())
            .rateType(request.getRateType())
            .baseRate(request.getBaseRate())
            .preferentialRate(request.getPreferentialRate())
            .finalAppliedRate(request.getFinalAppliedRate())
            .preferentialReason(request.getPreferentialReason())
            .interestCalculationBasis(request.getInterestCalculationBasis())
            .interestPaymentMethod(request.getInterestPaymentMethod())
            .interestType(request.getInterestType())
            .contractPrincipal(request.getContractPrincipal())
            .currentBalance(request.getCurrentBalance())
            .unpaidInterest(request.getUnpaidInterest())
            .unpaidTax(request.getUnpaidTax())
            .lastInterestCalculationDate(request.getLastInterestCalculationDate())
            .nextInterestPaymentDate(request.getNextInterestPaymentDate())
            .branchName(request.getBranchName())
            .monthlyPaymentAmount(request.getMonthlyPaymentAmount())
            .monthlyPaymentDay(request.getMonthlyPaymentDay())
            .totalInstallments(request.getTotalInstallments())
            .completedInstallments(request.getCompletedInstallments())
            .missedInstallments(request.getMissedInstallments())
            .build();

        ProductSubscription savedSubscription = productSubscriptionRepository.save(subscription);
        return ProductSubscriptionResponseDto.from(savedSubscription);
    }

    /**
     * IRP 상품 가입
     */
    public ProductSubscriptionResponseDto subscribeToIrpProduct(ProductSubscriptionRequestDto request) {
        // 1. 고객 존재 확인
        Customer customer = customerRepository.findByCi(request.getCustomerCi())
            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerCi()));

        // 2. 계좌 존재 확인
        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + request.getAccountNumber()));

        // 3. 계좌 소유자 확인
        if (!account.getCustomer().getCi().equals(request.getCustomerCi())) {
            throw new IllegalArgumentException("계좌 소유자가 일치하지 않습니다.");
        }

        // 4. IRP 상품 존재 확인
        IrpProduct irpProduct = irpProductRepository.findByProductCode(request.getProductCode())
            .orElseThrow(() -> new IllegalArgumentException("IRP 상품을 찾을 수 없습니다: " + request.getProductCode()));

        // 5. 중복 가입 확인
        if (productSubscriptionRepository.existsByCustomerCiAndProductCode(request.getCustomerCi(), request.getProductCode())) {
            throw new IllegalArgumentException("이미 해당 IRP 상품에 가입되어 있습니다.");
        }

        // 6. IRP 상품 가입 생성
        ProductSubscription subscription = ProductSubscription.builder()
            .customerCi(request.getCustomerCi())
            .productCode(request.getProductCode())
            .accountNumber(request.getAccountNumber())
            .status(request.getStatus())
            .subscriptionDate(request.getSubscriptionDate())
            .maturityDate(request.getMaturityDate())
            .contractPeriod(request.getContractPeriod())
            .rateType(request.getRateType())
            .baseRate(request.getBaseRate())
            .preferentialRate(request.getPreferentialRate())
            .finalAppliedRate(request.getFinalAppliedRate())
            .preferentialReason(request.getPreferentialReason())
            .interestCalculationBasis(request.getInterestCalculationBasis())
            .interestPaymentMethod(request.getInterestPaymentMethod())
            .interestType(request.getInterestType())
            .contractPrincipal(request.getContractPrincipal())
            .currentBalance(request.getCurrentBalance())
            .unpaidInterest(request.getUnpaidInterest())
            .unpaidTax(request.getUnpaidTax())
            .lastInterestCalculationDate(request.getLastInterestCalculationDate())
            .nextInterestPaymentDate(request.getNextInterestPaymentDate())
            .branchName(request.getBranchName())
            .monthlyPaymentAmount(request.getMonthlyPaymentAmount())
            .monthlyPaymentDay(request.getMonthlyPaymentDay())
            .totalInstallments(request.getTotalInstallments())
            .completedInstallments(request.getCompletedInstallments())
            .missedInstallments(request.getMissedInstallments())
            .build();

        ProductSubscription savedSubscription = productSubscriptionRepository.save(subscription);
        return ProductSubscriptionResponseDto.from(savedSubscription);
    }

    /**
     * 고객별 가입 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ProductSubscriptionResponseDto> getSubscriptionsByCustomerCi(String customerCi) {
        List<ProductSubscription> subscriptions = productSubscriptionRepository.findByCustomerCiOrderBySubscriptionDateDesc(customerCi);
        return subscriptions.stream()
            .map(ProductSubscriptionResponseDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 계좌별 가입 조회
     */
    @Transactional(readOnly = true)
    public Optional<ProductSubscriptionResponseDto> getSubscriptionByAccountNumber(String accountNumber) {
        return productSubscriptionRepository.findByAccountNumber(accountNumber)
            .map(ProductSubscriptionResponseDto::from);
    }

    /**
     * 상품별 가입 목록 조회
     */
    @Transactional(readOnly = true)
    public List<ProductSubscriptionResponseDto> getSubscriptionsByProductCode(String productCode) {
        List<ProductSubscription> subscriptions = productSubscriptionRepository.findByProductCode(productCode);
        return subscriptions.stream()
            .map(ProductSubscriptionResponseDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 가입 상태 변경
     */
    public ProductSubscriptionResponseDto updateSubscriptionStatus(Long subscriptionId, String newStatus) {
        ProductSubscription subscription = productSubscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("가입 정보를 찾을 수 없습니다: " + subscriptionId));

        subscription.setStatus(newStatus);
        ProductSubscription updatedSubscription = productSubscriptionRepository.save(subscription);
        return ProductSubscriptionResponseDto.from(updatedSubscription);
    }

    /**
     * 가입 해지
     */
    public void cancelSubscription(Long subscriptionId) {
        ProductSubscription subscription = productSubscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("가입 정보를 찾을 수 없습니다: " + subscriptionId));

        subscription.setStatus("CANCELLED");
        productSubscriptionRepository.save(subscription);
    }
}
