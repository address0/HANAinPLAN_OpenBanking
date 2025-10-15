import { useState, useEffect } from 'react';
import { useAccountStore } from '../../store/accountStore';
import { getBankPatternByPattern } from '../../store/bankStore';
import {
  withdrawal,
  internalTransfer,
  getAllAccounts,
  verifyExternalAccount,
  transferToIrp,
  externalTransfer
} from '../../api/bankingApi';
import type { AccountVerificationResponse } from '../../api/bankingApi';
import { useUserStore } from '../../store/userStore';

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
  const [transferType, setTransferType] = useState<'external' | 'internal'>('external');
  const [selectedToAccountId, setSelectedToAccountId] = useState<number | null>(null);
  const [selectedToAccountNumber, setSelectedToAccountNumber] = useState<string | null>(null);
  const [selectedToAccountIsIrp, setSelectedToAccountIsIrp] = useState(false);
  const [myAccounts, setMyAccounts] = useState<any[]>([]);
  const [verifiedAccount, setVerifiedAccount] = useState<AccountVerificationResponse | null>(null);
  const [isVerifying, setIsVerifying] = useState(false);

  const { accounts, selectedAccountId } = useAccountStore();
  const { user } = useUserStore();

  useEffect(() => {
    const checkMobile = () => {
      setIsMobile(window.innerWidth < 1024);
    };

    checkMobile();
    window.addEventListener('resize', checkMobile);

    document.body.style.overflow = 'hidden';

    if (user && transferType === 'internal') {
      loadMyAccounts();
    }

    return () => {
      window.removeEventListener('resize', checkMobile);
      document.body.style.overflow = 'unset';
    };
  }, [transferType, user]);

  const loadMyAccounts = async () => {
    if (!user) return;
    try {
      const response = await getAllAccounts(user.userId);
      const allAccounts: any[] = [...response.bankingAccounts];

      if (response.irpAccount) {
        allAccounts.push({
          accountId: null,
          accountNumber: response.irpAccount.accountNumber,
          accountName: response.irpAccount.accountName || 'IRP 계좌',
          accountType: 6,
          balance: response.irpAccount.currentBalance,
          accountTypeDescription: 'IRP',
          isIrp: true
        } as any);
      }

      setMyAccounts(allAccounts.filter(acc =>
        acc.isIrp ? true : acc.accountId !== selectedAccountId
      ));
    } catch (error) {
    }
  };

  const formatCurrency = (value: string) => {
    const number = value.replace(/[^\d]/g, '');
    return new Intl.NumberFormat('ko-KR').format(Number(number));
  };

  const handleAmountChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^\d]/g, '');
    setAmount(value);
  };

  const handleAccountNumberChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const value = e.target.value.replace(/[^\d]/g, '');
    setRecipientAccountNumber(value);
    setVerifiedAccount(null);

    if (value.length >= 10) {
      try {
        setIsVerifying(true);
        const result = await verifyExternalAccount(value);
        setVerifiedAccount(result);

        if (result.exists) {
        }
      } catch (error) {
        setVerifiedAccount(null);
      } finally {
        setIsVerifying(false);
      }
    }
  };

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

    if (transferType === 'internal') {
      if (!selectedToAccountId && !selectedToAccountIsIrp) {
        alert('입금할 계좌를 선택해주세요.');
        return;
      }

      try {
        setIsSubmitting(true);

        if (selectedToAccountIsIrp && selectedToAccountNumber) {
          await transferToIrp({
            fromAccountId: selectedAccountId,
            toIrpAccountNumber: selectedToAccountNumber,
            amount: transferAmount,
            description: description || 'IRP 계좌로 송금'
          });

          if (onTransferComplete) {
            await onTransferComplete();
          }

          alert('IRP 계좌 송금이 완료되었습니다!');
        } else {
          await internalTransfer({
            fromAccountId: selectedAccountId,
            toAccountId: selectedToAccountId!,
            amount: transferAmount,
            description: description || '계좌 간 송금'
          });

          if (onTransferComplete) {
            await onTransferComplete();
          }

          alert('송금이 완료되었습니다!');
        }

        onClose();
      } catch (error: any) {
        alert(error.response?.data?.message || '송금 처리 중 오류가 발생했습니다.');
      } finally {
        setIsSubmitting(false);
      }
      return;
    }

    if (!verifiedAccount?.exists) {
      alert('계좌번호를 정확히 입력해주세요.');
      return;
    }

    if (!recipientName.trim()) {
      alert('받는 분 성함을 입력해주세요.');
      return;
    }

    try {
      setIsSubmitting(true);

      if (verifiedAccount.accountType === 'IRP') {
        await transferToIrp({
          fromAccountId: selectedAccountId,
          toIrpAccountNumber: recipientAccountNumber,
          amount: transferAmount,
          description: description || `${recipientName}님께 IRP 송금`
        });

        if (onTransferComplete) {
          await onTransferComplete();
        }

        alert('IRP 계좌 송금이 완료되었습니다!');
      } else {
        await externalTransfer({
          fromAccountId: selectedAccountId,
          toAccountNumber: recipientAccountNumber,
          amount: transferAmount,
          description: description || `${recipientName}님께 송금`
        });

        if (onTransferComplete) {
          await onTransferComplete();
        }

        alert('송금이 완료되었습니다!');
      }

      onClose();
    } catch (error: any) {
      alert(error.response?.data?.message || '송금 처리 중 오류가 발생했습니다.');
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

            {}
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setTransferType('external')}
                className={`flex-1 py-3 rounded-lg font-hana-medium transition-colors ${
                  transferType === 'external'
                    ? 'bg-hana-green text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                타인 계좌로 송금
              </button>
              <button
                type="button"
                onClick={() => setTransferType('internal')}
                className={`flex-1 py-3 rounded-lg font-hana-medium transition-colors ${
                  transferType === 'internal'
                    ? 'bg-hana-green text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                내 계좌로 송금
              </button>
            </div>

            {}
            {transferType === 'internal' && (
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  받는 계좌 선택
                </label>
                <div className="space-y-2">
                  {myAccounts.map((account) => {
                    const isSelected = account.isIrp
                      ? selectedToAccountNumber === account.accountNumber
                      : selectedToAccountId === account.accountId;

                    return (
                      <button
                        key={account.isIrp ? account.accountNumber : account.accountId}
                        type="button"
                        onClick={() => {
                          if (account.isIrp) {
                            setSelectedToAccountId(null);
                            setSelectedToAccountNumber(account.accountNumber);
                            setSelectedToAccountIsIrp(true);
                          } else {
                            setSelectedToAccountId(account.accountId);
                            setSelectedToAccountNumber(null);
                            setSelectedToAccountIsIrp(false);
                          }
                        }}
                        className={`w-full p-4 border-2 rounded-lg text-left transition-colors ${
                          isSelected
                            ? 'border-hana-green bg-hana-green bg-opacity-5'
                            : 'border-gray-200 hover:border-gray-300'
                        }`}
                      >
                        <div className="font-hana-bold text-gray-800">{account.accountName}</div>
                        <div className="text-sm text-gray-600 font-hana-medium mt-1">
                          {account.accountNumber}
                        </div>
                        <div className="text-sm text-gray-500 mt-1">
                          잔액: {formatCurrency(account.balance.toString())}원
                        </div>
                        {account.isIrp && (
                          <div className="text-xs text-blue-600 mt-2 bg-blue-50 px-2 py-1 rounded inline-block">
                            IRP 계좌 (연간 한도: 1,800만원)
                          </div>
                        )}
                      </button>
                    );
                  })}
                  {myAccounts.length === 0 && (
                    <div className="text-center py-4 text-gray-500 font-hana-medium">
                      송금 가능한 계좌가 없습니다
                    </div>
                  )}
                </div>
              </div>
            )}

            {}
            {transferType === 'external' && (
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
                {}
                {isVerifying && (
                  <div className="mt-2 text-sm text-gray-600 flex items-center">
                    <svg className="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    계좌 확인 중...
                  </div>
                )}
                {verifiedAccount && verifiedAccount.exists && (
                  <div className="mt-2 text-sm text-hana-green flex items-center">
                    <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                    </svg>
                    {verifiedAccount.accountType === 'IRP' ? 'IRP 계좌' : '일반 계좌'} 확인됨
                  </div>
                )}
                {verifiedAccount && !verifiedAccount.exists && recipientAccountNumber.length >= 10 && (
                  <div className="mt-2 text-sm text-red-600 flex items-center">
                    <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                    </svg>
                    계좌를 찾을 수 없습니다
                  </div>
                )}
              </div>
            </div>
            )}

            {}
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

            {}
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

            {}
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

            {}
            <button
              type="submit"
              disabled={
                isSubmitting ||
                isInsufficientBalance ||
                !amount ||
                (transferType === 'external' && (!recipientName.trim() || !recipientAccountNumber.trim())) ||
                (transferType === 'internal' && !selectedToAccountId && !selectedToAccountIsIrp)
              }
              className={`w-full py-4 rounded-lg font-hana-bold text-white transition-colors ${
                isSubmitting ||
                isInsufficientBalance ||
                !amount ||
                (transferType === 'external' && (!recipientName.trim() || !recipientAccountNumber.trim())) ||
                (transferType === 'internal' && !selectedToAccountId && !selectedToAccountIsIrp)
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

            {}
            <div className="flex gap-2">
              <button
                type="button"
                onClick={() => setTransferType('external')}
                className={`flex-1 py-3 rounded-lg font-hana-medium transition-colors ${
                  transferType === 'external'
                    ? 'bg-hana-green text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                타인 계좌로 송금
              </button>
              <button
                type="button"
                onClick={() => setTransferType('internal')}
                className={`flex-1 py-3 rounded-lg font-hana-medium transition-colors ${
                  transferType === 'internal'
                    ? 'bg-hana-green text-white'
                    : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
                }`}
              >
                내 계좌로 송금
              </button>
            </div>

            {}
            {transferType === 'internal' && (
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  받는 계좌 선택
                </label>
                <div className="space-y-2">
                  {myAccounts.map((account) => {
                    const isSelected = account.isIrp
                      ? selectedToAccountNumber === account.accountNumber
                      : selectedToAccountId === account.accountId;

                    return (
                      <button
                        key={account.isIrp ? account.accountNumber : account.accountId}
                        type="button"
                        onClick={() => {
                          if (account.isIrp) {
                            setSelectedToAccountId(null);
                            setSelectedToAccountNumber(account.accountNumber);
                            setSelectedToAccountIsIrp(true);
                          } else {
                            setSelectedToAccountId(account.accountId);
                            setSelectedToAccountNumber(null);
                            setSelectedToAccountIsIrp(false);
                          }
                        }}
                        className={`w-full p-4 border-2 rounded-lg text-left transition-colors ${
                          isSelected
                            ? 'border-hana-green bg-hana-green bg-opacity-5'
                            : 'border-gray-200 hover:border-gray-300'
                        }`}
                      >
                        <div className="font-hana-bold text-gray-800">{account.accountName}</div>
                        <div className="text-sm text-gray-600 font-hana-medium mt-1">
                          {account.accountNumber}
                        </div>
                        <div className="text-sm text-gray-500 mt-1">
                          잔액: {formatCurrency(account.balance.toString())}원
                        </div>
                        {account.isIrp && (
                          <div className="text-xs text-blue-600 mt-2 bg-blue-50 px-2 py-1 rounded inline-block">
                            IRP 계좌 (연간 한도: 1,800만원)
                          </div>
                        )}
                      </button>
                    );
                  })}
                  {myAccounts.length === 0 && (
                    <div className="text-center py-4 text-gray-500 font-hana-medium">
                      송금 가능한 계좌가 없습니다
                    </div>
                  )}
                </div>
              </div>
            )}

            {}
            {transferType === 'external' && (
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
                {}
                {isVerifying && (
                  <div className="mt-2 text-sm text-gray-600 flex items-center">
                    <svg className="animate-spin h-4 w-4 mr-2" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                      <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                      <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    계좌 확인 중...
                  </div>
                )}
                {verifiedAccount && verifiedAccount.exists && (
                  <div className="mt-2 text-sm text-hana-green flex items-center">
                    <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clipRule="evenodd" />
                    </svg>
                    {verifiedAccount.accountType === 'IRP' ? 'IRP 계좌' : '일반 계좌'} 확인됨
                  </div>
                )}
                {verifiedAccount && !verifiedAccount.exists && recipientAccountNumber.length >= 10 && (
                  <div className="mt-2 text-sm text-red-600 flex items-center">
                    <svg className="w-4 h-4 mr-2" fill="currentColor" viewBox="0 0 20 20">
                      <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                    </svg>
                    계좌를 찾을 수 없습니다
                  </div>
                )}
              </div>
            </div>
            )}

            {}
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

            {}
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

            {}
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

            {}
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
                disabled={
                  isSubmitting ||
                  isInsufficientBalance ||
                  !amount ||
                  (transferType === 'external' && (!recipientName.trim() || !recipientAccountNumber.trim())) ||
                  (transferType === 'internal' && !selectedToAccountId && !selectedToAccountIsIrp)
                }
                className={`flex-1 py-3 rounded-lg font-hana-bold text-white transition-colors ${
                  isSubmitting ||
                  isInsufficientBalance ||
                  !amount ||
                  (transferType === 'external' && (!recipientName.trim() || !recipientAccountNumber.trim())) ||
                  (transferType === 'internal' && !selectedToAccountId && !selectedToAccountIsIrp)
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

      {}
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