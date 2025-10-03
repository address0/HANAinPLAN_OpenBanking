import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fundSubscriptionApi } from '../api/fundApi';
import type { FundSubscription } from '../types/fund.types';
import { useUserStore } from '../store/userStore';

const FundPortfolio = () => {
  const navigate = useNavigate();
  const { user, isLoggedIn } = useUserStore();
  const [subscriptions, setSubscriptions] = useState<FundSubscription[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 로그인 체크 및 데이터 로드
  useEffect(() => {
    if (!isLoggedIn || !user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }
    loadSubscriptions();
  }, [isLoggedIn, user, navigate]);

  const loadSubscriptions = async () => {
    try {
      setLoading(true);
      
      if (!user?.userId) {
        setError('사용자 정보가 없습니다. 다시 로그인해주세요.');
        return;
      }
      
      const data = await fundSubscriptionApi.getActiveSubscriptions(user.userId);
      setSubscriptions(data);
      setError(null);
    } catch (err) {
      setError('펀드 포트폴리오를 불러오는데 실패했습니다.');
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

  const getReturnColor = (returnRate: number) => {
    if (returnRate > 0) return 'text-red-600';
    if (returnRate < 0) return 'text-blue-600';
    return 'text-gray-600';
  };

  const getReturnBgColor = (returnRate: number) => {
    if (returnRate > 0) return 'bg-red-50';
    if (returnRate < 0) return 'bg-blue-50';
    return 'bg-gray-50';
  };

  // 포트폴리오 통계 계산
  const totalInvestment = subscriptions.reduce((sum, sub) => sum + sub.purchaseAmount, 0);
  const totalValue = subscriptions.reduce((sum, sub) => sum + sub.currentValue, 0);
  const totalReturn = totalValue - totalInvestment;
  const totalReturnRate = totalInvestment > 0 ? (totalReturn / totalInvestment) * 100 : 0;

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#00857D] mx-auto"></div>
          <p className="mt-4 text-gray-600">포트폴리오를 불러오는 중...</p>
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
            onClick={loadSubscriptions}
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
        <h1 className="text-3xl font-bold text-gray-900 mb-2">펀드 포트폴리오</h1>
        <p className="text-gray-600">보유 중인 펀드 현황을 확인하세요</p>
      </div>

      {/* 포트폴리오 요약 */}
      <div className={`rounded-lg shadow-md p-6 mb-6 ${getReturnBgColor(totalReturnRate)}`}>
        <h2 className="text-lg font-semibold mb-4 text-gray-900">전체 요약</h2>
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div>
            <div className="text-sm text-gray-600 mb-1">투자 원금</div>
            <div className="text-2xl font-bold text-gray-900">
              {formatNumber(totalInvestment)}원
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">평가 금액</div>
            <div className="text-2xl font-bold text-gray-900">
              {formatNumber(totalValue)}원
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">평가 손익</div>
            <div className={`text-2xl font-bold ${getReturnColor(totalReturn)}`}>
              {totalReturn >= 0 ? '+' : ''}{formatNumber(totalReturn)}원
            </div>
          </div>
          <div>
            <div className="text-sm text-gray-600 mb-1">수익률</div>
            <div className={`text-2xl font-bold ${getReturnColor(totalReturnRate)}`}>
              {totalReturnRate >= 0 ? '+' : ''}{formatPercent(totalReturnRate)}
            </div>
          </div>
        </div>
      </div>

      {/* 보유 펀드 목록 */}
      <div className="flex justify-between items-center mb-4">
        <h2 className="text-xl font-semibold">보유 펀드 ({subscriptions.length}개)</h2>
        <button
          onClick={() => navigate('/funds')}
          className="px-4 py-2 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]"
        >
          + 펀드 추가 매수
        </button>
      </div>

      {subscriptions.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-md">
          <p className="text-gray-500 mb-4">보유 중인 펀드가 없습니다.</p>
          <button
            onClick={() => navigate('/funds')}
            className="px-6 py-3 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]"
          >
            펀드 상품 보기
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-1 gap-4">
          {subscriptions.map((subscription) => (
            <div
              key={subscription.subscriptionId}
              className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow"
            >
              <div className="flex justify-between items-start mb-4">
                <div className="flex-1">
                  <div className="flex items-center gap-2 mb-2">
                    <h3 className="text-lg font-semibold text-gray-900">
                      {subscription.fundName}
                    </h3>
                    <span className="px-2 py-1 text-xs bg-[#00857D] text-white rounded">
                      {subscription.classCode}클래스
                    </span>
                    <span
                      className={`px-2 py-1 text-xs rounded ${
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
                  <p className="text-sm text-gray-600">
                    {subscription.bankName} | 매수일: {new Date(subscription.purchaseDate).toLocaleDateString('ko-KR')}
                  </p>
                </div>

                <div className="text-right">
                  <div className="text-sm text-gray-500 mb-1">현재 기준가</div>
                  <div className="text-lg font-bold text-gray-900">
                    {formatNumber(subscription.currentNav)}원
                  </div>
                </div>
              </div>

              <div className="grid grid-cols-2 md:grid-cols-5 gap-4 py-4 border-t border-gray-200">
                <div>
                  <div className="text-xs text-gray-500 mb-1">투자 원금</div>
                  <div className="text-sm font-semibold">
                    {formatNumber(subscription.purchaseAmount)}원
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">보유 좌수</div>
                  <div className="text-sm font-semibold">
                    {subscription.currentUnits.toFixed(6)}좌
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">평가 금액</div>
                  <div className="text-sm font-semibold">
                    {formatNumber(subscription.currentValue)}원
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">평가 손익</div>
                  <div className={`text-sm font-semibold ${getReturnColor(subscription.totalReturn)}`}>
                    {subscription.totalReturn >= 0 ? '+' : ''}{formatNumber(subscription.totalReturn)}원
                  </div>
                </div>
                <div>
                  <div className="text-xs text-gray-500 mb-1">수익률</div>
                  <div className={`text-sm font-semibold ${getReturnColor(subscription.returnRate)}`}>
                    {subscription.returnRate >= 0 ? '+' : ''}{formatPercent(subscription.returnRate)}
                  </div>
                </div>
              </div>

              <div className="flex gap-2 mt-4 pt-4 border-t border-gray-200">
                <button
                  onClick={() => navigate(`/fund/${subscription.childFundCd}`)}
                  className="flex-1 py-2 border border-gray-300 rounded-lg hover:bg-gray-50 text-sm"
                >
                  상세 보기
                </button>
                <button
                  onClick={() => navigate(`/fund/${subscription.subscriptionId}/sell`)}
                  className="flex-1 py-2 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66] text-sm"
                >
                  매도하기
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default FundPortfolio;

