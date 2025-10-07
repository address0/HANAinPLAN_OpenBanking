import { initializeApp } from 'firebase/app';
import { getMessaging, getToken, onMessage } from 'firebase/messaging';
import type { Messaging } from 'firebase/messaging';

/**
 * Firebase 설정
 * TODO: 실제 Firebase 프로젝트에서 받은 설정값으로 교체해야 합니다.
 * Firebase Console > 프로젝트 설정 > 일반 > 웹 앱에서 확인 가능
 */
const firebaseConfig = {
    apiKey: "AIzaSyDG40TTw7B4leIGdsJNhT2fDsa852ZHeiQ",
    authDomain: "hanainplan.firebaseapp.com",
    projectId: "hanainplan",
    storageBucket: "hanainplan.firebasestorage.app",
    messagingSenderId: "1027319351509",
    appId: "1:1027319351509:web:d681831a5259aa8ad4fa5c",
    measurementId: "G-8KHVVJLD3N"
};

// Firebase 앱 초기화
const app = initializeApp(firebaseConfig);

// Messaging 인스턴스
let messaging: Messaging | null = null;

try {
  messaging = getMessaging(app);
  console.log('Firebase Messaging initialized');
} catch (error) {
  console.error('Firebase Messaging initialization failed:', error);
  console.error('FCM notifications will not work. Please check your Firebase configuration.');
}

const vapidKey = import.meta.env.VITE_FCM_VAPID_KEY;

/**
 * FCM 토큰 요청
 */
export const requestFCMToken = async (): Promise<string | null> => {
  try {
    if (!messaging) {
      console.error('Firebase Messaging not initialized');
      return null;
    }

    // 알림 권한 요청
    const permission = await Notification.requestPermission();
    
    if (permission === 'granted') {
      console.log('Notification permission granted.');
      
      // FCM 토큰 가져오기
      const currentToken = await getToken(messaging, { vapidKey });
      
      if (currentToken) {
        console.log('FCM Token:', currentToken);
        return currentToken;
      } else {
        console.log('No registration token available. Request permission to generate one.');
        return null;
      }
    } else {
      console.log('Unable to get permission to notify.');
      return null;
    }
  } catch (error) {
    console.error('An error occurred while retrieving token:', error);
    return null;
  }
};

/**
 * 포그라운드 메시지 리스너 설정
 */
export const onMessageListener = (): Promise<any> => {
  return new Promise((resolve) => {
    if (!messaging) {
      console.error('Firebase Messaging not initialized');
      return;
    }

    onMessage(messaging, (payload) => {
      console.log('Message received in foreground:', payload);
      resolve(payload);
    });
  });
};

/**
 * FCM 토큰을 백엔드에 등록
 */
export const registerFCMToken = async (
  userId: number,
  deviceId?: string,
  deviceType: string = 'WEB'
): Promise<boolean> => {
  try {
    const fcmToken = await requestFCMToken();
    
    if (!fcmToken) {
      console.error('Failed to get FCM token');
      return false;
    }

    // 백엔드에 토큰 등록
    const response = await fetch('http://localhost:8080/api/fcm/token', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        userId,
        deviceToken: fcmToken,
        deviceId: deviceId || `web-${Date.now()}`,
        deviceType
      }),
    });

    const data = await response.json();
    
    if (data.success) {
      console.log('FCM token registered successfully');
      return true;
    } else {
      console.error('Failed to register FCM token:', data.message);
      return false;
    }
  } catch (error) {
    console.error('Error registering FCM token:', error);
    return false;
  }
};

/**
 * FCM 토큰 비활성화 (로그아웃 시)
 */
export const deactivateFCMToken = async (deviceToken: string): Promise<boolean> => {
  try {
    const response = await fetch('http://localhost:8080/api/fcm/token/deactivate', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        deviceToken
      }),
    });

    const data = await response.json();
    return data.success;
  } catch (error) {
    console.error('Error deactivating FCM token:', error);
    return false;
  }
};

export default { requestFCMToken, onMessageListener, registerFCMToken, deactivateFCMToken };