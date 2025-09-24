package com.hanainplan.kookmin.user.service;

import com.hanainplan.kookmin.account.dto.AccountResponseDto;
import com.hanainplan.kookmin.account.service.AccountService;
import com.hanainplan.kookmin.user.dto.CustomerAccountResponseDto;
import com.hanainplan.kookmin.user.entity.Customer;
import com.hanainplan.kookmin.user.repository.CustomerRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class CustomerAccountService {

    @Autowired
    private CustomerRepository customerRepository;

    @Autowired
    private AccountService accountService;

    /**
     * CI로 고객 및 계좌 정보 조회
     */
    public CustomerAccountResponseDto getCustomerAccountInfoByCi(String ci) {
        // 고객 정보 조회
        Customer customer = customerRepository.findByCi(ci).orElse(null);
        
        if (customer == null) {
            // 고객이 존재하지 않는 경우
            return new CustomerAccountResponseDto(false, null, ci, Collections.emptyList());
        }
        
        // 고객의 계좌 정보 조회
        List<AccountResponseDto> accounts = accountService.getAccountsByCi(ci);
        
        return new CustomerAccountResponseDto(true, customer.getName(), customer.getCi(), accounts);
    }
}
