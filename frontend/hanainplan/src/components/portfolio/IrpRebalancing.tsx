import { useState, useEffect } from 'react';
import { PieChart, Pie, Cell, ResponsiveContainer, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend } from 'recharts';
import { 
  simulateRebalancing, 
  simulateRecommendedRebalancing,
  simulateCustomRebalancing,
  approveRebalancing, 
  getRebalancingStatus,
  getSimilarUserPortfolio,
  type RebalancingSimulationResponse,
  type PortfolioSnapshot,
  type RebalancingOrder,
  type PortfolioRecommendationResponse
} from '../../api/rebalancingApi';

interface IrpRebalancingProps {
  customerId: number;
  irpAccountNumber: string;
}

const COLORS = {
  cash: '#10B981',      // green
  deposit: '#3B82F6',   // blue  
  fund: '#8B5CF6'       // purple
};

const COLORS_ARRAY = [COLORS.cash, COLORS.deposit, COLORS.fund];

export default function IrpRebalancing({ customerId, irpAccountNumber }: IrpRebalancingProps) {
  const [simulation, setSimulation] = useState<RebalancingSimulationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isExecuting, setIsExecuting] = useState(false);
  const [recommendation, setRecommendation] = useState<PortfolioRecommendationResponse | null>(null);
  const [customWeights, setCustomWeights] = useState({
    cashWeight: 5,
    depositWeight: 40,
    fundWeight: 55
  });

  // 추천 포트폴리오 조회
  useEffect(() => {
    const fetchRecommendation = async () => {
      try {
        const recommendationData = await getSimilarUserPortfolio(customerId);
        setRecommendation(recommendationData);
        
        // 추천 포트폴리오로 커스텀 가중치 초기화 (안전한 접근)
        if (recommendationData?.recommendedPortfolio) {
          setCustomWeights({
            cashWeight: recommendationData.recommendedPortfolio.cashWeight || 5,
            depositWeight: recommendationData.recommendedPortfolio.depositWeight || 40,
            fundWeight: recommendationData.recommendedPortfolio.fundWeight || 55
          });
        }
      } catch (error) {
        console.error('추천 포트폴리오 조회 실패:', error);
        // 기본값으로 설정
        setCustomWeights({
          cashWeight: 5,
          depositWeight: 40,
          fundWeight: 55
        });
      }
    };

    fetchRecommendation();
  }, [customerId]);

  // 리밸런싱 시뮬레이션 실행
  const handleSimulateRebalancing = async (type: 'recommended' | 'custom') => {
    setIsLoading(true);
    // 이전 시뮬레이션 결과 초기화
    setSimulation(null);
    
    try {
      let response: RebalancingSimulationResponse;
      
      if (type === 'recommended') {
        response = await simulateRecommendedRebalancing(customerId);
      } else {
        response = await simulateCustomRebalancing(
          customerId,
          customWeights.cashWeight,
          customWeights.depositWeight,
          customWeights.fundWeight
        );
      }
      
      setSimulation(response);
    } catch (error) {
      console.error('리밸런싱 시뮬레이션 실패:', error);
      alert('리밸런싱 시뮬레이션에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  // 리밸런싱 실행 승인
  const handleApproveRebalancing = async () => {
    if (!simulation?.jobId) return;
    
    setIsExecuting(true);
    try {
      const response = await approveRebalancing(simulation.jobId);
      setSimulation(response);
      alert('리밸런싱이 성공적으로 실행되었습니다.');
    } catch (error) {
      console.error('리밸런싱 실행 실패:', error);
      alert('리밸런싱 실행에 실패했습니다.');
    } finally {
      setIsExecuting(false);
    }
  };

  // 포트폴리오 차트 데이터 변환
  const getPortfolioChartData = (portfolio: PortfolioSnapshot | null) => {
    if (!portfolio) {
      return [
        { name: '현금', value: 0, amount: 0, timestamp: Date.now() },
        { name: '예금', value: 0, amount: 0, timestamp: Date.now() },
        { name: '펀드', value: 0, amount: 0, timestamp: Date.now() }
      ];
    }
    // 새로운 객체를 생성하여 참조 변경으로 리렌더링 강제
    const timestamp = Date.now();
    return [
      { name: '현금', value: portfolio.cashWeight || 0, amount: portfolio.cashAmount || 0, timestamp },
      { name: '예금', value: portfolio.depositWeight || 0, amount: portfolio.depositAmount || 0, timestamp },
      { name: '펀드', value: portfolio.fundWeight || 0, amount: portfolio.fundAmount || 0, timestamp }
    ];
  };

  // 리밸런싱 주문 차트 데이터
  const getOrdersChartData = (orders: RebalancingOrder[] | null | undefined) => {
    if (!orders || orders.length === 0) {
      return [
        { name: '매수', count: 0, amount: 0, timestamp: Date.now() },
        { name: '매도', count: 0, amount: 0, timestamp: Date.now() }
      ];
    }
    
    const buyOrders = orders.filter(order => order.orderType === 'BUY');
    const sellOrders = orders.filter(order => order.orderType === 'SELL');
    const timestamp = Date.now();
    
    return [
      { name: '매수', count: buyOrders.length, amount: buyOrders.reduce((sum, order) => sum + (order.orderAmount || 0), 0), timestamp },
      { name: '매도', count: sellOrders.length, amount: sellOrders.reduce((sum, order) => sum + (order.orderAmount || 0), 0), timestamp }
    ];
  };

  // 사용자 지정 포트폴리오 평가
  const evaluateCustomPortfolio = (customWeights: {cashWeight: number, depositWeight: number, fundWeight: number}) => {
    if (!recommendation) return null;

    const recommended = recommendation.recommendedPortfolio;
    const totalDeviation = Math.abs(customWeights.cashWeight - recommended.cashWeight) +
                          Math.abs(customWeights.depositWeight - recommended.depositWeight) +
                          Math.abs(customWeights.fundWeight - recommended.fundWeight);

    let riskLevel = '적정';
    let riskDescription = '추천 포트폴리오와 유사한 수준입니다.';
    let colorClass = 'text-green-600';

    if (totalDeviation > 30) {
      riskLevel = '높음';
      riskDescription = '추천 포트폴리오와 큰 차이가 있습니다. 리스크를 고려해보세요.';
      colorClass = 'text-red-600';
    } else if (totalDeviation > 15) {
      riskLevel = '보통';
      riskDescription = '추천 포트폴리오와 어느 정도 차이가 있습니다.';
      colorClass = 'text-yellow-600';
    }

    // 펀드 비중 체크
    if (customWeights.fundWeight > 70) {
      riskLevel = '높음';
      riskDescription = '펀드 비중이 70%를 초과합니다. IRP 규정을 확인해주세요.';
      colorClass = 'text-red-600';
    }

    return {
      riskLevel,
      riskDescription,
      colorClass,
      totalDeviation,
      fundWeightExceeded: customWeights.fundWeight > 70
    };
  };

  return (
    <div className="bg-white rounded-2xl shadow-lg p-8">
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-2xl font-hana-bold text-gray-900">IRP 포트폴리오 리밸런싱</h2>
        <div className="flex items-center">
          <img src="/bank/081.png" alt="하나은행" className="w-8 h-8 mr-2" />
          <span className="text-lg font-hana-medium text-gray-800">하나은행</span>
        </div>
      </div>

      {/* 추천 포트폴리오 섹션 */}
      {recommendation && (
        <div className="mb-8">
          <div className="bg-gradient-to-r from-blue-50 to-indigo-50 rounded-xl p-6 border border-blue-200">
            <h3 className="text-lg font-hana-bold text-blue-900 mb-4">추천 포트폴리오 비중</h3>
            
            {/* 리스크 프로필 정보 */}
            <div className="mb-4 p-3 bg-blue-100 rounded-lg">
              <div className="flex items-center justify-between">
                <div>
                  <div className="text-sm font-medium text-blue-800">리스크 프로필</div>
                  <div className="text-lg font-hana-bold text-blue-900">{recommendation.riskProfileType}</div>
                </div>
                <div className="text-right">
                  <div className="text-sm text-blue-700">{recommendation.riskProfileDescription}</div>
                </div>
              </div>
            </div>

            {/* 추천 포트폴리오 비중 */}
            <div className="grid grid-cols-3 gap-4 mb-4">
              <div className="text-center">
                <div className="text-2xl font-hana-bold text-blue-600">{(recommendation.recommendedPortfolio?.cashWeight || 0).toFixed(1)}%</div>
                <div className="text-sm text-blue-700">현금</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-hana-bold text-blue-600">{(recommendation.recommendedPortfolio?.depositWeight || 0).toFixed(1)}%</div>
                <div className="text-sm text-blue-700">예금</div>
              </div>
              <div className="text-center">
                <div className="text-2xl font-hana-bold text-blue-600">{(recommendation.recommendedPortfolio?.fundWeight || 0).toFixed(1)}%</div>
                <div className="text-sm text-blue-700">펀드</div>
              </div>
            </div>

            {/* 추천 설명 */}
            <div className="mb-4 p-3 bg-blue-50 rounded-lg">
              <div className="text-sm text-blue-800">
                <div className="font-medium mb-1">추천 근거:</div>
                <div>{recommendation.recommendedPortfolio?.description || '추천 근거를 불러오는 중입니다.'}</div>
              </div>
            </div>

            {/* 메타데이터 정보 */}
            <div className="mb-4 text-xs text-blue-600">
              <div>분석된 사용자: {recommendation.metadata?.totalUsersAnalyzed || 0}명</div>
              <div>유사 사용자: {recommendation.metadata?.similarUsersFound || 0}명</div>
              <div>생성 시간: {recommendation.metadata?.generatedAt ? new Date(recommendation.metadata.generatedAt).toLocaleString() : '정보 없음'}</div>
            </div>

            <button
              onClick={() => handleSimulateRebalancing('recommended')}
              disabled={isLoading}
              className="mt-4 w-full bg-blue-600 text-white py-2 px-4 rounded-lg font-hana-medium hover:bg-blue-700 disabled:opacity-50"
            >
              {isLoading ? '시뮬레이션 중...' : '추천 포트폴리오로 시뮬레이션'}
            </button>
          </div>
        </div>
      )}

      {/* 사용자 지정 비중 섹션 */}
      <div className="mb-8">
        <div className="bg-gradient-to-r from-green-50 to-emerald-50 rounded-xl p-6 border border-green-200">
          <h3 className="text-lg font-hana-bold text-green-900 mb-4">사용자 지정 포트폴리오 비중</h3>
          <div className="grid grid-cols-3 gap-4 mb-4">
            <div>
              <label className="block text-sm font-medium text-green-700 mb-2">현금 비중 (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                value={customWeights.cashWeight}
                onChange={(e) => setCustomWeights(prev => ({ ...prev, cashWeight: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-green-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-green-700 mb-2">예금 비중 (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                value={customWeights.depositWeight}
                onChange={(e) => setCustomWeights(prev => ({ ...prev, depositWeight: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-green-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
              />
            </div>
            <div>
              <label className="block text-sm font-medium text-green-700 mb-2">펀드 비중 (%)</label>
              <input
                type="number"
                min="0"
                max="100"
                value={customWeights.fundWeight}
                onChange={(e) => setCustomWeights(prev => ({ ...prev, fundWeight: Number(e.target.value) }))}
                className="w-full px-3 py-2 border border-green-300 rounded-lg focus:ring-2 focus:ring-green-500 focus:border-transparent"
              />
            </div>
          </div>
          <div className="text-sm text-green-700 mb-4">
            총 비중: {(customWeights.cashWeight + customWeights.depositWeight + customWeights.fundWeight).toFixed(1)}%
          </div>

          {/* 사용자 지정 포트폴리오 평가 */}
          {evaluateCustomPortfolio(customWeights) && (
            <div className="mb-4 p-3 bg-green-50 rounded-lg border border-green-200">
              <div className="flex items-center justify-between mb-2">
                <div className="text-sm font-medium text-green-800">포트폴리오 평가</div>
                <div className={`text-sm font-hana-bold ${evaluateCustomPortfolio(customWeights)?.colorClass}`}>
                  {evaluateCustomPortfolio(customWeights)?.riskLevel}
                </div>
              </div>
              <div className="text-sm text-green-700 mb-2">
                {evaluateCustomPortfolio(customWeights)?.riskDescription}
              </div>
              {evaluateCustomPortfolio(customWeights)?.fundWeightExceeded && (
                <div className="text-sm text-red-600 font-medium">
                  ⚠️ 펀드 비중이 IRP 규정(70%)을 초과합니다.
                </div>
              )}
              <div className="text-xs text-green-600 mt-2">
                추천 대비 편차: {evaluateCustomPortfolio(customWeights)?.totalDeviation.toFixed(1)}%p
              </div>
            </div>
          )}

          <button
            onClick={() => handleSimulateRebalancing('custom')}
            disabled={isLoading || Math.abs(customWeights.cashWeight + customWeights.depositWeight + customWeights.fundWeight - 100) > 0.1}
            className="w-full bg-green-600 text-white py-2 px-4 rounded-lg font-hana-medium hover:bg-green-700 disabled:opacity-50"
          >
            {isLoading ? '시뮬레이션 중...' : '사용자 지정 비중으로 시뮬레이션'}
          </button>
        </div>
      </div>

      {/* 시뮬레이션 결과 */}
      {simulation && (
        <div className="space-y-6">
          <div className="bg-gray-50 rounded-xl p-6">
            <h3 className="text-lg font-hana-bold text-gray-900 mb-4">리밸런싱 시뮬레이션 결과</h3>
            
            {/* 포트폴리오 비교 차트 */}
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6 mb-6">
              <div>
                <h4 className="text-md font-hana-medium text-gray-700 mb-3">현재 포트폴리오</h4>
                <ResponsiveContainer width="100%" height={200} key={`current-${simulation?.jobId || 'empty'}`}>
                  <PieChart>
                    <Pie
                      data={getPortfolioChartData(simulation?.currentPortfolio)}
                      cx="50%"
                      cy="50%"
                      innerRadius={40}
                      outerRadius={80}
                      dataKey="value"
                    >
                      {getPortfolioChartData(simulation?.currentPortfolio).map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS_ARRAY[index]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value, name) => [`${value}%`, name]} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              
              <div>
                <h4 className="text-md font-hana-medium text-gray-700 mb-3">목표 포트폴리오</h4>
                <ResponsiveContainer width="100%" height={200} key={`target-${simulation?.jobId || 'empty'}`}>
                  <PieChart>
                    <Pie
                      data={getPortfolioChartData(simulation?.targetPortfolio)}
                      cx="50%"
                      cy="50%"
                      innerRadius={40}
                      outerRadius={80}
                      dataKey="value"
                    >
                      {getPortfolioChartData(simulation?.targetPortfolio).map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS_ARRAY[index]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value, name) => [`${value}%`, name]} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
              
              <div>
                <h4 className="text-md font-hana-medium text-gray-700 mb-3">예상 포트폴리오</h4>
                <ResponsiveContainer width="100%" height={200} key={`expected-${simulation?.jobId || 'empty'}`}>
                  <PieChart>
                    <Pie
                      data={getPortfolioChartData(simulation?.expectedPortfolio)}
                      cx="50%"
                      cy="50%"
                      innerRadius={40}
                      outerRadius={80}
                      dataKey="value"
                    >
                      {getPortfolioChartData(simulation?.expectedPortfolio).map((entry, index) => (
                        <Cell key={`cell-${index}`} fill={COLORS_ARRAY[index]} />
                      ))}
                    </Pie>
                    <Tooltip formatter={(value, name) => [`${value}%`, name]} />
                  </PieChart>
                </ResponsiveContainer>
              </div>
            </div>

            {/* 리밸런싱 주문 정보 */}
            {simulation?.orders && simulation.orders.length > 0 && (
              <div className="mb-6">
                <h4 className="text-md font-hana-medium text-gray-700 mb-3">리밸런싱 주문</h4>
                <ResponsiveContainer width="100%" height={200} key={`orders-${simulation?.jobId || 'empty'}`}>
                  <BarChart data={getOrdersChartData(simulation?.orders)}>
                    <CartesianGrid strokeDasharray="3 3" />
                    <XAxis dataKey="name" />
                    <YAxis />
                    <Tooltip formatter={(value, name) => [name === 'count' ? `${value}건` : `${value.toLocaleString()}원`, name === 'count' ? '주문 수' : '주문 금액']} />
                    <Legend />
                    <Bar dataKey="count" fill="#3B82F6" name="주문 수" />
                    <Bar dataKey="amount" fill="#10B981" name="주문 금액" />
                  </BarChart>
                </ResponsiveContainer>
                
                <div className="mt-4 space-y-2">
                  {simulation?.orders?.map((order, index) => (
                    <div key={index} className="flex justify-between items-center p-3 bg-white rounded-lg border">
                      <div className="flex items-center">
                        <span className={`px-2 py-1 rounded text-xs font-medium ${
                          order.orderType === 'BUY' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                        }`}>
                          {order.orderType === 'BUY' ? '매수' : '매도'}
                        </span>
                        <span className="ml-3 font-medium">{order.fundName}</span>
                      </div>
                      <div className="text-right">
                        <div className="font-medium">{order.orderAmount.toLocaleString()}원</div>
                        <div className="text-sm text-gray-500">{order.orderUnits.toFixed(2)}좌수</div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            )}

            {/* 수수료 및 총 주문 금액 */}
            <div className="grid grid-cols-2 gap-4 mb-6">
              <div className="bg-yellow-50 rounded-lg p-4 border border-yellow-200">
                <div className="text-sm text-yellow-700 mb-1">예상 수수료</div>
                <div className="text-xl font-hana-bold text-yellow-800">{(simulation?.totalFee || 0).toLocaleString()}원</div>
              </div>
              <div className="bg-purple-50 rounded-lg p-4 border border-purple-200">
                <div className="text-sm text-purple-700 mb-1">총 주문 금액</div>
                <div className="text-xl font-hana-bold text-purple-800">{(simulation?.totalOrderAmount || 0).toLocaleString()}원</div>
              </div>
            </div>

            {/* 실행 버튼 */}
            {simulation?.status === 'PENDING' && (
              <div className="flex gap-4">
                <button
                  onClick={handleApproveRebalancing}
                  disabled={isExecuting}
                  className="flex-1 bg-hana-green text-white py-3 px-6 rounded-lg font-hana-bold hover:bg-green-700 disabled:opacity-50"
                >
                  {isExecuting ? '리밸런싱 실행 중...' : '리밸런싱 실행'}
                </button>
                <button
                  onClick={() => setSimulation(null)}
                  className="px-6 py-3 border border-gray-300 text-gray-700 rounded-lg font-hana-medium hover:bg-gray-50"
                >
                  취소
                </button>
              </div>
            )}

            {simulation?.status === 'COMPLETED' && (
              <div className="bg-green-50 rounded-lg p-4 border border-green-200">
                <div className="text-green-800 font-hana-medium">리밸런싱이 성공적으로 완료되었습니다.</div>
              </div>
            )}
          </div>
        </div>
      )}
    </div>
  );
}