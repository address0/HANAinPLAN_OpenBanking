import { create } from 'zustand';
import { persist } from 'zustand/middleware';
import type {
  InsuranceProduct,
  InsuranceApplication,
  PersonalInfo,
  InsuranceDetails,
  PaymentInfo,
  AgreementInfo,
  PremiumCalculationResponse
} from '../types/insurance';

interface InsuranceStore {
  currentStep: number;

  selectedProduct: InsuranceProduct | null;

  application: Partial<InsuranceApplication>;

  premiumCalculation: PremiumCalculationResponse | null;

  isLoading: boolean;

  error: string | null;

  setCurrentStep: (step: number) => void;
  setSelectedProduct: (product: InsuranceProduct) => void;
  setPersonalInfo: (personalInfo: PersonalInfo) => void;
  setInsuranceDetails: (details: InsuranceDetails) => void;
  setPaymentInfo: (paymentInfo: PaymentInfo) => void;
  setAgreementInfo: (agreementInfo: AgreementInfo) => void;
  setPremiumCalculation: (calculation: PremiumCalculationResponse) => void;
  setLoading: (loading: boolean) => void;
  setError: (error: string | null) => void;
  resetApplication: () => void;
  completeApplication: () => void;
}

const initialApplication: Partial<InsuranceApplication> = {
  status: 'DRAFT',
  applicationDate: new Date().toISOString(),
};

export const useInsuranceStore = create<InsuranceStore>()(
  persist(
    (set, get) => ({
      currentStep: 0,
      selectedProduct: null,
      application: initialApplication,
      premiumCalculation: null,
      isLoading: false,
      error: null,

      setCurrentStep: (step: number) => set({ currentStep: step }),

      setSelectedProduct: (product: InsuranceProduct) =>
        set({
          selectedProduct: product,
          application: { ...get().application, productId: product.id }
        }),

      setPersonalInfo: (personalInfo: PersonalInfo) =>
        set({
          application: {
            ...get().application,
            applicantInfo: personalInfo
          }
        }),

      setInsuranceDetails: (details: InsuranceDetails) =>
        set({
          application: {
            ...get().application,
            insuranceDetails: details
          }
        }),

      setPaymentInfo: (paymentInfo: PaymentInfo) =>
        set({
          application: {
            ...get().application,
            paymentInfo
          }
        }),

      setAgreementInfo: (agreementInfo: AgreementInfo) =>
        set({
          application: {
            ...get().application,
            agreementInfo
          }
        }),

      setPremiumCalculation: (calculation: PremiumCalculationResponse) =>
        set({ premiumCalculation: calculation }),

      setLoading: (loading: boolean) => set({ isLoading: loading }),

      setError: (error: string | null) => set({ error }),

      resetApplication: () => set({
        currentStep: 0,
        selectedProduct: null,
        application: initialApplication,
        premiumCalculation: null,
        error: null
      }),

      completeApplication: () => set({
        currentStep: 6,
        application: {
          ...get().application,
          status: 'COMPLETED'
        }
      })
    }),
    {
      name: 'insurance-application-storage',
      partialize: (state) => ({
        currentStep: state.currentStep,
        selectedProduct: state.selectedProduct,
        application: state.application,
        premiumCalculation: state.premiumCalculation
      })
    }
  )
);