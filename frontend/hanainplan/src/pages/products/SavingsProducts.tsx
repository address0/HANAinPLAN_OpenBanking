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
  subscriptionAmount: string;
  productCategory: string;
  interestPayment: string;
  taxBenefit: string;
  partialWithdrawal: string;
  depositorProtection: string;
  transactionMethod: string;
  precautions: string;
  contractCancellationRight: string;
  cancellationPenalty: string;
  paymentRestrictions: string;
  isActive: boolean;
  startDate: string;
  endDate: string;
}

function SavingsProducts() {
  const [selectedProduct, setSelectedProduct] = useState<SavingsProduct | null>(null);

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
      subscriptionAmount: "월 10만원 이상",
      productCategory: "일반",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용",
      partialWithdrawal: "불가",
      depositorProtection: "Y",
      transactionMethod: "인터넷뱅킹, 모바일뱅킹, 영업점",
      precautions: "납입 누락시 우대금리 혜택 제한",
      contractCancellationRight: "계약일로부터 15일 이내",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      paymentRestrictions: "납입기일 준수 필요",
      isActive: true,
      startDate: "2024-01-01",
      endDate: "2024-12-31"
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
      subscriptionAmount: "월 5만원 이상",
      productCategory: "청년우대",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용, 청년우대 혜택",
      partialWithdrawal: "불가",
      depositorProtection: "Y",
      transactionMethod: "인터넷뱅킹, 모바일뱅킹, 영업점",
      precautions: "납입 누락시 우대금리 혜택 제한",
      contractCancellationRight: "계약일로부터 15일 이내",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      paymentRestrictions: "납입기일 준수 필요",
      isActive: true,
      startDate: "2024-01-01",
      endDate: "2024-12-31"
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
      subscriptionAmount: "월 50만원 이상",
      productCategory: "주택청약",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용, 주택청약 혜택",
      partialWithdrawal: "불가",
      depositorProtection: "Y",
      transactionMethod: "인터넷뱅킹, 모바일뱅킹, 영업점",
      precautions: "납입 누락시 우대금리 혜택 제한",
      contractCancellationRight: "계약일로부터 15일 이내",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      paymentRestrictions: "납입기일 준수 필요",
      isActive: true,
      startDate: "2024-01-01",
      endDate: "2024-12-31"
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
      subscriptionAmount: "월 1만원 이상",
      productCategory: "자유",
      interestPayment: "만기일시지급",
      taxBenefit: "이자소득세 15.4% 적용",
      partialWithdrawal: "제한적 가능",
      depositorProtection: "Y",
      transactionMethod: "인터넷뱅킹, 모바일뱅킹, 영업점",
      precautions: "납입 누락시에도 계약 유지",
      contractCancellationRight: "계약일로부터 15일 이내",
      cancellationPenalty: "중도해지시 일반예금금리 적용",
      paymentRestrictions: "납입기일 유연 적용",
      isActive: true,
      startDate: "2024-01-01",
      endDate: "2024-12-31"
    }
  ];

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {/* Main Content */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Product Information */}
          <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
            <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">정기적금이란?</h2>
            <div className="space-y-4 text-gray-700">
              <p className="text-lg">
                정기적금은 일정한 기간 동안 매월 일정한 금액을 납입하여 만기 시 원금과 이자를 함께 받는 금융상품입니다.
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                <div className="bg-hana-green/5 p-6 rounded-lg">
                  <h3 className="text-xl font-hana-bold text-hana-green mb-3">주요 특징</h3>
                  <ul className="space-y-2">
                    <li>• 정기적인 저축 습관 형성</li>
                    <li>• 복리 효과로 높은 수익 창출</li>
                    <li>• 예금자보호법에 의한 원금 보호</li>
                  </ul>
                </div>
                <div className="bg-blue-50 p-6 rounded-lg">
                  <h3 className="text-xl font-hana-bold text-blue-600 mb-3">고객 혜택</h3>
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
                    <span className="font-hana-medium">{product.subscriptionAmount}</span>
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
                          <span className="font-hana-medium">{selectedProduct.subscriptionAmount}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">이자지급</span>
                          <span className="font-hana-medium">{selectedProduct.interestPayment}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">예금자보호</span>
                          <span className="font-hana-medium">{selectedProduct.depositorProtection === 'Y' ? '보호대상' : '비보호'}</span>
                        </div>
                      </div>
                    </div>

                    {/* 상세 정보 */}
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-4">상세 정보</h3>
                      <div className="space-y-3">
                        <div>
                          <span className="text-gray-500 block mb-1">거래방법</span>
                          <span className="font-hana-medium">{selectedProduct.transactionMethod}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">세제혜택</span>
                          <span className="font-hana-medium">{selectedProduct.taxBenefit}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">일부해지</span>
                          <span className="font-hana-medium">{selectedProduct.partialWithdrawal}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">계약해지권</span>
                          <span className="font-hana-medium">{selectedProduct.contractCancellationRight}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">해지시 불이익</span>
                          <span className="font-hana-medium">{selectedProduct.cancellationPenalty}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-1">지급관련제한</span>
                          <span className="font-hana-medium">{selectedProduct.paymentRestrictions}</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* 유의사항 */}
                  <div className="mt-8">
                    <h3 className="text-lg font-hana-bold text-gray-900 mb-4">유의사항</h3>
                    <div className="bg-yellow-50 border-l-4 border-yellow-500 p-4 rounded-r-lg">
                      <p className="text-yellow-800">{selectedProduct.precautions}</p>
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