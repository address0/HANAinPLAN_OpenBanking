import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

// 상담 신청 요청 타입
export interface ConsultationRequest {
  customerId: number;
  consultantId: number;
  consultationType: string;
  reservationDatetime: string;
  detail?: string;
}

// 상담 응답 타입
export interface ConsultationResponse {
  consultId: string;
  consultType: string;
  reservationDatetime: string;
  consultDatetime?: string;
  consultStatus: string;
  qrCode?: string;
  consultUrl?: string;
  detail?: string;
  consultResult?: string;
  branchCode?: string;
  customerId: string;
  consultantId: string;
  customerName?: string;
  consultantName?: string;
  consultantDepartment?: string;
}

/**
 * 상담 신청
 */
export const createConsultation = async (request: ConsultationRequest): Promise<ConsultationResponse> => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/consultations`, request);
    
    if (response.data.success) {
      return response.data.consultation;
    } else {
      throw new Error(response.data.message || '상담 신청에 실패했습니다.');
    }
  } catch (error: any) {
    console.error('상담 신청 실패:', error);
    throw new Error(error.response?.data?.message || error.message || '상담 신청에 실패했습니다.');
  }
};

/**
 * 고객의 상담 목록 조회
 */
export const getCustomerConsultations = async (customerId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/customer/${customerId}`);
    return response.data;
  } catch (error) {
    console.error('고객 상담 목록 조회 실패:', error);
    throw error;
  }
};

/**
 * 상담사의 상담 목록 조회
 */
export const getConsultantConsultations = async (consultantId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/consultant/${consultantId}`);
    return response.data;
  } catch (error) {
    console.error('상담사 상담 목록 조회 실패:', error);
    throw error;
  }
};

/**
 * 상담사의 오늘 상담 조회
 */
export const getTodayConsultations = async (consultantId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/consultant/${consultantId}/today`);
    return response.data;
  } catch (error) {
    console.error('오늘 상담 조회 실패:', error);
    throw error;
  }
};

/**
 * 상담사의 요청 내역 조회
 */
export const getConsultationRequests = async (consultantId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/consultant/${consultantId}/requests`);
    return response.data;
  } catch (error) {
    console.error('상담 요청 내역 조회 실패:', error);
    throw error;
  }
};

/**
 * 상담 상태 변경
 */
export const updateConsultationStatus = async (consultId: string, status: string): Promise<ConsultationResponse> => {
  try {
    const response = await axios.patch(
      `${API_BASE_URL}/api/consultations/${consultId}/status`,
      null,
      { params: { status } }
    );
    
    if (response.data.success) {
      return response.data.consultation;
    } else {
      throw new Error(response.data.message || '상담 상태 변경에 실패했습니다.');
    }
  } catch (error: any) {
    console.error('상담 상태 변경 실패:', error);
    throw new Error(error.response?.data?.message || error.message || '상담 상태 변경에 실패했습니다.');
  }
};

/**
 * 상담 취소 (고객용)
 */
export const cancelConsultation = async (consultId: string, customerId: number): Promise<ConsultationResponse> => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/consultations/${consultId}/cancel`,
      null,
      { params: { customerId } }
    );
    
    if (response.data.success) {
      return response.data.consultation;
    } else {
      throw new Error(response.data.message || '상담 취소에 실패했습니다.');
    }
  } catch (error: any) {
    console.error('상담 취소 실패:', error);
    throw new Error(error.response?.data?.message || error.message || '상담 취소에 실패했습니다.');
  }
};

/**
 * 상담 상세 정보 조회 (화상 상담 입장용)
 */
export const getConsultationDetails = async (consultId: string): Promise<ConsultationResponse> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/${consultId}/details`);
    return response.data;
  } catch (error: any) {
    console.error('상담 상세 정보 조회 실패:', error);
    throw new Error(error.response?.data?.message || error.message || '상담 상세 정보 조회에 실패했습니다.');
  }
};

/**
 * 화상 상담 방 입장
 */
export const joinConsultationRoom = async (consultationId: string, userId: number): Promise<any> => {
  try {
    const response = await axios.post(
      `${API_BASE_URL}/api/webrtc/consultation/${consultationId}/join`,
      { userId }
    );
    
    if (response.data.success) {
      return response.data;
    } else {
      throw new Error(response.data.message || '상담 방 입장에 실패했습니다.');
    }
  } catch (error: any) {
    console.error('상담 방 입장 실패:', error);
    throw new Error(error.response?.data?.message || error.message || '상담 방 입장에 실패했습니다.');
  }
};
