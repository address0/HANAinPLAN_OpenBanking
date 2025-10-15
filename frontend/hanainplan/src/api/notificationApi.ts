import { axiosInstance } from '../lib/axiosInstance';

const getCurrentUserId = (): number | null => {
  try {
    const userStorage = localStorage.getItem('user-storage');
    return userStorage ? JSON.parse(userStorage)?.state?.user?.userId : null;
  } catch (error) {
    return null;
  }
};

export type NotificationType = 'CONSULTATION' | 'TRANSACTION' | 'SYSTEM' | 'SCHEDULE' | 'OTHER';

export interface Notification {
  id: number;
  title: string;
  content: string;
  type: NotificationType;
  isRead: boolean;
  createdAt: string;
  readAt?: string;
}

export interface NotificationSummary {
  totalCount: number;
  unreadCount: number;
}

export const fetchNotifications = async (page: number = 0, size: number = 20): Promise<{ content: Notification[]; totalElements: number; totalPages: number }> => {
  try {
    const userId = getCurrentUserId();

    const params: any = { page, size };
    if (userId) params.userId = userId;

    const response = await axiosInstance.get(`/notifications`, {
      params
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const fetchUnreadNotifications = async (page: number = 0, size: number = 20, userId?: number): Promise<{ content: Notification[]; totalElements: number; totalPages: number }> => {
  try {
    const params: any = { page, size };
    if (userId) params.userId = userId;

    const response = await axiosInstance.get(`/notifications/unread`, {
      params
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

export const fetchNotificationSummary = async (userId?: number): Promise<NotificationSummary> => {
  try {
    const response = await axiosInstance.get(`/notifications/summary`, {
      params: userId ? { userId } : {}
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

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
    throw error;
  }
};

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
    throw error;
  }
};

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
    throw error;
  }
};

export const markNotificationAsRead = async (notificationId: number): Promise<Notification> => {
  try {
    const userId = getCurrentUserId();

    const params: any = {};
    if (userId) params.userId = userId;

    const response = await axiosInstance.patch(`/notifications/${notificationId}/read`, null, {
      params
    });
    return response.data;
  } catch (error) {
    throw error;
  }
};

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
    throw error;
  }
};

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
    throw error;
  }
};

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
    throw error;
  }
};

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
    throw error;
  }
};

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
    throw error;
  }
};