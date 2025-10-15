package com.hanainplan.hana.user.service;

import com.hanainplan.hana.account.dto.AccountResponseDto;
import com.hanainplan.hana.account.service.AccountService;
import com.hanainplan.hana.user.dto.CustomerAccountResponseDto;
import com.hanainplan.hana.user.entity.Customer;
import com.hanainplan.hana.user.repository.CustomerRepository;
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

    public CustomerAccountResponseDto getCustomerAccountInfoByCi(String ci) {
        Customer customer = customerRepository.findByCi(ci).orElse(null);

        if (customer == null) {
            return new CustomerAccountResponseDto(false, null, ci, Collections.emptyList());
        }

        List<AccountResponseDto> accounts = accountService.getAccountsByCi(ci);

        return new CustomerAccountResponseDto(true, customer.getName(), customer.getCi(), accounts);
    }
}