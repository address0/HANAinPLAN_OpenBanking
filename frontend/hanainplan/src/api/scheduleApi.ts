import axios from 'axios';

// 일정 타입 정의
export interface ScheduleEvent {
  id: string;
  title: string;
  start: string;
  end: string;
  description?: string;
  clientName?: string;
  type: 'consultation' | 'meeting' | 'other';
}

// 기본 API URL (환경변수로 관리 권장)
const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * 상담사 ID 가져오기 (로컬 스토리지에서)
 */
const getConsultantId = (): number => {
  const userStorage = localStorage.getItem('user-storage');
  if (!userStorage) {
    throw new Error('로그인이 필요합니다.');
  }
  
  try {
    const userData = JSON.parse(userStorage);
    const userId = userData?.state?.user?.userId;
    
    if (!userId) {
      throw new Error('사용자 정보를 찾을 수 없습니다.');
    }
    
    return userId;
  } catch (error) {
    console.error('사용자 정보 파싱 오류:', error);
    throw new Error('사용자 정보를 읽을 수 없습니다.');
  }
};

/**
 * 일정 목록 조회
 */
export const fetchSchedules = async (): Promise<ScheduleEvent[]> => {
  try {
    const consultantId = getConsultantId();
    
    const response = await axios.get(`${API_BASE_URL}/api/consultant/schedules`, {
      params: { consultantId }
    });
    
    // 백엔드 응답을 프론트엔드 형식으로 변환
    return response.data.map((schedule: any) => ({
      id: schedule.id || String(schedule.scheduleId),
      title: schedule.title,
      start: schedule.start || schedule.startTime,
      end: schedule.end || schedule.endTime,
      description: schedule.description,
      clientName: schedule.clientName,
      type: (schedule.type || schedule.scheduleType || 'other').toLowerCase()
    }));
  } catch (error) {
    console.error('일정 목록 조회 실패:', error);
    throw error;
  }
};

/**
 * 일정 생성
 */
export const createSchedule = async (event: Omit<ScheduleEvent, 'id'>): Promise<ScheduleEvent> => {
  try {
    const consultantId = getConsultantId();
    
    // 프론트엔드 형식을 백엔드 형식으로 변환
    const requestData = {
      title: event.title,
      description: event.description,
      scheduleType: event.type.toUpperCase(),
      clientName: event.clientName,
      startTime: event.start,
      endTime: event.end,
      isAllDay: false
    };
    
    const response = await axios.post(
      `${API_BASE_URL}/api/consultant/schedules`,
      requestData,
      {
        params: { consultantId },
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    
    // 백엔드 응답을 프론트엔드 형식으로 변환
    const schedule = response.data;
    return {
      id: schedule.id || String(schedule.scheduleId),
      title: schedule.title,
      start: schedule.start || schedule.startTime,
      end: schedule.end || schedule.endTime,
      description: schedule.description,
      clientName: schedule.clientName,
      type: (schedule.type || schedule.scheduleType || 'other').toLowerCase() as 'consultation' | 'meeting' | 'other'
    };
  } catch (error) {
    console.error('일정 생성 실패:', error);
    throw error;
  }
};

/**
 * 일정 수정
 */
export const updateSchedule = async (eventId: string, event: Partial<ScheduleEvent>): Promise<ScheduleEvent> => {
  try {
    const consultantId = getConsultantId();
    
    // 프론트엔드 형식을 백엔드 형식으로 변환
    const requestData: any = {};
    if (event.title) requestData.title = event.title;
    if (event.description) requestData.description = event.description;
    if (event.type) requestData.scheduleType = event.type.toUpperCase();
    if (event.clientName) requestData.clientName = event.clientName;
    if (event.start) requestData.startTime = event.start;
    if (event.end) requestData.endTime = event.end;
    
    const response = await axios.put(
      `${API_BASE_URL}/api/consultant/schedules/${eventId}`,
      requestData,
      {
        params: { consultantId },
        headers: {
          'Content-Type': 'application/json'
        }
      }
    );
    
    // 백엔드 응답을 프론트엔드 형식으로 변환
    const schedule = response.data;
    return {
      id: schedule.id || String(schedule.scheduleId),
      title: schedule.title,
      start: schedule.start || schedule.startTime,
      end: schedule.end || schedule.endTime,
      description: schedule.description,
      clientName: schedule.clientName,
      type: (schedule.type || schedule.scheduleType || 'other').toLowerCase() as 'consultation' | 'meeting' | 'other'
    };
  } catch (error) {
    console.error('일정 수정 실패:', error);
    throw error;
  }
};

/**
 * 일정 삭제
 */
export const deleteSchedule = async (eventId: string): Promise<void> => {
  try {
    const consultantId = getConsultantId();
    
    await axios.delete(`${API_BASE_URL}/api/consultant/schedules/${eventId}`, {
      params: { consultantId }
    });
  } catch (error) {
    console.error('일정 삭제 실패:', error);
    throw error;
  }
};

/**
 * 특정 시간대에 가능한 상담사 목록 조회
 */
export const fetchAvailableConsultantsAtTime = async (
  startTime: string,
  endTime: string
): Promise<number[]> => {
  try {
    const response = await axios.get(`${API_BASE_URL}/api/consultants/available-at-time`, {
      params: {
        startTime,
        endTime
      }
    });
    
    // 상담사 ID 배열 반환 (ConsultantDto의 consultantId 필드)
    return response.data.map((consultant: any) => consultant.consultantId);
  } catch (error) {
    console.error('가능한 상담사 조회 실패:', error);
    throw error;
  }
};



