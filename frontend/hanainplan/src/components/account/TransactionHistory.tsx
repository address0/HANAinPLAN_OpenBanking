import { useState, useEffect } from 'react';
import { getTransactionHistory, type Transaction } from '../../api/bankingApi';
import { useAccountStore } from '../../store/accountStore';
import { useUserStore } from '../../store/userStore';

interface TransactionHistoryProps {
  refreshTrigger?: number;
}

type FilterType = 'ALL' | 'DEPOSIT' | 'WITHDRAWAL' | 'KEYWORD';
type SortOrder = 'DESC' | 'ASC';
type PeriodType = '1WEEK' | '1MONTH' | '3MONTHS' | '1YEAR';

function TransactionHistory({ refreshTrigger }: TransactionHistoryProps) {
  const [transactions, setTransactions] = useState<Transaction[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [showFilterModal, setShowFilterModal] = useState(false);
  const [filters, setFilters] = useState({
    type: 'ALL' as FilterType,
    sortOrder: 'DESC' as SortOrder,
    period: '3MONTHS' as PeriodType,
    keyword: ''
  });
  
  const { selectedAccountId } = useAccountStore();
  const { user } = useUserStore();

  // 필터 옵션들
  const filterOptions = [
    { value: 'ALL', label: '전체조회' },
    { value: 'DEPOSIT', label: '입금' },
    { value: 'WITHDRAWAL', label: '출금' },
    { value: 'KEYWORD', label: '키워드검색' }
  ];

  const sortOptions = [
    { value: 'DESC', label: '최신순' },
    { value: 'ASC', label: '오래된순' }
  ];

  const periodOptions = [
    { value: '1WEEK', label: '1주' },
    { value: '1MONTH', label: '1개월' },
    { value: '3MONTHS', label: '3개월' },
    { value: '1YEAR', label: '1년' }
  ];

  // 기간 계산 함수
  const getDateRange = (period: PeriodType) => {
    const now = new Date();
    const startDate = new Date();
    
    switch (period) {
      case '1WEEK':
        startDate.setDate(now.getDate() - 7);
        break;
      case '1MONTH':
        startDate.setMonth(now.getMonth() - 1);
        break;
      case '3MONTHS':
        startDate.setMonth(now.getMonth() - 3);
        break;
      case '1YEAR':
        startDate.setFullYear(now.getFullYear() - 1);
        break;
    }
    
    return {
      startDate: startDate.toISOString().split('T')[0],
      endDate: now.toISOString().split('T')[0]
    };
  };

  useEffect(() => {
    if (selectedAccountId && user) {
      loadTransactions();
    } else {
      // 계좌가 없으면 로딩 상태를 false로 설정
      setIsLoading(false);
    }
  }, [selectedAccountId, user, refreshTrigger]);

  const loadTransactions = async (customFilters?: typeof filters) => {
    if (!selectedAccountId || !user) return;
    
    try {
      setIsLoading(true);
      setError(null);
      
      const currentFilters = customFilters || filters;
      const dateRange = getDateRange(currentFilters.period);
      
      const response = await getTransactionHistory({
        accountId: selectedAccountId,
        page: 0,
        size: 20,
        startDate: dateRange.startDate,
        endDate: dateRange.endDate,
        transactionType: currentFilters.type === 'ALL' ? undefined : currentFilters.type === 'DEPOSIT' ? 'DEPOSIT' : currentFilters.type === 'WITHDRAWAL' ? 'WITHDRAWAL' : undefined,
        sortDirection: currentFilters.sortOrder
      });
      
      let filteredTransactions = response.content || [];
      
      // 프론트엔드에서 추가 필터링
      if (currentFilters.type === 'DEPOSIT') {
        filteredTransactions = filteredTransactions.filter(transaction => 
          transaction.transactionType === 'DEPOSIT' || transaction.amount > 0
        );
      } else if (currentFilters.type === 'WITHDRAWAL') {
        filteredTransactions = filteredTransactions.filter(transaction => 
          transaction.transactionType === 'WITHDRAWAL' || transaction.amount < 0
        );
      }
      
      // 키워드 검색 필터링
      if (currentFilters.type === 'KEYWORD' && currentFilters.keyword.trim()) {
        filteredTransactions = filteredTransactions.filter(transaction =>
          transaction.description?.toLowerCase().includes(currentFilters.keyword.toLowerCase()) ||
          transaction.memo?.toLowerCase().includes(currentFilters.keyword.toLowerCase())
        );
      }
      
      setTransactions(filteredTransactions);
    } catch (err) {
      console.error('거래내역 로드 오류:', err);
      setError('거래내역을 불러오는데 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(Math.abs(amount));
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit'
    });
  };

  const formatTime = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  };

  // 필터 변경 핸들러
  const handleFilterChange = (key: keyof typeof filters, value: string) => {
    setFilters(prev => ({ ...prev, [key]: value }));
  };

  // 필터 적용
  const applyFilters = () => {
    loadTransactions(filters);
    setShowFilterModal(false);
  };

  // 현재 필터 표시 텍스트
  const getFilterDisplayText = () => {
    const typeText = filterOptions.find(opt => opt.value === filters.type)?.label || '전체조회';
    const sortText = sortOptions.find(opt => opt.value === filters.sortOrder)?.label || '최신순';
    const periodText = periodOptions.find(opt => opt.value === filters.period)?.label || '3개월';
    
    if (filters.type === 'KEYWORD' && filters.keyword.trim()) {
      return `${typeText} / ${sortText} / ${periodText} (${filters.keyword})`;
    }
    
    return `${typeText} / ${sortText} / ${periodText}`;
  };

  const getTransactionTypeDisplay = (type: string) => {
    const typeMap: { [key: string]: string } = {
      'DEPOSIT': '입금',
      'WITHDRAWAL': '출금',
      'TRANSFER_IN': '이체입금',
      'TRANSFER_OUT': '이체출금',
      'CARD_PAYMENT': '카드결제',
      'ATM_WITHDRAWAL': 'ATM출금',
      'SALARY': '급여입금',
      'INTEREST': '이자지급',
      'FEE': '수수료',
      'AUTO_TRANSFER': '자동이체'
    };
    return typeMap[type] || type;
  };

  // 계좌가 없으면 아무것도 렌더링하지 않음
  if (!selectedAccountId) {
    return null;
  }

  if (isLoading) {
    return (
      <div className="bg-white rounded-lg overflow-hidden">
        <div className="p-4 border-b border-gray-200 flex items-center">
          <img src="/icons/settings-gray.png" alt="settings" className="w-5 h-5 mr-2" />
          <span className="text-sm font-hana-medium text-gray-600">거래내역 조회 중...</span>
        </div>
        <div className="p-8 text-center">
          <div className="text-gray-500 font-hana-medium">거래내역을 불러오는 중...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="bg-white rounded-lg overflow-hidden">
        <div className="p-4 border-b border-gray-200 flex items-center">
          <img src="/icons/settings-gray.png" alt="settings" className="w-5 h-5 mr-2" />
          <span className="text-sm font-hana-medium text-gray-600">거래내역 조회 오류</span>
        </div>
        <div className="p-8 text-center">
          <div className="text-red-500 font-hana-medium">{error}</div>
        </div>
      </div>
    );
  }

  if (transactions.length === 0) {
    return (
      <div className="bg-white rounded-lg overflow-hidden">
        <div className="p-4 border-b border-gray-200 flex items-center">
          <img src="/icons/settings-gray.png" alt="settings" className="w-5 h-5 mr-2" />
          <span className="text-sm font-hana-medium text-gray-600">전체조회 / 최신순 / 3개월</span>
        </div>
        <div className="p-8 text-center">
          <div className="text-gray-500 font-hana-medium">거래내역이 없습니다.</div>
        </div>
      </div>
    );
  }

  return (
    <>
      <div className="bg-white rounded-lg overflow-hidden">
        <div 
          className="p-4 border-b border-gray-200 flex items-center cursor-pointer hover:bg-gray-50 transition-colors"
          onClick={() => setShowFilterModal(true)}
        >
          <img src="/icons/settings-gray.png" alt="settings" className="w-5 h-5 mr-2" />
          <span className="text-sm font-hana-medium text-gray-600">{getFilterDisplayText()}</span>
          <svg className="w-4 h-4 ml-auto text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
          </svg>
        </div>
        {transactions.map((transaction) => (
          <div key={transaction.transactionId} className="flex justify-between items-center py-4 px-6 border-b border-gray-200 hover:bg-gray-50 transition-colors">
            <div className="flex-1">
              <div className="font-hana-medium text-gray-800 mb-1 text-lg">
                {getTransactionTypeDisplay(transaction.transactionType)}
              </div>
              <div className="text-sm text-gray-500 font-hana-regular">
                {formatDate(transaction.transactionDate)} {formatTime(transaction.transactionDate)}
              </div>
              {transaction.memo && (
                <div className="text-xs text-gray-400 font-hana-regular mt-1">
                  {transaction.memo}
                </div>
              )}
            </div>
            <div className="text-right">
              <div className={`font-hana-bold text-lg mb-1 ${
                transaction.amount > 0 ? 'text-blue-600' : transaction.amount < 0 ? 'text-red-600' : 'text-gray-600'
              }`}>
                {transaction.amount > 0 ? '+' : transaction.amount < 0 ? '-' : ''}
                {transaction.amount !== 0 && formatCurrency(transaction.amount)} 원
              </div>
              <div className="text-sm text-gray-500 font-hana-regular">
                잔액: {formatCurrency(transaction.balanceAfter)}원
              </div>
            </div>
          </div>
        ))}
      </div>
      
      {/* 필터 모달 */}
      {showFilterModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4">
          <div className="bg-white rounded-lg shadow-xl max-w-md w-full max-h-[90vh] overflow-y-auto">
            <div className="p-6">
              <div className="flex justify-between items-center mb-6">
                <h3 className="text-lg font-hana-bold text-gray-800">거래내역 필터</h3>
                <button 
                  onClick={() => setShowFilterModal(false)}
                  className="text-gray-500 text-2xl hover:text-gray-700"
                >
                  ×
                </button>
              </div>

              <div className="space-y-6">
                {/* 조회 유형 */}
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-3">조회 유형</label>
                  <div className="grid grid-cols-2 gap-2">
                    {filterOptions.map((option) => (
                      <button
                        key={option.value}
                        onClick={() => handleFilterChange('type', option.value)}
                        className={`py-2 px-3 rounded-lg text-sm font-hana-medium transition-colors ${
                          filters.type === option.value
                            ? 'bg-hana-green text-white'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                      >
                        {option.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* 키워드 검색 */}
                {filters.type === 'KEYWORD' && (
                  <div>
                    <label className="block text-sm font-hana-medium text-gray-700 mb-2">검색 키워드</label>
                    <input
                      type="text"
                      value={filters.keyword}
                      onChange={(e) => handleFilterChange('keyword', e.target.value)}
                      placeholder="거래내역에서 검색할 키워드를 입력하세요"
                      className="w-full px-3 py-2 border border-gray-300 rounded-lg font-hana-medium focus:outline-none focus:ring-2 focus:ring-hana-green"
                    />
                  </div>
                )}

                {/* 정렬 순서 */}
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-3">정렬 순서</label>
                  <div className="grid grid-cols-2 gap-2">
                    {sortOptions.map((option) => (
                      <button
                        key={option.value}
                        onClick={() => handleFilterChange('sortOrder', option.value)}
                        className={`py-2 px-3 rounded-lg text-sm font-hana-medium transition-colors ${
                          filters.sortOrder === option.value
                            ? 'bg-hana-green text-white'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                      >
                        {option.label}
                      </button>
                    ))}
                  </div>
                </div>

                {/* 기간 선택 */}
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-3">조회 기간</label>
                  <div className="grid grid-cols-2 gap-2">
                    {periodOptions.map((option) => (
                      <button
                        key={option.value}
                        onClick={() => handleFilterChange('period', option.value)}
                        className={`py-2 px-3 rounded-lg text-sm font-hana-medium transition-colors ${
                          filters.period === option.value
                            ? 'bg-hana-green text-white'
                            : 'bg-gray-100 text-gray-700 hover:bg-gray-200'
                        }`}
                      >
                        {option.label}
                      </button>
                    ))}
                  </div>
                </div>
              </div>

              {/* 버튼들 */}
              <div className="flex gap-3 mt-8">
                <button
                  onClick={() => setShowFilterModal(false)}
                  className="flex-1 py-3 border border-gray-300 rounded-lg font-hana-medium text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  취소
                </button>
                <button
                  onClick={applyFilters}
                  className="flex-1 py-3 bg-hana-green text-white rounded-lg font-hana-medium hover:bg-green-700 transition-colors"
                >
                  적용
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </>
  );
}

export default TransactionHistory;