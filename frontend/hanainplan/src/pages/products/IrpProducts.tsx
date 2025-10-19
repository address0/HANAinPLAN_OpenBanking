import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Layout from '../../components/layout/Layout';
import type { IrpAccountOpenRequest, RiskProfileRequest, RiskProfileResponse, QuestionAnswer } from '../../api/productApi';
import { openIrpAccount, checkIrpAccountStatus, evaluateRiskProfile } from '../../api/productApi';
import { getActiveBankingAccounts, type BankingAccount } from '../../api/bankingApi';
import { useUserStore } from '../../store/userStore';
import { getBankByAccountNumber } from '../../store/bankStore';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';

function IrpProducts() {
  const navigate = useNavigate();
  const { user } = useUserStore();
  const [showModal, setShowModal] = useState(false);
  const [currentStep, setCurrentStep] = useState(1);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [hasIrpAccount, setHasIrpAccount] = useState<boolean | null>(null);
  const [irpAccountInfo, setIrpAccountInfo] = useState<any>(null);
  const [accounts, setAccounts] = useState<BankingAccount[]>([]);
  const [showAccountDropdown, setShowAccountDropdown] = useState(false);

  // 리스크 프로파일 관련 상태
  const [currentQuestionIndex, setCurrentQuestionIndex] = useState(0);
  const [surveyAnswers, setSurveyAnswers] = useState<QuestionAnswer[]>([]);
  const [riskProfileResult, setRiskProfileResult] = useState<RiskProfileResponse | null>(null);

  // 리스크 프로파일 질문과 답변 정의 (프론트엔드에서 직접)
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

  // 절세 효과 차트 데이터
  const taxSavingsData = [
    {
      category: '5,500만원 이하',
      amount: 1155000,
      percentage: 77,
      color: '#10b981'
    },
    {
      category: '5,500만원 초과',
      amount: 924000,
      percentage: 62,
      color: '#3b82f6'
    }
  ];
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [createdAccountInfo, setCreatedAccountInfo] = useState<any>(null);

  const [formData, setFormData] = useState<IrpAccountOpenRequest>({
    customerId: user?.id || 0,
    initialDeposit: 1000000,
    monthlyDeposit: 300000,
    isAutoDeposit: true,
    depositDay: 25,
    investmentStyle: 'CONSERVATIVE',
    linkedMainAccount: ''
  });

  const [showTermsModal, setShowTermsModal] = useState(false);
  const [selectedTermType, setSelectedTermType] = useState('');
  const [agreedTerms, setAgreedTerms] = useState({
    privacy: false,
    irp: false,
    management: false,
    electronic: false
  });
  const [allAgreed, setAllAgreed] = useState(false);

  const getBankInfo = (accountNumber: string) => {
    if (!accountNumber) return null;

    const bankInfo = getBankByAccountNumber(accountNumber);

    if (bankInfo) {
      return {
        code: bankInfo.code,
        name: bankInfo.name,
        logo: `/bank/${bankInfo.code}.png`
      };
    }

    const prefix = accountNumber.replace(/-/g, '').substring(0, 3);
    return {
      code: prefix,
      name: '기타은행',
      logo: null
    };
  };

  useEffect(() => {
    const initializeData = async () => {
      if (user?.id) {
        try {
          const irpResponse = await checkIrpAccountStatus(user.id);
          setHasIrpAccount(irpResponse.hasIrpAccount);

          const activeAccountsResponse = await getActiveBankingAccounts(user.id);
          setAccounts(activeAccountsResponse);

          if (activeAccountsResponse.length > 0) {
            setFormData(prev => ({
              ...prev,
              linkedMainAccount: activeAccountsResponse[0].accountNumber
            }));
          }

        } catch (error) {
          setHasIrpAccount(false);
          setAccounts([]);
        }
      } else {
        setHasIrpAccount(false);
        setAccounts([]);
      }
    };

    initializeData();
  }, [user?.id]);


  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const target = event.target as HTMLElement;
      if (!target.closest('.account-dropdown')) {
        setShowAccountDropdown(false);
      }
    };

    if (showAccountDropdown) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [showAccountDropdown]);

  const handleShowTermsModal = (termType: string) => {
    setSelectedTermType(termType);
    setShowTermsModal(true);
  };

  const handleTermAgreement = (termType: keyof typeof agreedTerms) => {
    setAgreedTerms(prev => ({
      ...prev,
      [termType]: !prev[termType]
    }));
  };

  const handleAllAgreement = () => {
    const newAllAgreed = !allAgreed;
    setAllAgreed(newAllAgreed);
    setAgreedTerms({
      privacy: newAllAgreed,
      irp: newAllAgreed,
      management: newAllAgreed,
      electronic: newAllAgreed
    });
  };

  const isAllTermsAgreed = Object.values(agreedTerms).every(agreed => agreed);

  const handleApplyClick = () => {
    setShowModal(true);
    setCurrentStep(1);
  };

  const handleConsultationClick = () => {
    navigate('/consultation/request');
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
    try {
      setIsLoading(true);
      const request: RiskProfileRequest = {
        customerId: user?.id || 0,
        sessionId: `session_${Date.now()}`,
        answers
      };

      const result = await evaluateRiskProfile(request);
      setRiskProfileResult(result);
      
      // 폼 데이터에 리스크 프로파일 결과 저장
      setFormData(prev => ({
        ...prev,
        investmentStyle: mapRiskTypeToInvestmentStyle(result.riskProfileType)
      }));
      
      setCurrentStep(3); // 결과 화면으로 이동
    } catch (error) {
      console.error('리스크 프로파일 평가 실패:', error);
      setError('리스크 프로파일 평가 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  // 리스크 타입을 투자 스타일로 매핑
  const mapRiskTypeToInvestmentStyle = (riskType: string): 'CONSERVATIVE' | 'MODERATE_CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE' => {
    switch (riskType) {
      case 'CONSERVATIVE':
        return 'CONSERVATIVE';
      case 'MODERATE_CONSERVATIVE':
        return 'MODERATE_CONSERVATIVE';
      case 'MODERATE':
        return 'MODERATE';
      case 'AGGRESSIVE':
        return 'AGGRESSIVE';
      default:
        return 'MODERATE';
    }
  };

  const handleIrpAccountCreation = async () => {
    setIsLoading(true);
    setError(null);

    try {
      const response = await openIrpAccount(formData);

      if (response.success) {
        setCreatedAccountInfo({
          accountNumber: response.accountNumber,
          bankName: '하나은행',
          initialDeposit: formData.initialDeposit,
          monthlyDeposit: formData.monthlyDeposit,
          depositDay: formData.depositDay,
          investmentStyle: formData.investmentStyle
        });
        setShowModal(false);
        setShowSuccessModal(true);
        setHasIrpAccount(true);
      } else {
        setError(response.message || '계좌 개설에 실패했습니다.');
      }

    } catch (error: any) {
      setError(error.response?.data?.message || '계좌 개설 중 오류가 발생했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  if (!user) {
    return (
      <Layout>
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto mb-4"></div>
            <p className="text-gray-600">정보를 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
          {hasIrpAccount ? (
            <div className="bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-2xl p-6 mb-8">
              <div className="flex items-center gap-4 mb-4">
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <svg className="w-6 h-6 text-hana-green" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="flex-1">
                  <h2 className="text-xl font-hana-medium text-hana-green mb-1">{user?.name}님은 하나은행 IRP 계좌를 보유하고 있습니다</h2>

                </div>
                <button
                  onClick={() => navigate('/portfolio')}
                  className="bg-hana-green text-white px-6 py-3 rounded-xl font-hana-bold hover:bg-green-700 transition-colors"
                >
                  내 IRP 포트폴리오
                </button>
              </div>
            
            </div>
          ) : (
            <div className="bg-white rounded-xl shadow-sm border border-hana-green/30 p-8 mb-8 flex flex-col items-center justify-center">
              <h3 className="text-2xl font-hana-medium text-hana-green mb-2 text-center">{user?.name}님은 하나은행 IRP 계좌를 보유하고 있지 않습니다.</h3>
              <p className="text-gray-600 font-hana-regular text-center">하나은행 IRP 계좌를 개설하고, 하나인플랜에서 투자 혜택을 누려 보세요!</p>
              <button
                onClick={handleApplyClick}
                className="bg-hana-green text-white px-6 py-3 mt-6 rounded-md font-hana-bold hover:bg-green-700 transition-colors"
              >
                하나은행 IRP 계좌 개설하기
              </button>
            </div>
          )}

          <h1 className="text-2xl font-hana-medium text-gray-900 mb-6 text-left">퇴직연금이란?</h1>
          <p className="text-gray-600 font-hana-light text-left">퇴직연금은 퇴직 후 안정적인 노후 생활을 위해 자발적으로 가입하는 개인형 퇴직연금 상품입니다.</p>
          <p className="text-gray-600 font-hana-light text-left">사용자(회사)와 근로자는 퇴직금 제도와 DB형, DC형 중 하나 이상을 설정해야 하며, 양측의 협의 하에 복수의 제도를 동시에 설정할 수 있습니다.</p>
          <h2 className="text-xl font-hana-medium text-gray-700 mb-4 mt-12 text-center">퇴직연금 제도의 종류</h2>
          <img src="/images/irp_intro1.png" alt="퇴직연금 비교표" className="w-full h-auto mb-8" />

          <h2 className="text-xl font-hana-medium text-gray-700 mb-4 mt-12 text-left">IRP(개인형퇴직연금)란?</h2>
          <p className="text-gray-600 font-hana-light text-left">개인형 퇴직연금(Individual Retirement Pension)은 근로자가 퇴직으로 수령한 퇴직급여를 바로 사용하지 않고, <br />보관/운용 하다가 연금/일시금으로 수령할 수 있는 퇴직급여 통합계좌입니다.</p>
          <img src="/images/irp_intro2.png" alt="IRP 주요 혜택" className="w-full h-auto mb-8" />
          <h3 className="text-lg font-hana-medium text-gray-700 mb-4 mt-12 text-left">퇴직 시 IRP 의무가입</h3>
          <ul className="list-disc list-inside text-gray-600 font-hana-regular text-left mt-4 mb-12">
            <li>원칙: 퇴직 시 IRP로 퇴직급여를 수령해야 함</li>
            <li>만 55세 이후 퇴직, 퇴직급여액 300만원 이하 수령 시 일반계좌로 수령 가능</li>
          </ul>
          
          <div className="bg-white w-full rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h1 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">개인형 퇴직연금(IRP) 혜택</h1>
            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              <div className="mt-8">
                <div className="text-xl font-hana-medium text-gray-900 mb-4 rounded-lg bg-gray-200 p-4 text-center">과세이연</div>
                <h3 className="text-hana-green font-hana-medium my-4">
                  퇴직급여가 세전금액으로 이전되어, IRP 운용 중 과세이연 혜택
                </h3>
                <p className="text-gray-600 font-hana-regular my-2">과세이연: 소득 발생(매매차익, 이자 등) 시 세금 납부 연기</p>
                <p className="text-gray-600 font-hana-regular">퇴직급여는 소득 과세 전 금액으로 IRP계좌에 이전 - 수령 전까지 과세이연</p>
              </div>
              <div className="mt-8">
                <div className="text-xl font-hana-medium text-gray-900 mb-4 rounded-lg bg-gray-200 p-4 text-center">연금수령</div>
                <h3 className="text-hana-green font-hana-medium my-4">
                  IRP를 통해 55세 이후 연금으로 수령 가능
                </h3>
                <p className="text-gray-600 font-hana-regular my-2">연금수령 시: 과세이연된 퇴직급여 + 운용수익 모두 저율 연금소득세 적용</p>
                <p className="text-gray-600 font-hana-regular">과세 이연된 퇴직급여: 퇴직소득세 30% ~ 40% 감면혜택</p>
                <p className="text-gray-600 font-hana-regular">운용수익: 저율과세(5.5% ~ 3.3%) 혜택</p>
              </div>
              <div className="mt-8">
                <div className="text-xl font-hana-medium text-gray-900 mb-4 rounded-lg bg-gray-200 p-4 text-center">다양한 상품투자</div>
                <h3 className="text-hana-green font-hana-medium my-4">
                  DC와 동일하게 포트폴리오 투자 가능
                </h3>
                <p className="text-gray-600 font-hana-regular my-2">원리금 보장/실적배당형 상품 등 본인 투자성향에 맞는 포트폴리오 구성 가능</p>
                <p className="text-gray-600 font-hana-regular">운용 중 언제든지 상품교체 가능</p>
              </div>
              <div className="mt-8">
                <div className="text-xl font-hana-medium text-gray-900 mb-4 rounded-lg bg-gray-200 p-4 text-center">세액공제</div>
                <h3 className="text-hana-green font-hana-medium my-4">
                  추가 납입금 세액공제 가능
                </h3>
                <p className="text-gray-600 font-hana-regular my-2">연간 900만원 한도(연금저축, DC합산) 이내 13.2% ~ 16.5% 세액공제 혜택</p>
                <p className="text-gray-600 font-hana-regular">IRP설정 시 연금저축계좌/DC 합산하여 매년 1,800만원까지 추가 납입 가능</p>
              </div>
            </div>
          </div>
          {}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">소득 수준 별 IRP 세제 혜택</h3>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {}
              <div className="space-y-6">
                <h4 className="text-xl font-hana-medium text-gray-900 mb-4">세액공제율</h4>
                <div className="space-y-4">
                  <div className="flex justify-between items-center p-4 bg-hana-green/10 rounded-lg border border-hana-green/30">
                    <div>
                      <span className="font-hana-bold text-lg text-hana-green">총급여 5,500만원 이하</span>
                      <p className="text-hana-green text-sm mt-1 font-hana-regular">16.5% 세액공제</p>
                    </div>
                    <div className="text-right">
                      <span className="text-2xl font-hana-bold text-hana-green">1,155,000원</span>
                      <p className="text-hana-green text-sm font-hana-regular">700만원 납입 시</p>
                    </div>
                  </div>
                  <div className="flex justify-between items-center p-4 bg-blue-50 rounded-lg border border-blue-200">
                    <div>
                      <span className="font-hana-bold text-lg text-blue-800">총급여 5,500만원 초과</span>
                      <p className="text-blue-700 text-sm mt-1 font-hana-regular">13.2% 세액공제</p>
                    </div>
                    <div className="text-right">
                      <span className="text-2xl font-hana-bold text-blue-700">924,000원</span>
                      <p className="text-blue-700 text-sm font-hana-regular">700만원 납입 시</p>
                    </div>
                  </div>
                </div>
              </div>

              {}
              <div className="space-y-6">
                <h4 className="text-lg font-hana-medium text-gray-800 mb-4">혜택 비교</h4>
                <div className="w-full h-64">
                  <ResponsiveContainer width="100%" height="100%">
                    <BarChart data={taxSavingsData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
                      <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                      <XAxis 
                        dataKey="category" 
                        tick={{ fontSize: 12, fontFamily: 'Hana2-Regular' }}
                        angle={0}
                        textAnchor="middle"
                        height={60}
                      />
                      <YAxis 
                        tick={{ fontSize: 12, fontFamily: 'Hana2-Regular' }}
                        tickFormatter={(value) => `${value.toLocaleString()}원`}
                      />
                      <Tooltip 
                        formatter={(value: number) => [
                          `${value.toLocaleString()}원`, 
                          '절세액'
                        ]}
                        labelFormatter={(label: string) => label}
                        contentStyle={{
                          backgroundColor: 'white',
                          border: '1px solid #e5e7eb',
                          borderRadius: '8px',
                          fontFamily: 'Hana2-Regular'
                        }}
                      />
                      <Bar 
                        dataKey="amount" 
                        radius={[4, 4, 0, 0]}
                        animationDuration={1000}
                        fill="#10b981"
                      />
                    </BarChart>
                  </ResponsiveContainer>
                </div>

                <div className="p-4 bg-gray-50 rounded-lg">
                  <p className="text-sm text-gray-600">
                    <strong>절세율 계산:</strong> 5,500만원 이하 소득자는 231만원 더 많은 세액공제를 받을 수 있습니다.
                    <br />
                    <span className="text-green-600">→ 고소득자 대비 25% 높은 절세 효과</span>
                  </p>
                </div>
              </div>
            </div>
          </div>

          {}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">자주 묻는 질문</h3>
            <div className="space-y-4">
              <div className="border-l-4 border-hana-green pl-4 py-2">
                <h4 className="font-hana-medium text-lg mb-2">Q. IRP 가입 자격이 있나요?</h4>
                <p className="text-gray-600 font-hana-regular">만 18세 이상의 개인이라면 누구나 가입 가능합니다. 직장인, 자영업자, 프리랜서 등 상관없이 가입하실 수 있습니다.</p>
              </div>
              <div className="border-l-4 border-blue-500 pl-4 py-2">
                <h4 className="font-hana-medium text-lg mb-2">Q. 퇴직금 이관은 어떻게 하나요?</h4>
                <p className="text-gray-600 font-hana-regular">기존 회사의 퇴직금을 하나은행 IRP로 이관할 수 있습니다. 이관 절차는 간단하며, 세제 혜택도 그대로 유지됩니다.</p>
              </div>
              <div className="border-l-4 border-purple-500 pl-4 py-2">
                <h4 className="font-hana-medium text-lg mb-2">Q. 언제부터 연금을 받을 수 있나요?</h4>
                <p className="text-gray-600 font-hana-regular">만 55세부터 연금 또는 일시금으로 수령할 수 있습니다.</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      {}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            {}
            <div className="bg-hana-green text-white p-6 rounded-t-2xl">
              <div className="flex justify-between items-center">
                <h2 className="text-2xl font-hana-medium">IRP 계좌 개설</h2>
                <button
                  onClick={() => setShowModal(false)}
                  className="text-white hover:text-gray-200 text-2xl"
                >
                  ×
                </button>
              </div>
            </div>

            {}
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

            {}
            <div className="p-6">
              {}
              {error && (
                <div className="mb-4 p-3 bg-red-50 border border-red-200 rounded-lg">
                  <p className="text-red-800 text-sm">{error}</p>
                </div>
              )}

              {currentStep === 1 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">초기 납입금액 설정</h3>
                  <div className="space-y-4 text-left max-w-md mx-auto">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        초기 납입금액
                      </label>
                      <select
                        className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                        value={formData.initialDeposit}
                        onChange={(e) => setFormData(prev => ({
                          ...prev,
                          initialDeposit: Number(e.target.value)
                        }))}
                      >
                        <option value={500000}>50만원</option>
                        <option value={1000000}>100만원</option>
                        <option value={3000000}>300만원</option>
                        <option value={5000000}>500만원</option>
                        <option value={10000000}>1,000만원</option>
                      </select>
                      <p className="text-xs text-gray-500 mt-1">최소 50만원부터 시작 가능</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        월 자동납입 설정 (선택사항)
                      </label>
                      <div className="flex space-x-3">
                        <div className="flex-1">
                          <input
                            type="number"
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                            placeholder="예: 30"
                            value={formData.monthlyDeposit ? formData.monthlyDeposit / 10000 : ''}
                            onChange={(e) => setFormData(prev => ({
                              ...prev,
                              monthlyDeposit: e.target.value ? Number(e.target.value) * 10000 : 0
                            }))}
                          />
                        </div>
                        <div className="w-24">
                          <select
                            className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                            value={formData.depositDay}
                            onChange={(e) => setFormData(prev => ({
                              ...prev,
                              depositDay: Number(e.target.value)
                            }))}
                          >
                            {Array.from({ length: 28 }, (_, i) => i + 1).map(day => (
                              <option key={day} value={day}>{day}일</option>
                            ))}
                          </select>
                        </div>
                      </div>
                      <p className="text-xs text-gray-500 mt-1">월 10만원~70만원 권장 (연간 120만원~840만원)</p>
                      <p className="text-xs text-blue-600 mt-1">💡 월 납입금액은 만 원 단위로 입력해주세요 (예: 30 = 30만원)</p>
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-2">
                        자동이체 연결계좌
                      </label>
                      <div className="relative account-dropdown">
                        <button
                          type="button"
                          className="w-full p-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent text-left bg-white flex items-center justify-between"
                          onClick={() => setShowAccountDropdown(!showAccountDropdown)}
                        >
                          <div className="flex items-center">
                            {formData.linkedMainAccount ? (
                              (() => {
                                const selectedAccount = accounts.find(acc => acc.accountNumber === formData.linkedMainAccount);
                                const bankInfo = getBankInfo(formData.linkedMainAccount);
                                return (
                                  <div className="flex items-center">
                                    {bankInfo?.logo && (
                                      <img
                                        src={bankInfo.logo}
                                        alt={bankInfo.name}
                                        className="w-6 h-6 mr-2"
                                      />
                                    )}
                                    <span>{bankInfo?.name} {formData.linkedMainAccount} (잔액: {selectedAccount?.balance?.toLocaleString()}원)</span>
                                  </div>
                                );
                              })()
                            ) : (
                              <span className="text-gray-500">계좌를 선택해주세요</span>
                            )}
                          </div>
                          <svg className="w-5 h-5 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                          </svg>
                        </button>

                        {showAccountDropdown && (
                          <div className="absolute z-10 w-full mt-1 bg-white border border-gray-300 rounded-lg shadow-lg max-h-60 overflow-y-auto">
                            {accounts.length === 0 ? (
                              <div className="p-3 text-gray-500 text-center">사용 가능한 계좌가 없습니다</div>
                            ) : (
                              accounts.map(account => {
                                const bankInfo = getBankInfo(account.accountNumber);
                                return (
                                  <button
                                    key={account.accountId}
                                    type="button"
                                    className="w-full p-3 text-left hover:bg-gray-50 flex items-center border-b border-gray-100 last:border-b-0"
                                    onClick={() => {
                                      setFormData(prev => ({
                                        ...prev,
                                        linkedMainAccount: account.accountNumber
                                      }));
                                      setShowAccountDropdown(false);
                                    }}
                                  >
                                    {bankInfo?.logo && (
                                      <img
                                        src={bankInfo.logo}
                                        alt={bankInfo.name}
                                        className="w-6 h-6 mr-3"
                                      />
                                    )}
                                    <div>
                                      <div className="font-medium">{bankInfo?.name}</div>
                                      <div className="text-sm text-gray-600">
                                        {account.accountNumber} • 잔액: {account.balance?.toLocaleString()}원
                                      </div>
                                    </div>
                                  </button>
                                );
                              })
                            )}
                          </div>
                        )}
                      </div>
                      <p className="text-xs text-gray-500 mt-1">IRP 납입금이 자동으로 출금될 계좌를 선택해주세요</p>
                    </div>
                  </div>
                </div>
              )}

              {currentStep === 2 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">투자 성향 측정({currentQuestionIndex + 1}/7)</h3>
                  
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
              )}

              {currentStep === 3 && riskProfileResult && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">투자 성향 분석 결과</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    고객님의 투자 성향을 분석한 결과입니다
                  </p>
                  
                  <div className="space-y-6 max-w-lg mx-auto">
                    {/* 리스크 프로파일 결과 카드 */}
                    <div className="bg-gradient-to-r from-hana-green to-green-600 text-white p-6 rounded-xl">
                      <div className="text-center">
                        <div className="text-3xl font-hana-bold mb-2">{riskProfileResult.riskProfileTypeName}</div>
                        <div className="text-lg font-hana-medium opacity-90">리스크 점수: {riskProfileResult.riskProfileScore}점</div>
                      </div>
                    </div>

                    {/* 투자 성향 설명 */}
                    <div className="bg-gray-50 p-4 rounded-lg text-left">
                      <h4 className="font-hana-medium text-gray-800 mb-3">투자 성향 분석</h4>
                      <div className="space-y-2 text-sm text-gray-600">
                        {riskProfileResult.riskProfileType === 'CONSERVATIVE' && (
                          <>
                            <p>• 안정적인 투자를 선호하시는 보수적 성향입니다</p>
                            <p>• 원금 보전을 최우선으로 생각하십니다</p>
                            <p>• 예금, 적금 등 안전한 상품을 선호합니다</p>
                          </>
                        )}
                        {riskProfileResult.riskProfileType === 'MODERATE_CONSERVATIVE' && (
                          <>
                            <p>• 약간의 위험을 감수하되 안정성을 중시합니다</p>
                            <p>• 안정적인 수익을 추구하십니다</p>
                            <p>• 채권형 펀드나 안정형 상품을 선호합니다</p>
                          </>
                        )}
                        {riskProfileResult.riskProfileType === 'MODERATE' && (
                          <>
                            <p>• 위험과 수익의 균형을 추구합니다</p>
                            <p>• 적당한 위험을 감수하며 안정적인 수익을 원합니다</p>
                            <p>• 균형형 펀드나 혼합형 상품을 선호합니다</p>
                          </>
                        )}
                        {riskProfileResult.riskProfileType === 'AGGRESSIVE' && (
                          <>
                            <p>• 높은 수익을 위해 위험을 감수할 수 있습니다</p>
                            <p>• 장기적인 성장을 추구합니다</p>
                            <p>• 주식형 펀드나 성장형 상품을 선호합니다</p>
                          </>
                        )}
                      </div>
                    </div>

                    {/* 계좌 개설 진행 버튼 */}
                    <div className="bg-blue-50 p-4 rounded-lg">
                      <p className="text-blue-800 text-sm mb-3">
                        💡 분석된 투자 성향에 맞는 IRP 계좌를 개설하시겠습니까?
                      </p>
                      <button
                        onClick={() => setCurrentStep(4)}
                        className="bg-hana-green text-white px-6 py-3 rounded-lg font-hana-medium hover:bg-green-700 transition-colors"
                      >
                        계좌 개설 계속하기
                      </button>
                    </div>
                  </div>
                </div>
              )}

              {currentStep === 4 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">약관 동의</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    IRP 계좌 개설을 위한 필수 약관에 동의해주세요
                  </p>

                  {}
                  <div className="mb-6 text-left max-w-lg mx-auto">
                    <label className="flex items-center p-4 bg-blue-50 rounded-lg border-2 border-blue-200 hover:border-blue-300 cursor-pointer">
                      <input
                        type="checkbox"
                        checked={allAgreed}
                        onChange={handleAllAgreement}
                        className="w-5 h-5 mr-3 text-hana-green"
                      />
                      <div>
                        <div className="font-hana-medium text-blue-800">모두 동의합니다</div>
                        <div className="text-sm text-blue-600">아래 모든 약관에 일괄 동의합니다.</div>
                      </div>
                    </label>
                  </div>

                  <div className="space-y-4 text-left max-w-lg mx-auto max-h-80 overflow-y-auto bg-gray-50 p-4 rounded-lg">
                    <label className="flex items-start p-3 bg-white rounded-lg border border-gray-200">
                      <input
                        type="checkbox"
                        checked={agreedTerms.privacy}
                        onChange={() => handleTermAgreement('privacy')}
                        className="mt-1 mr-3"
                      />
                      <div className="text-sm flex-1">
                        <div className="font-medium">개인정보 수집 및 이용 동의 (필수)</div>
                        <div className="text-gray-500">IRP 계좌 개설 및 관리를 위한 개인정보 수집에 동의합니다.</div>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleShowTermsModal('privacy')}
                        className="text-hana-green hover:text-green-700 text-sm font-medium ml-2 whitespace-nowrap"
                      >
                        원문 보기
                      </button>
                    </label>

                    <label className="flex items-start p-3 bg-white rounded-lg border border-gray-200">
                      <input
                        type="checkbox"
                        checked={agreedTerms.irp}
                        onChange={() => handleTermAgreement('irp')}
                        className="mt-1 mr-3"
                      />
                      <div className="text-sm flex-1">
                        <div className="font-medium">IRP 상품 가입 약관 동의 (필수)</div>
                        <div className="text-gray-500">개인형퇴직연금 가입 조건 및 운용 원칙에 동의합니다.</div>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleShowTermsModal('irp')}
                        className="text-hana-green hover:text-green-700 text-sm font-medium ml-2 whitespace-nowrap"
                      >
                        원문 보기
                      </button>
                    </label>

                    <label className="flex items-start p-3 bg-white rounded-lg border border-gray-200">
                      <input
                        type="checkbox"
                        checked={agreedTerms.management}
                        onChange={() => handleTermAgreement('management')}
                        className="mt-1 mr-3"
                      />
                      <div className="text-sm flex-1">
                        <div className="font-medium">퇴직연금 운용 관리 약관 동의 (필수)</div>
                        <div className="text-gray-500">퇴직연금 자산 운용 및 관리에 관한 사항에 동의합니다.</div>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleShowTermsModal('management')}
                        className="text-hana-green hover:text-green-700 text-sm font-medium ml-2 whitespace-nowrap"
                      >
                        원문 보기
                      </button>
                    </label>

                    <label className="flex items-start p-3 bg-white rounded-lg border border-gray-200">
                      <input
                        type="checkbox"
                        checked={agreedTerms.electronic}
                        onChange={() => handleTermAgreement('electronic')}
                        className="mt-1 mr-3"
                      />
                      <div className="text-sm flex-1">
                        <div className="font-medium">전자금융거래 이용약관 동의 (필수)</div>
                        <div className="text-gray-500">온라인 뱅킹 및 전자금융거래 서비스 이용에 동의합니다.</div>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleShowTermsModal('electronic')}
                        className="text-hana-green hover:text-green-700 text-sm font-medium ml-2 whitespace-nowrap"
                      >
                        원문 보기
                      </button>
                    </label>
                  </div>

                  {!isAllTermsAgreed && (
                    <div className="mt-4 p-3 bg-yellow-50 border border-yellow-200 rounded-lg">
                      <p className="text-yellow-800 text-sm">
                        ⚠️ 모든 필수 약관에 동의해야 다음 단계로 진행할 수 있습니다.
                      </p>
                    </div>
                  )}
                </div>
              )}

              {currentStep === 5 && (
                <div className="text-center">
                  <h3 className="text-xl font-hana-medium mb-4">정보 확인</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    입력하신 정보를 확인하고 계좌 개설을 진행해주세요
                  </p>

                  <div className="text-left max-w-lg mx-auto space-y-6">
                    {}
                    <div className="bg-gray-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-gray-900 mb-3">신청자 정보</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-gray-600">고객명:</span>
                          <span className="font-medium">{user?.name || '정보 없음'}</span>
                        </div>
                      </div>
                    </div>

                    {}
                    <div className="bg-orange-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-orange-900 mb-3">출금계좌 정보</h4>
                      <div className="space-y-2 text-sm">
                        {formData.linkedMainAccount ? (
                          (() => {
                            const bankInfo = getBankInfo(formData.linkedMainAccount);
                            return (
                              <div className="flex justify-between items-center">
                                <span className="text-orange-700">자동이체 계좌:</span>
                                <div className="flex items-center">
                                  {bankInfo?.logo && (
                                    <img
                                      src={bankInfo.logo}
                                      alt={bankInfo.name}
                                      className="w-5 h-5 mr-2"
                                    />
                                  )}
                                  <div className="text-right">
                                    <div className="font-medium">{bankInfo?.name}</div>
                                    <div className="text-xs text-gray-600">{formData.linkedMainAccount}</div>
                                  </div>
                                </div>
                              </div>
                            );
                          })()
                        ) : (
                          <div className="flex justify-between">
                            <span className="text-orange-700">자동이체 계좌:</span>
                            <span className="font-medium text-red-600">선택되지 않음</span>
                          </div>
                        )}
                      </div>
                    </div>

                    {}
                    <div className="bg-blue-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-blue-900 mb-3">납입 정보</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-blue-700">초기 납입금액:</span>
                          <span className="font-medium">{(formData.initialDeposit / 10000).toLocaleString()}만원</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-blue-700">월 자동납입:</span>
                          <span className="font-medium">
                            {formData.monthlyDeposit ? `${(formData.monthlyDeposit / 10000).toLocaleString()}만원` : '설정 안함'}
                          </span>
                        </div>
                        {formData.monthlyDeposit && formData.monthlyDeposit > 0 && (
                          <div className="flex justify-between">
                            <span className="text-blue-700">자동납입일:</span>
                            <span className="font-medium">매월 {formData.depositDay}일</span>
                          </div>
                        )}
                      </div>
                    </div>

                    {}
                    <div className="bg-green-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-green-900 mb-3">투자 성향</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-green-700">분석 결과:</span>
                          <span className="font-medium">{riskProfileResult?.riskProfileTypeName || '미설정'}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-green-700">리스크 점수:</span>
                          <span className="font-medium">{riskProfileResult?.riskProfileScore || 'N/A'}점</span>
                        </div>
                      </div>
                    </div>

                    {}
                    <div className="bg-purple-50 p-4 rounded-lg">
                      <h4 className="font-hana-medium text-purple-900 mb-3">약관 동의</h4>
                      <div className="space-y-2 text-sm">
                        <div className="flex justify-between">
                          <span className="text-purple-700">동의 여부:</span>
                          <span className={`font-medium ${isAllTermsAgreed ? 'text-green-600' : 'text-red-600'}`}>
                            {isAllTermsAgreed ? '모든 약관 동의 완료' : '동의 필요'}
                          </span>
                        </div>
                      </div>
                    </div>

                    {!isAllTermsAgreed && (
                      <div className="bg-red-50 p-4 rounded-lg border border-red-200">
                        <p className="text-red-800 text-sm">
                          ⚠️ 모든 필수 약관에 동의해야 계좌 개설을 진행할 수 있습니다.
                        </p>
                      </div>
                    )}
                  </div>
                </div>
              )}
            </div>

            {}
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
                {currentStep < 5 ? (
                  <button
                    onClick={() => {
                      if (currentStep === 4 && !isAllTermsAgreed) {
                        setError('모든 필수 약관에 동의해야 다음 단계로 진행할 수 있습니다.');
                        return;
                      }
                      setCurrentStep(currentStep + 1);
                    }}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 font-medium"
                  >
                    다음
                  </button>
                ) : (
                  <button
                    onClick={handleIrpAccountCreation}
                    disabled={isLoading || !isAllTermsAgreed}
                    className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 font-medium disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {isLoading ? '계좌 개설 중...' : 'IRP 계좌 개설하기'}
                  </button>
                )}
              </div>
            </div>
          </div>
        </div>
      )}

      {}
      {showTermsModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-[60] p-4">
          <div className="bg-white rounded-2xl max-w-4xl w-full max-h-[90vh] overflow-hidden">
            {}
            <div className="bg-gradient-to-r from-gray-700 to-gray-800 text-white p-6">
              <div className="flex justify-between items-center">
                <h2 className="text-2xl font-hana-medium">
                  {selectedTermType === 'privacy' && '개인정보 수집 및 이용 동의'}
                  {selectedTermType === 'irp' && 'IRP 상품 가입 약관'}
                  {selectedTermType === 'management' && '퇴직연금 운용 관리 약관'}
                  {selectedTermType === 'electronic' && '전자금융거래 이용약관'}
                </h2>
                <button
                  onClick={() => setShowTermsModal(false)}
                  className="text-white hover:text-gray-200 text-2xl"
                >
                  ×
                </button>
              </div>
            </div>

            {}
            <div className="p-6 max-h-[70vh] overflow-y-auto">
              <div className="prose max-w-none">
                {selectedTermType === 'privacy' && (
                  <div className="space-y-4 text-sm text-gray-700">
                    <h3 className="text-lg font-bold text-gray-900">제1조 (목적)</h3>
                    <p>본 약관은 하나은행(이하 "은행"이라 함)이 제공하는 개인형퇴직연금(IRP) 서비스와 관련하여 수집하는 개인정보의 처리 및 보호에 관한 사항을 규정함을 목적으로 합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제2조 (수집하는 개인정보 항목)</h3>
                    <p>은행은 IRP 계좌 개설 및 관리에 필요한 최소한의 개인정보를 수집합니다:</p>
                    <ul className="list-disc pl-6 space-y-1">
                      <li>필수정보: 성명, 주민등록번호, 연락처, 주소</li>
                      <li>부가정보: 직장정보, 소득정보, 가족관계정보</li>
                    </ul>

                    <h3 className="text-lg font-bold text-gray-900">제3조 (개인정보의 이용목적)</h3>
                    <p>수집된 개인정보는 다음과 같은 목적으로 이용됩니다:</p>
                    <ul className="list-disc pl-6 space-y-1">
                      <li>IRP 계좌 개설 및 관리</li>
                      <li>퇴직연금 운용 및 수익률 관리</li>
                      <li>세제혜택 신청 및 관리</li>
                      <li>고객상담 및 민원처리</li>
                    </ul>

                    <h3 className="text-lg font-bold text-gray-900">제4조 (보유 및 이용기간)</h3>
                    <p>개인정보는 IRP 계좌 해지 후 5년간 보유하며, 법령에 따른 보유기간 경과 시 즉시 파기합니다.</p>
                  </div>
                )}

                {selectedTermType === 'irp' && (
                  <div className="space-y-4 text-sm text-gray-700">
                    <h3 className="text-lg font-bold text-gray-900">제1조 (가입자격)</h3>
                    <p>만 18세 이상의 개인으로서 소득이 있는 자는 누구나 가입할 수 있습니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제2조 (납입한도)</h3>
                    <p>연간 납입한도는 세액공제 기준에 따라 결정되며, 소득 수준별로 상이합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제3조 (운용방법)</h3>
                    <p>IRP 자산은 은행의 전문적인 운용팀이 관리하며, 고객의 위험성향에 맞는 포트폴리오를 구성합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제4조 (중도인출 제한)</h3>
                    <p>IRP 자산은 만 55세 이전에는 제한적으로만 인출할 수 있으며, 일반적인 중도인출은 불가능합니다.</p>
                  </div>
                )}

                {selectedTermType === 'management' && (
                  <div className="space-y-4 text-sm text-gray-700">
                    <h3 className="text-lg font-bold text-gray-900">제1조 (운용원칙)</h3>
                    <p>퇴직연금 자산은 안전성과 수익성의 균형을 고려하여 운용합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제2조 (위험관리)</h3>
                    <p>자산운용 시 분산투자 원칙을 적용하며, 과도한 위험자산 집중을 피합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제3조 (수수료)</h3>
                    <p>자산운용 수수료는 연간 0.5% 이내로 제한하며, 투명하게 공개합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제4조 (보고의무)</h3>
                    <p>운용현황 및 수익률을 정기적으로 고객에게 보고합니다.</p>
                  </div>
                )}

                {selectedTermType === 'electronic' && (
                  <div className="space-y-4 text-sm text-gray-700">
                    <h3 className="text-lg font-bold text-gray-900">제1조 (서비스 범위)</h3>
                    <p>전자금융거래 서비스는 계좌조회, 이체, 투자정보 확인 등을 포함합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제2조 (보안관리)</h3>
                    <p>고객정보 보호를 위해 SSL 암호화 및 2단계 인증을 적용합니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제3조 (이용제한)</h3>
                    <p>부정사용이 의심되는 경우 서비스 이용을 제한할 수 있습니다.</p>

                    <h3 className="text-lg font-bold text-gray-900">제4조 (책임범위)</h3>
                    <p>고객의 부주의로 인한 손실에 대해서는 은행이 책임을 지지 않습니다.</p>
                  </div>
                )}
              </div>
            </div>

            {}
            <div className="flex justify-end p-6 bg-gray-50 rounded-b-2xl">
              <button
                onClick={() => setShowTermsModal(false)}
                className="px-6 py-3 bg-gray-600 text-white rounded-lg hover:bg-gray-700 font-medium"
              >
                닫기
              </button>
            </div>
          </div>
        </div>
      )}

      {}
      {showSuccessModal && createdAccountInfo && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-lg w-full">
            {}
            <div className="bg-gradient-to-r from-green-500 to-emerald-600 text-white p-6 rounded-t-2xl text-center">
              <div className="w-16 h-16 bg-white bg-opacity-20 rounded-full flex items-center justify-center mx-auto mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <h2 className="text-2xl font-hana-bold">IRP 계좌 개설 완료!</h2>
              <p className="text-green-100 font-hana-light mt-2">하나은행 IRP 계좌가 성공적으로 개설되었습니다.</p>
            </div>

            {}
            <div className="p-6">
              <div className="bg-gray-50 rounded-lg p-4 mb-6">
                <div className="text-center mb-4">
                  <div className="flex items-center justify-center mb-2">
                    <img src="/bank/081.png" alt="하나은행" className="w-8 h-8 mr-2" />
                    <span className="text-lg font-hana-medium text-gray-800">하나은행</span>
                  </div>
                  <div className="text-2xl font-hana-bold text-hana-green mb-1">
                    {createdAccountInfo.accountNumber}
                  </div>
                  <div className="text-sm text-gray-600">IRP 계좌번호</div>
                </div>

                <div className="space-y-3 text-sm">
                  <div className="flex justify-between items-center py-2 border-b border-gray-200">
                    <span className="text-gray-600">초기 납입금액</span>
                    <span className="font-hana-medium text-gray-800">
                      {createdAccountInfo.initialDeposit.toLocaleString()}원
                    </span>
                  </div>
                  {createdAccountInfo.monthlyDeposit > 0 && (
                    <>
                      <div className="flex justify-between items-center py-2 border-b border-gray-200">
                        <span className="text-gray-600">월 자동납입</span>
                        <span className="font-hana-medium text-gray-800">
                          {(createdAccountInfo.monthlyDeposit / 10000).toLocaleString()}만원
                        </span>
                      </div>
                      <div className="flex justify-between items-center py-2 border-b border-gray-200">
                        <span className="text-gray-600">자동납입일</span>
                        <span className="font-hana-medium text-gray-800">
                          매월 {createdAccountInfo.depositDay}일
                        </span>
                      </div>
                    </>
                  )}
                  <div className="flex justify-between items-center py-2">
                    <span className="text-gray-600">투자 성향</span>
                    <span className="font-hana-medium text-gray-800">
                      {createdAccountInfo.investmentStyle === 'CONSERVATIVE' && '안정추구'}
                      {createdAccountInfo.investmentStyle === 'MODERATE' && '중립'}
                      {createdAccountInfo.investmentStyle === 'AGGRESSIVE' && '위험추구'}
                      {createdAccountInfo.investmentStyle === 'MODERATE_CONSERVATIVE' && '중립보수'}
                    </span>
                  </div>
                </div>
              </div>

              <div className="bg-blue-50 border border-blue-200 rounded-lg p-4 mb-6">
                <div className="flex items-start">
                  <div className="text-blue-500 mr-3 mt-0.5">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <div>
                    <h4 className="font-hana-medium text-blue-800 mb-1">안내사항</h4>
                    <ul className="text-sm text-blue-700 space-y-1">
                      <li>• 초기 입금이 연결된 계좌에서 자동으로 처리됩니다</li>
                      <li>• 월 자동납입은 설정한 날짜에 자동으로 이체됩니다</li>
                      <li>• 포트폴리오에서 계좌 현황을 확인할 수 있습니다</li>
                    </ul>
                  </div>
                </div>
              </div>
            </div>

            {}
            <div className="flex gap-3 p-6 bg-gray-50 rounded-b-2xl">
              <button
                onClick={() => navigate('/portfolio')}
                className="flex-1 bg-hana-green text-white px-6 py-3 rounded-lg font-hana-bold hover:bg-green-600 transition-colors"
              >
                포트폴리오 보기
              </button>
              <button
                onClick={() => {
                  setShowSuccessModal(false);
                  setIrpAccountInfo({
                    accountNumber: createdAccountInfo.accountNumber,
                    currentBalance: createdAccountInfo.initialDeposit,
                    monthlyDeposit: createdAccountInfo.monthlyDeposit,
                    depositDay: createdAccountInfo.depositDay,
                    investmentStyle: createdAccountInfo.investmentStyle
                  });
                  setCreatedAccountInfo(null);
                }}
                className="flex-1 border border-gray-300 text-gray-700 px-6 py-3 rounded-lg font-hana-medium hover:bg-gray-50 transition-colors"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default IrpProducts;