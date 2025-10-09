import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { createPortal } from 'react-dom';
import {
  fetchNotificationSummary,
  fetchUnreadNotifications,
  markNotificationAsRead,
  markAllNotificationsAsRead,
  deleteNotification,
  type Notification,
  type NotificationType
} from '../../api/notificationApi';

interface NotificationDropdownProps {
  isOpen: boolean;
  onClose: () => void;
  onNotificationSelect: (notification: Notification) => void;
  selectedNotification: Notification | null;
  onCloseModal: () => void;
}

function NotificationDropdown({ 
  isOpen, 
  onClose, 
  onNotificationSelect,
  selectedNotification,
  onCloseModal
}: NotificationDropdownProps) {
  const queryClient = useQueryClient();

  // 현재 로그인한 사용자 정보 가져오기
  const getCurrentUserId = (): number | undefined => {
    try {
      const userStorage = localStorage.getItem('user-storage');
      return userStorage ? JSON.parse(userStorage)?.state?.user?.userId : undefined;
    } catch (error) {
      console.error('사용자 정보 파싱 실패:', error);
      return undefined;
    }
  };

  // 알림 개수 요약 조회
  const { data: summary } = useQuery({
    queryKey: ['notificationSummary'],
    queryFn: () => {
      const userId = getCurrentUserId();
      return fetchNotificationSummary(userId);
    },
    refetchInterval: 30000, // 30초마다 새로고침
    enabled: !!getCurrentUserId(), // 사용자 ID가 있을 때만 조회
  });

  // 읽지 않은 알림 목록 조회
  const { data: unreadNotifications, isLoading, error: unreadError } = useQuery({
    queryKey: ['unreadNotifications'],
    queryFn: async () => {
      const userId = getCurrentUserId();
      if (!userId) {
        throw new Error('로그인이 필요합니다.');
      }

      console.log('읽지 않은 알림 목록 API 호출 시작 - 사용자 ID:', userId);
      const result = await fetchUnreadNotifications(0, 10, userId);
      console.log('읽지 않은 알림 목록 API 응답:', result);
      return result;
    },
    refetchInterval: 30000,
    enabled: isOpen && !!getCurrentUserId(), // 드롭다운이 열려있고 사용자 ID가 있을 때만 조회
  });

  // 에러 처리
  if (unreadError) {
    console.error('읽지 않은 알림 목록 조회 실패:', unreadError);
  }

  // 알림 읽음 처리 mutation
  const markAsReadMutation = useMutation({
    mutationFn: async (notificationId: number) => {
      console.log('읽음 처리 API 호출 시작:', notificationId);
      const userId = getCurrentUserId();
      if (!userId) {
        throw new Error('로그인이 필요합니다.');
      }
      const result = await markNotificationAsRead(notificationId);
      console.log('읽음 처리 API 응답:', result);
      return result;
    },
    onSuccess: (data, variables) => {
      console.log('읽음 처리 성공:', variables, data);
      // 쿼리 무효화
      queryClient.invalidateQueries({ queryKey: ['notificationSummary'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotifications'] });
      console.log('읽음 처리 성공, 쿼리 무효화 완료');
      // 모달 닫기
      onCloseModal();
    },
    onError: (error, variables) => {
      console.error('읽음 처리 실패:', variables, error);
      alert('읽음 처리 중 오류가 발생했습니다.');
    },
  });

  // 모든 알림 읽음 처리 mutation
  const markAllAsReadMutation = useMutation({
    mutationFn: async () => {
      const userId = getCurrentUserId();
      if (!userId) {
        throw new Error('로그인이 필요합니다.');
      }
      return markAllNotificationsAsRead();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificationSummary'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotifications'] });
    },
  });

  // 알림 삭제 mutation
  const deleteMutation = useMutation({
    mutationFn: async (notificationId: number) => {
      const userId = getCurrentUserId();
      if (!userId) {
        throw new Error('로그인이 필요합니다.');
      }
      return deleteNotification(notificationId);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['notificationSummary'] });
      queryClient.invalidateQueries({ queryKey: ['unreadNotifications'] });
    },
  });

  // 알림 클릭 핸들러
  const handleNotificationClick = (notification: Notification, e: React.MouseEvent) => {
    e.stopPropagation(); // 클릭 이벤트 전파 방지

    console.log('=== 알림 클릭 시작 ===');
    console.log('클릭한 알림:', notification.id, notification.isRead);

    // 부모 컴포넌트에 알림 선택 전달
    onNotificationSelect(notification);

    console.log('알림 선택 완료');
    console.log('=== 알림 클릭 완료 ===');
  };

  // 모든 알림 읽음 처리 핸들러
  const handleMarkAllAsRead = () => {
    markAllAsReadMutation.mutate();
  };

  // 알림 삭제 핸들러
  const handleDeleteNotification = (notificationId: number, e: React.MouseEvent) => {
    e.stopPropagation(); // 클릭 이벤트 전파 방지
    if (confirm('알림을 삭제하시겠습니까?')) {
      deleteMutation.mutate(notificationId);
    }
  };

  // 알림 타입별 색상
  const getNotificationTypeColor = (type: NotificationType) => {
    switch (type) {
      case 'CONSULTATION':
        return 'text-blue-600 bg-blue-50';
      case 'TRANSACTION':
        return 'text-green-600 bg-green-50';
      case 'SYSTEM':
        return 'text-purple-600 bg-purple-50';
      case 'SCHEDULE':
        return 'text-orange-600 bg-orange-50';
      case 'OTHER':
        return 'text-gray-600 bg-gray-50';
      default:
        return 'text-gray-600 bg-gray-50';
    }
  };

  // 알림 타입 한글 변환
  const getNotificationTypeLabel = (type: NotificationType) => {
    switch (type) {
      case 'CONSULTATION':
        return '상담';
      case 'TRANSACTION':
        return '거래';
      case 'SYSTEM':
        return '시스템';
      case 'SCHEDULE':
        return '일정';
      case 'OTHER':
        return '기타';
      default:
        return '기타';
    }
  };

  // 시간 표시 포맷팅
  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    const now = new Date();
    const diffInMinutes = Math.floor((now.getTime() - date.getTime()) / (1000 * 60));

    if (diffInMinutes < 1) return '방금 전';
    if (diffInMinutes < 60) return `${diffInMinutes}분 전`;

    const diffInHours = Math.floor(diffInMinutes / 60);
    if (diffInHours < 24) return `${diffInHours}시간 전`;

    const diffInDays = Math.floor(diffInHours / 24);
    if (diffInDays < 7) return `${diffInDays}일 전`;

    return date.toLocaleDateString('ko-KR');
  };

  return (
    <>
      {/* 알림 드랍다운 */}
      {isOpen && (
        <div
          className="absolute top-full right-0 mt-2 w-80 bg-white rounded-lg shadow-lg border border-gray-200 z-[60] max-h-96 overflow-hidden"
          onClick={(e) => e.stopPropagation()}
        >
      {/* 헤더 */}
      <div className="flex items-center justify-between p-4 border-b border-gray-100">
        <h3 className="font-hana-bold text-lg text-gray-900">알림</h3>
        <div className="flex items-center gap-2">
          {summary?.unreadCount && summary.unreadCount > 0 && (
            <button
              onClick={handleMarkAllAsRead}
              className="text-sm text-hana-green hover:text-green-600 font-hana-medium"
            >
              모두 읽음
            </button>
          )}
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>
      </div>

      {/* 알림 개수 요약 */}
      {summary && summary.unreadCount > 0 && (
        <div className="px-4 py-2 bg-gray-50 border-b border-gray-100">
          <div className="flex items-center justify-center text-sm">
            <span className="text-hana-green font-hana-medium">
              읽지 않음 {summary.unreadCount}개
            </span>
          </div>
        </div>
      )}

      {/* 알림 목록 */}
      <div className="max-h-64 overflow-y-auto">
        {isLoading ? (
          <div className="flex justify-center items-center py-8">
            <div className="animate-spin rounded-full h-6 w-6 border-b-2 border-hana-green"></div>
          </div>
        ) : unreadNotifications?.content && unreadNotifications.content.length > 0 ? (
          <div className="divide-y divide-gray-100">
            {unreadNotifications.content.map((notification) => (
              <div
                key={notification.id}
                className={`p-4 hover:bg-gray-50 cursor-pointer transition-colors ${
                  !notification.isRead ? 'bg-blue-50/30' : ''
                }`}
                onClick={(e) => handleNotificationClick(notification, e)}
              >
                <div className="flex items-start justify-between">
                  <div className="flex-1 min-w-0">
                    <div className="flex items-center gap-2 mb-1">
                      <span className={`px-2 py-1 rounded-full text-xs font-hana-medium ${getNotificationTypeColor(notification.type)}`}>
                        {getNotificationTypeLabel(notification.type)}
                      </span>
                      {!notification.isRead && (
                        <span className="w-2 h-2 bg-hana-green rounded-full"></span>
                      )}
                    </div>
                    <h4 className="font-hana-medium text-sm text-gray-900 truncate mb-1">
                      {notification.title}
                    </h4>
                    <p className="text-xs text-gray-600 line-clamp-2 mb-2">
                      {notification.content}
                    </p>
                    <span className="text-xs text-gray-400">
                      {formatTime(notification.createdAt)}
                    </span>
                  </div>
                  <button
                    onClick={(e) => {
                      e.stopPropagation(); // 클릭 이벤트 전파 방지
                      handleDeleteNotification(notification.id, e);
                    }}
                    className="ml-2 p-1 text-gray-400 hover:text-red-500 transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                    </svg>
                  </button>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <div className="flex flex-col items-center justify-center py-8 px-4 text-center">
            <img src="/icons/noti_none.png" alt="알림 없음" className="w-12 h-12 mb-3" />
            <p className="text-sm text-gray-500 font-hana-regular">
              새로운 알림이 없습니다
            </p>
          </div>
        )}
      </div>

      {/* 푸터 */}
      <div className="p-3 border-t border-gray-100 bg-gray-50">
        <button
          onClick={onClose}
          className="w-full text-center text-sm text-gray-600 hover:text-gray-800 font-hana-medium"
        >
          알림 창 닫기
        </button>
      </div>
        </div>
      )}

      {/* 알림 상세 모달 - Portal을 사용하여 최상위에 렌더링 */}
      {selectedNotification && createPortal(
        <div 
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[70]"
          onClick={onCloseModal}
        >
          <div 
            className="bg-white rounded-lg shadow-xl p-6 w-full max-w-lg mx-4 max-h-[80vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="flex items-center justify-between mb-4">
              <div className="flex items-center gap-2">
                <span className={`px-2 py-1 rounded-full text-xs font-hana-medium ${getNotificationTypeColor(selectedNotification.type)}`}>
                  {getNotificationTypeLabel(selectedNotification.type)}
                </span>
                {!selectedNotification.isRead && (
                  <span className="w-2 h-2 bg-hana-green rounded-full"></span>
                )}
              </div>
              <button
                onClick={onCloseModal}
                className="text-gray-400 hover:text-gray-600"
              >
                <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </button>
            </div>

            <div className="space-y-4">
              <div>
                <h3 className="text-lg font-hana-bold text-gray-900 mb-2">
                  {selectedNotification.title}
                </h3>
                <p className="text-sm text-gray-500">
                  {formatTime(selectedNotification.createdAt)}
                </p>
              </div>

              <div className="bg-gray-50 p-4 rounded-lg">
                <p className="text-gray-800 font-hana-regular whitespace-pre-wrap">
                  {selectedNotification.content}
                </p>
              </div>
            </div>

            <div className="flex gap-3 mt-6">
              <button
                onClick={onCloseModal}
                className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-hana-medium"
              >
                닫기
              </button>
              {!selectedNotification.isRead && (
                <button
                  onClick={() => {
                    console.log('=== 읽음 처리 버튼 클릭 ===');
                    console.log('모달 내 알림:', selectedNotification.id, selectedNotification.isRead);

                    // 읽지 않은 알림만 읽음 처리
                    const userId = getCurrentUserId();
                    if (userId && selectedNotification) {
                      console.log('읽음 처리 mutation 호출:', selectedNotification.id);
                      markAsReadMutation.mutate(selectedNotification.id);
                    } else {
                      console.error('사용자 ID 또는 알림 정보를 찾을 수 없습니다');
                    }
                  }}
                  className="flex-1 px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                >
                  읽음 처리
                </button>
              )}
            </div>
          </div>
        </div>,
        document.body
      )}
    </>
  );
}

export default NotificationDropdown;
