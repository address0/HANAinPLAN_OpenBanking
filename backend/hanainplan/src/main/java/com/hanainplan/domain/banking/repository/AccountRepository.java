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
    
    // 사용자 ID로 계좌 목록 조회
    List<BankingAccount> findByUserIdOrderByCreatedAtDesc(Long userId);
    
    // 사용자 ID와 계좌 상태로 계좌 목록 조회
    List<BankingAccount> findByUserIdAndAccountStatusOrderByCreatedAtDesc(Long userId, BankingAccount.AccountStatus accountStatus);
    
    // 계좌번호로 계좌 조회
    Optional<BankingAccount> findByAccountNumber(String accountNumber);
    
    // 사용자 ID와 계좌번호로 계좌 조회
    Optional<BankingAccount> findByUserIdAndAccountNumber(Long userId, String accountNumber);
    
    // 사용자 ID와 계좌 유형으로 계좌 목록 조회
    List<BankingAccount> findByUserIdAndAccountTypeOrderByCreatedAtDesc(Long userId, Integer accountType);
    
    // 활성 계좌만 조회
    @Query("SELECT a FROM BankingAccount a WHERE a.userId = :userId AND a.accountStatus = 'ACTIVE' ORDER BY a.createdAt DESC")
    List<BankingAccount> findActiveAccountsByUserId(@Param("userId") Long userId);
    
    // 계좌번호 중복 확인
    boolean existsByAccountNumber(String accountNumber);
    
    // 사용자의 계좌 개수 조회
    long countByUserId(Long userId);
    
    // 사용자의 특정 유형 계좌 개수 조회
    long countByUserIdAndAccountType(Long userId, Integer accountType);
    
    // 잔액이 특정 금액 이상인 계좌 조회
    @Query("SELECT a FROM BankingAccount a WHERE a.userId = :userId AND a.balance >= :minBalance ORDER BY a.balance DESC")
    List<BankingAccount> findAccountsWithMinBalance(@Param("userId") Long userId, @Param("minBalance") java.math.BigDecimal minBalance);
    
    // 계좌 유형별 잔액 합계 조회
    @Query("SELECT a.accountType, SUM(a.balance) FROM BankingAccount a WHERE a.userId = :userId AND a.accountStatus = 'ACTIVE' GROUP BY a.accountType")
    List<Object[]> getBalanceSumByAccountType(@Param("userId") Long userId);
}
