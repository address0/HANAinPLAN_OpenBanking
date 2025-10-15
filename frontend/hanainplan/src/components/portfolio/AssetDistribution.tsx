import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';

interface AssetDistributionProps {
  totalAssets: number;
  savings: {
    general: number;
    irp: number;
  };
  insurance: {
    total: number;
    monthly: number;
  };
  expenses: {
    monthly: number;
    categories: {
      living: number;
      medical: number;
      entertainment: number;
      others: number;
    };
  };
}

function AssetDistribution({
  totalAssets,
  savings,
  insurance,
  expenses
}: AssetDistributionProps) {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  const assetData = [
    {
      name: '일반 예적금',
      value: savings.general,
      percentage: Math.round((savings.general / totalAssets) * 100),
      color: '#00857A',
    },
    {
      name: 'IRP',
      value: savings.irp,
      percentage: Math.round((savings.irp / totalAssets) * 100),
      color: '#006D6E',
    },
    {
      name: '보험',
      value: insurance.total,
      percentage: Math.round((insurance.total / totalAssets) * 100),
      color: '#00A8A8',
    },
    {
      name: '기타 자산',
      value: totalAssets - savings.general - savings.irp - insurance.total,
      percentage: Math.round(((totalAssets - savings.general - savings.irp - insurance.total) / totalAssets) * 100),
      color: '#6B7280',
    },
  ];

  const expenseData = [
    {
      name: '생활비',
      value: expenses.categories.living,
      percentage: Math.round((expenses.categories.living / expenses.monthly) * 100),
      color: '#00857A',
    },
    {
      name: '의료비',
      value: expenses.categories.medical,
      percentage: Math.round((expenses.categories.medical / expenses.monthly) * 100),
      color: '#006D6E',
    },
    {
      name: '문화생활',
      value: expenses.categories.entertainment,
      percentage: Math.round((expenses.categories.entertainment / expenses.monthly) * 100),
      color: '#00A8A8',
    },
    {
      name: '기타',
      value: expenses.categories.others,
      percentage: Math.round((expenses.categories.others / expenses.monthly) * 100),
      color: '#6B7280',
    },
  ];

  const CustomTooltip = ({ active, payload }: any) => {
    if (active && payload && payload.length) {
      const data = payload[0].payload;
      return (
        <div className="bg-white p-4 rounded-lg shadow-lg border border-gray-200">
          <p className="font-hana-bold text-gray-900">{data.name}</p>
          <p className="text-sm text-gray-600">
            금액: {formatCurrency(data.value)}
          </p>
          <p className="text-sm text-gray-600">
            비율: {data.percentage}%
          </p>
        </div>
      );
    }
    return null;
  };

  const renderLegend = (props: any) => {
    const { payload } = props;
    return (
      <div className="flex flex-wrap justify-center gap-4 mt-4">
        {payload.map((entry: any, index: number) => (
          <div key={index} className="flex items-center gap-2">
            <div
              className="w-3 h-3 rounded-full"
              style={{ backgroundColor: entry.color }}
            ></div>
            <span className="text-sm font-hana-medium text-gray-700">
              {entry.value} ({entry.payload.percentage}%)
            </span>
          </div>
        ))}
      </div>
    );
  };

  return (
    <div className="bg-white rounded-2xl shadow-lg p-8">
      <div className="mb-8">
        <h2 className="text-2xl font-hana-bold text-gray-900">자산 및 지출 분포</h2>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-12">
        {}
        <div className="text-center">
          <div className="mb-6">
            <h3 className="text-xl font-hana-bold text-gray-900">자산 분포</h3>
          </div>

          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={assetData}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, percentage }) => `${name}: ${percentage}%`}
                  labelLine={false}
                >
                  {assetData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend content={renderLegend} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          {}
          <div className="mt-6 p-4 bg-blue-50 rounded-xl">
            <h4 className="font-hana-bold text-gray-900 mb-3">자산 분포 요약</h4>
            <div className="grid grid-cols-2 gap-3 text-sm">
              {assetData.map((item, index) => (
                <div key={index} className="flex justify-between">
                  <span className="text-gray-600">{item.name}:</span>
                  <span className="font-hana-medium">{item.percentage}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>

        {}
        <div className="text-center">
          <div className="mb-6">
            <h3 className="text-xl font-hana-bold text-gray-900">월간 지출 분포</h3>
          </div>

          <div className="h-80 w-full">
            <ResponsiveContainer width="100%" height="100%">
              <PieChart>
                <Pie
                  data={expenseData}
                  cx="50%"
                  cy="50%"
                  outerRadius={100}
                  fill="#8884d8"
                  dataKey="value"
                  label={({ name, percentage }) => `${name}: ${percentage}%`}
                  labelLine={false}
                >
                  {expenseData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={entry.color} />
                  ))}
                </Pie>
                <Tooltip content={<CustomTooltip />} />
                <Legend content={renderLegend} />
              </PieChart>
            </ResponsiveContainer>
          </div>

          {}
          <div className="mt-6 p-4 bg-red-50 rounded-xl">
            <h4 className="font-hana-bold text-gray-900 mb-3">월간 지출 요약</h4>
            <div className="mb-3">
              <div className="flex justify-between items-center">
                <span className="text-gray-600">총 월간 지출:</span>
                <span className="font-hana-bold text-lg">{formatCurrency(expenses.monthly)}</span>
              </div>
            </div>
            <div className="grid grid-cols-2 gap-3 text-sm">
              {expenseData.map((item, index) => (
                <div key={index} className="flex justify-between">
                  <span className="text-gray-600">{item.name}:</span>
                  <span className="font-hana-medium">{item.percentage}%</span>
                </div>
              ))}
            </div>
          </div>
        </div>
      </div>

    </div>
  );
}

export default AssetDistribution;