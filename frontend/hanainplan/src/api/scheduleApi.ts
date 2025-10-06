// import axios from 'axios';

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
// const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

/**
 * 일정 목록 조회
 */
export const fetchSchedules = async (): Promise<ScheduleEvent[]> => {
  // 실제 API 호출 구현
  // try {
  //   const response = await axios.get<ScheduleEvent[]>(`${API_BASE_URL}/api/consultant/schedules`, {
  //     headers: {
  //       Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  //     }
  //   });
  //   return response.data;
  // } catch (error) {
  //   console.error('일정 목록 조회 실패:', error);
  //   throw error;
  // }

  // Mock 데이터 (개발용)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        {
          id: '1',
          title: '김철수 고객 상담',
          start: new Date().toISOString(),
          end: new Date(Date.now() + 3600000).toISOString(),
          description: 'IRP 상품 상담',
          clientName: '김철수',
          type: 'consultation'
        },
        {
          id: '2',
          title: '팀 회의',
          start: new Date(Date.now() + 86400000).toISOString(),
          end: new Date(Date.now() + 86400000 + 3600000).toISOString(),
          description: '월간 실적 회의',
          type: 'meeting'
        },
        {
          id: '3',
          title: '이영희 고객 상담',
          start: new Date(Date.now() + 172800000).toISOString(),
          end: new Date(Date.now() + 172800000 + 1800000).toISOString(),
          description: '펀드 상품 문의',
          clientName: '이영희',
          type: 'consultation'
        }
      ]);
    }, 500);
  });
};

/**
 * 일정 생성
 */
export const createSchedule = async (event: Omit<ScheduleEvent, 'id'>): Promise<ScheduleEvent> => {
  // 실제 API 호출 구현
  // try {
  //   const response = await axios.post<ScheduleEvent>(`${API_BASE_URL}/api/consultant/schedules`, event, {
  //     headers: {
  //       'Content-Type': 'application/json',
  //       Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  //     }
  //   });
  //   return response.data;
  // } catch (error) {
  //   console.error('일정 생성 실패:', error);
  //   throw error;
  // }

  // Mock 응답 (개발용)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        ...event,
        id: Math.random().toString(36).substr(2, 9)
      });
    }, 500);
  });
};

/**
 * 일정 수정
 */
export const updateSchedule = async (eventId: string, event: Partial<ScheduleEvent>): Promise<ScheduleEvent> => {
  // 실제 API 호출 구현
  // try {
  //   const response = await axios.put<ScheduleEvent>(`${API_BASE_URL}/api/consultant/schedules/${eventId}`, event, {
  //     headers: {
  //       'Content-Type': 'application/json',
  //       Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  //     }
  //   });
  //   return response.data;
  // } catch (error) {
  //   console.error('일정 수정 실패:', error);
  //   throw error;
  // }

  // Mock 응답 (개발용)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve({
        id: eventId,
        ...event
      } as ScheduleEvent);
    }, 500);
  });
};

/**
 * 일정 삭제
 */
export const deleteSchedule = async (eventId: string): Promise<void> => {
  // 실제 API 호출 구현
  // try {
  //   await axios.delete(`${API_BASE_URL}/api/consultant/schedules/${eventId}`, {
  //     headers: {
  //       Authorization: `Bearer ${localStorage.getItem('accessToken')}`
  //     }
  //   });
  // } catch (error) {
  //   console.error('일정 삭제 실패:', error);
  //   throw error;
  // }

  // Mock 응답 (개발용)
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve();
    }, 500);
  });
};


