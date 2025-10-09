import { useState, useEffect, useRef } from 'react';
import { useQuery } from '@tanstack/react-query';
import { useUserStore } from '../../store/userStore';
import { fetchNotificationSummary, type Notification } from '../../api/notificationApi';
import NotificationDropdown from './NotificationDropdown';

function NotificationIcon() {
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [selectedNotification, setSelectedNotification] = useState<Notification | null>(null);
  const notificationRef = useRef<HTMLDivElement>(null);
  const { user } = useUserStore();

  // 알림 개수 요약 조회
  const { data: summary, error: summaryError } = useQuery({
    queryKey: ['notificationSummary', user?.userId],
    queryFn: async () => {
      if (!user?.userId) {
        console.log('사용자 ID가 없어서 알림 조회를 건너뜁니다');
        return null;
      }

      console.log(`알림 개수 요약 API 호출 시작 - 사용자 ID: ${user.userId}`);
      const result = await fetchNotificationSummary(user.userId);
      console.log('알림 개수 요약 API 응답:', result);
      return result;
    },
    refetchInterval: 60000, // 1분마다 새로고침
    enabled: !!user?.userId, // 사용자 ID가 있을 때만 조회
  });

  // 에러 처리
  if (summaryError) {
    console.error('알림 개수 요약 조회 실패:', summaryError);
  }

  const handleIconClick = () => {
    setIsDropdownOpen(!isDropdownOpen);
  };

  const handleCloseDropdown = () => {
    setIsDropdownOpen(false);
  };

  const handleNotificationSelect = (notification: Notification) => {
    setSelectedNotification(notification);
    setIsDropdownOpen(false);
  };

  const handleCloseModal = () => {
    setSelectedNotification(null);
  };

  // 클릭 아웃사이드 처리
  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (notificationRef.current && !notificationRef.current.contains(event.target as Node)) {
        setIsDropdownOpen(false);
      }
    };

    if (isDropdownOpen) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [isDropdownOpen]);

  // 로그인하지 않은 사용자는 알림 아이콘을 표시하지 않음
  if (!user) {
    return null;
  }

  return (
    <div
      ref={notificationRef}
      className="relative"
      onClick={(e) => e.stopPropagation()}
    >
      <button
        onClick={(e) => {
          e.stopPropagation();
          handleIconClick();
        }}
        className="relative p-2 text-gray-600 hover:text-hana-green transition-colors"
      >
        <img src="/icons/noti.png" alt="알림" className="w-8 h-8" />

        {/* 알림 뱃지 */}
        {summary?.unreadCount !== undefined && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-hana-bold rounded-full h-5 w-5 flex items-center justify-center min-w-fit px-1">
            {summary.unreadCount > 99 ? '99+' : summary.unreadCount}
          </span>
        )}
      </button>

      {/* 알림 드롭다운 */}
      <NotificationDropdown
        isOpen={isDropdownOpen}
        onClose={handleCloseDropdown}
        onNotificationSelect={handleNotificationSelect}
        selectedNotification={selectedNotification}
        onCloseModal={handleCloseModal}
      />
    </div>
  );
}

export default NotificationIcon;
