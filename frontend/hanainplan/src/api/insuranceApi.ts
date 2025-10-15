import axios from 'axios';
import type {
  InsuranceProduct,
  InsuranceApplication,
  PremiumCalculationRequest,
  PremiumCalculationResponse,
  PersonalInfo,
  InsuranceDetails,
  PaymentInfo
} from '../types/insurance';

const API_BASE_URL = 'http://localhost:8080/api/insurance';

export const getInsuranceProducts = async (category?: string): Promise<InsuranceProduct[]> => {
  const response = await axios.get(`${API_BASE_URL}/products`, {
    params: category ? { category } : {}
  });
  return response.data;
};

export const getInsuranceProduct = async (productId: string): Promise<InsuranceProduct> => {
  const response = await axios.get(`${API_BASE_URL}/products/${productId}`);
  return response.data;
};

export const calculatePremium = async (request: PremiumCalculationRequest): Promise<PremiumCalculationResponse> => {
  const response = await axios.post(`${API_BASE_URL}/premium/calculate`, request);
  return response.data;
};

export const submitInsuranceApplication = async (application: InsuranceApplication): Promise<{ success: boolean; applicationId: string; policyNumber?: string }> => {
  const response = await axios.post(`${API_BASE_URL}/applications`, application);
  return response.data;
};

export const getInsuranceApplication = async (applicationId: string): Promise<InsuranceApplication> => {
  const response = await axios.get(`${API_BASE_URL}/applications/${applicationId}`);
  return response.data;
};

export const validatePersonalInfo = async (personalInfo: PersonalInfo): Promise<{ valid: boolean; errors: string[] }> => {
  const response = await axios.post(`${API_BASE_URL}/validate/personal-info`, personalInfo);
  return response.data;
};

export const checkResidentNumberDuplicate = async (residentNumber: string): Promise<{ duplicate: boolean }> => {
  const response = await axios.post(`${API_BASE_URL}/validate/resident-number`, { residentNumber });
  return response.data;
};

export const validateBankAccount = async (bankAccount: { bankCode: string; accountNumber: string; accountHolder: string }): Promise<{ valid: boolean; message: string }> => {
  const response = await axios.post(`${API_BASE_URL}/validate/bank-account`, bankAccount);
  return response.data;
};

export const updateApplicationStatus = async (applicationId: string, status: string): Promise<{ success: boolean }> => {
  const response = await axios.patch(`${API_BASE_URL}/applications/${applicationId}/status`, { status });
  return response.data;
};

export const getInsuranceHistory = async (userId: string): Promise<InsuranceApplication[]> => {
  const response = await axios.get(`${API_BASE_URL}/applications/user/${userId}`);
  return response.data;
};