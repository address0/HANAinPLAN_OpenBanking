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

    public UserResponseDto createUser(CreateUserRequestDto request) {
        ciConversionService.validateResidentNumber(
            request.getName(),
            request.getBirthDate(),
            request.getGender(),
            request.getResidentNumber()
        );

        String ci = ciConversionService.convertToCi(
            request.getName(),
            request.getBirthDate(),
            request.getGender(),
            request.getResidentNumber()
        );

        if (userRepository.existsByCi(ci)) {
            throw new IllegalArgumentException("이미 존재하는 CI입니다. 동일한 개인정보로 가입된 사용자가 있습니다.");
        }

        User user = new User();
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        user.setBirthDate(request.getBirthDate());
        user.setGender(request.getGender());
        user.setCi(ci);

        User savedUser = userRepository.save(user);
        return UserResponseDto.from(savedUser);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserById(Long id) {
        return userRepository.findById(id)
            .map(UserResponseDto::from);
    }

    @Transactional(readOnly = true)
    public Optional<UserResponseDto> getUserByCi(String ci) {
        return userRepository.findByCi(ci)
            .map(UserResponseDto::from);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDto> getAllUsers() {
        return userRepository.findAll().stream()
            .map(UserResponseDto::from)
            .collect(Collectors.toList());
    }

    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다: " + id));

        userRepository.delete(user);
    }

}