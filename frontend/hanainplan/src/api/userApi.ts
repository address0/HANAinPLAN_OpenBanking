import { httpGet, httpPost } from '../lib/http';

export interface IndustryData {
  industryCode: string;
  industryName: string;
  riskLevel: string;
  averageIncomeLevel?: string;
}

export const getIndustries = async (): Promise<IndustryData[]> => {
  return await httpGet<IndustryData[]>('industries');
};

export const searchIndustries = async (keyword: string): Promise<IndustryData[]> => {
  return await httpGet<IndustryData[]>('industries/search', { keyword });
};

export interface DiseaseCodeData {
  diseaseCode: string;
  diseaseName: string;
  diseaseCategory: string;
  riskLevel: string;
  isInsurable: string;
  description?: string;
  minAge?: number;
  maxAge?: number;
  waitingPeriodDays?: number;
  renewalCycleYears?: number;
  applicableInsCount?: number;
  applicableInsType?: string;
  insuranceCompCount?: number;
  remark?: string;
}

export const getDiseaseCodes = async (): Promise<DiseaseCodeData[]> => {
  return await httpGet<DiseaseCodeData[]>('diseases');
};

export const searchDiseaseCodes = async (keyword: string): Promise<DiseaseCodeData[]> => {
  return await httpGet<DiseaseCodeData[]>('diseases/search', { keyword });
};

export interface SignUpRequest {
  userType: 'GENERAL' | 'COUNSELOR';
  name: string;
  socialNumber: string;
  phoneNumber: string;
  verificationCode: string;
  ci: string;
  password?: string;
  confirmPassword?: string;
  kakaoId?: string;
  email?: string;
  healthInfo?: {
    recentMedicalAdvice: boolean;
    recentHospitalization: boolean;
    majorDisease: boolean;
    diseaseDetails?: Array<{
      diseaseCode: string;
      diseaseName: string;
      diseaseCategory: string;
      riskLevel: string;
      severity: string;
      progressPeriod: string;
      isChronic: boolean;
      description?: string;
    }>;
    longTermMedication: boolean;
    disabilityRegistered: boolean;
    insuranceRejection: boolean;
  };
  jobInfo?: {
    industryCode: string;
    industryName: string;
    careerYears: number;
    assetLevel: string;
  };
  counselorInfo?: {
    employeeId: string;
    specialty: string;
    position: string;
    workPhoneNumber: string;
    workEmail: string;
    branchCode: string;
    branchName: string;
    branchAddress: string;
    branchLatitude: number;
    branchLongitude: number;
    additionalNotes?: string;
  };
}

export interface SignUpResponse {
  userId?: number;
  userType?: string;
  name?: string;
  phoneNumber?: string;
  isPhoneVerified?: boolean;
  createdDate?: string;
  message: string;
}

export const signUp = async (request: SignUpRequest): Promise<SignUpResponse> => {
  return await httpPost<SignUpResponse>('auth/signup', request);
};

export const checkSocialNumber = async (socialNumber: string): Promise<{ isDuplicate: boolean; message: string }> => {
  return await httpGet<{ isDuplicate: boolean; message: string }>('auth/check-social-number', { socialNumber });
};

export const checkPhoneNumber = async (phoneNumber: string): Promise<{ isDuplicate: boolean; message: string }> => {
  return await httpGet<{ isDuplicate: boolean; message: string }>('auth/check-phone-number', { phoneNumber });
};

export const sendVerificationCode = async (phoneNumber: string): Promise<{
  success: boolean;
  message: string;
  verificationCode?: string;
  expiresInMinutes?: number
}> => {
  return await httpPost<{ success: boolean; message: string; verificationCode?: string; expiresInMinutes?: number }>('auth/send-verification-code', { phoneNumber });
};

export const verifyCode = async (phoneNumber: string, verificationCode: string): Promise<{
  success: boolean;
  message: string
}> => {
  return await httpPost<{ success: boolean; message: string }>('auth/verify-code', { phoneNumber, verificationCode });
};

export interface CiVerificationRequest {
  name: string;
  residentNumber: string;
}

export interface CiVerificationResponse {
  success: boolean;
  message: string;
  ci?: string;
  errorCode?: string;
}

export const verifyCi = async (request: CiVerificationRequest): Promise<CiVerificationResponse> => {
  return await httpPost<CiVerificationResponse>('auth/ci/verify', request);
};

export interface LoginRequest {
  phoneNumber: string;
  password: string;
  loginType?: string;
}

