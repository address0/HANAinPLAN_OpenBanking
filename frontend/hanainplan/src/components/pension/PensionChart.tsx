import {
  LineChart,
  Line,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Legend,
  ResponsiveContainer,
  Area,
  AreaChart
} from 'recharts'

type PensionData = {
  age: number
  yearlyPension: number
  monthlyPension: number
  totalAssets: number
}

type PensionChartProps = {
  data: PensionData[]
  currentAge: number
}

function PensionChart({ data, currentAge }: PensionChartProps) {
  if (!data.length) return null

  const chartData = data.map(item => ({
    age: `${item.age}세`,
    총자산: Math.round(item.totalAssets / 10000),
    연간연금: item.yearlyPension,
    월간연금: item.monthlyPension
  }))

  const CustomTooltip = ({ active, payload, label }: any) => {
    if (active && payload && payload.length) {
      return (
        <div className="bg-white p-4 border border-gray-200 rounded-lg shadow-lg">
          <p className="font-hana-medium text-gray-900">{label}</p>
          {payload.map((entry: any, index: number) => (
            <p key={index} className="font-hana-regular text-sm" style={{ color: entry.color }}>
              {entry.name}: {entry.value.toLocaleString()} 만원
            </p>
          ))}
        </div>
      )
    }
    return null
  }

  return (
    <div className="bg-white rounded-xl shadow-lg p-6 w-full">
      <h3 className="text-xl font-hana-bold text-gray-900 mb-6">자산 여정 및 연금 수령 예상</h3>

      {}
      <div className="w-full h-80 mb-6">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={chartData} margin={{ top: 20, right: 30, left: 20, bottom: 5 }}>
            <defs>
              <linearGradient id="colorAssets" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#00857A" stopOpacity={0.3}/>
                <stop offset="95%" stopColor="#00857A" stopOpacity={0.1}/>
              </linearGradient>
              <linearGradient id="colorPension" x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor="#E8F5F4" stopOpacity={0.8}/>
                <stop offset="95%" stopColor="#E8F5F4" stopOpacity={0.3}/>
              </linearGradient>
            </defs>
            <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
            <XAxis
              dataKey="age"
              tick={{ fontSize: 12, fontFamily: 'Hana2-Regular' }}
              stroke="#666"
            />
            <YAxis
              yAxisId="left"
              tick={{ fontSize: 12, fontFamily: 'Hana2-Regular' }}
              stroke="#666"
              label={{ value: '총자산 (만원)', angle: -90, position: 'insideLeft' }}
            />
            <YAxis
              yAxisId="right"
              orientation="right"
              tick={{ fontSize: 12, fontFamily: 'Hana2-Regular' }}
              stroke="#666"
              label={{ value: '연간연금 (만원)', angle: 90, position: 'insideRight' }}
            />
            <Tooltip content={<CustomTooltip />} />
            <Legend />
            <Area
              yAxisId="left"
              type="monotone"
              dataKey="총자산"
              stroke="#00857A"
              strokeWidth={3}
              fillOpacity={1}
              fill="url(#colorAssets)"
            />
            <Line
              yAxisId="right"
              type="monotone"
              dataKey="연간연금"
              stroke="#008485"
              strokeWidth={2}
              dot={{ fill: '#008485', strokeWidth: 2, r: 4 }}
              activeDot={{ r: 6, stroke: '#008485', strokeWidth: 2 }}
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>

      {}
      <div className="overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-gray-200">
              <th className="text-left py-3 font-hana-medium text-gray-700">나이</th>
              <th className="text-right py-3 font-hana-medium text-gray-700">연간 연금</th>
              <th className="text-right py-3 font-hana-medium text-gray-700">월간 연금</th>
              <th className="text-right py-3 font-hana-medium text-gray-700">총 자산</th>
            </tr>
          </thead>
          <tbody>
            {data.map((item, index) => (
              <tr key={index} className={`border-b border-gray-100 ${item.age === currentAge ? 'bg-hana-light-green' : ''}`}>
                <td className="py-3 font-hana-regular">{item.age}세</td>
                <td className="text-right py-3 font-hana-regular">
                  {item.yearlyPension.toLocaleString()}만원
                </td>
                <td className="text-right py-3 font-hana-regular">
                  {item.monthlyPension.toLocaleString()}만원
                </td>
                <td className="text-right py-3 font-hana-regular">
                  {(item.totalAssets / 10000).toFixed(1)}만원
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default PensionChart