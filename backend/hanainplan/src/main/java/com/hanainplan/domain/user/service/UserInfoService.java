package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.UserInfoResponseDto;
import com.hanainplan.domain.user.dto.UserInfoUpdateRequestDto;
import com.hanainplan.domain.user.dto.PasswordChangeRequestDto;
import com.hanainplan.domain.user.entity.User;
import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.entity.CustomerDiseaseDetail;
import com.hanainplan.domain.user.repository.UserRepository;
import com.hanainplan.domain.user.repository.CustomerRepository;
import com.hanainplan.domain.user.repository.CustomerDiseaseDetailRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserInfoService {

    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final CustomerDiseaseDetailRepository customerDiseaseDetailRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserInfoService(
            UserRepository userRepository,
            CustomerRepository customerRepository,
            CustomerDiseaseDetailRepository customerDiseaseDetailRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.customerRepository = customerRepository;
        this.customerDiseaseDetailRepository = customerDiseaseDetailRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public UserInfoResponseDto getUserInfo(Long userId) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        User user = userOptional.get();
        Customer customer = null;
        List<CustomerDiseaseDetail> diseaseDetails = null;

        if (user.getUserType() == User.UserType.GENERAL) {
            Optional<Customer> customerOptional = customerRepository.findById(userId);
            if (customerOptional.isPresent()) {
                customer = customerOptional.get();
                final Customer finalCustomer = customer;
                diseaseDetails = customerDiseaseDetailRepository.findAll().stream()
                    .filter(detail -> detail.getCustomerId().equals(finalCustomer.getCustomerId()))
                    .toList();
            }
        }

        return new UserInfoResponseDto(user, customer, diseaseDetails);
    }

    public UserInfoResponseDto updateUserInfo(Long userId, UserInfoUpdateRequestDto updateRequest) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        User user = userOptional.get();

        if (updateRequest.getUserBasicInfo() != null) {
            updateUserBasicInfo(user, updateRequest.getUserBasicInfo());
        }

        if (user.getUserType() == User.UserType.GENERAL && updateRequest.getCustomerDetailInfo() != null) {
            updateCustomerDetailInfo(userId, updateRequest.getCustomerDetailInfo());
        }

        return getUserInfo(userId);
    }

    public boolean changePassword(Long userId, PasswordChangeRequestDto passwordChangeRequest) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(passwordChangeRequest.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException("현재 비밀번호가 일치하지 않습니다.");
        }

        if (!passwordChangeRequest.getNewPassword().equals(passwordChangeRequest.getConfirmPassword())) {
            throw new RuntimeException("새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        user.setPassword(passwordEncoder.encode(passwordChangeRequest.getNewPassword()));
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    public boolean deleteAccount(Long userId, String password) {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isEmpty()) {
            throw new RuntimeException("사용자를 찾을 수 없습니다.");
        }

        User user = userOptional.get();

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("비밀번호가 일치하지 않습니다.");
        }

        user.setIsActive(false);
        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);

        return true;
    }

    private void updateUserBasicInfo(User user, UserInfoUpdateRequestDto.UserBasicInfoUpdate basicInfo) {
        if (basicInfo.getUserName() != null && !basicInfo.getUserName().trim().isEmpty()) {
            user.setUserName(basicInfo.getUserName().trim());
        }

        if (basicInfo.getPhoneNumber() != null && !basicInfo.getPhoneNumber().trim().isEmpty()) {
            String formattedPhoneNumber = formatPhoneNumber(basicInfo.getPhoneNumber());
            user.setPhoneNumber(formattedPhoneNumber);
        }

        if (basicInfo.getEmail() != null && !basicInfo.getEmail().trim().isEmpty()) {
            user.setEmail(basicInfo.getEmail().trim());
        }

        user.setUpdatedDate(LocalDateTime.now());
        userRepository.save(user);
    }

    private void updateCustomerDetailInfo(Long userId, UserInfoUpdateRequestDto.CustomerDetailInfoUpdate detailInfo) {
        Optional<Customer> customerOptional = customerRepository.findById(userId);
        if (customerOptional.isEmpty()) {
            return;
        }

        Customer customer = customerOptional.get();

        if (detailInfo.getHealthInfo() != null) {
            updateHealthInfo(customer, detailInfo.getHealthInfo());
        }

        if (detailInfo.getJobInfo() != null) {
            updateJobInfo(customer, detailInfo.getJobInfo());
        }

        customerRepository.save(customer);
    }

    private void updateHealthInfo(Customer customer, UserInfoUpdateRequestDto.HealthInfoUpdate healthInfo) {
        if (healthInfo.getRecentMedicalAdvice() != null) {
            customer.setRecentMedicalAdvice(healthInfo.getRecentMedicalAdvice());
        }

        if (healthInfo.getRecentHospitalization() != null) {
            customer.setRecentHospitalization(healthInfo.getRecentHospitalization());
        }

        if (healthInfo.getMajorDisease() != null) {
            customer.setMajorDisease(healthInfo.getMajorDisease());
        }

        if (healthInfo.getLongTermMedication() != null) {
            customer.setLongTermMedication(healthInfo.getLongTermMedication());
        }

        if (healthInfo.getDisabilityRegistered() != null) {
            customer.setDisabilityRegistered(healthInfo.getDisabilityRegistered());
        }

        if (healthInfo.getInsuranceRejection() != null) {
            customer.setInsuranceRejection(healthInfo.getInsuranceRejection());
        }

        if (healthInfo.getDiseaseDetails() != null) {
            updateDiseaseDetails(customer.getCustomerId(), healthInfo.getDiseaseDetails());
        }
    }

    private void updateJobInfo(Customer customer, UserInfoUpdateRequestDto.JobInfoUpdate jobInfo) {
        if (jobInfo.getIndustryCode() != null && !jobInfo.getIndustryCode().trim().isEmpty()) {
            customer.setIndustryCode(jobInfo.getIndustryCode().trim());
        }

        if (jobInfo.getIndustryName() != null && !jobInfo.getIndustryName().trim().isEmpty()) {
            customer.setIndustryName(jobInfo.getIndustryName().trim());
        }

        if (jobInfo.getCareerYears() != null) {
            customer.setCareerYears(jobInfo.getCareerYears());
        }

        if (jobInfo.getAssetLevel() != null && !jobInfo.getAssetLevel().trim().isEmpty()) {
            try {
                Customer.AssetLevel assetLevel = Customer.AssetLevel.valueOf(jobInfo.getAssetLevel().trim());
                customer.setAssetLevel(assetLevel);
            } catch (IllegalArgumentException e) {
            }
        }
    }

    private void updateDiseaseDetails(Long customerId, List<UserInfoUpdateRequestDto.DiseaseDetailUpdate> diseaseDetails) {
        List<CustomerDiseaseDetail> existingDetails = customerDiseaseDetailRepository.findAll().stream()
            .filter(detail -> detail.getCustomerId().equals(customerId))
            .toList();

        customerDiseaseDetailRepository.deleteAll(existingDetails);

        for (UserInfoUpdateRequestDto.DiseaseDetailUpdate detailUpdate : diseaseDetails) {
            CustomerDiseaseDetail detail = new CustomerDiseaseDetail();
            detail.setCustomerId(customerId);
            detail.setDiseaseCode(detailUpdate.getDiseaseCode());
            detail.setDiseaseName(detailUpdate.getDiseaseName());
            detail.setDiseaseCategory(detailUpdate.getDiseaseCategory());
            detail.setRiskLevel(detailUpdate.getRiskLevel());
            detail.setSeverity(detailUpdate.getSeverity());
            detail.setProgressPeriod(detailUpdate.getProgressPeriod());
            detail.setIsChronic(detailUpdate.getIsChronic());
            detail.setDescription(detailUpdate.getDescription());

            customerDiseaseDetailRepository.save(detail);
        }
    }

    private String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber == null) {
            return null;
        }

        String numbers = phoneNumber.replaceAll("-", "");

        if (numbers.length() != 11) {
            return phoneNumber;
        }

        return numbers.substring(0, 3) + "-" + numbers.substring(3, 7) + "-" + numbers.substring(7);
    }
}