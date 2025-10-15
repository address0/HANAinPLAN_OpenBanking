import React, { useState } from 'react';
import { useInsuranceStore } from '../../store/insuranceStore';

interface LocalInsuranceProduct {
  id: string;
  name: string;
  category: string;
  premium: number;
  coverage: string;
  description: string;
  terms: string[];
  benefits: string[];
  exclusions: string[];
}

interface HighlightInfo {
  id: string;
  text: string;
  blockId: string;
  startIndex: number;
  endIndex: number;
  color: string;
  author: 'counselor' | 'customer';
}

interface InsuranceDashboardProps {
  selectedProduct: LocalInsuranceProduct | null;
  highlights: HighlightInfo[];
  onHighlightAdd: (highlight: Omit<HighlightInfo, 'id'>) => void;
  onHighlightRemove: (highlightId: string) => void;
  currentStep: number;
  onStepChange: (step: number) => void;
  userRole: 'counselor' | 'customer';
}

const InsuranceDashboard: React.FC<InsuranceDashboardProps> = ({
  selectedProduct,
  highlights,
  onHighlightAdd,
  currentStep,
  onStepChange,
  userRole
}) => {
  const [selectedText, setSelectedText] = useState<string>('');
  const [selectionRange, setSelectionRange] = useState<{start: number, end: number, blockId: string} | null>(null);

  const [isInsuranceApplicationMode, setIsInsuranceApplicationMode] = useState<boolean>(false);
  const {
    currentStep: insuranceStep,
    setCurrentStep: setInsuranceStep,
    selectedProduct: storeSelectedProduct,
    setSelectedProduct,
    setAgreementInfo,
    setPersonalInfo,
    setInsuranceDetails,
    setPremiumCalculation,
    resetApplication
  } = useInsuranceStore();

  const insuranceProducts: LocalInsuranceProduct[] = [
    {
      id: '1',
      name: '하나 생명보험',
      category: '생명보험',
      premium: 50000,
      coverage: '사망보험금, 완전장해보험금, 만기보험금 지급',
      description: '안정적인 생명보장과 저축기능을 함께 제공하는 생명보험입니다.',
      terms: [
        '보험계약은 보험계약자가 청약하고 보험회사가 승낙함으로써 성립됩니다.',
        '보험료는 보험계약자가 약정한 방법에 따라 납입합니다.',
        '보험금 지급은 보험사고 발생일로부터 30일 이내에 처리합니다.'
      ],
      benefits: [
        '사고 발생 시 즉시 보험금 지급',
        '의료비 전액 보장',
        '재해 시 특별 보험금 지급',
        '만기 시 원금 보장'
      ],
      exclusions: [
        '자살 또는 자해행위',
        '전쟁, 테러, 폭동',
        '핵폭발, 방사능 오염',
        '자연재해로 인한 손해'
      ]
    },
    {
      id: '2',
      name: '하나 종합보험',
      category: '종합보험',
      premium: 75000,
      coverage: '사망보험금, 상해보험금, 질병보험금, 입원보험금 지급',
      description: '다양한 위험에 대한 종합적인 보장을 제공하는 보험입니다.',
      terms: [
        '보험계약은 보험계약자가 청약하고 보험회사가 승낙함으로써 성립됩니다.',
        '보험료는 보험계약자가 약정한 방법에 따라 납입합니다.',
        '보험금 지급은 보험사고 발생일로부터 30일 이내에 처리합니다.'
      ],
      benefits: [
        '사고 발생 시 즉시 보험금 지급',
        '의료비 전액 보장',
        '재해 시 특별 보험금 지급',
        '만기 시 원금 보장'
      ],
      exclusions: [
        '자살 또는 자해행위',
        '전쟁, 테러, 폭동',
        '핵폭발, 방사능 오염',
        '자연재해로 인한 손해'
      ]
    }
  ];

  const consultationSteps = [
    '상품 선택',
    '약관 확인',
    '보험료 계산',
    '개인정보 입력',
    '최종 확인',
    '가입 완료'
  ];

  const selectedProductData = selectedProduct || insuranceProducts[0];

  const handleStartInsuranceApplication = () => {
    setIsInsuranceApplicationMode(true);
    setInsuranceStep(0);
  };

  const handleProductSelect = (product: LocalInsuranceProduct) => {
    setSelectedProduct({
      id: product.id,
      name: product.name,
      category: product.category as '생명보험' | '건강보험' | '자동차보험' | '여행보험' | '화재보험',
      description: product.description,
      coverage: product.coverage,
      benefits: product.benefits,
      exclusions: product.exclusions,
      minAge: 20,
      maxAge: 65,
      minPremium: product.premium * 0.8,
      maxPremium: product.premium * 1.5,
      isActive: true,
      createdAt: new Date().toISOString()
    });
    setInsuranceStep(1);
  };

  const handleTermsAgreement = () => {
    setAgreementInfo({
      termsAgreed: true,
      privacyAgreed: true,
      marketingAgreed: false,
      medicalDisclosureAgreed: true,
      agreementDate: new Date().toISOString()
    });
    setInsuranceStep(2);
  };

  const handlePremiumCalculation = () => {
    setInsuranceDetails({
      coverageAmount: 100000000,
      premium: selectedProductData.premium,
      paymentPeriod: 20,
      coveragePeriod: 30,
      paymentFrequency: 'MONTHLY',
      riders: []
    });
    setPremiumCalculation({
      basePremium: selectedProductData.premium,
      riderPremium: 0,
      totalPremium: selectedProductData.premium,
      discount: 0,
      finalPremium: selectedProductData.premium,
      breakdown: []
    });
    setInsuranceStep(3);
  };

  const handlePersonalInfo = () => {
    setPersonalInfo({
      name: '',
      residentNumber: '',
      gender: 'M',
      birthDate: '',
      phoneNumber: '',
      email: '',
      address: {
        zipCode: '',
        address1: '',
        address2: ''
      },
      occupation: '',
      maritalStatus: 'SINGLE'
    });
    setInsuranceStep(4);
  };

  const handleFinalConfirmation = () => {
    setInsuranceStep(5);
  };

  const handleBackToConsultation = () => {
    setIsInsuranceApplicationMode(false);
    resetApplication();
  };

  const handleNewApplication = () => {
    setInsuranceStep(0);
    resetApplication();
  };

  const handleGoHome = () => {
    setIsInsuranceApplicationMode(false);
    resetApplication();
  };

  const handleTextSelection = (_event: React.MouseEvent, blockId: string) => {
    const selection = window.getSelection();
    if (selection && selection.toString().trim()) {
      const selectedText = selection.toString().trim();
      setSelectedText(selectedText);

      const range = selection.getRangeAt(0);
      const container = range.commonAncestorContainer.parentElement;

      if (container) {
        const containerText = container.textContent || '';
        const startIndex = containerText.indexOf(selectedText);

        if (startIndex !== -1) {
          const endIndex = startIndex + selectedText.length;
          setSelectionRange({ start: startIndex, end: endIndex, blockId });
        }
      }
    }
  };

  const handleAddHighlight = (color: string) => {
    if (selectedText && selectionRange && userRole === 'counselor') {
      const highlight: Omit<HighlightInfo, 'id'> = {
        text: selectedText,
        blockId: selectionRange.blockId,
        startIndex: selectionRange.start,
        endIndex: selectionRange.end,
        color,
        author: userRole
      };
      onHighlightAdd(highlight);
      setSelectedText('');
      setSelectionRange(null);

      if (window.getSelection) {
        window.getSelection()?.removeAllRanges();
      }
    }
  };

  const applyHighlights = (text: string, blockId: string) => {
    if (!highlights.length) return text;

    const blockHighlights = highlights.filter(h => h.blockId === blockId);
    if (!blockHighlights.length) return text;

    let highlightedText = text;

    const sortedHighlights = [...blockHighlights].sort((a, b) => b.startIndex - a.startIndex);

    sortedHighlights.forEach(highlight => {
      const beforeText = highlightedText.substring(0, highlight.startIndex);
      const highlightedPart = highlightedText.substring(highlight.startIndex, highlight.endIndex);
      const afterText = highlightedText.substring(highlight.endIndex);

      if (!highlightedPart.includes('<mark') && highlightedPart.trim().length > 0) {
        highlightedText = `${beforeText}<mark style="background-color: ${highlight.color}; padding: 2px 4px; border-radius: 3px; opacity: 0.8;">${highlightedPart}</mark>${afterText}`;
      }
    });

    return highlightedText;
  };

  const renderInsuranceApplicationStep = () => {
    switch (insuranceStep) {
      case 0:
        return (
          <div className="space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">보험 상품 선택</h2>
              <p className="text-gray-600">가입하고 싶은 보험 상품을 선택해주세요</p>
            </div>

            <div className="grid gap-4">
              {insuranceProducts.map((product) => (
                <div
                  key={product.id}
                  className="border-2 border-gray-200 rounded-lg p-4 cursor-pointer hover:border-blue-500 transition-colors"
                  onClick={() => handleProductSelect(product)}
                >
                  <div className="flex justify-between items-start">
                    <div>
                      <h3 className="font-bold text-lg">{product.name}</h3>
                      <p className="text-gray-600 text-sm">{product.category}</p>
                      <p className="text-gray-700 mt-2">{product.description}</p>
                    </div>
                    <div className="text-right">
                      <div className="text-sm text-gray-500">월 보험료</div>
                      <div className="font-bold text-lg">{new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', minimumFractionDigits: 0 }).format(product.premium)}</div>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>
        );

      case 1:
        return (
          <div className="space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">약관 확인</h2>
              <p className="text-gray-600">보험 약관을 확인하고 동의해주세요</p>
            </div>

            {storeSelectedProduct && (
              <div className="space-y-4">
                <div className="bg-blue-50 p-4 rounded-lg">
                  <h3 className="font-bold mb-2">{storeSelectedProduct.name}</h3>
                  <p className="text-sm text-gray-700">{storeSelectedProduct.description}</p>
                </div>

                <div className="space-y-3">
                  <h4 className="font-semibold">주요 약관</h4>
                  <div className="p-3 bg-gray-50 rounded text-sm">
                    보험계약은 보험계약자가 청약하고 보험회사가 승낙함으로써 성립됩니다.
                  </div>
                  <div className="p-3 bg-gray-50 rounded text-sm">
                    보험료는 보험계약자가 약정한 방법에 따라 납입합니다.
                  </div>
                  <div className="p-3 bg-gray-50 rounded text-sm">
                    보험금 지급은 보험사고 발생일로부터 30일 이내에 처리합니다.
                  </div>
                </div>

                <div className="space-y-3">
                  <label className="flex items-center">
                    <input type="checkbox" className="mr-2" />
                    <span className="text-sm">보험약관에 동의합니다</span>
                  </label>
                  <label className="flex items-center">
                    <input type="checkbox" className="mr-2" />
                    <span className="text-sm">개인정보 처리방침에 동의합니다</span>
                  </label>
                  <label className="flex items-center">
                    <input type="checkbox" className="mr-2" />
                    <span className="text-sm">마케팅 정보 수신에 동의합니다 (선택)</span>
                  </label>
                </div>
              </div>
            )}
          </div>
        );

      case 2:
        return (
          <div className="space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">보험료 계산</h2>
              <p className="text-gray-600">보장 내용과 보험료를 확인해주세요</p>
            </div>

            {storeSelectedProduct && (
              <div className="space-y-4">
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="font-bold mb-2">보장 내용</h3>
                  <p className="text-sm">{storeSelectedProduct.coverage}</p>
                </div>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-sm font-medium mb-1">보장 금액</label>
                    <input type="number" className="w-full p-2 border rounded" placeholder="100,000,000" />
                  </div>
                  <div>
                    <label className="block text-sm font-medium mb-1">납입 기간</label>
                    <select className="w-full p-2 border rounded">
                      <option value="10">10년</option>
                      <option value="20">20년</option>
                      <option value="30">30년</option>
                    </select>
                  </div>
                </div>

                <div className="bg-blue-50 p-4 rounded-lg">
                  <div className="flex justify-between items-center">
                    <span className="font-medium">월 보험료</span>
                    <span className="font-bold text-xl">{new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', minimumFractionDigits: 0 }).format(50000)}</span>
                  </div>
                </div>
              </div>
            )}
          </div>
        );

      case 3:
        return (
          <div className="space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">개인정보 입력</h2>
              <p className="text-gray-600">보험 가입에 필요한 개인정보를 입력해주세요</p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium mb-1">이름</label>
                <input type="text" className="w-full p-2 border rounded" placeholder="홍길동" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">생년월일</label>
                <input type="date" className="w-full p-2 border rounded" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">성별</label>
                <select className="w-full p-2 border rounded">
                  <option value="male">남성</option>
                  <option value="female">여성</option>
                </select>
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">휴대폰 번호</label>
                <input type="tel" className="w-full p-2 border rounded" placeholder="010-1234-5678" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">이메일</label>
                <input type="email" className="w-full p-2 border rounded" placeholder="example@email.com" />
              </div>
              <div>
                <label className="block text-sm font-medium mb-1">직업</label>
                <input type="text" className="w-full p-2 border rounded" placeholder="회사원" />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium mb-1">주소</label>
              <input type="text" className="w-full p-2 border rounded" placeholder="서울시 강남구 테헤란로 123" />
            </div>
          </div>
        );

      case 4:
        return (
          <div className="space-y-6">
            <div className="text-center">
              <h2 className="text-2xl font-bold text-gray-800 mb-2">최종 확인</h2>
              <p className="text-gray-600">입력하신 정보를 최종 확인해주세요</p>
            </div>

            {storeSelectedProduct && (
              <div className="space-y-4">
                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="font-bold mb-2">선택한 상품</h3>
                  <p className="font-medium">{storeSelectedProduct.name}</p>
                  <p className="text-sm text-gray-600">{storeSelectedProduct.category}</p>
                </div>

                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="font-bold mb-2">보험료 정보</h3>
                  <div className="flex justify-between">
                    <span>월 보험료</span>
                    <span className="font-bold">{new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', minimumFractionDigits: 0 }).format(50000)}</span>
                  </div>
                </div>

                <div className="bg-gray-50 p-4 rounded-lg">
                  <h3 className="font-bold mb-2">개인정보</h3>
                  <p className="text-sm">이름: 홍길동</p>
                  <p className="text-sm">생년월일: 1990-01-01</p>
                  <p className="text-sm">휴대폰: 010-1234-5678</p>
                </div>
              </div>
            )}
          </div>
        );

      case 5:
        return (
          <div className="space-y-6 text-center">
            <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto">
              <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
              </svg>
            </div>

            <div>
              <h2 className="text-2xl font-bold text-gray-800 mb-2">보험 가입이 완료되었습니다!</h2>
              <p className="text-gray-600">신청이 정상적으로 처리되었습니다</p>
            </div>

            {storeSelectedProduct && (
              <div className="bg-blue-50 p-4 rounded-lg">
                <h3 className="font-bold mb-2">{storeSelectedProduct.name}</h3>
                <p className="text-sm text-gray-600">신청번호: INS-2024-001234</p>
                <p className="text-sm text-gray-600">증권번호: POL-2024-567890</p>
              </div>
            )}

            <div className="space-y-3">
              <p className="text-sm text-gray-600">보험증권은 이메일로 발송됩니다</p>
              <p className="text-sm text-gray-600">첫 보험료 납입일: 다음 달 1일</p>
            </div>
          </div>
        );

      default:
        return null;
    }
  };

  const renderConsultationMode = () => (
    <div className="space-y-4">
      {}
      <div className="px-4 py-3 bg-gradient-to-r from-blue-600 to-blue-700 text-white rounded-t-xl">
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-lg font-bold">{selectedProductData.name}</h2>
            <p className="text-blue-100 text-sm">{selectedProductData.category}</p>
          </div>
          <div className="text-right">
            <div className="text-sm font-medium">월 보험료</div>
            <div className="text-lg font-bold">{new Intl.NumberFormat('ko-KR', { style: 'currency', currency: 'KRW', minimumFractionDigits: 0 }).format(selectedProductData.premium)}</div>
          </div>
        </div>
      </div>

      {}
      <div className="px-4">
        <div className="flex items-center justify-between mb-4">
          {consultationSteps.map((step, index) => (
            <div key={index} className="flex items-center">
              <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-semibold ${
                index <= currentStep ? 'bg-blue-600 text-white' : 'bg-gray-300 text-gray-600'
              }`}>
                {index < currentStep ? '✓' : index + 1}
              </div>
              <span className={`ml-2 text-xs font-medium ${
                index <= currentStep ? 'text-blue-600' : 'text-gray-500'
              }`}>
                {step}
              </span>
              {index < consultationSteps.length - 1 && (
                <div className={`w-8 h-0.5 mx-2 ${
                  index < currentStep ? 'bg-blue-600' : 'bg-gray-300'
                }`} />
              )}
            </div>
          ))}
        </div>
      </div>

      {}
      <div className="px-4 space-y-4">
        {}
        <div>
          <h3 className="text-base font-semibold text-gray-800 mb-2">상품 설명</h3>
          <div
            className="p-3 bg-gray-50 rounded-lg text-sm"
            onMouseUp={(e) => handleTextSelection(e, 'description')}
            dangerouslySetInnerHTML={{ __html: applyHighlights(selectedProductData.description, 'description') }}
          />
        </div>

        {}
        <div>
          <h3 className="text-base font-semibold text-gray-800 mb-2">보장 내용</h3>
          <div className="p-3 bg-blue-50 rounded-lg text-sm">
            {selectedProductData.coverage}
          </div>
        </div>

        {}
        <div>
          <h3 className="text-base font-semibold text-gray-800 mb-2">주요 약관</h3>
          <div className="space-y-2">
            {selectedProductData.terms.map((term, index) => (
              <div
                key={index}
                className="p-3 bg-gray-50 rounded-lg cursor-pointer hover:bg-gray-100 transition-colors text-sm"
                onMouseUp={(e) => handleTextSelection(e, `term-${index}`)}
                dangerouslySetInnerHTML={{ __html: applyHighlights(term, `term-${index}`) }}
              />
            ))}
          </div>
        </div>

        {}
        <div>
          <h3 className="text-base font-semibold text-gray-800 mb-2">보험 혜택</h3>
          <div className="space-y-2">
            {selectedProductData.benefits.map((benefit, index) => (
              <div
                key={index}
                className="p-3 bg-green-50 rounded-lg cursor-pointer hover:bg-green-100 transition-colors text-sm"
                onMouseUp={(e) => handleTextSelection(e, `benefit-${index}`)}
                dangerouslySetInnerHTML={{ __html: applyHighlights(benefit, `benefit-${index}`) }}
              />
            ))}
          </div>
        </div>

        {}
        <div>
          <h3 className="text-base font-semibold text-gray-800 mb-2">보험 제외사항</h3>
          <div className="space-y-2">
            {selectedProductData.exclusions.map((exclusion, index) => (
              <div
                key={index}
                className="p-3 bg-red-50 rounded-lg cursor-pointer hover:bg-red-100 transition-colors text-sm"
                onMouseUp={(e) => handleTextSelection(e, `exclusion-${index}`)}
                dangerouslySetInnerHTML={{ __html: applyHighlights(exclusion, `exclusion-${index}`) }}
              />
            ))}
          </div>
        </div>

        {}
        {selectedText && userRole === 'counselor' && (
          <div className="fixed bottom-4 left-1/2 transform -translate-x-1/2 bg-white rounded-lg shadow-lg border border-gray-200 p-3 z-50">
            <div className="flex items-center space-x-2">
              <span className="text-sm text-gray-600">"{selectedText}"</span>
              <div className="flex space-x-1">
                <button
                  onClick={() => handleAddHighlight('#ffeb3b')}
                  className="w-6 h-6 bg-yellow-300 rounded hover:bg-yellow-400 transition-colors"
                  title="노란색"
                />
                <button
                  onClick={() => handleAddHighlight('#4caf50')}
                  className="w-6 h-6 bg-green-400 rounded hover:bg-green-500 transition-colors"
                  title="초록색"
                />
                <button
                  onClick={() => handleAddHighlight('#2196f3')}
                  className="w-6 h-6 bg-blue-400 rounded hover:bg-blue-500 transition-colors"
                  title="파란색"
                />
                <button
                  onClick={() => handleAddHighlight('#ff9800')}
                  className="w-6 h-6 bg-orange-400 rounded hover:bg-orange-500 transition-colors"
                  title="주황색"
                />
              </div>
              <button
                onClick={() => {
                  setSelectedText('');
                  setSelectionRange(null);
                  if (window.getSelection) {
                    window.getSelection()?.removeAllRanges();
                  }
                }}
                className="ml-2 text-gray-400 hover:text-gray-600"
              >
                ✕
              </button>
            </div>
          </div>
        )}
      </div>

      {}
      <div className="px-4 py-3 bg-gray-50 border-t">
        <div className="flex justify-between items-center">
          <button
            onClick={() => onStepChange(Math.max(0, currentStep - 1))}
            disabled={currentStep === 0}
            className="px-3 py-2 bg-gray-300 text-gray-700 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-400 transition-colors text-sm"
          >
            이전
          </button>

          <div className="text-center">
            <div className="text-xs text-gray-600">현재 단계</div>
            <div className="font-semibold text-blue-600 text-sm">{consultationSteps[currentStep]}</div>
          </div>

          <div className="flex space-x-2">
            <button
              onClick={handleStartInsuranceApplication}
              className="px-3 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors text-sm"
            >
              보험 가입
            </button>
            <button
              onClick={() => onStepChange(Math.min(consultationSteps.length - 1, currentStep + 1))}
              disabled={currentStep === consultationSteps.length - 1}
              className="px-3 py-2 bg-blue-600 text-white rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-blue-700 transition-colors text-sm"
            >
              다음
            </button>
          </div>
        </div>
      </div>
    </div>
  );

  return (
    <div className="bg-white rounded-xl shadow-lg overflow-hidden h-full flex flex-col">
      {isInsuranceApplicationMode ? (
        <div className="flex flex-col h-full">
          {}
          <div className="px-6 py-4 bg-gradient-to-r from-green-600 to-green-700 text-white">
            <div className="flex items-center justify-between">
              <h2 className="text-xl font-bold">보험 가입</h2>
              <button
                onClick={handleBackToConsultation}
                className="px-3 py-1 bg-white bg-opacity-20 rounded hover:bg-opacity-30 transition-colors text-sm"
              >
                상담으로 돌아가기
              </button>
            </div>
          </div>

          {}
          <div className="px-6 py-3 bg-gray-50 border-b">
            <div className="flex items-center justify-between">
              {consultationSteps.map((step, index) => (
                <div key={index} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-semibold ${
                    index <= insuranceStep ? 'bg-green-600 text-white' : 'bg-gray-300 text-gray-600'
                  }`}>
                    {index < insuranceStep ? '✓' : index + 1}
                  </div>
                  <span className={`ml-2 text-sm font-medium ${
                    index <= insuranceStep ? 'text-green-600' : 'text-gray-500'
                  }`}>
                    {step}
                  </span>
                  {index < consultationSteps.length - 1 && (
                    <div className={`w-12 h-0.5 mx-3 ${
                      index < insuranceStep ? 'bg-green-600' : 'bg-gray-300'
                    }`} />
                  )}
                </div>
              ))}
            </div>
          </div>

          {}
          <div className="flex-1 p-6 overflow-y-auto">
            {renderInsuranceApplicationStep()}
          </div>

          {}
          <div className="px-6 py-4 bg-gray-50 border-t">
            <div className="flex justify-between">
              <button
                onClick={() => setInsuranceStep(Math.max(0, insuranceStep - 1))}
                disabled={insuranceStep === 0}
                className="px-4 py-2 bg-gray-300 text-gray-700 rounded-lg font-medium disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-400 transition-colors"
              >
                이전
              </button>

              {insuranceStep === 5 ? (
                <div className="flex space-x-3">
                  <button
                    onClick={handleNewApplication}
                    className="px-4 py-2 bg-blue-600 text-white rounded-lg font-medium hover:bg-blue-700 transition-colors"
                  >
                    다른 보험 가입하기
                  </button>
                  <button
                    onClick={handleGoHome}
                    className="px-4 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors"
                  >
                    홈으로 이동
                  </button>
                </div>
              ) : (
                <button
                  onClick={() => {
                    switch (insuranceStep) {
                      case 0:
                        break;
                      case 1:
                        handleTermsAgreement();
                        break;
                      case 2:
                        handlePremiumCalculation();
                        break;
                      case 3:
                        handlePersonalInfo();
                        break;
                      case 4:
                        handleFinalConfirmation();
                        break;
                    }
                  }}
                  className="px-4 py-2 bg-green-600 text-white rounded-lg font-medium hover:bg-green-700 transition-colors"
                >
                  다음
                </button>
              )}
            </div>
          </div>
        </div>
      ) : (
        renderConsultationMode()
      )}
    </div>
  );
};

export default InsuranceDashboard;