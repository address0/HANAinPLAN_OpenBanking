import { useState } from 'react';
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
  const [showConfirmModal, setShowConfirmModal] = useState(false);
  const [confirmedConsultation, setConfirmedConsultation] = useState<ConsultationResponse | null>(null);
  const queryClient = useQueryClient();

  const { user } = useUserStore();
  const consultantId = user?.userId;

  const { data: todayConsultations = [], isLoading: todayLoading } = useQuery({
    queryKey: ['todayConsultations', consultantId],
    queryFn: () => consultantId ? getTodayConsultations(consultantId) : Promise.resolve([]),
    enabled: !!consultantId
  });

  const { data: consultationRequests = [], isLoading: requestsLoading } = useQuery({
    queryKey: ['consultationRequests', consultantId],
    queryFn: () => consultantId ? getConsultationRequests(consultantId) : Promise.resolve([]),
    enabled: !!consultantId
  });

  const { data: allConsultationsData = [], isLoading: allLoading } = useQuery({
    queryKey: ['consultations', consultantId],
    queryFn: () => consultantId ? getConsultantConsultations(consultantId) : Promise.resolve([]),
    enabled: !!consultantId
  });

  const allConsultations = statusFilter === 'ALL'
    ? allConsultationsData
    : allConsultationsData.filter(c => c.consultStatus === statusFilter);

  const stats = {
    todayConsultations: todayConsultations.length,
    pendingRequests: consultationRequests.filter(r => r.consultStatus === '예약신청').length,
    totalConsultations: allConsultationsData.length,
    completedConsultations: allConsultationsData.filter(c => c.consultStatus === '상담완료').length
  };

  const statusMutation = useMutation({
    mutationFn: ({ consultId, status }: { consultId: string; status: string }) =>
      updateConsultationStatus(consultId, status),
    onSuccess: (data, variables) => {
      queryClient.invalidateQueries({ queryKey: ['todayConsultations'] });
      queryClient.invalidateQueries({ queryKey: ['consultationRequests'] });
      queryClient.invalidateQueries({ queryKey: ['consultations'] });

      if (variables.status === '예약확정') {
        setConfirmedConsultation(data);
        setShowConfirmModal(true);
      } else {
        alert('상담 상태가 변경되었습니다.');
      }
    },
    onError: (error: any) => {
      alert(error.message || '상태 변경에 실패했습니다.');
    }
  });

  const handleStatusChange = (consultationId: string, newStatus: string) => {
    statusMutation.mutate({ consultId: consultationId, status: newStatus });
  };

  const handleAcceptRequest = (requestId: string) => {
    handleStatusChange(requestId, '예약확정');
  };

  const handleRejectRequest = (requestId: string) => {
    if (confirm('정말 거절하시겠습니까?')) {
      handleStatusChange(requestId, '취소');
    }
  };

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
          {}
          <div className="mb-8">
            <h1 className="text-3xl font-hana-bold text-gray-900 mb-3">상담 관리</h1>
            <p className="text-gray-600 font-hana-regular">
              고객 상담 일정을 관리하고 요청을 처리할 수 있습니다.
            </p>

            {}
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

          {}
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

            {}
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
                        {(consultation.consultStatus === '예약확정' || consultation.consultStatus === '상담중') && (
                          <div className="mt-3">
                            <button
                              onClick={() => window.location.href = `/video-call?consultationId=${consultation.consultId}`}
                              className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                            >
                              {consultation.consultStatus === '상담중' ? '상담 재입장' : '상담 시작'}
                            </button>
                          </div>
                        )}
                        {consultation.detail && (
                          <div className="bg-blue-50 border border-blue-200 rounded-lg p-3 mt-3">
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

            {}
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

            {}
            {activeTab === 'all' && (
              <div className="p-6">
                {}
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
                            {(consultation.consultStatus === '예약확정' || consultation.consultStatus === '상담중') && (
                              <>
                                <button
                                  onClick={() => window.location.href = `/video-call?consultationId=${consultation.consultId}`}
                                  className="px-3 py-1 bg-hana-green text-white rounded text-sm hover:bg-green-600 font-hana-medium transition-colors"
                                >
                                  {consultation.consultStatus === '상담중' ? '상담 재입장' : '상담 시작'}
                                </button>
                                <button
                                  onClick={() => handleStatusChange(consultation.consultId, '상담완료')}
                                  disabled={statusMutation.isPending}
                                  className="px-3 py-1 bg-blue-600 text-white rounded text-sm hover:bg-blue-700 font-hana-medium transition-colors disabled:opacity-50"
                                >
                                  완료
                                </button>
                              </>
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

      {}
      {showConfirmModal && confirmedConsultation && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md mx-4">
            {}
            <div className="text-center mb-6">
              <div className="w-20 h-20 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h3 className="text-2xl font-hana-bold text-gray-900 mb-2">상담 확정 완료!</h3>
              <p className="text-gray-600 font-hana-regular">고객에게 예약 확정 알림이 전송되었습니다.</p>
            </div>

            {}
            <div className="bg-gray-50 rounded-xl p-6 mb-6">
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-hana-green rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 font-hana-regular">고객명</p>
                    <p className="text-base font-hana-bold text-gray-900">{confirmedConsultation.customerName}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 font-hana-regular">상담 일시</p>
                    <p className="text-base font-hana-bold text-gray-900">{formatDateTime(confirmedConsultation.reservationDatetime)}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-purple-500 rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 font-hana-regular">상담 유형</p>
                    <p className="text-base font-hana-bold text-gray-900">{getConsultationTypeText(confirmedConsultation.consultType)}</p>
                  </div>
                </div>
              </div>
            </div>

            {}
            <div className="bg-blue-50 border border-blue-200 rounded-xl p-4 mb-6">
              <div className="flex items-start gap-3">
                <svg className="w-5 h-5 text-blue-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm text-blue-800 font-hana-medium">알림 전송 완료</p>
                  <p className="text-xs text-blue-600 font-hana-regular mt-1">
                    고객님에게 상담 정보 메일이 전송되었습니다.
                  </p>
                </div>
              </div>
            </div>

            {}
            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowConfirmModal(false);
                  setConfirmedConsultation(null);
                }}
                className="flex-1 px-6 py-3 bg-hana-green text-white rounded-xl hover:bg-green-600 transition-colors font-hana-bold text-lg"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default ConsultationManagement;