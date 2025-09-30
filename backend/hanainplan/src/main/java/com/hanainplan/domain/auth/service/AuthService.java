package com.hanainplan.domain.auth.service;

import com.hanainplan.domain.auth.dto.CiVerificationRequestDto;
import com.hanainplan.domain.auth.dto.CiVerificationResponseDto;
import com.hanainplan.domain.auth.dto.LoginRequestDto;
import com.hanainplan.domain.auth.dto.LoginResponseDto;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
@Transactional
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final RestTemplate restTemplate;

    @Value("${external.api.identity-verification.base-url:http://localhost:8084}")
    private String identityVerificationBaseUrl;

    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, RestTemplate restTemplate) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.restTemplate = restTemplate;
    }
    
    /**
     * 전화번호와 비밀번호를 이용한 로그인
     */
    public LoginResponseDto loginWithPhoneAndPassword(LoginRequestDto loginRequest) {
        try {
            // 전화번호 형식 통일 (하이픈 추가)
            String phoneNumber = formatPhoneNumber(loginRequest.getPhoneNumber());
            
            // 전화번호로 사용자 조회
            Optional<User> userOptional = userRepository.findByPhoneNumber(phoneNumber);
            
            if (userOptional.isEmpty()) {
                return new LoginResponseDto(false, "등록되지 않은 전화번호입니다.");
            }
            
            User user = userOptional.get();
            
            // 계정 활성화 상태 확인
            if (!user.getIsActive()) {
                return new LoginResponseDto(false, "비활성화된 계정입니다. 고객센터에 문의해주세요.");
            }
            
            // 전화번호 인증 상태 확인
            if (!user.getIsPhoneVerified()) {
                return new LoginResponseDto(false, "전화번호 인증이 완료되지 않은 계정입니다.");
            }
            
            // 카카오 로그인으로만 가입한 사용자인 경우
            if (user.getLoginType() == User.LoginType.KAKAO && (user.getPassword() == null || user.getPassword().isEmpty())) {
                return new LoginResponseDto(false, "카카오 로그인을 이용해주세요.");
            }
            
            // 비밀번호 확인
            if (!passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
                return new LoginResponseDto(false, "비밀번호가 일치하지 않습니다.");
            }
            
            // 로그인 성공 - 마지막 로그인 시간 업데이트
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            
            return new LoginResponseDto(true, "로그인이 성공적으로 완료되었습니다.", user);
            
        } catch (Exception e) {
            return new LoginResponseDto(false, "로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 카카오 로그인 (추후 구현)
     */
    public LoginResponseDto loginWithKakao(String kakaoId) {
        try {
            // 카카오 ID로 사용자 조회
            Optional<User> userOptional = userRepository.findByKakaoId(kakaoId);
            
            if (userOptional.isEmpty()) {
                return new LoginResponseDto(false, "등록되지 않은 카카오 계정입니다. 회원가입을 진행해주세요.");
            }
            
            User user = userOptional.get();
            
            // 계정 활성화 상태 확인
            if (!user.getIsActive()) {
                return new LoginResponseDto(false, "비활성화된 계정입니다. 고객센터에 문의해주세요.");
            }
            
            // 로그인 성공 - 마지막 로그인 시간 업데이트
            user.setLastLoginDate(LocalDateTime.now());
            userRepository.save(user);
            
            return new LoginResponseDto(true, "카카오 로그인이 성공적으로 완료되었습니다.", user);
            
        } catch (Exception e) {
            return new LoginResponseDto(false, "카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * 사용자 존재 여부 확인 (전화번호 기준)
     */
    @Transactional(readOnly = true)
    public boolean isUserExistsByPhoneNumber(String phoneNumber) {
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        return userRepository.existsByPhoneNumber(formattedPhoneNumber);
    }
    
    /**
     * 사용자 조회 (전화번호 기준)
     */
    @Transactional(readOnly = true)
    public Optional<User> findUserByPhoneNumber(String phoneNumber) {
        String formattedPhoneNumber = formatPhoneNumber(phoneNumber);
        return userRepository.findByPhoneNumber(formattedPhoneNumber);
    }
    
    /**
     * 전화번호 형식 통일 (하이픈 추가)
     * 01012345678 -> 010-1234-5678
     */
    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        // 하이픈 제거
        String numbers = phoneNumber.replaceAll("-", "");

        // 11자리 숫자가 아니면 원본 반환
        if (numbers.length() != 11) {
            return phoneNumber;
        }

        // 하이픈 추가: 010-1234-5678 형식
        return numbers.substring(0, 3) + "-" + numbers.substring(3, 7) + "-" + numbers.substring(7);
    }

    /**
     * CI 검증 (실명인증 서버 연동)
     */
    @Transactional(readOnly = true)
    public CiVerificationResponseDto verifyCi(CiVerificationRequestDto request) {
        try {
            // 주민번호에서 생년월일과 성별 추출
            Map<String, String> extractedInfo = extractBirthDateAndGender(request.getResidentNumber());

            // 실명인증 서버 요청 데이터 구성
            Map<String, Object> verificationRequest = new HashMap<>();
            verificationRequest.put("name", request.getName());
            verificationRequest.put("birthDate", extractedInfo.get("birthDate"));
            verificationRequest.put("gender", extractedInfo.get("gender"));
            verificationRequest.put("residentNumber", request.getResidentNumber());

            // 실명인증 서버에 요청
            String url = identityVerificationBaseUrl + "/api/user/ci/verify";
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            HttpEntity<Map<String, Object>> httpEntity = new HttpEntity<>(verificationRequest, headers);
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, Map.class);

            if (response.getBody() != null) {
                Map<String, Object> responseBody = response.getBody();

                // 실명인증 서버 응답에서 CI 추출
                String ci = (String) responseBody.get("ci");

                if (ci != null && !ci.trim().isEmpty()) {
                    return CiVerificationResponseDto.success(ci);
                } else {
                    return CiVerificationResponseDto.failure("실명인증 서버에서 CI 값을 반환받지 못했습니다.");
                }
            } else {
                return CiVerificationResponseDto.failure("실명인증 서버 응답이 없습니다.");
            }

        } catch (Exception e) {
            return CiVerificationResponseDto.failure("CI 검증 중 오류가 발생했습니다: " + e.getMessage(), "SYSTEM_ERROR");
        }
    }

    /**
     * 주민번호에서 생년월일과 성별 추출
     * @param residentNumber 13자리 주민번호
     * @return 생년월일(8자리)과 성별 정보
     */
    private Map<String, String> extractBirthDateAndGender(String residentNumber) {
        Map<String, String> result = new HashMap<>();

        if (residentNumber == null || residentNumber.length() != 13) {
            throw new IllegalArgumentException("주민번호는 13자리여야 합니다.");
        }

        // 뒷자리 첫 번째 숫자로 성별과 출생년도 결정
        char genderDigit = residentNumber.charAt(6);

        // 성별 결정 (1, 3: 남성, 2, 4: 여성)
        String gender = (genderDigit == '1' || genderDigit == '3') ? "M" : "F";
        result.put("gender", gender);

        // 출생년도 결정 (1, 2: 1900년대, 3, 4: 2000년대)
        String yearPrefix = (genderDigit == '1' || genderDigit == '2') ? "19" : "20";

        // 생년월일 추출 (YYMMDD 형식의 앞 6자리)
        String birthDatePart = residentNumber.substring(0, 6);

        // 8자리 생년월일 생성
        String birthDate = yearPrefix + birthDatePart;
        result.put("birthDate", birthDate);

        return result;
    }
}
