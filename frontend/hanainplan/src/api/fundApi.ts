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

export const fundProductApi = {
  getAllFundClasses: async (): Promise<FundClassDetail[]> => {
    const response = await fundApi.get('/fund-classes');
    return response.data;
  },

  getFundClass: async (childFundCd: string): Promise<FundClassDetail> => {
    const response = await fundApi.get(`/fund-classes/${childFundCd}`);
    return response.data;
  },

  getFundClassesByMaster: async (fundCd: string): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/master/${fundCd}`);
    return response.data;
  },

  getFundClassesByAssetType: async (assetType: string): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/asset-type/${assetType}`);
    return response.data;
  },

  getFundClassesByClassCode: async (classCode: string): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/class-code/${classCode}`);
    return response.data;
  },

  getFundClassesByMaxAmount: async (maxAmount: number): Promise<FundClassDetail[]> => {
    const response = await fundApi.get(`/fund-classes/max-amount/${maxAmount}`);
    return response.data;
  },
};

export const fundSubscriptionApi = {
  purchaseFund: async (request: FundPurchaseRequest): Promise<FundPurchaseResponse> => {
    const response = await fundApi.post('/fund-subscription/purchase', request);
    return response.data;
  },

  redeemFund: async (request: FundRedemptionRequest): Promise<FundRedemptionResponse> => {
    const response = await fundApi.post('/fund-subscription/redeem', request);
    return response.data;
  },

  getActiveSubscriptions: async (userId: number): Promise<FundSubscription[]> => {
    const response = await fundApi.get(`/fund-subscription/user/${userId}/active`);
    return response.data;
  },
};

export const fundTransactionApi = {
  getUserTransactions: async (userId: number): Promise<FundTransaction[]> => {
    const response = await fundApi.get(`/fund-subscription/user/${userId}/transactions`);
    return response.data;
  },

  getTransactionStats: async (userId: number): Promise<FundTransactionStats> => {
    const response = await fundApi.get(`/fund-subscription/user/${userId}/stats`);
    return response.data;
  },
};

const HANA_BANK_URL = 'http://localhost:8081/api/hana';

export const hanaBankFundApi = {
  getCustomerSubscriptions: async (customerCi: string): Promise<FundSubscription[]> => {
    const response = await axios.get(`${HANA_BANK_URL}/fund-subscription/customer/${customerCi}`);
    return response.data;
  },

  getActiveSubscriptions: async (customerCi: string): Promise<FundSubscription[]> => {
    const response = await axios.get(`${HANA_BANK_URL}/fund-subscription/customer/${customerCi}/active`);
    return response.data;
  },
};

export default fundApi;