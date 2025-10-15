import { create } from 'zustand';
import type { BankingAccount, AllAccountsResponse } from '../api/bankingApi';

interface AccountStore {
  accounts: BankingAccount[];
  selectedAccountId: number | null;
  selectedAccountType: 'BANKING' | 'IRP' | null;
  isLoading: boolean;
  error: string | null;
  allAccountsData: AllAccountsResponse | null;
  hasIrpAccount: boolean;

  setAccounts: (accounts: BankingAccount[]) => void;
  addAccount: (account: BankingAccount) => void;
  updateAccount: (accountId: number, updates: Partial<BankingAccount>) => void;
  removeAccount: (accountId: number) => void;
  setSelectedAccount: (accountId: number | null, accountType?: 'BANKING' | 'IRP') => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearAccounts: () => void;

  setAllAccountsData: (data: AllAccountsResponse) => void;
  setHasIrpAccount: (hasIrp: boolean) => void;
  clearAllAccountsData: () => void;
}

export const useAccountStore = create<AccountStore>((set) => ({
  accounts: [],
  selectedAccountId: null,
  selectedAccountType: null,
  isLoading: false,
  error: null,
  allAccountsData: null,
  hasIrpAccount: false,

  setAccounts: (accounts: BankingAccount[]) => set({ accounts, error: null }),
  addAccount: (account: BankingAccount) =>
    set((state) => ({
      accounts: [...state.accounts, account],
      error: null,
    })),
  updateAccount: (accountId: number, updates: Partial<BankingAccount>) =>
    set((state) => ({
      accounts: state.accounts.map((account) =>
        account.accountId === accountId ? { ...account, ...updates } : account
      ),
      error: null,
    })),
  removeAccount: (accountId: number) =>
    set((state) => ({
      accounts: state.accounts.filter((account) => account.accountId !== accountId),
      selectedAccountId: state.selectedAccountId === accountId ? null : state.selectedAccountId,
    })),
  setSelectedAccount: (accountId: number | null, accountType?: 'BANKING' | 'IRP') => set({
    selectedAccountId: accountId,
    selectedAccountType: accountType || null
  }),
  setLoading: (loading: boolean) => set({ isLoading: loading }),
  setError: (error: string | null) => set({ error }),
  clearAccounts: () => set({ accounts: [], selectedAccountId: null, selectedAccountType: null, error: null }),

  setAllAccountsData: (data: AllAccountsResponse) => set({
    allAccountsData: data,
    accounts: data.bankingAccounts,
    hasIrpAccount: !!data.irpAccount,
    error: null
  }),
  setHasIrpAccount: (hasIrp: boolean) => set({ hasIrpAccount: hasIrp }),
  clearAllAccountsData: () => set({
    allAccountsData: null,
    hasIrpAccount: false,
    accounts: [],
    selectedAccountId: null,
    selectedAccountType: null
  }),
}));