import { useState, useEffect } from 'react';
import { useUserStore } from '../store/userStore';
import Layout from '../components/layout/Layout';
import AssetSummary from '../components/portfolio/AssetSummary';
import SavingsInsurance from '../components/portfolio/SavingsInsurance';
import AssetDistribution from '../components/portfolio/AssetDistribution';

interface PortfolioData {
  totalAssets: number;
  totalLiabilities: number;
  netWorth: number;
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

function Portfolio() {
  const { user } = useUserStore();
  const [portfolioData] = useState<PortfolioData>({
    totalAssets: 125000000,
    totalLiabilities: 45000000,
    netWorth: 80000000,
    savings: {
      general: 35000000,
      irp: 15000000,
    },
    insurance: {
      total: 20000000,
      monthly: 150000,
    },
    expenses: {
      monthly: 2500000,
      categories: {
        living: 1200000,
        medical: 300000,
        entertainment: 500000,
        others: 500000,
      },
    },
  });

  useEffect(() => {
    // TODO: 실제 API에서 포트폴리오 데이터를 가져오는 로직
    // fetchPortfolioData();
  }, []);

  if (!user) {
    return (
      <Layout>
        <div className="min-h-screen flex items-center justify-center">
          <div className="text-center">
            <h2 className="text-2xl font-hana-bold text-gray-800 mb-4">로그인이 필요합니다</h2>
            <p className="text-gray-600">포트폴리오를 확인하려면 먼저 로그인해주세요.</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="px-6 py-8">
        <div className="max-w-7xl mx-auto">
          {/* Header */}
          <div className="mb-8">
            <h1 className="text-3xl font-hana-bold text-gray-900 mb-2">
              포트폴리오 관리
            </h1>
            <p className="text-lg text-gray-600">
              {user.name}님의 통합 자산 현황을 한눈에 확인하세요
            </p>
          </div>

          {/* Top Section - Asset Summary */}
          <div className="mb-8">
            <AssetSummary 
              totalAssets={portfolioData.totalAssets}
              totalLiabilities={portfolioData.totalLiabilities}
              netWorth={portfolioData.netWorth}
            />
          </div>

          {/* Middle Section - Savings & Insurance */}
          <div className="mb-8">
            <SavingsInsurance 
              savings={portfolioData.savings}
              insurance={portfolioData.insurance}
            />
          </div>

          {/* Bottom Section - Asset Distribution Chart */}
          <div>
            <AssetDistribution 
              totalAssets={portfolioData.totalAssets}
              savings={portfolioData.savings}
              insurance={portfolioData.insurance}
              expenses={portfolioData.expenses}
            />
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default Portfolio;
