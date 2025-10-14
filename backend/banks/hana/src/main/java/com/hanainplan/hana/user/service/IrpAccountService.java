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

    /**
     * IRP 계좌 개설 요청 처리
     * @param request IRP 계좌 개설 요청 정보
     * @return IRP 계좌 개설 응답
     * @throws Exception 중복 계좌 보유, 유효하지 않은 정보 등의 예외
     */
    IrpAccountResponse openIrpAccount(IrpAccountRequest request) throws Exception;

    /**
     * 사용자 CI로 IRP 계좌 조회
     * @param customerCi 사용자 CI
     * @return IRP 계좌 정보 (Optional)
     */
    Optional<IrpAccount> getIrpAccountByCustomerCi(String customerCi);

    /**
     * 계좌번호로 IRP 계좌 조회
     * @param accountNumber 계좌번호
     * @return IRP 계좌 정보 (Optional)
     */
    Optional<IrpAccount> getIrpAccountByAccountNumber(String accountNumber);

    /**
     * 중복 계좌 보유 여부 확인
     * @param customerCi 사용자 CI
     * @return 중복 보유 여부
     */
    boolean hasExistingIrpAccount(String customerCi);

    /**
     * IRP 계좌 상태 업데이트
     * @param accountNumber 계좌번호
     * @param status 새로운 상태
     */
    void updateAccountStatus(String accountNumber, String status);

    /**
     * 월 자동납입 처리 (스케줄러에서 호출)
     * @param accountNumber 계좌번호
     * @param amount 납입금액
     */
    void processMonthlyDeposit(String accountNumber, BigDecimal amount);

    /**
     * IRP 계좌 해지 처리
     * @param accountNumber 계좌번호
     * @return 해지 처리 결과
     */
    boolean closeIrpAccount(String accountNumber);

    /**
     * HANAinPLAN과 계좌 정보 동기화
     * @param customerCi 사용자 CI
     * @return 동기화 결과
     */
    boolean syncWithHanaInPlan(String customerCi);

    /**
     * 모든 IRP 계좌 조회 (관리자용)
     * @return 모든 IRP 계좌 목록
     */
    List<IrpAccount> getAllIrpAccounts();

    /**
     * 특정 시간 이후 변경된 IRP 계좌 조회
     * @param sinceDateTime 기준 시간
     * @return 변경된 IRP 계좌 목록
     */
    List<IrpAccount> getChangedIrpAccounts(LocalDateTime sinceDateTime);

    /**
     * IRP 통계 정보 조회
     * @return IRP 통계 정보
     */
    Map<String, Object> getIrpStatistics();

    /**
     * IRP 계좌 입금 처리
     * @param accountNumber IRP 계좌번호
     * @param amount 입금금액
     * @param description 입금 설명
     * @return IRP 입금 처리 결과
     */
    com.hanainplan.hana.user.dto.IrpDepositResponse processIrpDeposit(String accountNumber, BigDecimal amount, String description);
}
