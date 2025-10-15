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

export interface PersonalInfo {
  name: string;
  residentNumber: string;
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

export interface InsuranceDetails {
  coverageAmount: number;
  premium: number;
  paymentPeriod: number;
  coveragePeriod: number;
  paymentFrequency: 'MONTHLY' | 'QUARTERLY' | 'SEMI_ANNUAL' | 'ANNUAL';
  riders: string[];
}

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
  transferDate?: number;
}

export interface AgreementInfo {
  termsAgreed: boolean;
  privacyAgreed: boolean;
  marketingAgreed: boolean;
  medicalDisclosureAgreed: boolean;
  agreementDate: string;
}

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