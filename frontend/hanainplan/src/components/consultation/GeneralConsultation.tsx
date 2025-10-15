import React from 'react';
import NotesTab from './NotesTab';

interface GeneralConsultationProps {
  consultationInfo: {
    id?: string;
    type: string;
    detail?: string;
  };
  currentUserId: number;
  currentUserRole: 'customer' | 'counselor';
  targetUserId: number;
  isInCall: boolean;
}

const GeneralConsultation: React.FC<GeneralConsultationProps> = ({
  consultationInfo,
  currentUserId,
  currentUserRole,
  targetUserId,
  isInCall
}) => {
  return (
    <div className="bg-white rounded-xl shadow-lg p-6 h-full flex flex-col overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100 hover:scrollbar-thumb-gray-400">
      <div className="flex items-center gap-3 mb-6">
        <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center">
          <span className="text-2xl">💬</span>
        </div>
        <div>
          <h2 className="text-2xl font-bold text-gray-900">일반 상담</h2>
          <p className="text-sm text-gray-600">고객 요청사항 및 상담 기록</p>
        </div>
      </div>

      {}
      {consultationInfo.detail && (
        <div className="mb-6">
          <h3 className="text-lg font-bold text-gray-900 mb-3 flex items-center">
            <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
            고객 요청 사항
          </h3>
          <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
            <p className="text-gray-800 whitespace-pre-wrap">{consultationInfo.detail}</p>
          </div>
        </div>
      )}

      {}
      <div className="flex-1 flex flex-col">
        <h3 className="text-lg font-bold text-gray-900 mb-3 flex items-center">
          <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
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
          <div className="mt-3 p-3 bg-blue-50 rounded-lg">
            <p className="text-sm text-blue-800">
              ✅ 상담 진행 중 - 실시간 기록 가능
            </p>
          </div>
        )}
      </div>
    </div>
  );
};

export default GeneralConsultation;