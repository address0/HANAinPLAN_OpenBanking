
interface AssetSummaryProps {
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
}

function AssetSummary({ totalAssets, totalLiabilities, netWorth }: AssetSummaryProps) {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const getChangePercentage = () => {
    return 3.2;
  };

  const getChangeAmount = () => {
    return netWorth * 0.032;
  };

  return (
    <div className="bg-white rounded-2xl shadow-lg p-8">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-hana-bold text-gray-900">통합 자산 현황</h2>
        <div className="flex items-center gap-2 text-green-600">
          <div className="w-5 h-5 bg-green-600 rounded-full"></div>
          <span className="text-sm font-hana-medium">
            +{getChangePercentage()}% (전월 대비)
          </span>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        {}
        <div className="bg-gradient-to-br from-hana-green/10 to-hana-green/20 rounded-xl p-6 border border-hana-green/30">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-hana-bold text-gray-900">총 자산</h3>
            <span className="text-sm font-hana-medium text-hana-green">+2.1% 증가</span>
          </div>
          <div className="space-y-2">
            <p className="text-3xl font-hana-bold text-gray-900">
              {formatCurrency(totalAssets)}
            </p>
          </div>
        </div>

        {}
        <div className="bg-gradient-to-br from-gray-50 to-gray-100 rounded-xl p-6 border border-gray-200">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-hana-bold text-gray-900">총 부채</h3>
            <span className="text-sm font-hana-medium text-gray-600">-1.2% 감소</span>
          </div>
          <div className="space-y-2">
            <p className="text-3xl font-hana-bold text-gray-900">
              {formatCurrency(totalLiabilities)}
            </p>
          </div>
        </div>

        {}
        <div className="bg-gradient-to-br from-hana-green/10 to-hana-green/20 rounded-xl p-6 border border-hana-green/30">
          <div className="flex items-center justify-between mb-4">
            <h3 className="text-lg font-hana-bold text-gray-900">순자산</h3>
            <span className="text-sm font-hana-medium text-hana-green">
              +{formatCurrency(getChangeAmount())}
            </span>
          </div>
          <div className="space-y-2">
            <p className="text-3xl font-hana-bold text-gray-900">
              {formatCurrency(netWorth)}
            </p>
          </div>
        </div>
      </div>

      {}
      <div className="mt-8">
        <h3 className="text-lg font-hana-bold text-gray-900 mb-4">자산 구성</h3>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <div className="text-center">
            <div className="w-16 h-16 bg-hana-green/10 rounded-full flex items-center justify-center mx-auto mb-2 border border-hana-green/30">
              <span className="text-hana-green font-hana-bold text-lg">
                {Math.round((35000000 / totalAssets) * 100)}%
              </span>
            </div>
            <p className="text-sm font-hana-medium text-gray-700">예적금</p>
            <p className="text-xs text-gray-500">₩35,000,000</p>
          </div>
          <div className="text-center">
            <div className="w-16 h-16 bg-hana-green/20 rounded-full flex items-center justify-center mx-auto mb-2 border border-hana-green/40">
              <span className="text-hana-green font-hana-bold text-lg">
                {Math.round((15000000 / totalAssets) * 100)}%
              </span>
            </div>
            <p className="text-sm font-hana-medium text-gray-700">IRP</p>
            <p className="text-xs text-gray-500">₩15,000,000</p>
          </div>
          <div className="text-center">
            <div className="w-16 h-16 bg-hana-green/30 rounded-full flex items-center justify-center mx-auto mb-2 border border-hana-green/50">
              <span className="text-hana-green font-hana-bold text-lg">
                {Math.round((20000000 / totalAssets) * 100)}%
              </span>
            </div>
            <p className="text-sm font-hana-medium text-gray-700">보험</p>
            <p className="text-xs text-gray-500">₩20,000,000</p>
          </div>
          <div className="text-center">
            <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-2 border border-gray-300">
              <span className="text-gray-600 font-hana-bold text-lg">
                {Math.round(((totalAssets - 70000000) / totalAssets) * 100)}%
              </span>
            </div>
            <p className="text-sm font-hana-medium text-gray-700">기타</p>
            <p className="text-xs text-gray-500">₩55,000,000</p>
          </div>
        </div>
      </div>
    </div>
  );
}

export default AssetSummary;