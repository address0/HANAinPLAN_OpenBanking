package com.hanainplan.shinhan.product.service;

import com.hanainplan.shinhan.product.dto.ProductSubscriptionRequestDto;
import com.hanainplan.shinhan.product.dto.ProductSubscriptionResponseDto;
import com.hanainplan.shinhan.product.entity.FinancialProduct;
import com.hanainplan.shinhan.product.entity.IrpProduct;
import com.hanainplan.shinhan.product.entity.ProductSubscription;
import com.hanainplan.shinhan.product.repository.FinancialProductRepository;
import com.hanainplan.shinhan.product.repository.IrpProductRepository;
import com.hanainplan.shinhan.product.repository.ProductSubscriptionRepository;
import com.hanainplan.shinhan.product.repository.InterestRateRepository;
import com.hanainplan.shinhan.product.entity.InterestRate;
import com.hanainplan.shinhan.user.entity.Customer;
import com.hanainplan.shinhan.user.repository.CustomerRepository;
import com.hanainplan.shinhan.account.entity.Account;
import com.hanainplan.shinhan.account.repository.AccountRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Autowired
    private InterestRateRepository interestRateRepository;

    public ProductSubscriptionResponseDto subscribeToFinancialProduct(ProductSubscriptionRequestDto request) {
        Customer customer = customerRepository.findByCi(request.getCustomerCi())
            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerCi()));

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + request.getAccountNumber()));

        if (!account.getCustomerCi().equals(request.getCustomerCi())) {
            throw new IllegalArgumentException("계좌 소유자가 일치하지 않습니다.");
        }

        FinancialProduct product = financialProductRepository.findByProductCode(request.getProductCode())
            .orElseThrow(() -> new IllegalArgumentException("금융상품을 찾을 수 없습니다: " + request.getProductCode()));

        if (productSubscriptionRepository.existsByCustomerCiAndProductCode(request.getCustomerCi(), request.getProductCode())) {
            throw new IllegalArgumentException("이미 해당 상품에 가입되어 있습니다.");
        }

        InterestRate basicRate = interestRateRepository
            .findLatestBasicRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElseThrow(() -> new IllegalArgumentException("해당 상품의 기본금리 정보를 찾을 수 없습니다: " + request.getProductCode() + ", " + request.getMaturityPeriod()));

        InterestRate preferentialRate = interestRateRepository
            .findLatestPreferentialRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElse(null);

        BigDecimal finalRate = (preferentialRate != null) ? preferentialRate.getInterestRate() : basicRate.getInterestRate();

        ProductSubscription subscription = ProductSubscription.builder()
            .customerCi(request.getCustomerCi())
            .productCode(request.getProductCode())
            .accountNumber(request.getAccountNumber())
            .status(request.getStatus())
            .subscriptionDate(request.getSubscriptionDate())
            .maturityDate(request.getMaturityDate())
            .contractPeriod(request.getContractPeriod())
            .maturityPeriod(request.getMaturityPeriod())
            .rateType(request.getRateType())
            .baseRate(basicRate.getInterestRate())
            .preferentialRate(preferentialRate != null ? preferentialRate.getInterestRate() : null)
            .finalAppliedRate(finalRate)
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

    public ProductSubscriptionResponseDto subscribeToIrpProduct(ProductSubscriptionRequestDto request) {
        Customer customer = customerRepository.findByCi(request.getCustomerCi())
            .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerCi()));

        Account account = accountRepository.findByAccountNumber(request.getAccountNumber())
            .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + request.getAccountNumber()));

        if (!account.getCustomerCi().equals(request.getCustomerCi())) {
            throw new IllegalArgumentException("계좌 소유자가 일치하지 않습니다.");
        }

        IrpProduct irpProduct = irpProductRepository.findByProductCode(request.getProductCode())
            .orElseThrow(() -> new IllegalArgumentException("IRP 상품을 찾을 수 없습니다: " + request.getProductCode()));

        if (productSubscriptionRepository.existsByCustomerCiAndProductCode(request.getCustomerCi(), request.getProductCode())) {
            throw new IllegalArgumentException("이미 해당 IRP 상품에 가입되어 있습니다.");
        }

        InterestRate irpRate = interestRateRepository
            .findLatestBasicRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElseThrow(() -> new IllegalArgumentException("해당 IRP 상품의 금리 정보를 찾을 수 없습니다: " + request.getProductCode() + ", " + request.getMaturityPeriod()));

        InterestRate irpPreferentialRate = interestRateRepository
            .findLatestPreferentialRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElse(null);

        BigDecimal finalIrpRate = (irpPreferentialRate != null) ? irpPreferentialRate.getInterestRate() : irpRate.getInterestRate();

        ProductSubscription subscription = ProductSubscription.builder()
            .customerCi(request.getCustomerCi())
            .productCode(request.getProductCode())
            .accountNumber(request.getAccountNumber())
            .status(request.getStatus())
            .subscriptionDate(request.getSubscriptionDate())
            .maturityDate(request.getMaturityDate())
            .contractPeriod(request.getContractPeriod())
            .maturityPeriod(request.getMaturityPeriod())
            .rateType(request.getRateType())
            .baseRate(irpRate.getInterestRate())
            .preferentialRate(irpPreferentialRate != null ? irpPreferentialRate.getInterestRate() : null)
            .finalAppliedRate(finalIrpRate)
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

    @Transactional(readOnly = true)
    public List<ProductSubscriptionResponseDto> getSubscriptionsByCustomerCi(String customerCi) {
        List<ProductSubscription> subscriptions = productSubscriptionRepository.findByCustomerCiOrderBySubscriptionDateDesc(customerCi);
        return subscriptions.stream()
            .map(ProductSubscriptionResponseDto::from)
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<ProductSubscriptionResponseDto> getSubscriptionByAccountNumber(String accountNumber) {
        return productSubscriptionRepository.findByAccountNumber(accountNumber)
            .map(ProductSubscriptionResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<ProductSubscriptionResponseDto> getSubscriptionsByProductCode(String productCode) {
        List<ProductSubscription> subscriptions = productSubscriptionRepository.findByProductCode(productCode);
        return subscriptions.stream()
            .map(ProductSubscriptionResponseDto::from)
            .collect(Collectors.toList());
    }

    public ProductSubscriptionResponseDto updateSubscriptionStatus(Long subscriptionId, String newStatus) {
        ProductSubscription subscription = productSubscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("가입 정보를 찾을 수 없습니다: " + subscriptionId));

        subscription.setStatus(newStatus);
        ProductSubscription updatedSubscription = productSubscriptionRepository.save(subscription);
        return ProductSubscriptionResponseDto.from(updatedSubscription);
    }

    public void cancelSubscription(Long subscriptionId) {
        ProductSubscription subscription = productSubscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new IllegalArgumentException("가입 정보를 찾을 수 없습니다: " + subscriptionId));

        subscription.setStatus("CANCELLED");
        productSubscriptionRepository.save(subscription);
    }
}