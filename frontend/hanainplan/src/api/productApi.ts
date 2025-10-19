import { axiosInstance } from '../lib/axiosInstance';
import { httpGet, httpPost, httpDelete } from '../lib/http';

// 리스크 프로파일 관련 타입
export interface RiskProfileRequest {
  customerId: number;
  sessionId?: string;
  answers: QuestionAnswer[];
}

export interface QuestionAnswer {
  questionNumber: number;
  answerScore: number;
  questionText?: string;
  answerText?: string;
}

export interface RiskProfileResponse {
  customerId: number;
  riskProfileScore: number;
  riskProfileType: string;
  riskProfileTypeName: string;
  answers: QuestionAnswer[];
  evaluatedAt: string;
}

export interface IrpAccountOpenRequest {
  customerId: number;
  initialDeposit: number;
  monthlyDeposit?: number;
  isAutoDeposit: boolean;
  depositDay: number;
  investmentStyle: 'CONSERVATIVE' | 'MODERATE_CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
  linkedMainAccount: string;
}

export interface IrpAccountOpenResponse {
  accountNumber: string;
  accountStatus: string;
  initialDeposit: number;
  monthlyDeposit?: number;
  investmentStyle: string;
  openDate: string;
  createdAt: string;
  message: string;
  success: boolean;
}

export interface IrpAccountInfo {
  irpAccountId: number;
  customerCi: string;
  accountNumber: string;
  accountStatus: string;
  initialDeposit: number;
  monthlyDeposit: number;
  currentBalance: number;
  investmentStyle: string;
  productCode?: string;
  openDate: string;
  lastContributionDate: string;
  totalContribution: number;
  totalReturn: number;
  returnRate: number;
  managementFee: number;
  isAutoDeposit: boolean;
  depositDay: number;
  linkedMainAccount: string;
  taxBenefitYear: number;
  maturityDate?: string;
  isMatured: boolean;
  penaltyAmount: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface IrpAccountStatusResponse {
  customerCi: string;
  hasIrpAccount: boolean;
  message: string;
}

export const openIrpAccount = async (request: IrpAccountOpenRequest): Promise<IrpAccountOpenResponse> => {
  return httpPost<IrpAccountOpenResponse, IrpAccountOpenRequest>('/v1/irp-integration/accounts/open', request);
};

export const getIrpAccount = async (customerId: number): Promise<IrpAccountInfo> => {
  return httpGet<IrpAccountInfo, { customerId: number }>(`/v1/irp-integration/accounts/customer/${customerId}`, { customerId });
};

export const checkIrpAccountStatus = async (customerId: number): Promise<IrpAccountStatusResponse> => {
  return httpGet<IrpAccountStatusResponse, { customerId: number }>(`/v1/irp-integration/accounts/check/${customerId}`, { customerId });
};

export const syncIrpAccountWithHanaInPlan = async (customerId: number): Promise<{ syncSuccess: boolean; message: string }> => {
  return httpPost<{ syncSuccess: boolean; message: string }, { customerId: number }>(`/v1/irp-integration/sync/customer/${customerId}`, { customerId });
};

export const closeIrpAccount = async (accountNumber: string): Promise<{ accountNumber: string; message: string }> => {
  return httpDelete<{ accountNumber: string; message: string }, { accountNumber: string }>(`/v1/irp/close/${accountNumber}`, { accountNumber });
};

export interface InterestRateInfo {
  bankCode: string;
  bankName: string;
  productCode: string;
  productName: string;
  maturityPeriod: string;
  interestRate: number;
  interestType: string;
}

export interface DepositProduct {
  depositCode: string;
  name: string;
  bankCode: string;
  bankName: string;
  description: string;
  rateInfo: string;
}

export const getAllInterestRates = async (): Promise<InterestRateInfo[]> => {
  return httpGet<InterestRateInfo[], {}>('/banking/interest-rates/all', {});
};

export interface OptimalDepositRecommendation {
  depositCode: string;
  depositName: string;
  bankCode: string;
  bankName: string;
  productType: number;
  productTypeName: string;
  contractPeriod: number;
  contractPeriodUnit: string;
  maturityPeriod: string;
  depositAmount: number;
  appliedRate: number;
  expectedInterest: number;
  expectedMaturityAmount: number;
  expectedMaturityDate: string;
  recommendationReason: string;
  yearsToRetirement: number;
  currentIrpBalance: number;
}

export interface DepositSubscriptionRequest {
  userId: number;
  bankCode: string;
  irpAccountNumber: string;
  linkedAccountNumber: string;
  depositCode: string;
  productType: number;
  contractPeriod: number;
  subscriptionAmount: number;
}

export const getAllDepositProducts = async (): Promise<{ success: boolean; count: number; products: DepositProduct[] }> => {
  return httpGet<{ success: boolean; count: number; products: DepositProduct[] }, {}>('/banking/deposit-products', {});
};

export const getDepositProductsByBank = async (bankCode: string): Promise<{ success: boolean; bankCode: string; count: number; products: DepositProduct[] }> => {
  return httpGet<{ success: boolean; bankCode: string; count: number; products: DepositProduct[] }, { bankCode: string }>(`/banking/deposit-products/bank/${bankCode}`, { bankCode });
};

export interface DepositRecommendationRequest {
  userId: number;
  retirementDate: string;
  depositAmount: number;
}

export const getOptimalDepositRecommendation = async (request: DepositRecommendationRequest): Promise<{ success: boolean; message: string; recommendation: OptimalDepositRecommendation }> => {
  return httpPost<{ success: boolean; message: string; recommendation: OptimalDepositRecommendation }, DepositRecommendationRequest>('/banking/deposit-products/recommend', request);
};

export const subscribeDeposit = async (request: DepositSubscriptionRequest): Promise<any> => {
  return httpPost<any, DepositSubscriptionRequest>('/banking/deposit/subscribe', request);
};

// 리스크 프로파일 관련 API 함수들
export const evaluateRiskProfile = async (request: RiskProfileRequest): Promise<RiskProfileResponse> => {
  return httpPost<RiskProfileResponse, RiskProfileRequest>('/risk-profile/evaluate', request);
};

export const getRiskProfile = async (customerId: number): Promise<RiskProfileResponse> => {
  return httpGet<RiskProfileResponse, {}>(`/risk-profile/${customerId}`, {});
};

export const getRiskProfileQuestions = async (): Promise<string[]> => {
  return httpPost<string[], {}>('/risk-profile/questions', {});
};

export const getRiskProfileAnswerOptions = async (questionNumber: number): Promise<string[]> => {
  return httpPost<string[], { questionNumber: number }>(`/risk-profile/answers/${questionNumber}`, { questionNumber });
};