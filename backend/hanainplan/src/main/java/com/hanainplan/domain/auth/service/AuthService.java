package com.hanainplan.domain.auth.service;

import com.hanainplan.domain.auth.dto.LoginRequestDto;
import com.hanainplan.domain.auth.dto.LoginResponseDto;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
public class AuthService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
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
}
