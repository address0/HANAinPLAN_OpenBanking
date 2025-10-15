package com.hanainplan.domain.banking.service;

import com.hanainplan.domain.banking.client.*;
import com.hanainplan.domain.banking.dto.InterestRateDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InterestRateService {

    private final HanaBankClient hanaBankClient;
    private final KookminBankClient kookminBankClient;
    private final ShinhanBankClient shinhanBankClient;

    public List<InterestRateDto> getAllInterestRates() {
        List<InterestRateDto> allRates = new ArrayList<>();

        allRates.addAll(getHanaRates());

        allRates.addAll(getKookminRates());

        allRates.addAll(getShinhanRates());

        return allRates;
    }

    private List<InterestRateDto> getHanaRates() {
        List<InterestRateDto> rates = new ArrayList<>();
        String[][] periods = {
            {"6개월", "0.0207"},
            {"1년", "0.0240"},
            {"2년", "0.0200"},
            {"3년", "0.0210"},
            {"5년", "0.0202"}
        };

        for (String[] period : periods) {
            rates.add(InterestRateDto.builder()
                .bankCode("HANA")
                .bankName("하나은행")
                .productCode("HANA-DEP-001")
                .productName("하나 정기예금")
                .maturityPeriod(period[0])
                .interestRate(new BigDecimal(period[1]))
                .interestType("BASIC")
                .build());
        }

        rates.add(InterestRateDto.builder()
            .bankCode("HANA")
            .bankName("하나은행")
            .productCode("HANA-DEP-002")
            .productName("하나 디폴트옵션 정기예금")
            .maturityPeriod("3년")
            .interestRate(new BigDecimal("0.0220"))
            .interestType("BASIC")
            .isIrp(true)
            .build());

        return rates;
    }

    private List<InterestRateDto> getKookminRates() {
        List<InterestRateDto> rates = new ArrayList<>();
        String[][] periods = {
            {"6개월", "0.0203"},
            {"1년", "0.0230"},
            {"2년", "0.0187"},
            {"3년", "0.0197"},
            {"5년", "0.0182"}
        };

        for (String[] period : periods) {
            rates.add(InterestRateDto.builder()
                .bankCode("KOOKMIN")
                .bankName("국민은행")
                .productCode("KOOKMIN-DEP-001")
                .productName("국민 정기예금")
                .maturityPeriod(period[0])
                .interestRate(new BigDecimal(period[1]))
                .interestType("BASIC")
                .build());
        }

        rates.add(InterestRateDto.builder()
            .bankCode("KOOKMIN")
            .bankName("국민은행")
            .productCode("KOOKMIN-DEP-002")
            .productName("국민 디폴트옵션 정기예금")
            .maturityPeriod("3년")
            .interestRate(new BigDecimal("0.0207"))
            .interestType("BASIC")
            .isIrp(true)
            .build());

        return rates;
    }

    private List<InterestRateDto> getShinhanRates() {
        List<InterestRateDto> rates = new ArrayList<>();
        String[][] periods = {
            {"6개월", "0.0198"},
            {"1년", "0.0233"},
            {"2년", "0.0197"},
            {"3년", "0.0202"},
            {"5년", "0.0205"}
        };

        for (String[] period : periods) {
            rates.add(InterestRateDto.builder()
                .bankCode("SHINHAN")
                .bankName("신한은행")
                .productCode("SHINHAN-DEP-001")
                .productName("신한 정기예금")
                .maturityPeriod(period[0])
                .interestRate(new BigDecimal(period[1]))
                .interestType("BASIC")
                .build());
        }

        rates.add(InterestRateDto.builder()
            .bankCode("SHINHAN")
            .bankName("신한은행")
            .productCode("SHINHAN-DEP-002")
            .productName("신한 디폴트옵션 정기예금")
            .maturityPeriod("3년")
            .interestRate(new BigDecimal("0.0212"))
            .interestType("BASIC")
            .isIrp(true)
            .build());

        return rates;
    }
}