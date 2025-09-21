package com.hanainplan.samsung.customer.service;

import com.hanainplan.samsung.customer.dto.CustomerRequestDto;
import com.hanainplan.samsung.customer.dto.CustomerResponseDto;
import com.hanainplan.samsung.customer.entity.Customer;
import com.hanainplan.samsung.customer.repository.CustomerRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional
    public CustomerResponseDto createCustomer(CustomerRequestDto requestDto) {
        if (customerRepository.existsByCi(requestDto.getCi())) {
            throw new IllegalArgumentException("이미 존재하는 CI입니다: " + requestDto.getCi());
        }

        if (requestDto.getPhone() != null && customerRepository.existsByPhone(requestDto.getPhone())) {
            throw new IllegalArgumentException("이미 존재하는 전화번호입니다: " + requestDto.getPhone());
        }

        if (requestDto.getEmail() != null && customerRepository.existsByEmail(requestDto.getEmail())) {
            throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + requestDto.getEmail());
        }

        Customer customer = Customer.builder()
                .ci(requestDto.getCi())
                .name(requestDto.getName())
                .gender(requestDto.getGender())
                .birthDate(requestDto.getBirthDate())
                .phone(requestDto.getPhone())
                .email(requestDto.getEmail())
                .address(requestDto.getAddress())
                .occupation(requestDto.getOccupation())
                .maritalStatus(requestDto.getMaritalStatus())
                .customerType(requestDto.getCustomerType())
                .riskLevel(requestDto.getRiskLevel())
                .isActive(requestDto.getIsActive() != null ? requestDto.getIsActive() : true)
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return CustomerResponseDto.fromEntity(savedCustomer);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerByCi(String ci) {
        Customer customer = customerRepository.findByCi(ci)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다. CI: " + ci));
        return CustomerResponseDto.fromEntity(customer);
    }

    @Transactional(readOnly = true)
    public CustomerResponseDto getCustomerById(Long customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다. ID: " + customerId));
        return CustomerResponseDto.fromEntity(customer);
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllCustomers() {
        return customerRepository.findAll().stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getActiveCustomers() {
        return customerRepository.findByIsActiveTrue().stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getCustomersByType(String customerType) {
        return customerRepository.findByCustomerTypeAndActive(customerType).stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getCustomersByRiskLevel(String riskLevel) {
        return customerRepository.findByRiskLevelAndActive(riskLevel).stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> searchCustomersByName(String keyword) {
        return customerRepository.findByNameContaining(keyword).stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> searchCustomersByOccupation(String keyword) {
        return customerRepository.findByOccupationContaining(keyword).stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> searchCustomersByAddress(String keyword) {
        return customerRepository.findByAddressContaining(keyword).stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getCustomersByGender(String gender) {
        return customerRepository.findByGenderAndActive(gender).stream()
                .map(CustomerResponseDto::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public CustomerResponseDto updateCustomer(String ci, CustomerRequestDto requestDto) {
        Customer customer = customerRepository.findByCi(ci)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다. CI: " + ci));

        customer.setName(requestDto.getName());
        customer.setGender(requestDto.getGender());
        customer.setBirthDate(requestDto.getBirthDate());
        
        if (requestDto.getPhone() != null && !requestDto.getPhone().equals(customer.getPhone())) {
            if (customerRepository.existsByPhone(requestDto.getPhone())) {
                throw new IllegalArgumentException("이미 존재하는 전화번호입니다: " + requestDto.getPhone());
            }
        }
        customer.setPhone(requestDto.getPhone());
        
        if (requestDto.getEmail() != null && !requestDto.getEmail().equals(customer.getEmail())) {
            if (customerRepository.existsByEmail(requestDto.getEmail())) {
                throw new IllegalArgumentException("이미 존재하는 이메일입니다: " + requestDto.getEmail());
            }
        }
        customer.setEmail(requestDto.getEmail());
        
        customer.setAddress(requestDto.getAddress());
        customer.setOccupation(requestDto.getOccupation());
        customer.setMaritalStatus(requestDto.getMaritalStatus());
        customer.setCustomerType(requestDto.getCustomerType());
        customer.setRiskLevel(requestDto.getRiskLevel());
        if (requestDto.getIsActive() != null) {
            customer.setIsActive(requestDto.getIsActive());
        }

        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerResponseDto.fromEntity(updatedCustomer);
    }

    @Transactional
    public void deleteCustomer(String ci) {
        Customer customer = customerRepository.findByCi(ci)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다. CI: " + ci));
        customerRepository.delete(customer);
    }

    @Transactional
    public CustomerResponseDto toggleCustomerStatus(String ci) {
        Customer customer = customerRepository.findByCi(ci)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다. CI: " + ci));
        
        customer.setIsActive(!customer.getIsActive());
        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerResponseDto.fromEntity(updatedCustomer);
    }
}
