package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.IrpAccountCreateRequest;
import com.hanainplan.domain.user.dto.IrpAccountCreateResponse;
import com.hanainplan.domain.user.entity.Customer;
import com.hanainplan.domain.user.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class IrpAccountService {

    private final CustomerRepository customerRepository;

    /**
     * IRP 계좌 개설 처리
     */
    public IrpAccountCreateResponse createIrpAccount(IrpAccountCreateRequest request) {
        log.info("IRP 계좌 개설 요청 - 고객 ID: {}, 계좌번호: {}", request.getCustomerId(), request.getIrpAccountNumber());

        // 고객 정보 조회
        Customer customer = customerRepository.findById(request.getCustomerId())
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + request.getCustomerId()));

        // 이미 IRP 계좌가 있는지 확인
        if (customer.hasIrpAccount()) {
            log.warn("고객 ID {}는 이미 IRP 계좌를 보유하고 있습니다: {}", request.getCustomerId(), customer.getIrpAccountNumber());
            return IrpAccountCreateResponse.builder()
                    .customerId(request.getCustomerId())
                    .irpAccountNumber(customer.getIrpAccountNumber())
                    .bankCode(customer.getIrpBankCode())
                    .bankName(getBankName(customer.getIrpBankCode()))
                    .message("이미 IRP 계좌를 보유하고 있습니다.")
                    .success(false)
                    .build();
        }

        // IRP 계좌 정보 업데이트
        customer.setHasIrpAccount(true);
        customer.setIrpAccountNumber(request.getIrpAccountNumber());
        customer.setIrpBankCode(request.getBankCode());
        customerRepository.save(customer);

        log.info("IRP 계좌 개설 완료 - 고객 ID: {}, 계좌번호: {}", request.getCustomerId(), request.getIrpAccountNumber());

        return IrpAccountCreateResponse.builder()
                .customerId(request.getCustomerId())
                .irpAccountNumber(request.getIrpAccountNumber())
                .bankCode(request.getBankCode())
                .bankName(request.getBankName())
                .message("IRP 계좌가 성공적으로 개설되었습니다.")
                .success(true)
                .build();
    }

    /**
     * IRP 계좌 해지 처리
     */
    public IrpAccountCreateResponse closeIrpAccount(Long customerId) {
        log.info("IRP 계좌 해지 요청 - 고객 ID: {}", customerId);

        // 고객 정보 조회
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        // IRP 계좌가 없는 경우
        if (!customer.hasIrpAccount()) {
            log.warn("고객 ID {}는 IRP 계좌를 보유하고 있지 않습니다.", customerId);
            return IrpAccountCreateResponse.builder()
                    .customerId(customerId)
                    .message("IRP 계좌를 보유하고 있지 않습니다.")
                    .success(false)
                    .build();
        }

        String closedAccountNumber = customer.getIrpAccountNumber();
        String closedBankCode = customer.getIrpBankCode();

        // IRP 계좌 정보 초기화
        customer.setHasIrpAccount(false);
        customer.setIrpAccountNumber(null);
        customer.setIrpBankCode(null);
        customerRepository.save(customer);

        log.info("IRP 계좌 해지 완료 - 고객 ID: {}, 계좌번호: {}", customerId, closedAccountNumber);

        return IrpAccountCreateResponse.builder()
                .customerId(customerId)
                .irpAccountNumber(closedAccountNumber)
                .bankCode(closedBankCode)
                .bankName(getBankName(closedBankCode))
                .message("IRP 계좌가 성공적으로 해지되었습니다.")
                .success(true)
                .build();
    }

    /**
     * IRP 계좌 정보 조회
     */
    @Transactional(readOnly = true)
    public IrpAccountCreateResponse getIrpAccount(Long customerId) {
        log.debug("IRP 계좌 정보 조회 - 고객 ID: {}", customerId);

        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new IllegalArgumentException("고객을 찾을 수 없습니다: " + customerId));

        if (!customer.hasIrpAccount()) {
            return IrpAccountCreateResponse.builder()
                    .customerId(customerId)
                    .message("IRP 계좌를 보유하고 있지 않습니다.")
                    .success(false)
                    .build();
        }

        return IrpAccountCreateResponse.builder()
                .customerId(customerId)
                .irpAccountNumber(customer.getIrpAccountNumber())
                .bankCode(customer.getIrpBankCode())
                .bankName(getBankName(customer.getIrpBankCode()))
                .message("IRP 계좌 정보 조회 성공")
                .success(true)
                .build();
    }

    /**
     * 은행 코드로 은행명 조회
     */
    private String getBankName(String bankCode) {
        if (bankCode == null) {
            return null;
        }

        return switch (bankCode) {
            case "001" -> "하나은행";
            case "004" -> "국민은행";
            case "011" -> "신한은행";
            case "020" -> "우리은행";
            case "081" -> "하나은행(구 외환은행)";
            case "088" -> "신한은행(구 조흥은행)";
            case "090" -> "카카오뱅크";
            case "092" -> "토스뱅크";
            default -> "기타은행";
        };
    }
}
