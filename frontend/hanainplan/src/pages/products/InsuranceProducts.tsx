import { useState } from 'react';
import Layout from '../../components/layout/Layout';

interface GeneralInsurance {
  productId: number;
  productCode: string;
  productName: string;
  category: string;
  benefitName: string;
  paymentReason: string;
  paymentAmount: number;
  subscriptionAmountBasic: number;
  subscriptionAmountMale: number;
  subscriptionAmountFemale: number;
  interestRate: number;
  insurancePriceIndexMale: number;
  insurancePriceIndexFemale: number;
  productFeatures: string;
  surrenderValue: number;
  renewalCycle: string;
  isUniversal: string;
  salesChannel: string;
  specialNotes: string;
  representativeNumber: string;
  isActive: boolean;
}

interface PensionInsurance {
  productId: number;
  productCode: string;
  productName: string;
  category: string;
  maintenancePeriod: string;
  premiumPayment: number;
  contractorAccumulationMale: number;
  accumulationRateMale: number;
  surrenderValueMale: number;
  contractorAccumulationFemale: number;
  accumulationRateFemale: number;
  surrenderValueFemale: number;
  expectedReturnRateMinimum: number;
  expectedReturnRateCurrent: number;
  expectedReturnRateAverage: number;
  businessExpenseRatio: number;
  riskCoverage: string;
  currentAnnouncedRate: number;
  minimumGuaranteedRate: string;
  subscriptionType: string;
  isUniversal: string;
  paymentMethod: string;
  salesChannel: string;
  specialNotes: string;
  representativeNumber: string;
  isActive: boolean;
}

type InsuranceProduct = GeneralInsurance | PensionInsurance;

