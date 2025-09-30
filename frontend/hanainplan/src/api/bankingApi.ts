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
  transactionDirection: 'CREDIT' | 'DEBIT';
  transactionDirectionDescription: string;
  transactionDirectionSymbol: string;
  description?: string;
  transactionStatus: 'PENDING' | 'PROCESSING' | 'COMPLETED' | 'FAILED' | 'CANCELLED' | 'REVERSED';
  transactionStatusDescription: string;
  transactionDate: string | number[];
  processedDate?: string;
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
  const response = await httpGet<BankingAccount[]>(`/banking/user/${userId}`);
  return response;
};

export const getActiveBankingAccounts = async (userId: number): Promise<BankingAccount[]> => {
  const response = await httpGet<BankingAccount[]>(`/banking/user/${userId}/active`);
  return response;
};

// 통합 계좌 조회 (일반 계좌 + IRP 계좌)
export interface AllAccountsResponse {
  bankingAccounts: BankingAccount[];
  irpAccount?: IrpAccountInfo;
  totalBankingBalance: number;
  totalIrpBalance: number;
  totalBalance: number;
}

export const getAllAccounts = async (userId: number): Promise<AllAccountsResponse> => {
  try {
    // 일반 은행 계좌 조회
    const bankingAccounts = await getActiveBankingAccounts(userId);
    const totalBankingBalance = bankingAccounts.reduce((total, account) => total + (account.balance || 0), 0);

    // IRP 계좌 조회 시도
    let irpAccount: IrpAccountInfo | undefined;
    let totalIrpBalance = 0;

    try {
      // IRP 관련 import가 필요함
      const { getIrpAccount } = await import('./productApi');
      const irpResponse = await getIrpAccount(userId);
      irpAccount = irpResponse;
      totalIrpBalance = irpResponse.currentBalance || 0;
    } catch (error) {
      // IRP 계좌가 없는 경우는 정상 상황이므로 무시
      console.log('IRP 계좌 없음 또는 조회 실패:', error);
    }

    return {
      bankingAccounts,
      irpAccount,
      totalBankingBalance,
      totalIrpBalance,
      totalBalance: totalBankingBalance + totalIrpBalance
    };
  } catch (error) {
    console.error('통합 계좌 조회 실패:', error);
    throw error;
  }
};

export const getBankingAccount = async (accountId: number): Promise<BankingAccount> => {
  const response = await httpGet<BankingAccount>(`/banking/${accountId}`);
  return response;
};

export const createBankingAccount = async (request: CreateAccountRequest): Promise<BankingAccount> => {
  const response = await httpPost<BankingAccount>('/banking', request);
  return response;
};

export const updateBankingAccount = async (accountId: number, accountName?: string, description?: string): Promise<BankingAccount> => {
  const response = await httpPut<BankingAccount>(`/banking/${accountId}`, null, {
    params: {
      accountName,
      description
    }
  });
  return response;
};

export const updateBankingAccountStatus = async (accountId: number, status: string): Promise<BankingAccount> => {
  const response = await httpPut<BankingAccount>(`/banking/${accountId}/status`, null, {
    params: {
      status
    }
  });
  return response;
};

export const getBankingAccountBalance = async (accountId: number): Promise<number> => {
  const response = await httpGet<number>(`/banking/${accountId}/balance`);
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
