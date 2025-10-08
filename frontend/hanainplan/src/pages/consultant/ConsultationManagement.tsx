import { useState, useEffect } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import {
  getTodayConsultations,
  getConsultationRequests,
  getConsultantConsultations,
  updateConsultationStatus,
  type ConsultationResponse
} from '../../api/consultationApi';
import { useUserStore } from '../../store/userStore';

type TabType = 'today' | 'requests' | 'all';

function ConsultationManagement() {
  const [activeTab, setActiveTab] = useState<TabType>('today');
  const [statusFilter, setStatusFilter] = useState('ALL');
  const queryClient = useQueryClient();
  
  const { user } = useUserStore();
  const consultantId = user?.userId;

  // 오늘의 상담 조회
  const { data: todayConsultations = [], isLoading: todayLoading, refetch: refetchToday } = useQuery({
    queryKey: ['todayConsultations', consultantId],
    queryFn: () => consultantId ? getTodayConsultations(consultantId) : Promise.resolve([]),
    enabled: !!consultantId
  });

  // 요청 내역 조회
  const { data: consultationRequests = [], isLoading: requestsLoading, refetch: refetchRequests } = useQuery({
    queryKey: ['consultationRequests', consultantId],
    queryFn: () => consultantId ? getConsultationRequests(consultantId) : Promise.resolve([]),
    enabled: !!consultantId
  });

  // 전체 상담 조회
  const { data: allConsultationsData = [], isLoading: allLoading, refetch: refetchAll } = useQuery({
    queryKey: ['consultations', consultantId],
    queryFn: () => consultantId ? getConsultantConsultations(consultantId) : Promise.resolve([]),
    enabled: !!consultantId
  });

  // 필터링된 상담 목록
  const allConsultations = statusFilter === 'ALL' 
    ? allConsultationsData
    : allConsultationsData.filter(c => c.consultStatus === statusFilter);

  // 통계 계산
  const stats = {
    todayConsultations: todayConsultations.length,
    pendingRequests: consultationRequests.filter(r => r.consultStatus === '예약신청').length,
    totalConsultations: allConsultationsData.length,
    completedConsultations: allConsultationsData.filter(c => c.consultStatus === '상담완료').length
  };

  // 상담 상태 변경 mutation
  const statusMutation = useMutation({
    mutationFn: ({ consultId, status }: { consultId: string; status: string }) => 
      updateConsultationStatus(consultId, status),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['todayConsultations'] });
      queryClient.invalidateQueries({ queryKey: ['consultationRequests'] });
      queryClient.invalidateQueries({ queryKey: ['consultations'] });
      alert('상담 상태가 변경되었습니다.');
    },
    onError: (error: any) => {
      alert(error.message || '상태 변경에 실패했습니다.');
    }
  });

  // 상담 상태 변경 핸들러
  const handleStatusChange = (consultationId: string, newStatus: string) => {
    statusMutation.mutate({ consultId: consultationId, status: newStatus });
  };

  // 요청 수락 핸들러
  const handleAcceptRequest = (requestId: string) => {
    handleStatusChange(requestId, '예약확정');
  };

  // 요청 거절 핸들러
  const handleRejectRequest = (requestId: string) => {
    if (confirm('정말 거절하시겠습니까?')) {
      handleStatusChange(requestId, '취소');
    }
  };

  // 날짜/시간 포맷팅 함수
  const formatDateTime = (datetimeString: string) => {
    const date = new Date(datetimeString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      weekday: 'short',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  // 상태 색상 반환 함수
  const getStatusColor = (status: string) => {
    switch (status) {
      case '예약확정': return 'bg-blue-100 text-blue-800 border-blue-200';
      case '예약신청': return 'bg-yellow-100 text-yellow-800 border-yellow-200';
      case '상담완료': return 'bg-green-100 text-green-800 border-green-200';
      case '취소': return 'bg-gray-100 text-gray-800 border-gray-200';
      case '상담중': return 'bg-purple-100 text-purple-800 border-purple-200';
      default: return 'bg-gray-100 text-gray-800 border-gray-200';
    }
  };

  // 상담 유형 한글 변환 함수
  const getConsultationTypeText = (type: string) => {
    switch (type) {
      case 'general': return '일반';
      case 'product': return '상품가입';
      case 'asset-management': return '자산관리';
      default: return type;
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
                요청 내역 ({consultationRequests.length})
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
                      <div key={consultation.consultId} className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors">
                        <div className="flex justify-between items-start mb-4">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h3 className="text-lg font-hana-bold text-gray-900">
                                {consultation.customerName || '고객'}님 상담
                              </h3>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getStatusColor(consultation.consultStatus)}`}>
                                {consultation.consultStatus}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              고객: {consultation.customerName} | 상담 유형: {getConsultationTypeText(consultation.consultType)}
                            </p>
                            <p className="text-sm text-gray-600 font-hana-regular">
                              예약 시간: {formatDateTime(consultation.reservationDatetime)}
                            </p>
                          </div>
                        </div>
                        {consultation.detail && (
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-sm text-blue-800 font-hana-regular">
                              <strong>상담 내용:</strong> {consultation.detail}
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
                ) : consultationRequests.length === 0 ? (
                  <div className="text-center py-12">
                    <p className="text-gray-500 font-hana-regular">새로운 요청이 없습니다.</p>
                  </div>
                ) : (
                  <div className="space-y-4">
                    {consultationRequests.map((request) => (
                      <div key={request.consultId} className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors">
                        <div className="flex justify-between items-start mb-4">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h3 className="text-lg font-hana-bold text-gray-900">
                                {request.customerName || '고객'}님의 상담 요청
                              </h3>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getStatusColor(request.consultStatus)}`}>
                                {request.consultStatus}
                              </span>
                              <span className="px-2 py-1 text-xs border rounded-full font-hana-medium bg-blue-100 text-blue-800 border-blue-200">
                                {getConsultationTypeText(request.consultType)}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              예약일시: {formatDateTime(request.reservationDatetime)}
                            </p>
                          </div>
                          <div className="flex gap-2 ml-4">
                            {request.consultStatus === '예약신청' && (
                              <>
                                <button
                                  onClick={() => handleAcceptRequest(request.consultId)}
                                  disabled={statusMutation.isPending}
                                  className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-medium transition-colors disabled:opacity-50"
                                >
                                  수락
                                </button>
                                <button
                                  onClick={() => handleRejectRequest(request.consultId)}
                                  disabled={statusMutation.isPending}
                                  className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 font-hana-medium transition-colors disabled:opacity-50"
                                >
                                  거절
                                </button>
                              </>
                            )}
                          </div>
                        </div>
                        {request.detail && (
                          <div className="bg-gray-100 rounded-lg p-3">
                            <p className="text-sm text-gray-700 font-hana-regular">
                              <strong>상담 내용:</strong> {request.detail}
                            </p>
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
            )}

            {/* 전체 목록 탭 */}
            {activeTab === 'all' && (
              <div className="p-6">
                {/* 필터 */}
                <div className="mb-6">
                  <select
                    value={statusFilter}
                    onChange={(e) => setStatusFilter(e.target.value)}
                    className="px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                  >
                    <option value="ALL">전체 상태</option>
                    <option value="예약신청">예약신청</option>
                    <option value="예약확정">예약확정</option>
                    <option value="상담중">상담중</option>
                    <option value="상담완료">상담완료</option>
                    <option value="취소">취소</option>
                  </select>
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
                      <div key={consultation.consultId} className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors">
                        <div className="flex justify-between items-start mb-4">
                          <div className="flex-1">
                            <div className="flex items-center gap-2 mb-2">
                              <h3 className="text-lg font-hana-bold text-gray-900">
                                {consultation.customerName || '고객'}님 상담
                              </h3>
                              <span className={`px-2 py-1 text-xs border rounded-full font-hana-medium ${getStatusColor(consultation.consultStatus)}`}>
                                {consultation.consultStatus}
                              </span>
                            </div>
                            <p className="text-sm text-gray-600 font-hana-regular mb-2">
                              고객: {consultation.customerName} | 상담 유형: {getConsultationTypeText(consultation.consultType)}
                            </p>
                            <p className="text-sm text-gray-600 font-hana-regular">
                              예약 시간: {formatDateTime(consultation.reservationDatetime)}
                            </p>
                            {consultation.consultDatetime && (
                              <p className="text-sm text-gray-600 font-hana-regular">
                                실제 상담 시간: {formatDateTime(consultation.consultDatetime)}
                              </p>
                            )}
                          </div>
                          <div className="flex gap-2 ml-4">
                            {consultation.consultStatus === '예약신청' && (
                              <>
                                <button
                                  onClick={() => handleStatusChange(consultation.consultId, '예약확정')}
                                  disabled={statusMutation.isPending}
                                  className="px-3 py-1 bg-hana-green text-white rounded text-sm hover:bg-green-600 font-hana-medium transition-colors disabled:opacity-50"
                                >
                                  확정
                                </button>
                                <button
                                  onClick={() => handleStatusChange(consultation.consultId, '취소')}
                                  disabled={statusMutation.isPending}
                                  className="px-3 py-1 bg-red-500 text-white rounded text-sm hover:bg-red-600 font-hana-medium transition-colors disabled:opacity-50"
                                >
                                  취소
                                </button>
                              </>
                            )}
                            {consultation.consultStatus === '예약확정' && (
                              <button
                                onClick={() => handleStatusChange(consultation.consultId, '상담완료')}
                                disabled={statusMutation.isPending}
                                className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 font-hana-medium transition-colors disabled:opacity-50"
                              >
                                완료
                              </button>
                            )}
                          </div>
                        </div>
                        {consultation.detail && (
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                            <p className="text-sm text-blue-800 font-hana-regular">
                              <strong>상담 내용:</strong> {consultation.detail}
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

