import { axiosInstance } from '../lib/axiosInstance';

// 로그인한 사용자 정보 가져오기 헬퍼 함수
const getCurrentUserId = (): number | null => {
  try {
    const userStorage = localStorage.getItem('user-storage');
    return userStorage ? JSON.parse(userStorage)?.state?.user?.userId : null;
  } catch (error) {
    console.error('사용자 정보 파싱 실패:', error);
    return null;
  }
};

// 알림 타입 정의
export type NotificationType = 'CONSULTATION' | 'TRANSACTION' | 'SYSTEM' | 'SCHEDULE' | 'OTHER';

// 알림 인터페이스
export interface Notification {
  id: number;
  title: string;
  content: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

// 알림 개수 요약 인터페이스
export interface NotificationSummary {
  totalCount: number;
  unreadCount: number;
}

// 알림 목록 조회 (페이징)
export const fetchNotifications = async (page: number = 0, size: number = 20): Promise<{ content: Notification[]; totalElements: number; totalPages: number }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = { page, size };
    if (userId) params.userId = userId;

    console.log(`알림 목록 조회 요청: /notifications?page=${page}&size=${size}${userId ? `&userId=${userId}` : ''}`);
    const response = await axiosInstance.get(`/notifications`, {
      params
    });
    console.log('알림 목록 조회 응답:', response.data);
    return response.data;
  } catch (error) {
    console.error('알림 목록 조회 실패:', error);
    throw error;
  }
};

// 읽지 않은 알림 목록 조회 (페이징)
export const fetchUnreadNotifications = async (page: number = 0, size: number = 20, userId?: number): Promise<{ content: Notification[]; totalElements: number; totalPages: number }> => {
  try {
    const params: any = { page, size };
    if (userId) params.userId = userId;

    console.log(`읽지 않은 알림 목록 조회 요청: /notifications/unread?page=${page}&size=${size}${userId ? `&userId=${userId}` : ''}`);
    const response = await axiosInstance.get(`/notifications/unread`, {
      params
    });
    console.log('읽지 않은 알림 목록 조회 응답:', response.data);
    return response.data;
  } catch (error) {
    console.error('읽지 않은 알림 목록 조회 실패:', error);
    throw error;
  }
};

// 알림 개수 요약 조회
export const fetchNotificationSummary = async (userId?: number): Promise<NotificationSummary> => {
  try {
    console.log(`알림 개수 요약 조회 요청: /notifications/summary${userId ? `?userId=${userId}` : ''}`);
    const response = await axiosInstance.get(`/notifications/summary`, {
      params: userId ? { userId } : {}
    });
    console.log('알림 개수 요약 조회 응답:', response.data);
    return response.data;
  } catch (error) {
    console.error('알림 개수 요약 조회 실패:', error);
    throw error;
  }
};

// 특정 타입 알림 목록 조회 (페이징)
export const fetchNotificationsByType = async (type: NotificationType, page: number = 0, size: number = 20): Promise<{ content: Notification[]; totalElements: number; totalPages: number }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = { page, size };
    if (userId) params.userId = userId;

    const response = await axiosInstance.get(`/notifications/type/${type}`, {
      params
    });
    return response.data;
  } catch (error) {
    console.error(`${type} 알림 목록 조회 실패:`, error);
    throw error;
  }
};

// 기간별 알림 목록 조회 (페이징)
export const fetchNotificationsByPeriod = async (
  startDate: string,
  endDate: string,
  page: number = 0,
  size: number = 20
): Promise<{ content: Notification[]; totalElements: number; totalPages: number }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = { startDate, endDate, page, size };
    if (userId) params.userId = userId;

    const response = await axiosInstance.get(`/notifications/period`, {
      params
    });
    return response.data;
  } catch (error) {
    console.error('기간별 알림 목록 조회 실패:', error);
    throw error;
  }
};

// 단일 알림 조회
export const fetchNotification = async (notificationId: number): Promise<Notification> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    const response = await axiosInstance.get(`/notifications/${notificationId}`, {
      params
    });
    return response.data;
  } catch (error) {
    console.error('알림 조회 실패:', error);
    throw error;
  }
};

// 알림 읽음 처리
export const markNotificationAsRead = async (notificationId: number): Promise<Notification> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    console.log(`알림 읽음 처리 요청: /notifications/${notificationId}/read${userId ? `?userId=${userId}` : ''}`);
    const response = await axiosInstance.patch(`/notifications/${notificationId}/read`, null, {
      params
    });
    console.log('알림 읽음 처리 응답:', response.data);
    return response.data;
  } catch (error) {
    console.error('알림 읽음 처리 실패:', error);
    console.error('요청 URL:', axiosInstance.defaults.baseURL + `notifications/${notificationId}/read`);
    throw error;
  }
};

// 모든 알림 읽음 처리
export const markAllNotificationsAsRead = async (): Promise<{ success: boolean; message: string }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    const response = await axiosInstance.patch(`/notifications/read-all`, null, {
      params
    });
    return response.data;
  } catch (error) {
    console.error('모든 알림 읽음 처리 실패:', error);
    throw error;
  }
};

// 특정 타입 모든 알림 읽음 처리
export const markAllNotificationsAsReadByType = async (type: NotificationType): Promise<{ success: boolean; message: string }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    const response = await axiosInstance.patch(`/notifications/type/${type}/read-all`, null, {
      params
    });
    return response.data;
  } catch (error) {
    console.error(`${type} 모든 알림 읽음 처리 실패:`, error);
    throw error;
  }
};

// 알림 삭제
export const deleteNotification = async (notificationId: number): Promise<{ success: boolean; message: string }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    const response = await axiosInstance.delete(`/notifications/${notificationId}`, {
      params
    });
    return response.data;
  } catch (error) {
    console.error('알림 삭제 실패:', error);
    throw error;
  }
};

// 여러 알림 일괄 삭제
export const deleteNotifications = async (notificationIds: number[]): Promise<{ success: boolean; message: string }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = { ids: notificationIds };
    if (userId) params.userId = userId;

    const response = await axiosInstance.delete(`/notifications/batch`, {
      params,
      data: notificationIds
    });
    return response.data;
  } catch (error) {
    console.error('알림 일괄 삭제 실패:', error);
    throw error;
  }
};

// 오래된 알림 정리
export const cleanupOldNotifications = async (): Promise<{ success: boolean; message: string }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    const response = await axiosInstance.delete(`/notifications/cleanup`, {
      params
    });
    return response.data;
  } catch (error) {
    console.error('오래된 알림 정리 실패:', error);
    throw error;
  }
};
