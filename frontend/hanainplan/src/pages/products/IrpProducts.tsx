import { useState } from 'react';
import Layout from '../../components/layout/Layout';

interface IrpProduct {
  irpProductId: number;
  productCode: string;
  productName: string;
  productType: string;
  managementCompany: string;
  trustCompany: string;
  minimumContribution: number;
  maximumContribution: number;
  annualContributionLimit: number;
  managementFeeRate: number;
  trustFeeRate: number;
  salesFeeRate: number;
  totalFeeRate: number;
  investmentOptions: string;
  riskLevel: string;
  expectedReturnRate: number;
  guaranteeType: string;
  guaranteeRate: number;
  maturityAge: string;
  earlyWithdrawalPenalty: string;
  taxBenefit: string;
  contributionFrequency: string;
  contributionMethod: string;
  minimumHoldingPeriod: number;
  autoRebalancing: string;
  rebalancingFrequency: string;
  performanceFee: number;
  performanceFeeThreshold: number;
  fundAllocation: string;
  benchmarkIndex: string;
  description: string;
  precautions: string;
  isActive: boolean;
  startDate: string;
  endDate: string;
}

function IrpProducts() {
  const [selectedProduct, setSelectedProduct] = useState<IrpProduct | null>(null);

  // 목업 데이터
  const irpProducts: IrpProduct[] = [
    {
      irpProductId: 1,
      productCode: "HANA201",
      productName: "하나안정형IRP",
      productType: "DC형",
      managementCompany: "하나자산운용",
      trustCompany: "하나신탁",
      minimumContribution: 100000,
      maximumContribution: 7000000,
      annualContributionLimit: 7000000,
      managementFeeRate: 0.005,
      trustFeeRate: 0.002,
      salesFeeRate: 0.001,
      totalFeeRate: 0.008,
      investmentOptions: "안정형, 균형형",
      riskLevel: "1-2",
      expectedReturnRate: 0.035,
      guaranteeType: "원금보장",
      guaranteeRate: 0.025,
      maturityAge: "55세",
      earlyWithdrawalPenalty: "퇴직소득세 부과",
      taxBenefit: "연간 700만원까지 소득공제",
      contributionFrequency: "월납",
      contributionMethod: "자동이체",
      minimumHoldingPeriod: 60,
      autoRebalancing: "Y",
      rebalancingFrequency: "분기별",
      performanceFee: 0.001,
      performanceFeeThreshold: 0.05,
      fundAllocation: "채권 70%, 주식 30%",
      benchmarkIndex: "KOSPI200",
      description: "안정적인 노후 준비를 위한 보수적 운용 IRP",
      precautions: "운용손실 위험 있음, 장기투자 상품",
      isActive: true,
      startDate: "2024-01-01",
      endDate: "2024-12-31"
    },
    {
      irpProductId: 2,
      productCode: "HANA202",
      productName: "하나균형형IRP",
      productType: "DC형",
      managementCompany: "하나자산운용",
      trustCompany: "하나신탁",
      minimumContribution: 100000,
      maximumContribution: 7000000,
      annualContributionLimit: 7000000,
      managementFeeRate: 0.006,
      trustFeeRate: 0.002,
      salesFeeRate: 0.001,
      totalFeeRate: 0.009,
      investmentOptions: "안정형, 균형형, 성장형",
      riskLevel: "2-3",
      expectedReturnRate: 0.045,
      guaranteeType: "수익보장",
      guaranteeRate: 0.03,
      maturityAge: "55세",
      earlyWithdrawalPenalty: "퇴직소득세 부과",
      taxBenefit: "연간 700만원까지 소득공제",
      contributionFrequency: "월납",
      contributionMethod: "자동이체",
      minimumHoldingPeriod: 60,
      autoRebalancing: "Y",
      rebalancingFrequency: "분기별",
      performanceFee: 0.001,
      performanceFeeThreshold: 0.06,
      fundAllocation: "채권 50%, 주식 50%",
      benchmarkIndex: "KOSPI200",
      description: "안정성과 수익성을 균형있게 추구하는 IRP",
      precautions: "운용손실 위험 있음, 장기투자 상품",
      isActive: true,
      startDate: "2024-01-01",
      endDate: "2024-12-31"
    },
    {
      irpProductId: 3,
      productCode: "HANA203",
      productName: "하나성장형IRP",
      productType: "DC형",
      managementCompany: "하나자산운용",
      trustCompany: "하나신탁",
      minimumContribution: 100000,
      maximumContribution: 7000000,
      annualContributionLimit: 7000000,
      managementFeeRate: 0.007,
      trustFeeRate: 0.002,
      salesFeeRate: 0.001,
      totalFeeRate: 0.01,
      investmentOptions: "성장형, 적극형",
      riskLevel: "4-5",
      expectedReturnRate: 0.055,
      guaranteeType: "무보장",
      guaranteeRate: 0,
      maturityAge: "55세",
      earlyWithdrawalPenalty: "퇴직소득세 부과",
      taxBenefit: "연간 700만원까지 소득공제",
      contributionFrequency: "월납",
      contributionMethod: "자동이체",
      minimumHoldingPeriod: 60,
      autoRebalancing: "Y",
      rebalancingFrequency: "반기별",
      performanceFee: 0.001,
      performanceFeeThreshold: 0.07,
      fundAllocation: "채권 30%, 주식 70%",
      benchmarkIndex: "KOSPI200",
      description: "장기 자산 증식을 목표로 하는 적극적 운용 IRP",
      precautions: "높은 운용손실 위험 있음, 장기투자 상품",
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
            <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">IRP(개인형퇴직연금)란?</h2>
            <div className="space-y-4 text-gray-700">
              <p className="text-lg">
                IRP는 개인이 퇴직 후 안정적인 노후 생활을 위해 자발적으로 가입하는 개인형 퇴직연금 상품입니다.
              </p>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                <div className="bg-hana-green/5 p-6 rounded-lg">
                  <h3 className="text-xl font-hana-bold text-hana-green mb-3">주요 특징</h3>
                  <ul className="space-y-2">
                    <li>• 퇴직금 이관 및 추가 납입 가능</li>
                    <li>• 다양한 투자 옵션 제공</li>
                    <li>• 55세부터 연금 또는 일시금 수령 가능</li>
                  </ul>
                </div>
                <div className="bg-blue-50 p-6 rounded-lg">
                  <h3 className="text-xl font-hana-bold text-blue-600 mb-3">고객 혜택</h3>
                  <ul className="space-y-2">
                    <li>• 연간 최대 900만원 세액공제</li>
                    <li>• 퇴직소득세 절세 효과</li>
                    <li>• 장기 투자를 통한 자산 증식</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {/* Product List */}
          <div className="mb-6">
            <h3 className="text-2xl font-hana-bold text-gray-900 mb-4">하나금융그룹 IRP 상품</h3>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6 mb-8">
            {irpProducts.map((product) => (
              <div 
                key={product.irpProductId} 
                className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer"
                onClick={() => setSelectedProduct(product)}
              >
                <div className="flex items-center justify-between mb-4">
                  <h3 className="text-xl font-hana-bold text-gray-900">{product.productName}</h3>
                  <span className={`px-3 py-1 rounded-full text-sm font-hana-medium ${
                    product.riskLevel === '1-2' ? 'bg-green-500 text-white' :
                    product.riskLevel === '2-3' ? 'bg-yellow-500 text-white' :
                    'bg-red-500 text-white'
                  }`}>
                    위험등급 {product.riskLevel}
                  </span>
                </div>
                
                <div className="space-y-3 mb-4">
                  <div className="flex justify-between">
                    <span className="text-gray-500">상품코드</span>
                    <span className="font-hana-medium">{product.productCode}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">상품유형</span>
                    <span className="font-hana-medium">{product.productType}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">운용회사</span>
                    <span className="font-hana-medium">{product.managementCompany}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">예상수익률</span>
                    <span className="font-hana-medium">{(product.expectedReturnRate * 100).toFixed(1)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">총 수수료율</span>
                    <span className="font-hana-medium">{(product.totalFeeRate * 100).toFixed(2)}%</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-500">만기연령</span>
                    <span className="font-hana-medium">{product.maturityAge}</span>
                  </div>
                </div>

                <div className="border-t pt-4">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-600">상세 정보 보기</span>
                    <span className="text-purple-500 font-hana-medium">→</span>
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
                  <div className="grid grid-cols-1 md:grid-cols-3 gap-8">
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
                          <span className="font-hana-medium">{selectedProduct.productType}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">운용회사</span>
                          <span className="font-hana-medium">{selectedProduct.managementCompany}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">신탁회사</span>
                          <span className="font-hana-medium">{selectedProduct.trustCompany}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">위험등급</span>
                          <span className="font-hana-medium">{selectedProduct.riskLevel}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">만기연령</span>
                          <span className="font-hana-medium">{selectedProduct.maturityAge}</span>
                        </div>
                      </div>
                    </div>

                    {/* 납입 정보 */}
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-4">납입 정보</h3>
                      <div className="space-y-3">
                        <div className="flex justify-between">
                          <span className="text-gray-500">최소납입금액</span>
                          <span className="font-hana-medium">{(selectedProduct.minimumContribution / 10000).toFixed(0)}만원</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">최대납입금액</span>
                          <span className="font-hana-medium">{(selectedProduct.maximumContribution / 10000).toFixed(0)}만원</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">연간납입한도</span>
                          <span className="font-hana-medium">{(selectedProduct.annualContributionLimit / 10000).toFixed(0)}만원</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">납입주기</span>
                          <span className="font-hana-medium">{selectedProduct.contributionFrequency}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">납입방법</span>
                          <span className="font-hana-medium">{selectedProduct.contributionMethod}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">최소보유기간</span>
                          <span className="font-hana-medium">{selectedProduct.minimumHoldingPeriod}개월</span>
                        </div>
                      </div>
                    </div>

                    {/* 수익/수수료 정보 */}
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-4">수익/수수료 정보</h3>
                      <div className="space-y-3">
                        <div className="flex justify-between">
                          <span className="text-gray-500">예상수익률</span>
                          <span className="font-hana-medium">{(selectedProduct.expectedReturnRate * 100).toFixed(1)}%</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">보장유형</span>
                          <span className="font-hana-medium">{selectedProduct.guaranteeType}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">보장수익률</span>
                          <span className="font-hana-medium">{(selectedProduct.guaranteeRate * 100).toFixed(1)}%</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">운용수수료</span>
                          <span className="font-hana-medium">{(selectedProduct.managementFeeRate * 100).toFixed(2)}%</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">신탁수수료</span>
                          <span className="font-hana-medium">{(selectedProduct.trustFeeRate * 100).toFixed(2)}%</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">총 수수료율</span>
                          <span className="font-hana-medium text-red-600">{(selectedProduct.totalFeeRate * 100).toFixed(2)}%</span>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* 투자 정보 */}
                  <div className="mt-8">
                    <h3 className="text-lg font-hana-bold text-gray-900 mb-4">투자 정보</h3>
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                      <div>
                        <div className="space-y-3">
                          <div>
                            <span className="text-gray-500 block mb-1">투자옵션</span>
                            <span className="font-hana-medium">{selectedProduct.investmentOptions}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">펀드 배분</span>
                            <span className="font-hana-medium">{selectedProduct.fundAllocation}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">벤치마크 지수</span>
                            <span className="font-hana-medium">{selectedProduct.benchmarkIndex}</span>
                          </div>
                        </div>
                      </div>
                      <div>
                        <div className="space-y-3">
                          <div>
                            <span className="text-gray-500 block mb-1">자동 리밸런싱</span>
                            <span className="font-hana-medium">{selectedProduct.autoRebalancing === 'Y' ? '적용' : '미적용'}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">리밸런싱 주기</span>
                            <span className="font-hana-medium">{selectedProduct.rebalancingFrequency}</span>
                          </div>
                          <div>
                            <span className="text-gray-500 block mb-1">성과수수료율</span>
                            <span className="font-hana-medium">{(selectedProduct.performanceFee * 100).toFixed(2)}%</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {/* 세제 혜택 */}
                  <div className="mt-8">
                    <h3 className="text-lg font-hana-bold text-gray-900 mb-4">세제 혜택</h3>
                    <div className="bg-green-50 border-l-4 border-green-500 p-4 rounded-r-lg">
                      <p className="text-green-800">{selectedProduct.taxBenefit}</p>
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
                    <button className="bg-purple-500 text-white px-8 py-3 rounded-lg font-hana-medium hover:bg-purple-600 transition-colors">
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

export default IrpProducts;