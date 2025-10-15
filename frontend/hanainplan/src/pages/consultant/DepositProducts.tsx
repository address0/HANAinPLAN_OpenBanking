import { useState, useEffect } from 'react';
import Layout from '../../components/layout/Layout';
import InterestRateComparison from '../../components/products/InterestRateComparison';
import {
  getAllInterestRates,
  type InterestRateInfo
} from '../../api/productApi';

function ConsultantDepositProducts() {
  const [interestRates, setInterestRates] = useState<InterestRateInfo[]>([]);
  const [loading, setLoading] = useState(false);

  useEffect(() => {
    fetchInterestRates();
  }, []);

  const fetchInterestRates = async () => {
    try {
      setLoading(true);
      const rates = await getAllInterestRates();
      setInterestRates(rates);
    } catch (error) {
      alert('금리 정보 조회에 실패했습니다.');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[calc(100vh-240px)]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
            <p className="mt-4 text-gray-600 font-hana-regular">금리 정보를 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
          <div className="mb-8">
            <h1 className="text-3xl font-hana-bold text-gray-900 mb-3">정기예금 상품 금리 조회</h1>
          </div>

          {}
          <InterestRateComparison rates={interestRates} />
        </div>
      </div>
    </Layout>
  );
}

export default ConsultantDepositProducts;