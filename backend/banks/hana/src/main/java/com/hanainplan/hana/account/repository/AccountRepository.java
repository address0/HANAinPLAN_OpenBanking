package com.hanainplan.hana.account.repository;

import com.hanainplan.hana.account.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, String> {

    List<Account> findByCustomerUserId(Long userId);

    @Query("SELECT a FROM Account a WHERE a.customer.ci = :ci")
    List<Account> findByCustomerCi(@Param("ci") String ci);

    Optional<Account> findByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a WHERE a.customer.ci = :ci AND a.accountType = :accountType")
    List<Account> findByCustomerCiAndAccountType(@Param("ci") String ci, @Param("accountType") Integer accountType);
}
