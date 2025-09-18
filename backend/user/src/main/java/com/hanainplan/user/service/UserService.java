package com.hanainplan.user.service;

import com.hanainplan.user.dto.CreateUserRequestDto;
import com.hanainplan.user.dto.UserResponseDto;
import com.hanainplan.user.entity.User;
import com.hanainplan.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CiConversionService ciConversionService;

    /**
     * 사용자 생성 (CI 포함)
     */
    public UserResponseDto createUser(CreateUserRequestDto request) {
        // 1. 주민번호 유효성 검증
        ciConversionService.validateResidentNumber(
            request.getName(),
            request.getBirthDate(),
            request.getGender(),
            request.getResidentNumber()
        );

        // 2. CI 생성
        String ci = ciConversionService.convertToCi(
            request.getName(),
            request.getBirthDate(),
            request.getGender(),
            request.getResidentNumber()
        );

        // 3. CI 중복 검증
        if (userRepository.existsByCi(ci)) {
            throw new IllegalArgumentException("이미 존재하는 CI입니다. 동일한 개인정보로 가입된 사용자가 있습니다.");
        }

        // 4. 사용자 생성
        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setCi(ci);

        User savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);
    }

    /**
     * ID로 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(UserResponseDto::from);
    }

    /**
     * CI로 조회
     */
    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserByCi(String ci) {
        return userRepository.findByCi(ci)
            .map(UserResponseDto::from);
    }

    /**
     * 모든 사용자 조회
     */
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponseDto::from)
            .collect(Collectors.toList());
    }

    /**
     * 사용자 삭제
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));
        
        userRepository.delete(user);
    }

}
