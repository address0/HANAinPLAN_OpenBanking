import { useState, useEffect } from 'react';
import Layout from '../../components/layout/Layout';
import { useUserStore } from '../../store/userStore';
import { fundProductApi } from '../../api/fundApi';
import type { FundClassDetail } from '../../types/fund.types';

function ConsultantFundProducts() {
  const { user } = useUserStore();

  // 상품 목록 상태
  const [allProducts, setAllProducts] = useState<FundClassDetail[]>([]);
  const [filteredProducts, setFilteredProducts] = useState<FundClassDetail[]>([]);
  const [loading, setLoading] = useState(false);

  // 필터 상태
  const [selectedFundType, setSelectedFundType] = useState<string>('ALL');
  const [selectedRiskLevel, setSelectedRiskLevel] = useState<string>('ALL');

  // 상세보기 모달 상태
  const [selectedProduct, setSelectedProduct] = useState<FundClassDetail | null>(null);

  // 펀드 유형 목록
  const fundTypes = [
    { value: 'ALL', label: '전체' },
    { value: '주식형', label: '주식형' },
    { value: '채권형', label: '채권형' },
    { value: '혼합형', label: '혼합형' },
    { value: 'MMF', label: 'MMF' },
  ];

  // 위험등급 목록
  const riskLevels = [
    { value: 'ALL', label: '전체' },
    { value: '1', label: '매우 높음' },
    { value: '2', label: '높음' },
    { value: '3', label: '보통' },
    { value: '4', label: '낮음' },
    { value: '5', label: '매우 낮음' },
  ];

  // 컴포넌트 마운트 시 상품 목록 조회
  useEffect(() => {
    fetchAllProducts();
  }, []);

  // 필터 변경 시 적용
  useEffect(() => {
    applyFilters();
  }, [selectedFundType, selectedRiskLevel, allProducts]);

  /**
   * 상세보기 모달 열기
   */
  const openProductDetail = (product: FundClassDetail) => {
    setSelectedProduct(product);
  };

  /**
   * 상세보기 모달 닫기
   */
  const closeProductDetail = () => {
    setSelectedProduct(null);
  };

  /**
   * 모든 펀드 상품 조회
   */
  const fetchAllProducts = async () => {
    try {
      setLoading(true);
      const products = await fundProductApi.getAllFundClasses();
      setAllProducts(products);
    } catch (error) {
      console.error('펀드 상품 조회 실패:', error);
      alert('펀드 상품 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  /**
   * 필터 적용
   */
  const applyFilters = () => {
    let filtered = [...allProducts];

    // 펀드 유형 필터
    if (selectedFundType !== 'ALL') {
      filtered = filtered.filter(product => {
        return product.fundMaster?.assetType === selectedFundType;
      });
    }

    // 위험등급 필터
    if (selectedRiskLevel !== 'ALL') {
      filtered = filtered.filter(product => {
        return product.fundMaster?.riskGrade === selectedRiskLevel;
      });
    }

    setFilteredProducts(filtered);
  };

  /**
   * 필터 초기화
   */
  const resetFilters = () => {
    setSelectedFundType('ALL');
    setSelectedRiskLevel('ALL');
    setFilteredProducts(allProducts);
  };

  if (loading && allProducts.length === 0) {
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

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* 헤더 */}
          <div className="mb-8">
            <h1 className="text-3xl font-hana-bold text-gray-900 mb-2">펀드 상품 목록</h1>
            <p className="text-gray-600 font-hana-regular">고객님께 추천할 수 있는 펀드 상품을 확인하세요</p>
          </div>

          {/* 필터 */}
          <div className="bg-white rounded-xl shadow-lg p-6 mb-8">
            <h2 className="text-lg font-hana-bold mb-4">필터</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-4">
              {/* 펀드 유형 */}
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  펀드 유형
                </label>
                <select
                  value={selectedFundType}
                  onChange={(e) => setSelectedFundType(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
                >
                  {fundTypes.map((type) => (
                    <option key={type.value} value={type.value}>
                      {type.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* 위험등급 */}
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  위험등급
                </label>
                <select
                  value={selectedRiskLevel}
                  onChange={(e) => setSelectedRiskLevel(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-regular"
                >
                  {riskLevels.map((level) => (
                    <option key={level.value} value={level.value}>
                      {level.label}
                    </option>
                  ))}
                </select>
              </div>

              {/* 필터 초기화 버튼 */}
              <div className="flex items-end">
                <button
                  onClick={resetFilters}
                  className="w-full px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-hana-medium hover:bg-gray-200 transition-colors"
                >
                  초기화
                </button>
              </div>
            </div>

            <div className="flex justify-between items-center">
              <p className="text-sm text-gray-600 font-hana-regular">
                총 <span className="font-hana-bold text-hana-green">{filteredProducts.length}</span>개 펀드
              </p>
            </div>
          </div>

          {/* 펀드 목록 */}
          <div className="grid grid-cols-1 gap-6">
            {filteredProducts.map((fund) => (
              <div key={fund.childFundCd} className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow">
                <div className="flex justify-between items-start mb-4">
                  <div className="flex-1">
                    <div className="flex items-center gap-3 mb-2">
                      <h3 className="text-xl font-hana-bold text-gray-900">{fund.fundMaster?.fundName}</h3>
                      <span className="px-2 py-1 text-xs font-hana-medium bg-hana-green text-white rounded">
                        {fund.classCode}클래스
                      </span>
                      <span className={`px-2 py-1 text-xs font-hana-medium rounded ${
                        fund.fundMaster?.riskGrade === '1' ? 'bg-red-100 text-red-800' :
                        fund.fundMaster?.riskGrade === '2' ? 'bg-orange-100 text-orange-800' :
                        fund.fundMaster?.riskGrade === '3' ? 'bg-yellow-100 text-yellow-800' :
                        fund.fundMaster?.riskGrade === '4' ? 'bg-blue-100 text-blue-800' :
                        'bg-green-100 text-green-800'
                      }`}>
                        위험 {fund.fundMaster?.riskGrade}등급
                      </span>
                    </div>
                    <p className="text-gray-600 font-hana-regular mb-2">{fund.fundMaster?.assetType}</p>
                    <p className="text-sm text-gray-500 font-hana-regular">
                      펀드코드: {fund.childFundCd}
                    </p>
                  </div>
                </div>

                {/* 수익률 정보 테이블 */}
                <div className="bg-white border border-gray-200 rounded-lg mb-4 overflow-hidden">
                  <div className="bg-hana-green text-white px-4 py-3">
                    <h4 className="font-hana-bold text-base">수익률 정보</h4>
                  </div>
                  <div className="p-4">
                    <div className="overflow-x-auto">
                      <table className="w-full">
                        <thead>
                          <tr className="border-b border-gray-200">
                            <th className="text-left py-2 px-3 text-sm font-hana-medium text-gray-700">기간</th>
                            <th className="text-right py-2 px-3 text-sm font-hana-medium text-gray-700">수익률</th>
                            <th className="text-right py-2 px-3 text-sm font-hana-medium text-gray-700">예상 수익</th>
                          </tr>
                        </thead>
                        <tbody>
                          <tr className="border-b border-gray-100">
                            <td className="py-3 px-3 text-sm font-hana-regular">1개월</td>
                            <td className={`py-3 px-3 text-right text-sm font-hana-bold ${(fund.return1month ?? 0) >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                              {(fund.return1month ?? 0) >= 0 ? '+' : ''}{(fund.return1month ?? 0).toFixed(2)}%
                            </td>
                            <td className="py-3 px-3 text-right text-sm font-hana-medium text-gray-600">
                              1,000만원 기준: 약 {Math.round(10000000 * ((fund.return1month ?? 0) / 100) / 12).toLocaleString()}원
                            </td>
                          </tr>
                          <tr className="border-b border-gray-100">
                            <td className="py-3 px-3 text-sm font-hana-regular">3개월</td>
                            <td className={`py-3 px-3 text-right text-sm font-hana-bold ${(fund.return3month ?? 0) >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                              {(fund.return3month ?? 0) >= 0 ? '+' : ''}{(fund.return3month ?? 0).toFixed(2)}%
                            </td>
                            <td className="py-3 px-3 text-right text-sm font-hana-medium text-gray-600">
                              1,000만원 기준: 약 {Math.round(10000000 * ((fund.return3month ?? 0) / 100) / 4).toLocaleString()}원
                            </td>
                          </tr>
                          <tr className="border-b border-gray-100">
                            <td className="py-3 px-3 text-sm font-hana-regular">6개월</td>
                            <td className={`py-3 px-3 text-right text-sm font-hana-bold ${(fund.return6month ?? 0) >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                              {(fund.return6month ?? 0) >= 0 ? '+' : ''}{(fund.return6month ?? 0).toFixed(2)}%
                            </td>
                            <td className="py-3 px-3 text-right text-sm font-hana-medium text-gray-600">
                              1,000만원 기준: 약 {Math.round(10000000 * ((fund.return6month ?? 0) / 100) / 2).toLocaleString()}원
                            </td>
                          </tr>
                          <tr>
                            <td className="py-3 px-3 text-sm font-hana-regular">1년</td>
                            <td className={`py-3 px-3 text-right text-sm font-hana-bold ${(fund.return1year ?? 0) >= 0 ? 'text-red-600' : 'text-blue-600'}`}>
                              {(fund.return1year ?? 0) >= 0 ? '+' : ''}{(fund.return1year ?? 0).toFixed(2)}%
                            </td>
                            <td className="py-3 px-3 text-right text-sm font-hana-medium text-gray-600">
                              1,000만원 기준: 약 {Math.round(10000000 * ((fund.return1year ?? 0) / 100)).toLocaleString()}원
                            </td>
                          </tr>
                        </tbody>
                      </table>
                    </div>
                    <p className="text-xs text-gray-500 mt-3 text-center">
                      * 예시금액은 세전 기준이며, 실제 수익률은 시장 상황에 따라 변동될 수 있습니다.
                    </p>
                  </div>
                </div>

                {/* 기준가 정보 */}
                <div className="bg-gray-50 p-4 rounded-lg mb-4">
                  <h4 className="font-hana-medium text-gray-900 mb-3">기준가 정보</h4>
                  <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">기준가</div>
                      <div className="text-lg font-hana-bold text-hana-green">
                        {fund.latestNav?.toLocaleString()}원
                      </div>
                    </div>
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">기준일</div>
                      <div className="text-sm font-hana-medium text-gray-600">
                        {fund.latestNavDate ? new Date(fund.latestNavDate).toLocaleDateString('ko-KR') : '정보 없음'}
                      </div>
                    </div>
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">수수료</div>
                      <div className="text-sm font-hana-medium text-gray-600">
                        판매: {fund.fees?.salesFeeBps ? `${fund.fees.salesFeeBps}bps` : '정보 없음'}
                      </div>
                    </div>
                    <div className="text-center">
                      <div className="text-sm text-gray-600 mb-1">운용보수</div>
                      <div className="text-sm font-hana-medium text-gray-600">
                        {fund.fees?.mgmtFeeBps ? `${fund.fees.mgmtFeeBps}bps` : '정보 없음'}
                      </div>
                    </div>
                  </div>
                </div>

                {/* 기본 정보 */}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4 pt-4 border-t border-gray-200">
                  <div>
                    <div className="text-xs text-gray-500 mb-1 font-hana-regular">최소 투자금액</div>
                    <div className="text-sm font-hana-medium">
                      {fund.rules?.minInitialAmount?.toLocaleString()}원
                    </div>
                  </div>
                  <div>
                    <div className="text-xs text-gray-500 mb-1 font-hana-regular">총보수율</div>
                    <div className="text-sm font-hana-medium">
                      {fund.fees?.totalFeeBps ? `${fund.fees.totalFeeBps}bps` : '정보 없음'}
                    </div>
                  </div>
                  <div>
                    <div className="text-xs text-gray-500 mb-1 font-hana-regular">자산운용사</div>
                    <div className="text-sm font-hana-medium">
                      {fund.fundMaster?.managementCompany || '정보 없음'}
                    </div>
                  </div>
                  <div>
                    <div className="text-xs text-gray-500 mb-1 font-hana-regular">위탁회사</div>
                    <div className="text-sm font-hana-medium">
                      {fund.fundMaster?.trustCompany || '정보 없음'}
                    </div>
                  </div>
                </div>

                {/* 추천 대상 및 액션 버튼 */}
                <div className="flex justify-between items-center pt-4 border-t border-gray-200">
                  <div className="text-sm text-gray-600">
                    <span className="font-hana-medium">추천 대상:</span> {fund.fundMaster?.riskGrade ? `${fund.fundMaster.riskGrade}등급 위험 선호 고객` : '정보 없음'}
                  </div>
                  <div className="flex gap-3">
                    <button
                      onClick={() => openProductDetail(fund)}
                      className="px-4 py-2 bg-gray-100 text-gray-700 rounded-lg font-hana-medium hover:bg-gray-200 transition-colors"
                    >
                      상세보기
                    </button>
                    <button className="px-4 py-2 bg-hana-green text-white rounded-lg font-hana-medium hover:bg-green-600 transition-colors">
                      고객 추천
                    </button>
                  </div>
                </div>
              </div>
            ))}

            {filteredProducts.length === 0 && (
              <div className="text-center py-12 bg-white rounded-xl shadow-lg">
                <p className="text-gray-500 font-hana-regular">조건에 맞는 펀드가 없습니다.</p>
              </div>
            )}
          </div>
        </div>
      </div>

      {/* 상세보기 모달 */}
      {selectedProduct && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
          onClick={closeProductDetail}
        >
          <div
            className="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="p-6 border-b">
              <div className="flex items-center justify-between">
                <h2 className="text-2xl font-hana-bold text-gray-900">{selectedProduct.fundMaster?.fundName}</h2>
                <button
                  onClick={closeProductDetail}
                  className="text-gray-400 hover:text-gray-600 text-2xl"
                >
                  ×
                </button>
              </div>
            </div>

            <div className="p-6">
              <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                {/* 기본 정보 */}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">기본 정보</h3>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-gray-500">펀드코드</span>
                      <span className="font-hana-medium">{selectedProduct.childFundCd}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">클래스코드</span>
                      <span className="font-hana-medium">{selectedProduct.classCode}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">자산유형</span>
                      <span className="font-hana-medium">{selectedProduct.fundMaster?.assetType}</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">위험등급</span>
                      <span className={`font-hana-medium ${
                        selectedProduct.fundMaster?.riskGrade === '1' ? 'text-red-600' :
                        selectedProduct.fundMaster?.riskGrade === '2' ? 'text-orange-600' :
                        selectedProduct.fundMaster?.riskGrade === '3' ? 'text-yellow-600' :
                        selectedProduct.fundMaster?.riskGrade === '4' ? 'text-blue-600' :
                        'text-green-600'
                      }`}>
                        {selectedProduct.fundMaster?.riskGrade}등급
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">통화</span>
                      <span className="font-hana-medium">{selectedProduct.fundMaster?.currency}</span>
                    </div>
                  </div>
                </div>

                {/* 기준가 및 수수료 정보 */}
                <div>
                  <h3 className="text-lg font-hana-bold text-gray-900 mb-4">기준가 및 수수료</h3>
                  <div className="space-y-3">
                    <div className="flex justify-between">
                      <span className="text-gray-500">기준가</span>
                      <span className="font-hana-medium">{selectedProduct.latestNav?.toLocaleString()}원</span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">기준일</span>
                      <span className="font-hana-medium">
                        {selectedProduct.latestNavDate ? new Date(selectedProduct.latestNavDate).toLocaleDateString('ko-KR') : '정보 없음'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">판매수수료</span>
                      <span className="font-hana-medium">
                        {selectedProduct.fees?.salesFeeBps ? `${selectedProduct.fees.salesFeeBps}bps` : '정보 없음'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">운용보수</span>
                      <span className="font-hana-medium">
                        {selectedProduct.fees?.mgmtFeeBps ? `${selectedProduct.fees.mgmtFeeBps}bps` : '정보 없음'}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-gray-500">총보수율</span>
                      <span className="font-hana-medium">
                        {selectedProduct.fees?.totalFeeBps ? `${selectedProduct.fees.totalFeeBps}bps` : '정보 없음'}
                      </span>
                    </div>
                  </div>
                </div>
              </div>

              {/* 규칙 정보 */}
              <div className="mt-8">
                <h3 className="text-lg font-hana-bold text-gray-900 mb-4">규칙 정보</h3>
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h4 className="font-hana-medium text-gray-900 mb-3">최소 투자금액</h4>
                    <p className="text-gray-700">
                      {selectedProduct.rules?.minInitialAmount?.toLocaleString()}원
                    </p>
                  </div>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h4 className="font-hana-medium text-gray-900 mb-3">자산운용사</h4>
                    <p className="text-gray-700">
                      {selectedProduct.fundMaster?.managementCompany || '정보 없음'}
                    </p>
                  </div>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h4 className="font-hana-medium text-gray-900 mb-3">위탁회사</h4>
                    <p className="text-gray-700">
                      {selectedProduct.fundMaster?.trustCompany || '정보 없음'}
                    </p>
                  </div>
                  <div className="bg-gray-50 p-4 rounded-lg">
                    <h4 className="font-hana-medium text-gray-900 mb-3">판매상태</h4>
                    <p className="text-gray-700">
                      {selectedProduct.saleStatus === 'ACTIVE' ? '판매중' : '판매중지'}
                    </p>
                  </div>
                </div>
              </div>

              <div className="mt-8">
                <button
                  onClick={closeProductDetail}
                  className="w-full bg-hana-green text-white px-8 py-3 rounded-lg font-hana-medium hover:bg-green-600 transition-colors"
                >
                  닫기
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </Layout>
  );
}

export default ConsultantFundProducts;
