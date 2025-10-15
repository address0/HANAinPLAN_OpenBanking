import { axiosInstance } from '../lib/axiosInstance';

export interface PortfolioData {
  totalAssets?: number;
  deposits?: any[];
  funds?: any[];
  irp?: number;
  insurance?: number;
}

export interface IrpAccountInfo {
  irpAccountId: number;
  customerId: number;
  customerCi: string;
  bankCode: string;
  accountNumber: string;
  accountStatus: string;
  currentBalance: number;
  totalContribution: number;
  totalReturn: number;
  returnRate: number;
  productName: string;
  bankName: string;
  displayAccountNumber: string;
  openDate: string;
  createdDate: string;
  investmentStyle?: string;
  investmentStyleDisplay?: string;
}

export const getPortfolio = async (customerId: number): Promise<PortfolioData> => {
  const response = await axiosInstance.get(`/api/portfolio/${customerId}`);
  return response.data;
};

export const getIrpAccount = async (userId: number): Promise<IrpAccountInfo | null> => {
  try {
    const response = await axiosInstance.get(`/banking/irp/account/user/${userId}`);

    if (response.data) {
      return response.data;
    }

    return null;
  } catch (error) {
    return null;
  }
};