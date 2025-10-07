// Firebase Messaging Service Worker
// 백그라운드 푸시 알림 수신 처리

importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-app-compat.js');
importScripts('https://www.gstatic.com/firebasejs/10.7.1/firebase-messaging-compat.js');

/**
 * Firebase 설정
 * TODO: 실제 Firebase 프로젝트 설정값으로 교체
 */
firebase.initializeApp({
    apiKey: "AIzaSyDG40TTw7B4leIGdsJNhT2fDsa852ZHeiQ",
    authDomain: "hanainplan.firebaseapp.com",
    projectId: "hanainplan",
    storageBucket: "hanainplan.firebasestorage.app",
    messagingSenderId: "1027319351509",
    appId: "1:1027319351509:web:d681831a5259aa8ad4fa5c",
    measurementId: "G-8KHVVJLD3N"
});

const messaging = firebase.messaging();

/**
 * 백그라운드 메시지 수신 처리
 */
messaging.onBackgroundMessage((payload) => {
  console.log('[firebase-messaging-sw.js] Received background message:', payload);

  // 알림 제목과 내용
  const notificationTitle = payload.notification?.title || '하나인플랜 상담';
  const notificationOptions = {
    body: payload.notification?.body || '새로운 알림이 있습니다.',
    icon: '/logo/hana-logo.png',
    badge: '/logo/hana-symbol.png',
    data: payload.data,
    vibrate: [200, 100, 200],
    tag: payload.data?.type || 'general',
    requireInteraction: true, // 사용자가 알림을 확인할 때까지 유지
    actions: []
  };

  // 알림 타입에 따라 다른 액션 추가
  if (payload.data?.type === 'CONSULTATION_REQUEST') {
    notificationOptions.actions = [
      { action: 'accept', title: '상담 수락' },
      { action: 'reject', title: '거절' }
    ];
  } else if (payload.data?.type === 'CONSULTATION_ACCEPTED') {
    notificationOptions.actions = [
      { action: 'join', title: '상담 참여' }
    ];
  }

  // 알림 표시
  return self.registration.showNotification(notificationTitle, notificationOptions);
});

/**
 * 알림 클릭 처리
 */
self.addEventListener('notificationclick', (event) => {
  console.log('[Service Worker] Notification click received:', event);

  event.notification.close();

  const data = event.notification.data;
  let url = '/videocall'; // 기본 URL

  // 알림 타입에 따라 URL 결정
  if (data?.roomId) {
    url = `/videocall?roomId=${data.roomId}`;
  }

  // 액션 버튼 클릭 처리
  if (event.action === 'accept') {
    // 상담 수락 API 호출 (선택사항)
    console.log('Accepting consultation:', data?.roomId);
    url = `/videocall?roomId=${data.roomId}&autoAccept=true`;
  } else if (event.action === 'reject') {
    // 상담 거절
    console.log('Rejecting consultation:', data?.roomId);
    return; // 페이지를 열지 않음
  } else if (event.action === 'join') {
    // 상담 참여
    console.log('Joining consultation:', data?.roomId);
    url = `/videocall?roomId=${data.roomId}`;
  }

  // 클라이언트 윈도우 열기 또는 포커스
  event.waitUntil(
    clients.matchAll({ type: 'window', includeUncontrolled: true })
      .then((clientList) => {
        // 이미 열려있는 윈도우가 있으면 포커스
        for (const client of clientList) {
          if (client.url.includes('/videocall') && 'focus' in client) {
            return client.focus();
          }
        }
        // 새 윈도우 열기
        if (clients.openWindow) {
          return clients.openWindow(url);
        }
      })
  );
});

/**
 * Service Worker 설치
 */
self.addEventListener('install', (event) => {
  console.log('[Service Worker] Installing...');
  self.skipWaiting();
});

/**
 * Service Worker 활성화
 */
self.addEventListener('activate', (event) => {
  console.log('[Service Worker] Activating...');
  event.waitUntil(clients.claim());
});

console.log('[Service Worker] Firebase Messaging Service Worker loaded');