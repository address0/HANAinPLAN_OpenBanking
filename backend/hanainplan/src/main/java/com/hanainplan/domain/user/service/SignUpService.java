package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.SignUpRequestDto;
import com.hanainplan.domain.user.dto.SignUpResponseDto;
import com.hanainplan.domain.user.entity.*;
import com.hanainplan.domain.user.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 회원가입 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SignUpService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ConsultantRepository consultantRepository;
    private final CustomerDiseaseDetailRepository diseaseDetailRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    /**
     * 회원가입 처리
     */
    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        try {
            // 1. 중복 검사
            validateDuplicateUser(request);

            // 2. 사용자 기본 정보 생성
            User user = createUser(request);
            User savedUser = userRepository.save(user);

            // 3. 사용자 타입에 따른 추가 정보 저장
            if (request.isGeneralCustomer()) {
                createCustomer(savedUser, request);
            } else if (request.isCounselor()) {
                createConsultant(savedUser, request);
            }

            log.info("회원가입 완료: userId={}, userType={}, name={}", 
                    savedUser.getUserId(), savedUser.getUserType(), savedUser.getUserName());

            return SignUpResponseDto.success(
                    savedUser.getUserId(),
                    savedUser.getUserType(),
                    savedUser.getUserName(),
                    savedUser.getPhoneNumber(),
                    savedUser.getCreatedDate()
            );

        } catch (IllegalArgumentException e) {
            log.warn("회원가입 실패 - 유효성 검사 오류: {}", e.getMessage());
            return SignUpResponseDto.failure(e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 처리 중 오류 발생", e);
            return SignUpResponseDto.failure("회원가입 처리 중 오류가 발생했습니다.");
        }
    }

    /**
     * 중복 사용자 검사
     */
    private void validateDuplicateUser(SignUpRequestDto request) {
        if (userRepository.existsBySocialNumber(request.getSocialNumber())) {
            throw new IllegalArgumentException("이미 가입된 주민번호입니다.");
        }

        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
        }

        if (request.isKakaoLogin() && userRepository.existsByKakaoId(request.getKakaoId())) {
            throw new IllegalArgumentException("이미 가입된 카카오 계정입니다.");
        }

        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("이미 가입된 이메일입니다.");
        }

        // 전화번호 인증 완료 여부 확인 (카카오 로그인이 아닌 경우만)
        if (!request.isKakaoLogin() && !verificationService.isPhoneNumberVerified(request.getPhoneNumber())) {
            throw new IllegalArgumentException("전화번호 인증이 완료되지 않았습니다.");
        }
    }

    /**
     * 사용자 엔터티 생성
     */
    private User createUser(SignUpRequestDto request) {
        // 주민번호에서 생년월일과 성별 추출
        LocalDate birthDate = User.extractBirthDateFromSocialNumber(request.getSocialNumber());
        User.Gender gender = User.extractGenderFromSocialNumber(request.getSocialNumber());

        User.UserBuilder userBuilder = User.builder()
                .userType(request.getUserType())
                .userName(request.getName())
                .socialNumber(request.getSocialNumber())
                .phoneNumber(request.getPhoneNumber())
                .birthDate(birthDate)
                .gender(gender)
                .ci(request.getCi()) // CI값 저장
                .isPhoneVerified(true); // 인증번호 확인 완료 가정

        // 로그인 타입에 따른 처리
        if (request.isKakaoLogin()) {
            userBuilder.kakaoId(request.getKakaoId())
                      .email(request.getEmail())
                      .loginType(User.LoginType.KAKAO);
        } else {
            userBuilder.password(passwordEncoder.encode(request.getPassword()))
                      .loginType(User.LoginType.PASSWORD);
        }

        return userBuilder.build();
    }

    /**
     * 일반고객 정보 생성
     */
    private void createCustomer(User user, SignUpRequestDto request) {
        SignUpRequestDto.HealthInfoDto healthInfo = request.getHealthInfo();
        SignUpRequestDto.JobInfoDto jobInfo = request.getJobInfo();

        if (healthInfo == null || jobInfo == null) {
            throw new IllegalArgumentException("일반고객 가입시 건강정보와 직업정보는 필수입니다.");
        }

        // 고객 기본 정보 생성
        Customer customer = Customer.builder()
                .customerId(user.getUserId())
                .recentMedicalAdvice(healthInfo.getRecentMedicalAdvice())
                .recentHospitalization(healthInfo.getRecentHospitalization())
                .majorDisease(healthInfo.getMajorDisease())
                .longTermMedication(healthInfo.getLongTermMedication())
                .disabilityRegistered(healthInfo.getDisabilityRegistered())
                .insuranceRejection(healthInfo.getInsuranceRejection())
                .industryCode(jobInfo.getIndustryCode())
                .industryName(jobInfo.getIndustryName())
                .careerYears(jobInfo.getCareerYears())
                .assetLevel(Customer.AssetLevel.fromValue(jobInfo.getAssetLevel()))
                .build();

        customerRepository.save(customer);

        // 질병 상세 정보 저장
        if (Boolean.TRUE.equals(healthInfo.getMajorDisease()) && 
            healthInfo.getDiseaseDetails() != null && 
            !healthInfo.getDiseaseDetails().isEmpty()) {
            
            List<CustomerDiseaseDetail> diseaseDetails = healthInfo.getDiseaseDetails().stream()
                    .map(dto -> CustomerDiseaseDetail.builder()
                            .customerId(user.getUserId())
                            .diseaseCode(dto.getDiseaseCode())
                            .diseaseName(dto.getDiseaseName())
                            .diseaseCategory(dto.getDiseaseCategory())
                            .riskLevel(dto.getRiskLevel())
                            .severity(dto.getSeverity())
                            .progressPeriod(dto.getProgressPeriod())
                            .isChronic(dto.getIsChronic())
                            .description(dto.getDescription())
                            .build())
                    .collect(Collectors.toList());

            diseaseDetailRepository.saveAll(diseaseDetails);
        }

        log.info("일반고객 정보 저장 완료: customerId={}", customer.getCustomerId());
    }

    /**
     * 상담원 정보 생성 (기본 정보만)
     */
    private void createConsultant(User user, SignUpRequestDto request) {
        // 현재는 기본 정보만 저장 (추후 상담원 가입 폼 구현시 확장)
        Consultant consultant = Consultant.builder()
                .consultantId(user.getUserId())
                .workStatus(Consultant.WorkStatus.ACTIVE)
                .build();

        consultantRepository.save(consultant);

        log.info("상담원 정보 저장 완료: consultantId={}", consultant.getConsultantId());
    }

    /**
     * 주민번호 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateSocialNumber(String socialNumber) {
        return userRepository.existsBySocialNumber(socialNumber);
    }

    /**
     * 전화번호 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isDuplicatePhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    /**
     * 카카오 ID 중복 확인
     */
    @Transactional(readOnly = true)
    public boolean isDuplicateKakaoId(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }
}
