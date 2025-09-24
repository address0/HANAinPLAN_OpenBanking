package com.hanainplan.shinhan.user.service;

import com.hanainplan.shinhan.user.dto.CustomerRequestDto;
import com.hanainplan.shinhan.user.dto.CustomerResponseDto;
import com.hanainplan.shinhan.user.entity.Customer;
import com.hanainplan.shinhan.user.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class CustomerService {

    @Autowired
    private CustomerRepository customerRepository;

    /**
     * 고객 생성
     */
    public CustomerResponseDto createCustomer(CustomerRequestDto request) {
        // CI 중복 확인
        if (customerRepository.existsByCi(request.getCi())) {
            throw new IllegalArgumentException("이미 존재하는 CI입니다: " + request.getCi());
        }

        Customer customer = Customer.builder()
                .ci(request.getCi())
                .name(request.getName())
                .gender(request.getGender())
                .birthDate(request.getBirthDate())
                .phone(request.getPhone())
                .build();

        Customer savedCustomer = customerRepository.save(customer);
        return CustomerResponseDto.from(savedCustomer);
    }

    /**
     * 고객 조회 (CI)
     */
    @Transactional(readOnly = true)
    public Optional<CustomerResponseDto> getCustomerByCi(String ci) {
        return customerRepository.findByCi(ci)
                .map(CustomerResponseDto::from);
    }

    /**
     * 고객 조회 (ID)
     */
    @Transactional(readOnly = true)
    public Optional<CustomerResponseDto> getCustomerById(Long userId) {
        return customerRepository.findById(userId)
                .map(CustomerResponseDto::from);
    }

    /**
     * 모든 고객 조회
     */
    @Transactional(readOnly = true)
    public List<CustomerResponseDto> getAllCustomers() {
        List<Customer> customers = customerRepository.findAll();
        return customers.stream()
                .map(CustomerResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 고객 수정
     */
    public CustomerResponseDto updateCustomer(Long userId, CustomerRequestDto request) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + userId));

        // CI 변경 시 중복 확인
        if (!customer.getCi().equals(request.getCi()) && customerRepository.existsByCi(request.getCi())) {
            throw new IllegalArgumentException("이미 존재하는 CI입니다: " + request.getCi());
        }

        customer.setCi(request.getCi());
        customer.setName(request.getName());
        customer.setGender(request.getGender());
        customer.setBirthDate(request.getBirthDate());
        customer.setPhone(request.getPhone());

        Customer updatedCustomer = customerRepository.save(customer);
        return CustomerResponseDto.from(updatedCustomer);
    }

    /**
     * 고객 삭제
     */
    public void deleteCustomer(Long userId) {
        if (!customerRepository.existsById(userId)) {
            throw new IllegalArgumentException("고객을 찾을 수 없습니다: " + userId);
        }
        customerRepository.deleteById(userId);
    }
}
