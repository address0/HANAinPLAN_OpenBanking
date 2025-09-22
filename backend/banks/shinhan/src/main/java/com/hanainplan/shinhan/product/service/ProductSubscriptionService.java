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

        // 6. 금리 정보 조회 (상품코드와 만기기간으로 기본금리 조회)
        InterestRate basicRate = interestRateRepository
            .findLatestBasicRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElseThrow(() -> new IllegalArgumentException("해당 상품의 기본금리 정보를 찾을 수 없습니다: " + request.getProductCode() + ", " + request.getMaturityPeriod()));

        // 7. 우대금리 조회 (선택사항)
        InterestRate preferentialRate = interestRateRepository
            .findLatestPreferentialRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElse(null);

        // 8. 최종 적용금리 계산 (우대금리가 있으면 우대금리, 없으면 기본금리)
        BigDecimal finalRate = (preferentialRate != null) ? preferentialRate.getInterestRate() : basicRate.getInterestRate();

        // 9. 상품 가입 생성
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
            .baseRate(basicRate.getInterestRate()) // 금리 테이블에서 조회한 기본금리
            .preferentialRate(preferentialRate != null ? preferentialRate.getInterestRate() : null) // 우대금리 (있는 경우)
            .finalAppliedRate(finalRate) // 계산된 최종 적용금리
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

        // 6. IRP 금리 정보 조회 (IRP 상품의 경우 isIrp = true로 조회)
        InterestRate irpRate = interestRateRepository
            .findLatestBasicRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElseThrow(() -> new IllegalArgumentException("해당 IRP 상품의 금리 정보를 찾을 수 없습니다: " + request.getProductCode() + ", " + request.getMaturityPeriod()));

        // 7. IRP 우대금리 조회 (선택사항)
        InterestRate irpPreferentialRate = interestRateRepository
            .findLatestPreferentialRateByProductCodeAndMaturityPeriod(request.getProductCode(), request.getMaturityPeriod())
            .orElse(null);

        // 8. 최종 적용금리 계산 (우대금리가 있으면 우대금리, 없으면 기본금리)
        BigDecimal finalIrpRate = (irpPreferentialRate != null) ? irpPreferentialRate.getInterestRate() : irpRate.getInterestRate();

        // 9. IRP 상품 가입 생성
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
            .baseRate(irpRate.getInterestRate()) // IRP 금리 테이블에서 조회한 기본금리
            .preferentialRate(irpPreferentialRate != null ? irpPreferentialRate.getInterestRate() : null) // IRP 우대금리 (있는 경우)
            .finalAppliedRate(finalIrpRate) // 계산된 최종 적용금리
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
