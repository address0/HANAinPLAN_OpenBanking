import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { fundProductApi } from '../api/fundApi';
import type { FundClassDetail } from '../types/fund.types';

const FundDetail = () => {
  const { fundCode } = useParams<{ fundCode: string }>();
  const navigate = useNavigate();
  const [fund, setFund] = useState<FundClassDetail | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (fundCode) {
      loadFundDetail(fundCode);
    }
  }, [fundCode]);

  const loadFundDetail = async (code: string) => {
    try {
      setLoading(true);
      const data = await fundProductApi.getFundClass(code);
      setFund(data);
      setError(null);
    } catch (err) {
      setError('펀드 정보를 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const formatNumber = (num: number | null | undefined) => {
    if (num === null || num === undefined) return '-';
    return num.toLocaleString('ko-KR');
  };

  const formatPercent = (bps: number | null | undefined) => {
    if (bps === null || bps === undefined) return '-';
    return `${(bps / 100).toFixed(2)}%`;
  };

  const getRiskColor = (riskGrade: string) => {
    const riskNum = parseInt(riskGrade);
    if (riskNum <= 2) return 'bg-blue-100 text-blue-800';
    if (riskNum <= 4) return 'bg-green-100 text-green-800';
    return 'bg-red-100 text-red-800';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#00857D] mx-auto"></div>
          <p className="mt-4 text-gray-600">펀드 정보를 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error || !fund) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <p className="text-red-600">{error || '펀드를 찾을 수 없습니다.'}</p>
          <button
            onClick={() => navigate('/funds')}
            className="mt-4 px-4 py-2 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]"
          >
            목록으로
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="mb-6">
        <button
          onClick={() => navigate('/funds')}
          className="text-[#00857D] hover:underline mb-4 flex items-center gap-2"
        >
          ← 펀드 목록으로
        </button>
        <div className="flex justify-between items-start">
          <div>
            <h1 className="text-3xl font-bold text-gray-900 mb-2">
              {fund.fundMaster.fundName}
            </h1>
            <div className="flex items-center gap-2">
              {fund.fundMaster.riskGrade && (
                <span className={`px-3 py-1 text-sm rounded-full ${getRiskColor(fund.fundMaster.riskGrade)}`}>
                  위험등급 {fund.fundMaster.riskGrade}
                </span>
              )}
              <span className="px-3 py-1 text-sm bg-[#00857D] text-white rounded-full">
                {fund.classCode}클래스
              </span>
              <span className="px-3 py-1 text-sm bg-pink-100 text-pink-800 rounded-full">
                {fund.fundMaster.assetType}
              </span>
            </div>
          </div>
          <button
            onClick={() => navigate(`/fund/${fund.childFundCd}/purchase`)}
            className="px-6 py-3 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66] font-semibold"
          >
            매수하기
          </button>
        </div>
      </div>

      {/* 기준가 */}
      {fund.latestNav && fund.latestNavDate && (
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-lg font-semibold mb-4">기준가 정보</h2>
          <div className="flex items-center gap-8">
            <div>
              <div className="text-sm text-gray-500 mb-1">현재 기준가</div>
              <div className="text-3xl font-bold text-gray-900">
                {formatNumber(fund.latestNav)}원
              </div>
            </div>
            <div>
              <div className="text-sm text-gray-500 mb-1">기준일</div>
              <div className="text-lg font-semibold text-gray-700">
                {new Date(fund.latestNavDate).toLocaleDateString('ko-KR')}
              </div>
            </div>
          </div>
        </div>
      )}

      {/* 기본 정보 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-lg font-semibold mb-4">기본 정보</h2>
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-gray-600">펀드코드</span>
            <span className="font-semibold">{fund.childFundCd}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-gray-600">모펀드코드</span>
            <span className="font-semibold">{fund.fundMaster.fundCd}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-gray-600">자산 유형</span>
            <span className="font-semibold">{fund.fundMaster.assetType}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-gray-600">펀드 구분</span>
            <span className="font-semibold">
              {fund.fundMaster.fundGb === 1 ? '공모' : fund.fundMaster.fundGb === 2 ? '사모' : '전문투자자'}
            </span>
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-gray-600">통화</span>
            <span className="font-semibold">{fund.fundMaster.currency}</span>
          </div>
          <div className="flex justify-between py-2 border-b border-gray-100">
            <span className="text-gray-600">판매 상태</span>
            <span className={`font-semibold ${fund.fundMaster.isActive ? 'text-green-600' : 'text-red-600'}`}>
              {fund.fundMaster.isActive ? '판매중' : '판매중단'}
            </span>
          </div>
        </div>
      </div>

      {/* 투자 규칙 */}
      {fund.rules && (
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-lg font-semibold mb-4">투자 규칙</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">최소 최초 투자금액</span>
              <span className="font-semibold text-[#00857D]">
                {formatNumber(fund.rules.minInitialAmount)}원
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">최소 추가 투자금액</span>
              <span className="font-semibold">
                {formatNumber(fund.rules.minAdditional)}원
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">매수 결제일</span>
              <span className="font-semibold">
                T+{fund.rules.buySettleDays || 0}
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">환매 결제일</span>
              <span className="font-semibold">
                T+{fund.rules.redeemSettleDays || 0}
              </span>
            </div>
            {fund.rules.redemptionFeeRate && (
              <>
                <div className="flex justify-between py-2 border-b border-gray-100">
                  <span className="text-gray-600">환매수수료율</span>
                  <span className="font-semibold text-red-600">
                    {(fund.rules.redemptionFeeRate * 100).toFixed(2)}%
                  </span>
                </div>
                <div className="flex justify-between py-2 border-b border-gray-100">
                  <span className="text-gray-600">환매수수료 적용 기간</span>
                  <span className="font-semibold">
                    {fund.rules.redemptionFeeDays}일 이내
                  </span>
                </div>
              </>
            )}
          </div>
        </div>
      )}

      {/* 수수료 정보 */}
      {fund.fees && (
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <h2 className="text-lg font-semibold mb-4">수수료 정보</h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">판매보수</span>
              <span className="font-semibold">
                {formatPercent(fund.fees.salesFeeBps)}
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">운용보수</span>
              <span className="font-semibold">
                {formatPercent(fund.fees.mgmtFeeBps)}
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">수탁보수</span>
              <span className="font-semibold">
                {formatPercent(fund.fees.trusteeFeeBps)}
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">사무관리보수</span>
              <span className="font-semibold">
                {formatPercent(fund.fees.adminFeeBps)}
              </span>
            </div>
            <div className="flex justify-between py-2 border-b border-gray-100">
              <span className="text-gray-600">총 보수비용 (TER)</span>
              <span className="font-semibold text-[#00857D]">
                {formatPercent(fund.fees.totalFeeBps)}
              </span>
            </div>
            {fund.fees.frontLoadPct && (
              <div className="flex justify-between py-2 border-b border-gray-100">
                <span className="text-gray-600">선취 판매수수료</span>
                <span className="font-semibold">
                  {(fund.fees.frontLoadPct * 100).toFixed(2)}%
                </span>
              </div>
            )}
          </div>
        </div>
      )}

      {/* 유의사항 */}
      <div className="bg-yellow-50 rounded-lg p-6 border border-yellow-200">
        <h3 className="text-lg font-semibold text-yellow-800 mb-3">투자 유의사항</h3>
        <ul className="space-y-2 text-sm text-yellow-700">
          <li>• 펀드는 원금 손실이 발생할 수 있는 실적배당형 상품입니다.</li>
          <li>• 과거의 운용실적이 미래의 수익을 보장하지 않습니다.</li>
          <li>• 환매수수료가 부과될 수 있으니 투자 기간을 고려하시기 바랍니다.</li>
          <li>• 펀드 투자 전 투자설명서 및 간이투자설명서를 반드시 확인하시기 바랍니다.</li>
        </ul>
      </div>
    </div>
  );
};

export default FundDetail;

