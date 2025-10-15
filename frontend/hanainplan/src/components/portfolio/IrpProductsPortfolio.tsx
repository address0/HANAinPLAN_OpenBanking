import { useState, useEffect } from 'react';
import axios from 'axios';

interface DepositPortfolio {
  portfolioId: number;
  bankCode: string;
  bankName: string;
  productCode: string;
  productName: string;
  subscriptionDate: string;
  maturityDate: string;
  contractPeriod: number;
  principalAmount: number;
  interestRate: number;
  expectedInterest: number;
  maturityAmount: number;
  status: string;
  daysRemaining: number;
  progressPercentage: number;
}

interface IrpProductsPortfolioProps {
  irpAccountNumber: string;
}

function IrpProductsPortfolio({ irpAccountNumber }: IrpProductsPortfolioProps) {
  const [portfolios, setPortfolios] = useState<DepositPortfolio[]>([]);
  const [loading, setLoading] = useState(true);
  const [showAll, setShowAll] = useState(false);

  useEffect(() => {
    fetchPortfolioData();
  }, [irpAccountNumber]);

  const fetchPortfolioData = async () => {
    try {
      setLoading(true);
      const endpoint = showAll
        ? `/api/banking/portfolio/irp/${irpAccountNumber}`
        : `/api/banking/portfolio/irp/${irpAccountNumber}/active`;

      const response = await axios.get(endpoint);
      setPortfolios(response.data.portfolios || []);
    } catch (error) {
    } finally {
      setLoading(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR').format(amount) + '원';
  };

  const formatDate = (dateString: string) => {
    const date = new Date(dateString);
    return date.toLocaleDateString('ko-KR', {
      year: 'numeric',
      month: '2-digit',
      day: '2-digit',
    }).replace(/\./g, '.').replace(/\s/g, '');
  };

  const getStatusBadge = (status: string) => {
    const statusMap: { [key: string]: { label: string; color: string } } = {
      ACTIVE: { label: '운용중', color: 'bg-green-100 text-green-800' },
      MATURED: { label: '만기', color: 'bg-blue-100 text-blue-800' },
      CANCELLED: { label: '해지', color: 'bg-gray-100 text-gray-800' },
    };

    const statusInfo = statusMap[status] || { label: status, color: 'bg-gray-100 text-gray-800' };

    return (
      <span className={`px-2 py-1 rounded-full text-xs font-hana-medium ${statusInfo.color}`}>
        {statusInfo.label}
      </span>
    );
  };

  const getBankLogo = (bankCode: string) => {
    const logoMap: { [key: string]: string } = {
      HANA: '/bank/081.png',
      KOOKMIN: '/bank/004.png',
      SHINHAN: '/bank/088.png',
    };
    return logoMap[bankCode] || '/bank/081.png';
  };

  const totalPrincipal = portfolios.reduce((sum, p) => sum + p.principalAmount, 0);
  const totalExpectedInterest = portfolios.reduce((sum, p) => sum + p.expectedInterest, 0);
  const totalMaturityAmount = portfolios.reduce((sum, p) => sum + p.maturityAmount, 0);

  if (loading) {
    return (
      <div className="bg-white rounded-2xl shadow-lg p-8">
        <div className="flex items-center justify-center py-12">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-hana-green"></div>
          <span className="ml-3 text-gray-600">포트폴리오 정보를 불러오는 중...</span>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-2xl shadow-lg p-8">
      <div className="flex items-center justify-between mb-6">
        <div>
          <h2 className="text-2xl font-hana-bold text-gray-900">IRP 포트폴리오 구성 상품</h2>
          <p className="text-sm text-gray-500 mt-1">IRP 계좌로 가입한 금융상품 내역</p>
        </div>
        <button
          onClick={() => {
            setShowAll(!showAll);
            fetchPortfolioData();
          }}
          className="px-4 py-2 text-sm font-hana-medium text-hana-green border border-hana-green rounded-lg hover:bg-hana-green hover:text-white transition-colors"
        >
          {showAll ? '운용중만 보기' : '전체 보기'}
        </button>
      </div>

      {portfolios.length === 0 ? (
        <div className="text-center py-12">
          <div className="w-16 h-16 bg-gray-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
            </svg>
          </div>
          <h3 className="text-lg font-hana-medium text-gray-900 mb-2">가입 상품이 없습니다</h3>
          <p className="text-sm text-gray-500">
            IRP 계좌로 정기예금, 펀드, 채권 등의 상품을 가입해보세요
          </p>
        </div>
      ) : (
        <>
          {}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-4 mb-8 p-4 bg-gradient-to-r from-hana-green/5 to-blue-50 rounded-xl">
            <div className="text-center">
              <p className="text-sm text-gray-600 mb-1">총 투자 원금</p>
              <p className="text-2xl font-hana-bold text-gray-900">{formatCurrency(totalPrincipal)}</p>
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-600 mb-1">예상 이자 수익</p>
              <p className="text-2xl font-hana-bold text-green-600">+{formatCurrency(totalExpectedInterest)}</p>
            </div>
            <div className="text-center">
              <p className="text-sm text-gray-600 mb-1">만기 예상 금액</p>
              <p className="text-2xl font-hana-bold text-purple-600">{formatCurrency(totalMaturityAmount)}</p>
            </div>
          </div>

          {}
          <div className="space-y-4">
            {portfolios.map((portfolio) => (
              <div
                key={portfolio.portfolioId}
                className="border border-gray-200 rounded-xl p-6 hover:shadow-md transition-shadow"
              >
                {}
                <div className="flex items-center justify-between mb-4">
                  <div className="flex items-center space-x-3">
                    <img
                      src={getBankLogo(portfolio.bankCode)}
                      alt={portfolio.bankName}
                      className="h-10 w-10 object-contain"
                    />
                    <div>
                      <h3 className="text-lg font-hana-bold text-gray-900">{portfolio.productName}</h3>
                      <p className="text-sm text-gray-500">{portfolio.bankName}</p>
                    </div>
                  </div>
                  {getStatusBadge(portfolio.status)}
                </div>

                {}
                <div className="grid grid-cols-2 md:grid-cols-4 gap-4 mb-4">
                  <div>
                    <p className="text-xs text-gray-500 mb-1">투자 원금</p>
                    <p className="text-base font-hana-bold text-gray-900">{formatCurrency(portfolio.principalAmount)}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 mb-1">적용 금리</p>
                    <p className="text-base font-hana-bold text-hana-green">{portfolio.interestRate}%</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 mb-1">예상 이자</p>
                    <p className="text-base font-hana-bold text-green-600">+{formatCurrency(portfolio.expectedInterest)}</p>
                  </div>
                  <div>
                    <p className="text-xs text-gray-500 mb-1">만기 예상액</p>
                    <p className="text-base font-hana-bold text-purple-600">{formatCurrency(portfolio.maturityAmount)}</p>
                  </div>
                </div>

                {}
                <div className="flex items-center justify-between text-sm text-gray-600 mb-4 pb-4 border-b border-gray-200">
                  <div className="flex items-center space-x-4">
                    <span>가입일: <span className="font-hana-medium text-gray-900">{formatDate(portfolio.subscriptionDate)}</span></span>
                    <span>만기일: <span className="font-hana-medium text-gray-900">{formatDate(portfolio.maturityDate)}</span></span>
                    <span>계약기간: <span className="font-hana-medium text-gray-900">{portfolio.contractPeriod}개월</span></span>
                  </div>
                </div>

                {}
                {portfolio.status === 'ACTIVE' && (
                  <div>
                    <div className="flex items-center justify-between mb-2">
                      <span className="text-sm text-gray-600">운용 진행률</span>
                      <div className="flex items-center space-x-2">
                        <span className="text-sm font-hana-medium text-gray-900">{portfolio.progressPercentage}%</span>
                        <span className="text-xs text-gray-500">만기까지 {portfolio.daysRemaining}일</span>
                      </div>
                    </div>
                    <div className="w-full bg-gray-200 rounded-full h-2">
                      <div
                        className="bg-gradient-to-r from-hana-green to-green-400 h-2 rounded-full transition-all"
                        style={{ width: `${portfolio.progressPercentage}%` }}
                      ></div>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export default IrpProductsPortfolio;