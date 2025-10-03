import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fundProductApi } from '../api/fundApi';
import type { FundClassDetail } from '../types/fund.types';

const FundList = () => {
  const navigate = useNavigate();
  const [fundClasses, setFundClasses] = useState<FundClassDetail[]>([]);
  const [filteredFunds, setFilteredFunds] = useState<FundClassDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  // 필터 상태
  const [selectedAssetType, setSelectedAssetType] = useState<string>('ALL');
  const [selectedClassCode, setSelectedClassCode] = useState<string>('ALL');
  const [maxAmount, setMaxAmount] = useState<string>('');

  // 자산 유형 목록
  const assetTypes = [
    { value: 'ALL', label: '전체' },
    { value: 'MIXED', label: '혼합형' },
    { value: 'BOND', label: '채권형' },
    { value: 'EQUITY', label: '주식형' },
  ];

  // 클래스 코드 목록
  const classCodes = [
    { value: 'ALL', label: '전체' },
    { value: 'P', label: 'P클래스 (퇴직연금)' },
    { value: 'C', label: 'C클래스 (일반)' },
    { value: 'A', label: 'A클래스' },
  ];

  // 데이터 로드
  useEffect(() => {
    loadFundClasses();
  }, []);

  // 필터 적용
  useEffect(() => {
    applyFilters();
  }, [fundClasses, selectedAssetType, selectedClassCode, maxAmount]);

  const loadFundClasses = async () => {
    try {
      setLoading(true);
      const data = await fundProductApi.getAllFundClasses();
      setFundClasses(data);
      setError(null);
    } catch (err) {
      setError('펀드 목록을 불러오는데 실패했습니다.');
      console.error(err);
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...fundClasses];

    // 자산 유형 필터
    if (selectedAssetType !== 'ALL') {
      filtered = filtered.filter(
        (fund) => fund.fundMaster.assetType === selectedAssetType
      );
    }

    // 클래스 코드 필터
    if (selectedClassCode !== 'ALL') {
      filtered = filtered.filter((fund) => fund.classCode === selectedClassCode);
    }

    // 최소 투자금액 필터
    if (maxAmount) {
      const maxAmountNum = parseInt(maxAmount);
      filtered = filtered.filter((fund) => {
        const minAmount = fund.rules?.minInitialAmount || 0;
        return minAmount <= maxAmountNum;
      });
    }

    setFilteredFunds(filtered);
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
    if (riskNum <= 2) return 'text-blue-600';
    if (riskNum <= 4) return 'text-green-600';
    return 'text-red-600';
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-[#00857D] mx-auto"></div>
          <p className="mt-4 text-gray-600">펀드 목록을 불러오는 중...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="flex justify-center items-center min-h-screen">
        <div className="text-center">
          <p className="text-red-600">{error}</p>
          <button
            onClick={loadFundClasses}
            className="mt-4 px-4 py-2 bg-[#00857D] text-white rounded-lg hover:bg-[#006D66]"
          >
            다시 시도
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto px-4 py-8">
      {/* 헤더 */}
      <div className="mb-8">
        <h1 className="text-3xl font-bold text-gray-900 mb-2">펀드 상품</h1>
        <p className="text-gray-600">IRP 계좌로 투자 가능한 펀드 상품을 확인하세요</p>
      </div>

      {/* 필터 */}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-lg font-semibold mb-4">필터</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {/* 자산 유형 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              자산 유형
            </label>
            <select
              value={selectedAssetType}
              onChange={(e) => setSelectedAssetType(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#00857D]"
            >
              {assetTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>

          {/* 클래스 코드 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              클래스 유형
            </label>
            <select
              value={selectedClassCode}
              onChange={(e) => setSelectedClassCode(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#00857D]"
            >
              {classCodes.map((code) => (
                <option key={code.value} value={code.value}>
                  {code.label}
                </option>
              ))}
            </select>
          </div>

          {/* 최소 투자금액 */}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-2">
              투자 가능 금액 (원)
            </label>
            <input
              type="number"
              value={maxAmount}
              onChange={(e) => setMaxAmount(e.target.value)}
              placeholder="예: 10000"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-[#00857D]"
            />
          </div>
        </div>

        <div className="mt-4 flex justify-between items-center">
          <p className="text-sm text-gray-600">
            총 <span className="font-semibold text-[#00857D]">{filteredFunds.length}</span>개 펀드
          </p>
          <button
            onClick={() => {
              setSelectedAssetType('ALL');
              setSelectedClassCode('ALL');
              setMaxAmount('');
            }}
            className="text-sm text-gray-600 hover:text-[#00857D]"
          >
            필터 초기화
          </button>
        </div>
      </div>

      {/* 펀드 목록 */}
      <div className="grid grid-cols-1 gap-4">
        {filteredFunds.map((fund) => (
          <div
            key={fund.childFundCd}
            className="bg-white rounded-lg shadow-md p-6 hover:shadow-lg transition-shadow cursor-pointer"
            onClick={() => navigate(`/fund/${fund.childFundCd}`)}
          >
            <div className="flex justify-between items-start mb-4">
              <div className="flex-1">
                <div className="flex items-center gap-2 mb-2">
                  <h3 className="text-lg font-semibold text-gray-900">
                    {fund.fundMaster.fundName}
                  </h3>
                  <span className="px-2 py-1 text-xs bg-[#00857D] text-white rounded">
                    {fund.classCode}클래스
                  </span>
                </div>
                <p className="text-sm text-gray-600 mb-2">
                  {fund.fundMaster.assetType}
                </p>
                <p className="text-xs text-gray-500">
                  펀드코드: {fund.childFundCd}
                </p>
              </div>

              <div className="text-right">
                {fund.fundMaster.riskGrade && (
                  <div className={`text-sm font-semibold ${getRiskColor(fund.fundMaster.riskGrade)}`}>
                    위험등급 {fund.fundMaster.riskGrade}
                  </div>
                )}
                {fund.latestNav && fund.latestNavDate && (
                  <div className="mt-2">
                    <div className="text-xs text-gray-500">기준가</div>
                    <div className="text-lg font-bold text-gray-900">
                      {formatNumber(fund.latestNav)}원
                    </div>
                    <div className="text-xs text-gray-500">
                      {new Date(fund.latestNavDate).toLocaleDateString('ko-KR')}
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-gray-200">
              <div>
                <div className="text-xs text-gray-500 mb-1">최소 투자금액</div>
                <div className="text-sm font-semibold">
                  {formatNumber(fund.rules?.minInitialAmount)}원
                </div>
              </div>
              <div>
                <div className="text-xs text-gray-500 mb-1">판매수수료</div>
                <div className="text-sm font-semibold">
                  {formatPercent(fund.fees?.salesFeeBps)}
                </div>
              </div>
              <div>
                <div className="text-xs text-gray-500 mb-1">운용수수료</div>
                <div className="text-sm font-semibold">
                  {formatPercent(fund.fees?.mgmtFeeBps)}
                </div>
              </div>
              <div>
                <div className="text-xs text-gray-500 mb-1">총보수</div>
                <div className="text-sm font-semibold">
                  {formatPercent(fund.fees?.totalFeeBps)}
                </div>
              </div>
            </div>
          </div>
        ))}

        {filteredFunds.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow-md">
            <p className="text-gray-500">조건에 맞는 펀드가 없습니다.</p>
          </div>
        )}
      </div>
    </div>
  );
};

export default FundList;

