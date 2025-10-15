import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import { getCustomerConsultations, cancelConsultation, type ConsultationResponse } from '../../api/consultationApi';
import { useUserStore } from '../../store/userStore';

function MyConsultations() {
  const { user } = useUserStore();
  const queryClient = useQueryClient();
  const [selectedTab, setSelectedTab] = useState<'all' | 'upcoming' | 'completed' | 'cancelled'>('all');
  const [showCancelModal, setShowCancelModal] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [selectedConsultation, setSelectedConsultation] = useState<ConsultationResponse | null>(null);

  const { data: consultations = [], isLoading, error } = useQuery({
    queryKey: ['myConsultations', user?.userId],
    queryFn: () => {
      if (!user?.userId) throw new Error('로그인이 필요합니다.');
      return getCustomerConsultations(user.userId);
    },
    enabled: !!user?.userId,
  });

  const cancelMutation = useMutation({
    mutationFn: ({ consultId, customerId }: { consultId: string; customerId: number }) =>
      cancelConsultation(consultId, customerId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['myConsultations'] });
      setShowCancelModal(false);
      setShowSuccessModal(true);
    },
    onError: (error: Error) => {
      alert(error.message || '상담 취소에 실패했습니다.');
    },
  });

  const handleCancelConsultation = () => {
    if (!selectedConsultation || !user?.userId) return;

    cancelMutation.mutate({
      consultId: selectedConsultation.consultId,
      customerId: user.userId,
    });
  };

  const filteredConsultations = consultations.filter((consultation) => {
    if (selectedTab === 'all') return true;
    if (selectedTab === 'upcoming') {
      return ['예약신청', '예약확정'].includes(consultation.consultStatus);
    }
    if (selectedTab === 'completed') {
      return consultation.consultStatus === '상담완료';
    }
    if (selectedTab === 'cancelled') {
      return consultation.consultStatus === '취소';
    }
    return true;
  });

  const getConsultationTypeLabel = (type: string) => {
    switch (type) {
      case 'general': return '일반';
      case 'product': return '상품가입';
      case 'asset-management': return '자산관리';
      default: return type;
    }
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case '예약신청': return 'bg-yellow-100 text-yellow-800 border-yellow-300';
      case '예약확정': return 'bg-blue-100 text-blue-800 border-blue-300';
      case '상담중': return 'bg-green-100 text-green-800 border-green-300';
      case '상담완료': return 'bg-gray-100 text-gray-800 border-gray-300';
      case '취소': return 'bg-red-100 text-red-800 border-red-300';
      default: return 'bg-gray-100 text-gray-800 border-gray-300';
    }
  };

  const formatDateTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit',
    });
  };

  const isCancelable = (status: string) => {
    return ['예약신청', '예약확정'].includes(status);
  };

  if (error) {
    return (
      <Layout>
        <div className="flex items-center justify-center min-h-[60vh]">
          <div className="text-center">
            <p className="text-red-500 mb-4">상담 목록을 불러오는데 실패했습니다.</p>
            <button
              onClick={() => window.location.reload()}
              className="px-6 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors"
            >
              다시 시도
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 py-8">
        {}
        <div className="mb-8">
          <h1 className="text-3xl font-hana-bold text-gray-900 mb-2">내 상담</h1>
          <p className="text-gray-600 font-hana-regular">신청한 상담 내역을 확인하고 관리하세요</p>
        </div>

        {}
        <div className="mb-6 border-b border-gray-200">
          <div className="flex gap-2 overflow-x-auto">
            <button
              onClick={() => setSelectedTab('all')}
              className={`px-6 py-3 font-hana-medium whitespace-nowrap transition-colors ${
                selectedTab === 'all'
                  ? 'text-hana-green border-b-2 border-hana-green'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              전체 ({consultations.length})
            </button>
            <button
              onClick={() => setSelectedTab('upcoming')}
              className={`px-6 py-3 font-hana-medium whitespace-nowrap transition-colors ${
                selectedTab === 'upcoming'
                  ? 'text-hana-green border-b-2 border-hana-green'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              예정된 상담 ({consultations.filter(c => ['예약신청', '예약확정'].includes(c.consultStatus)).length})
            </button>
            <button
              onClick={() => setSelectedTab('completed')}
              className={`px-6 py-3 font-hana-medium whitespace-nowrap transition-colors ${
                selectedTab === 'completed'
                  ? 'text-hana-green border-b-2 border-hana-green'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              완료된 상담 ({consultations.filter(c => c.consultStatus === '상담완료').length})
            </button>
            <button
              onClick={() => setSelectedTab('cancelled')}
              className={`px-6 py-3 font-hana-medium whitespace-nowrap transition-colors ${
                selectedTab === 'cancelled'
                  ? 'text-hana-green border-b-2 border-hana-green'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              취소된 상담 ({consultations.filter(c => c.consultStatus === '취소').length})
            </button>
          </div>
        </div>

        {}
        {isLoading ? (
          <div className="flex justify-center items-center py-20">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green"></div>
          </div>
        ) : filteredConsultations.length === 0 ? (
          <div className="text-center py-20">
            <div className="inline-block p-8 bg-gray-50 rounded-full mb-4">
              <svg className="w-16 h-16 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
              </svg>
            </div>
            <p className="text-gray-500 font-hana-medium mb-4">
              {selectedTab === 'all' && '신청한 상담이 없습니다.'}
              {selectedTab === 'upcoming' && '예정된 상담이 없습니다.'}
              {selectedTab === 'completed' && '완료된 상담이 없습니다.'}
              {selectedTab === 'cancelled' && '취소된 상담이 없습니다.'}
            </p>
            {selectedTab === 'all' && (
              <button
                onClick={() => window.location.href = '/consultation/request'}
                className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
              >
                상담 신청하기
              </button>
            )}
          </div>
        ) : (
          <div className="space-y-4">
            {filteredConsultations.map((consultation) => (
              <div
                key={consultation.consultId}
                className="bg-white border border-gray-200 rounded-lg p-6 hover:shadow-md transition-shadow"
              >
                <div className="flex items-start justify-between mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <span className={`px-3 py-1 rounded-full text-sm font-hana-medium border ${getStatusColor(consultation.consultStatus)}`}>
                        {consultation.consultStatus}
                      </span>
                      <span className="px-3 py-1 bg-gray-100 text-gray-700 rounded-full text-sm font-hana-medium">
                        {getConsultationTypeLabel(consultation.consultType)}
                      </span>
                    </div>
                    <h3 className="text-lg font-hana-bold text-gray-900 mb-2">
                      상담 예약 #{consultation.consultId}
                    </h3>
                  </div>
                  <div className="flex gap-2">
                    {(consultation.consultStatus === '예약확정' || consultation.consultStatus === '상담중') && (
                      <button
                        onClick={() => window.location.href = `/video-call?consultationId=${consultation.consultId}`}
                        className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                      >
                        {consultation.consultStatus === '상담중' ? '상담 재입장' : '상담 방 입장'}
                      </button>
                    )}
                    {isCancelable(consultation.consultStatus) && (
                      <button
                        onClick={() => {
                          setSelectedConsultation(consultation);
                          setShowCancelModal(true);
                        }}
                        className="px-4 py-2 text-red-600 border border-red-300 rounded-lg hover:bg-red-50 transition-colors font-hana-medium"
                      >
                        취소
                      </button>
                    )}
                  </div>
                </div>

                <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-sm">
                  <div className="flex items-start gap-2">
                    <svg className="w-5 h-5 text-gray-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                    <div>
                      <p className="text-gray-600 font-hana-regular">담당 상담사</p>
                      <p className="text-gray-900 font-hana-medium">{consultation.consultantName || '미배정'}</p>
                    </div>
                  </div>

                  <div className="flex items-start gap-2">
                    <svg className="w-5 h-5 text-gray-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                    <div>
                      <p className="text-gray-600 font-hana-regular">예약 일시</p>
                      <p className="text-gray-900 font-hana-medium">{formatDateTime(consultation.reservationDatetime)}</p>
                    </div>
                  </div>

                  {consultation.detail && (
                    <div className="md:col-span-2 flex items-start gap-2">
                      <svg className="w-5 h-5 text-gray-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                      </svg>
                      <div>
                        <p className="text-gray-600 font-hana-regular">상담 내용</p>
                        <p className="text-gray-900 font-hana-regular">{consultation.detail}</p>
                      </div>
                    </div>
                  )}

                  {consultation.consultDatetime && (
                    <div className="flex items-start gap-2">
                      <svg className="w-5 h-5 text-gray-400 flex-shrink-0 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <div>
                        <p className="text-gray-600 font-hana-regular">실제 상담 일시</p>
                        <p className="text-gray-900 font-hana-medium">{formatDateTime(consultation.consultDatetime)}</p>
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ))}
          </div>
        )}
      </div>

      {}
      {showCancelModal && selectedConsultation && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-lg shadow-xl p-6 w-full max-w-md">
            <h3 className="text-xl font-hana-bold text-gray-900 mb-4">상담 취소</h3>
            <p className="text-gray-600 font-hana-regular mb-6">
              정말로 이 상담을 취소하시겠습니까?
              <br />
              <span className="text-sm text-gray-500">취소된 상담은 복구할 수 없습니다.</span>
            </p>

            <div className="bg-gray-50 rounded-lg p-4 mb-6">
              <div className="space-y-2 text-sm">
                <div className="flex justify-between">
                  <span className="text-gray-600 font-hana-regular">상담 번호</span>
                  <span className="text-gray-900 font-hana-medium">{selectedConsultation.consultId}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 font-hana-regular">예약 일시</span>
                  <span className="text-gray-900 font-hana-medium">{formatDateTime(selectedConsultation.reservationDatetime)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600 font-hana-regular">상담 유형</span>
                  <span className="text-gray-900 font-hana-medium">{getConsultationTypeLabel(selectedConsultation.consultType)}</span>
                </div>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowCancelModal(false);
                  setSelectedConsultation(null);
                }}
                className="flex-1 px-4 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-hana-medium"
                disabled={cancelMutation.isPending}
              >
                돌아가기
              </button>
              <button
                onClick={handleCancelConsultation}
                className="flex-1 px-4 py-3 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors font-hana-medium disabled:opacity-50"
                disabled={cancelMutation.isPending}
              >
                {cancelMutation.isPending ? '취소 중...' : '취소하기'}
              </button>
            </div>
          </div>
        </div>
      )}

      {}
      {showSuccessModal && selectedConsultation && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 px-4">
          <div className="bg-white rounded-2xl shadow-2xl p-8 w-full max-w-md mx-4">
            {}
            <div className="text-center mb-6">
              <div className="w-20 h-20 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                </svg>
              </div>
              <h3 className="text-2xl font-hana-bold text-gray-900 mb-2">상담 취소 완료</h3>
              <p className="text-gray-600 font-hana-regular">상담이 성공적으로 취소되었습니다.</p>
            </div>

            {}
            <div className="bg-gray-50 rounded-xl p-6 mb-6">
              <div className="space-y-3">
                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-red-500 rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 20l4-16m2 16l4-16M6 9h14M4 15h14" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 font-hana-regular">상담 번호</p>
                    <p className="text-base font-hana-bold text-gray-900">{selectedConsultation.consultId}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-blue-500 rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 font-hana-regular">취소된 상담 일시</p>
                    <p className="text-base font-hana-bold text-gray-900">{formatDateTime(selectedConsultation.reservationDatetime)}</p>
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
                    <p className="text-base font-hana-bold text-gray-900">{getConsultationTypeLabel(selectedConsultation.consultType)}</p>
                  </div>
                </div>

                <div className="flex items-center gap-3">
                  <div className="w-8 h-8 bg-hana-green rounded-full flex items-center justify-center">
                    <svg className="w-4 h-4 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                    </svg>
                  </div>
                  <div>
                    <p className="text-sm text-gray-600 font-hana-regular">담당 상담사</p>
                    <p className="text-base font-hana-bold text-gray-900">{selectedConsultation.consultantName || '미배정'}</p>
                  </div>
                </div>
              </div>
            </div>

            {}
            <div className="bg-orange-50 border border-orange-200 rounded-xl p-4 mb-6">
              <div className="flex items-start gap-3">
                <svg className="w-5 h-5 text-orange-600 mt-0.5 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
                <div>
                  <p className="text-sm text-orange-800 font-hana-medium">취소 처리 완료</p>
                  <p className="text-xs text-orange-600 font-hana-regular mt-1">
                    상담사에게 취소 알림이 전송되고, 상담사의 일정에서도 자동으로 삭제되었습니다.
                  </p>
                </div>
              </div>
            </div>

            {}
            <div className="flex gap-3">
              <button
                onClick={() => {
                  setShowSuccessModal(false);
                  setSelectedConsultation(null);
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

export default MyConsultations;