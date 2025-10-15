import { axiosInstance } from '../lib/axiosInstance';

export interface FundPurchaseRequest {
  userId: number;
  childFundCd: string;
  purchaseAmount: number;
}

export interface FundPurchaseResponse {
  success: boolean;
  subscriptionId?: number;
  purchaseAmount?: number;
  purchaseUnits?: number;
  message?: string;
  errorMessage?: string;
}

export interface FundRedemptionRequest {
  userId: number;
  subscriptionId: number;
  sellUnits?: number;
  sellAll?: boolean;
}

export interface FundRedemptionResponse {
  success: boolean;
  sellUnits?: number;
  netAmount?: number;
  profit?: number;
  message?: string;
  errorMessage?: string;
}

export interface FundPortfolio {
  subscriptionId: number;
  childFundCd: string;
  fundName: string;
  totalUnits: number;
  averagePrice: number;
  currentValue: number;
  profit: number;
  profitRate: number;
}

export const purchaseFund = async (request: FundPurchaseRequest): Promise<FundPurchaseResponse> => {
  const response = await axiosInstance.post('/banking/fund-subscription/purchase', request);
  return response.data;
};

export const redeemFund = async (request: FundRedemptionRequest): Promise<FundRedemptionResponse> => {
  const response = await axiosInstance.post('/banking/fund-subscription/redeem', request);
  return response.data;
};

export const getActiveFundSubscriptions = async (userId: number): Promise<FundPortfolio[]> => {
  const response = await axiosInstance.get(`/banking/fund-subscription/user/${userId}/active`);
  return response.data;
};