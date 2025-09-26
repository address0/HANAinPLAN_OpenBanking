package com.hanainplan.domain.user.service;

import com.hanainplan.domain.banking.dto.AccountDto;
import com.hanainplan.domain.banking.service.MyDataAccountService;
import com.hanainplan.domain.user.dto.CustomerAccountInfoDto;
import com.hanainplan.domain.user.dto.MyDataConsentRequestDto;
import com.hanainplan.domain.user.dto.MyDataConsentResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserAccountTransactionService {
    
    private final MyDataAccountService myDataAccountService;
    
    /**
     * 사용자 정보와 계좌 정보를 함께 저장하는 트랜잭션 처리
     */
    @Transactional
    public MyDataConsentResponseDto processUserAndAccountData(
            Long userId, 
            String customerCi, 
            MyDataConsentRequestDto request,
            List<CustomerAccountInfoDto> bankAccountInfo) {
        
        log.info("사용자 및 계좌 정보 통합 저장 시작 - 사용자 ID: {}, CI: {}", userId, customerCi);
        
        try {
            // 1. 마이데이터 계좌 정보를 hanainplan 계좌 테이블에 저장
            List<AccountDto> savedAccounts = new ArrayList<>();
            
            if (bankAccountInfo != null && !bankAccountInfo.isEmpty()) {
                // 모든 계좌 정보를 하나의 리스트로 변환
                List<MyDataAccountService.MyDataAccountInfo> allAccounts = new ArrayList<>();
                
                for (CustomerAccountInfoDto bankInfo : bankAccountInfo) {
                    if (bankInfo.getAccounts() != null) {
                        for (CustomerAccountInfoDto.AccountInfoDto account : bankInfo.getAccounts()) {
                            // balance를 안전하게 BigDecimal로 변환
                            BigDecimal balance;
                            try {
                                Object balanceObj = account.getBalance();
                                if (balanceObj == null) {
                                    balance = BigDecimal.ZERO;
                                } else if (balanceObj instanceof BigDecimal) {
                                    balance = (BigDecimal) balanceObj;
                                } else if (balanceObj instanceof Number) {
                                    balance = new BigDecimal(((Number) balanceObj).toString());
                                } else {
                                    balance = new BigDecimal(balanceObj.toString());
                                }
                            } catch (Exception e) {
                                // 타입 변환 오류 시 기본값 사용
                                log.warn("Balance 타입 변환 오류, 기본값 사용: {}", e.getMessage());
                                balance = BigDecimal.ZERO;
                            }
                            
                            // openingDate를 LocalDateTime으로 변환
                            LocalDateTime openingDateTime;
                            Object openingDateObj = account.getOpeningDate();
                            if (openingDateObj instanceof LocalDateTime) {
                                openingDateTime = (LocalDateTime) openingDateObj;
                            } else if (openingDateObj instanceof LocalDate) {
                                openingDateTime = ((LocalDate) openingDateObj).atStartOfDay();
                            } else {
                                openingDateTime = LocalDateTime.now();
                            }
                            
                            MyDataAccountService.MyDataAccountInfo myDataAccount = 
                                new MyDataAccountService.MyDataAccountInfo(
                                    account.getAccountNumber(),
                                    account.getAccountType(),
                                    balance,
                                    openingDateTime
                                );
                            allAccounts.add(myDataAccount);
                        }
                    }
                }
                
                if (!allAccounts.isEmpty()) {
                    savedAccounts = myDataAccountService.saveMyDataAccounts(userId, customerCi, allAccounts);
                    log.info("계좌 정보 저장 완료 - 저장된 계좌 수: {}", savedAccounts.size());
                }
            }
            
            // 2. 응답 생성
            MyDataConsentResponseDto response = new MyDataConsentResponseDto();
            
            int totalAccounts = bankAccountInfo != null ? 
                bankAccountInfo.stream()
                    .mapToInt(bank -> bank.getAccounts() != null ? bank.getAccounts().size() : 0)
                    .sum() : 0;
            
            if (bankAccountInfo == null || bankAccountInfo.isEmpty()) {
                response.setMessage("등록된 계좌 정보가 없습니다. 해당 CI로 등록된 고객이 없거나 계좌가 없습니다.");
            } else {
                response.setMessage("마이데이터 수집 동의가 완료되었습니다. " + 
                    bankAccountInfo.size() + "개 은행에서 " + totalAccounts + "개의 계좌를 발견하고 저장했습니다.");
            }
            
            response.setBankAccountInfo(bankAccountInfo != null ? bankAccountInfo : new ArrayList<>());
            response.setTotalBanks(bankAccountInfo != null ? bankAccountInfo.size() : 0);
            response.setTotalAccounts(totalAccounts);
            
            log.info("사용자 및 계좌 정보 통합 저장 완료 - 사용자 ID: {}, 저장된 계좌 수: {}", userId, savedAccounts.size());
            
            return response;
            
        } catch (Exception e) {
            log.error("사용자 및 계좌 정보 통합 저장 실패 - 사용자 ID: {}", userId, e);
            throw new RuntimeException("사용자 및 계좌 정보 저장 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 프론트엔드에서 받은 계좌 정보를 직접 저장
     */
    @Transactional
    public void saveAccountsDirectly(Long userId, List<Object> bankAccountInfo) {
        log.info("계좌 정보 직접 저장 시작 - 사용자 ID: {}, 계좌 수: {}", userId, bankAccountInfo.size());
        
        try {
            List<MyDataAccountService.MyDataAccountInfo> allAccounts = new ArrayList<>();
            String customerCi = null;
            
            // 프론트엔드에서 받은 계좌 정보를 파싱
            for (Object bankObj : bankAccountInfo) {
                if (bankObj instanceof java.util.Map) {
                    @SuppressWarnings("unchecked")
                    java.util.Map<String, Object> bankMap = (java.util.Map<String, Object>) bankObj;
                    
                    // customerCi 추출 (첫 번째 은행에서만)
                    if (customerCi == null) {
                        customerCi = (String) bankMap.get("customerCi");
                    }
                    
                    Object accountsObj = bankMap.get("accounts");
                    if (accountsObj instanceof java.util.List) {
                        List<Object> accountsList = (List<Object>) accountsObj;
                        
                        for (Object accountObj : accountsList) {
                            if (accountObj instanceof java.util.Map) {
                                @SuppressWarnings("unchecked")
                                java.util.Map<String, Object> accountMap = (java.util.Map<String, Object>) accountObj;
                                
                                // 계좌 정보 추출
                                String accountNumber = (String) accountMap.get("accountNumber");
                                Integer accountType = (Integer) accountMap.get("accountType");
                                
                                // balance 처리
                                BigDecimal balance = BigDecimal.ZERO;
                                Object balanceObj = accountMap.get("balance");
                                if (balanceObj != null) {
                                    if (balanceObj instanceof BigDecimal) {
                                        balance = (BigDecimal) balanceObj;
                                    } else if (balanceObj instanceof Number) {
                                        balance = new BigDecimal(((Number) balanceObj).toString());
                                    } else {
                                        balance = new BigDecimal(balanceObj.toString());
                                    }
                                }
                                
                                // openingDate 처리
                                LocalDateTime openingDateTime = LocalDateTime.now();
                                Object openingDateObj = accountMap.get("openingDate");
                                if (openingDateObj instanceof java.util.List) {
                                    // 배열 형태 [년, 월, 일, 시, 분, 초, 나노초]
                                    @SuppressWarnings("unchecked")
                                    List<Number> dateArray = (List<Number>) openingDateObj;
                                    if (dateArray.size() >= 3) {
                                        int year = dateArray.get(0).intValue();
                                        int month = dateArray.get(1).intValue();
                                        int day = dateArray.get(2).intValue();
                                        int hour = dateArray.size() > 3 ? dateArray.get(3).intValue() : 0;
                                        int minute = dateArray.size() > 4 ? dateArray.get(4).intValue() : 0;
                                        int second = dateArray.size() > 5 ? dateArray.get(5).intValue() : 0;
                                        
                                        openingDateTime = LocalDateTime.of(year, month, day, hour, minute, second);
                                    }
                                }
                                
                                MyDataAccountService.MyDataAccountInfo myDataAccount = 
                                    new MyDataAccountService.MyDataAccountInfo(
                                        accountNumber,
                                        accountType,
                                        balance,
                                        openingDateTime
                                    );
                                allAccounts.add(myDataAccount);
                            }
                        }
                    }
                }
            }
            
            if (!allAccounts.isEmpty()) {
                // customerCi가 없으면 기본값 사용
                if (customerCi == null || customerCi.isEmpty()) {
                    customerCi = "DEFAULT_CI_" + userId;
                }
                
                myDataAccountService.saveMyDataAccounts(userId, customerCi, allAccounts);
                log.info("계좌 정보 직접 저장 완료 - 저장된 계좌 수: {}, CI: {}", allAccounts.size(), customerCi);
            } else {
                log.warn("저장할 계좌 정보가 없습니다.");
            }
            
        } catch (Exception e) {
            log.error("계좌 정보 직접 저장 실패 - 사용자 ID: {}", userId, e);
            throw new RuntimeException("계좌 정보 저장 중 오류가 발생했습니다.", e);
        }
    }
    
    /**
     * 사용자 추가 정보 입력 시 계좌 정보도 함께 업데이트하는 트랜잭션 처리
     */
    @Transactional
    public void updateUserWithAccountInfo(Long userId, String additionalInfo) {
        log.info("사용자 추가 정보 업데이트 시작 - 사용자 ID: {}, 추가 정보: {}", userId, additionalInfo);
        
        try {
            // 여기서 사용자 추가 정보를 업데이트하는 로직을 구현
            // 예: 사용자 프로필 업데이트, 선호도 설정 등
            
            // 계좌 정보도 함께 업데이트 (예: 계좌 설명, 용도 등)
            // 이 부분은 실제 비즈니스 요구사항에 따라 구현
            
            log.info("사용자 추가 정보 업데이트 완료 - 사용자 ID: {}", userId);
            
        } catch (Exception e) {
            log.error("사용자 추가 정보 업데이트 실패 - 사용자 ID: {}", userId, e);
            throw new RuntimeException("사용자 추가 정보 업데이트 중 오류가 발생했습니다.", e);
        }
    }
}
