import { useState } from 'react';
import Layout from '../../components/layout/Layout';

interface DepositProduct {
  productId: number;
  productCode: string;
  productName: string;
  depositType: string;
  minContractPeriod: number;
  maxContractPeriod: number;
  contractPeriodUnit: string;
  subscriptionTarget: string;
  subscriptionAmount: number;
  productCategory: string;
  interestPayment: string;
  taxBenefit: string;
  partialWithdrawal: string;
  cancellationPenalty: string;
  description: string;
}

function DepositProducts() {
  const [selectedProduct, setSelectedProduct] = useState<DepositProduct | null>(null);
  const [activeTab, setActiveTab] = useState<'products' | 'calculator'>('products');
  
  // 예금 계산기 상태 (일시불 예치)
  const [depositAmount, setDepositAmount] = useState<number>(0);
  const [depositInterestRate, setDepositInterestRate] = useState<number>(3.5);
  const [depositPeriod, setDepositPeriod] = useState<number>(12);
  const [depositResult, setDepositResult] = useState<{
    principal: number;
    interest: number;
    finalAmount: number;
  } | null>(null);

  // 예금 계산 함수 (일시불 예치)
  const calculateDeposit = () => {
    if (depositAmount <= 0 || depositPeriod <= 0 || depositInterestRate <= 0) {
      setDepositResult(null);
      return;
    }

    // 예금은 일시불 예치이므로 단리 계산
    const annualRate = depositInterestRate / 100;
    const interest = depositAmount * annualRate * (depositPeriod / 12);
    const finalAmount = depositAmount + interest;

    setDepositResult({
      principal: depositAmount,
      interest: Math.round(interest),
      finalAmount: Math.round(finalAmount)
    });
  };

  // 목업 데이터
  const depositProducts: DepositProduct[] = [
    {
      productId: 1,
      productCode: "1479088",
      productName: "하나의 정기예금",
      depositType: "정기예금",
      minContractPeriod: 1,
      maxContractPeriod: 60,
      contractPeriodUnit: "개월",
      subscriptionTarget: "실명의 개인 또는 개인사업자",
      subscriptionAmount: 1000000,
      productCategory: "예금",
      interestPayment: "만기일시지급식",
      taxBenefit: "비과세종합저축으로 가입 가능",
      partialWithdrawal: "만기일 이전 2회까지 가능",
      cancellationPenalty: "",
      description: "자유롭게 자금관리가 가능한 하나원큐(스마트폰 뱅킹) 전용 정기예금"
    },
    {
      productId: 2,
      productCode: "1419635",
      productName: "1년 연동형 정기예금",
      depositType: "정기예금",
      minContractPeriod: 12,
      maxContractPeriod: 180,
      contractPeriodUnit: "개월",
      subscriptionTarget: "개인, 개인사업자 또는 법인",
      subscriptionAmount: 10000,
      productCategory: "예금",
      interestPayment: "만기일시지급식",
      taxBenefit: "개인의 경우 비과세종합저축 가능",
      partialWithdrawal: "불가",
      cancellationPenalty: "",
      description: "서울보증보험의 보증서 발급 담보용 정기예금"
    },
    {
      productId: 3,
      productCode: "1419664",
      productName: "행복knowhow 연금예금",
      depositType: "정기예금",
      minContractPeriod: 12,
      maxContractPeriod: 360,
      contractPeriodUnit: "개월",
      subscriptionTarget: "실명의 개인 또는 개인사업자",
      subscriptionAmount: 1000000,
      productCategory: "적금",
      interestPayment: "원리금균등지급식",
      taxBenefit: "비과세종합저축으로 가입 가능",
      partialWithdrawal: "거치기간 동안에만 총3회까지 가능",
      cancellationPenalty: "",
      description: "노후자금, 생활자금, 재투자자금까지! 행복knowhow 연금예금으로 설계하세요!"
    },
    {
      productId: 4,
      productCode: "1419602",
      productName: "정기예금",
      depositType: "정기예금",
      minContractPeriod: 1,
      maxContractPeriod: 36,
      contractPeriodUnit: "개월",
      subscriptionTarget: "제한없음",
      subscriptionAmount: 10000,
      productCategory: "예금",
      interestPayment: "만기일시지급식",
      taxBenefit: "비과세종합저축으로 가입 가능",
      partialWithdrawal: "만기해지 포함 총3회 일부해지 가능",
      cancellationPenalty: "",
      description: "목돈을 일정기간 동안 예치하여 안정적인 수익을 추구하는 예금"
    }
  ];

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {/* Main Content */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Tab Navigation */}
          <div className="bg-white rounded-xl shadow-lg mb-8">
            <div className="flex border-b">
              <button
                onClick={() => setActiveTab('products')}
                className={`flex-1 py-4 px-6 text-center font-hana-medium transition-colors ${
                  activeTab === 'products'
                    ? 'text-hana-green border-b-2 border-hana-green bg-hana-green/5'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                상품 종류
              </button>
              <button
                onClick={() => setActiveTab('calculator')}
                className={`flex-1 py-4 px-6 text-center font-hana-medium transition-colors ${
                  activeTab === 'calculator'
                    ? 'text-hana-green border-b-2 border-hana-green bg-hana-green/5'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                목표 금액 계산
              </button>
            </div>
          </div>

          {/* Tab Content */}
          {activeTab === 'products' && (
            <>
              {/* Product Information */}
              <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
                <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">정기예금이란?</h2>
                <div className="space-y-4 text-gray-700">
                  <p className="text-lg">
                    정기예금은 일정한 기간 동안 일정한 금액을 예치하여 약정한 이자를 받는 금융상품입니다.
                  </p>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div className="bg-hana-green/5 p-6 rounded-lg">
                      <h3 className="text-xl font-hana-bold text-hana-green mb-3">주요 특징</h3>
                      <ul className="space-y-2">
                        <li>• 일정 기간 동안 안정적인 수익 보장</li>
                        <li>• 예금자보호법에 의한 원금 보호</li>
                        <li>• 만기 전 해지 시 약정 이자보다 낮은 이자 적용</li>
                      </ul>
                    </div>
                    <div className="bg-blue-50 p-6 rounded-lg">
                      <h3 className="text-xl font-hana-bold text-blue-600 mb-3">고객 혜택</h3>
                      <ul className="space-y-2">
                        <li>• 안정적인 자산 증식</li>
                        <li>• 세제 혜택 (비과세, 소득공제 등)</li>
                        <li>• 다양한 만기 옵션 제공</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>

              {/* Product List */}
              <div className="mb-6">
                <h3 className="text-2xl font-hana-bold text-gray-900 mb-4">하나은행 정기예금 상품</h3>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {depositProducts.map((product) => (
                  <div 
                    key={product.productId} 
                    className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer"
                    onClick={() => setSelectedProduct(product)}
                  >
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-xl font-hana-bold text-gray-900">{product.productName}</h3>
                      <span className="bg-hana-green text-white px-3 py-1 rounded-full text-sm font-hana-medium">
                        {product.productCategory}
                      </span>
                    </div>
                    
                    <div className="space-y-3 mb-4">
                      <div className="flex justify-between">
                        <span className="text-gray-500">상품코드</span>
                        <span className="font-hana-medium">{product.productCode}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">가입기간</span>
                        <span className="font-hana-medium">
                          {product.minContractPeriod === product.maxContractPeriod 
                            ? `${product.minContractPeriod}${product.contractPeriodUnit}`
                            : `${product.minContractPeriod}~${product.maxContractPeriod}${product.contractPeriodUnit}`
                          }
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">가입금액</span>
                        <span className="font-hana-medium">{product.subscriptionAmount.toLocaleString()}원 이상</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">가입대상</span>
                        <span className="font-hana-medium">{product.subscriptionTarget}</span>
                      </div>
                    </div>

                    <div className="border-t pt-4">
                      <div className="flex justify-between items-center">
                        <span className="text-sm text-gray-600">상세 정보 보기</span>
                        <span className="text-hana-green font-hana-medium">→</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {activeTab === 'calculator' && (
            <div className="bg-white rounded-xl shadow-lg p-8">
              <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">정기예금 수익 계산기</h2>
              <p className="text-gray-600 mb-8">예치할 금액과 기간을 입력하면 예상 수익을 계산해드립니다.</p>
              
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* 입력 폼 */}
                <div className="space-y-6">
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      예치 금액 (원)
                    </label>
                    <input
                      type="number"
                      value={depositAmount || ''}
                      onChange={(e) => setDepositAmount(Number(e.target.value))}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      placeholder="예: 10000000"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      예상 금리 (%)
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      value={depositInterestRate}
                      onChange={(e) => setDepositInterestRate(Number(e.target.value))}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      예치 기간 (개월)
                    </label>
                    <input
                      type="number"
                      value={depositPeriod}
                      onChange={(e) => setDepositPeriod(Number(e.target.value))}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                    />
                  </div>
                  
                  <button
                    onClick={calculateDeposit}
                    className="w-full bg-hana-green text-white py-3 px-6 rounded-lg font-hana-medium hover:bg-green-600 transition-colors"
                  >
                    수익 계산하기
                  </button>
                </div>

                {/* 결과 표시 */}
                <div className="bg-gray-50 rounded-lg p-6">
                  <h3 className="text-xl font-hana-bold text-gray-900 mb-4">예상 수익</h3>
                  {depositResult ? (
                    <div className="space-y-4">
                      <div className="bg-white p-4 rounded-lg">
                        <div className="text-sm text-gray-500">예치 원금</div>
                        <div className="text-xl font-hana-bold text-gray-900">
                          {depositResult.principal.toLocaleString()}원
                        </div>
                      </div>
                      <div className="bg-white p-4 rounded-lg">
                        <div className="text-sm text-gray-500">예상 이자</div>
                        <div className="text-xl font-hana-bold text-blue-600">
                          {depositResult.interest.toLocaleString()}원
                        </div>
                      </div>
                      <div className="bg-white p-4 rounded-lg border-2 border-hana-green">
                        <div className="text-sm text-gray-500">만기 수령액</div>
                        <div className="text-2xl font-hana-bold text-hana-green">
                          {depositResult.finalAmount.toLocaleString()}원
                        </div>
                      </div>
                      <div className="bg-hana-green/10 p-4 rounded-lg">
                        <div className="text-sm text-hana-green font-hana-medium">수익률</div>
                        <div className="text-lg font-hana-bold text-hana-green">
                          {((depositResult.interest / depositResult.principal) * 100).toFixed(2)}%
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="text-center text-gray-500 py-8">
                      예치 금액과 기간을 입력하고 수익 계산하기 버튼을 눌러주세요.
                    </div>
                  )}
                </div>
              </div>
            </div>
          )}

          {/* Product Detail Modal */}
          {selectedProduct && (
            <div 
              className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
              onClick={() => setSelectedProduct(null)}
            >
              <div 
                className="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="p-6 border-b">
                  <div className="flex items-center justify-between">
                    <h2 className="text-2xl font-hana-bold text-gray-900">{selectedProduct.productName}</h2>
                    <button
                      onClick={() => setSelectedProduct(null)}
                      className="text-gray-400 hover:text-gray-600 text-2xl"
                    >
                      ×
                    </button>
                  </div>
                </div>

                <div className="p-6">
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                    {/* 기본 정보 */}
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-4">기본 정보</h3>
                      <div className="space-y-3">
                        <div className="flex justify-between">
                          <span className="text-gray-500">상품코드</span>
                          <span className="font-hana-medium">{selectedProduct.productCode}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">상품유형</span>
                          <span className="font-hana-medium">{selectedProduct.depositType}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">가입기간</span>
                          <span className="font-hana-medium">
                            {selectedProduct.minContractPeriod === selectedProduct.maxContractPeriod 
                              ? `${selectedProduct.minContractPeriod}${selectedProduct.contractPeriodUnit}`
                              : `${selectedProduct.minContractPeriod}~${selectedProduct.maxContractPeriod}${selectedProduct.contractPeriodUnit}`
                            }
                          </span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">가입대상</span>
                          <span className="font-hana-medium">{selectedProduct.subscriptionTarget}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">가입금액</span>
                          <span className="font-hana-medium">{selectedProduct.subscriptionAmount.toLocaleString()}원 이상</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">이자지급</span>
                          <span className="font-hana-medium">{selectedProduct.interestPayment}</span>
                        </div>
                      </div>
                    </div>

                    {/* 상세 정보 */}
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-4">상세 정보</h3>
                      <div className="space-y-3">
                        <div>
                          <span className="text-gray-500 block mb-1">세제혜택</span>
                          <span className="font-hana-medium">{selectedProduct.taxBenefit}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">일부해지</span>
                          <span className="font-hana-medium">{selectedProduct.partialWithdrawal}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">해지시 불이익</span>
                          <span className="font-hana-medium">{selectedProduct.cancellationPenalty || '없음'}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">상품 설명</span>
                          <span className="font-hana-medium">{selectedProduct.description}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* 신청 버튼 */}
                  <div className="mt-8 flex justify-center">
                    <button className="bg-hana-green text-white px-8 py-3 rounded-lg font-hana-medium hover:bg-green-600 transition-colors">
                      상품 신청하기
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}

export default DepositProducts;