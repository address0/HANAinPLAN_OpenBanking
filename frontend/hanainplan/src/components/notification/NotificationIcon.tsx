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

  const { data: summary, error: summaryError } = useQuery({
    queryKey: ['notificationSummary', user?.userId],
    queryFn: async () => {
      if (!user?.userId) {
        return null;
      }

      const result = await fetchNotificationSummary(user.userId);
      return result;
    },
    refetchInterval: 60000,
    enabled: !!user?.userId,
  });

  if (summaryError) {
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

        {}
        {summary?.unreadCount !== undefined && (
          <span className="absolute -top-1 -right-1 bg-red-500 text-white text-xs font-hana-bold rounded-full h-5 w-5 flex items-center justify-center min-w-fit px-1">
            {summary.unreadCount > 99 ? '99+' : summary.unreadCount}
          </span>
        )}
      </button>

      {}
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