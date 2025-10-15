package com.hanainplan.shinhan.account.repository;

import com.hanainplan.shinhan.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByCustomerCi(String ci);

    Optional<Account> findByAccountNumber(String accountNumber);

    List<Account> findByCustomerCiAndAccountType(String ci, Integer accountType);
}