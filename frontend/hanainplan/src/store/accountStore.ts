import { create } from 'zustand';
import type { BankingAccount } from '../api/bankingApi';

interface AccountStore {
  accounts: BankingAccount[];
  selectedAccountId: number | null;
  isLoading: boolean;
  error: string | null;
  setAccounts: (accounts: BankingAccount[]) => void;
  addAccount: (account: BankingAccount) => void;
  updateAccount: (accountId: number, updates: Partial<BankingAccount>) => void;
  removeAccount: (accountId: number) => void;
  setSelectedAccount: (accountId: number | null) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  clearAccounts: () => void;
}

export const useAccountStore = create<AccountStore>((set) => ({
  accounts: [],
  selectedAccountId: null,
  isLoading: false,
  error: null,
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
  setSelectedAccount: (accountId: number | null) => set({ selectedAccountId: accountId }),
  setLoading: (loading: boolean) => set({ isLoading: loading }),
  setError: (error: string | null) => set({ error }),
  clearAccounts: () => set({ accounts: [], selectedAccountId: null, error: null }),
}));
