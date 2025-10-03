import axios from 'axios';
import type {
  FundClassDetail,
  FundPurchaseRequest,
  FundPurchaseResponse,
  FundRedemptionRequest,
  FundRedemptionResponse,
  FundSubscription,
  FundTransaction,
  FundTransactionStats,
} from '../types/fund.types';

const API_BASE_URL = 'http://localhost:8080/api/banking';

const fundApi = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

/**
 * 펀드 상품 API
 */
export const fundProductApi = {
  // 판매중인 펀드 클래스 목록 조회
  getAllFundClasses: async (): Promise<FundClassDetail[]> => {
    const response = await fundApi.get('/fund-classes');
    return response.data;
  },

  // 펀드 클래스 상세 조회
  getFundClass: async (childFundCd: string): Promise<FundClassDetail> => {
    const response = await fundApi.get(`/fund-classes/${childFundCd}`);
    return response.data;
  },

  // 모펀드별 클래스 조회
  getFundClassesByMaster: async (fundCd: string): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/master/${fundCd}`);
    return response.data;
  },

  // 자산 유형별 조회
  getFundClassesByAssetType: async (assetType: string): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/asset-type/${assetType}`);
    return response.data;
  },

  // 클래스 코드별 조회
  getFundClassesByClassCode: async (classCode: string): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/class-code/${classCode}`);
    return response.data;
  },

  // 최소 투자금액 이하 펀드 조회
  getFundClassesByMaxAmount: async (maxAmount: number): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/max-amount/${maxAmount}`);
    return response.data;
  },
};

/**
 * 펀드 매수/매도 API
 */
export const fundSubscriptionApi = {
  // 펀드 매수
  purchaseFund: async (request: FundPurchaseRequest): Promise<FundPurchaseResponse> => {
    const response = await fundApi.post('/fund-subscription/purchase', request);
    return response.data;
  },

  // 펀드 매도
  redeemFund: async (request: FundRedemptionRequest): Promise<FundRedemptionResponse> => {
    const response = await fundApi.post('/fund-subscription/redeem', request);
    return response.data;
  },

  // 활성 펀드 가입 목록 조회 (userId 기반)
  getActiveSubscriptions: async (userId: number): Promise<FundSubscription[]> => {
    const response = await fundApi.get(`/fund-subscription/user/${userId}/active`);
    return response.data;
  },
};

/**
 * 펀드 거래 내역 API (userId 기반)
 */
export const fundTransactionApi = {
  // 사용자의 모든 거래 내역 조회
  getUserTransactions: async (userId: number): Promise<FundTransaction[]> => {
    const response = await fundApi.get(`/fund-subscription/user/${userId}/transactions`);
    return response.data;
  },

  // 거래 통계 조회
  getTransactionStats: async (userId: number): Promise<FundTransactionStats> => {
    const response = await fundApi.get(`/fund-subscription/user/${userId}/stats`);
    return response.data;
  },
};

/**
 * Hana Bank API (직접 호출용)
 */
const HANA_BANK_URL = 'http://localhost:8081/api/hana';

export const hanaBankFundApi = {
  // 고객의 펀드 가입 목록 조회
  getCustomerSubscriptions: async (customerCi: string): Promise<FundSubscription[]> => {
    const response = await axios.get(`${HANA_BANK_URL}/fund-subscription/customer/${customerCi}`);
    return response.data;
  },

  // 활성 펀드 가입 목록 조회
  getActiveSubscriptions: async (customerCi: string): Promise<FundSubscription[]> => {
    const response = await axios.get(`${HANA_BANK_URL}/fund-subscription/customer/${customerCi}/active`);
    return response.data;
  },
};

export default fundApi;

