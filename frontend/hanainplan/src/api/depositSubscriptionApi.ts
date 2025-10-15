import { axiosInstance } from '../lib/axiosInstance';

export interface CreateDepositAccountRequest {
  userId: number;
  accountType: number;
  accountName: string;
  initialBalance: number;
  depositPeriod: number;
  interestPaymentMethod: string;
  accountPassword: string;
  description?: string;
  purpose?: string;
}

export interface DepositSubscribeRequest {
  userId: number;
  bankCode: string;
  irpAccountNumber: string;
  linkedAccountNumber: string;
  depositCode: string;
  productName: string;
  productType: number;
  contractPeriod: number;
  subscriptionAmount: number;
}

export interface DepositSubscribeResponse {
  success: boolean;
  message?: string;
  subscriptionId?: number;
  depositCode?: string;
  subscriptionAmount?: number;
  contractPeriod?: number;
  expectedMaturityAmount?: number;
}

export interface AccountDto {
  accountId: number;
  userId: number;
  accountNumber: string;
  accountName: string;
  accountType: number;
  accountStatus: number;
  balance: number;
  currencyCode: string;
  openedDate: string;
  expiryDate?: string;
  description?: string;
  purpose?: string;
  depositPeriod?: number;
  interestPaymentMethod?: string;
}

export const createDepositAccount = async (request: CreateDepositAccountRequest): Promise<AccountDto> => {
  const response = await axiosInstance.post('/banking', request);
  return response.data;
};

export const subscribeDepositProduct = async (request: DepositSubscribeRequest): Promise<DepositSubscribeResponse> => {
  const response = await axiosInstance.post('/banking/deposit/subscribe', request);
  return response.data;
};