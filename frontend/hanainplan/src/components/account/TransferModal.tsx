import { useState, useEffect } from 'react';
import { useAccountStore } from '../../store/accountStore';
import { useBankStore, getBankPatternByPattern } from '../../store/bankStore';
import { withdrawal } from '../../api/bankingApi';

interface TransferModalProps {
  onClose: () => void;
  onTransferComplete?: () => void;
}

function TransferModal({ onClose, onTransferComplete }: TransferModalProps) {
  const [amount, setAmount] = useState('');
  const [description, setDescription] = useState('');
  const [recipientAccountNumber, setRecipientAccountNumber] = useState('');
  const [recipientName, setRecipientName] = useState('');
  const [isMobile, setIsMobile] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showInsufficientBalanceModal, setShowInsufficientBalanceModal] = useState(false);
  
  const { accounts, selectedAccountId } = useAccountStore();
  const { bankPatterns } = useBankStore();

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 1024);
    };
    
    checkMobile();
    window.addEventListener('resize', checkMobile);
    
    // Prevent body scroll when modal is open
    document.body.style.overflow = 'hidden';
    
    return () => {
      window.removeEventListener('resize', checkMobile);
      document.body.style.overflow = 'unset';
    };
  }, []);

  const formatCurrency = (value: string) => {
    const number = value.replace(/[^\d]/g, '');
    return new Intl.NumberFormat('ko-KR').format(Number(number));
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^\d]/g, '');
    setAmount(value);
  };

  const handleAccountNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^\d]/g, '');
    setRecipientAccountNumber(value);
  };

  // 계좌번호 패턴으로 은행 정보 찾기
  const getBankInfo = (accountNumber: string) => {
    if (accountNumber.length >= 3) {
      const pattern = accountNumber.substring(0, 3);
      return getBankPatternByPattern(pattern);
    }
    return null;
  };

  const bankInfo = getBankInfo(recipientAccountNumber);
  const selectedAccount = accounts.find(acc => acc.accountId === selectedAccountId);
  const currentBalance = selectedAccount?.balance || 0;
  const transferAmount = Number(amount);
  const isInsufficientBalance = transferAmount > currentBalance;

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    
    if (!selectedAccountId) {
      alert('계좌를 선택해주세요.');
      return;
    }

    if (isInsufficientBalance) {
      setShowInsufficientBalanceModal(true);
      return;
    }

    if (!bankInfo) {
      alert('올바른 계좌번호를 입력해주세요.');
      return;
    }

    if (!recipientName.trim()) {
      alert('받는 분 성함을 입력해주세요.');
      return;
    }

    try {
      setIsSubmitting(true);
      await withdrawal({
        accountId: selectedAccountId,
        amount: transferAmount,
        description: description || `${recipientName}님께 송금`,
        memo: `송금: ${recipientName} (${bankInfo.name})`
      });
      
      alert('송금이 완료되었습니다.');
      onTransferComplete?.();
      onClose();
    } catch (error) {
      console.error('송금 오류:', error);
      alert('송금 처리 중 오류가 발생했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const quickAmounts = [10000, 50000, 100000, 500000, 1000000];

  const handleOverlayClick = (e: React.MouseEvent) => {
    if (e.target === e.currentTarget) {
      onClose();
    }
  };

  if (isMobile) {
    return (
      <div 
        className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-end"
        onClick={handleOverlayClick}
      >
        <div 
          className="w-full bg-white rounded-t-3xl p-6 animate-slide-up max-h-[90vh] overflow-y-auto"
          onClick={(e) => e.stopPropagation()}
        >
          <div className="flex justify-between items-center mb-6">
            <h2 className="text-xl font-hana-bold text-gray-800">계좌 송금</h2>
            <button 
              onClick={onClose}
              className="text-gray-500 text-2xl hover:text-gray-700"
            >
              ×
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">

            {/* Recipient Account Info */}
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  받는 분 성함
                </label>
                <input
                  type="text"
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                  placeholder="받는 분 성함을 입력하세요"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
                  required
                />
              </div>
              
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  받는 계좌번호
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={recipientAccountNumber}
                    onChange={handleAccountNumberChange}
                    placeholder="계좌번호를 입력하세요"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
                    required
                  />
                  {bankInfo && (
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2 flex items-center space-x-2">
                      <img 
                        src={`/bank/${bankInfo.code}.png`} 
                        alt={bankInfo.name}
                        className="w-6 h-6 object-contain"
                      />
                      <span className="text-sm text-gray-600 font-hana-medium">{bankInfo.name}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Amount Input */}
            <div>
              <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                송금할 금액
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={amount ? formatCurrency(amount) : ''}
                  onChange={handleAmountChange}
                  placeholder="0"
                  className={`w-full px-4 py-3 border rounded-lg font-hana-medium text-lg text-right focus:outline-none focus:ring-2 ${
                    isInsufficientBalance ? 'border-red-300 focus:ring-red-500' : 'border-gray-300 focus:ring-hana-green'
                  }`}
                  required
                />
                <span className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-500">원</span>
              </div>
              {isInsufficientBalance && (
                <p className="text-red-500 text-sm mt-1">잔액 부족 (현재 잔액: {formatCurrency(currentBalance.toString())}원)</p>
              )}
            </div>

            {/* Quick Amount Buttons */}
            <div className="grid grid-cols-3 gap-2">
              {quickAmounts.map((quickAmount) => (
                <button
                  key={quickAmount}
                  type="button"
                  onClick={() => setAmount((amount) => (Number(amount) + quickAmount).toString())}
                  className="py-2 px-3 border border-gray-300 rounded-lg font-hana-medium text-sm hover:bg-gray-50 transition-colors"
                >
                  + {formatCurrency(quickAmount.toString())}원
                </button>
              ))}
              <button
                type="button"
                onClick={() => setAmount('')}
                className="py-2 px-3 border border-gray-300 rounded-lg font-hana-medium text-sm hover:bg-gray-50 transition-colors"
              >
                초기화
              </button>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                거래 내용 (선택)
              </label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="거래 내용을 입력하세요"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
              />
            </div>

            {/* Submit Button */}
            <button
              type="submit"
              disabled={isSubmitting || isInsufficientBalance || !recipientName.trim() || !recipientAccountNumber.trim() || !amount}
              className={`w-full py-4 rounded-lg font-hana-bold text-white transition-colors ${
                isSubmitting || isInsufficientBalance || !recipientName.trim() || !recipientAccountNumber.trim() || !amount
                  ? 'bg-gray-400 cursor-not-allowed'
                  : 'bg-red-600 hover:bg-red-700'
              }`}
            >
              {isSubmitting ? '송금 중...' : '송금하기'}
            </button>
          </form>
        </div>
      </div>
    );
  }

  // Desktop Modal
  return (
    <div 
      className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
      onClick={handleOverlayClick}
    >
      <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
        <div className="p-6">
          <div className="flex justify-between items-center mb-4">
            <h2 className="text-xl font-hana-bold text-gray-800">계좌 송금</h2>
            <button 
              onClick={onClose}
              className="text-gray-500 text-2xl hover:text-gray-700 transition-colors"
            >
              ×
            </button>
          </div>

          <form onSubmit={handleSubmit} className="space-y-6">

            {/* Recipient Account Info */}
            <div className="space-y-4">
              <div>
                <label className="block font-hana-medium text-gray-700 mb-2">
                  받는 분 성함
                </label>
                <input
                  type="text"
                  value={recipientName}
                  onChange={(e) => setRecipientName(e.target.value)}
                  placeholder="받는 분 성함을 입력하세요"
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
                  required
                />
              </div>
              
              <div>
                <label className="block font-hana-medium text-gray-700 mb-2">
                  받는 계좌번호
                </label>
                <div className="relative">
                  <input
                    type="text"
                    value={recipientAccountNumber}
                    onChange={handleAccountNumberChange}
                    placeholder="계좌번호를 입력하세요"
                    className="w-full px-4 py-3 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
                    required
                  />
                  {bankInfo && (
                    <div className="absolute right-3 top-1/2 transform -translate-y-1/2 flex items-center space-x-2">
                      <img 
                        src={`/bank/${bankInfo.code}.png`} 
                        alt={bankInfo.name}
                        className="w-6 h-6 object-contain"
                      />
                      <span className="text-sm text-gray-600 font-hana-medium">{bankInfo.name}</span>
                    </div>
                  )}
                </div>
              </div>
            </div>

            {/* Amount Input */}
            <div>
              <label className="block font-hana-medium text-gray-700 mb-2">
                송금할 금액
              </label>
              <div className="relative">
                <input
                  type="text"
                  value={amount ? formatCurrency(amount) : ''}
                  onChange={handleAmountChange}
                  placeholder="0"
                  className={`w-full px-4 py-3 border rounded-lg font-hana-medium text-lg text-right focus:outline-none focus:ring-2 pr-10 ${
                    isInsufficientBalance ? 'border-red-300 focus:ring-red-500' : 'border-gray-300 focus:ring-hana-green'
                  }`}
                  required
                />
                <span className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-500">원</span>
              </div>
              {isInsufficientBalance && (
                <p className="text-red-500 text-sm mt-1">잔액 부족 (현재 잔액: {formatCurrency(currentBalance.toString())}원)</p>
              )}
            </div>

            {/* Quick Amount Buttons */}
            <div className="grid grid-cols-3 gap-2">
              {quickAmounts.map((quickAmount) => (
                <button
                  key={quickAmount}
                  type="button"
                  onClick={() => setAmount((amount) => (Number(amount) + quickAmount).toString())}
                  className="py-2 px-3 border border-gray-300 rounded-lg font-hana-medium text-sm hover:bg-gray-50 transition-colors"
                >
                  + {formatCurrency(quickAmount.toString())}원
                </button>
              ))}
              <button
                type="button"
                onClick={() => setAmount('')}
                className="py-2 px-3 border border-gray-300 rounded-lg font-hana-medium text-sm hover:bg-gray-50 transition-colors"
              >
                초기화
              </button>
            </div>

            {/* Description */}
            <div>
              <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                거래 내용 (선택)
              </label>
              <input
                type="text"
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                placeholder="거래 내용을 입력하세요"
                className="w-full px-4 py-3 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
              />
            </div>

            {/* Action Buttons */}
            <div className="flex gap-3">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 py-3 border border-gray-300 rounded-lg font-hana-medium text-gray-700 hover:bg-gray-50 transition-colors"
              >
                취소
              </button>
              <button
                type="submit"
                disabled={isSubmitting || isInsufficientBalance || !recipientName.trim() || !recipientAccountNumber.trim() || !amount}
                className={`flex-1 py-3 rounded-lg font-hana-bold text-white transition-colors ${
                  isSubmitting || isInsufficientBalance || !recipientName.trim() || !recipientAccountNumber.trim() || !amount
                    ? 'bg-gray-400 cursor-not-allowed'
                    : 'bg-red-600 hover:bg-red-700'
                }`}
              >
                {isSubmitting ? '송금 중...' : '송금하기'}
              </button>
            </div>
          </form>
        </div>
      </div>

      {/* 잔액 부족 경고 모달 */}
      {showInsufficientBalanceModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-60 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full p-6">
            <div className="text-center">
              <div className="mx-auto flex items-center justify-center h-12 w-12 rounded-full bg-red-100 mb-4">
                <svg className="h-6 w-6 text-red-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
                </svg>
              </div>
              <h3 className="text-lg font-hana-bold text-gray-900 mb-2">잔액 부족</h3>
              <p className="text-sm text-gray-600 mb-6">
                송금하려는 금액이 현재 잔액보다 큽니다.<br/>
                현재 잔액: <span className="font-hana-bold">{formatCurrency(currentBalance.toString())}원</span><br/>
                송금 요청 금액: <span className="font-hana-bold text-red-600">{formatCurrency(amount)}원</span>
              </p>
              <button
                onClick={() => setShowInsufficientBalanceModal(false)}
                className="w-full bg-red-600 text-white py-3 px-4 rounded-lg font-hana-medium hover:bg-red-700 transition-colors"
              >
                확인
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}

export default TransferModal;