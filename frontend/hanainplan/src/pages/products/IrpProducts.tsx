import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import Layout from '../../components/layout/Layout';

function IrpProducts() {
  const navigate = useNavigate();
  const [showModal, setShowModal] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);

  const handleApplyClick = () => {
    setShowModal(true);
    setCurrentStep(1);
  };

  const handleConsultationClick = () => {
    // 상담 신청 페이지로 이동
    navigate('/consultation/request');
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {/* Main Content */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Hero Section with Character */}
          <div className="bg-gradient-to-br from-hana-green/5 to-blue-50 rounded-2xl p-8 mb-8">
            <div className="flex flex-col lg:flex-row items-center gap-8">
              <div className="flex-1">
                <h1 className="text-4xl font-hana-medium text-gray-900 mb-4">IRP로 안전한 노후 준비하세요</h1>
                <p className="text-xl font-hana-light text-gray-600 mb-6">
                  개인형퇴직연금(IRP)으로 퇴직 후에도 안정적인 생활을 보장받으세요
                </p>
                <div className="flex flex-col sm:flex-row gap-4">
                  <button
                    onClick={handleApplyClick}
                    className="bg-hana-green text-white px-8 py-4 rounded-xl font-hana-bold text-lg hover:bg-green-600 transition-colors shadow-lg"
                  >
                    하나은행 IRP 계좌 개설하기
                  </button>
                  <button 
                    onClick={handleConsultationClick}
                    className="bg-white border-2 border-hana-green text-hana-green px-8 py-4 rounded-xl font-hana-bold text-lg hover:bg-hana-green hover:text-white transition-colors"
                  >
                    전문가 상담받기
                  </button>
                </div>
                <div className="mt-4 p-4 bg-blue-50 border border-blue-200 rounded-lg">
                  <p className="text-blue-800 font-hana-light text-sm">
                    💡 <strong className="font-hana-medium">하나은행 IRP 계좌를 개설하면</strong> 여러 금융기관에 분산된 퇴직연금과 개인연금을
                    <br />
                    HANAinPLAN에서 통합 관리할 수 있습니다.
                  </p>
                </div>
              </div>
              <div className="flex-shrink-0">
                <div className="w-64 h-64 flex items-center justify-center">
                  <img 
                    src="/character/irp.png" 
                    alt="IRP 캐릭터" 
                    className="w-full h-full object-contain"
                  />
                </div>
              </div>
            </div>
          </div>

          {/* Product Information */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h2 className="text-2xl font-hana-medium text-gray-900 mb-6">IRP(개인형퇴직연금)란?</h2>
            <div className="space-y-4 text-gray-700 font-hana-light">
              <p className="text-lg">
                IRP는 개인이 퇴직 후 안정적인 노후 생활을 위해 자발적으로 가입하는 개인형 퇴직연금 상품입니다.
              </p>
              <div className="bg-blue-50 border-l-4 border-blue-400 p-4 my-4">
                <p className="text-blue-800 font-medium">
                  💡 <strong>DB형 퇴직연금에 가입된 재직자라면 퇴사나 이직할 때 IRP 계좌가 필요합니다.</strong>
                </p>
                <p className="text-blue-700 text-sm mt-2">
                  DB형 퇴직연금(확정급여형)은 퇴직 시 퇴직금을 수령할 수 없어 IRP 계좌로 의무 이관해야 합니다.
                </p>
              </div>
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                <div className="bg-gray-50 p-6 rounded-lg border border-gray-100">
                  <h3 className="text-lg font-hana-medium text-gray-900 mb-3">주요 특징</h3>
                  <ul className="space-y-2 text-gray-600 font-hana-light">
                    <li>• 퇴직금 이관 및 추가 납입 가능</li>
                    <li>• 다양한 투자 옵션 제공</li>
                    <li>• 55세부터 연금 또는 일시금 수령 가능</li>
                  </ul>
                </div>
                <div className="bg-gray-50 p-6 rounded-lg border border-gray-100">
                  <h3 className="text-lg font-hana-medium text-gray-900 mb-3">고객 혜택</h3>
                  <ul className="space-y-2 text-gray-600 font-hana-light">
                    <li>• 연간 최대 900만원 세액공제</li>
                    <li>• 퇴직소득세 절세 효과</li>
                    <li>• 장기 투자를 통한 자산 증식</li>
                  </ul>
                </div>
              </div>
            </div>
          </div>

          {/* IRP 혜택 섹션 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">IRP 주요 혜택</h3>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="text-center p-6 bg-gray-50 rounded-lg border border-gray-100">
                <div className="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">💰</span>
                </div>
                <h4 className="font-hana-bold text-lg mb-2">퇴직금 이관</h4>
                <p className="text-gray-600 text-sm">기존 퇴직금을 하나은행 IRP로 안전하게 이관</p>
              </div>
              <div className="text-center p-6 bg-gray-50 rounded-lg border border-gray-100">
                <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">📈</span>
                </div>
                <h4 className="font-hana-bold text-lg mb-2">추가 납입</h4>
                <p className="text-gray-600 text-sm">연간 최대 900만원까지 추가 납입 가능</p>
              </div>
              <div className="text-center p-6 bg-gray-50 rounded-lg border border-gray-100">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">🏆</span>
                </div>
                <h4 className="font-hana-bold text-lg mb-2">세제 혜택</h4>
                <p className="text-gray-600 text-sm">연간 최대 900만원 세액공제 혜택</p>
              </div>
            </div>
          </div>

          {/* 연소득별 세제혜택 시각화 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">IRP 세제 혜택 정리</h3>
            <p className="text-gray-600 font-hana-light text-center mb-8">
              소득 수준별로 다른 세액공제율과 납입한도를 적용받습니다
            </p>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {/* 세제혜택 표 */}
              <div className="space-y-6">
                <h4 className="text-xl font-hana-medium text-gray-900 mb-4">세액공제율</h4>
                <div className="space-y-4">
                  <div className="flex justify-between items-center p-4 bg-green-50 rounded-lg border border-green-200">
                    <div>
                      <span className="font-hana-bold text-lg text-green-800">총급여 5,500만원 이하</span>
                      <p className="text-green-600 text-sm mt-1">16.5% 세액공제</p>
                    </div>
                    <div className="text-right">
                      <span className="text-2xl font-hana-bold text-green-700">1,155,000원</span>
                      <p className="text-green-600 text-sm">700만원 납입 시</p>
                    </div>
                  </div>
                  <div className="flex justify-between items-center p-4 bg-blue-50 rounded-lg border border-blue-200">
                    <div>
                      <span className="font-hana-bold text-lg text-blue-800">총급여 5,500만원 초과</span>
                      <p className="text-blue-600 text-sm mt-1">13.2% 세액공제</p>
                    </div>
                    <div className="text-right">
                      <span className="text-2xl font-hana-bold text-blue-700">924,000원</span>
                      <p className="text-blue-600 text-sm">700만원 납입 시</p>
                    </div>
                  </div>
                </div>
              </div>

              {/* 시각적 차트 */}
              <div className="space-y-6">
                <h4 className="text-xl font-hana-medium text-gray-900 mb-4">혜택 비교 차트</h4>
                <div className="space-y-4">
                  {/* 5,500만원 이하 차트 바 */}
                  <div>
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-medium text-gray-700">총급여 5,500만원 이하</span>
                      <span className="text-green-600 font-hana-bold">1,155,000원 절세</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-8 relative overflow-hidden">
                      <div
                        className="bg-gradient-to-r from-green-400 to-green-600 h-full rounded-full transition-all duration-1000 ease-out relative"
                        style={{ width: '77%' }}
                      >
                        <div className="absolute inset-0 bg-white/20 animate-pulse"></div>
                      </div>
                      <span className="absolute right-2 top-1/2 transform -translate-y-1/2 text-white text-sm font-bold">77%</span>
                    </div>
                  </div>

                  {/* 5,500만원 초과 차트 바 */}
                  <div>
                    <div className="flex justify-between items-center mb-2">
                      <span className="font-medium text-gray-700">총급여 5,500만원 초과</span>
                      <span className="text-blue-600 font-hana-bold">924,000원 절세</span>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-8 relative overflow-hidden">
                      <div
                        className="bg-gradient-to-r from-blue-400 to-blue-600 h-full rounded-full transition-all duration-1000 ease-out relative"
                        style={{ width: '62%' }}
                      >
                        <div className="absolute inset-0 bg-white/20 animate-pulse"></div>
                      </div>
                      <span className="absolute right-2 top-1/2 transform -translate-y-1/2 text-white text-sm font-bold">62%</span>
                    </div>
                  </div>
                </div>

                <div className="mt-6 p-4 bg-gray-50 rounded-lg">
                  <p className="text-sm text-gray-600">
                    <strong>절세율 계산:</strong> 5,500만원 이하 소득자는 231만원 더 많은 세액공제를 받을 수 있습니다.
                    <br />
                    <span className="text-green-600">→ 고소득자 대비 25% 높은 절세 효과</span>
                  </p>
                </div>
              </div>
            </div>

            {/* 추가 납입 혜택 */}
            <div className="mt-8 grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="text-center p-4 bg-gradient-to-br from-yellow-50 to-orange-50 rounded-lg border border-yellow-200">
                <div className="text-3xl mb-2">💰</div>
                <h5 className="font-hana-medium text-lg text-yellow-800 mb-2">연간 납입한도</h5>
                <p className="text-yellow-700 font-hana-light text-sm">900만원 (총급여 5,500만원 이하)</p>
                <p className="text-yellow-700 font-hana-light text-sm">700만원 (총급여 5,500만원 초과)</p>
              </div>
              <div className="text-center p-4 bg-gradient-to-br from-green-50 to-emerald-50 rounded-lg border border-green-200">
                <div className="text-3xl mb-2">📊</div>
                <h5 className="font-hana-medium text-lg text-green-800 mb-2">세액공제율</h5>
                <p className="text-green-700 font-hana-light text-sm">16.5% (5,500만원 이하)</p>
                <p className="text-green-700 font-hana-light text-sm">13.2% (5,500만원 초과)</p>
              </div>
              <div className="text-center p-4 bg-gradient-to-br from-blue-50 to-cyan-50 rounded-lg border border-blue-200">
                <div className="text-3xl mb-2">🎯</div>
                <h5 className="font-hana-medium text-lg text-blue-800 mb-2">최대 절세효과</h5>
                <p className="text-blue-700 font-hana-light text-sm">1,485,000원 (5,500만원 이하)</p>
                <p className="text-blue-700 font-hana-light text-sm">924,000원 (5,500만원 초과)</p>
              </div>
            </div>
          </div>

          {/* 하나은행 IRP 상품 특징 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">하나은행 IRP 상품 특징</h3>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
              <div className="text-center p-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">🛡️</span>
                </div>
                <h4 className="font-hana-medium text-lg mb-2">안전한 운용</h4>
                <p className="text-gray-600 font-hana-light text-sm">하나자산운용의 전문적인 운용으로 안정적인 수익 추구</p>
              </div>
              <div className="text-center p-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">⚡</span>
                </div>
                <h4 className="font-hana-medium text-lg mb-2">간편한 관리</h4>
                <p className="text-gray-600 font-hana-light text-sm">온라인으로 언제든지 계좌 조회 및 관리 가능</p>
              </div>
              <div className="text-center p-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">🎯</span>
                </div>
                <h4 className="font-hana-medium text-lg mb-2">맞춤형 투자</h4>
                <p className="text-gray-600 font-hana-light text-sm">고객의 위험성향에 맞는 다양한 투자 옵션 제공</p>
              </div>
              <div className="text-center p-4">
                <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <span className="text-2xl">💎</span>
                </div>
                <h4 className="font-hana-medium text-lg mb-2">우수한 수수료</h4>
                <p className="text-gray-600 text-sm">경쟁력 있는 수수료로 고객의 수익 극대화</p>
              </div>
            </div>
          </div>

          {/* 추가 정보 및 FAQ 섹션 */}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">자주 묻는 질문</h3>
            <div className="space-y-4">
              <div className="border-l-4 border-hana-green pl-4 py-2">
                <h4 className="font-hana-medium text-lg mb-2">Q. IRP 가입 자격이 있나요?</h4>
                <p className="text-gray-600 font-hana-light">만 18세 이상의 개인이라면 누구나 가입 가능합니다. 직장인, 자영업자, 프리랜서 등 상관없이 가입하실 수 있습니다.</p>
              </div>
              <div className="border-l-4 border-blue-500 pl-4 py-2">
                <h4 className="font-hana-medium text-lg mb-2">Q. 퇴직금 이관은 어떻게 하나요?</h4>
                <p className="text-gray-600 font-hana-light">기존 회사의 퇴직금을 하나은행 IRP로 이관할 수 있습니다. 이관 절차는 간단하며, 세제 혜택도 그대로 유지됩니다.</p>
              </div>
              <div className="border-l-4 border-purple-500 pl-4 py-2">
                <h4 className="font-hana-medium text-lg mb-2">Q. 언제부터 연금을 받을 수 있나요?</h4>
                <p className="text-gray-600">만 55세부터 연금 또는 일시금으로 수령할 수 있습니다. 연금으로 받으면 세제 혜택이 더욱 커집니다.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {/* IRP 계좌 개설 모달 */}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            {/* 모달 헤더 */}
            <div className="bg-gradient-to-r from-hana-green to-green-600 text-white p-6 rounded-t-2xl">
              <div className="flex justify-between items-center">
                <h2 className="text-2xl font-hana-medium">하나은행 IRP 계좌 개설</h2>
                <button
                  onClick={() => setShowModal(false)}
                  className="text-white hover:text-gray-200 text-2xl"
                >
                  ×
                </button>
              </div>
              <p className="text-green-100 mt-2">단계별로 간편하게 계좌를 개설하세요</p>
            </div>

            {/* 진행 단계 표시 */}
            <div className="flex justify-center p-6 bg-gray-50">
              {[1, 2, 3, 4].map((step) => (
                <div key={step} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-bold ${
                    step <= currentStep
                      ? 'bg-hana-green text-white'
                      : 'bg-gray-300 text-gray-600'
                  }`}>
                    {step}
                  </div>
                  {step < 4 && (
                    <div className={`w-12 h-1 mx-2 ${
                      step < currentStep ? 'bg-hana-green' : 'bg-gray-300'
                    }`} />
                  )}
                </div>
              ))}
            </div>

            {/* 단계별 컨텐츠 */}
            <div className="p-6">
              {currentStep === 1 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">납입금액 설정</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    초기 납입금액과 자동납입 설정을 선택해주세요
                  </p>
                  <div className="space-y-4 text-left max-w-md mx-auto">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        초기 납입금액
                      </label>
                      <select className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent">
                        <option>50만원</option>
                        <option>100만원</option>
                        <option>300만원</option>
                        <option>500만원</option>
                        <option>1,000만원</option>
                      </select>
                      <p className="text-xs text-gray-500 mt-1">최소 50만원부터 시작 가능</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        월 자동납입 설정 (선택사항)
                      </label>
                      <input
                        type="number"
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                        placeholder="예: 30"
                      />
                      <p className="text-xs text-gray-500 mt-1">월 10만원~70만원 권장 (연간 120만원~840만원)</p>
                    </div>
                  </div>
                </div>
              )}

              {currentStep === 2 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">투자 성향 설정</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    고객님의 위험성향에 맞는 포트폴리오를 설정해주세요
                  </p>
                  <div className="space-y-4 text-left max-w-md mx-auto">
                    <div className="space-y-3">
                      <label className="flex items-center p-3 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input type="radio" name="risk" value="conservative" className="mr-3 text-hana-green" />
                        <div>
                          <div className="font-medium">안정형</div>
                          <div className="text-sm text-gray-500">원금손실 가능성 낮음, 안정적 수익 추구</div>
                        </div>
                      </label>
                      <label className="flex items-center p-3 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input type="radio" name="risk" value="moderate-conservative" className="mr-3 text-hana-green" />
                        <div>
                          <div className="font-medium">안정추구형</div>
                          <div className="text-sm text-gray-500">안정성을 우선하되 일부 성장자산 포함</div>
                        </div>
                      </label>
                      <label className="flex items-center p-3 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input type="radio" name="risk" value="moderate" className="mr-3 text-hana-green" />
                        <div>
                          <div className="font-medium">위험중립형</div>
                          <div className="text-sm text-gray-500">위험과 수익의 균형을 추구</div>
                        </div>
                      </label>
                      <label className="flex items-center p-3 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input type="radio" name="risk" value="aggressive" className="mr-3 text-hana-green" />
                        <div>
                          <div className="font-medium">공격투자형</div>
                          <div className="text-sm text-gray-500">고위험 고수익 추구, 적극적 자산운용</div>
                        </div>
                      </label>
                    </div>
                    <div className="bg-blue-50 p-3 rounded-lg">
                      <p className="text-blue-800 text-sm">
                        💡 투자 성향에 따라 채권, 주식, 대체투자 등의 비중이 자동으로 조정됩니다.
                      </p>
                    </div>
                  </div>
                </div>
              )}

              {currentStep === 3 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">약관 동의</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    IRP 계좌 개설을 위한 필수 약관에 동의해주세요
                  </p>
                  <div className="space-y-4 text-left max-w-lg mx-auto max-h-64 overflow-y-auto bg-gray-50 p-4 rounded-lg">
                    <label className="flex items-start">
                      <input type="checkbox" className="mt-1 mr-3" />
                      <div className="text-sm">
                        <div className="font-medium">개인정보 수집 및 이용 동의 (필수)</div>
                        <div className="text-gray-500">IRP 계좌 개설 및 관리를 위한 개인정보 수집에 동의합니다.</div>
                      </div>
                    </label>
                    <label className="flex items-start">
                      <input type="checkbox" className="mt-1 mr-3" />
                      <div className="text-sm">
                        <div className="font-medium">IRP 상품 가입 약관 동의 (필수)</div>
                        <div className="text-gray-500">개인형퇴직연금 가입 조건 및 운용 원칙에 동의합니다.</div>
                      </div>
                    </label>
                    <label className="flex items-start">
                      <input type="checkbox" className="mt-1 mr-3" />
                      <div className="text-sm">
                        <div className="font-medium">퇴직연금 운용 관리 약관 동의 (필수)</div>
                        <div className="text-gray-500">퇴직연금 자산 운용 및 관리에 관한 사항에 동의합니다.</div>
                      </div>
                    </label>
                    <label className="flex items-start">
                      <input type="checkbox" className="mt-1 mr-3" />
                      <div className="text-sm">
                        <div className="font-medium">전자금융거래 이용약관 동의 (필수)</div>
                        <div className="text-gray-500">온라인 뱅킹 및 전자금융거래 서비스 이용에 동의합니다.</div>
                      </div>
                    </label>
                  </div>
                </div>
              )}

              {currentStep === 4 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">신청 완료!</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    하나은행 IRP 계좌 개설 신청이 완료되었습니다!
                    <br />
                    빠른 시일 내에 연락드리겠습니다.
                  </p>
                  <div className="bg-green-50 p-4 rounded-lg">
                    <p className="text-green-800 font-hana-light text-sm">
                      📞 문의사항이 있으시면 언제든지 연락주세요
                      <br />
                      📧 이메일: support@kebhana.com
                      <br />
                      💼 하나은행 영업점 방문도 가능합니다.
                    </p>
                  </div>
                </div>
              )}
            </div>

            {/* 모달 버튼 */}
            <div className="flex justify-between p-6 bg-gray-50 rounded-b-2xl">
              <button
                onClick={() => setShowModal(false)}
                className="px-6 py-3 text-gray-600 hover:text-gray-800 font-medium"
              >
                취소
              </button>
              <div className="space-x-3">
                {currentStep > 1 && (
                  <button
                    onClick={() => setCurrentStep(currentStep - 1)}
                    className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 font-medium"
                  >
                    이전
                  </button>
                )}
                {currentStep < 4 ? (
                  <button
                    onClick={() => setCurrentStep(currentStep + 1)}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 font-medium"
                  >
                    다음
                  </button>
                ) : (
                  <button
                    onClick={() => {
                      setShowModal(false);
                      alert('하나은행 IRP 계좌 개설 신청이 완료되었습니다! 빠른 시일 내에 연락드리겠습니다.');
                    }}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 font-medium"
                  >
                    완료
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default IrpProducts;