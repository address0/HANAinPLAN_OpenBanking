import { axiosInstance } from '../lib/axiosInstance';

// IRP 계좌 개설 요청 인터페이스
export interface IrpAccountOpenRequest {
  customerId: number;
  initialDeposit: number;
  monthlyDeposit?: number;
  isAutoDeposit: boolean;
  depositDay: number;
  investmentStyle: 'CONSERVATIVE' | 'MODERATE_CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE';
  linkedMainAccount: string;
}

// IRP 계좌 개설 응답 인터페이스
export interface IrpAccountOpenResponse {
  accountNumber: string;
  accountStatus: string;
  initialDeposit: number;
  monthlyDeposit?: number;
  investmentStyle: string;
  openDate: string;
  createdAt: string;
  message: string;
  success: boolean;
}

// IRP 계좌 조회 응답 인터페이스
export interface IrpAccountInfo {
  irpAccountId: number;
  customerCi: string;
  accountNumber: string;
  accountStatus: string;
  initialDeposit: number;
  monthlyDeposit: number;
  currentBalance: number;
  investmentStyle: string;
  productCode?: string;
  openDate: string;
  lastContributionDate: string;
  totalContribution: number;
  totalReturn: number;
  returnRate: number;
  managementFee: number;
  isAutoDeposit: boolean;
  depositDay: number;
  linkedMainAccount: string;
  taxBenefitYear: number;
  maturityDate?: string;
  isMatured: boolean;
  penaltyAmount: number;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

// IRP 계좌 보유 여부 확인 응답 인터페이스
export interface IrpAccountStatusResponse {
  customerCi: string;
  hasIrpAccount: boolean;
  message: string;
}

/**
 * IRP 계좌 개설 API
 */
export const openIrpAccount = async (request: IrpAccountOpenRequest): Promise<IrpAccountOpenResponse> => {
  try {
    const response = await axiosInstance.post<IrpAccountOpenResponse>('/v1/irp-integration/accounts/open', request);
    return response.data;
  } catch (error) {
    console.error('IRP 계좌 개설 실패:', error);
    throw error;
  }
};

/**
 * IRP 계좌 정보 조회 API
 */
export const getIrpAccount = async (customerId: number): Promise<IrpAccountInfo> => {
  try {
    const response = await axiosInstance.get<IrpAccountInfo>(`/v1/irp-integration/accounts/customer/${customerId}`);
    return response.data;
  } catch (error) {
    console.error('IRP 계좌 조회 실패:', error);
    throw error;
  }
};

/**
 * IRP 계좌 보유 여부 확인 API
 */
export const checkIrpAccountStatus = async (customerId: number): Promise<IrpAccountStatusResponse> => {
  try {
    const response = await axiosInstance.get<IrpAccountStatusResponse>(`/v1/irp-integration/accounts/check/${customerId}`);
    return response.data;
  } catch (error) {
    console.error('IRP 계좌 보유 여부 확인 실패:', error);
    throw error;
  }
};

/**
 * HANAinPLAN과 IRP 계좌 동기화 API
 */
export const syncIrpAccountWithHanaInPlan = async (customerId: number): Promise<{ syncSuccess: boolean; message: string }> => {
  try {
    const response = await axiosInstance.post<{ syncSuccess: boolean; message: string }>(`/v1/irp-integration/sync/customer/${customerId}`);
    return response.data;
  } catch (error) {
    console.error('HANAinPLAN 동기화 실패:', error);
    throw error;
  }
};

/**
 * IRP 계좌 해지 API
 */
export const closeIrpAccount = async (accountNumber: string): Promise<{ accountNumber: string; message: string }> => {
  try {
    const response = await axiosInstance.delete<{ accountNumber: string; message: string }>(`/v1/irp/close/${accountNumber}`);
    return response.data;
  } catch (error) {
    console.error('IRP 계좌 해지 실패:', error);
    throw error;
  }
};

