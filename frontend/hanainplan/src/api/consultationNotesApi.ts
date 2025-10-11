import { axiosInstance } from '../lib/axiosInstance';

export interface ConsultationNote {
  noteId?: number;
  consultId: string;
  userId: number;
  noteType: 'PERSONAL' | 'SHARED';
  content: string;
  createdDate?: string;
  updatedDate?: string;
}

export interface SaveConsultationNoteRequest {
  consultId: string;
  userId: number;
  noteType: 'PERSONAL' | 'SHARED';
  content: string;
}

// 상담 메모 저장/업데이트
export const saveConsultationNote = async (request: SaveConsultationNoteRequest): Promise<ConsultationNote> => {
  const response = await axiosInstance.post('/consultation/notes', request);
  return response.data;
};

// 특정 상담의 모든 메모 조회
export const getConsultationNotes = async (consultId: string): Promise<ConsultationNote[]> => {
  const response = await axiosInstance.get(`/consultation/${consultId}/notes`);
  return response.data;
};

// 특정 상담의 특정 사용자 메모 조회
export const getUserNote = async (consultId: string, userId: number, noteType: 'PERSONAL' | 'SHARED'): Promise<ConsultationNote | null> => {
  try {
    const response = await axiosInstance.get(`/consultation/${consultId}/notes/user/${userId}/type/${noteType}`);
    return response.data;
  } catch (error: any) {
    if (error.response?.status === 404) {
      return null;
    }
    throw error;
  }
};

// 특정 상담의 공유 메모 조회
export const getSharedNotes = async (consultId: string): Promise<ConsultationNote[]> => {
  const response = await axiosInstance.get(`/consultation/${consultId}/notes/shared`);
  return response.data;
};

// 특정 상담의 공유 메모 조회 (고객용 - 첫 번째 공유 메모만 반환)
export const getSharedNote = async (consultId: string): Promise<ConsultationNote | null> => {
  try {
    const sharedNotes = await getSharedNotes(consultId);
    return sharedNotes && sharedNotes.length > 0 ? sharedNotes[0] : null;
  } catch (error) {
    console.error('공유 메모 조회 실패:', error);
    return null;
  }
};
