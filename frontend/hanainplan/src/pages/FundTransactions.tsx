import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fundTransactionApi } from '../api/fundApi';
import type { FundTransaction, FundTransactionStats } from '../types/fund.types';
import { useUserStore } from '../store/userStore';

const FundTransactions = () => {
  const navigate = useNavigate();
  const { user, isLoggedIn } = useUserStore();
  const [transactions, setTransactions] = useState<FundTransaction[]>([]);
  const [stats, setStats] = useState<FundTransactionStats | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [filter, setFilter] = useState<string>('ALL');

  // 로그인 체크 및 데이터 로드
  useEffect(() => {
    if (!isLoggedIn || !user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }
    loadData();
  }, [isLoggedIn, user, navigate]);

  const loadData = async () => {
    try {
      setLoading(true);
      
      if (!user?.userId) {
        setError('사용자 정보가 없습니다. 다시 로그인해주세요.');
        return;
      }
      
      const [transactionsData, statsData] = await Promise.all([
        fundTransactionApi.getUserTransactions(user.userId),
        fundTransactionApi.getTransactionStats(user.userId),
      ]);
      setTransactions(transactionsData);
      setStats(statsData);
      setError(null);
    } catch (err) {
      setError('거래 내역을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatNumber = (num: number | null | undefined) => {
    if (num === null || num === undefined) return '-';
    return num.toLocaleString('ko-KR');
  };

  const formatPercent = (rate: number | null | undefined) => {
    if (rate === null || rate === undefined) return '-';
    return `${rate.toFixed(2)}%`;
  };

  const getTypeColor = (type: string) => {
    switch (type) {
      case 'BUY':
        return 'bg-blue-100 text-blue-800';
      case 'SELL':
        return 'bg-red-100 text-red-800';
      case 'DIVIDEND':
        return 'bg-green-100 text-green-800';
      default:
        return 'bg-gray-100 text-gray-800';
    }
  };

  const filteredTransactions = transactions.filter((tx) => {
    if (filter === 'ALL') return true;
    return tx.transactionType === filter;
  });

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#00857D] mx-auto"></div>
          <p className="mt-4 text-gray-600">거래 내역을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <p className="text-red-600">{error}</p>
          <button
            onClick={loadData}
            className="mt-4 px-4 py-2 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">펀드 거래 내역</h1>
        <p className="text-gray-600">펀드 매수/매도 거래 내역을 확인하세요</p>
      </div>

      {/* 통계 */}
      {stats && (
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-6">
          {/* 매수 통계 */}
          <div className="bg-blue-50 rounded-lg shadow-md p-6 border border-blue-200">
            <h3 className="text-lg font-semibold text-blue-900 mb-4">매수</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-blue-700">총 건수</span>
                <span className="font-semibold text-blue-900">
                  {stats.totalPurchaseCount}건
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-blue-700">총 금액</span>
                <span className="font-semibold text-blue-900">
                  {formatNumber(stats.totalPurchaseAmount)}원
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-blue-700">수수료</span>
                <span className="font-semibold text-blue-900">
                  {formatNumber(stats.totalPurchaseFee)}원
                </span>
              </div>
            </div>
          </div>

          {/* 매도 통계 */}
          <div className="bg-red-50 rounded-lg shadow-md p-6 border border-red-200">
            <h3 className="text-lg font-semibold text-red-900 mb-4">매도</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-red-700">총 건수</span>
                <span className="font-semibold text-red-900">
                  {stats.totalRedemptionCount}건
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-red-700">총 금액</span>
                <span className="font-semibold text-red-900">
                  {formatNumber(stats.totalRedemptionAmount)}원
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-red-700">수수료</span>
                <span className="font-semibold text-red-900">
                  {formatNumber(stats.totalRedemptionFee)}원
                </span>
              </div>
            </div>
          </div>

          {/* 손익 통계 */}
          <div className="bg-green-50 rounded-lg shadow-md p-6 border border-green-200">
            <h3 className="text-lg font-semibold text-green-900 mb-4">손익</h3>
            <div className="space-y-2">
              <div className="flex justify-between">
                <span className="text-sm text-green-700">실현 손익</span>
                <span className={`font-semibold ${stats.totalRealizedProfit >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                  {stats.totalRealizedProfit >= 0 ? '+' : ''}{formatNumber(stats.totalRealizedProfit)}원
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-green-700">총 수수료</span>
                <span className="font-semibold text-green-900">
                  {formatNumber(stats.totalFees)}원
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-sm text-green-700">순현금흐름</span>
                <span className="font-semibold text-green-900">
                  {formatNumber(stats.netCashFlow)}원
                </span>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 필터 */}
      <div className="bg-white rounded-lg shadow-md p-4 mb-6">
        <div className="flex gap-2">
          <button
            onClick={() => setFilter('ALL')}
            className={`px-4 py-2 rounded-lg ${
              filter === 'ALL'
                ? 'bg-[#00857D] text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            전체 ({transactions.length})
          </button>
          <button
            onClick={() => setFilter('BUY')}
            className={`px-4 py-2 rounded-lg ${
              filter === 'BUY'
                ? 'bg-blue-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            매수 ({transactions.filter((t) => t.transactionType === 'BUY').length})
          </button>
          <button
            onClick={() => setFilter('SELL')}
            className={`px-4 py-2 rounded-lg ${
              filter === 'SELL'
                ? 'bg-red-600 text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            매도 ({transactions.filter((t) => t.transactionType === 'SELL').length})
          </button>
        </div>
      </div>

      {/* 거래 내역 목록 */}
      <div className="space-y-4">
        {filteredTransactions.length === 0 ? (
          <div className="text-center py-12 bg-white rounded-lg shadow-md">
            <p className="text-gray-500">거래 내역이 없습니다.</p>
          </div>
        ) : (
          filteredTransactions.map((tx) => (
            <div
              key={tx.transactionId}
              className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <span className={`px-3 py-1 text-sm rounded-full ${getTypeColor(tx.transactionType)}`}>
                      {tx.transactionTypeName}
                    </span>
                    <h3 className="text-lg font-semibold text-gray-900">
                      {tx.fundName}
                    </h3>
                    <span className="text-sm text-gray-500">
                      {tx.classCode}클래스
                    </span>
                  </div>
                  <p className="text-sm text-gray-600">
                    거래일: {new Date(tx.transactionDate).toLocaleDateString('ko-KR')} |
                    결제일: {new Date(tx.settlementDate).toLocaleDateString('ko-KR')}
                  </p>
                </div>

                <div className="text-right">
                  <div className="text-sm text-gray-500 mb-1">거래금액</div>
                  <div className="text-xl font-bold text-gray-900">
                    {formatNumber(tx.amount)}원
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-5 gap-4 py-4 border-t border-gray-200">
                <div>
                  <div className="text-xs text-gray-500 mb-1">거래 좌수</div>
                  <div className="text-sm font-semibold">
                    {tx.units.toFixed(6)}좌
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">기준가</div>
                  <div className="text-sm font-semibold">
                    {formatNumber(tx.nav)}원
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">수수료</div>
                  <div className="text-sm font-semibold text-red-600">
                    -{formatNumber(tx.fee)}원
                  </div>
                </div>
                {tx.transactionType === 'SELL' && (
                  <>
                    <div>
                      <div className="text-xs text-gray-500 mb-1">실현 손익</div>
                      <div className={`text-sm font-semibold ${tx.profit && tx.profit >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                        {tx.profit && tx.profit >= 0 ? '+' : ''}{formatNumber(tx.profit)}원
                      </div>
                    </div>
                    <div>
                      <div className="text-xs text-gray-500 mb-1">수익률</div>
                      <div className={`text-sm font-semibold ${tx.profitRate && tx.profitRate >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                        {tx.profitRate && tx.profitRate >= 0 ? '+' : ''}{formatPercent(tx.profitRate)}
                      </div>
                    </div>
                  </>
                )}
              </div>

              <div className="pt-4 border-t border-gray-200">
                <div className="text-xs text-gray-500">
                  IRP 잔액: {formatNumber(tx.irpBalanceBefore)}원 → {formatNumber(tx.irpBalanceAfter)}원
                </div>
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default FundTransactions;

