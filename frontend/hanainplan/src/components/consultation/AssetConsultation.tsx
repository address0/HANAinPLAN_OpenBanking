import React from 'react';
import { useQuery } from '@tanstack/react-query';
import NotesTab from './NotesTab';
import { getPortfolio } from '../../api/portfolioApi';

interface AssetConsultationProps {
  consultationInfo: {
    id?: string;
    type: string;
    detail?: string;
  };
  customerId: number;
  currentUserId: number;
  currentUserRole: 'customer' | 'counselor';
  targetUserId: number;
  isInCall: boolean;
}

const AssetConsultation: React.FC<AssetConsultationProps> = ({
  consultationInfo,
  customerId,
  currentUserId,
  currentUserRole,
  targetUserId,
  isInCall
}) => {
  const { data: portfolioData, isLoading } = useQuery({
    queryKey: ['portfolio', customerId],
    queryFn: () => getPortfolio(customerId),
    enabled: !!customerId
  });

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 h-full flex flex-col overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100 hover:scrollbar-thumb-gray-400">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 bg-purple-100 rounded-full flex items-center justify-center">
          <span className="text-2xl">💰</span>
        </div>
        <div>
          <h2 className="text-2xl font-bold text-gray-900">자산 관리 상담</h2>
          <p className="text-sm text-gray-600">고객 포트폴리오 및 자산 현황</p>
        </div>
      </div>

      {}
      {consultationInfo.detail && (
        <div className="mb-4">
          <div className="inline-flex items-center bg-purple-100 text-purple-800 px-3 py-1 rounded-full text-sm font-medium">
            {consultationInfo.detail}
          </div>
        </div>
      )}

      {}
      <div className="mb-6">
        <h3 className="text-lg font-bold text-gray-900 mb-3 flex items-center">
          <svg className="w-5 h-5 mr-2 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
          </svg>
          고객 자산 현황
        </h3>

        {isLoading ? (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-8 text-center">
            <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-purple-600 mx-auto mb-2"></div>
            <p className="text-gray-600 text-sm">포트폴리오 정보를 불러오는 중...</p>
          </div>
        ) : portfolioData ? (
          <div className="space-y-3">
            {}
            <div className="bg-purple-50 border border-purple-200 rounded-lg p-4">
              <div className="flex justify-between items-center">
                <span className="text-gray-700 font-medium">총 자산</span>
                <span className="text-2xl font-bold text-purple-900">
                  {portfolioData.totalAssets?.toLocaleString() || '0'}원
                </span>
              </div>
            </div>

            {}
            <div className="grid grid-cols-2 gap-3">
              {portfolioData.deposits && (
                <div className="bg-blue-50 border border-blue-200 rounded-lg p-3">
                  <p className="text-xs text-gray-600 mb-1">예금/적금</p>
                  <p className="text-lg font-bold text-blue-900">
                    {portfolioData.deposits.toLocaleString()}원
                  </p>
                </div>
              )}
              {portfolioData.funds && (
                <div className="bg-green-50 border border-green-200 rounded-lg p-3">
                  <p className="text-xs text-gray-600 mb-1">펀드</p>
                  <p className="text-lg font-bold text-green-900">
                    {portfolioData.funds.toLocaleString()}원
                  </p>
                </div>
              )}
              {portfolioData.irp && (
                <div className="bg-orange-50 border border-orange-200 rounded-lg p-3">
                  <p className="text-xs text-gray-600 mb-1">IRP</p>
                  <p className="text-lg font-bold text-orange-900">
                    {portfolioData.irp.toLocaleString()}원
                  </p>
                </div>
              )}
              {portfolioData.insurance && (
                <div className="bg-pink-50 border border-pink-200 rounded-lg p-3">
                  <p className="text-xs text-gray-600 mb-1">보험</p>
                  <p className="text-lg font-bold text-pink-900">
                    {portfolioData.insurance.toLocaleString()}원
                  </p>
                </div>
              )}
            </div>
          </div>
        ) : (
          <div className="bg-gray-50 border border-gray-200 rounded-lg p-8 text-center">
            <svg className="w-12 h-12 text-gray-400 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            <p className="text-gray-600 text-sm">포트폴리오 정보가 없습니다</p>
          </div>
        )}
      </div>

      {}
      <div className="flex-1 flex flex-col">
        <h3 className="text-lg font-bold text-gray-900 mb-3 flex items-center">
          <svg className="w-5 h-5 mr-2 text-purple-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
          </svg>
          상담 기록
        </h3>

        <div className="flex-1">
          <NotesTab
            consultationId={consultationInfo.id || ''}
            currentUserId={currentUserId}
            currentUserRole={currentUserRole}
            targetUserId={targetUserId}
          />
        </div>

        {isInCall && (
          <div className="mt-3 p-3 bg-purple-50 rounded-lg">
            <p className="text-sm text-purple-800">
              ✅ 상담 진행 중 - 자산 관리 전략 및 조언 기록 가능
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default AssetConsultation;