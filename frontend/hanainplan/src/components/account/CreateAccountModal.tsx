import { useState } from 'react';
import { createBankingAccount, type CreateAccountRequest } from '../../api/bankingApi';
import { useAccountStore } from '../../store/accountStore';

interface CreateAccountModalProps {
  isOpen: boolean;
  onClose: () => void;
  userId: number;
  onAccountCreated: () => void;
}

function CreateAccountModal({ isOpen, onClose, userId, onAccountCreated }: CreateAccountModalProps) {
  const [formData, setFormData] = useState({
    accountType: 'CHECKING' as 'CHECKING' | 'SAVINGS' | 'TIME_DEPOSIT' | 'FIXED_DEPOSIT' | 'LOAN' | 'CREDIT',
    accountName: '',
    description: '',
    purpose: '',
    monthlyDepositAmount: 0,
    depositPeriod: 12,
    interestPaymentMethod: 'AUTO' as 'AUTO' | 'MANUAL',
    accountPassword: '',
    confirmPassword: ''
  });
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { addAccount } = useAccountStore();

  const accountTypes = [
    {
      value: 'CHECKING',
      label: '입출금통장',
      description: '자유롭게 입출금 가능한 기본 계좌',
      features: ['자유입출금', '이자지급', '카드연결가능']
    },
    {
      value: 'TIME_DEPOSIT',
      label: '정기예금',
      description: '일정 기간 예치하여 높은 이자 수익',
      features: ['고정기간예치', '높은이자율', '자동갱신가능']
    },
    {
      value: 'FIXED_DEPOSIT',
      label: '정기적금',
      description: '매월 정해진 금액을 적립하는 저축 상품',
      features: ['월정기적립', '복리이자', '목표금액설정']
    }
  ];

  const purposes = [
    { value: 'SAVINGS', label: '일반저축용' },
    { value: 'INVESTMENT', label: '투자자금용' },
    { value: 'EMERGENCY', label: '비상금용' },
    { value: 'RETIREMENT', label: '노후준비용' }
  ];

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    if (!formData.accountName.trim()) {
      setError('계좌명을 입력해주세요.');
      return;
    }

    if (!formData.purpose) {
      setError('계좌 용도를 선택해주세요.');
      return;
    }

    if (!formData.accountPassword.trim()) {
      setError('계좌 비밀번호를 입력해주세요.');
      return;
    }

    if (formData.accountPassword !== formData.confirmPassword) {
      setError('계좌 비밀번호가 일치하지 않습니다.');
      return;
    }

    if (formData.accountPassword.length < 4) {
      setError('계좌 비밀번호는 4자리 이상이어야 합니다.');
      return;
    }

    if (formData.accountType === 'FIXED_DEPOSIT') {
      if (formData.monthlyDepositAmount <= 0) {
        setError('월 적립 금액을 입력해주세요.');
        return;
      }
      if (formData.depositPeriod < 6 || formData.depositPeriod > 60) {
        setError('적립 기간은 6개월 이상 60개월 이하여야 합니다.');
        return;
      }
    }

    if (formData.accountType === 'TIME_DEPOSIT') {
      if (formData.depositPeriod < 1 || formData.depositPeriod > 60) {
        setError('예치 기간은 1개월 이상 60개월 이하여야 합니다.');
        return;
      }
    }

    try {
      setIsSubmitting(true);
      setError(null);

      const request: CreateAccountRequest = {
        userId,
        accountType: formData.accountType,
        accountName: formData.accountName.trim(),
        initialBalance: 0,
        description: formData.description.trim() || undefined,
        purpose: formData.purpose || undefined,
        monthlyDepositAmount: formData.monthlyDepositAmount || undefined,
        depositPeriod: formData.depositPeriod || undefined,
        interestPaymentMethod: formData.interestPaymentMethod || undefined,
        accountPassword: formData.accountPassword || undefined
      };

      const newAccount = await createBankingAccount(request);

      addAccount(newAccount);

      setFormData({
        accountType: 'CHECKING',
        accountName: '',
        description: '',
        purpose: '',
        monthlyDepositAmount: 0,
        depositPeriod: 12,
        interestPaymentMethod: 'AUTO',
        accountPassword: '',
        confirmPassword: ''
      });

      onAccountCreated();
      onClose();
    } catch (err) {
      setError('계좌 생성에 실패했습니다. 다시 시도해주세요.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleClose = () => {
    if (!isSubmitting) {
      setFormData({
        accountType: 'CHECKING',
        accountName: '',
        description: '',
        purpose: '',
        monthlyDepositAmount: 0,
        depositPeriod: 12,
        interestPaymentMethod: 'AUTO',
        accountPassword: '',
        confirmPassword: ''
      });
      setError(null);
      onClose();
    }
  };

  if (!isOpen) return null;

  return (
    <div
      className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
      onClick={handleClose}
    >
      <div
        className="bg-white rounded-lg p-6 w-full max-w-2xl max-h-[90vh] overflow-y-auto"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex justify-between items-center mb-6">
          <h2 className="text-xl font-hana-bold text-gray-800">새 계좌 생성</h2>
          <button
            onClick={handleClose}
            disabled={isSubmitting}
            className="text-gray-400 hover:text-gray-600 transition-colors disabled:opacity-50"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        <form onSubmit={handleSubmit} className="space-y-4">
          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-3">
              계좌 유형 *
            </label>
            <div className="grid grid-cols-1 gap-3">
              {accountTypes.map((type) => (
                <div
                  key={type.value}
                  className={`p-4 border-2 rounded-lg cursor-pointer transition-all ${
                    formData.accountType === type.value
                      ? 'border-hana-green bg-green-50'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                  onClick={() => setFormData({ ...formData, accountType: type.value as any })}
                >
                  <div className="flex items-center justify-between">
                    <div>
                      <h3 className="font-hana-bold text-gray-800">{type.label}</h3>
                      <p className="text-sm text-gray-600 mt-1">{type.description}</p>
                      <div className="flex flex-wrap gap-1 mt-2">
                        {type.features.map((feature, index) => (
                          <span
                            key={index}
                            className="px-2 py-1 bg-gray-100 text-xs text-gray-600 rounded"
                          >
                            {feature}
                          </span>
                        ))}
                      </div>
                    </div>
                    <div className="w-4 h-4 border-2 rounded-full flex items-center justify-center">
                      {formData.accountType === type.value && (
                        <div className="w-2 h-2 bg-hana-green rounded-full"></div>
                      )}
                    </div>
                  </div>
                </div>
              ))}
            </div>
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              계좌명 *
            </label>
            <input
              type="text"
              value={formData.accountName}
              onChange={(e) => setFormData({ ...formData, accountName: e.target.value })}
              className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
              placeholder="예: 하나원큐 통장"
              required
            />
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              계좌 용도 *
            </label>
            <select
              value={formData.purpose}
              onChange={(e) => setFormData({ ...formData, purpose: e.target.value })}
              className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
              required
            >
              <option value="">계좌 용도를 선택하세요</option>
              {purposes.map((purpose) => (
                <option key={purpose.value} value={purpose.value}>
                  {purpose.label}
                </option>
              ))}
            </select>
          </div>

          {}
          {(formData.accountType === 'FIXED_DEPOSIT' || formData.accountType === 'TIME_DEPOSIT') && (
            <>
              <div className="border-t pt-4">
                <h3 className="text-lg font-hana-bold text-gray-800 mb-4">
                  {formData.accountType === 'FIXED_DEPOSIT' ? '적금 설정' : '예금 설정'}
                </h3>

                {}
                {formData.accountType === 'FIXED_DEPOSIT' && (
                  <div className="mb-4">
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                      월 적립 금액 *
                    </label>
                    <input
                      type="number"
                      min="10000"
                      step="10000"
                      value={formData.monthlyDepositAmount}
                      onChange={(e) => setFormData({ ...formData, monthlyDepositAmount: parseInt(e.target.value) || 0 })}
                      className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      placeholder="100000"
                      required
                    />
                    <p className="text-xs text-gray-500 mt-1">최소 10,000원부터 가능합니다.</p>
                  </div>
                )}

                {}
                <div className="mb-4">
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    {formData.accountType === 'FIXED_DEPOSIT' ? '적립 기간' : '예치 기간'} *
                  </label>
                  <div className="flex items-center space-x-2">
                    <input
                      type="number"
                      min={formData.accountType === 'FIXED_DEPOSIT' ? 6 : 1}
                      max="60"
                      value={formData.depositPeriod}
                      onChange={(e) => setFormData({ ...formData, depositPeriod: parseInt(e.target.value) || 12 })}
                      className="flex-1 p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      required
                    />
                    <span className="text-gray-600 font-hana-medium">개월</span>
                  </div>
                  <p className="text-xs text-gray-500 mt-1">
                    {formData.accountType === 'FIXED_DEPOSIT'
                      ? '6개월 이상 60개월 이하'
                      : '1개월 이상 60개월 이하'
                    }
                  </p>
                </div>

                {}
                <div className="mb-4">
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    이자 지급 방법
                  </label>
                  <div className="flex space-x-4">
                    <label className="flex items-center">
                      <input
                        type="radio"
                        value="AUTO"
                        checked={formData.interestPaymentMethod === 'AUTO'}
                        onChange={(e) => setFormData({ ...formData, interestPaymentMethod: e.target.value as 'AUTO' | 'MANUAL' })}
                        className="mr-2"
                      />
                      <span className="text-sm font-hana-medium">자동 지급</span>
                    </label>
                    <label className="flex items-center">
                      <input
                        type="radio"
                        value="MANUAL"
                        checked={formData.interestPaymentMethod === 'MANUAL'}
                        onChange={(e) => setFormData({ ...formData, interestPaymentMethod: e.target.value as 'AUTO' | 'MANUAL' })}
                        className="mr-2"
                      />
                      <span className="text-sm font-hana-medium">수동 지급</span>
                    </label>
                  </div>
                </div>
              </div>
            </>
          )}

          {}
          <div className="border-t pt-4">
            <h3 className="text-lg font-hana-bold text-gray-800 mb-4">보안 설정</h3>

            <div className="mb-4">
              <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                계좌 비밀번호 *
              </label>
              <input
                type="password"
                value={formData.accountPassword}
                onChange={(e) => setFormData({ ...formData, accountPassword: e.target.value })}
                className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                placeholder="4자리 이상 입력하세요"
                required
              />
            </div>

            <div className="mb-4">
              <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                비밀번호 확인 *
              </label>
              <input
                type="password"
                value={formData.confirmPassword}
                onChange={(e) => setFormData({ ...formData, confirmPassword: e.target.value })}
                className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                placeholder="비밀번호를 다시 입력하세요"
                required
              />
            </div>
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              계좌 설명
            </label>
            <textarea
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
              placeholder="계좌에 대한 설명을 입력하세요 (선택사항)"
              rows={3}
            />
          </div>

          {}
          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3">
              <p className="text-red-600 font-hana-medium text-sm">{error}</p>
            </div>
          )}

          {}
          <div className="flex gap-3 pt-4">
            <button
              type="button"
              onClick={handleClose}
              disabled={isSubmitting}
              className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-hana-medium disabled:opacity-50"
            >
              취소
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex-1 px-4 py-3 bg-hana-green text-white rounded-lg hover:bg-hana-green/80 transition-colors font-hana-medium disabled:opacity-50"
            >
              {isSubmitting ? '생성 중...' : '계좌 생성'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}

export default CreateAccountModal;