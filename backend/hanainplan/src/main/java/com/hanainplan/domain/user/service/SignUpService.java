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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

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

    @Transactional
    public SignUpResponseDto signUp(SignUpRequestDto request) {
        try {
            validateDuplicateUser(request);

            User user = createUser(request);
            User savedUser = userRepository.save(user);

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

    }

    private User createUser(SignUpRequestDto request) {
        LocalDate birthDate = User.extractBirthDateFromSocialNumber(request.getSocialNumber());
        User.Gender gender = User.extractGenderFromSocialNumber(request.getSocialNumber());

        User.UserBuilder userBuilder = User.builder()
                .userType(request.getUserType())
                .userName(request.getName())
                .socialNumber(request.getSocialNumber())
                .phoneNumber(request.getPhoneNumber())
                .birthDate(birthDate)
                .gender(gender)
                .ci(request.getCi())
                .isPhoneVerified(true);

        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            userBuilder.email(request.getEmail());
        }

        if (request.isKakaoLogin()) {
            userBuilder.kakaoId(request.getKakaoId())
                      .loginType(User.LoginType.KAKAO);
        } else {
            userBuilder.password(passwordEncoder.encode(request.getPassword()))
                      .loginType(User.LoginType.PASSWORD);
        }

        return userBuilder.build();
    }

    private void createCustomer(User user, SignUpRequestDto request) {
        SignUpRequestDto.HealthInfoDto healthInfo = request.getHealthInfo();
        SignUpRequestDto.JobInfoDto jobInfo = request.getJobInfo();

        if (healthInfo == null || jobInfo == null) {
            throw new IllegalArgumentException("일반고객 가입시 건강정보와 직업정보는 필수입니다.");
        }

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
                .hasIrpAccount(false)  // 초기 회원가입 시 IRP 계좌 없음
                .irpAccountNumber(null)  // IRP 계좌번호 없음
                .build();

        customerRepository.save(customer);

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

    private void createConsultant(User user, SignUpRequestDto request) {
        SignUpRequestDto.CounselorInfoDto counselorInfo = request.getCounselorInfo();

        if (counselorInfo == null) {
            throw new IllegalArgumentException("상담원 가입시 상담원 정보는 필수입니다.");
        }

        Consultant.ConsultantBuilder builder = Consultant.builder()
                .consultantId(user.getUserId())
                .employeeId(counselorInfo.getEmployeeId())
                .position(counselorInfo.getPosition())
                .branchCode(counselorInfo.getBranchCode())
                .branchName(counselorInfo.getBranchName())
                .branchAddress(counselorInfo.getBranchAddress())
                .officePhone(counselorInfo.getWorkPhoneNumber())
                .workEmail(counselorInfo.getWorkEmail())
                .workStatus(Consultant.WorkStatus.ACTIVE);

        if (counselorInfo.getSpecialty() != null && !counselorInfo.getSpecialty().isEmpty()) {
            builder.specialization("[\"" + counselorInfo.getSpecialty() + "\"]");
        }

        if (counselorInfo.getBranchLatitude() != null) {
            builder.branchLatitude(BigDecimal.valueOf(counselorInfo.getBranchLatitude()));
        }
        if (counselorInfo.getBranchLongitude() != null) {
            builder.branchLongitude(BigDecimal.valueOf(counselorInfo.getBranchLongitude()));
        }

        Consultant consultant = builder.build();
        consultantRepository.save(consultant);

        log.info("상담원 정보 저장 완료: consultantId={}, employeeId={}, branchName={}", 
                consultant.getConsultantId(), consultant.getEmployeeId(), consultant.getBranchName());
    }

    @Transactional(readOnly = true)
    public boolean isDuplicateSocialNumber(String socialNumber) {
        return userRepository.existsBySocialNumber(socialNumber);
    }

    @Transactional(readOnly = true)
    public boolean isDuplicatePhoneNumber(String phoneNumber) {
        return userRepository.existsByPhoneNumber(phoneNumber);
    }

    @Transactional(readOnly = true)
    public boolean isDuplicateKakaoId(String kakaoId) {
        return userRepository.existsByKakaoId(kakaoId);
    }
}