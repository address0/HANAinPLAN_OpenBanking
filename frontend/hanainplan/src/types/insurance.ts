// 보험 상품 타입
export interface InsuranceProduct {
  id: string;
  name: string;
  category: '생명보험' | '건강보험' | '자동차보험' | '여행보험' | '화재보험';
  description: string;
  coverage: string;
  benefits: string[];
  exclusions: string[];
  minAge: number;
  maxAge: number;
  minPremium: number;
  maxPremium: number;
  isActive: boolean;
  createdAt: string;
}

// 보험 가입 신청 정보
export interface InsuranceApplication {
  productId: string;
  applicantInfo: PersonalInfo;
  beneficiaryInfo: PersonalInfo;
  insuranceDetails: InsuranceDetails;
  paymentInfo: PaymentInfo;
  agreementInfo: AgreementInfo;
  status: 'DRAFT' | 'SUBMITTED' | 'UNDER_REVIEW' | 'APPROVED' | 'REJECTED' | 'COMPLETED';
  applicationDate: string;
  policyNumber?: string;
}

// 개인정보
export interface PersonalInfo {
  name: string;
  residentNumber: string; // 주민번호 (암호화 필요)
  gender: 'M' | 'F';
  birthDate: string;
  phoneNumber: string;
  email: string;
  address: {
    zipCode: string;
    address1: string;
    address2?: string;
  };
  occupation: string;
  maritalStatus: 'SINGLE' | 'MARRIED' | 'DIVORCED' | 'WIDOWED';
}

// 보험 세부 정보
export interface InsuranceDetails {
  coverageAmount: number; // 가입금액
  premium: number; // 보험료
  paymentPeriod: number; // 납입기간 (년)
  coveragePeriod: number; // 보장기간 (년)
  paymentFrequency: 'MONTHLY' | 'QUARTERLY' | 'SEMI_ANNUAL' | 'ANNUAL';
  riders: string[]; // 특약
}

// 결제 정보
export interface PaymentInfo {
  paymentMethod: 'BANK_TRANSFER' | 'CREDIT_CARD' | 'DEBIT_CARD';
  bankAccount?: {
    bankCode: string;
    accountNumber: string;
    accountHolder: string;
  };
  creditCard?: {
    cardNumber: string;
    expiryDate: string;
    cardHolder: string;
  };
  autoTransfer: boolean;
  transferDate?: number; // 매월 이체일
}

// 동의 정보
export interface AgreementInfo {
  termsAgreed: boolean;
  privacyAgreed: boolean;
  marketingAgreed: boolean;
  medicalDisclosureAgreed: boolean;
  agreementDate: string;
}

// 보험료 계산 요청
export interface PremiumCalculationRequest {
  productId: string;
  age: number;
  gender: 'M' | 'F';
  coverageAmount: number;
  paymentPeriod: number;
  coveragePeriod: number;
  paymentFrequency: 'MONTHLY' | 'QUARTERLY' | 'SEMI_ANNUAL' | 'ANNUAL';
  riders: string[];
  medicalHistory?: string[];
}

// 보험료 계산 응답
export interface PremiumCalculationResponse {
  basePremium: number;
  riderPremium: number;
  totalPremium: number;
  discount: number;
  finalPremium: number;
  breakdown: {
    category: string;
    amount: number;
    description: string;
  }[];
}

