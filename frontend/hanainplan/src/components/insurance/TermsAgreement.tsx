import React, { useState } from 'react';
import type { InsuranceProduct, AgreementInfo } from '../../types/insurance';

interface TermsAgreementProps {
  selectedProduct: InsuranceProduct;
  onNext: (agreementInfo: AgreementInfo) => void;
  onPrevious: () => void;
}

const TermsAgreement: React.FC<TermsAgreementProps> = ({ selectedProduct, onNext, onPrevious }) => {
  const [agreements, setAgreements] = useState({
    termsAgreed: false,
    privacyAgreed: false,
    marketingAgreed: false,
    medicalDisclosureAgreed: false
  });
  const [expandedSections, setExpandedSections] = useState<Set<string>>(new Set());

  const toggleSection = (section: string) => {
    const newExpanded = new Set(expandedSections);
    if (newExpanded.has(section)) {
      newExpanded.delete(section);
    } else {
      newExpanded.add(section);
    }
    setExpandedSections(newExpanded);
  };

  const handleAgreementChange = (key: keyof typeof agreements) => {
    setAgreements(prev => ({
      ...prev,
      [key]: !prev[key]
    }));
  };

  const handleNext = () => {
    const agreementInfo: AgreementInfo = {
      ...agreements,
      agreementDate: new Date().toISOString()
    };
    onNext(agreementInfo);
  };

  const canProceed = agreements.termsAgreed && agreements.privacyAgreed && agreements.medicalDisclosureAgreed;

  const termsSections = [
    {
      id: 'general-terms',
      title: '보험약관',
      content: `
        제1조 (목적)
        이 약관은 ${selectedProduct.name} 보험계약의 체결, 이행 및 해지에 관한 사항을 정함을 목적으로 합니다.

        제2조 (보험계약의 성립)
        1. 보험계약은 보험계약자가 청약하고 보험회사가 승낙함으로써 성립됩니다.
        2. 보험회사는 보험계약자의 청약을 접수한 때부터 30일 이내에 승낙 여부를 결정합니다.

        제3조 (보험료의 납입)
        1. 보험료는 보험계약자가 약정한 방법에 따라 납입합니다.
        2. 보험료를 납입하지 않은 경우 보험계약의 효력이 정지됩니다.
      `
    },
    {
      id: 'privacy-policy',
      title: '개인정보 처리방침',
      content: `
        제1조 (개인정보의 수집 및 이용)
        1. 보험회사는 보험계약 체결 및 이행을 위해 다음과 같은 개인정보를 수집합니다.
           - 성명, 주민등록번호, 주소, 연락처
           - 직업, 소득정보, 건강상태
           - 가족관계, 의료기록

        2. 수집된 개인정보는 다음 목적으로 이용됩니다.
           - 보험계약 체결 및 이행
           - 보험금 지급 및 사후관리
           - 보험상품 개발 및 개선
      `
    },
    {
      id: 'medical-disclosure',
      title: '의료정보 제공 동의',
      content: `
        제1조 (의료정보 수집 및 이용)
        1. 보험회사는 보험계약 체결 시 의료정보를 수집할 수 있습니다.
        2. 수집 대상 의료정보:
           - 질병 및 상해 이력
           - 수술 및 입원 이력
           - 현재 복용 중인 약물
           - 가족력
      `
    },
    {
      id: 'marketing-terms',
      title: '마케팅 정보 수신 동의 (선택)',
      content: `
        제1조 (마케팅 정보 수신)
        1. 보험회사는 보험계약자에게 다음과 같은 마케팅 정보를 제공할 수 있습니다.
           - 새로운 보험상품 안내
           - 보험 관련 정보 및 소식
           - 할인 혜택 및 이벤트 안내
      `
    }
  ];

  return (
    <div className="space-y-4">
      {}
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-800 mb-2">약관 및 동의</h1>
        <p className="text-gray-600">보험 가입에 필요한 약관을 확인하고 동의해주세요</p>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-3">선택된 보험 상품</h2>
        <div className="flex items-center justify-between p-3 bg-blue-50 rounded-lg">
          <div>
            <h3 className="font-bold text-gray-800">{selectedProduct.name}</h3>
            <p className="text-gray-600 text-sm">{selectedProduct.category}</p>
          </div>
          <span className="px-2 py-1 bg-blue-600 text-white text-xs font-medium rounded-full">
            {selectedProduct.category}
          </span>
        </div>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">약관 및 동의사항</h2>

        <div className="space-y-3">
          {termsSections.map((section) => (
            <div key={section.id} className="border border-gray-200 rounded-lg">
              <button
                onClick={() => toggleSection(section.id)}
                className="w-full p-3 text-left flex items-center justify-between hover:bg-gray-50"
              >
                <h3 className="font-semibold text-gray-800 text-sm">{section.title}</h3>
                <svg
                  className={`w-4 h-4 text-gray-500 transform transition-transform ${
                    expandedSections.has(section.id) ? 'rotate-180' : ''
                  }`}
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </button>

              {expandedSections.has(section.id) && (
                <div className="px-3 pb-3">
                  <div className="bg-gray-50 rounded-lg p-3 max-h-40 overflow-y-auto">
                    <pre className="text-xs text-gray-700 whitespace-pre-wrap font-sans">
                      {section.content}
                    </pre>
                  </div>
                </div>
              )}
            </div>
          ))}
        </div>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">필수 동의사항</h2>

        <div className="space-y-3">
          <label className="flex items-start space-x-3 cursor-pointer">
            <input
              type="checkbox"
              checked={agreements.termsAgreed}
              onChange={() => handleAgreementChange('termsAgreed')}
              className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <div>
              <span className="font-medium text-gray-800 text-sm">보험약관에 동의합니다 (필수)</span>
              <p className="text-xs text-gray-600 mt-1">보험계약의 기본 약관에 동의합니다.</p>
            </div>
          </label>

          <label className="flex items-start space-x-3 cursor-pointer">
            <input
              type="checkbox"
              checked={agreements.privacyAgreed}
              onChange={() => handleAgreementChange('privacyAgreed')}
              className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <div>
              <span className="font-medium text-gray-800 text-sm">개인정보 처리방침에 동의합니다 (필수)</span>
              <p className="text-xs text-gray-600 mt-1">개인정보 수집, 이용, 제공에 동의합니다.</p>
            </div>
          </label>

          <label className="flex items-start space-x-3 cursor-pointer">
            <input
              type="checkbox"
              checked={agreements.medicalDisclosureAgreed}
              onChange={() => handleAgreementChange('medicalDisclosureAgreed')}
              className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <div>
              <span className="font-medium text-gray-800 text-sm">의료정보 제공에 동의합니다 (필수)</span>
              <p className="text-xs text-gray-600 mt-1">보험 가입을 위한 의료정보 수집에 동의합니다.</p>
            </div>
          </label>

          <label className="flex items-start space-x-3 cursor-pointer">
            <input
              type="checkbox"
              checked={agreements.marketingAgreed}
              onChange={() => handleAgreementChange('marketingAgreed')}
              className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
            />
            <div>
              <span className="font-medium text-gray-800 text-sm">마케팅 정보 수신에 동의합니다 (선택)</span>
              <p className="text-xs text-gray-600 mt-1">보험 상품 및 서비스 안내를 받습니다.</p>
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
          onClick={handleNext}
          disabled={!canProceed}
          className={`px-6 py-2 rounded-lg font-semibold transition-colors text-sm ${
            canProceed
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
          }`}
        >
          다음 단계
        </button>
      </div>
    </div>
  );
};

export default TermsAgreement;