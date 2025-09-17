package com.hanainplan.domain.insurance.service;

import com.hanainplan.domain.insurance.dto.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class InsuranceService {

    // 임시 저장소 (실제로는 데이터베이스 사용)
    private final Map<String, InsuranceProductDto> products = new ConcurrentHashMap<>();
    private final Map<String, InsuranceApplicationDto> applications = new ConcurrentHashMap<>();
    private final Set<String> usedResidentNumbers = new HashSet<>();

    @PostConstruct
    public void init() {
        initializeProducts();
    }

    private void initializeProducts() {
        // 생명보험
        products.put("life-001", InsuranceProductDto.builder()
            .id("life-001")
            .name("하나 생명보험")
            .category("생명보험")
            .description("안정적인 생명보장과 저축기능을 함께 제공하는 생명보험입니다.")
            .coverage("사망보험금, 완전장해보험금, 만기보험금 지급")
            .benefits(Arrays.asList("사망 시 보험금 지급", "완전장해 시 보험금 지급", "만기 시 보험금 지급", "적립금 운용"))
            .exclusions(Arrays.asList("자살", "전쟁", "핵폭발", "자연재해"))
            .minAge(0)
            .maxAge(65)
            .minPremium(100000L)
            .maxPremium(5000000L)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());

        // 건강보험
        products.put("health-001", InsuranceProductDto.builder()
            .id("health-001")
            .name("하나 건강보험")
            .category("건강보험")
            .description("질병과 상해로 인한 의료비를 보장하는 건강보험입니다.")
            .coverage("입원비, 수술비, 통원치료비, 특정질병 진단금")
            .benefits(Arrays.asList("입원비 보장", "수술비 보장", "통원치료비 보장", "특정질병 진단금 지급"))
            .exclusions(Arrays.asList("선천적 질환", "자해", "음주운전", "마약복용"))
            .minAge(0)
            .maxAge(70)
            .minPremium(50000L)
            .maxPremium(2000000L)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());

        // 자동차보험
        products.put("auto-001", InsuranceProductDto.builder()
            .id("auto-001")
            .name("하나 자동차보험")
            .category("자동차보험")
            .description("자동차 사고로 인한 피해를 보장하는 자동차보험입니다.")
            .coverage("대인배상, 대물배상, 자기차량손해, 무보험차상해")
            .benefits(Arrays.asList("대인배상 무한", "대물배상 2천만원", "자기차량손해 보장", "무보험차상해 보장"))
            .exclusions(Arrays.asList("음주운전", "무면허운전", "경기용 차량", "과실치사"))
            .minAge(18)
            .maxAge(80)
            .minPremium(200000L)
            .maxPremium(1000000L)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .build());
    }

    public List<InsuranceProductDto> getInsuranceProducts(String category) {
        if (category == null || category.isEmpty()) {
            return new ArrayList<>(products.values());
        }
        return products.values().stream()
            .filter(product -> product.getCategory().equals(category))
            .collect(Collectors.toList());
    }

    public InsuranceProductDto getInsuranceProduct(String productId) {
        InsuranceProductDto product = products.get(productId);
        if (product == null) {
            throw new RuntimeException("Product not found: " + productId);
        }
        return product;
    }

    public PremiumCalculationResponseDto calculatePremium(PremiumCalculationRequestDto request) {
        log.info("Calculating premium for product: {}, age: {}, gender: {}", 
            request.getProductId(), request.getAge(), request.getGender());

        InsuranceProductDto product = getInsuranceProduct(request.getProductId());
        
        // 기본 보험료 계산 (간단한 로직)
        long basePremium = calculateBasePremium(product, request);
        
        // 특약 보험료 계산
        long riderPremium = calculateRiderPremium(request.getRiders());
        
        // 총 보험료
        long totalPremium = basePremium + riderPremium;
        
        // 납입주기 할인 적용
        long discount = calculateDiscount(totalPremium, request.getPaymentFrequency());
        
        // 최종 보험료
        long finalPremium = totalPremium - discount;
        
        // 상세 내역
        List<PremiumBreakdownDto> breakdown = Arrays.asList(
            PremiumBreakdownDto.builder()
                .category("기본보험료")
                .amount(basePremium)
                .description("연령 및 성별 기반 기본 보험료")
                .build(),
            PremiumBreakdownDto.builder()
                .category("특약보험료")
                .amount(riderPremium)
                .description("선택한 특약의 추가 보험료")
                .build(),
            PremiumBreakdownDto.builder()
                .category("납입주기할인")
                .amount(discount)
                .description(request.getPaymentFrequency() + " 납입 할인")
                .build()
        );

        return PremiumCalculationResponseDto.builder()
            .basePremium(basePremium)
            .riderPremium(riderPremium)
            .totalPremium(totalPremium)
            .discount(discount)
            .finalPremium(finalPremium)
            .breakdown(breakdown)
            .build();
    }

    private long calculateBasePremium(InsuranceProductDto product, PremiumCalculationRequestDto request) {
        // 간단한 보험료 계산 로직
        long baseAmount = product.getMinPremium();
        
        // 연령별 요율
        if (request.getAge() > 50) {
            baseAmount *= 1.5;
        } else if (request.getAge() > 40) {
            baseAmount *= 1.2;
        }
        
        // 성별 요율 (여성이 더 저렴)
        if ("F".equals(request.getGender())) {
            baseAmount *= 0.9;
        }
        
        // 가입금액 비례
        double ratio = (double) request.getCoverageAmount() / product.getMinPremium();
        baseAmount = (long) (baseAmount * ratio);
        
        return baseAmount;
    }

    private long calculateRiderPremium(List<String> riders) {
        if (riders == null || riders.isEmpty()) {
            return 0;
        }
        
        Map<String, Long> riderRates = Map.of(
            "accident", 5000L,
            "cancer", 15000L,
            "diabetes", 8000L,
            "heart", 12000L,
            "hospital", 3000L
        );
        
        return riders.stream()
            .mapToLong(rider -> riderRates.getOrDefault(rider, 0L))
            .sum();
    }

    private long calculateDiscount(long totalPremium, String paymentFrequency) {
        Map<String, Double> discountRates = Map.of(
            "MONTHLY", 0.0,
            "QUARTERLY", 0.02,
            "SEMI_ANNUAL", 0.05,
            "ANNUAL", 0.1
        );
        
        double discountRate = discountRates.getOrDefault(paymentFrequency, 0.0);
        return (long) (totalPremium * discountRate);
    }

    public String submitInsuranceApplication(InsuranceApplicationDto application) {
        String applicationId = generateApplicationId();
        
        application.setId(applicationId);
        application.setStatus("SUBMITTED");
        application.setApplicationDate(LocalDateTime.now());
        
        applications.put(applicationId, application);
        
        // 주민번호 등록 (중복 방지)
        usedResidentNumbers.add(application.getApplicantInfo().getResidentNumber());
        
        log.info("Insurance application submitted: {}", applicationId);
        return applicationId;
    }

    public String generatePolicyNumber(String applicationId) {
        // 간단한 보험증번호 생성 로직
        return "POL" + System.currentTimeMillis() + applicationId.substring(0, 3).toUpperCase();
    }

    public InsuranceApplicationDto getInsuranceApplication(String applicationId) {
        InsuranceApplicationDto application = applications.get(applicationId);
        if (application == null) {
            throw new RuntimeException("Application not found: " + applicationId);
        }
        return application;
    }

    public List<String> validatePersonalInfo(PersonalInfoDto personalInfo) {
        List<String> errors = new ArrayList<>();
        
        // 이름 검증
        if (personalInfo.getName() == null || personalInfo.getName().trim().isEmpty()) {
            errors.add("이름은 필수입니다.");
        }
        
        // 주민번호 검증
        if (personalInfo.getResidentNumber() == null || !personalInfo.getResidentNumber().matches("\\d{6}-\\d{7}")) {
            errors.add("주민등록번호 형식이 올바르지 않습니다.");
        }
        
        // 전화번호 검증
        if (personalInfo.getPhoneNumber() == null || !personalInfo.getPhoneNumber().matches("010-\\d{4}-\\d{4}")) {
            errors.add("휴대폰 번호 형식이 올바르지 않습니다.");
        }
        
        // 이메일 검증
        if (personalInfo.getEmail() == null || !personalInfo.getEmail().matches("[^@]+@[^@]+\\.[^@]+")) {
            errors.add("이메일 형식이 올바르지 않습니다.");
        }
        
        return errors;
    }

    public boolean checkResidentNumberDuplicate(String residentNumber) {
        return usedResidentNumbers.contains(residentNumber);
    }

    public boolean validateBankAccount(BankAccountDto bankAccount) {
        // 간단한 은행 계좌 검증 로직
        if (bankAccount.getBankCode() == null || bankAccount.getAccountNumber() == null) {
            return false;
        }
        
        // 실제로는 은행 API를 호출하여 검증
        return bankAccount.getAccountNumber().matches("\\d{10,}");
    }

    public boolean updateApplicationStatus(String applicationId, String status) {
        InsuranceApplicationDto application = applications.get(applicationId);
        if (application != null) {
            application.setStatus(status);
            return true;
        }
        return false;
    }

    public List<InsuranceApplicationDto> getInsuranceHistory(String userId) {
        // 사용자별 가입 히스토리 조회 (실제로는 데이터베이스에서 조회)
        return applications.values().stream()
            .filter(app -> app.getApplicantInfo().getResidentNumber().contains(userId))
            .collect(Collectors.toList());
    }

    private String generateApplicationId() {
        return "APP" + System.currentTimeMillis() + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}

