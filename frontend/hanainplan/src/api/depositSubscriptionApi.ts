import { axiosInstance } from '../lib/axiosInstance';

export interface CreateDepositAccountRequest {
  userId: number;
  accountType: number; // BankingAccount.AccountType.SAVINGS (1)
  accountName: string;
  initialBalance: number;
  depositPeriod: number; // 개월 수
  interestPaymentMethod: string; // 'AUTO' | 'MANUAL'
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

/**
 * 정기예금 계좌 생성 (가입)
 * @deprecated IRP 계좌 기반으로 변경되었습니다. subscribeDepositProduct를 사용하세요.
 */
export const createDepositAccount = async (request: CreateDepositAccountRequest): Promise<AccountDto> => {
  const response = await axiosInstance.post('/banking', request);
  return response.data;
};

/**
 * IRP 계좌 내 정기예금 상품 가입
 */
export const subscribeDepositProduct = async (request: DepositSubscribeRequest): Promise<DepositSubscribeResponse> => {
  const response = await axiosInstance.post('/banking/deposit/subscribe', request);
  return response.data;
};

