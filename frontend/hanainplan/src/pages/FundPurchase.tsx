import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fundProductApi, fundSubscriptionApi } from '../api/fundApi';
import type { FundClassDetail, FundPurchaseRequest } from '../types/fund.types';
import { useUserStore } from '../store/userStore';

const FundPurchase = () => {
  const { fundCode } = useParams<{ fundCode: string }>();
  const navigate = useNavigate();
  const { user, isLoggedIn } = useUserStore();
  
  const [fund, setFund] = useState<FundClassDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [purchasing, setPurchasing] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // 입력 값
  const [purchaseAmount, setPurchaseAmount] = useState<string>('');
  const [agreedToRisks, setAgreedToRisks] = useState(false);
  
  // 계산된 값
  const [estimatedFee, setEstimatedFee] = useState<number>(0);
  const [estimatedUnits, setEstimatedUnits] = useState<number>(0);

  // 로그인 체크
  useEffect(() => {
    if (!isLoggedIn || !user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
    }
  }, [isLoggedIn, user, navigate]);

  useEffect(() => {
    if (fundCode) {
      loadFundDetail(fundCode);
    }
  }, [fundCode]);

  useEffect(() => {
    calculateEstimates();
  }, [purchaseAmount, fund]);

  const loadFundDetail = async (code: string) => {
    try {
      setLoading(true);
      const data = await fundProductApi.getFundClass(code);
      setFund(data);
      setError(null);
    } catch (err) {
      setError('펀드 정보를 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const calculateEstimates = () => {
    if (!fund || !purchaseAmount || isNaN(Number(purchaseAmount))) {
      setEstimatedFee(0);
      setEstimatedUnits(0);
      return;
    }

    const amount = Number(purchaseAmount);
    
    // 수수료 계산
    let feeRate = 0;
    if (fund.fees?.frontLoadPct) {
      feeRate = fund.fees.frontLoadPct;
    } else if (fund.fees?.salesFeeBps) {
      feeRate = fund.fees.salesFeeBps / 10000;
    }
    const fee = amount * feeRate;
    setEstimatedFee(fee);

    // 매수 좌수 계산
    if (fund.latestNav) {
      const netAmount = amount - fee;
      const units = netAmount / fund.latestNav;
      setEstimatedUnits(units);
    }
  };

  const handlePurchase = async () => {
    if (!fund || !purchaseAmount) {
      alert('매수 금액을 입력해주세요.');
      return;
    }

    if (!user) {
      alert('로그인이 필요합니다.');
      navigate('/login');
      return;
    }

    const amount = Number(purchaseAmount);

    // 유효성 검사
    if (fund.rules?.minInitialAmount && amount < fund.rules.minInitialAmount) {
      alert(`최소 투자금액은 ${fund.rules.minInitialAmount.toLocaleString()}원입니다.`);
      return;
    }

    if (!agreedToRisks) {
      alert('투자 유의사항에 동의해주세요.');
      return;
    }

    try {
      setPurchasing(true);

      const request: FundPurchaseRequest = {
        userId: user.userId,  // 백엔드에서 userId로 CI 조회
        childFundCd: fund.childFundCd,
        purchaseAmount: amount,
      };

      const response = await fundSubscriptionApi.purchaseFund(request);

      if (response.success) {
        alert(`펀드 매수가 완료되었습니다!\n\n매수 좌수: ${response.purchaseUnits?.toFixed(6)}좌\n결제일: ${response.settlementDate}`);
        navigate('/fund/portfolio');
      } else {
        alert(`펀드 매수 실패: ${response.errorMessage}`);
      }
    } catch (err: any) {
      console.error('매수 오류:', err);
      alert(`펀드 매수 중 오류가 발생했습니다: ${err.message}`);
    } finally {
      setPurchasing(false);
    }
  };

  const formatNumber = (num: number | null | undefined) => {
    if (num === null || num === undefined) return '-';
    return num.toLocaleString('ko-KR');
  };

  const quickAmounts = [10000, 50000, 100000, 500000, 1000000];

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#00857D] mx-auto"></div>
          <p className="mt-4 text-gray-600">펀드 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !fund) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <p className="text-red-600">{error || '펀드를 찾을 수 없습니다.'}</p>
          <button
            onClick={() => navigate('/funds')}
            className="mt-4 px-4 py-2 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]"
          >
            목록으로
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-4xl mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="mb-6">
        <button
          onClick={() => navigate(`/fund/${fund.childFundCd}`)}
          className="text-[#00857D] hover:underline mb-4 flex items-center gap-2"
        >
          ← 펀드 상세로
        </button>
        <h1 className="text-3xl font-bold text-gray-900 mb-2">펀드 매수</h1>
        <p className="text-gray-600">IRP 계좌로 펀드를 매수합니다</p>
      </div>

      {/* 펀드 정보 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-lg font-semibold mb-4">선택한 펀드</h2>
        <div>
          <h3 className="text-xl font-bold text-gray-900 mb-2">
            {fund.fundMaster.fundName}
          </h3>
          <div className="flex items-center gap-2 mb-4">
            <span className="px-3 py-1 text-sm bg-[#00857D] text-white rounded-full">
              {fund.classCode}클래스
            </span>
            {fund.fundMaster.riskGrade && (
              <span className="text-sm text-gray-600">
                위험등급 {fund.fundMaster.riskGrade}
              </span>
            )}
          </div>
          {fund.latestNav && fund.latestNavDate && (
            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">현재 기준가</span>
                <span className="text-2xl font-bold text-gray-900">
                  {formatNumber(fund.latestNav)}원
                </span>
              </div>
              <div className="text-sm text-gray-500 text-right mt-1">
                {new Date(fund.latestNavDate).toLocaleDateString('ko-KR')}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* 매수 금액 입력 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-lg font-semibold mb-4">매수 금액</h2>
        
        {/* 금액 입력 */}
        <div className="mb-4">
          <input
            type="number"
            value={purchaseAmount}
            onChange={(e) => setPurchaseAmount(e.target.value)}
            placeholder="매수 금액을 입력하세요"
            className="w-full px-4 py-3 text-lg border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#00857D]"
          />
          <p className="text-sm text-gray-500 mt-2">
            최소 투자금액: {formatNumber(fund.rules?.minInitialAmount)}원
          </p>
        </div>

        {/* 빠른 금액 선택 */}
        <div className="grid grid-cols-5 gap-2">
          {quickAmounts.map((amount) => (
            <button
              key={amount}
              onClick={() => setPurchaseAmount(amount.toString())}
              className="py-2 text-sm border border-gray-300 rounded-lg hover:bg-[#00857D] hover:text-white hover:border-[#00857D] transition-colors"
            >
              {(amount / 10000).toFixed(0)}만
            </button>
          ))}
        </div>
      </div>

      {/* 예상 정보 */}
      {purchaseAmount && Number(purchaseAmount) > 0 && (
        <div className="bg-blue-50 rounded-lg p-6 mb-6 border border-blue-200">
          <h2 className="text-lg font-semibold text-blue-900 mb-4">예상 정보</h2>
          <div className="space-y-3">
            <div className="flex justify-between">
              <span className="text-blue-700">매수 금액</span>
              <span className="font-semibold text-blue-900">
                {Number(purchaseAmount).toLocaleString()}원
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-blue-700">수수료</span>
              <span className="font-semibold text-blue-900">
                -{estimatedFee.toLocaleString()}원
              </span>
            </div>
            <div className="flex justify-between pt-3 border-t border-blue-200">
              <span className="text-blue-700">실투자금</span>
              <span className="font-semibold text-blue-900">
                {(Number(purchaseAmount) - estimatedFee).toLocaleString()}원
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-blue-700">예상 매수 좌수</span>
              <span className="font-semibold text-blue-900">
                {estimatedUnits.toFixed(6)}좌
              </span>
            </div>
            <div className="flex justify-between">
              <span className="text-blue-700">결제일</span>
              <span className="font-semibold text-blue-900">
                T+{fund.rules?.buySettleDays || 0}
              </span>
            </div>
          </div>
        </div>
      )}

      {/* 동의사항 */}
      <div className="bg-yellow-50 rounded-lg p-6 mb-6 border border-yellow-200">
        <h3 className="text-lg font-semibold text-yellow-800 mb-3">투자 유의사항</h3>
        <ul className="space-y-2 text-sm text-yellow-700 mb-4">
          <li>• 펀드는 원금 손실이 발생할 수 있는 실적배당형 상품입니다.</li>
          <li>• 과거의 운용실적이 미래의 수익을 보장하지 않습니다.</li>
          <li>• 환매수수료가 부과될 수 있으니 투자 기간을 고려하시기 바랍니다.</li>
        </ul>
        <label className="flex items-center gap-2 cursor-pointer">
          <input
            type="checkbox"
            checked={agreedToRisks}
            onChange={(e) => setAgreedToRisks(e.target.checked)}
            className="w-5 h-5 text-[#00857D] focus:ring-[#00857D]"
          />
          <span className="text-sm text-gray-700">
            투자 유의사항을 확인하였으며, 이에 동의합니다.
          </span>
        </label>
      </div>

      {/* 매수 버튼 */}
      <div className="flex gap-4">
        <button
          onClick={() => navigate(`/fund/${fund.childFundCd}`)}
          className="flex-1 py-4 border border-gray-300 rounded-lg hover:bg-gray-50 font-semibold"
        >
          취소
        </button>
        <button
          onClick={handlePurchase}
          disabled={purchasing || !agreedToRisks || !purchaseAmount}
          className="flex-1 py-4 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66] font-semibold disabled:bg-gray-300 disabled:cursor-not-allowed"
        >
          {purchasing ? '처리중...' : '매수하기'}
        </button>
      </div>
    </div>
  );
};

export default FundPurchase;

