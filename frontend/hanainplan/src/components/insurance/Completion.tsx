import React from 'react';
import type {
  InsuranceProduct,
  InsuranceApplication,
  PremiumCalculationResponse
} from '../../types/insurance';

interface CompletionProps {
  selectedProduct: InsuranceProduct;
  application: Partial<InsuranceApplication>;
  premiumCalculation: PremiumCalculationResponse;
  applicationId: string;
  policyNumber?: string;
  onNewApplication: () => void;
  onGoHome: () => void;
}

const Completion: React.FC<CompletionProps> = ({
  selectedProduct,
  application,
  premiumCalculation,
  applicationId,
  policyNumber,
  onNewApplication,
  onGoHome
}) => {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0
    }).format(amount);
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: 'long',
      day: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="space-y-4">
      {}
      <div className="text-center">
        <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
          <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <h1 className="text-2xl font-bold text-gray-800 mb-2">보험 가입 완료!</h1>
        <p className="text-gray-600">보험 가입이 성공적으로 완료되었습니다</p>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-3">가입 완료 정보</h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {}
          <div>
            <h3 className="font-semibold text-gray-800 mb-2 text-sm">보험 상품</h3>
            <div className="space-y-1 text-xs">
              <div className="flex justify-between">
                <span className="text-gray-600">상품명:</span>
                <span className="font-medium">{selectedProduct.name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">카테고리:</span>
                <span className="font-medium">{selectedProduct.category}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">가입금액:</span>
                <span className="font-medium">{formatCurrency(application.insuranceDetails?.coverageAmount || 0)}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">월 보험료:</span>
                <span className="font-medium text-blue-600">{formatCurrency(premiumCalculation.finalPremium)}</span>
              </div>
            </div>
          </div>

          {}
          <div>
            <h3 className="font-semibold text-gray-800 mb-2 text-sm">가입자 정보</h3>
            <div className="space-y-1 text-xs">
              <div className="flex justify-between">
                <span className="text-gray-600">이름:</span>
                <span className="font-medium">{application.applicantInfo?.name}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">휴대폰:</span>
                <span className="font-medium">{application.applicantInfo?.phoneNumber}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">이메일:</span>
                <span className="font-medium">{application.applicantInfo?.email}</span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600">가입일:</span>
                <span className="font-medium">{formatDate(new Date().toISOString())}</span>
              </div>
            </div>
          </div>
        </div>
      </div>

      {}
      <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-3">보험증 정보</h2>

        <div className="bg-white rounded-lg p-4 border-2 border-dashed border-blue-300">
          <div className="text-center">
            <div className="w-12 h-12 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-3">
              <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
              </svg>
            </div>

            {policyNumber ? (
              <>
                <h3 className="font-bold text-gray-800 mb-1">보험증번호</h3>
                <p className="text-lg font-mono font-bold text-blue-600 mb-2">{policyNumber}</p>
                <p className="text-xs text-gray-600 mb-3">
                  보험증은 이메일로 발송되며, 모바일 앱에서도 확인하실 수 있습니다.
                </p>
                <button
                  onClick={() => window.print()}
                  className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors text-sm"
                >
                  보험증 인쇄
                </button>
              </>
            ) : (
              <>
                <h3 className="font-bold text-gray-800 mb-1">신청 번호</h3>
                <p className="text-lg font-mono font-bold text-blue-600 mb-2">{applicationId}</p>
                <p className="text-xs text-gray-600 mb-3">
                  보험 가입 심사가 완료되면 보험증번호가 발급됩니다.
                  심사 결과는 3-5영업일 내에 연락드리겠습니다.
                </p>
              </>
            )}
          </div>
        </div>
      </div>

      {}
      <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4">
        <h3 className="font-semibold text-yellow-800 mb-2 text-sm flex items-center">
          <svg className="w-4 h-4 mr-1" fill="currentColor" viewBox="0 0 20 20">
            <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
          </svg>
          중요 안내사항
        </h3>
        <ul className="space-y-1 text-xs text-yellow-700">
          <li className="flex items-start">
            <span className="mr-1">•</span>
            <span>첫 보험료 납입일은 가입일로부터 30일 이내입니다.</span>
          </li>
          <li className="flex items-start">
            <span className="mr-1">•</span>
            <span>보험료를 납입하지 않으면 보험계약의 효력이 정지됩니다.</span>
          </li>
          <li className="flex items-start">
            <span className="mr-1">•</span>
            <span>보험 가입 후 15일 이내에는 계약 철회가 가능합니다.</span>
          </li>
          <li className="flex items-start">
            <span className="mr-1">•</span>
            <span>보험 사고 발생 시 즉시 고객센터(1588-0000)로 연락해주세요.</span>
          </li>
          <li className="flex items-start">
            <span className="mr-1">•</span>
            <span>개인정보 변경 시 보험회사에 신고해야 합니다.</span>
          </li>
        </ul>
      </div>

      {}
      <div className="bg-blue-50 border border-blue-200 rounded-lg p-4">
        <h3 className="font-semibold text-blue-800 mb-2 text-sm">고객센터 안내</h3>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-xs text-blue-700">
          <div>
            <p className="font-medium mb-1">보험 가입 문의</p>
            <p>전화: 1588-0000 (평일 09:00-18:00)</p>
            <p>이메일: insurance@hana.com</p>
          </div>
          <div>
            <p className="font-medium mb-1">보험금 지급 문의</p>
            <p>전화: 1588-1111 (24시간)</p>
            <p>이메일: claim@hana.com</p>
          </div>
        </div>
      </div>

      {}
      <div className="flex flex-col sm:flex-row gap-3 justify-center">
        <button
          onClick={onGoHome}
          className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors font-medium text-sm"
        >
          홈으로 이동
        </button>
        <button
          onClick={onNewApplication}
          className="px-6 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors font-medium text-sm"
        >
          다른 보험 가입하기
        </button>
      </div>
    </div>
  );
};

export default Completion;