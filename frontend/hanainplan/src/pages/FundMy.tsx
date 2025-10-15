import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fundSubscriptionApi, fundTransactionApi } from '../api/fundApi';
import type { FundSubscription, FundTransaction, FundTransactionStats } from '../types/fund.types';
import { useUserStore } from '../store/userStore';
import Layout from '../components/layout/Layout';
import FundSellModal from '../components/fund/FundSellModal';
import { formatAmount, formatUnits, formatPercent, formatReturnRate, getReturnColor, getReturnBgColor, getTransactionTypeColor } from '../utils/fundUtils';

type TabType = 'portfolio' | 'transactions';

const FundMy = () => {
  const navigate = useNavigate();
  const { user, isLoggedIn } = useUserStore();
  const [activeTab, setActiveTab] = useState<TabType>('portfolio');

  const [subscriptions, setSubscriptions] = useState<FundSubscription[]>([]);
  const [portfolioLoading, setPortfolioLoading] = useState(true);
  const [portfolioError, setPortfolioError] = useState<string | null>(null);

  const [transactions, setTransactions] = useState<FundTransaction[]>([]);
  const [stats, setStats] = useState<FundTransactionStats | null>(null);
  const [transactionsLoading, setTransactionsLoading] = useState(true);
  const [transactionsError, setTransactionsError] = useState<string | null>(null);
  const [filter, setFilter] = useState<string>('ALL');

  const [showSellModal, setShowSellModal] = useState(false);
  const [selectedSubscription, setSelectedSubscription] = useState<FundSubscription | null>(null);

  useEffect(() => {
    if (!isLoggedIn || !user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }
    loadPortfolioData();
    loadTransactionsData();
  }, [isLoggedIn, user, navigate]);

  const loadPortfolioData = async () => {
    try {
      setPortfolioLoading(true);
      if (!user?.userId) {
        setPortfolioError('사용자 정보가 없습니다. 다시 로그인해주세요.');
        return;
      }
      const data = await fundSubscriptionApi.getActiveSubscriptions(user.userId);
      setSubscriptions(data);
      setPortfolioError(null);
    } catch (err) {
      setPortfolioError('펀드 포트폴리오를 불러오는데 실패했습니다.');
    } finally {
      setPortfolioLoading(false);
    }
  };

  const loadTransactionsData = async () => {
    try {
      setTransactionsLoading(true);
      if (!user?.userId) {
        setTransactionsError('사용자 정보가 없습니다. 다시 로그인해주세요.');
        return;
      }
      const [transactionsData, statsData] = await Promise.all([
        fundTransactionApi.getUserTransactions(user.userId),
        fundTransactionApi.getTransactionStats(user.userId),
      ]);
      setTransactions(transactionsData);
      setStats(statsData);
      setTransactionsError(null);
    } catch (err) {
      setTransactionsError('거래 내역을 불러오는데 실패했습니다.');
    } finally {
      setTransactionsLoading(false);
    }
  };

  const handleSellClick = (subscription: FundSubscription) => {
    setSelectedSubscription(subscription);
    setShowSellModal(true);
  };

  const totalInvestment = subscriptions.reduce((sum, sub) => sum + sub.purchaseAmount, 0);
  const totalValue = subscriptions.reduce((sum, sub) => sum + sub.currentValue, 0);
  const totalReturn = totalValue - totalInvestment;
  const totalReturnRate = totalInvestment > 0 ? (totalReturn / totalInvestment) * 100 : 0;

  const filteredTransactions = transactions.filter((tx) => {
    if (filter === 'ALL') return true;
    return tx.transactionType === filter;
  });

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 py-8">
        {}
        <div className="mb-8">
          <h1 className="text-3xl font-hana-bold text-gray-900 mb-2">나의 펀드</h1>
          <p className="text-gray-600 font-hana-regular">보유 펀드와 거래 내역을 확인하세요</p>
        </div>

        {}
        <div className="bg-white rounded-lg shadow-md mb-6">
          <div className="flex border-b border-gray-200">
            <button
              onClick={() => setActiveTab('portfolio')}
              className={`flex-1 py-4 px-6 font-hana-bold text-lg transition-colors ${
                activeTab === 'portfolio'
                  ? 'text-hana-green border-b-2 border-hana-green'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              포트폴리오
            </button>
            <button
              onClick={() => setActiveTab('transactions')}
              className={`flex-1 py-4 px-6 font-hana-bold text-lg transition-colors ${
                activeTab === 'transactions'
                  ? 'text-hana-green border-b-2 border-hana-green'
                  : 'text-gray-500 hover:text-gray-700'
              }`}
            >
              거래 내역
            </button>
          </div>

          {}
          {activeTab === 'portfolio' && (
            <div className="p-6">
              {portfolioLoading ? (
                <div className="flex justify-center items-center py-12">
                  <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
                    <p className="mt-4 text-gray-600 font-hana-regular">포트폴리오를 불러오는 중...</p>
                  </div>
                </div>
              ) : portfolioError ? (
                <div className="text-center py-12">
                  <p className="text-red-600 font-hana-regular">{portfolioError}</p>
                  <button
                    onClick={loadPortfolioData}
                    className="mt-4 px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-medium transition-colors"
                  >
                    다시 시도
                  </button>
                </div>
              ) : (
                <>
                  {}
                  <div className={`rounded-lg p-6 mb-6 ${getReturnBgColor(totalReturnRate)}`}>
                    <h2 className="text-lg font-hana-bold mb-4 text-gray-900">전체 요약</h2>
                    <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                      <div>
                        <div className="text-sm text-gray-600 mb-1 font-hana-regular">투자 원금</div>
                        <div className="text-2xl font-hana-bold text-gray-900">
                          {formatAmount(totalInvestment)}원
                        </div>
                      </div>
                      <div>
                        <div className="text-sm text-gray-600 mb-1 font-hana-regular">평가 금액</div>
                        <div className="text-2xl font-hana-bold text-gray-900">
                          {formatAmount(totalValue)}원
                        </div>
                      </div>
                      <div>
                        <div className="text-sm text-gray-600 mb-1 font-hana-regular">평가 손익</div>
                        <div className={`text-2xl font-hana-bold ${getReturnColor(totalReturn)}`}>
                          {totalReturn >= 0 ? '+' : ''}{formatAmount(totalReturn)}원
                        </div>
                      </div>
                      <div>
                        <div className="text-sm text-gray-600 mb-1 font-hana-regular">수익률</div>
                        <div className={`text-2xl font-hana-bold ${getReturnColor(totalReturnRate)}`}>
                          {formatReturnRate(totalReturnRate)}
                        </div>
                      </div>
                    </div>
                  </div>

                  {}
                  <div className="flex justify-between items-center mb-4">
                    <h2 className="text-xl font-hana-bold">보유 펀드 ({subscriptions.length}개)</h2>
                    <button
                      onClick={() => navigate('/funds')}
                      className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-medium transition-colors"
                    >
                      + 펀드 추가 매수
                    </button>
                  </div>

                  {subscriptions.length === 0 ? (
                    <div className="text-center py-12">
                      <p className="text-gray-500 mb-4 font-hana-regular">보유 중인 펀드가 없습니다.</p>
                      <button
                        onClick={() => navigate('/funds')}
                        className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-bold transition-colors"
                      >
                        펀드 상품 보기
                      </button>
                    </div>
                  ) : (
                    <div className="space-y-4">
                      {subscriptions.map((subscription) => (
                        <div
                          key={subscription.subscriptionId}
                          className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors"
                        >
                          <div className="flex justify-between items-start mb-4">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-2">
                                <h3 className="text-lg font-hana-bold text-gray-900">
                                  {subscription.fundName}
                                </h3>
                                <span className="px-2 py-1 text-xs bg-hana-green text-white rounded font-hana-medium">
                                  {subscription.classCode}클래스
                                </span>
                                <span
                                  className={`px-2 py-1 text-xs rounded font-hana-medium ${
                                    subscription.status === 'ACTIVE'
                                      ? 'bg-green-100 text-green-800'
                                      : subscription.status === 'PARTIAL_SOLD'
                                      ? 'bg-yellow-100 text-yellow-800'
                                      : 'bg-gray-100 text-gray-800'
                                  }`}
                                >
                                  {subscription.status === 'ACTIVE'
                                    ? '보유중'
                                    : subscription.status === 'PARTIAL_SOLD'
                                    ? '일부매도'
                                    : '전량매도'}
                                </span>
                              </div>
                              <p className="text-sm text-gray-600 font-hana-regular">
                                {subscription.bankName} | 매수일: {new Date(subscription.purchaseDate).toLocaleDateString('ko-KR')}
                              </p>
                            </div>

                            <div className="text-right">
                              <div className="text-sm text-gray-500 mb-1 font-hana-regular">현재 기준가</div>
                              <div className="text-lg font-hana-bold text-gray-900">
                                {formatAmount(subscription.currentNav)}원
                              </div>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 py-4 border-t border-gray-200">
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">투자 원금</div>
                              <div className="text-sm font-hana-medium">
                                {formatAmount(subscription.purchaseAmount)}원
                              </div>
                            </div>
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">보유 좌수</div>
                              <div className="text-sm font-hana-medium">
                                {formatUnits(subscription.currentUnits)}좌
                              </div>
                            </div>
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">평가 금액</div>
                              <div className="text-sm font-hana-medium">
                                {formatAmount(subscription.currentValue)}원
                              </div>
                            </div>
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">평가 손익</div>
                              <div className={`text-sm font-hana-bold ${getReturnColor(subscription.totalReturn)}`}>
                                {subscription.totalReturn >= 0 ? '+' : ''}{formatAmount(subscription.totalReturn)}원
                              </div>
                            </div>
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">수익률</div>
                              <div className={`text-sm font-hana-bold ${getReturnColor(subscription.returnRate)}`}>
                                {formatReturnRate(subscription.returnRate)}
                              </div>
                            </div>
                          </div>

                          <div className="flex gap-2 mt-4 pt-4 border-t border-gray-200">
                            <button
                              onClick={() => navigate(`/fund/${subscription.childFundCd}`)}
                              className="flex-1 py-2 border border-gray-300 rounded-lg hover:bg-white text-sm font-hana-medium transition-colors"
                            >
                              상세 보기
                            </button>
                            <button
                              onClick={() => handleSellClick(subscription)}
                              className="flex-1 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 text-sm font-hana-bold transition-colors"
                            >
                              매도하기
                            </button>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </>
              )}
            </div>
          )}

          {}
          {activeTab === 'transactions' && (
            <div className="p-6">
              {transactionsLoading ? (
                <div className="flex justify-center items-center py-12">
                  <div className="text-center">
                    <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
                    <p className="mt-4 text-gray-600 font-hana-regular">거래 내역을 불러오는 중...</p>
                  </div>
                </div>
              ) : transactionsError ? (
                <div className="text-center py-12">
                  <p className="text-red-600 font-hana-regular">{transactionsError}</p>
                  <button
                    onClick={loadTransactionsData}
                    className="mt-4 px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-medium transition-colors"
                  >
                    다시 시도
                  </button>
                </div>
              ) : (
                <>
                  {}
                  {stats && (
                    <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
                      {}
                      <div className="bg-blue-50 rounded-lg p-6 border border-blue-200">
                        <h3 className="text-lg font-hana-bold text-blue-900 mb-4">매수</h3>
                        <div className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-sm text-blue-700 font-hana-regular">총 건수</span>
                            <span className="font-hana-medium text-blue-900">
                              {stats.totalPurchaseCount}건
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-blue-700 font-hana-regular">총 금액</span>
                            <span className="font-hana-medium text-blue-900">
                              {formatAmount(stats.totalPurchaseAmount)}원
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-blue-700 font-hana-regular">수수료</span>
                            <span className="font-hana-medium text-blue-900">
                              {formatAmount(stats.totalPurchaseFee)}원
                            </span>
                          </div>
                        </div>
                      </div>

                      {}
                      <div className="bg-red-50 rounded-lg p-6 border border-red-200">
                        <h3 className="text-lg font-hana-bold text-red-900 mb-4">매도</h3>
                        <div className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-sm text-red-700 font-hana-regular">총 건수</span>
                            <span className="font-hana-medium text-red-900">
                              {stats.totalRedemptionCount}건
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-red-700 font-hana-regular">총 금액</span>
                            <span className="font-hana-medium text-red-900">
                              {formatAmount(stats.totalRedemptionAmount)}원
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-red-700 font-hana-regular">수수료</span>
                            <span className="font-hana-medium text-red-900">
                              {formatAmount(stats.totalRedemptionFee)}원
                            </span>
                          </div>
                        </div>
                      </div>

                      {}
                      <div className="bg-green-50 rounded-lg p-6 border border-green-200">
                        <h3 className="text-lg font-hana-bold text-green-900 mb-4">손익</h3>
                        <div className="space-y-2">
                          <div className="flex justify-between">
                            <span className="text-sm text-green-700 font-hana-regular">실현 손익</span>
                            <span className={`font-hana-bold ${stats.totalRealizedProfit >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                              {stats.totalRealizedProfit >= 0 ? '+' : ''}{formatAmount(stats.totalRealizedProfit)}원
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-green-700 font-hana-regular">총 수수료</span>
                            <span className="font-hana-medium text-green-900">
                              {formatAmount(stats.totalFees)}원
                            </span>
                          </div>
                          <div className="flex justify-between">
                            <span className="text-sm text-green-700 font-hana-regular">순현금흐름</span>
                            <span className="font-hana-medium text-green-900">
                              {formatAmount(stats.netCashFlow)}원
                            </span>
                          </div>
                        </div>
                      </div>
                    </div>
                  )}

                  {}
                  <div className="flex gap-2 mb-6">
                    <button
                      onClick={() => setFilter('ALL')}
                      className={`px-4 py-2 rounded-lg font-hana-medium transition-colors ${
                        filter === 'ALL'
                          ? 'bg-hana-green text-white'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      전체 ({transactions.length})
                    </button>
                    <button
                      onClick={() => setFilter('BUY')}
                      className={`px-4 py-2 rounded-lg font-hana-medium transition-colors ${
                        filter === 'BUY'
                          ? 'bg-blue-600 text-white'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      매수 ({transactions.filter((t) => t.transactionType === 'BUY').length})
                    </button>
                    <button
                      onClick={() => setFilter('SELL')}
                      className={`px-4 py-2 rounded-lg font-hana-medium transition-colors ${
                        filter === 'SELL'
                          ? 'bg-red-600 text-white'
                          : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                      }`}
                    >
                      매도 ({transactions.filter((t) => t.transactionType === 'SELL').length})
                    </button>
                  </div>

                  {}
                  <div className="space-y-4">
                    {filteredTransactions.length === 0 ? (
                      <div className="text-center py-12">
                        <p className="text-gray-500 font-hana-regular">거래 내역이 없습니다.</p>
                      </div>
                    ) : (
                      filteredTransactions.map((tx) => (
                        <div
                          key={tx.transactionId}
                          className="bg-gray-50 rounded-lg p-6 hover:bg-gray-100 transition-colors"
                        >
                          <div className="flex justify-between items-start mb-4">
                            <div className="flex-1">
                              <div className="flex items-center gap-2 mb-2">
                                <span className={`px-3 py-1 text-sm rounded-full font-hana-medium ${getTransactionTypeColor(tx.transactionType)}`}>
                                  {tx.transactionTypeName}
                                </span>
                                <h3 className="text-lg font-hana-bold text-gray-900">
                                  {tx.fundName}
                                </h3>
                                <span className="text-sm text-gray-500 font-hana-regular">
                                  {tx.classCode}클래스
                                </span>
                              </div>
                              <p className="text-sm text-gray-600 font-hana-regular">
                                거래일: {new Date(tx.transactionDate).toLocaleDateString('ko-KR')} |
                                결제일: {new Date(tx.settlementDate).toLocaleDateString('ko-KR')}
                              </p>
                            </div>

                            <div className="text-right">
                              <div className="text-sm text-gray-500 mb-1 font-hana-regular">거래금액</div>
                              <div className="text-xl font-hana-bold text-gray-900">
                                {formatAmount(tx.amount)}원
                              </div>
                            </div>
                          </div>

                          <div className="grid grid-cols-2 md:grid-cols-5 gap-4 py-4 border-t border-gray-200">
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">거래 좌수</div>
                              <div className="text-sm font-hana-medium">
                                {formatUnits(tx.units)}좌
                              </div>
                            </div>
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">기준가</div>
                              <div className="text-sm font-hana-medium">
                                {formatAmount(tx.nav)}원
                              </div>
                            </div>
                            <div>
                              <div className="text-xs text-gray-500 mb-1 font-hana-regular">수수료</div>
                              <div className="text-sm font-hana-medium text-red-600">
                                -{formatAmount(tx.fee)}원
                              </div>
                            </div>
                            {tx.transactionType === 'SELL' && (
                              <>
                                <div>
                                  <div className="text-xs text-gray-500 mb-1 font-hana-regular">실현 손익</div>
                                  <div className={`text-sm font-hana-bold ${tx.profit && tx.profit >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                                    {tx.profit && tx.profit >= 0 ? '+' : ''}{formatAmount(tx.profit)}원
                                  </div>
                                </div>
                                <div>
                                  <div className="text-xs text-gray-500 mb-1 font-hana-regular">수익률</div>
                                  <div className={`text-sm font-hana-bold ${tx.profitRate && tx.profitRate >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                                    {formatReturnRate(tx.profitRate)}
                                  </div>
                                </div>
                              </>
                            )}
                          </div>

                          <div className="pt-4 border-t border-gray-200">
                            <div className="text-xs text-gray-500 font-hana-regular">
                              IRP 잔액: {formatAmount(tx.irpBalanceBefore)}원 → {formatAmount(tx.irpBalanceAfter)}원
                            </div>
                          </div>
                        </div>
                      ))
                    )}
                  </div>
                </>
              )}
            </div>
          )}
        </div>
      </div>

      {}
      {showSellModal && selectedSubscription && (
        <FundSellModal
          isOpen={showSellModal}
          onClose={() => {
            setShowSellModal(false);
            setSelectedSubscription(null);
          }}
          subscription={selectedSubscription}
          onSuccess={() => {
            loadPortfolioData();
            loadTransactionsData();
          }}
        />
      )}
    </Layout>
  );
};

export default FundMy;