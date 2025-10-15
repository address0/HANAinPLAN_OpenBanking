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

export const saveConsultationNote = async (request: SaveConsultationNoteRequest): Promise<ConsultationNote> => {
  const response = await axiosInstance.post('/consultation/notes', request);
  return response.data;
};

export const getConsultationNotes = async (consultId: string): Promise<ConsultationNote[]> => {
  const response = await axiosInstance.get(`/consultation/${consultId}/notes`);
  return response.data;
};

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

export const getSharedNotes = async (consultId: string): Promise<ConsultationNote[]> => {
  const response = await axiosInstance.get(`/consultation/${consultId}/notes/shared`);
  return response.data;
};

export const getSharedNote = async (consultId: string): Promise<ConsultationNote | null> => {
  try {
    const sharedNotes = await getSharedNotes(consultId);
    return sharedNotes && sharedNotes.length > 0 ? sharedNotes[0] : null;
  } catch (error) {
    return null;
  }
};