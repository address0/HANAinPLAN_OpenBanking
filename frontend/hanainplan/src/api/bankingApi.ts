import { httpGet, httpPost, httpPut, httpDelete } from '../lib/http';

// 계좌 관련 타입 정의
export interface BankingAccount {
  accountId: number;
  userId: number;
  accountNumber: string;
  accountName: string;
  accountType: number;
  accountTypeDescription: string;
  accountStatus: 'ACTIVE' | 'INACTIVE' | 'SUSPENDED' | 'CLOSED' | 'FROZEN';
  accountStatusDescription: string;
  balance: number;
  currencyCode: string;
  openedDate: string; // LocalDate는 문자열로 전송됨
  expiryDate?: string;
  interestRate?: number;
  minimumBalance?: number;
  creditLimit?: number;
  description?: string;
  createdAt: string;
  updatedAt: string;
}

export interface CreateAccountRequest {
  userId: number;
  accountType: number;
  accountName: string;
  initialBalance?: number;
  description?: string;
  purpose?: string;
  monthlyDepositAmount?: number;
  depositPeriod?: number;
  interestPaymentMethod?: 'AUTO' | 'MANUAL';
  accountPassword?: string;
}

export interface Transaction {
  transactionId: number;
  transactionNumber: string;
  fromAccountId?: number;
  fromAccountNumber?: string;
  toAccountId?: number;
  toAccountNumber?: string;
  transactionType: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'AUTO_TRANSFER' | 'INTEREST' | 'FEE' | 'REFUND' | 'REVERSAL';
  transactionTypeDescription: string;
  transactionCategory: 'SALARY' | 'PENSION' | 'SAVINGS' | 'INVESTMENT' | 'LOAN' | 'INSURANCE' | 'UTILITY' | 'SHOPPING' | 'FOOD' | 'TRANSPORT' | 'MEDICAL' | 'EDUCATION' | 'ENTERTAINMENT' | 'OTHER';
  transactionCategoryDescription: string;
  amount: number;
  balanceAfter: number;
  fee?: number;
  description?: string;
  counterpartAccountNumber?: string;
  counterpartName?: string;
  transactionStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REVERSED';
  transactionStatusDescription: string;
  transactionDate: string;
  processedDate?: string;
  failureReason?: string;
  referenceNumber?: string;
  memo?: string;
  createdAt: string;
}

export interface DepositRequest {
  accountId: number;
  amount: number;
  description?: string;
  memo?: string;
  referenceNumber?: string;
}

export interface WithdrawalRequest {
  accountId: number;
  amount: number;
  description?: string;
  memo?: string;
  referenceNumber?: string;
}

export interface TransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  description?: string;
  memo?: string;
  referenceNumber?: string;
}

export interface TransactionResponse {
  success: boolean;
  message: string;
  transactionNumber?: string;
  amount?: number;
  balanceAfter?: number;
  fee?: number;
  transactionStatus?: string;
  transactionDate?: string;
  failureReason?: string;
}

export interface TransactionHistoryRequest {
  accountId: number;
  transactionType?: 'DEPOSIT' | 'WITHDRAWAL' | 'TRANSFER' | 'AUTO_TRANSFER' | 'INTEREST' | 'FEE' | 'REFUND' | 'REVERSAL';
  transactionCategory?: 'SALARY' | 'PENSION' | 'SAVINGS' | 'INVESTMENT' | 'LOAN' | 'INSURANCE' | 'UTILITY' | 'SHOPPING' | 'FOOD' | 'TRANSPORT' | 'MEDICAL' | 'EDUCATION' | 'ENTERTAINMENT' | 'OTHER';
  startDate?: string;
  endDate?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDirection?: 'ASC' | 'DESC';
}

export interface TransactionHistoryResponse {
  content: Transaction[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// 계좌 API 함수들
export const getBankingAccounts = async (userId: number): Promise<BankingAccount[]> => {
  const response = await httpGet<BankingAccount[]>(`/banking/accounts/user/${userId}`);
  return response;
};

export const getActiveBankingAccounts = async (userId: number): Promise<BankingAccount[]> => {
  const response = await httpGet<BankingAccount[]>(`/banking/accounts/user/${userId}/active`);
  return response;
};

export const getBankingAccount = async (accountId: number): Promise<BankingAccount> => {
  const response = await httpGet<BankingAccount>(`/banking/accounts/${accountId}`);
  return response;
};

export const createBankingAccount = async (request: CreateAccountRequest): Promise<BankingAccount> => {
  const response = await httpPost<BankingAccount>('/banking/accounts', request);
  return response;
};

export const updateBankingAccount = async (accountId: number, accountName?: string, description?: string): Promise<BankingAccount> => {
  const response = await httpPut<BankingAccount>(`/banking/accounts/${accountId}`, null, {
    params: {
      accountName,
      description
    }
  });
  return response;
};

export const updateBankingAccountStatus = async (accountId: number, status: string): Promise<BankingAccount> => {
  const response = await httpPut<BankingAccount>(`/banking/accounts/${accountId}/status`, null, {
    params: {
      status
    }
  });
  return response;
};

export const getBankingAccountBalance = async (accountId: number): Promise<number> => {
  const response = await httpGet<number>(`/banking/accounts/${accountId}/balance`);
  return response;
};

// 거래 API 함수들
export const deposit = async (request: DepositRequest): Promise<TransactionResponse> => {
  const response = await httpPost<TransactionResponse>('/banking/transactions/deposit', request);
  return response;
};

export const withdrawal = async (request: WithdrawalRequest): Promise<TransactionResponse> => {
  const response = await httpPost<TransactionResponse>('/banking/transactions/withdrawal', request);
  return response;
};

export const transfer = async (request: TransferRequest): Promise<TransactionResponse> => {
  const response = await httpPost<TransactionResponse>('/banking/transactions/transfer', request);
  return response;
};

export const getTransactionHistory = async (request: TransactionHistoryRequest): Promise<TransactionHistoryResponse> => {
  const response = await httpGet<TransactionHistoryResponse>('/banking/transactions/history', request);
  return response;
};

export const getTransaction = async (transactionId: number): Promise<Transaction> => {
  const response = await httpGet<Transaction>(`/banking/transactions/${transactionId}`);
  return response;
};

export const getTransactionByNumber = async (transactionNumber: string): Promise<Transaction> => {
  const response = await httpGet<Transaction>(`/banking/transactions/number/${transactionNumber}`);
  return response;
};
