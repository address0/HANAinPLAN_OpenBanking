import { useState } from 'react';
import Calendar from 'react-calendar';
import '../../css/calendar.css';
import productStore from '../../store/productStore';
import { formatAmount } from '../../utils/fundUtils';

type ValuePiece = Date | null;
type Value = ValuePiece | [ValuePiece, ValuePiece];

interface ProductTransaction {
  productName: string;
  productType: string;
  monthlyAmount: number;
  totalAmount: number;
  nextPaymentDate: string;
  status: 'active' | 'pending' | 'completed';
}

function ProductTransactions() {
  const [selectedPeriod, setSelectedPeriod] = useState('monthly');
  const [viewMode, setViewMode] = useState<'list' | 'calendar'>('list');
  const [selectedDate, setSelectedDate] = useState<Value>(new Date());
  const { categories } = productStore();

  const productData: ProductTransaction[] = [
    {
      productName: '하나생명 종신보험',
      productType: '종신',
      monthlyAmount: 150000,
      totalAmount: 1800000,
      nextPaymentDate: '2025-09-05',
      status: 'active'
    },
    {
      productName: '하나 정기적금',
      productType: '정기',
      monthlyAmount: 500000,
      totalAmount: 6000000,
      nextPaymentDate: '2025-09-10',
      status: 'active'
    },
    {
      productName: '하나연금보험',
      productType: '자산',
      monthlyAmount: 300000,
      totalAmount: 3600000,
      nextPaymentDate: '2025-09-15',
      status: 'active'
    },
    {
      productName: '하나생명 암보험',
      productType: '의료',
      monthlyAmount: 300000,
      totalAmount: 3600000,
      nextPaymentDate: '2025-09-25',
      status: 'active'
    }
  ];

  const calendarTransactions = [
    { date: '2025-09-05', productName: '하나생명 종신보험', productType: '종신', amount: -150000, type: 'withdrawal' },
    { date: '2025-09-10', productName: '하나 정기적금', productType: '정기', amount: -500000, type: 'withdrawal' },
    { date: '2025-09-15', productName: '하나연금보험', productType: '자산', amount: -300000, type: 'withdrawal' },
    { date: '2025-09-25', productName: '하나생명 암보험', productType: '의료', amount: -300000, type: 'withdrawal' },
    { date: '2025-08-05', productName: '하나생명 종신보험', productType: '종신', amount: -150000, type: 'withdrawal' },
    { date: '2025-08-10', productName: '하나 정기적금', productType: '정기', amount: -500000, type: 'withdrawal' },
    { date: '2025-08-15', productName: '하나연금보험', productType: '자산', amount: -300000, type: 'withdrawal' },
    { date: '2025-08-25', productName: '하나생명 암보험', productType: '의료', amount: -300000, type: 'withdrawal' },
  ];

  const getTransactionsByDate = (date: Date) => {
    const dateStr = date.toISOString().split('T')[0];
    return calendarTransactions.filter(t => t.date === dateStr);
  };

  const tileContent = ({ date, view }: { date: Date; view: string }) => {
    if (view === 'month') {
      const dayTransactions = getTransactionsByDate(date);
      if (dayTransactions.length > 0) {
        return (
          <div className="flex flex-col items-center mt-1 space-y-1 w-full">
            {dayTransactions.slice(0, 2).map((t, index) => (
              <div key={index} className="flex items-center gap-1 w-full">
                <span className="text-xs px-1 py-0.5 rounded overflow-hidden text-ellipsis whitespace-nowrap font-hana-medium"
                  style={{
                    backgroundColor: categories.find(c => c.name === t.productType)?.color + '20',
                    color: categories.find(c => c.name === t.productType)?.color }}
                >
                  {t.productName}
                </span>
              </div>
            ))}
            {dayTransactions.length > 2 && (
              <div className="text-xs text-gray-500">+{dayTransactions.length - 2}개</div>
            )}
          </div>
        );
      }
    }
    return null;
  };

  const getProductTypeColor = (type: string) => {
    switch (type) {
      case '보험': return 'bg-blue-100 text-blue-800';
      case '적금': return 'bg-green-100 text-green-800';
      case '투자': return 'bg-purple-100 text-purple-800';
      case '대출': return 'bg-red-100 text-red-800';
      default: return 'bg-gray-100 text-gray-800';
    }
  };

  return (
    <div className="bg-white rounded-lg p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-hana-bold text-gray-800">가입상품 별 거래금액</h2>
        <div className="flex gap-2">
        {viewMode === 'list' && (
            <select
              value={selectedPeriod}
              onChange={(e) => setSelectedPeriod(e.target.value)}
              className="px-3 py-2 border border-gray-300 rounded-lg font-hana-medium text-sm focus:outline-none focus:ring-2 focus:ring-hana-green"
            >
              <option value="monthly">월별</option>
              <option value="yearly">연별</option>
            </select>
          )}
          <button
            onClick={() => setViewMode('list')}
            className={`px-4 py-2 rounded-lg font-hana-medium transition-colors ${
              viewMode === 'list'
                ? 'bg-hana-green text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            목록으로 보기
          </button>
          <button
            onClick={() => setViewMode('calendar')}
            className={`px-4 py-2 rounded-lg font-hana-medium transition-colors ${
              viewMode === 'calendar'
                ? 'bg-hana-green text-white'
                : 'bg-gray-100 text-gray-600 hover:bg-gray-200'
            }`}
          >
            달력으로 보기
          </button>
        </div>
      </div>

      {viewMode === 'list' ? (
        <div className="space-y-4">

          {productData.map((product, index) => (
          <div key={index} className="border border-gray-200 rounded-lg p-4 hover:shadow-md transition-shadow">
            <div className="flex justify-between items-start mb-3">
              <div className="flex items-center gap-2 mb-2">
                <h3 className="font-hana-bold text-gray-800">{product.productName}</h3>
                <span className={`px-2 py-1 rounded-full text-xs font-hana-medium ${getProductTypeColor(product.productType)}`}>
                  {product.productType}
                </span>
              </div>
              <p className="text-sm text-gray-600 font-hana-regular">
                다음 출금일: {product.nextPaymentDate}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div className="bg-gray-50 p-3 rounded-lg">
                <div className="text-sm text-gray-600 font-hana-medium mb-1">월 납입금액</div>
                <div className="text-lg font-hana-bold text-hana-green">
                  {formatAmount(product.monthlyAmount)} 원
                </div>
              </div>
              <div className="bg-gray-50 p-3 rounded-lg">
                <div className="text-sm text-gray-600 font-hana-medium mb-1">
                  {selectedPeriod === 'monthly' ? '연간 예상금액' : '총 납입금액'}
                </div>
                <div className="text-lg font-hana-bold text-gray-800">
                  {formatAmount(product.totalAmount)} 원
                </div>
              </div>
            </div>

            {}
            <div className="mt-3">
              <div className="flex justify-between text-xs text-gray-500 mb-1">
                <span>진행률</span>
                <span>75%</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div className="bg-hana-green h-2 rounded-full" style={{ width: '75%' }}></div>
              </div>
            </div>
          </div>
        ))}

        <div className="mt-6 pt-4 border-t">
          <div className="flex justify-between items-center">
            <span className="font-hana-bold text-gray-800">총 월 납입금액</span>
            <span className="text-xl font-hana-bold text-hana-green">
              {formatAmount(productData.reduce((sum, product) => sum + product.monthlyAmount, 0))} 원
            </span>
          </div>
        </div>
      </div>
      ) : (
        <div className="calendar-container">

          <Calendar
            onChange={setSelectedDate}
            value={selectedDate}
            tileContent={tileContent}
            formatDay={(locale, date) => date.toLocaleString("en", {day: "numeric"})}
            calendarType="gregory"
          />

          {selectedDate instanceof Date && (
            <div className="mt-6">
              <h3 className="font-hana-bold text-gray-800 mb-3">
                {selectedDate.toLocaleDateString('ko-KR')} 내역 목록
              </h3>
              <div className="space-y-2">
                {getTransactionsByDate(selectedDate).map((transaction, index) => (
                  <div key={index} className="flex flex-col py-3 px-4 border border-gray-200 rounded-lg">
                    <div className="flex items-center gap-3">
                      <span className="px-2 py-1 rounded-md text-xs font-hana-medium text-white"
                        style={{ backgroundColor: categories.find(c => c.name === transaction.productType)?.color + '20',
                          color: categories.find(c => c.name === transaction.productType)?.color }}
                      >
                        {transaction.productType}
                      </span>
                      <div className="font-hana-medium text-gray-800 text-lg">{transaction.productName}</div>
                    </div>
                    <div className="text-sm text-gray-500">출금일: {transaction.date}</div>
                  </div>
                ))}
                {getTransactionsByDate(selectedDate).length === 0 && (
                  <div className="text-gray-500 text-center py-8">해당 날짜에 상품 거래내역이 없습니다.</div>
                )}
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
}

export default ProductTransactions;