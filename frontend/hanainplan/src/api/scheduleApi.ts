import axios from 'axios';

export interface ScheduleEvent {
  id: string;
  title: string;
  start: string;
  end: string;
  description?: string;
  clientName?: string;
  type: 'consultation' | 'meeting' | 'other';
}

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

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
    throw new Error('사용자 정보를 읽을 수 없습니다.');
  }
};

export const fetchSchedules = async (): Promise<ScheduleEvent[]> => {
  try {
    const consultantId = getConsultantId();

    const response = await axios.get(`${API_BASE_URL}/api/consultant/schedules`, {
      params: { consultantId }
    });

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
    throw error;
  }
};

export const createSchedule = async (event: Omit<ScheduleEvent, 'id'>): Promise<ScheduleEvent> => {
  try {
    const consultantId = getConsultantId();

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
    throw error;
  }
};

export const updateSchedule = async (eventId: string, event: Partial<ScheduleEvent>): Promise<ScheduleEvent> => {
  try {
    const consultantId = getConsultantId();

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
    throw error;
  }
};

export const deleteSchedule = async (eventId: string): Promise<void> => {
  try {
    const consultantId = getConsultantId();

    await axios.delete(`${API_BASE_URL}/api/consultant/schedules/${eventId}`, {
      params: { consultantId }
    });
  } catch (error) {
    throw error;
  }
};

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

    return response.data.map((consultant: any) => consultant.consultantId);
  } catch (error) {
    throw error;
  }
};