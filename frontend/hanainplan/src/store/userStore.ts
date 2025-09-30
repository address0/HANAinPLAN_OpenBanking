import { create } from 'zustand';
import { persist } from 'zustand/middleware';

export interface User {
  id: number;
  userId: number;
  phoneNumber: string;
  name: string;
  userType: 'GENERAL' | 'CONSULTANT';
  ci?: string; // 실명인증 CI값
  isActive: boolean;
  createdAt: string;
  updatedAt: string;
}

interface UserStore {
  user: User | null;
  isLoggedIn: boolean;
  setUser: (user: User) => void;
  clearUser: () => void;
  updateUser: (updates: Partial<User>) => void;
}

export const useUserStore = create<UserStore>()(
  persist(
    (set) => ({
      user: null,
      isLoggedIn: false,
      setUser: (user: User) => set({ user, isLoggedIn: true }),
      clearUser: () => set({ user: null, isLoggedIn: false }),
      updateUser: (updates: Partial<User>) =>
        set((state) => ({
          user: state.user ? { ...state.user, ...updates } : null,
        })),
    }),
    {
      name: 'user-storage',
      partialize: (state) => ({ user: state.user, isLoggedIn: state.isLoggedIn }),
    }
  )
);
