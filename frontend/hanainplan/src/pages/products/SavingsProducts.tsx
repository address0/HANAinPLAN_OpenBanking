import { useState } from 'react';
import Layout from '../../components/layout/Layout';

interface SavingsProduct {
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

function SavingsProducts() {
  const [selectedProduct, setSelectedProduct] = useState<SavingsProduct | null>(null);
  const [activeTab, setActiveTab] = useState<'products' | 'calculator'>('products');
  
  // 적금 계산기 상태 (월 납입)
  const [monthlyAmount, setMonthlyAmount] = useState<number>(0);
  const [savingsInterestRate, setSavingsInterestRate] = useState<number>(3.5);
  const [savingsPeriod, setSavingsPeriod] = useState<number>(12);
  const [savingsResult, setSavingsResult] = useState<{
    totalDeposit: number;
    interest: number;
    finalAmount: number;
  } | null>(null);

  // 적금 계산 함수 (월 납입)
  const calculateSavings = () => {
    if (monthlyAmount <= 0 || savingsPeriod <= 0 || savingsInterestRate <= 0) {
      setSavingsResult(null);
      return;
    }

    const monthlyRate = savingsInterestRate / 100 / 12;
    const totalDeposit = monthlyAmount * savingsPeriod;
    
    // 적금 복리 계산 (월 단위)
    let totalInterest = 0;
    let currentAmount = 0;
    
    for (let i = 0; i < savingsPeriod; i++) {
      currentAmount += monthlyAmount;
      if (i < savingsPeriod - 1) { // 마지막 달은 이자 계산 안함
        currentAmount *= (1 + monthlyRate);
      }
    }
    
    totalInterest = currentAmount - totalDeposit;
    const finalAmount = totalDeposit + totalInterest;

    setSavingsResult({
      totalDeposit: totalDeposit,
      interest: Math.round(totalInterest),
      finalAmount: Math.round(finalAmount)
    });
  };

  // 목업 데이터
  const savingsProducts: SavingsProduct[] = [
    {
      productId: 1,
      productCode: "HANA101",
      productName: "하나적금",
      depositType: "정기적금",
      minContractPeriod: 12,
      maxContractPeriod: 12,
      contractPeriodUnit: "개월",
      subscriptionTarget: "개인",
      subscriptionAmount: 100000,
      productCategory: "일반",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용",
      partialWithdrawal: "불가",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      description: "안정적인 저축 습관을 기를 수 있는 기본 정기적금 상품입니다."
    },
    {
      productId: 2,
      productCode: "HANA102",
      productName: "하나청년적금",
      depositType: "정기적금",
      minContractPeriod: 24,
      maxContractPeriod: 24,
      contractPeriodUnit: "개월",
      subscriptionTarget: "만 19-34세",
      subscriptionAmount: 50000,
      productCategory: "청년우대",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용, 청년우대 혜택",
      partialWithdrawal: "불가",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      description: "청년층을 위한 특별 정기적금 상품으로 우대금리를 제공합니다."
    },
    {
      productId: 3,
      productCode: "HANA103",
      productName: "하나주택청약적금",
      depositType: "정기적금",
      minContractPeriod: 24,
      maxContractPeriod: 24,
      contractPeriodUnit: "개월",
      subscriptionTarget: "주택구입 예정자",
      subscriptionAmount: 500000,
      productCategory: "주택청약",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용, 주택청약 혜택",
      partialWithdrawal: "불가",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      description: "주택 구매를 위한 특별 정기적금으로 주택청약 혜택을 받을 수 있습니다."
    },
    {
      productId: 4,
      productCode: "HANA104",
      productName: "하나자유적금",
      depositType: "정기적금",
      minContractPeriod: 12,
      maxContractPeriod: 36,
      contractPeriodUnit: "개월",
      subscriptionTarget: "개인",
      subscriptionAmount: 10000,
      productCategory: "자유",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용",
      partialWithdrawal: "제한적 가능",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      description: "유연한 납입 조건으로 자유롭게 저축할 수 있는 정기적금 상품입니다."
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
                    ? 'text-blue-500 border-b-2 border-blue-500 bg-blue-50'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                상품 종류
              </button>
              <button
                onClick={() => setActiveTab('calculator')}
                className={`flex-1 py-4 px-6 text-center font-hana-medium transition-colors ${
                  activeTab === 'calculator'
                    ? 'text-blue-500 border-b-2 border-blue-500 bg-blue-50'
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
                <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">정기적금이란?</h2>
                <div className="space-y-4 text-gray-700">
                  <p className="text-lg">
                    정기적금은 일정한 기간 동안 매월 일정한 금액을 납입하여 만기 시 원금과 이자를 함께 받는 금융상품입니다.
                  </p>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                    <div className="bg-blue-50 p-6 rounded-lg">
                      <h3 className="text-xl font-hana-bold text-blue-600 mb-3">주요 특징</h3>
                      <ul className="space-y-2">
                        <li>• 정기적인 저축 습관 형성</li>
                        <li>• 복리 효과로 높은 수익 창출</li>
                        <li>• 예금자보호법에 의한 원금 보호</li>
                      </ul>
                    </div>
                    <div className="bg-green-50 p-6 rounded-lg">
                      <h3 className="text-xl font-hana-bold text-green-600 mb-3">고객 혜택</h3>
                      <ul className="space-y-2">
                        <li>• 체계적인 자산 형성</li>
                        <li>• 세제 혜택 (비과세, 소득공제 등)</li>
                        <li>• 다양한 납입 금액 옵션</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>

              {/* Product List */}
              <div className="mb-6">
                <h3 className="text-2xl font-hana-bold text-gray-900 mb-4">하나은행 정기적금 상품</h3>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
                {savingsProducts.map((product) => (
                  <div 
                    key={product.productId} 
                    className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer"
                    onClick={() => setSelectedProduct(product)}
                  >
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-xl font-hana-bold text-gray-900">{product.productName}</h3>
                      <span className="bg-blue-500 text-white px-3 py-1 rounded-full text-sm font-hana-medium">
                        {product.productCategory}
                      </span>
                    </div>
                    
                    <div className="space-y-3 mb-4">
                      <div className="flex justify-between">
                        <span className="text-gray-500">상품코드</span>
                        <span className="font-hana-medium">{product.productCode}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">저축기간</span>
                        <span className="font-hana-medium">
                          {product.minContractPeriod === product.maxContractPeriod 
                            ? `${product.minContractPeriod}${product.contractPeriodUnit}`
                            : `${product.minContractPeriod}~${product.maxContractPeriod}${product.contractPeriodUnit}`
                          }
                        </span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">월 납입금액</span>
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
                        <span className="text-blue-500 font-hana-medium">→</span>
                      </div>
                    </div>
                  </div>
                ))}
              </div>
            </>
          )}

          {activeTab === 'calculator' && (
            <div className="bg-white rounded-xl shadow-lg p-8">
              <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">정기적금 수익 계산기</h2>
              <p className="text-gray-600 mb-8">월 납입금액과 기간을 입력하면 예상 수익을 계산해드립니다.</p>
              
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* 입력 폼 */}
                <div className="space-y-6">
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      월 납입금액 (원)
                    </label>
                    <input
                      type="number"
                      value={monthlyAmount || ''}
                      onChange={(e) => setMonthlyAmount(Number(e.target.value))}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                      placeholder="예: 100000"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      예상 금리 (%)
                    </label>
                    <input
                      type="number"
                      step="0.1"
                      value={savingsInterestRate}
                      onChange={(e) => setSavingsInterestRate(Number(e.target.value))}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      저축 기간 (개월)
                    </label>
                    <input
                      type="number"
                      value={savingsPeriod}
                      onChange={(e) => setSavingsPeriod(Number(e.target.value))}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-transparent"
                    />
                  </div>
                  
                  <button
                    onClick={calculateSavings}
                    className="w-full bg-blue-500 text-white py-3 px-6 rounded-lg font-hana-medium hover:bg-blue-600 transition-colors"
                  >
                    수익 계산하기
                  </button>
                </div>

                {/* 결과 표시 */}
                <div className="bg-gray-50 rounded-lg p-6">
                  <h3 className="text-xl font-hana-bold text-gray-900 mb-4">예상 수익</h3>
                  {savingsResult ? (
                    <div className="space-y-4">
                      <div className="bg-white p-4 rounded-lg">
                        <div className="text-sm text-gray-500">총 납입금액</div>
                        <div className="text-xl font-hana-bold text-gray-900">
                          {savingsResult.totalDeposit.toLocaleString()}원
                        </div>
                      </div>
                      <div className="bg-white p-4 rounded-lg">
                        <div className="text-sm text-gray-500">예상 이자</div>
                        <div className="text-xl font-hana-bold text-green-600">
                          {savingsResult.interest.toLocaleString()}원
                        </div>
                      </div>
                      <div className="bg-white p-4 rounded-lg border-2 border-blue-500">
                        <div className="text-sm text-gray-500">만기 수령액</div>
                        <div className="text-2xl font-hana-bold text-blue-500">
                          {savingsResult.finalAmount.toLocaleString()}원
                        </div>
                      </div>
                      <div className="bg-blue-50 p-4 rounded-lg">
                        <div className="text-sm text-blue-600 font-hana-medium">수익률</div>
                        <div className="text-lg font-hana-bold text-blue-600">
                          {((savingsResult.interest / savingsResult.totalDeposit) * 100).toFixed(2)}%
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="text-center text-gray-500 py-8">
                      월 납입금액과 기간을 입력하고 수익 계산하기 버튼을 눌러주세요.
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
                          <span className="text-gray-500">저축기간</span>
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
                          <span className="text-gray-500">월 납입금액</span>
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
                    <button className="bg-blue-500 text-white px-8 py-3 rounded-lg font-hana-medium hover:bg-blue-600 transition-colors">
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

export default SavingsProducts;