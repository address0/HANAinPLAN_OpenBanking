import { axiosInstance } from '../lib/axiosInstance';

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
  try {
    const response = await axiosInstance.post<IrpAccountOpenResponse>('/v1/irp-integration/accounts/open', request);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getIrpAccount = async (customerId: number): Promise<IrpAccountInfo> => {
  try {
    const response = await axiosInstance.get<IrpAccountInfo>(`/v1/irp-integration/accounts/customer/${customerId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const checkIrpAccountStatus = async (customerId: number): Promise<IrpAccountStatusResponse> => {
  try {
    const response = await axiosInstance.get<IrpAccountStatusResponse>(`/v1/irp-integration/accounts/check/${customerId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const syncIrpAccountWithHanaInPlan = async (customerId: number): Promise<{ syncSuccess: boolean; message: string }> => {
  try {
    const response = await axiosInstance.post<{ syncSuccess: boolean; message: string }>(`/v1/irp-integration/sync/customer/${customerId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const closeIrpAccount = async (accountNumber: string): Promise<{ accountNumber: string; message: string }> => {
  try {
    const response = await axiosInstance.delete<{ accountNumber: string; message: string }>(`/v1/irp/close/${accountNumber}`);
    return response.data;
  } catch (error) {
    throw error;
  }
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
  try {
    const response = await axiosInstance.get<InterestRateInfo[]>('/banking/interest-rates/all');
    return response.data;
  } catch (error) {
    throw error;
  }
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
  try {
    const response = await axiosInstance.get('/banking/deposit-products');
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getDepositProductsByBank = async (bankCode: string): Promise<{ success: boolean; bankCode: string; count: number; products: DepositProduct[] }> => {
  try {
    const response = await axiosInstance.get(`/banking/deposit-products/bank/${bankCode}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export interface DepositRecommendationRequest {
  userId: number;
  retirementDate: string;
  depositAmount: number;
}

export const getOptimalDepositRecommendation = async (request: DepositRecommendationRequest): Promise<{ success: boolean; message: string; recommendation: OptimalDepositRecommendation }> => {
  try {
    const response = await axiosInstance.post('/banking/deposit-products/recommend', request);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const subscribeDeposit = async (request: DepositSubscriptionRequest): Promise<any> => {
  try {
    const response = await axiosInstance.post('/banking/deposit/subscribe', request);
    return response.data;
  } catch (error) {
    throw error;
  }
};