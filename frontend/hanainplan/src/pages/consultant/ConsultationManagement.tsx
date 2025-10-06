import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import {
  getTodayConsultations,
  getConsultationRequests,
  getConsultations,
  getConsultationStats,
  updateConsultationStatus,
  assignConsultationRequest,
  rejectConsultationRequest,
  type Consultation,
  type ConsultationRequest,
  type ConsultationFilters
} from '../../api/consultationApi';

type TabType = 'today' | 'requests' | 'all';

function ConsultationManagement() {
  const [activeTab, setActiveTab] = useState<TabType>('today');
  const [searchTerm, setSearchTerm] = useState('');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [priorityFilter, setPriorityFilter] = useState('ALL');

  // 오늘의 상담 조회
  const { data: todayConsultations = [], isLoading: todayLoading } = useQuery({
    queryKey: ['todayConsultations'],
    queryFn: getTodayConsultations
  });

  // 요청 내역 조회
  const { data: consultationRequests = [], isLoading: requestsLoading } = useQuery({
    queryKey: ['consultationRequests'],
    queryFn: getConsultationRequests
  });

  // 전체 상담 조회 (필터 적용)
  const { data: allConsultations = [], isLoading: allLoading } = useQuery({
    queryKey: ['consultations', searchTerm, statusFilter, priorityFilter],
    queryFn: () => getConsultations({
      search: searchTerm || undefined,
      status: statusFilter !== 'ALL' ? statusFilter : undefined,
      priority: priorityFilter !== 'ALL' ? priorityFilter : undefined
    })
  });

  // 통계 조회
  const { data: stats } = useQuery({
    queryKey: ['consultationStats'],
    queryFn: getConsultationStats,
    refetchInterval: 30000 // 30초마다 업데이트
  });

  // 상담 상태 변경 핸들러
  const handleStatusChange = async (consultationId: string, newStatus: Consultation['status']) => {
    try {
      await updateConsultationStatus(consultationId, newStatus);
      // 쿼리 무효화로 데이터 새로고침
      window.location.reload();
    } catch (error) {
      alert('상태 변경에 실패했습니다.');
    }
  };

  // 요청 수락 핸들러
  const handleAcceptRequest = async (requestId: string) => {
    try {
      await assignConsultationRequest(requestId, 'consultant1'); // 현재 상담사 ID
      alert('상담 요청을 수락했습니다.');
      window.location.reload();
    } catch (error) {
      alert('요청 수락에 실패했습니다.');
    }
  };

  // 요청 거절 핸들러
  const handleRejectRequest = async (requestId: string) => {
    const reason = prompt('거절 사유를 입력해주세요 (선택사항):');
    try {
      await rejectConsultationRequest(requestId, reason || undefined);
      alert('상담 요청을 거절했습니다.');
      window.location.reload();
    } catch (error) {
      alert('요청 거절에 실패했습니다.');
    }
  };

  // 날짜 포맷팅 함수
  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'short'
    });
  };

  // 시간 포맷팅 함수
  const formatTime = (timeString: string) => {
    return timeString.slice(0, 5); // HH:MM 형태로 반환
  };

  // 우선순위 색상 반환 함수
  const getPriorityColor = (priority: string) => {
    switch (priority) {
      case 'HIGH': return 'bg-red-100 text-red-800 border-red-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'LOW': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  // 상태 색상 반환 함수
  const getStatusColor = (status: string) => {
    switch (status) {
      case 'CONFIRMED': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'PENDING': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'COMPLETED': return 'bg-green-100 text-green-800 border-green-200';
      case 'CANCELLED': return 'bg-gray-100 text-gray-800 border-gray-200';
      case 'WAITING': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'ASSIGNED': return 'bg-orange-100 text-orange-800 border-orange-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  // 긴급도 색상 반환 함수
  const getUrgencyColor = (urgency: string) => {
    switch (urgency) {
      case 'HIGH': return 'bg-red-100 text-red-800 border-red-200';
      case 'MEDIUM': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case 'LOW': return 'bg-green-100 text-green-800 border-green-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  // 상담 유형 한글 변환 함수
  const getConsultationTypeText = (type: string) => {
    switch (type) {
      case 'GENERAL': return '일반 상담';
      case 'RETIREMENT': return '퇴직연금';
      case 'INVESTMENT': return '투자 상담';
      case 'INSURANCE': return '보험 상담';
      case 'TAX': return '세금 상담';
      default: return type;
    }
  };

  // 상담 유형 색상 반환 함수
  const getConsultationTypeColor = (type: string) => {
    switch (type) {
      case 'GENERAL': return 'bg-blue-100 text-blue-800 border-blue-200';
      case 'RETIREMENT': return 'bg-green-100 text-green-800 border-green-200';
      case 'INVESTMENT': return 'bg-purple-100 text-purple-800 border-purple-200';
      case 'INSURANCE': return 'bg-orange-100 text-orange-800 border-orange-200';
      case 'TAX': return 'bg-gray-100 text-gray-800 border-gray-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* 헤더 */}
          <div className="mb-8">
            <h1 className="text-3xl font-hana-bold text-gray-900 mb-3">상담 관리</h1>
            <p className="text-gray-600 font-hana-regular">
              고객 상담 일정을 관리하고 요청을 처리할 수 있습니다.
            </p>

            {/* 통계 카드 */}
            {stats && (
              <div className="grid grid-cols-1 md:grid-cols-4 gap-4 mt-6">
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                  <div className="text-2xl font-hana-bold text-hana-green">{stats.todayConsultations}</div>
                  <div className="text-sm text-gray-600 font-hana-regular">오늘의 상담</div>
                </div>
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                  <div className="text-2xl font-hana-bold text-orange-600">{stats.pendingRequests}</div>
                  <div className="text-sm text-gray-600 font-hana-regular">대기 중인 요청</div>
                </div>
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                  <div className="text-2xl font-hana-bold text-blue-600">{stats.totalConsultations}</div>
                  <div className="text-sm text-gray-600 font-hana-regular">전체 상담</div>
                </div>
                <div className="bg-white rounded-lg shadow-sm border border-gray-200 p-4">
                  <div className="text-2xl font-hana-bold text-green-600">{stats.completedConsultations}</div>
                  <div className="text-sm text-gray-600 font-hana-regular">완료된 상담</div>
                </div>
              </div>
            )}
          </div>

          {/* 탭 메뉴 */}
          <div className="bg-white rounded-lg shadow-md mb-6">
            <div className="flex border-b border-gray-200">
              <button
                onClick={() => setActiveTab('today')}
                className={`flex-1 py-4 px-6 font-hana-bold text-lg transition-colors ${
                  activeTab === 'today'
                    ? 'text-hana-green border-b-2 border-hana-green'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                오늘의 상담 ({todayConsultations.length})
              </button>
              <button
                onClick={() => setActiveTab('requests')}
                className={`flex-1 py-4 px-6 font-hana-bold text-lg transition-colors ${
                  activeTab === 'requests'
                    ? 'text-hana-green border-b-2 border-hana-green'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                요청 내역 ({consultationRequests.filter(r => r.status === 'WAITING' || r.status === 'ASSIGNED').length})
              </button>
              <button
                onClick={() => setActiveTab('all')}
                className={`flex-1 py-4 px-6 font-hana-bold text-lg transition-colors ${
                  activeTab === 'all'
                    ? 'text-hana-green border-b-2 border-hana-green'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                전체 목록 ({allConsultations.length})
              </button>
            </div>

            {/* 오늘의 상담 탭 */}
            {activeTab === 'today' && (
              <div className="p-6">
                {todayLoading ? (
                  <div className="flex justify-center items-center py-12">
                    <div className="text-center">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
                      <p className="mt-4 text-gray-600 font-hana-regular">오늘의 상담을 불러오는 중...</p>
                    </div>
                  </div>
                ) : todayConsultations.length === 0 ? (
                  <div className="text-center py-12">
                    <p className="text-gray-500 font-hana-regular">오늘 예정된 상담이 없습니다.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {todayConsultations.map((consultation) => (
                      <div key={consultation.id} className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors">
                        <div className="flex justify-between items-start mb-4">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h3 className="text-lg font-hana-bold text-gray-900">{consultation.title}</h3>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getPriorityColor(consultation.priority)}`}>
                                {consultation.priority === 'HIGH' ? '높음' : consultation.priority === 'MEDIUM' ? '보통' : '낮음'}
                              </span>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getStatusColor(consultation.status)}`}>
                                {consultation.status === 'CONFIRMED' ? '확정' : consultation.status === 'PENDING' ? '대기' : '완료'}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              고객: {consultation.customerName} | 상담 유형: {consultation.meetingType === 'ONLINE' ? '온라인' : consultation.meetingType === 'OFFLINE' ? '대면' : '전화'}
                            </p>
                            <p className="text-sm text-gray-600 font-hana-regular">
                              시간: {formatTime(consultation.consultationTime)} ({consultation.duration}분) |
                              {consultation.location && ` 장소: ${consultation.location}`}
                            </p>
                          </div>
                        </div>
                        {consultation.description && (
                          <div className="mb-4">
                            <p className="text-sm text-gray-700 font-hana-regular">{consultation.description}</p>
                          </div>
                        )}
                        {consultation.notes && (
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-sm text-blue-800 font-hana-regular">
                              <strong>메모:</strong> {consultation.notes}
                            </p>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* 요청 내역 탭 */}
            {activeTab === 'requests' && (
              <div className="p-6">
                {requestsLoading ? (
                  <div className="flex justify-center items-center py-12">
                    <div className="text-center">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
                      <p className="mt-4 text-gray-600 font-hana-regular">요청 내역을 불러오는 중...</p>
                    </div>
                  </div>
                ) : consultationRequests.filter(r => r.status === 'WAITING' || r.status === 'ASSIGNED').length === 0 ? (
                  <div className="text-center py-12">
                    <p className="text-gray-500 font-hana-regular">새로운 요청이 없습니다.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {consultationRequests
                      .filter(r => r.status === 'WAITING' || r.status === 'ASSIGNED')
                      .map((request) => (
                      <div key={request.id} className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors">
                        <div className="flex justify-between items-start mb-4">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h3 className="text-lg font-hana-bold text-gray-900">
                                {request.customerName}님의 상담 요청
                              </h3>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getUrgencyColor(request.urgency)}`}>
                                {request.urgency === 'HIGH' ? '긴급' : request.urgency === 'MEDIUM' ? '보통' : '여유'}
                              </span>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getStatusColor(request.status)}`}>
                                {request.status === 'WAITING' ? '대기중' : '배정됨'}
                              </span>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getConsultationTypeColor(request.consultationType)}`}>
                                {getConsultationTypeText(request.consultationType)}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              연락처: {request.customerPhone} {request.customerEmail && `| 이메일: ${request.customerEmail}`}
                            </p>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              희망일시: {formatDate(request.preferredDate)} {formatTime(request.preferredTime)}
                            </p>
                          </div>
                          <div className="flex gap-2 ml-4">
                            {request.status === 'WAITING' && (
                              <>
                                <button
                                  onClick={() => handleAcceptRequest(request.id)}
                                  className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-medium transition-colors"
                                >
                                  수락
                                </button>
                                <button
                                  onClick={() => handleRejectRequest(request.id)}
                                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 font-hana-medium transition-colors"
                                >
                                  거절
                                </button>
                              </>
                            )}
                            {request.status === 'ASSIGNED' && (
                              <span className="px-3 py-2 bg-blue-100 text-blue-800 rounded-lg text-sm font-hana-medium">
                                배정됨
                              </span>
                            )}
                          </div>
                        </div>
                        <div className="bg-gray-100 rounded-lg p-3">
                          <p className="text-sm text-gray-700 font-hana-regular">
                            <strong>상담 내용:</strong> {request.description}
                          </p>
                        </div>
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* 전체 목록 탭 */}
            {activeTab === 'all' && (
              <div className="p-6">
                {/* 검색 및 필터 */}
                <div className="mb-6 space-y-4">
                  <div className="flex gap-4">
                    <div className="flex-1">
                      <input
                        type="text"
                        placeholder="상담 내용이나 고객명으로 검색..."
                        value={searchTerm}
                        onChange={(e) => setSearchTerm(e.target.value)}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                      />
                    </div>
                    <select
                      value={statusFilter}
                      onChange={(e) => setStatusFilter(e.target.value)}
                      className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                    >
                      <option value="ALL">전체 상태</option>
                      <option value="PENDING">대기</option>
                      <option value="CONFIRMED">확정</option>
                      <option value="COMPLETED">완료</option>
                      <option value="CANCELLED">취소</option>
                    </select>
                    <select
                      value={priorityFilter}
                      onChange={(e) => setPriorityFilter(e.target.value)}
                      className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                    >
                      <option value="ALL">전체 우선순위</option>
                      <option value="HIGH">높음</option>
                      <option value="MEDIUM">보통</option>
                      <option value="LOW">낮음</option>
                    </select>
                  </div>
                </div>

                {allLoading ? (
                  <div className="flex justify-center items-center py-12">
                    <div className="text-center">
                      <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
                      <p className="mt-4 text-gray-600 font-hana-regular">상담 목록을 불러오는 중...</p>
                    </div>
                  </div>
                ) : allConsultations.length === 0 ? (
                  <div className="text-center py-12">
                    <p className="text-gray-500 font-hana-regular">상담 내역이 없습니다.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {allConsultations.map((consultation) => (
                      <div key={consultation.id} className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors">
                        <div className="flex justify-between items-start mb-4">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h3 className="text-lg font-hana-bold text-gray-900">{consultation.title}</h3>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getPriorityColor(consultation.priority)}`}>
                                {consultation.priority === 'HIGH' ? '높음' : consultation.priority === 'MEDIUM' ? '보통' : '낮음'}
                              </span>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getStatusColor(consultation.status)}`}>
                                {consultation.status === 'CONFIRMED' ? '확정' : consultation.status === 'PENDING' ? '대기' : consultation.status === 'COMPLETED' ? '완료' : '취소'}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              고객: {consultation.customerName} | 일자: {formatDate(consultation.consultationDate)} {formatTime(consultation.consultationTime)}
                            </p>
                            <p className="text-sm text-gray-600 font-hana-regular">
                              상담 유형: {consultation.meetingType === 'ONLINE' ? '온라인' : consultation.meetingType === 'OFFLINE' ? '대면' : '전화'}
                              {consultation.location && ` | 장소: ${consultation.location}`}
                            </p>
                          </div>
                          <div className="flex gap-2 ml-4">
                            {consultation.status === 'PENDING' && (
                              <>
                                <button
                                  onClick={() => handleStatusChange(consultation.id, 'CONFIRMED')}
                                  className="px-3 py-1 bg-hana-green text-white rounded text-sm hover:bg-green-600 font-hana-medium transition-colors"
                                >
                                  확정
                                </button>
                                <button
                                  onClick={() => handleStatusChange(consultation.id, 'CANCELLED')}
                                  className="px-3 py-1 bg-red-500 text-white rounded text-sm hover:bg-red-600 font-hana-medium transition-colors"
                                >
                                  취소
                                </button>
                              </>
                            )}
                            {consultation.status === 'CONFIRMED' && (
                              <button
                                onClick={() => handleStatusChange(consultation.id, 'COMPLETED')}
                                className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 font-hana-medium transition-colors"
                              >
                                완료
                              </button>
                            )}
                          </div>
                        </div>
                        {consultation.description && (
                          <div className="mb-4">
                            <p className="text-sm text-gray-700 font-hana-regular">{consultation.description}</p>
                          </div>
                        )}
                        {consultation.notes && (
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-sm text-blue-800 font-hana-regular">
                              <strong>메모:</strong> {consultation.notes}
                            </p>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default ConsultationManagement;
