import { formatAmount } from '../../utils/fundUtils';

interface NextMonthWithdrawalProps {
  onProductClick?: () => void;
  isMobile?: boolean;
}

function NextMonthWithdrawal({ onProductClick, isMobile = false }: NextMonthWithdrawalProps) {
  const nextMonthData = {
    totalAmount: 1250000,
    withdrawals: [
      { name: '보험료', amount: 150000, date: '2024-11-05' },
      { name: '적금', amount: 500000, date: '2024-11-10' },
      { name: '투자상품', amount: 300000, date: '2024-11-15' },
      { name: '대출상환', amount: 300000, date: '2024-11-25' }
    ]
  };

  const handleClick = () => {
    if (isMobile && onProductClick) {
      onProductClick();
    }
  };

  return (
    <div
      className="bg-white rounded-lg p-4 h-[200px] border border-[#008485]/50 hover:border-[#008485] hover:shadow-lg transition-all grid grid-cols-5 gap-4"
      onClick={handleClick}
    >
      <div className="col-span-2 flex flex-col items-center justify-center mb-4 h-full relative border-r border-gray-200">
        <h2 className="text-lg font-hana-medium text-gray-600 absolute top-0 left-0">다음 달 출금예정액 합계</h2>
        <div className="text-3xl font-hana-bold text-center text-hana-green">
          {formatAmount(nextMonthData.totalAmount)} 원
        </div>
        <p className="text-sm font-hana-medium text-gray-600 absolute bottom-0 right-4">
          기준일: 2024-10-01
        </p>
      </div>

      {!isMobile && (
        <div className="space-y-3 col-span-3 h-full overflow-y-auto">

          {nextMonthData.withdrawals.map((item, index) => (
            <div key={index} className="flex justify-between items-center py-2 border-b border-gray-100 last:border-b-0">
              <div>
                <div className="font-hana-medium text-gray-800">{item.name}</div>
                <div className="text-sm text-gray-500">{item.date}</div>
              </div>
              <div className="text-right mr-4">
                <div className="font-hana-bold text-red-600">
                  -{formatAmount(item.amount)} 원
                </div>
              </div>
            </div>
          ))}
        </div>
      )}

      {isMobile && (
        <div className="text-sm text-gray-500 font-hana-medium text-center mt-4">
          상세 내역 보기
        </div>
      )}
    </div>
  );
}

export default NextMonthWithdrawal;