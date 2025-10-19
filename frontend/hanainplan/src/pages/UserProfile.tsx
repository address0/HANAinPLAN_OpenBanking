import { useState, useEffect } from 'react';
import Layout from '../components/layout/Layout';
import { useUserStore } from '../store/userStore';
import type { RiskProfileRequest, RiskProfileResponse, QuestionAnswer } from '../api/productApi';
import { getRiskProfile, evaluateRiskProfile } from '../api/productApi';
// recharts import 제거 (더 이상 사용하지 않음)

function UserProfile() {
  const { user } = useUserStore();
  const [isEditing, setIsEditing] = useState(false);
  const [formData, setFormData] = useState({
    name: user?.name || '',
    email: user?.email || '',
    phone: user?.phoneNumber || ''
  });

  // 리스크 프로파일 관련 상태
  const [riskProfile, setRiskProfile] = useState<RiskProfileResponse | null>(null);
  const [showRiskModal, setShowRiskModal] = useState(false);
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [surveyAnswers, setSurveyAnswers] = useState<QuestionAnswer[]>([]);
  const [isLoading, setIsLoading] = useState(false);

  // 리스크 프로파일 질문과 답변 정의
  const riskProfileQuestions = [
    "귀하의 투자 경험은 어느 정도입니까?",
    "투자금의 일부 손실 가능성에 대해 어떻게 생각하십니까?",
    "IRP 계좌의 운용 기간(인출 시점까지)은 어느 정도입니까?",
    "투자 성과를 평가할 때 가장 중요하게 생각하는 것은?",
    "최근 1년간 금융상품 투자 시, 변동성에 따른 대응 방식은?",
    "현재의 전체 자산 대비 투자 가능 비율은 어느 정도입니까?",
    "IRP를 통해 얻고 싶은 주요 목표는 무엇입니까?"
  ];

  const riskProfileAnswers = [
    ["전혀 없음", "예·적금만 있음", "펀드 등 간접투자 경험 있음", "직접투자(주식 등) 경험 있음", "3년 이상 투자활동 지속"],
    ["절대 손실 원하지 않음", "약간의 손실은 감수 가능", "수익을 위해 손실도 감수 가능", "손실을 감수하더라도 고수익 추구", "큰 손실 가능해도 고수익 기대"],
    ["3년 미만", "3~5년", "5~10년", "10~20년", "20년 이상"],
    ["원금 보전", "안정적인 이자 수익", "예금보다 높은 안정적 수익", "시장 평균 이상의 수익", "높은 기대수익률"],
    ["손실 시 즉시 해지", "일부 축소", "유지", "저가 매수", "추가 매수"],
    ["10% 이하", "20% 이하", "30% 이하", "50% 이하", "50% 이상"],
    ["안정적 퇴직금 운용", "물가 상승 방어", "중위험 중수익", "장기 성장성 확보", "공격적 수익 추구"]
  ];

  const handleInputChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
      setIsEditing(false);
  };

  // 리스크 프로파일 로드
  const loadRiskProfile = async () => {
    if (!user?.id) return;
    try {
      const profile = await getRiskProfile(user.id);
      setRiskProfile(profile);
    } catch (error) {
      console.error('리스크 프로파일 로드 실패:', error);
    }
  };

  // 설문 답변 처리
  const handleSurveyAnswer = (answerScore: number) => {
    const questionNumber = currentQuestionIndex + 1;
    const answer: QuestionAnswer = {
      questionNumber,
      answerScore,
      questionText: riskProfileQuestions[currentQuestionIndex],
      answerText: riskProfileAnswers[currentQuestionIndex]?.[answerScore - 1] || ''
    };

    const updatedAnswers = [...surveyAnswers];
    const existingAnswerIndex = updatedAnswers.findIndex(a => a.questionNumber === questionNumber);
    
    if (existingAnswerIndex >= 0) {
      updatedAnswers[existingAnswerIndex] = answer;
    } else {
      updatedAnswers.push(answer);
    }
    
    setSurveyAnswers(updatedAnswers);
    
    // 다음 질문으로 이동
    if (currentQuestionIndex < riskProfileQuestions.length - 1) {
      setCurrentQuestionIndex(currentQuestionIndex + 1);
    } else {
      // 설문 완료 - 리스크 프로파일 평가
      submitRiskProfileSurvey(updatedAnswers);
    }
  };

  // 리스크 프로파일 설문 제출
  const submitRiskProfileSurvey = async (answers: QuestionAnswer[]) => {
    if (!user?.id) return;
    try {
      setIsLoading(true);
      const request: RiskProfileRequest = {
        customerId: user.id,
        sessionId: `session_${Date.now()}`,
        answers
      };

      const result = await evaluateRiskProfile(request);
      setRiskProfile(result);
      setShowRiskModal(false);
      setCurrentQuestionIndex(0);
      setSurveyAnswers([]);
    } catch (error) {
      console.error('리스크 프로파일 평가 실패:', error);
    } finally {
      setIsLoading(false);
    }
  };

  // 리스크 프로파일 재설문 시작
  const startRiskProfileSurvey = () => {
    setCurrentQuestionIndex(0);
    setSurveyAnswers([]);
    setShowRiskModal(true);
  };

  // 컴포넌트 마운트 시 리스크 프로파일 로드
  useEffect(() => {
    loadRiskProfile();
  }, [user?.id]);

  if (!user) {
    return (
      <Layout>
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <div className="text-center">
            <p className="text-gray-600">로그인이 필요합니다.</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50 py-8">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8">
            <div className="flex items-center gap-4 mb-8">
              <div className="w-16 h-16 bg-hana-green/10 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-hana-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
              </div>
              <div>
                <h1 className="text-2xl font-hana-medium text-gray-900">내 정보</h1>
                <p className="text-gray-600 font-hana-light">개인 정보를 확인하고 수정할 수 있습니다.</p>
              </div>
            </div>

            <form onSubmit={handleSubmit} className="space-y-6">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    이름
                  </label>
                  {isEditing ? (
                    <input
                      type="text"
                      name="name"
                      value={formData.name}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      required
                    />
                  ) : (
                    <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                      {user.name}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    이메일
                  </label>
                  {isEditing ? (
                    <input
                      type="email"
                      name="email"
                      value={formData.email}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      required
                    />
                  ) : (
                    <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                      {user.email}
                    </div>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    전화번호
                  </label>
                  {isEditing ? (
                    <input
                      type="tel"
                      name="phone"
                      value={formData.phone}
                      onChange={handleInputChange}
                      className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                      required
                    />
                  ) : (
                    <div className="w-full px-4 py-3 bg-gray-50 border border-gray-200 rounded-lg text-gray-900">
                      {user.phoneNumber}
                    </div>
                  )}
                </div>

              </div>

              <div className="flex justify-end gap-4 pt-6 border-t border-gray-200">
                {isEditing ? (
                  <>
                    <button
                      type="button"
                      onClick={() => setIsEditing(false)}
                      className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-hana-medium"
                    >
                      취소
                    </button>
                    <button
                      type="submit"
                      className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                    >
                      저장
                    </button>
                  </>
                ) : (
                <button
                    type="button"
                    onClick={() => setIsEditing(true)}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                  >
                    수정
                </button>
                )}
              </div>
            </form>

            {/* 리스크 프로파일 섹션 */}
            <div className="mt-12 pt-8 border-t border-gray-200">
              <div className="flex items-center justify-between mb-6">
                <div>
                  <h2 className="text-xl font-hana-medium text-gray-900">투자 성향 분석</h2>
                  <p className="text-gray-600 font-hana-light">현재 투자 성향과 리스크 프로파일을 확인하세요.</p>
                </div>
                <button
                  onClick={startRiskProfileSurvey}
                  className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium text-sm"
                >
                  {riskProfile ? '재설문' : '설문하기'}
                </button>
              </div>

              {riskProfile ? (
                <div className="space-y-6">
                  {/* 리스크 프로파일 결과 카드 */}
                  <div className="bg-gradient-to-r from-hana-green to-green-600 text-white p-6 rounded-xl">
                    <div className="text-center">
                      <div className="text-2xl font-hana-bold mb-2">{riskProfile.riskProfileTypeName}</div>
                      <div className="text-lg font-hana-medium opacity-90">리스크 점수: {riskProfile.riskProfileScore}점</div>
                    </div>
                  </div>

                  {/* 위험도 스펙트럼 차트 */}
                  <div className="bg-white p-6 rounded-lg border border-gray-200">
                    <h3 className="text-lg font-hana-medium text-gray-900 mb-4">내 투자성향</h3>
                    <div className="space-y-4">
                      {/* 위험도 스펙트럼 바 */}
                      <div className="relative">
                        <div className="flex h-12 rounded-lg overflow-visible">
                          {/* 안정형 */}
                          <div className="flex-1 bg-green-600 flex items-center justify-center relative">
                            <span className="text-white text-xs font-hana-medium">안정형</span>
                            {riskProfile.riskProfileType === 'STABLE' && (
                              <div className="absolute -bottom-3 left-1/2 transform -translate-x-1/2">
                                <div className="w-0 h-0 border-l-4 border-r-4 border-t-4 border-l-transparent border-r-transparent border-t-hana-green"></div>
                              </div>
                            )}
                          </div>
                          {/* 안정추구형 */}
                          <div className="flex-1 bg-green-500 flex items-center justify-center relative">
                            <span className="text-white text-xs font-hana-medium">안정추구형</span>
                            {riskProfile.riskProfileType === 'STABLE_PLUS' && (
                              <div className="absolute -bottom-3 left-1/2 transform -translate-x-1/2">
                                <div className="w-0 h-0 border-l-4 border-r-4 border-t-4 border-l-transparent border-r-transparent border-t-hana-green"></div>
                              </div>
                            )}
                          </div>
                          {/* 중립형 */}
                          <div className="flex-1 bg-yellow-500 flex items-center justify-center relative">
                            <span className="text-white text-xs font-hana-medium">중립형</span>
                            {riskProfile.riskProfileType === 'NEUTRAL' && (
                              <div className="absolute -bottom-3 left-1/2 transform -translate-x-1/2">
                                <div className="w-0 h-0 border-l-4 border-r-4 border-t-4 border-l-transparent border-r-transparent border-t-hana-green"></div>
                              </div>
                            )}
                          </div>
                          {/* 적극형 */}
                          <div className="flex-1 bg-orange-500 flex items-center justify-center relative">
                            <span className="text-white text-xs font-hana-medium">적극형</span>
                            {riskProfile.riskProfileType === 'AGGRESSIVE' && (
                              <div className="absolute -bottom-3 left-1/2 transform -translate-x-1/2">
                                <div className="w-0 h-0 border-l-4 border-r-4 border-t-4 border-l-transparent border-r-transparent border-t-hana-green"></div>
                              </div>
                            )}
                          </div>
                          {/* 공격투자형 */}
                          <div className="flex-1 bg-red-500 flex items-center justify-center relative">
                            <span className="text-white text-xs font-hana-medium">공격투자형</span>
                          </div>
                        </div>
                        
                        {/* 사용자 위치 표시 */}
                        <div className="mt-4 text-center">
                          <div className="inline-flex items-center px-6 py-3 bg-white border-2 border-hana-green rounded-xl shadow-sm">
                            <div className="w-4 h-4 bg-hana-green rounded-full mr-3 animate-pulse"></div>
                            <div className="text-left">
                              <div className="text-sm font-hana-medium text-gray-600">
                                {user?.name}님의 투자성향
                              </div>
                              <div className="text-lg font-hana-bold text-hana-green">
                                {riskProfile.riskProfileType === 'STABLE' && '안정형'}
                                {riskProfile.riskProfileType === 'STABLE_PLUS' && '안정추구형'}
                                {riskProfile.riskProfileType === 'NEUTRAL' && '중립형'}
                                {riskProfile.riskProfileType === 'AGGRESSIVE' && '적극형'}
                              </div>
                            </div>
                          </div>
                        </div>
                        
                        {/* 투자 성향 설명 */}
                        <div className="mt-4 p-4 bg-blue-50 rounded-lg border border-blue-200">
                          <div className="text-center">
                            <div className="text-sm font-hana-medium text-blue-800 mb-2">투자 성향 설명</div>
                            <div className="text-xs text-blue-700 leading-relaxed">
                              {riskProfile.riskProfileType === 'STABLE' && '안정형은 원금 보전을 최우선으로 하는 보수적인 투자 성향입니다.'}
                              {riskProfile.riskProfileType === 'STABLE_PLUS' && '안정추구형은 약간의 위험을 감수하되 안정성을 중시하는 투자 성향입니다.'}
                              {riskProfile.riskProfileType === 'NEUTRAL' && '중립형은 위험과 수익의 균형을 추구하는 중립적인 투자 성향입니다.'}
                              {riskProfile.riskProfileType === 'AGGRESSIVE' && '적극형은 높은 수익을 위해 위험을 감수할 수 있는 공격적인 투자 성향입니다.'}
                            </div>
                          </div>
                        </div>
                      </div>

                      {/* 위험도 점수 표시 */}
                      <div className="grid grid-cols-3 gap-4 text-center">
                        <div className="bg-gray-50 p-3 rounded-lg">
                          <div className="text-lg font-hana-bold text-hana-green">{riskProfile.riskProfileScore}</div>
                          <div className="text-xs text-gray-600">리스크 점수</div>
                        </div>
                        <div className="bg-gray-50 p-3 rounded-lg">
                          <div className="text-lg font-hana-bold text-gray-800">
                            {riskProfile.riskProfileType === 'STABLE' && '1.0-1.9'}
                            {riskProfile.riskProfileType === 'STABLE_PLUS' && '1.9-2.7'}
                            {riskProfile.riskProfileType === 'NEUTRAL' && '2.7-3.5'}
                            {riskProfile.riskProfileType === 'AGGRESSIVE' && '3.5-5.0'}
                          </div>
                          <div className="text-xs text-gray-600">점수 범위</div>
                        </div>
                        <div className="bg-gray-50 p-3 rounded-lg">
                          <div className="text-lg font-hana-bold text-gray-800">2년</div>
                          <div className="text-xs text-gray-600">유효기간</div>
                        </div>
                      </div>
                    </div>
                  </div>

                </div>
              ) : (
                <div className="text-center py-12">
                  <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
                    <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                    </svg>
                  </div>
                  <h3 className="text-lg font-hana-medium text-gray-900 mb-2">투자 성향 분석이 없습니다</h3>
                  <p className="text-gray-600 mb-4">설문을 통해 투자 성향을 분석해보세요.</p>
                  <button
                    onClick={startRiskProfileSurvey}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                  >
                    투자 성향 설문하기
                  </button>
                </div>
              )}
            </div>
            </div>
          </div>
      </div>

      {/* 리스크 프로파일 설문 모달 */}
      {showRiskModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            <div className="bg-hana-green text-white p-6 rounded-t-2xl">
              <div className="flex justify-between items-center">
                <h2 className="text-2xl font-hana-medium">투자 성향 측정</h2>
                <button
                  onClick={() => setShowRiskModal(false)}
                  className="text-white hover:text-gray-200 transition-colors"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            <div className="p-6">
              <div className="text-center mb-6">
                <h3 className="text-xl font-hana-medium mb-2">투자 성향 측정({currentQuestionIndex + 1}/7)</h3>
                <p className="text-gray-600 font-hana-light">
                  고객님의 투자 성향을 정확히 파악하기 위한 설문입니다
                </p>
              </div>
              
              <div className="space-y-4 text-left max-w-lg mx-auto">
                <div className="bg-gray-50 p-4 rounded-lg mb-6">
                  <h4 className="font-hana-medium text-gray-800 mb-3">
                    {riskProfileQuestions[currentQuestionIndex]}
                  </h4>
                  <div className="space-y-2">
                    {riskProfileAnswers[currentQuestionIndex]?.map((option, index) => (
                      <button
                        key={index}
                        onClick={() => handleSurveyAnswer(index + 1)}
                        className="w-full p-3 text-left border-2 border-gray-200 rounded-lg hover:border-hana-green hover:bg-green-50 transition-colors"
                      >
                        <div className="flex items-center">
                          <div className="w-6 h-6 border-2 border-gray-300 rounded-full mr-3 flex items-center justify-center">
                            <div className="w-3 h-3 bg-hana-green rounded-full opacity-0"></div>
                          </div>
                          <span className="font-hana-regular">{option}</span>
                        </div>
                      </button>
                    ))}
                  </div>
                </div>
                
                <div className="flex justify-center">
                  <div className="flex space-x-2">
                    {Array.from({ length: 7 }).map((_, index) => (
                      <div
                        key={index}
                        className={`w-3 h-3 rounded-full ${
                          index <= currentQuestionIndex ? 'bg-hana-green' : 'bg-gray-300'
                        }`}
                      />
                    ))}
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default UserProfile;