export interface LoginResponse {
  success: boolean;
  message: string;
  userId?: number;
  userName?: string;
  phoneNumber?: string;
  userType?: string;
  email?: string;
  loginType?: string;
}

export const login = async (loginData: LoginRequest): Promise<LoginResponse> => {
  return await httpPost<LoginResponse>('auth/login', {
    ...loginData,
    loginType: loginData.loginType || 'PHONE'
  });
};

export const kakaoLogin = async (kakaoId: string): Promise<LoginResponse> => {
  return await httpPost<LoginResponse>(`auth/kakao-login?kakaoId=${kakaoId}`, {});
};

export const checkUserExists = async (phoneNumber: string): Promise<boolean> => {
  return await httpGet<boolean>(`auth/check-user?phoneNumber=${phoneNumber}`);
};

export const logout = async (): Promise<LoginResponse> => {
  return await httpPost<LoginResponse>('auth/logout', {});
};

export interface UserInfoResponse {
  userBasicInfo: {
    userId: number;
    userName: string;
    phoneNumber: string;
    email: string;
    userType: string;
    gender: string;
    birthDate: string;
    loginType: string;
    isPhoneVerified: boolean;
    isActive: boolean;
    createdDate: string;
    lastLoginDate: string;
  };
  customerDetailInfo?: {
    customerId: number;
    healthInfo: {
      recentMedicalAdvice: boolean;
      recentHospitalization: boolean;
      majorDisease: boolean;
      diseaseDetails: Array<{
        diseaseCode: string;
        diseaseName: string;
        diseaseCategory: string;
        riskLevel: string;
        severity: string;
        progressPeriod: string;
        isChronic: boolean;
        description?: string;
      }>;
      longTermMedication: boolean;
      disabilityRegistered: boolean;
      insuranceRejection: boolean;
    };
    jobInfo: {
      industryCode: string;
      industryName: string;
      careerYears: number;
      assetLevel: string;
    };
  };
}

export interface UserInfoUpdateRequest {
  userBasicInfo?: {
    userName?: string;
    phoneNumber?: string;
    email?: string;
  };
  customerDetailInfo?: {
    healthInfo?: {
      recentMedicalAdvice?: boolean;
      recentHospitalization?: boolean;
      majorDisease?: boolean;
      diseaseDetails?: Array<{
        diseaseCode: string;
        diseaseName: string;
        diseaseCategory: string;
        riskLevel: string;
        severity: string;
        progressPeriod: string;
        isChronic: boolean;
        description?: string;
      }>;
      longTermMedication?: boolean;
      disabilityRegistered?: boolean;
      insuranceRejection?: boolean;
    };
    jobInfo?: {
      industryCode?: string;
      industryName?: string;
      careerYears?: number;
      assetLevel?: string;
    };
  };
}

export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

export const getUserInfo = async (userId: number): Promise<UserInfoResponse> => {
  return await httpGet<UserInfoResponse>(`user/info/${userId}`);
};

export const updateUserInfo = async (userId: number, updateData: UserInfoUpdateRequest): Promise<UserInfoResponse> => {
  const response = await fetch(`/api/user/info/${userId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(updateData),
  });
  return await response.json();
};

export const changePassword = async (userId: number, passwordData: PasswordChangeRequest): Promise<{success: boolean; message: string}> => {
  const response = await fetch(`/api/user/password/${userId}`, {
    method: 'PUT',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(passwordData),
  });
  return await response.json();
};

export const deleteAccount = async (userId: number, password: string): Promise<{success: boolean; message: string}> => {
  const response = await fetch(`/api/user/account/${userId}?password=${encodeURIComponent(password)}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return await response.json();
};

export interface AccountInfo {
  accountNumber: string;
  accountType: number;
  balance: number;
  openingDate: string;
  createdAt: string;
  updatedAt: string;
}

export interface BankAccountInfo {
  bankName: string;
  bankCode: string;
  customerName: string;
  customerCi: string;
  accounts: AccountInfo[];
  isCustomer: boolean;
}

export interface MyDataConsentRequest {
  phoneNumber: string;
  socialNumber: string;
  name: string;
  consentToMyDataCollection: boolean;
}

export interface MyDataConsentResponse {
  message: string;
  bankAccountInfo: BankAccountInfo[];
  totalBanks: number;
  totalAccounts: number;
}

export const processMyDataConsent = async (request: MyDataConsentRequest): Promise<MyDataConsentResponse> => {
  return await httpPost<MyDataConsentResponse>('user/mydata/consent', request);
};