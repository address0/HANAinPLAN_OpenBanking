import { httpGet, httpPost } from '../lib/http';

// 산업 데이터 타입 정의
export interface IndustryData {
  industryCode: string;
  industryName: string;
  riskLevel: string;
  averageIncomeLevel?: string;
}

// 모든 산업 데이터 조회
export const getIndustries = async (): Promise<IndustryData[]> => {
  return await httpGet<IndustryData[]>('industries');
};

// 키워드로 산업 검색
export const searchIndustries = async (keyword: string): Promise<IndustryData[]> => {
  return await httpGet<IndustryData[]>('industries/search', { keyword });
};

// 질병 코드 데이터 타입 정의
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

// 모든 질병 코드 조회
export const getDiseaseCodes = async (): Promise<DiseaseCodeData[]> => {
  return await httpGet<DiseaseCodeData[]>('diseases');
};

// 키워드로 질병 검색
export const searchDiseaseCodes = async (keyword: string): Promise<DiseaseCodeData[]> => {
  return await httpGet<DiseaseCodeData[]>('diseases/search', { keyword });
};

// ================================
// 회원가입 관련 API
// ================================

// 회원가입 요청 데이터 타입
export interface SignUpRequest {
  userType: 'GENERAL' | 'COUNSELOR';
  name: string;
  socialNumber: string;
  phoneNumber: string;
  verificationCode: string;
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
}

// 회원가입 응답 데이터 타입
export interface SignUpResponse {
  userId?: number;
  userType?: string;
  name?: string;
  phoneNumber?: string;
  isPhoneVerified?: boolean;
  createdDate?: string;
  message: string;
}

// 회원가입
export const signUp = async (request: SignUpRequest): Promise<SignUpResponse> => {
  return await httpPost<SignUpResponse>('auth/signup', request);
};

// 주민번호 중복 확인
export const checkSocialNumber = async (socialNumber: string): Promise<{ isDuplicate: boolean; message: string }> => {
  return await httpGet<{ isDuplicate: boolean; message: string }>('auth/check-social-number', { socialNumber });
};

// 전화번호 중복 확인
export const checkPhoneNumber = async (phoneNumber: string): Promise<{ isDuplicate: boolean; message: string }> => {
  return await httpGet<{ isDuplicate: boolean; message: string }>('auth/check-phone-number', { phoneNumber });
};

// 인증번호 전송
export const sendVerificationCode = async (phoneNumber: string): Promise<{ 
  success: boolean; 
  message: string; 
  verificationCode?: string; 
  expiresInMinutes?: number 
}> => {
  return await httpPost<{ success: boolean; message: string; verificationCode?: string; expiresInMinutes?: number }>('auth/send-verification-code', { phoneNumber });
};

// 인증번호 확인
export const verifyCode = async (phoneNumber: string, verificationCode: string): Promise<{ 
  success: boolean; 
  message: string 
}> => {
  return await httpPost<{ success: boolean; message: string }>('auth/verify-code', { phoneNumber, verificationCode });
};

// ================================
// 로그인 관련 API
// ================================

// 로그인 요청 데이터 타입
export interface LoginRequest {
  phoneNumber: string;
  password: string;
  loginType?: string;
}

// 로그인 응답 데이터 타입
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

// 로그인
export const login = async (loginData: LoginRequest): Promise<LoginResponse> => {
  return await httpPost<LoginResponse>('auth/login', {
    ...loginData,
    loginType: loginData.loginType || 'PHONE'
  });
};

// 카카오 로그인
export const kakaoLogin = async (kakaoId: string): Promise<LoginResponse> => {
  return await httpPost<LoginResponse>(`auth/kakao-login?kakaoId=${kakaoId}`, {});
};

// 사용자 존재 여부 확인 (전화번호 기준)
export const checkUserExists = async (phoneNumber: string): Promise<boolean> => {
  return await httpGet<boolean>(`auth/check-user?phoneNumber=${phoneNumber}`);
};

// 로그아웃
export const logout = async (): Promise<LoginResponse> => {
  return await httpPost<LoginResponse>('auth/logout', {});
};

// ================================
// 사용자 정보 관리 API
// ================================

// 사용자 정보 응답 타입
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

// 사용자 정보 업데이트 요청 타입
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

// 비밀번호 변경 요청 타입
export interface PasswordChangeRequest {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// 사용자 정보 조회
export const getUserInfo = async (userId: number): Promise<UserInfoResponse> => {
  return await httpGet<UserInfoResponse>(`user/info/${userId}`);
};

// 사용자 정보 수정
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

// 비밀번호 변경
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

// 계정 탈퇴
export const deleteAccount = async (userId: number, password: string): Promise<{success: boolean; message: string}> => {
  const response = await fetch(`/api/user/account/${userId}?password=${encodeURIComponent(password)}`, {
    method: 'DELETE',
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return await response.json();
};

// ================================
// 마이데이터 수집 동의 관련 API
// ================================

// 계좌 정보 타입
export interface AccountInfo {
  accountNumber: string;
  accountType: number;
  balance: number;
  openingDate: string;
  createdAt: string;
  updatedAt: string;
}

// 은행 계좌 정보 타입
export interface BankAccountInfo {
  bankName: string;
  bankCode: string;
  customerName: string;
  customerCi: string;
  accounts: AccountInfo[];
  isCustomer: boolean;
}

// 마이데이터 수집 동의 요청 타입
export interface MyDataConsentRequest {
  phoneNumber: string;
  socialNumber: string;
  name: string;
  consentToMyDataCollection: boolean;
}

// 마이데이터 수집 동의 응답 타입
export interface MyDataConsentResponse {
  message: string;
  bankAccountInfo: BankAccountInfo[];
  totalBanks: number;
  totalAccounts: number;
}

// 마이데이터 수집 동의
export const processMyDataConsent = async (request: MyDataConsentRequest): Promise<MyDataConsentResponse> => {
  return await httpPost<MyDataConsentResponse>('user/mydata/consent', request);
};
