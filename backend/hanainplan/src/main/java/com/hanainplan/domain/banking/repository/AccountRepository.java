package com.hanainplan.domain.banking.repository;

import com.hanainplan.domain.banking.entity.BankingAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<BankingAccount, Long> {

    List<BankingAccount> findByUserIdOrderByCreatedAtDesc(Long userId);

    List<BankingAccount> findByUserIdAndAccountStatusOrderByCreatedAtDesc(Long userId, BankingAccount.AccountStatus accountStatus);

    Optional<BankingAccount> findByAccountNumber(String accountNumber);

    Optional<BankingAccount> findByUserIdAndAccountNumber(Long userId, String accountNumber);

    List<BankingAccount> findByUserIdAndAccountTypeOrderByCreatedAtDesc(Long userId, Integer accountType);

    @Query("SELECT a FROM BankingAccount a WHERE a.userId = :userId AND a.accountStatus = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<BankingAccount> findActiveAccountsByUserId(@Param("userId") Long userId);

    boolean existsByAccountNumber(String accountNumber);

    long countByUserId(Long userId);

    long countByUserIdAndAccountType(Long userId, Integer accountType);

    @Query("SELECT a FROM BankingAccount a WHERE a.userId = :userId AND a.balance >= :minBalance ORDER BY a.balance DESC")
    List<BankingAccount> findAccountsWithMinBalance(@Param("userId") Long userId, @Param("minBalance") java.math.BigDecimal minBalance);

    @Query("SELECT a.accountType, SUM(a.balance) FROM BankingAccount a WHERE a.userId = :userId AND a.accountStatus = 'ACTIVE' GROUP BY a.accountType")
    List<Object[]> getBalanceSumByAccountType(@Param("userId") Long userId);
}