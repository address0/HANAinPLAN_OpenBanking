import { httpGet, httpPost, httpPut } from '../lib/http';

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
  openedDate: string;
  expiryDate?: string;
  interestRate?: number;
  minimumBalance?: number;
  creditLimit?: number;
  description: string;
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
  accountNumber?: string;
  accountId?: number;
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

export const getBankingAccounts = async (userId: number): Promise<BankingAccount[]> => {
  const response = await httpGet<BankingAccount[]>(`/banking/user/${userId}`);
  return response;
};

export const getActiveBankingAccounts = async (userId: number): Promise<BankingAccount[]> => {
  const response = await httpGet<BankingAccount[]>(`/banking/user/${userId}/active`);
  return response;
};

export interface IrpAccountInfo {
  accountNumber: string;
  currentBalance: number;
  accountName?: string;
  totalContribution?: number;
  returnRate?: number;
  investmentStyle?: string;
  openDate?: string;
  maturityDate?: string;
  monthlyDeposit?: number;
  isAutoDeposit?: boolean;
  depositDay?: number;
  linkedMainAccount?: string;
}

export interface AllAccountsResponse {
  bankingAccounts: BankingAccount[];
  irpAccount?: IrpAccountInfo;
  totalBankingBalance: number;
  totalIrpBalance: number;
  totalBalance: number;
}

export const getAllAccounts = async (userId: number): Promise<AllAccountsResponse> => {
  try {
    const allBankingAccounts = await getActiveBankingAccounts(userId);

    let irpAccount: IrpAccountInfo | undefined;
    let totalIrpBalance = 0;

    try {
      const { getIrpAccount } = await import('./productApi');
      const irpResponse = await getIrpAccount(userId);
      irpAccount = irpResponse;
      totalIrpBalance = irpResponse.currentBalance || 0;
    } catch (error) {
    }

    const bankingAccounts = allBankingAccounts.filter(account => {
      const isIrpAccount = account.accountType === 6 &&
                          (account.accountName?.includes('IRP') ||
                           account.description?.includes('IRP') ||
                           (irpAccount && account.accountNumber === irpAccount.accountNumber));
      return !isIrpAccount;
    });

    const totalBankingBalance = bankingAccounts.reduce((total, account) => total + (account.balance || 0), 0);

    return {
      bankingAccounts,
      irpAccount,
      totalBankingBalance,
      totalIrpBalance,
      totalBalance: totalBankingBalance + totalIrpBalance
    };
  } catch (error) {
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

export interface InternalTransferRequest {
  fromAccountId: number;
  toAccountId: number;
  amount: number;
  description?: string;
  memo?: string;
}

export const internalTransfer = async (request: InternalTransferRequest): Promise<TransactionResponse> => {
  const response = await httpPost<TransactionResponse>('/banking/transactions/internal-transfer', request);
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

export interface AccountVerificationResponse {
  exists: boolean;
  accountType?: 'IRP' | 'GENERAL';
  bankCode?: string;
  accountStatus?: string;
  accountNumber?: string;
  message?: string;
}

export const verifyExternalAccount = async (accountNumber: string): Promise<AccountVerificationResponse> => {
  const response = await httpPost<AccountVerificationResponse>('/banking/transactions/verify-account', {
    accountNumber
  });
  return response;
};

export interface TransferToIrpRequest {
  fromAccountId: number;
  toIrpAccountNumber: string;
  amount: number;
  description?: string;
}

export const transferToIrp = async (request: TransferToIrpRequest): Promise<TransactionResponse> => {
  const response = await httpPost<TransactionResponse>('/banking/transactions/transfer-to-irp', request);
  return response;
};

export interface ExternalTransferRequest {
  fromAccountId: number;
  toAccountNumber: string;
  amount: number;
  description?: string;
}

export const externalTransfer = async (request: ExternalTransferRequest): Promise<TransactionResponse> => {
  const response = await httpPost<TransactionResponse>('/banking/transactions/external-transfer', request);
  return response;
};