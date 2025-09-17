import { useState, useEffect } from 'react';
import { type BankingAccount } from '../../api/bankingApi';
import { useAccountStore } from '../../store/accountStore';

interface AccountInfoProps {
  onTransferClick: () => void;
  userId: number;
}

function AccountInfo({ onTransferClick, userId }: AccountInfoProps) {
  const { 
    accounts, 
    selectedAccountId, 
    setSelectedAccount
  } = useAccountStore();
  
  const [selectedAccount, setLocalSelectedAccount] = useState<BankingAccount | null>(null);

  useEffect(() => {
    if (accounts.length > 0) {
      const account = accounts.find(acc => acc.accountId === selectedAccountId) || accounts[0];
      setLocalSelectedAccount(account);
      if (!selectedAccountId) {
        setSelectedAccount(account.accountId);
      }
    } else {
      setLocalSelectedAccount(null);
    }
  }, [accounts, selectedAccountId, setSelectedAccount]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount);
  };

  const getAccountTypeDisplay = (accountType: string) => {
    const typeMap: { [key: string]: string } = {
      'CHECKING': '입출금통장',
      'SAVINGS': '저축예금',
      'TIME_DEPOSIT': '정기예금',
      'FIXED_DEPOSIT': '정기적금',
      'LOAN': '대출계좌',
      'CREDIT': '신용계좌'
    };
    return typeMap[accountType] || accountType;
  };

  if (accounts.length === 0) {
    return null; // 계좌가 없을 때는 아무것도 렌더링하지 않음
  }

  return (
    <div className="space-y-4">
      {/* 계좌 선택 드롭다운 */}
      {accounts.length > 1 && (
        <div className="bg-white rounded-lg p-4 shadow-sm">
          <label className="block text-sm font-hana-medium text-gray-700 mb-2">계좌 선택</label>
          <select
            value={selectedAccount?.accountId || ''}
            onChange={(e) => {
              const accountId = parseInt(e.target.value);
              setSelectedAccount(accountId);
            }}
            className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
          >
            {accounts.map((account) => (
              <option key={account.accountId} value={account.accountId}>
                {getAccountTypeDisplay(account.accountType)} - {account.accountNumber}
              </option>
            ))}
          </select>
        </div>
      )}

      {/* 선택된 계좌 정보 */}
      {selectedAccount && (
        <div 
          className="bg-hana-green w-full h-[240px] rounded-lg relative cursor-pointer flex flex-col justify-center items-center">
          <div className="flex items-center mb-6 text-white text-lg font-hana-regular absolute top-6 left-6">
            <img src="/bank/081.png" alt="bank_logo" className="w-8 h-8 mr-2" />
            하나은행 {selectedAccount.accountNumber}
            <img src="/icons/info.png" alt="info" className="w-5 h-5 ml-2" />
          </div>
          <div className="text-center">
            <p className="text-white text-xl font-hana-medium mb-2">{selectedAccount.accountName + ' (' + getAccountTypeDisplay(selectedAccount.accountType) + ')'}</p>
            <p className="text-white text-4xl font-hana-medium">{formatCurrency(selectedAccount.balance)} 원</p>
          </div>
          <button 
            className="text-white text-lg font-hana-regular absolute bottom-6 right-6 bg-black/20 px-4 py-2 rounded-lg hover:bg-black/30 transition-colors" 
            onClick={onTransferClick}
          >
            송금
          </button>
        </div>
      )}
    </div>
  );
}

export default AccountInfo;