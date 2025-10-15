import React, { useState } from 'react';
import { submitInsuranceApplication } from '../../api/insuranceApi';
import type {
  InsuranceProduct,
  InsuranceApplication,
  PersonalInfo,
  InsuranceDetails,
  PremiumCalculationResponse,
  PaymentInfo
} from '../../types/insurance';

interface FinalConfirmationProps {
  selectedProduct: InsuranceProduct;
  application: Partial<InsuranceApplication>;
  premiumCalculation: PremiumCalculationResponse;
  onNext: (applicationId: string, policyNumber?: string) => void;
  onPrevious: () => void;
}

const FinalConfirmation: React.FC<FinalConfirmationProps> = ({
  selectedProduct,
  application,
  premiumCalculation,
  onNext,
  onPrevious
}) => {
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [finalAgreement, setFinalAgreement] = useState(false);
  const [error, setError] = useState<string | null>(null);

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
      day: 'numeric'
    });
  };

  const handleSubmit = async () => {
    if (!selectedProduct || !application || !premiumCalculation) {
      setError('필수 정보가 누락되었습니다.');
      return;
    }

    setIsSubmitting(true);
    setError(null);

    try {
      const paymentInfo: PaymentInfo = {
        paymentMethod: 'BANK_TRANSFER',
        bankAccount: {
          bankCode: '004',
          accountNumber: '1234567890',
          accountHolder: application.applicantInfo?.name || ''
        },
        autoTransfer: true,
        transferDate: 15
      };

      const finalApplication: InsuranceApplication = {
        productId: selectedProduct.id,
        applicantInfo: application.applicantInfo!,
        beneficiaryInfo: application.applicantInfo!,
        insuranceDetails: application.insuranceDetails!,
        paymentInfo: paymentInfo,
        agreementInfo: {
          ...application.agreementInfo!,
          finalAgreement: finalAgreement
        },
        status: 'SUBMITTED',
        applicationDate: new Date().toISOString()
      };

      const result = await submitInsuranceApplication(finalApplication);

      if (result.success) {
        onNext(result.applicationId, result.policyNumber);
      } else {
        setError('보험 가입 신청에 실패했습니다.');
      }
    } catch (error) {
      setError('보험 가입 신청 중 오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!selectedProduct || !application || !premiumCalculation) {
    return (
      <div className="text-center py-8">
        <p className="text-red-600 mb-4">필수 정보가 누락되었습니다.</p>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {}
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-800 mb-2">최종 확인</h1>
        <p className="text-gray-600">입력하신 정보를 최종 확인해주세요</p>
      </div>

      {}
      {error && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3">
          <p className="text-red-600 text-sm">{error}</p>
        </div>
      )}

      <div className="space-y-4">
        {}
        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="text-lg font-semibold text-gray-800 mb-3">선택된 보험 상품</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h3 className="font-bold text-gray-800 mb-1">{selectedProduct.name}</h3>
              <p className="text-gray-600 text-sm mb-2">{selectedProduct.category}</p>
              <p className="text-xs text-gray-600">{selectedProduct.description}</p>
            </div>
            <div className="bg-blue-50 rounded-lg p-3">
              <h4 className="font-semibold text-gray-800 mb-2 text-sm">보장 내용</h4>
              <ul className="space-y-1 text-xs text-gray-600">
                {selectedProduct.benefits.slice(0, 3).map((benefit, index) => (
                  <li key={index} className="flex items-center">
                    <svg className="w-3 h-3 text-green-500 mr-1" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                    </svg>
                    {benefit}
                  </li>
                ))}
              </ul>
            </div>
          </div>
        </div>

        {}
        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="text-lg font-semibold text-gray-800 mb-3">보험료 정보</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h3 className="font-semibold text-gray-800 mb-2 text-sm">가입 조건</h3>
              <div className="space-y-1 text-xs">
                <div className="flex justify-between">
                  <span className="text-gray-600">가입금액:</span>
                  <span className="font-medium">{formatCurrency(application.insuranceDetails!.coverageAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">납입기간:</span>
                  <span className="font-medium">{application.insuranceDetails!.paymentPeriod}년</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">보장기간:</span>
                  <span className="font-medium">{application.insuranceDetails!.coveragePeriod}년</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">납입주기:</span>
                  <span className="font-medium">
                    {application.insuranceDetails!.paymentFrequency === 'MONTHLY' ? '월납' :
                     application.insuranceDetails!.paymentFrequency === 'QUARTERLY' ? '분기납' :
                     application.insuranceDetails!.paymentFrequency === 'SEMI_ANNUAL' ? '반기납' : '연납'}
                  </span>
                </div>
              </div>
            </div>

            <div>
              <h3 className="font-semibold text-gray-800 mb-2 text-sm">보험료 내역</h3>
              <div className="space-y-1 text-xs">
                <div className="flex justify-between">
                  <span className="text-gray-600">기본 보험료:</span>
                  <span className="font-medium">{formatCurrency(premiumCalculation.basePremium)}</span>
                </div>
                {premiumCalculation.riderPremium > 0 && (
                  <div className="flex justify-between">
                    <span className="text-gray-600">특약 보험료:</span>
                    <span className="font-medium">+{formatCurrency(premiumCalculation.riderPremium)}</span>
                  </div>
                )}
                {premiumCalculation.discount > 0 && (
                  <div className="flex justify-between">
                    <span className="text-gray-600">할인:</span>
                    <span className="font-medium text-green-600">-{formatCurrency(premiumCalculation.discount)}</span>
                  </div>
                )}
                <div className="border-t pt-1 mt-1">
                  <div className="flex justify-between">
                    <span className="font-semibold text-gray-800">최종 보험료:</span>
                    <span className="font-bold text-blue-600">{formatCurrency(premiumCalculation.finalPremium)}</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {}
        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="text-lg font-semibold text-gray-800 mb-3">개인정보</h2>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <h3 className="font-semibold text-gray-800 mb-2 text-sm">기본 정보</h3>
              <div className="space-y-1 text-xs">
                <div className="flex justify-between">
                  <span className="text-gray-600">이름:</span>
                  <span className="font-medium">{application.applicantInfo!.name}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">성별:</span>
                  <span className="font-medium">{application.applicantInfo!.gender === 'M' ? '남성' : '여성'}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">생년월일:</span>
                  <span className="font-medium">{formatDate(application.applicantInfo!.birthDate)}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">휴대폰:</span>
                  <span className="font-medium">{application.applicantInfo!.phoneNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">이메일:</span>
                  <span className="font-medium">{application.applicantInfo!.email}</span>
                </div>
              </div>
            </div>

            <div>
              <h3 className="font-semibold text-gray-800 mb-2 text-sm">추가 정보</h3>
              <div className="space-y-1 text-xs">
                <div className="flex justify-between">
                  <span className="text-gray-600">직업:</span>
                  <span className="font-medium">{application.applicantInfo!.occupation}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">결혼상태:</span>
                  <span className="font-medium">
                    {application.applicantInfo!.maritalStatus === 'SINGLE' ? '미혼' :
                     application.applicantInfo!.maritalStatus === 'MARRIED' ? '기혼' :
                     application.applicantInfo!.maritalStatus === 'DIVORCED' ? '이혼' : '사별'}
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-gray-600">주소:</span>
                  <span className="font-medium text-right">
                    {application.applicantInfo!.address.address1}<br/>
                    {application.applicantInfo!.address.address2 &&
                      `${application.applicantInfo!.address.address2}`}
                  </span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {}
        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="text-lg font-semibold text-gray-800 mb-3">동의사항</h2>

          <div className="space-y-2">
            <div className="flex items-center space-x-2">
              <div className={`w-4 h-4 rounded flex items-center justify-center ${
                application.agreementInfo!.termsAgreed ? 'bg-green-500' : 'bg-gray-300'
              }`}>
                {application.agreementInfo!.termsAgreed && (
                  <svg className="w-2 h-2 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                )}
              </div>
              <span className="text-sm">보험약관에 동의</span>
            </div>

            <div className="flex items-center space-x-2">
              <div className={`w-4 h-4 rounded flex items-center justify-center ${
                application.agreementInfo!.privacyAgreed ? 'bg-green-500' : 'bg-gray-300'
              }`}>
                {application.agreementInfo!.privacyAgreed && (
                  <svg className="w-2 h-2 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                )}
              </div>
              <span className="text-sm">개인정보 처리방침에 동의</span>
            </div>

            <div className="flex items-center space-x-2">
              <div className={`w-4 h-4 rounded flex items-center justify-center ${
                application.agreementInfo!.medicalDisclosureAgreed ? 'bg-green-500' : 'bg-gray-300'
              }`}>
                {application.agreementInfo!.medicalDisclosureAgreed && (
                  <svg className="w-2 h-2 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                )}
              </div>
              <span className="text-sm">의료정보 제공에 동의</span>
            </div>

            <div className="flex items-center space-x-2">
              <div className={`w-4 h-4 rounded flex items-center justify-center ${
                application.agreementInfo!.marketingAgreed ? 'bg-green-500' : 'bg-gray-300'
              }`}>
                {application.agreementInfo!.marketingAgreed && (
                  <svg className="w-2 h-2 text-white" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                  </svg>
                )}
              </div>
              <span className="text-sm">마케팅 정보 수신에 동의</span>
            </div>
          </div>
        </div>

        {}
        <div className="bg-white rounded-lg shadow p-4">
          <h2 className="text-lg font-semibold text-gray-800 mb-3">최종 동의</h2>

          <label className="flex items-start space-x-2 cursor-pointer">
            <input
              type="checkbox"
              checked={finalAgreement}
              onChange={(e) => setFinalAgreement(e.target.checked)}
              className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <div>
              <span className="font-medium text-gray-800 text-sm">
                위 모든 내용을 확인했으며, 보험 가입을 신청합니다. (필수)
              </span>
              <p className="text-xs text-gray-600 mt-1">
                보험 가입 신청 후에는 철회가 제한될 수 있으며,
                허위 정보 제공 시 보험계약이 무효가 될 수 있습니다.
              </p>
            </div>
          </label>
        </div>
      </div>

      {}
      <div className="flex justify-between">
        <button
          onClick={onPrevious}
          className="px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors text-sm"
        >
          이전 단계
        </button>
        <button
          onClick={handleSubmit}
          disabled={!finalAgreement || isSubmitting}
          className={`px-6 py-2 rounded-lg font-semibold transition-colors text-sm ${
            finalAgreement && !isSubmitting
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
          }`}
        >
          {isSubmitting ? '가입 신청 중...' : '보험 가입 신청'}
        </button>
      </div>
    </div>
  );
};

export default FinalConfirmation;