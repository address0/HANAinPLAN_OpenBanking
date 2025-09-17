package com.hanainplan.domain.insurance.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsuranceApplicationDto {
    private String id;
    private String productId;
    private PersonalInfoDto applicantInfo;
    private PersonalInfoDto beneficiaryInfo;
    private InsuranceDetailsDto insuranceDetails;
    private PaymentInfoDto paymentInfo;
    private AgreementInfoDto agreementInfo;
    private String status;
    private LocalDateTime applicationDate;
    private String policyNumber;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class InsuranceDetailsDto {
    private Long coverageAmount;
    private Long premium;
    private Integer paymentPeriod;
    private Integer coveragePeriod;
    private String paymentFrequency;
    private java.util.List<String> riders;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PaymentInfoDto {
    private String paymentMethod;
    private BankAccountDto bankAccount;
    private CreditCardDto creditCard;
    private Boolean autoTransfer;
    private Integer transferDate;
}

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class CreditCardDto {
    private String cardNumber;
    private String expiryDate;
    private String cardHolder;
}


@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
class AgreementInfoDto {
    private Boolean termsAgreed;
    private Boolean privacyAgreed;
    private Boolean marketingAgreed;
    private Boolean medicalDisclosureAgreed;
    private String agreementDate;
}

