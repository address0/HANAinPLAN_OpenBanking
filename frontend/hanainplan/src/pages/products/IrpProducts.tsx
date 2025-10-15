import { useNavigate } from 'react-router-dom';
import { useState, useEffect } from 'react';
import Layout from '../../components/layout/Layout';
import type { IrpAccountOpenRequest } from '../../api/productApi';
import { openIrpAccount, checkIrpAccountStatus } from '../../api/productApi';
import { getActiveBankingAccounts, type BankingAccount } from '../../api/bankingApi';
import { useUserStore } from '../../store/userStore';
import { getBankByAccountNumber } from '../../store/bankStore';

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
          {hasIrpAccount && (
            <div className="bg-gradient-to-r from-green-50 to-emerald-50 border border-green-200 rounded-2xl p-6 mb-8">
              <div className="flex items-center gap-4 mb-4">
                <div className="w-12 h-12 bg-green-100 rounded-full flex items-center justify-center">
                  <svg className="w-6 h-6 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <div className="flex-1">
                  <h2 className="text-xl font-hana-medium text-green-800 mb-1">하나은행 IRP 계좌를 보유하고 있습니다</h2>
                  <p className="text-green-700 font-hana-light">안전한 노후 준비를 위해 IRP 계좌를 개설하셨습니다.</p>
                </div>
                <button
                  onClick={() => navigate('/portfolio')}
                  className="bg-green-600 text-white px-6 py-3 rounded-xl font-hana-bold hover:bg-green-700 transition-colors"
                >
                  포트폴리오 보기
                </button>
              </div>

              {}
              {(irpAccountInfo || createdAccountInfo) && (
                <div className="grid grid-cols-1 md:grid-cols-3 gap-4 pt-4 border-t border-green-200">
                  <div className="bg-white p-4 rounded-lg border border-green-100">
                    <div className="text-sm text-green-700 mb-1">계좌번호</div>
                    <div className="font-hana-medium text-green-900">
                      {irpAccountInfo?.accountNumber || createdAccountInfo?.accountNumber || '조회 중...'}
                    </div>
                  </div>
                  <div className="bg-white p-4 rounded-lg border border-green-100">
                    <div className="text-sm text-green-700 mb-1">현재 잔액</div>
                    <div className="font-hana-medium text-green-900">
                      {irpAccountInfo?.currentBalance
                        ? `${irpAccountInfo.currentBalance.toLocaleString()}원`
                        : createdAccountInfo?.initialDeposit
                        ? `${createdAccountInfo.initialDeposit.toLocaleString()}원`
                        : '0원'
                      }
                    </div>
                  </div>
                  <div className="bg-white p-4 rounded-lg border border-green-100">
                    <div className="text-sm text-green-700 mb-1">월 자동납입</div>
                    <div className="font-hana-medium text-green-900">
                      {(irpAccountInfo?.monthlyDeposit || createdAccountInfo?.monthlyDeposit)
                        ? `${((irpAccountInfo?.monthlyDeposit || createdAccountInfo?.monthlyDeposit) / 10000).toLocaleString()}만원`
                        : '설정 안함'
                      }
                    </div>
                  </div>
                </div>
              )}
            </div>
          )}

          {}
          <div className="bg-gradient-to-br from-hana-green/5 to-blue-50 rounded-2xl p-8 mb-8">
            <div className="flex flex-col lg:flex-row items-center gap-8">
              <div className="flex-1">
                <h1 className="text-4xl font-hana-medium text-gray-900 mb-4">IRP로 안전한 노후 준비하세요</h1>
                <p className="text-xl font-hana-light text-gray-600 mb-6">
                  개인형퇴직연금(IRP)으로 퇴직 후에도 안정적인 생활을 보장받으세요
                </p>

                {}
                {error && (
                  <div className="mb-6 p-4 bg-red-50 border border-red-200 rounded-lg">
                    <div className="flex items-center gap-3">
                      <svg className="w-5 h-5 text-red-500 flex-shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
                      </svg>
                      <p className="text-red-800 font-hana-light">{error}</p>
                      <button
                        onClick={() => setError(null)}
                        className="ml-auto text-red-500 hover:text-red-700"
                      >
                        <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>
                  </div>
                )}

                <div className="flex flex-col sm:flex-row gap-4">
                  {!hasIrpAccount && (
                    <button
                      onClick={handleApplyClick}
                      className="bg-hana-green text-white px-8 py-4 rounded-xl font-hana-bold text-lg hover:bg-green-600 transition-colors shadow-lg"
                    >
                      하나은행 IRP 계좌 개설하기
                    </button>
                  )}
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

          {}
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

          {}
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

          {}
          <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 mb-8">
            <h3 className="text-2xl font-hana-medium text-gray-900 mb-6 text-center">IRP 세제 혜택 정리</h3>
            <p className="text-gray-600 font-hana-light text-center mb-8">
              소득 수준별로 다른 세액공제율과 납입한도를 적용받습니다
            </p>

            <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
              {}
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

              {}
              <div className="space-y-6">
                <h4 className="text-xl font-hana-medium text-gray-900 mb-4">혜택 비교 차트</h4>
                <div className="space-y-4">
                  {}
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

                  {}
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

            {}
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

          {}
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

          {}
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

      {}
      {showModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-2xl max-w-2xl w-full max-h-[90vh] overflow-y-auto">
            {}
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
                  <h3 className="text-xl font-hana-medium mb-4">납입금액 설정</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    초기 납입금액과 자동납입 설정을 선택해주세요
                  </p>
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
                  <h3 className="text-xl font-hana-medium mb-4">투자 성향 설정</h3>
                  <p className="text-gray-600 font-hana-light mb-6">
                    고객님의 위험성향에 맞는 포트폴리오를 설정해주세요
                  </p>
                  <div className="space-y-4 text-left max-w-md mx-auto">
                    <div className="space-y-3">
                      <label className="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input
                          type="radio"
                          name="risk"
                          value="CONSERVATIVE"
                          className="mr-3 text-hana-green"
                          checked={formData.investmentStyle === 'CONSERVATIVE'}
                          onChange={(e) => setFormData(prev => ({
                            ...prev,
                            investmentStyle: e.target.value as 'CONSERVATIVE' | 'MODERATE_CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE'
                          }))}
                        />
                        <div>
                          <div className="font-medium">안정추구</div>
                          <div className="text-sm text-gray-500">안전하고 안정적인 투자를 선호</div>
                        </div>
                      </label>
                      <label className="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input
                          type="radio"
                          name="risk"
                          value="MODERATE"
                          className="mr-3 text-hana-green"
                          checked={formData.investmentStyle === 'MODERATE'}
                          onChange={(e) => setFormData(prev => ({
                            ...prev,
                            investmentStyle: e.target.value as 'CONSERVATIVE' | 'MODERATE_CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE'
                          }))}
                        />
                        <div>
                          <div className="font-medium">중립</div>
                          <div className="text-sm text-gray-500">위험과 수익의 균형을 추구</div>
                        </div>
                      </label>
                      <label className="flex items-center p-4 border-2 border-gray-200 rounded-lg hover:border-hana-green cursor-pointer">
                        <input
                          type="radio"
                          name="risk"
                          value="AGGRESSIVE"
                          className="mr-3 text-hana-green"
                          checked={formData.investmentStyle === 'AGGRESSIVE'}
                          onChange={(e) => setFormData(prev => ({
                            ...prev,
                            investmentStyle: e.target.value as 'CONSERVATIVE' | 'MODERATE_CONSERVATIVE' | 'MODERATE' | 'AGGRESSIVE'
                          }))}
                        />
                        <div>
                          <div className="font-medium">위험추구</div>
                          <div className="text-sm text-gray-500">고위험 고수익을 추구</div>
                        </div>
                      </label>
                    </div>
                    <div className="bg-blue-50 p-3 rounded-lg">
                      <p className="text-blue-800 text-sm">
                        💡 투자 성향에 따라 원금손실 가능성과 수익률이 달라집니다.
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

              {currentStep === 4 && (
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
                          <span className="text-green-700">선택한 성향:</span>
                          <span className="font-medium">
                            {formData.investmentStyle === 'CONSERVATIVE' && '안정추구'}
                            {formData.investmentStyle === 'MODERATE' && '중립'}
                            {formData.investmentStyle === 'AGGRESSIVE' && '위험추구'}
                            {formData.investmentStyle === 'MODERATE_CONSERVATIVE' && '중립보수'}
                          </span>
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
                {currentStep < 4 ? (
                  <button
                    onClick={() => {
                      if (currentStep === 3 && !isAllTermsAgreed) {
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