import { axiosInstance } from '../lib/axiosInstance';

export interface FundPurchaseRequest {
  userId: number;
  childFundCd: string; // 펀드 코드
  purchaseAmount: number; // 매수 금액
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
  sellUnits?: number; // 매도 좌수
  sellAll?: boolean; // 전량 매도 여부
}

export interface FundRedemptionResponse {
  success: boolean;
  sellUnits?: number;
  netAmount?: number; // 실수령액
  profit?: number; // 실현 손익
  message?: string;
  errorMessage?: string;
}

export interface FundPortfolio {
  subscriptionId: number;
  childFundCd: string;
  fundName: string;
  totalUnits: number; // 보유 좌수
  averagePrice: number;
  currentValue: number;
  profit: number;
  profitRate: number;
}

/**
 * 펀드 매수
 */
export const purchaseFund = async (request: FundPurchaseRequest): Promise<FundPurchaseResponse> => {
  const response = await axiosInstance.post('/banking/fund-subscription/purchase', request);
  return response.data;
};

/**
 * 펀드 매도 (환매)
 */
export const redeemFund = async (request: FundRedemptionRequest): Promise<FundRedemptionResponse> => {
  const response = await axiosInstance.post('/banking/fund-subscription/redeem', request);
  return response.data;
};

/**
 * 사용자의 활성 펀드 가입 목록 조회
 */
export const getActiveFundSubscriptions = async (userId: number): Promise<FundPortfolio[]> => {
  const response = await axiosInstance.get(`/banking/fund-subscription/user/${userId}/active`);
  return response.data;
};

