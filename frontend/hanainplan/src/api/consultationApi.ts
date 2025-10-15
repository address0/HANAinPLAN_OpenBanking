import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

export interface ConsultationRequest {
  customerId: number;
  consultantId: number;
  consultationType: string;
  reservationDatetime: string;
  detail?: string;
}

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

export const createConsultation = async (request: ConsultationRequest): Promise<ConsultationResponse> => {
  try {
    const response = await axios.post(`${API_BASE_URL}/api/consultations`, request);

    if (response.data.success) {
      return response.data.consultation;
    } else {
      throw new Error(response.data.message || '상담 신청에 실패했습니다.');
    }
  } catch (error: any) {
    throw new Error(error.response?.data?.message || error.message || '상담 신청에 실패했습니다.');
  }
};

export const getCustomerConsultations = async (customerId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/customer/${customerId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getConsultantConsultations = async (consultantId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/consultant/${consultantId}`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getTodayConsultations = async (consultantId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/consultant/${consultantId}/today`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const getConsultationRequests = async (consultantId: number): Promise<ConsultationResponse[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/consultant/${consultantId}/requests`);
    return response.data;
  } catch (error) {
    throw error;
  }
};

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
    throw new Error(error.response?.data?.message || error.message || '상담 상태 변경에 실패했습니다.');
  }
};

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
    throw new Error(error.response?.data?.message || error.message || '상담 취소에 실패했습니다.');
  }
};

export const getConsultationDetails = async (consultId: string): Promise<ConsultationResponse> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultations/${consultId}/details`);
    return response.data;
  } catch (error: any) {
    throw new Error(error.response?.data?.message || error.message || '상담 상세 정보 조회에 실패했습니다.');
  }
};

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
    throw new Error(error.response?.data?.message || error.message || '상담 방 입장에 실패했습니다.');
  }
};