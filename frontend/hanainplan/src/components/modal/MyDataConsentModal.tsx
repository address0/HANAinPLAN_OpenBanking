import React, { useState } from 'react';
import { processMyDataConsent } from '../../api/userApi';
import type { MyDataConsentRequest, BankAccountInfo } from '../../api/userApi';

interface MyDataConsentModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConsent: (consent: boolean, bankAccountInfo?: BankAccountInfo[]) => void;
  personalInfo: {
    phoneNumber: string;
    socialNumber: string;
    name: string;
    ci?: string;
  };
}


const MyDataConsentModal: React.FC<MyDataConsentModalProps> = ({
  isOpen,
  onClose,
  onConsent,
  personalInfo
}) => {
  const [isLoading, setIsLoading] = useState(false);
  const [bankAccountInfo, setBankAccountInfo] = useState<BankAccountInfo[]>([]);
  const [totalAccounts, setTotalAccounts] = useState(0);
  const [hasSearched, setHasSearched] = useState(false);

  const handleSearchAccounts = async () => {
    setIsLoading(true);
    try {
      const request: MyDataConsentRequest = {
        phoneNumber: personalInfo.phoneNumber,
        socialNumber: personalInfo.socialNumber,
        name: personalInfo.name,
        consentToMyDataCollection: true
      };

      const data = await processMyDataConsent(request);
      console.log("=== 마이데이터 응답 데이터 ===");
      console.log("전체 응답:", data);
      console.log("계좌 정보:", data.bankAccountInfo);
      if (data.bankAccountInfo && data.bankAccountInfo.length > 0) {
        data.bankAccountInfo.forEach((bank, bankIndex) => {
          console.log(`은행 ${bankIndex + 1} (${bank.bankName}):`, bank);
          if (bank.accounts) {
            bank.accounts.forEach((account, accountIndex) => {
              console.log(`  계좌 ${accountIndex + 1}:`, account);
              console.log(`    openingDate:`, account.openingDate, typeof account.openingDate);
              console.log(`    createdAt:`, account.createdAt, typeof account.createdAt);
              console.log(`    updatedAt:`, account.updatedAt, typeof account.updatedAt);
            });
          }
        });
      }
      console.log("================================");
      setBankAccountInfo(data.bankAccountInfo || []);
      setTotalAccounts(data.totalAccounts || 0);
      setHasSearched(true);
    } catch (error) {
      console.error('마이데이터 수집 동의 요청 실패:', error);
      setHasSearched(true);
    } finally {
      setIsLoading(false);
    }
  };

  const handleFinalConsent = () => {
    // 최종 동의 처리 - 계좌 정보와 함께 전달
    onConsent(true, bankAccountInfo);
  };

  const getAccountTypeText = (type: number) => {
    switch (type) {
      case 1: return '수시입출금';
      case 2: return '예적금';
      case 6: return '수익증권';
      case 0: return '통합계좌';
      default: return '기타';
    }
  };

  const formatBalance = (balance: number) => {
    return new Intl.NumberFormat('ko-KR').format(balance) + '원';
  };

  const formatDate = (dateInput: number[] | string) => {
    if (Array.isArray(dateInput) && dateInput.length >= 3) {
      // 배열: [년, 월, 일, 시, 분, 초, 나노초]
      const [year, month, day, hour = 0, minute = 0, second = 0] = dateInput;
      const date = new Date(year, month - 1, day, hour, minute, second); // month는 0부터 시작
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    } else if (typeof dateInput === 'string') {
      // 문자열인 경우 기존 방식
      const date = new Date(dateInput);
      return date.toLocaleDateString('ko-KR', {
        year: 'numeric',
        month: '2-digit',
        day: '2-digit'
      });
    }
    return '날짜 정보 없음';
  };

  const formatAccountNumber = (accountNumber: string) => {
    // 계좌번호를 4자리씩 대시로 구분
    return accountNumber.replace(/(\d{4})(?=\d)/g, '$1-');
  };

  const getBankLogoSrc = (bankCode: string) => {
    switch (bankCode) {
      case '081': // 하나은행
      case '001':
        return '/bank/081.png';
      case '088': // 신한은행
      case '002':
        return '/bank/088.png';
      case '004': // 국민은행
      case '003':
        return '/bank/004.png';
      default:
        return '/bank/081.png';
    }
  };

  const getMissingBankPromotions = () => {
    const allBanks = [
      { name: '하나은행', code: '001', message: '하나은행 계좌를 만들면 하나인플랜 서비스에서의 예적금 금리 혜택을 볼 수 있어요' }
    ];
    
    const existingBankCodes = bankAccountInfo.map(bank => bank.bankCode);
    return allBanks.filter(bank => !existingBankCodes.includes(bank.code));
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
      <div className="bg-white rounded-lg p-6 max-w-2xl w-full mx-4 max-h-[90vh] overflow-y-auto">
        {/* 헤더 */}
        <div className="flex justify-between items-center mb-6">
          <div className="flex items-center gap-3">
            <svg className="w-6 h-6 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m5.618-4.016A11.955 11.955 0 0112 2.944a11.955 11.955 0 01-8.618 3.04A12.02 12.02 0 003 9c0 5.591 3.824 10.29 9 11.622 5.176-1.332 9-6.03 9-11.622 0-1.042-.133-2.052-.382-3.016z" />
            </svg>
            <h2 className="text-xl font-bold text-gray-900">마이데이터 수집 동의</h2>
          </div>
          <button
            onClick={onClose}
            className="text-gray-400 hover:text-gray-600 transition-colors"
          >
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {/* 동의 안내 */}
        <div className="mb-6 p-4 bg-blue-50 rounded-lg">
          <div className="flex items-start gap-3">
            <svg className="w-5 h-5 text-blue-600 mt-0.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-2.5L13.732 4c-.77-.833-1.964-.833-2.732 0L3.732 16.5c-.77.833.192 2.5 1.732 2.5z" />
            </svg>
            <div>
              <h3 className="font-semibold text-blue-900 mb-2">마이데이터 수집에 동의하시겠습니까?</h3>
              <p className="text-blue-800 text-sm leading-relaxed">
                동의하시면 등록된 CI 정보를 바탕으로 각 은행사에서 고객 여부를 확인하고, 
                해당하는 계좌 정보를 조회하여 통합 계좌 관리 서비스를 제공할 수 있습니다.
              </p>
            </div>
          </div>
        </div>

        {/* 계좌 조회 전 안내 */}
        {!hasSearched && (
          <div className="mb-6 text-center">
            <div className="mb-4">
              <img 
                src="/images/myData.png" 
                alt="마이데이터 연동" 
                className="w-32 h-32 mx-auto object-contain"
              />
            </div>
            <h3 className="text-lg font-semibold text-gray-800 mb-2">
              마이데이터를 연동하면 계좌 정보를 한 번에 관리할 수 있어요
            </h3>
            <p className="text-gray-600 text-sm mb-4">
              각 은행사에서 보유한 계좌 정보를 통합하여 관리할 수 있습니다.
            </p>
          </div>
        )}

        {/* 계좌 정보 표시 */}
        {hasSearched && bankAccountInfo.length > 0 && (
          <div className="mb-6">
            <h3 className="font-semibold text-gray-900 mb-4">
              발견된 계좌 정보 ({totalAccounts}개 계좌)
            </h3>
            <div className="space-y-4">
              {bankAccountInfo.map((bank, index) => (
                <div key={index} className="border rounded-lg p-4">
                  <div className="flex items-center gap-2 mb-3">
                    <img src={getBankLogoSrc(bank.bankCode)} alt={`${bank.bankName} 로고`} className="w-5 h-5 object-contain" />
                    <h4 className="font-semibold text-gray-900">{bank.bankName}</h4>
                    <span className="text-sm text-gray-600">({bank.accounts.length}개 계좌)</span>
                  </div>
                  <div className="space-y-2">
                    {bank.accounts.map((account, accIndex) => (
                      <div key={accIndex} className="bg-gray-50 p-3 rounded">
                        <div className="flex justify-between items-start">
                          <div>
                            <p className="font-medium text-gray-900">
                              {getAccountTypeText(account.accountType)}
                            </p>
                            <p className="text-sm text-gray-600">
                              {formatAccountNumber(account.accountNumber)}
                            </p>
                          </div>
                          <div className="text-right">
                            <p className="font-semibold text-gray-900">
                              {formatBalance(account.balance)}
                            </p>
                            <p className="text-xs text-gray-500">
                              개설일: {formatDate(account.openingDate)}
                            </p>
                          </div>
                        </div>
                      </div>
                    ))}
                  </div>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 계좌가 일부만 있을 때 누락된 은행 안내 */}
        {hasSearched && bankAccountInfo.length > 0 && getMissingBankPromotions().length > 0 && (
          <div className="mb-6">
            <h3 className="font-semibold text-gray-900 mb-4">추가 계좌 개설 안내</h3>
            <div className="space-y-3">
              {getMissingBankPromotions().map((bank, index) => (
                <div key={index} className="p-3 bg-[#008485]/10 rounded-lg border border-[#008485]/30">
                  <div className="flex items-center gap-2 mb-2">
                    <img src={getBankLogoSrc(bank.code)} alt={`${bank.name} 로고`} className="w-5 h-5 object-contain" />
                    <span className="font-medium text-[#008485]">{bank.name}</span>
                  </div>
                  <p className="text-sm text-[#008485] text-left">
                    {bank.message}
                  </p>
                </div>
              ))}
            </div>
          </div>
        )}

        {/* 계좌가 없을 때 메시지 */}
        {hasSearched && bankAccountInfo.length === 0 && (
          <div className="mb-6">
            <div className="text-center p-6 bg-gray-50 rounded-lg">
              <svg className="w-12 h-12 text-gray-400 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20 13V6a2 2 0 00-2-2H6a2 2 0 00-2 2v7m16 0v5a2 2 0 01-2 2H6a2 2 0 01-2-2v-5m16 0h-2.586a1 1 0 00-.707.293l-2.414 2.414a1 1 0 01-.707.293h-3.172a1 1 0 01-.707-.293l-2.414-2.414A1 1 0 006.586 13H4" />
              </svg>
              <h3 className="text-lg font-semibold text-gray-700 mb-2">등록된 계좌 정보가 없습니다</h3>
              <p className="text-gray-500 text-sm mb-4">
                해당 정보로 등록된 고객이 없거나 계좌가 없습니다.
              </p>
              
              {/* 은행별 계좌 생성 안내 */}
              <div className="mt-4 space-y-3">
                {getMissingBankPromotions().map((bank, index) => (
                  <div key={index} className="p-3 bg-white rounded-lg border border-gray-200">
                    <div className="flex items-center gap-2 mb-2">
                      <div className="w-6 h-6 bg-blue-100 rounded-full flex items-center justify-center">
                        <svg className="w-4 h-4 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1" />
                        </svg>
                      </div>
                      <span className="font-medium text-gray-800">{bank.name}</span>
                    </div>
                    <p className="text-sm text-gray-600 text-left">
                      {bank.message}
                    </p>
                  </div>
                ))}
              </div>
            </div>
          </div>
        )}

        {/* 버튼 */}
        <div className="flex gap-3 justify-end">
          <button
            onClick={() => onConsent(false)}
            disabled={isLoading}
            className="px-4 py-2 text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors disabled:opacity-50"
          >
            거부
          </button>
          
          {!hasSearched ? (
            <button
              onClick={handleSearchAccounts}
              disabled={isLoading}
              className="px-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 flex items-center gap-2"
            >
              {isLoading && (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              )}
              연동하기
            </button>
          ) : (
            <button
              onClick={handleFinalConsent}
              className="px-4 py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors flex items-center gap-2"
            >
              확인
            </button>
          )}
        </div>
      </div>
    </div>
  );
};

export default MyDataConsentModal;
