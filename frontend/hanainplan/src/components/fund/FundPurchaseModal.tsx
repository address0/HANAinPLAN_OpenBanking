import { useState, useEffect } from 'react';
import type { FundClassDetail, FundPurchaseRequest } from '../../types/fund.types';
import { fundSubscriptionApi } from '../../api/fundApi';
import { useUserStore } from '../../store/userStore';
import { formatAmount, formatUnits } from '../../utils/fundUtils';

interface FundPurchaseModalProps {
  isOpen: boolean;
  onClose: () => void;
  fund: FundClassDetail;
  onSuccess?: () => void;
}

const FundPurchaseModal = ({ isOpen, onClose, fund, onSuccess }: FundPurchaseModalProps) => {
  const { user } = useUserStore();
  const [purchaseAmount, setPurchaseAmount] = useState<string>('');
  const [agreedToRisks, setAgreedToRisks] = useState(false);
  const [purchasing, setPurchasing] = useState(false);
  const [estimatedFee, setEstimatedFee] = useState<number>(0);
  const [estimatedUnits, setEstimatedUnits] = useState<number>(0);

  useEffect(() => {
    calculateEstimates();
  }, [purchaseAmount, fund]);

  const calculateEstimates = () => {
    if (!fund || !purchaseAmount || isNaN(Number(purchaseAmount))) {
      setEstimatedFee(0);
      setEstimatedUnits(0);
      return;
    }

    const amount = Number(purchaseAmount);

    let feeRate = 0;
    if (fund.fees?.frontLoadPct) {
      feeRate = fund.fees.frontLoadPct;
    } else if (fund.fees?.salesFeeBps) {
      feeRate = fund.fees.salesFeeBps / 10000;
    }
    const fee = amount * feeRate;
    setEstimatedFee(fee);

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
      return;
    }

    const amount = Number(purchaseAmount);

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
        userId: user.userId,
        childFundCd: fund.childFundCd,
        purchaseAmount: amount,
      };

      const response = await fundSubscriptionApi.purchaseFund(request);

      if (response.success) {
        alert(`펀드 매수가 완료되었습니다!\n\n매수 좌수: ${response.purchaseUnits?.toFixed(6)}좌\n결제일: ${response.settlementDate}`);
        setPurchaseAmount('');
        setAgreedToRisks(false);
        onClose();
        if (onSuccess) onSuccess();
      } else {
        alert(`펀드 매수 실패: ${response.errorMessage}`);
      }
    } catch (err: any) {
      alert(`펀드 매수 중 오류가 발생했습니다: ${err.message}`);
    } finally {
      setPurchasing(false);
    }
  };

  const quickAmounts = [10000, 50000, 100000, 500000, 1000000];

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 animate-fade-in">
      <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-up">
        {}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center rounded-t-xl">
          <h2 className="text-2xl font-hana-bold text-gray-900">펀드 매수</h2>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <div className="p-6 space-y-6">
          {}
          <div className="bg-gray-50 rounded-lg p-4">
            <h3 className="text-lg font-hana-bold text-gray-900 mb-2">
              {fund.fundMaster.fundName}
            </h3>
            <div className="flex items-center gap-2 mb-3">
              <span className="px-3 py-1 text-sm bg-hana-green text-white rounded-full font-hana-medium">
                {fund.classCode}클래스
              </span>
              {fund.fundMaster.riskGrade && (
                <span className="text-sm text-gray-600 font-hana-regular">
                  위험등급 {fund.fundMaster.riskGrade}
                </span>
              )}
            </div>
            {fund.latestNav && fund.latestNavDate && (
              <div className="bg-white p-3 rounded-lg">
                <div className="flex justify-between items-center">
                  <span className="text-gray-600 font-hana-regular">현재 기준가</span>
                  <span className="text-xl font-hana-bold text-gray-900">
                    {formatAmount(fund.latestNav)}원
                  </span>
                </div>
                <div className="text-sm text-gray-500 text-right mt-1 font-hana-regular">
                  {new Date(fund.latestNavDate).toLocaleDateString('ko-KR')}
                </div>
              </div>
            )}
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              매수 금액
            </label>
            <input
              type="number"
              value={purchaseAmount}
              onChange={(e) => setPurchaseAmount(e.target.value)}
              placeholder="매수 금액을 입력하세요"
              className="w-full px-4 py-3 text-lg border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
            />
            <p className="text-sm text-gray-500 mt-2 font-hana-regular">
              최소 투자금액: {formatAmount(fund.rules?.minInitialAmount)}원
            </p>
          </div>

          {}
          <div className="grid grid-cols-5 gap-2">
            {quickAmounts.map((amount) => (
              <button
                key={amount}
                onClick={() => setPurchaseAmount(amount.toString())}
                className="py-2 text-sm border border-gray-300 rounded-lg hover:bg-hana-green hover:text-white hover:border-hana-green transition-colors font-hana-medium"
              >
                {(amount / 10000).toFixed(0)}만
              </button>
            ))}
          </div>

          {}
          {purchaseAmount && Number(purchaseAmount) > 0 && (
            <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
              <h3 className="text-lg font-hana-bold text-blue-900 mb-3">예상 정보</h3>
              <div className="space-y-2">
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">매수 금액</span>
                  <span className="font-hana-medium text-blue-900">
                    {Number(purchaseAmount).toLocaleString()}원
                  </span>
                </div>
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">수수료</span>
                  <span className="font-hana-medium text-blue-900">
                    -{estimatedFee.toLocaleString()}원
                  </span>
                </div>
                <div className="flex justify-between pt-2 border-t border-blue-200">
                  <span className="text-blue-700 font-hana-regular">실투자금</span>
                  <span className="font-hana-bold text-blue-900">
                    {(Number(purchaseAmount) - estimatedFee).toLocaleString()}원
                  </span>
                </div>
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">예상 매수 좌수</span>
                  <span className="font-hana-medium text-blue-900">
                    {formatUnits(estimatedUnits)}좌
                  </span>
                </div>
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">결제일</span>
                  <span className="font-hana-medium text-blue-900">
                    T+{fund.rules?.buySettleDays || 0}
                  </span>
                </div>
              </div>
            </div>
          )}

          {}
          <div className="bg-yellow-50 rounded-lg p-4 border border-yellow-200">
            <h3 className="text-base font-hana-bold text-yellow-800 mb-2">투자 유의사항</h3>
            <ul className="space-y-1 text-sm text-yellow-700 mb-3 font-hana-regular">
              <li>• 펀드는 원금 손실이 발생할 수 있는 실적배당형 상품입니다.</li>
              <li>• 과거의 운용실적이 미래의 수익을 보장하지 않습니다.</li>
              <li>• 환매수수료가 부과될 수 있으니 투자 기간을 고려하시기 바랍니다.</li>
            </ul>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={agreedToRisks}
                onChange={(e) => setAgreedToRisks(e.target.checked)}
                className="w-5 h-5 text-hana-green focus:ring-hana-green rounded"
              />
              <span className="text-sm text-gray-700 font-hana-regular">
                투자 유의사항을 확인하였으며, 이에 동의합니다.
              </span>
            </label>
          </div>
        </div>

        {}
        <div className="sticky bottom-0 bg-white border-t border-gray-200 px-6 py-4 flex gap-3 rounded-b-xl">
          <button
            onClick={onClose}
            className="flex-1 py-3 border border-gray-300 rounded-lg hover:bg-gray-50 font-hana-medium transition-colors"
          >
            취소
          </button>
          <button
            onClick={handlePurchase}
            disabled={purchasing || !agreedToRisks || !purchaseAmount}
            className="flex-1 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-bold disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {purchasing ? '처리중...' : '매수하기'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default FundPurchaseModal;