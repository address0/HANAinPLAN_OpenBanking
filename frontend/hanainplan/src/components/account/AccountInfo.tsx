import { useState, useEffect } from 'react';
import { type BankingAccount } from '../../api/bankingApi';
import { useAccountStore } from '../../store/accountStore';
import { getBankPatternByPattern } from '../../store/bankStore';
import { formatAmount } from '../../utils/fundUtils';

interface AccountInfoProps {
  onTransferClick: () => void;
}

function AccountInfo({ onTransferClick }: AccountInfoProps) {
  const {
    accounts,
    selectedAccountId,
    selectedAccountType,
    setSelectedAccount,
    allAccountsData
  } = useAccountStore();

  const [, setLocalSelectedAccount] = useState<BankingAccount | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  const getBankInfo = (accountNumber: string) => {
    if (accountNumber.length >= 3) {
      const pattern = accountNumber.substring(0, 3);
      return getBankPatternByPattern(pattern);
    }
    return null;
  };

  const getBankColor = (bankCode: string) => {
    const colorMap: { [key: string]: string } = {
      '081': 'bg-[#008485]',
      '004': 'bg-[#ffbc00]',
      '088': 'bg-[#0046ff]',
    };
    return colorMap[bankCode] || 'bg-gray-500';
  };

  useEffect(() => {
    if (allAccountsData) {
      setIsLoading(false);
    }
  }, [allAccountsData]);

  useEffect(() => {
    if (accounts.length > 0) {
      if (selectedAccountType === 'IRP') {
        return;
      }

      if (selectedAccountId) {
        const validAccount = accounts.find(acc => acc.accountId === selectedAccountId);
        if (validAccount) {
          setLocalSelectedAccount(validAccount);
        } else {
          setLocalSelectedAccount(accounts[0]);
          setSelectedAccount(accounts[0].accountId, 'BANKING');
        }
      } else {
        setLocalSelectedAccount(accounts[0]);
        setSelectedAccount(accounts[0].accountId, 'BANKING');
      }
    } else {
      setLocalSelectedAccount(null);
    }
  }, [accounts, selectedAccountId, selectedAccountType, setSelectedAccount]);

  const getAccountTypeDisplay = (accountType: number) => {
    const typeMap: { [key: number]: string } = {
      1: '입출금통장',
      2: '저축예금',
      3: '정기예금',
      4: '정기적금',
      5: '대출계좌',
      6: '신용계좌',
      7: '수익증권',
      0: '통합계좌'
    };
    return typeMap[accountType] || '기타';
  };

  if (isLoading) {
    return (
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-lg font-hana-bold text-gray-900">내 계좌</h3>
          <span className="text-sm text-gray-500 font-hana-medium">로딩 중...</span>
        </div>
        <div className="flex items-center justify-center py-8">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green"></div>
        </div>
      </div>
    );
  }

  const totalAccountCount = accounts.length + (allAccountsData?.irpAccount ? 1 : 0);

  if (totalAccountCount === 0) {
    return (
      <div className="space-y-4">
        <div className="flex justify-between items-center">
          <h3 className="text-lg font-hana-bold text-gray-900">내 계좌</h3>
          <span className="text-sm text-gray-500 font-hana-medium">0개 계좌</span>
        </div>
        <div className="text-center py-8 text-gray-500">
          보유하고 있는 계좌가 없습니다.
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {}
      <div className="flex justify-between items-center">
        <h3 className="text-lg font-hana-bold text-gray-900">내 계좌</h3>
        <span className="text-sm text-gray-500 font-hana-medium">{totalAccountCount}개 계좌</span>
      </div>

      {}
      <div className="space-y-3">
        {}
        {accounts.map((account) => {
          const bankInfo = getBankInfo(account.accountNumber);
          const bankColor = bankInfo ? getBankColor(bankInfo.code) : 'bg-gray-500';

          return (
            <div
              key={account.accountId}
              className={`${bankColor} w-full h-[140px] rounded-xl relative cursor-pointer flex flex-col justify-between p-6 text-white hover:opacity-90 transition-opacity shadow-lg ${
                selectedAccountId === account.accountId && selectedAccountType === 'BANKING' ? 'ring-4 ring-white ring-opacity-50' : ''
              }`}
              onClick={() => setSelectedAccount(account.accountId, 'BANKING')}
            >
              {}
              <div className="flex items-center justify-between">
                <div className="flex items-center space-x-3">
                  <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
                    <img
                      src={bankInfo ? `/bank/${bankInfo.code}.png` : '/bank/081.png'}
                      alt={bankInfo?.name || '은행'}
                      className="w-6 h-6 object-contain"
                    />
                  </div>
                  <div>
                    <p className="text-lg font-hana-bold">{bankInfo?.name || '은행'} {getAccountTypeDisplay(account.accountType)}</p>
                    <p className="text-sm opacity-80">{account.accountNumber}</p>
                  </div>
                </div>
                <button
                  className="bg-white/20 px-4 py-2 rounded-lg text-sm font-hana-medium hover:bg-white/30 transition-colors"
                  onClick={(e) => {
                    e.stopPropagation();
                    setSelectedAccount(account.accountId, 'BANKING');
                    onTransferClick();
                  }}
                >
                  송금
                </button>
              </div>

              {}
              <div className="text-right">
                <p className="text-3xl font-hana-bold">{formatAmount(account.balance)}원</p>
                <p className="text-sm opacity-80">{account.accountName}</p>
              </div>
            </div>
          );
        })}

        {}
        {allAccountsData?.irpAccount && (
          <div
            className={`bg-gradient-to-r from-green-500 to-emerald-600 w-full h-[140px] rounded-xl relative cursor-pointer flex flex-col justify-between p-6 text-white hover:opacity-90 transition-opacity shadow-lg ${
              selectedAccountType === 'IRP' ? 'ring-4 ring-white ring-opacity-50' : ''
            }`}
            onClick={() => setSelectedAccount(null, 'IRP')}
          >
            {}
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-3">
                <div className="w-10 h-10 bg-white/20 rounded-full flex items-center justify-center">
                  <img
                    src="/bank/081.png"
                    alt="하나은행"
                    className="w-6 h-6 object-contain"
                  />
                </div>
                <div>
                  <p className="text-lg font-hana-bold">하나은행 IRP</p>
                  <p className="text-sm opacity-80">{allAccountsData.irpAccount.accountNumber}</p>
                </div>
              </div>
              <button
                className="bg-white/20 px-4 py-2 rounded-lg text-sm font-hana-medium hover:bg-white/30 transition-colors"
                onClick={(e) => {
                  e.stopPropagation();
                  window.location.href = '/portfolio';
                }}
              >
                포트폴리오
              </button>
            </div>

            {}
            <div className="text-right">
              <p className="text-3xl font-hana-bold">{formatAmount(allAccountsData.irpAccount.currentBalance)}원</p>
              <p className="text-sm opacity-80">IRP 계좌</p>
            </div>
          </div>
        )}
      </div>

      {}
      <div className="flex justify-center pt-4">
        <button className="w-full py-3 border-2 border-dashed border-gray-300 rounded-lg text-gray-500 hover:border-hana-green hover:text-hana-green transition-colors font-hana-medium">
          + 계좌 추가하기
        </button>
      </div>
    </div>
  );
}

export default AccountInfo;