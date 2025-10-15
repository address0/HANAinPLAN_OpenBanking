import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { fundProductApi } from '../api/fundApi';
import type { FundClassDetail } from '../types/fund.types';
import Layout from '../components/layout/Layout';
import { formatAmount, formatPercent, getRiskColor } from '../utils/fundUtils';
import { useUserStore } from '../store/userStore';

const FundList = () => {
  const navigate = useNavigate();
  const { user } = useUserStore();
  const [fundClasses, setFundClasses] = useState<FundClassDetail[]>([]);
  const [filteredFunds, setFilteredFunds] = useState<FundClassDetail[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selectedAssetType, setSelectedAssetType] = useState<string>('ALL');
  const [selectedClassCode, setSelectedClassCode] = useState<string>('ALL');
  const [maxAmount, setMaxAmount] = useState<string>('');

  const assetTypes = [
    { value: 'ALL', label: '전체' },
    { value: 'MIXED', label: '혼합형' },
    { value: 'BOND', label: '채권형' },
    { value: 'EQUITY', label: '주식형' },
  ];

  const classCodes = [
    { value: 'ALL', label: '전체' },
    { value: 'P', label: 'P클래스 (퇴직연금)' },
    { value: 'C', label: 'C클래스 (일반)' },
    { value: 'A', label: 'A클래스' },
  ];

  useEffect(() => {
    loadFundClasses();
  }, []);

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
    } finally {
      setLoading(false);
    }
  };

  const applyFilters = () => {
    let filtered = [...fundClasses];

    if (selectedAssetType !== 'ALL') {
      filtered = filtered.filter(
        (fund) => fund.fundMaster.assetType === selectedAssetType
      );
    }

    if (selectedClassCode !== 'ALL') {
      filtered = filtered.filter((fund) => fund.classCode === selectedClassCode);
    }

    if (maxAmount) {
      const maxAmountNum = parseInt(maxAmount);
      filtered = filtered.filter((fund) => {
        const minAmount = fund.rules?.minInitialAmount || 0;
        return minAmount <= maxAmountNum;
      });
    }

    setFilteredFunds(filtered);
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[calc(100vh-240px)]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
            <p className="mt-4 text-gray-600 font-hana-regular">펀드 목록을 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  if (error) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[calc(100vh-240px)]">
          <div className="text-center">
            <p className="text-red-600 font-hana-regular">{error}</p>
            <button
              onClick={loadFundClasses}
              className="mt-4 px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 font-hana-medium transition-colors"
            >
              다시 시도
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="max-w-7xl mx-auto px-4 py-8">
      {}
      <div className="mb-8">
        <h1 className="text-3xl font-hana-bold text-gray-900 mb-2">펀드 상품</h1>
        <p className="text-gray-600 font-hana-regular">
          {user?.userType === 'COUNSELOR'
            ? '고객님께 추천할 수 있는 펀드 상품을 확인하세요'
            : 'IRP 계좌로 투자 가능한 펀드 상품을 확인하세요'}
        </p>
      </div>

      {}
      <div className="bg-white rounded-lg shadow-md p-6 mb-6">
        <h2 className="text-lg font-hana-bold mb-4">필터</h2>
        <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              자산 유형
            </label>
            <select
              value={selectedAssetType}
              onChange={(e) => setSelectedAssetType(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
            >
              {assetTypes.map((type) => (
                <option key={type.value} value={type.value}>
                  {type.label}
                </option>
              ))}
            </select>
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              클래스 유형
            </label>
            <select
              value={selectedClassCode}
              onChange={(e) => setSelectedClassCode(e.target.value)}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
            >
              {classCodes.map((code) => (
                <option key={code.value} value={code.value}>
                  {code.label}
                </option>
              ))}
            </select>
          </div>

          {}
          <div>
            <label className="block text-sm font-hana-medium text-gray-700 mb-2">
              투자 가능 금액 (원)
            </label>
            <input
              type="number"
              value={maxAmount}
              onChange={(e) => setMaxAmount(e.target.value)}
              placeholder="예: 10000"
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
            />
          </div>
        </div>

        <div className="mt-4 flex justify-between items-center">
          <p className="text-sm text-gray-600 font-hana-regular">
            총 <span className="font-hana-bold text-hana-green">{filteredFunds.length}</span>개 펀드
          </p>
          <button
            onClick={() => {
              setSelectedAssetType('ALL');
              setSelectedClassCode('ALL');
              setMaxAmount('');
            }}
            className="text-sm text-gray-600 hover:text-hana-green font-hana-medium transition-colors"
          >
            필터 초기화
          </button>
        </div>
      </div>

      {}
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
                  <h3 className="text-lg font-hana-bold text-gray-900">
                    {fund.fundMaster.fundName}
                  </h3>
                  <span className="px-2 py-1 text-xs bg-hana-green text-white rounded font-hana-medium">
                    {fund.classCode}클래스
                  </span>
                  {fund.sourceUrl && (
                    <a
                      href={fund.sourceUrl}
                      target="_blank"
                      rel="noopener noreferrer"
                      onClick={(e) => e.stopPropagation()}
                      className="text-hana-green hover:text-green-700 transition-colors"
                      title="공식 페이지에서 보기"
                    >
                      <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M10 6H6a2 2 0 00-2 2v10a2 2 0 002 2h10a2 2 0 002-2v-4M14 4h6m0 0v6m0-6L10 14" />
                      </svg>
                    </a>
                  )}
                </div>
                <p className="text-sm text-gray-600 mb-2 font-hana-regular">
                  {fund.fundMaster.assetType}
                </p>
                <p className="text-xs text-gray-500 font-hana-regular">
                  펀드코드: {fund.childFundCd}
                </p>
              </div>

              <div className="text-right">
                {fund.fundMaster.riskGrade && (
                  <div className={`text-sm font-hana-bold ${getRiskColor(fund.fundMaster.riskGrade)}`}>
                    위험등급 {fund.fundMaster.riskGrade}
                  </div>
                )}
                {fund.latestNav && fund.latestNavDate && (
                  <div className="mt-2">
                    <div className="text-xs text-gray-500 font-hana-regular">기준가</div>
                    <div className="text-lg font-hana-bold text-gray-900">
                      {formatAmount(fund.latestNav)}원
                    </div>
                    <div className="text-xs text-gray-500 font-hana-regular">
                      {new Date(fund.latestNavDate).toLocaleDateString('ko-KR')}
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="grid grid-cols-2 md:grid-cols-4 gap-4 pt-4 border-t border-gray-200">
              <div>
                <div className="text-xs text-gray-500 mb-1 font-hana-regular">최소 투자금액</div>
                <div className="text-sm font-hana-medium">
                  {formatAmount(fund.rules?.minInitialAmount)}원
                </div>
              </div>
              <div>
                <div className="text-xs text-gray-500 mb-1 font-hana-regular">판매수수료</div>
                <div className="text-sm font-hana-medium">
                  {formatPercent(fund.fees?.salesFeeBps)}
                </div>
              </div>
              <div>
                <div className="text-xs text-gray-500 mb-1 font-hana-regular">운용수수료</div>
                <div className="text-sm font-hana-medium">
                  {formatPercent(fund.fees?.mgmtFeeBps)}
                </div>
              </div>
              <div>
                <div className="text-xs text-gray-500 mb-1 font-hana-regular">총보수</div>
                <div className="text-sm font-hana-medium">
                  {formatPercent(fund.fees?.totalFeeBps)}
                </div>
              </div>
            </div>
          </div>
        ))}

        {filteredFunds.length === 0 && (
          <div className="text-center py-12 bg-white rounded-lg shadow-md">
            <p className="text-gray-500 font-hana-regular">조건에 맞는 펀드가 없습니다.</p>
          </div>
        )}
      </div>
      </div>
    </Layout>
  );
};

export default FundList;