package com.hanainplan.hana.user.service;

import com.hanainplan.hana.user.entity.IrpAccount;
import com.hanainplan.hana.user.entity.Customer;
import com.hanainplan.hana.user.dto.IrpAccountRequest;
import com.hanainplan.hana.user.dto.IrpAccountResponse;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface IrpAccountService {

    IrpAccountResponse openIrpAccount(IrpAccountRequest request) throws Exception;

    Optional<IrpAccount> getIrpAccountByCustomerCi(String customerCi);

    Optional<IrpAccount> getIrpAccountByAccountNumber(String accountNumber);

    boolean hasExistingIrpAccount(String customerCi);

    void updateAccountStatus(String accountNumber, String status);

    void processMonthlyDeposit(String accountNumber, BigDecimal amount);

    boolean closeIrpAccount(String accountNumber);

    boolean syncWithHanaInPlan(String customerCi);

    List<IrpAccount> getAllIrpAccounts();

    List<IrpAccount> getChangedIrpAccounts(LocalDateTime sinceDateTime);

    Map<String, Object> getIrpStatistics();

    com.hanainplan.hana.user.dto.IrpDepositResponse processIrpDeposit(String accountNumber, BigDecimal amount, String description);
}