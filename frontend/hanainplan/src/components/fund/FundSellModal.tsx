import { useState, useEffect } from 'react';
import type { FundSubscription, FundRedemptionRequest } from '../../types/fund.types';
import { fundSubscriptionApi } from '../../api/fundApi';
import { useUserStore } from '../../store/userStore';
import { formatAmount, formatUnits } from '../../utils/fundUtils';

interface FundSellModalProps {
  isOpen: boolean;
  onClose: () => void;
  subscription: FundSubscription;
  onSuccess?: () => void;
}

const FundSellModal = ({ isOpen, onClose, subscription, onSuccess }: FundSellModalProps) => {
  const { user } = useUserStore();
  const [sellUnits, setSellUnits] = useState<string>('');
  const [sellPercentage, setSellPercentage] = useState<number>(0);
  const [agreedToRisks, setAgreedToRisks] = useState(false);
  const [selling, setSelling] = useState(false);
  const [estimatedAmount, setEstimatedAmount] = useState<number>(0);
  const [estimatedFee, setEstimatedFee] = useState<number>(0);
  const [estimatedProfit, setEstimatedProfit] = useState<number>(0);

  useEffect(() => {
    calculateEstimates();
  }, [sellUnits, subscription]);

  const calculateEstimates = () => {
    if (!subscription || !sellUnits || isNaN(Number(sellUnits))) {
      setEstimatedAmount(0);
      setEstimatedFee(0);
      setEstimatedProfit(0);
      setSellPercentage(0);
      return;
    }

    const units = Number(sellUnits);
    const percentage = (units / subscription.currentUnits) * 100;
    setSellPercentage(percentage);

    const amount = units * subscription.currentNav;
    setEstimatedAmount(amount);

    const fee = amount * 0.001;
    setEstimatedFee(fee);

    const avgPurchasePrice = subscription.purchaseAmount / subscription.currentUnits;
    const profit = (subscription.currentNav - avgPurchasePrice) * units - fee;
    setEstimatedProfit(profit);
  };

  const handleSell = async () => {
    if (!subscription || !sellUnits) {
      alert('매도 좌수를 입력해주세요.');
      return;
    }

    if (!user) {
      alert('로그인이 필요합니다.');
      return;
    }

    const units = Number(sellUnits);

    if (units <= 0 || units > subscription.currentUnits) {
      alert(`매도 가능한 좌수는 0 ~ ${subscription.currentUnits.toFixed(6)}좌입니다.`);
      return;
    }

    if (!agreedToRisks) {
      alert('환매 유의사항에 동의해주세요.');
      return;
    }

    try {
      setSelling(true);

      const request: FundRedemptionRequest = {
        userId: user.userId,
        subscriptionId: subscription.subscriptionId,
        sellUnits: units,
      };

      const response = await fundSubscriptionApi.redeemFund(request);

      if (response.success) {
        alert(`펀드 매도가 완료되었습니다!\n\n매도 좌수: ${response.sellUnits?.toFixed(6)}좌\n결제일: ${response.settlementDate}`);
        setSellUnits('');
        setAgreedToRisks(false);
        onClose();
        if (onSuccess) onSuccess();
      } else {
        alert(`펀드 매도 실패: ${response.errorMessage}`);
      }
    } catch (err: any) {
      alert(`펀드 매도 중 오류가 발생했습니다: ${err.message}`);
    } finally {
      setSelling(false);
    }
  };

  const quickPercentages = [25, 50, 75, 100];

  const handleQuickSelect = (percentage: number) => {
    const units = (subscription.currentUnits * percentage) / 100;
    setSellUnits(units.toFixed(6));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black bg-opacity-50 animate-fade-in">
      <div className="bg-white rounded-xl shadow-2xl max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto animate-slide-up">
        {}
        <div className="sticky top-0 bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center rounded-t-xl">
          <h2 className="text-2xl font-hana-bold text-gray-900">펀드 매도</h2>
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
              {subscription.fundName}
            </h3>
            <div className="flex items-center gap-2 mb-3">
              <span className="px-3 py-1 text-sm bg-hana-green text-white rounded-full font-hana-medium">
                {subscription.classCode}클래스
              </span>
            </div>
            <div className="bg-white p-3 rounded-lg space-y-2">
              <div className="flex justify-between">
                <span className="text-gray-600 font-hana-regular">현재 기준가</span>
                <span className="font-hana-bold text-gray-900">
                  {formatAmount(subscription.currentNav)}원
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600 font-hana-regular">보유 좌수</span>
                <span className="font-hana-bold text-gray-900">
                  {formatUnits(subscription.currentUnits)}좌
                </span>
              </div>
              <div className="flex justify-between">
                <span className="text-gray-600 font-hana-regular">평가 금액</span>
                <span className="font-hana-bold text-gray-900">
                  {formatAmount(subscription.currentValue)}원
                </span>
              </div>
            </div>
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              매도 좌수
            </label>
            <input
              type="number"
              value={sellUnits}
              onChange={(e) => setSellUnits(e.target.value)}
              placeholder="매도할 좌수를 입력하세요"
              step="0.000001"
              max={subscription.currentUnits}
              className="w-full px-4 py-3 text-lg border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
            />
            <p className="text-sm text-gray-500 mt-2 font-hana-regular">
              보유 좌수: {formatUnits(subscription.currentUnits)}좌
            </p>
          </div>

          {}
          <div className="grid grid-cols-4 gap-2">
            {quickPercentages.map((percentage) => (
              <button
                key={percentage}
                onClick={() => handleQuickSelect(percentage)}
                className="py-2 text-sm border border-gray-300 rounded-lg hover:bg-hana-green hover:text-white hover:border-hana-green transition-colors font-hana-medium"
              >
                {percentage}%
              </button>
            ))}
          </div>

          {}
          {sellUnits && Number(sellUnits) > 0 && (
            <div className="bg-blue-50 rounded-lg p-4 border border-blue-200">
              <h3 className="text-lg font-hana-bold text-blue-900 mb-3">예상 정보</h3>
              <div className="space-y-2">
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">매도 비율</span>
                  <span className="font-hana-medium text-blue-900">
                    {sellPercentage.toFixed(2)}%
                  </span>
                </div>
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">매도 좌수</span>
                  <span className="font-hana-medium text-blue-900">
                    {formatUnits(Number(sellUnits))}좌
                  </span>
                </div>
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">예상 매도 금액</span>
                  <span className="font-hana-medium text-blue-900">
                    {formatAmount(estimatedAmount)}원
                  </span>
                </div>
                <div className="flex justify-between font-hana-regular">
                  <span className="text-blue-700">환매 수수료</span>
                  <span className="font-hana-medium text-blue-900">
                    -{formatAmount(estimatedFee)}원
                  </span>
                </div>
                <div className="flex justify-between pt-2 border-t border-blue-200">
                  <span className="text-blue-700 font-hana-regular">실수령액</span>
                  <span className="font-hana-bold text-blue-900">
                    {formatAmount(estimatedAmount - estimatedFee)}원
                  </span>
                </div>
                <div className="flex justify-between">
                  <span className="text-blue-700 font-hana-regular">예상 손익</span>
                  <span className={`font-hana-bold ${estimatedProfit >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                    {estimatedProfit >= 0 ? '+' : ''}{formatAmount(estimatedProfit)}원
                  </span>
                </div>
              </div>
            </div>
          )}

          {}
          <div className="bg-yellow-50 rounded-lg p-4 border border-yellow-200">
            <h3 className="text-base font-hana-bold text-yellow-800 mb-2">환매 유의사항</h3>
            <ul className="space-y-1 text-sm text-yellow-700 mb-3 font-hana-regular">
              <li>• 환매 신청 후 취소가 불가능합니다.</li>
              <li>• 환매수수료가 부과될 수 있습니다.</li>
              <li>• 실제 환매 금액은 결제일 기준가로 산정됩니다.</li>
            </ul>
            <label className="flex items-center gap-2 cursor-pointer">
              <input
                type="checkbox"
                checked={agreedToRisks}
                onChange={(e) => setAgreedToRisks(e.target.checked)}
                className="w-5 h-5 text-hana-green focus:ring-hana-green rounded"
              />
              <span className="text-sm text-gray-700 font-hana-regular">
                환매 유의사항을 확인하였으며, 이에 동의합니다.
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
            onClick={handleSell}
            disabled={selling || !agreedToRisks || !sellUnits}
            className="flex-1 py-3 bg-red-600 text-white rounded-lg hover:bg-red-700 font-hana-bold disabled:bg-gray-300 disabled:cursor-not-allowed transition-colors"
          >
            {selling ? '처리중...' : '매도하기'}
          </button>
        </div>
      </div>
    </div>
  );
};

export default FundSellModal;