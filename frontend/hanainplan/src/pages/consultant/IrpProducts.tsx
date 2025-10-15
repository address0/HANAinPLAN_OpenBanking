import { useState, useEffect } from 'react';
import Layout from '../../components/layout/Layout';
import { useUserStore } from '../../store/userStore';

function IrpProducts() {
  const { user } = useUserStore();
  const [activeTab, setActiveTab] = useState<'overview' | 'regulations'>('overview');

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
          <div className="mb-8">
            <h1 className="text-3xl font-hana-bold text-gray-900 mb-2">IRP 상품</h1>
            <p className="text-gray-600 font-hana-regular">하나은행 IRP 상품 정보 및 관련 규정</p>
          </div>

          {}
          <div className="bg-white rounded-xl shadow-lg mb-8">
            <div className="flex border-b">
              <button
                onClick={() => setActiveTab('overview')}
                className={`flex-1 py-4 px-6 text-center font-hana-medium transition-colors ${
                  activeTab === 'overview'
                    ? 'text-hana-green border-b-2 border-hana-green bg-hana-green/5'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                상품 개요
              </button>
              <button
                onClick={() => setActiveTab('regulations')}
                className={`flex-1 py-4 px-6 text-center font-hana-medium transition-colors ${
                  activeTab === 'regulations'
                    ? 'text-hana-green border-b-2 border-hana-green bg-hana-green/5'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
              >
                관련 규정
              </button>
            </div>
          </div>

          {}
          {activeTab === 'overview' && (
            <div className="bg-white rounded-xl shadow-lg p-8">
              <h2 className="text-2xl font-hana-bold text-gray-900 mb-6">하나은행 IRP (개인퇴직연금)</h2>

              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">상품 정보</h3>
                  <div className="space-y-4">
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-gray-900 mb-2">상품 특징</h4>
                      <ul className="text-sm text-gray-700 space-y-2">
                        <li>• 퇴직금 및 개인 추가 납입금을 적립하여 운용</li>
                        <li>• 세제 혜택: 납입금에 대한 소득공제 및 운용수익 비과세</li>
                        <li>• 55세 이후 연금 수령 또는 일시금 수령 가능</li>
                        <li>• 다양한 투자 상품으로 포트폴리오 구성 가능</li>
                      </ul>
                    </div>

                    <div className="bg-hana-green/5 p-4 rounded-lg border border-hana-green/20">
                      <h4 className="font-hana-medium text-hana-green mb-2">혜택</h4>
                      <ul className="text-sm text-gray-700 space-y-2">
                        <li>• 연간 납입금 700만원까지 13.2% 소득공제</li>
                        <li>• 운용수익 비과세 (15.4% 절세 효과)</li>
                        <li>• 노후 생활 안정 자금 확보</li>
                        <li>• 전문적인 자산 관리 서비스 제공</li>
                      </ul>
                    </div>
                  </div>
                </div>

                {}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">가입 조건 및 절차</h3>
                  <div className="space-y-4">
                    <div className="bg-blue-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-blue-600 mb-2">가입 자격</h4>
                      <ul className="text-sm text-gray-700 space-y-2">
                        <li>• 소득이 있는 개인 (근로소득자, 사업소득자 등)</li>
                        <li>• 만 18세 이상부터 가입 가능</li>
                        <li>• 기존 퇴직연금(DC, DB) 보유자도 추가 가입 가능</li>
                      </ul>
                    </div>

                    <div className="bg-yellow-50 p-4 rounded-lg border border-yellow-200">
                      <h4 className="font-hana-medium text-yellow-700 mb-2">가입 절차</h4>
                      <ol className="text-sm text-gray-700 space-y-2 list-decimal list-inside">
                        <li>IRP 계좌 개설 (하나은행 영업점 또는 온라인)</li>
                        <li>연금보험료 또는 개인부담금 납입</li>
                        <li>투자 상품 선택 및 포트폴리오 구성</li>
                        <li>정기적인 운용 성과 확인 및 리밸런싱</li>
                      </ol>
                    </div>
                  </div>
                </div>
              </div>

              {}
              <div className="mt-8">
                <h3 className="text-lg font-hana-bold text-gray-900 mb-4">현재 금리 정보</h3>
                <div className="bg-gray-50 p-6 rounded-lg">
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">6개월</div>
                      <div className="text-xl font-hana-bold text-hana-green">2.07%</div>
                    </div>
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">1년</div>
                      <div className="text-xl font-hana-bold text-hana-green">2.40%</div>
                    </div>
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">2년</div>
                      <div className="text-xl font-hana-bold text-hana-green">2.00%</div>
                    </div>
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">3년</div>
                      <div className="text-xl font-hana-bold text-hana-green">2.10%</div>
                    </div>
                  </div>
                  <p className="text-xs text-gray-500 mt-3 text-center">
                    * 금리는 시장 상황에 따라 변동될 수 있습니다.
                  </p>
                </div>
              </div>
            </div>
          )}

          {}
          {activeTab === 'regulations' && (
            <div className="bg-white rounded-xl shadow-lg p-8">
              <h2 className="text-2xl font-hana-bold text-gray-900 mb-6">IRP 관련 규정 및 정책</h2>

              <div className="space-y-8">
                {}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">세제 혜택</h3>
                  <div className="bg-green-50 p-6 rounded-lg border border-green-200">
                    <h4 className="font-hana-medium text-green-700 mb-3">소득공제 혜택</h4>
                    <div className="space-y-3 text-sm text-gray-700">
                      <p>• 연간 납입금의 13.2% 소득공제 (700만원 한도)</p>
                      <p>• 총 급여의 8.25%까지 추가 납입 가능</p>
                      <p>• 세액공제와 소득공제 중 선택 가능</p>
                    </div>

                    <h4 className="font-hana-medium text-green-700 mb-3 mt-6">비과세 혜택</h4>
                    <div className="space-y-3 text-sm text-gray-700">
                      <p>• 운용수익에 대한 비과세 (이자소득세 15.4% 면제)</p>
                      <p>• 연금 수령 시 연금소득세 적용 (퇴직소득세 면제)</p>
                      <p>• 일시금 수령 시 퇴직소득세 30% 감면</p>
                    </div>
                  </div>
                </div>

                {}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">납입 한도 및 제한사항</h3>
                  <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                    <div className="bg-blue-50 p-6 rounded-lg">
                      <h4 className="font-hana-medium text-blue-600 mb-3">연간 납입 한도</h4>
                      <ul className="text-sm text-gray-700 space-y-2">
                        <li>• 기본 한도: 700만원 (13.2% 소득공제)</li>
                        <li>• 추가 한도: 총 급여의 8.25%까지</li>
                        <li>• 최대 1,800만원까지 납입 가능</li>
                      </ul>
                    </div>

                    <div className="bg-orange-50 p-6 rounded-lg">
                      <h4 className="font-hana-medium text-orange-600 mb-3">중도인출 제한</h4>
                      <ul className="text-sm text-gray-700 space-y-2">
                        <li>• 무주택자 주택 구입 시 1회 한도</li>
                        <li>• 6개월 이상 요양 필요 시</li>
                        <li>• 개인회생 절차 개시 시</li>
                        <li>• 천재지변 등 긴급한 사유</li>
                      </ul>
                    </div>
                  </div>
                </div>

                {}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">연금 수령 규정</h3>
                  <div className="bg-purple-50 p-6 rounded-lg border border-purple-200">
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      <div>
                        <h4 className="font-hana-medium text-purple-700 mb-3">수령 개시 연령</h4>
                        <ul className="text-sm text-gray-700 space-y-2">
                          <li>• 만 55세부터 연금 수령 가능</li>
                          <li>• 최소 5년 이상 가입 유지 필요</li>
                          <li>• 조기 수령 시 감액 적용</li>
                        </ul>
                      </div>

                      <div>
                        <h4 className="font-hana-medium text-purple-700 mb-3">수령 방법</h4>
                        <ul className="text-sm text-gray-700 space-y-2">
                          <li>• 종신 연금 (평생 수령)</li>
                          <li>• 확정 기간 연금 (10년 이상)</li>
                          <li>• 상속 신탁 활용 가능</li>
                        </ul>
                      </div>
                    </div>
                  </div>
                </div>

                {}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">투자 규제 및 제한</h3>
                  <div className="bg-gray-50 p-6 rounded-lg">
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                      <div className="text-center">
                        <div className="text-lg font-hana-bold text-gray-900 mb-2">채권 비중</div>
                        <div className="text-sm text-gray-600">최소 60%</div>
                      </div>
                      <div className="text-center">
                        <div className="text-lg font-hana-bold text-gray-900 mb-2">주식 비중</div>
                        <div className="text-sm text-gray-600">최대 40%</div>
                      </div>
                      <div className="text-center">
                        <div className="text-lg font-hana-bold text-gray-900 mb-2">해외투자</div>
                        <div className="text-sm text-gray-600">최대 30%</div>
                      </div>
                    </div>
                    <p className="text-xs text-gray-500 mt-4 text-center">
                      * 원리금 보장 상품의 비중을 고려한 포트폴리오 구성 필요
                    </p>
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