function InsuranceProducts() {
  const [selectedProduct, setSelectedProduct] = useState<InsuranceProduct | null>(null);
  const [activeTab, setActiveTab] = useState<'all' | 'general' | 'pension'>('all');

  const generalInsuranceProducts: GeneralInsurance[] = [
    {
      productId: 1,
      productCode: "HANA301",
      productName: "하나실손의료보험",
      category: "건강보험",
      benefitName: "실손의료비",
      paymentReason: "질병, 상해로 인한 의료비",
      paymentAmount: 50000000,
      subscriptionAmountBasic: 3000000,
      subscriptionAmountMale: 3500000,
      subscriptionAmountFemale: 3200000,
      interestRate: 0.035,
      insurancePriceIndexMale: 0.85,
      insurancePriceIndexFemale: 0.82,
      productFeatures: "실제 의료비 보상, 높은 보장한도",
      surrenderValue: 0,
      renewalCycle: "1년",
      isUniversal: "N",
      salesChannel: "인터넷, 모바일, 대면상담",
      specialNotes: "면책기간 90일, 기존질병 제외",
      representativeNumber: "1588-1234",
      isActive: true
    },
    {
      productId: 2,
      productCode: "HANA302",
      productName: "하나중대질병보험",
      category: "건강보험",
      benefitName: "중대질병진단급여금",
      paymentReason: "암, 심장병, 뇌혈관질환 진단",
      paymentAmount: 100000000,
      subscriptionAmountBasic: 5000000,
      subscriptionAmountMale: 5500000,
      subscriptionAmountFemale: 5200000,
      interestRate: 0.032,
      insurancePriceIndexMale: 0.88,
      insurancePriceIndexFemale: 0.85,
      productFeatures: "고액 진단급여금, 3대 질병 보장",
      surrenderValue: 0,
      renewalCycle: "1년",
      isUniversal: "N",
      salesChannel: "인터넷, 모바일, 대면상담",
      specialNotes: "면책기간 90일, 기존질병 제외",
      representativeNumber: "1588-1234",
      isActive: true
    },
    {
      productId: 3,
      productCode: "HANA303",
      productName: "하나자동차보험",
      category: "손해보험",
      benefitName: "대인배상, 대물배상",
      paymentReason: "교통사고 발생",
      paymentAmount: 200000000,
      subscriptionAmountBasic: 2000000,
      subscriptionAmountMale: 2200000,
      subscriptionAmountFemale: 2100000,
      interestRate: 0.028,
      insurancePriceIndexMale: 0.92,
      insurancePriceIndexFemale: 0.89,
      productFeatures: "종합보장, 무사고할인",
      surrenderValue: 0,
      renewalCycle: "1년",
      isUniversal: "N",
      salesChannel: "인터넷, 모바일, 대면상담",
      specialNotes: "면책기간 없음",
      representativeNumber: "1588-1234",
      isActive: true
    }
  ];

  const pensionInsuranceProducts: PensionInsurance[] = [
    {
      productId: 4,
      productCode: "HANA401",
      productName: "하나확정연금보험",
      category: "연금보험",
      maintenancePeriod: "20년",
      premiumPayment: 1200000,
      contractorAccumulationMale: 25000000,
      accumulationRateMale: 0.042,
      surrenderValueMale: 22000000,
      contractorAccumulationFemale: 24000000,
      accumulationRateFemale: 0.038,
      surrenderValueFemale: 21000000,
      expectedReturnRateMinimum: 0.025,
      expectedReturnRateCurrent: 0.035,
      expectedReturnRateAverage: 0.032,
      businessExpenseRatio: 0.08,
      riskCoverage: "사망보험금, 연금보장",
      currentAnnouncedRate: 0.035,
      minimumGuaranteedRate: "2.5%",
      subscriptionType: "개인",
      isUniversal: "N",
      paymentMethod: "월납",
      salesChannel: "인터넷, 모바일, 대면상담",
      specialNotes: "연금수령시 연금소득세 적용",
      representativeNumber: "1588-1234",
      isActive: true
    },
    {
      productId: 5,
      productCode: "HANA402",
      productName: "하나변액연금보험",
      category: "연금보험",
      maintenancePeriod: "15년",
      premiumPayment: 1500000,
      contractorAccumulationMale: 30000000,
      accumulationRateMale: 0.055,
      surrenderValueMale: 28000000,
      contractorAccumulationFemale: 29000000,
      accumulationRateFemale: 0.052,
      surrenderValueFemale: 27000000,
      expectedReturnRateMinimum: 0.02,
      expectedReturnRateCurrent: 0.045,
      expectedReturnRateAverage: 0.042,
      businessExpenseRatio: 0.12,
      riskCoverage: "사망보험금, 연금보장",
      currentAnnouncedRate: 0.045,
      minimumGuaranteedRate: "2.0%",
      subscriptionType: "개인",
      isUniversal: "Y",
      paymentMethod: "월납",
      salesChannel: "인터넷, 모바일, 대면상담",
      specialNotes: "운용결과에 따라 수익률 변동",
      representativeNumber: "1588-1234",
      isActive: true
    }
  ];

  const allProducts: InsuranceProduct[] = [...generalInsuranceProducts, ...pensionInsuranceProducts];

  const getDisplayProducts = (): InsuranceProduct[] => {
    switch (activeTab) {
      case 'general':
        return generalInsuranceProducts;
      case 'pension':
        return pensionInsuranceProducts;
      default:
        return allProducts;
    }
  };

  const isGeneralInsurance = (product: InsuranceProduct): product is GeneralInsurance => {
    return 'category' in product;
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
          <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
            <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">보험 상품이란?</h2>
            <div className="space-y-4 text-gray-700">
              <p className="text-lg">
                보험은 예상치 못한 사고나 질병 등으로 인한 경제적 손실을 보상받을 수 있는 금융상품입니다.
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                <div className="bg-hana-green/5 p-6 rounded-lg">
                  <h3 className="text-xl font-hana-bold text-hana-green mb-3">보험의 종류</h3>
                  <ul className="space-y-2">
                    <li>• <strong>건강보험:</strong> 질병·상해 치료비 보장</li>
                    <li>• <strong>연금보험:</strong> 노후 생활비 보장</li>
                    <li>• <strong>생명보험:</strong> 사망·상해 시 보험금 지급</li>
                  </ul>
                </div>
                <div className="bg-blue-50 p-6 rounded-lg">
                  <h3 className="text-xl font-hana-bold text-blue-600 mb-3">보험의 혜택</h3>
                  <ul className="space-y-2">
                    <li>• 예상치 못한 위험에 대한 보호</li>
                    <li>• 의료비 부담 경감</li>
                    <li>• 안정적인 노후 준비</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {}
          <div className="flex space-x-1 bg-gray-100 p-1 rounded-lg mb-8">
            <button
              onClick={() => setActiveTab('all')}
              className={`flex-1 py-2 px-4 rounded-md font-hana-medium transition-colors ${
                activeTab === 'all'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              전체
            </button>
            <button
              onClick={() => setActiveTab('general')}
              className={`flex-1 py-2 px-4 rounded-md font-hana-medium transition-colors ${
                activeTab === 'general'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              일반보험
            </button>
            <button
              onClick={() => setActiveTab('pension')}
              className={`flex-1 py-2 px-4 rounded-md font-hana-medium transition-colors ${
                activeTab === 'pension'
                  ? 'bg-white text-gray-900 shadow-sm'
                  : 'text-gray-600 hover:text-gray-900'
              }`}
            >
              연금보험
            </button>
          </div>

          {}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {getDisplayProducts().map((product) => (
              <div
                key={product.productId}
                className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer"
                onClick={() => setSelectedProduct(product)}
              >
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-xl font-hana-bold text-gray-900">{product.productName}</h3>
                  <span className={`px-3 py-1 rounded-full text-sm font-hana-medium ${
                    isGeneralInsurance(product)
                      ? product.category === '건강보험' ? 'bg-blue-500 text-white' : 'bg-green-500 text-white'
                      : 'bg-purple-500 text-white'
                  }`}>
                    {isGeneralInsurance(product) ? product.category : '연금보험'}
                  </span>
                </div>

                <div className="space-y-3 mb-4">
                  <div className="flex justify-between">
                    <span className="text-gray-500">상품코드</span>
                    <span className="font-hana-medium">{product.productCode}</span>
                  </div>
                  {isGeneralInsurance(product) ? (
                    <>
                      <div className="flex justify-between">
                        <span className="text-gray-500">급부명칭</span>
                        <span className="font-hana-medium">{product.benefitName}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">지급금액</span>
                        <span className="font-hana-medium">{(product.paymentAmount / 10000).toFixed(0)}만원</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">기본보험료</span>
                        <span className="font-hana-medium">{(product.subscriptionAmountBasic / 10000).toFixed(0)}만원</span>
                      </div>
                    </>
                  ) : (
                    <>
                      <div className="flex justify-between">
                        <span className="text-gray-500">유지기간</span>
                        <span className="font-hana-medium">{product.maintenancePeriod}</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">납입보험료</span>
                        <span className="font-hana-medium">{(product.premiumPayment / 10000).toFixed(0)}만원</span>
                      </div>
                      <div className="flex justify-between">
                        <span className="text-gray-500">현재공시이율</span>
                        <span className="font-hana-medium">{(product.currentAnnouncedRate * 100).toFixed(1)}%</span>
                      </div>
                    </>
                  )}
                </div>

                <div className="border-t pt-4">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">상세 정보 보기</span>
                    <span className="text-green-500 font-hana-medium">→</span>
                  </div>
                </div>
              </div>
            ))}
          </div>

          {}
          {selectedProduct && (
            <div
              className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
              onClick={() => setSelectedProduct(null)}
            >
              <div
                className="bg-white rounded-xl max-w-5xl w-full max-h-[90vh] overflow-y-auto"
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
                  {isGeneralInsurance(selectedProduct) ? (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                      <div>
                        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">기본 정보</h3>
                        <div className="space-y-3">
                          <div className="flex justify-between">
                            <span className="text-gray-500">상품코드</span>
                            <span className="font-hana-medium">{selectedProduct.productCode}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">구분</span>
                            <span className="font-hana-medium">{selectedProduct.category}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">급부명칭</span>
                            <span className="font-hana-medium">{selectedProduct.benefitName}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">지급사유</span>
                            <span className="font-hana-medium">{selectedProduct.paymentReason}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">지급금액</span>
                            <span className="font-hana-medium">{(selectedProduct.paymentAmount / 10000).toFixed(0)}만원</span>
                          </div>
                        </div>
                      </div>

                      <div>
                        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">보험료 정보</h3>
                        <div className="space-y-3">
                          <div className="flex justify-between">
                            <span className="text-gray-500">기본보험료</span>
                            <span className="font-hana-medium">{(selectedProduct.subscriptionAmountBasic / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">남자보험료</span>
                            <span className="font-hana-medium">{(selectedProduct.subscriptionAmountMale / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">여자보험료</span>
                            <span className="font-hana-medium">{(selectedProduct.subscriptionAmountFemale / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">이율</span>
                            <span className="font-hana-medium">{(selectedProduct.interestRate * 100).toFixed(1)}%</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">갱신주기</span>
                            <span className="font-hana-medium">{selectedProduct.renewalCycle}</span>
                          </div>
                        </div>
                      </div>

                      <div>
                        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">상세 정보</h3>
                        <div className="space-y-3">
                          <div>
                            <span className="text-gray-500 block mb-1">상품특징</span>
                            <span className="font-hana-medium">{selectedProduct.productFeatures}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">판매채널</span>
                            <span className="font-hana-medium">{selectedProduct.salesChannel}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">유니버셜</span>
                            <span className="font-hana-medium">{selectedProduct.isUniversal === 'Y' ? '적용' : '미적용'}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">대표번호</span>
                            <span className="font-hana-medium">{selectedProduct.representativeNumber}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  ) : (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
                      <div>
                        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">기본 정보</h3>
                        <div className="space-y-3">
                          <div className="flex justify-between">
                            <span className="text-gray-500">상품코드</span>
                            <span className="font-hana-medium">{selectedProduct.productCode}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">유지기간</span>
                            <span className="font-hana-medium">{selectedProduct.maintenancePeriod}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">가입유형</span>
                            <span className="font-hana-medium">{selectedProduct.subscriptionType}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">납입방법</span>
                            <span className="font-hana-medium">{selectedProduct.paymentMethod}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">납입보험료</span>
                            <span className="font-hana-medium">{(selectedProduct.premiumPayment / 10000).toFixed(0)}만원</span>
                          </div>
                        </div>
                      </div>

                      <div>
                        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">수익 정보 (남자)</h3>
                        <div className="space-y-3">
                          <div className="flex justify-between">
                            <span className="text-gray-500">계약자적립액</span>
                            <span className="font-hana-medium">{(selectedProduct.contractorAccumulationMale / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">적립률</span>
                            <span className="font-hana-medium">{(selectedProduct.accumulationRateMale * 100).toFixed(1)}%</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">해약환급금</span>
                            <span className="font-hana-medium">{(selectedProduct.surrenderValueMale / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">현재공시이율</span>
                            <span className="font-hana-medium">{(selectedProduct.currentAnnouncedRate * 100).toFixed(1)}%</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">최저보증이율</span>
                            <span className="font-hana-medium">{selectedProduct.minimumGuaranteedRate}</span>
                          </div>
                        </div>
                      </div>

                      <div>
                        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">수익 정보 (여자)</h3>
                        <div className="space-y-3">
                          <div className="flex justify-between">
                            <span className="text-gray-500">계약자적립액</span>
                            <span className="font-hana-medium">{(selectedProduct.contractorAccumulationFemale / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">적립률</span>
                            <span className="font-hana-medium">{(selectedProduct.accumulationRateFemale * 100).toFixed(1)}%</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">해약환급금</span>
                            <span className="font-hana-medium">{(selectedProduct.surrenderValueFemale / 10000).toFixed(0)}만원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">위험보장</span>
                            <span className="font-hana-medium">{selectedProduct.riskCoverage}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-500">사업비율</span>
                            <span className="font-hana-medium">{(selectedProduct.businessExpenseRatio * 100).toFixed(1)}%</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {}
                  <div className="mt-8">
                    <h3 className="text-lg font-hana-bold text-gray-900 mb-4">특이사항</h3>
                    <div className="bg-blue-50 border-l-4 border-blue-500 p-4 rounded-r-lg">
                      <p className="text-blue-800">
                        {isGeneralInsurance(selectedProduct) ? selectedProduct.specialNotes : selectedProduct.specialNotes}
                      </p>
                    </div>
                  </div>

                  {}
                  <div className="mt-8 flex justify-center">
                    <button className="bg-green-500 text-white px-8 py-3 rounded-lg font-hana-medium hover:bg-green-600 transition-colors">
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

export default InsuranceProducts;