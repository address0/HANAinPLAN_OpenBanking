import { useState, useEffect } from 'react';
import Layout from '../../components/layout/Layout';
import { useUserStore } from '../../store/userStore';
import InterestRateComparison from '../../components/products/InterestRateComparison';
import {
  getAllDepositProducts,
  getOptimalDepositRecommendation,
  subscribeDeposit,
  getIrpAccount,
  getAllInterestRates
} from '../../api/productApi';
import { getAllAccounts } from '../../api/bankingApi';
import type {
  DepositProduct,
  OptimalDepositRecommendation,
  DepositSubscriptionRequest,
  DepositRecommendationRequest,
  InterestRateInfo
} from '../../api/productApi';

function DepositProducts() {
  const { user } = useUserStore();
  const [activeTab, setActiveTab] = useState<'products' | 'recommend'>('products');

  const [interestRates, setInterestRates] = useState<InterestRateInfo[]>([]);
  const [selectedBank, setSelectedBank] = useState<string>('ALL');

  const [depositProducts, setDepositProducts] = useState<DepositProduct[]>([]);
  const [selectedProduct, setSelectedProduct] = useState<DepositProduct | null>(null);
  const [loading, setLoading] = useState(false);

  const [recommendation, setRecommendation] = useState<OptimalDepositRecommendation | null>(null);
  const [recommendLoading, setRecommendLoading] = useState(false);
  const [hasIrpAccount, setHasIrpAccount] = useState(false);
  const [irpAccount, setIrpAccount] = useState<any>(null);

  const [retirementDate, setRetirementDate] = useState<string>('');
  const [depositAmount, setDepositAmount] = useState<number>(0);

  const [subscribing, setSubscribing] = useState(false);

  const [subscriptionResult, setSubscriptionResult] = useState<any>(null);
  const [showResultModal, setShowResultModal] = useState(false);

  const [showConfirmModal, setShowConfirmModal] = useState(false);

  useEffect(() => {
    fetchInterestRates();
    fetchDepositProducts();
    if (user?.userId) {
      checkIrpAccount();
    }
  }, [user]);

  const fetchInterestRates = async () => {
    try {
      const rates = await getAllInterestRates();
      setInterestRates(rates);
    } catch (error) {
    }
  };

  const fetchDepositProducts = async () => {
    try {
      setLoading(true);
      const response = await getAllDepositProducts();
      if (response.success) {
        setDepositProducts(response.products);
      }
    } catch (error) {
      alert('예금 상품 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  const checkIrpAccount = async () => {
    try {
      const irpData = await getIrpAccount(user!.userId);
      setIrpAccount(irpData);
      setHasIrpAccount(true);
    } catch (error) {
      setHasIrpAccount(false);
    }
  };

  const fetchRecommendation = async () => {
    if (!user?.userId) {
      alert('로그인이 필요합니다.');
      return;
    }

    if (!hasIrpAccount) {
      alert('IRP 계좌를 먼저 개설해주세요.');
      return;
    }

    if (!retirementDate || !depositAmount || depositAmount <= 0) {
      alert('은퇴 예정일과 예치 희망 금액을 입력해주세요.');
      return;
    }

    try {
      setRecommendLoading(true);

      const request: DepositRecommendationRequest = {
        userId: user.userId,
        retirementDate: retirementDate,
        depositAmount: depositAmount
      };

      const response = await getOptimalDepositRecommendation(request);
      if (response.success) {
        setRecommendation(response.recommendation);
      }
    } catch (error: any) {
      alert(error.response?.data?.message || '추천 조회에 실패했습니다.');
    } finally {
      setRecommendLoading(false);
    }
  };

  const openConfirmModal = () => {
    if (!recommendation || !user || !irpAccount) {
      alert('가입 정보가 부족합니다.');
      return;
    }
    setShowConfirmModal(true);
  };

  const handleSubscribe = async () => {
    setShowConfirmModal(false);

    try {
      setSubscribing(true);

      let linkedAccountNumber = irpAccount!.linkedMainAccount;

      if (!linkedAccountNumber) {
        const allAccountsResponse = await getAllAccounts(user!.userId);
        const firstAccount = allAccountsResponse.bankingAccounts[0];

        if (firstAccount) {
          linkedAccountNumber = firstAccount.accountNumber;
        } else {
          alert('연결할 주계좌가 없습니다. 먼저 계좌를 개설해주세요.');
          setSubscribing(false);
          return;
        }
      }

      const request: DepositSubscriptionRequest = {
        userId: user!.userId,
        bankCode: recommendation!.bankCode,
        irpAccountNumber: irpAccount!.accountNumber,
        linkedAccountNumber: linkedAccountNumber,
        depositCode: recommendation!.depositCode,
        productType: recommendation!.productType,
        contractPeriod: recommendation!.contractPeriod,
        subscriptionAmount: recommendation!.depositAmount
      };

      const response = await subscribeDeposit(request);

      if (response.success) {
        setSubscriptionResult(response);
        setShowResultModal(true);

        setRecommendation(null);
      }
    } catch (error: any) {
      alert(error.response?.data?.message || '예금 가입에 실패했습니다.');
    } finally {
      setSubscribing(false);
    }
  };

  const closeResultModal = () => {
    setShowResultModal(false);
    setSubscriptionResult(null);
    setActiveTab('products');
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        {}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
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
                상품 목록
              </button>
              <button
                onClick={() => setActiveTab('recommend')}
                className={`flex-1 py-4 px-6 text-center font-hana-medium transition-colors ${
                  activeTab === 'recommend'
                    ? 'text-hana-green border-b-2 border-hana-green bg-hana-green/5'
                    : 'text-gray-500 hover:text-gray-700'
                }`}
                disabled={!hasIrpAccount}
              >
                맞춤 상품 추천
                {!hasIrpAccount && <span className="ml-2 text-xs text-gray-400">(IRP 계좌 필요)</span>}
              </button>
            </div>
          </div>

          {}
          {activeTab === 'products' && (
            <>
              {}
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
                        <li>• IRP 계좌와 연동하여 세제 혜택</li>
                      </ul>
                    </div>
                    <div className="bg-blue-50 p-6 rounded-lg">
                      <h3 className="text-xl font-hana-bold text-blue-600 mb-3">고객 혜택</h3>
                      <ul className="space-y-2">
                        <li>• 안정적인 자산 증식</li>
                        <li>• 노후 자금 준비</li>
                        <li>• AI 기반 최적 상품 추천</li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>

              {}
              {hasIrpAccount && (
                <div className="bg-gradient-to-r from-hana-green to-green-600 rounded-xl shadow-lg p-6 mb-8">
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="text-2xl font-hana-bold text-white mb-2">
                        AI 맞춤 상품 추천
                      </h3>
                      <p className="text-white/90">
                        은퇴 시점에 맞춰 최적의 금리로 안전하게 자산을 운용하세요.
                      </p>
                    </div>
                    <button
                      onClick={() => setActiveTab('recommend')}
                      className="bg-white text-hana-green px-6 py-3 rounded-lg font-hana-medium hover:bg-gray-100 transition-colors"
                    >
                      추천받기
                    </button>
                  </div>
                </div>
              )}

              {}
              <div>
                <h2 className="text-2xl font-hana-bold text-gray-900 mb-6">은행별 금리 비교</h2>
                <InterestRateComparison rates={interestRates} />
              </div>
            </>
          )}

          {activeTab === 'recommend' && (
            <div className="bg-white rounded-xl shadow-lg p-8">
              <h2 className="text-3xl font-hana-bold text-gray-900 mb-6">AI 맞춤 상품 추천</h2>

              {!recommendation ? (
                <div className="max-w-2xl mx-auto">
                  <div className="mb-8 text-center">
                    <svg className="mx-auto h-24 w-24 text-hana-green mb-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <p className="text-gray-600 mb-2">
                      은퇴 시점과 예치 희망 금액을 입력하시면<br />
                      최고 금리의 정기예금 상품을 추천해드립니다.
                    </p>
                  </div>

                  {}
                  <div className="space-y-6 mb-8">
                    <div>
                      <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                        은퇴 예정일 *
                      </label>
                      <input
                        type="date"
                        value={retirementDate}
                        onChange={(e) => setRetirementDate(e.target.value)}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                        min={new Date().toISOString().split('T')[0]}
                      />
                    </div>

                    <div>
                      <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                        예치 희망 금액 (원) *
                      </label>
                      <input
                        type="number"
                        value={depositAmount || ''}
                        onChange={(e) => setDepositAmount(Number(e.target.value))}
                        className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent"
                        placeholder="예: 5000000 (최소 100만원)"
                        min="1000000"
                        step="100000"
                      />
                      <p className="mt-2 text-sm text-gray-500">
                        {depositAmount > 0 ? `${depositAmount.toLocaleString()}원` : '정기예금에 예치하실 금액을 입력하세요 (최소 100만원)'}
                      </p>
                      {irpAccount && (
                        <p className="mt-1 text-xs text-blue-600">
                          💡 현재 IRP 잔액: {irpAccount.currentBalance?.toLocaleString()}원
                        </p>
                      )}
                    </div>
                  </div>

                  <button
                    onClick={fetchRecommendation}
                    disabled={recommendLoading || !retirementDate || !depositAmount || depositAmount < 1000000}
                    className="w-full bg-hana-green text-white px-8 py-4 rounded-lg font-hana-medium hover:bg-green-600 transition-colors disabled:opacity-50 disabled:cursor-not-allowed"
                  >
                    {recommendLoading ? '최적 상품 찾는 중...' : '최고 금리 찾기 🔍'}
                  </button>
                </div>
              ) : (
                <div className="space-y-6">
                  {}
                  <div className="bg-gradient-to-r from-blue-50 to-hana-green/5 p-6 rounded-lg border border-hana-green/20">
                    <h3 className="text-lg font-hana-bold text-gray-900 mb-3 flex items-center">
                      <span className="text-2xl mr-2">💰</span>
                      최적 상품 추천 근거
                    </h3>
                    <div className="grid grid-cols-2 gap-4 mb-4">
                      <div className="bg-white rounded-lg p-4">
                        <div className="text-sm text-gray-600 mb-1">은퇴까지</div>
                        <div className="text-2xl font-hana-bold text-hana-green">{recommendation.yearsToRetirement}년</div>
                      </div>
                      <div className="bg-white rounded-lg p-4">
                        <div className="text-sm text-gray-600 mb-1">현재 IRP 잔액</div>
                        <div className="text-xl font-hana-bold text-gray-900">{recommendation.currentIrpBalance.toLocaleString()}원</div>
                      </div>
                    </div>
                    <p className="text-gray-800 bg-white rounded-lg p-3 border border-gray-200">{recommendation.recommendationReason}</p>
                  </div>

                  {}
                  <div className="border-2 border-hana-green rounded-lg p-6">
                    <div className="flex items-center justify-between mb-4">
                      <h3 className="text-2xl font-hana-bold text-gray-900">{recommendation.depositName}</h3>
                      <span className="bg-hana-green text-white px-4 py-2 rounded-full font-hana-medium">
                        {recommendation.bankName}
                      </span>
                    </div>

                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                      {}
                      <div>
                        <h4 className="font-hana-bold text-gray-900 mb-3">가입 조건</h4>
                        <div className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-gray-600">상품 유형</span>
                            <span className="font-hana-medium">{recommendation.productTypeName}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">가입 기간</span>
                            <span className="font-hana-medium">{recommendation.contractPeriod}{recommendation.contractPeriodUnit}</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">예치 금액</span>
                            <span className="font-hana-medium text-hana-green">{recommendation.depositAmount.toLocaleString()}원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">적용 금리</span>
                            <span className="font-hana-medium">{(recommendation.appliedRate * 100).toFixed(2)}%</span>
                          </div>
                        </div>
                      </div>

                      {}
                      <div>
                        <h4 className="font-hana-bold text-gray-900 mb-3">예상 수익</h4>
                        <div className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-gray-600">원금</span>
                            <span className="font-hana-medium">{recommendation.depositAmount.toLocaleString()}원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">예상 이자</span>
                            <span className="font-hana-medium text-blue-600">{recommendation.expectedInterest.toLocaleString()}원</span>
                          </div>
                          <div className="flex justify-between border-t pt-2">
                            <span className="text-gray-900 font-hana-bold">만기 수령액</span>
                            <span className="font-hana-bold text-hana-green text-lg">{recommendation.expectedMaturityAmount.toLocaleString()}원</span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-gray-600">만기일</span>
                            <span className="font-hana-medium">{recommendation.expectedMaturityDate}</span>
                          </div>
                        </div>
                      </div>
                    </div>
                  </div>

                  {}
                  <div className="flex gap-4">
                    <button
                      onClick={() => setRecommendation(null)}
                      className="flex-1 bg-gray-200 text-gray-700 py-3 px-6 rounded-lg font-hana-medium hover:bg-gray-300 transition-colors"
                    >
                      다시 추천받기
                    </button>
                    <button
                      onClick={openConfirmModal}
                      disabled={subscribing}
                      className="flex-1 bg-hana-green text-white py-3 px-6 rounded-lg font-hana-medium hover:bg-green-600 transition-colors disabled:opacity-50"
                    >
                      {subscribing ? '가입 처리 중...' : '이 상품으로 가입하기'}
                    </button>
                  </div>
                </div>
              )}
            </div>
          )}

          {}
          {selectedProduct && (
            <div
              className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
              onClick={() => setSelectedProduct(null)}
            >
              <div
                className="bg-white rounded-xl max-w-2xl w-full max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="p-6 border-b">
                  <div className="flex items-center justify-between">
                    <h2 className="text-2xl font-hana-bold text-gray-900">{selectedProduct.name}</h2>
                    <button
                      onClick={() => setSelectedProduct(null)}
                      className="text-gray-400 hover:text-gray-600 text-2xl"
                    >
                      ×
                    </button>
                  </div>
                </div>

                <div className="p-6">
                  <div className="space-y-6">
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-3">기본 정보</h3>
                      <div className="space-y-3">
                        <div className="flex justify-between">
                          <span className="text-gray-500">상품코드</span>
                          <span className="font-hana-medium">{selectedProduct.depositCode}</span>
                        </div>
                        <div className="flex justify-between">
                          <span className="text-gray-500">은행</span>
                          <span className="font-hana-medium">{selectedProduct.bankName}</span>
                        </div>
                        <div>
                          <span className="text-gray-500 block mb-2">금리 정보</span>
                          <span className="font-hana-medium">{selectedProduct.rateInfo}</span>
                        </div>
                      </div>
                    </div>

                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900 mb-3">상품 설명</h3>
                      <p className="text-gray-700">{selectedProduct.description}</p>
                    </div>
                  </div>

                  <div className="mt-8">
                    <button
                      onClick={() => {
                        setSelectedProduct(null);
                        if (hasIrpAccount) {
                          fetchRecommendation();
                        } else {
                          alert('IRP 계좌를 먼저 개설해주세요.');
                        }
                      }}
                      className="w-full bg-hana-green text-white px-8 py-3 rounded-lg font-hana-medium hover:bg-green-600 transition-colors"
                    >
                      AI 추천 받기
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>

      {}
      {showConfirmModal && recommendation && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full p-8 animate-fade-in">
            <div className="text-center mb-6">
              <div className="mx-auto w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mb-4">
                <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                </svg>
              </div>
              <h3 className="text-2xl font-hana-bold text-gray-900 mb-2">
                정기예금 가입 확인
              </h3>
              <p className="text-gray-600">
                아래 내용으로 정기예금에 가입하시겠습니까?
              </p>
            </div>

            <div className="space-y-4 mb-6">
              <div className="bg-gray-50 rounded-lg p-4">
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">상품명</span>
                    <span className="font-hana-medium text-gray-900">{recommendation.depositName}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">은행</span>
                    <span className="font-hana-medium text-gray-900">{recommendation.bankName}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">상품 유형</span>
                    <span className="font-hana-medium text-gray-900">{recommendation.productTypeName}</span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">가입 기간</span>
                    <span className="font-hana-medium text-gray-900">
                      {recommendation.contractPeriod}{recommendation.contractPeriodUnit}
                    </span>
                  </div>
                </div>
              </div>

              <div className="bg-hana-green/5 rounded-lg p-4 border border-hana-green/20">
                <div className="space-y-3">
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">예치 금액</span>
                    <span className="font-hana-bold text-hana-green text-lg">
                      {recommendation.depositAmount.toLocaleString()}원
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">적용 금리</span>
                    <span className="font-hana-medium text-gray-900">
                      연 {(recommendation.appliedRate * 100).toFixed(2)}%
                    </span>
                  </div>
                  <div className="flex justify-between items-center">
                    <span className="text-sm text-gray-500">예상 이자</span>
                    <span className="font-hana-medium text-blue-600">
                      +{recommendation.expectedInterest.toLocaleString()}원
                    </span>
                  </div>
                  <div className="flex justify-between items-center pt-3 border-t border-gray-200">
                    <span className="text-sm font-hana-bold text-gray-700">만기 수령액</span>
                    <span className="font-hana-bold text-hana-green text-xl">
                      {recommendation.expectedMaturityAmount.toLocaleString()}원
                    </span>
                  </div>
                </div>
              </div>

              <div className="text-center text-sm text-gray-600 bg-yellow-50 rounded-lg p-3 border border-yellow-200">
                <p>⚠️ 가입 후 IRP 계좌에서 즉시 차감됩니다.</p>
              </div>
            </div>

            <div className="flex gap-3">
              <button
                onClick={() => setShowConfirmModal(false)}
                className="flex-1 bg-gray-200 text-gray-700 px-6 py-3 rounded-lg font-hana-medium hover:bg-gray-300 transition-colors"
              >
                취소
              </button>
              <button
                onClick={handleSubscribe}
                disabled={subscribing}
                className="flex-1 bg-hana-green text-white px-6 py-3 rounded-lg font-hana-medium hover:bg-green-600 transition-colors disabled:opacity-50"
              >
                {subscribing ? '처리 중...' : '가입하기'}
              </button>
            </div>
          </div>
        </div>
      )}

      {}
      {showResultModal && subscriptionResult && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-xl shadow-2xl max-w-lg w-full p-8 animate-fade-in">
            <div className="text-center mb-6">
              <div className="mx-auto w-16 h-16 bg-hana-green rounded-full flex items-center justify-center mb-4">
                <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                </svg>
              </div>
              <h3 className="text-2xl font-hana-bold text-gray-900 mb-2">
                정기예금 가입 완료
              </h3>
              <p className="text-gray-600">
                정기예금 가입이 성공적으로 완료되었습니다.
              </p>
            </div>

            <div className="space-y-4 mb-6">
              <div className="bg-gray-50 rounded-lg p-4">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500 mb-1">IRP 계좌번호</p>
                    <p className="font-hana-medium text-gray-900">{subscriptionResult.accountNumber}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">은행</p>
                    <p className="font-hana-medium text-gray-900">{subscriptionResult.bankName}</p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">가입 금액</p>
                    <p className="font-hana-medium text-hana-green">
                      {subscriptionResult.expectedMaturityAmount ?
                        (subscriptionResult.expectedMaturityAmount - subscriptionResult.expectedInterest).toLocaleString() :
                        '0'}원
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">만기일</p>
                    <p className="font-hana-medium text-gray-900">{subscriptionResult.maturityDate}</p>
                  </div>
                </div>
              </div>

              <div className="bg-hana-green/5 rounded-lg p-4 border border-hana-green/20">
                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <p className="text-sm text-gray-500 mb-1">예상 이자</p>
                    <p className="font-hana-bold text-hana-green text-lg">
                      +{subscriptionResult.expectedInterest?.toLocaleString() || '0'}원
                    </p>
                  </div>
                  <div>
                    <p className="text-sm text-gray-500 mb-1">만기 수령액</p>
                    <p className="font-hana-bold text-hana-green text-lg">
                      {subscriptionResult.expectedMaturityAmount?.toLocaleString() || '0'}원
                    </p>
                  </div>
                </div>
              </div>

              <div className="text-center text-sm text-gray-600 bg-blue-50 rounded-lg p-3">
                <p>💡 예금 상품은 IRP 계좌에 가입되었습니다.</p>
                <p className="mt-1">만기 시 자동으로 IRP 계좌 잔액에 반영됩니다.</p>
              </div>
            </div>

            <button
              onClick={closeResultModal}
              className="w-full bg-hana-green text-white px-6 py-3 rounded-lg font-hana-medium hover:bg-green-600 transition-colors"
            >
              확인
            </button>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default DepositProducts;