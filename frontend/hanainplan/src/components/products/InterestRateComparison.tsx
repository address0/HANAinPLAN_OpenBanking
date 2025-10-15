import { useState } from 'react';
import type { InterestRateInfo } from '../../api/productApi';

interface InterestRateComparisonProps {
  rates: InterestRateInfo[];
}

function InterestRateComparison({ rates }: InterestRateComparisonProps) {
  const [selectedBank, setSelectedBank] = useState<string>('ALL');

  const filteredRates = selectedBank === 'ALL'
    ? rates
    : rates.filter(rate => rate.bankCode === selectedBank);

  const ratesByBank: { [key: string]: InterestRateInfo[] } = {};
  filteredRates.forEach(rate => {
    if (!ratesByBank[rate.bankName]) {
      ratesByBank[rate.bankName] = [];
    }
    ratesByBank[rate.bankName].push(rate);
  });

  const periodOrder = ['6개월', '1년', '2년', '3년', '5년'];
  Object.keys(ratesByBank).forEach(bankName => {
    ratesByBank[bankName].sort((a, b) => {
      return periodOrder.indexOf(a.maturityPeriod) - periodOrder.indexOf(b.maturityPeriod);
    });
  });

  return (
    <div className="space-y-6">
      {}
      <div className="flex gap-2 border-b border-gray-200">
        {['ALL', 'HANA', 'KOOKMIN', 'SHINHAN'].map(bank => (
          <button
            key={bank}
            onClick={() => setSelectedBank(bank)}
            className={`px-6 py-3 font-hana-medium transition-colors ${
              selectedBank === bank
                ? 'border-b-2 border-hana-green text-hana-green'
                : 'text-gray-500 hover:text-gray-700'
            }`}
          >
            {bank === 'ALL' ? '전체' : bank === 'HANA' ? '하나은행' : bank === 'KOOKMIN' ? '국민은행' : '신한은행'}
          </button>
        ))}
      </div>

      {}
      {Object.entries(ratesByBank).map(([bankName, bankRates]) => {
        const normalDeposits = bankRates.filter(r => r.productCode.includes('001'));
        const defaultOptions = bankRates.filter(r => r.productCode.includes('002'));

        return (
          <div key={bankName} className="bg-white rounded-lg shadow-md overflow-hidden">
            <div className="bg-gradient-to-r from-hana-green to-green-600 text-white px-6 py-4">
              <h3 className="text-xl font-hana-bold">{bankName}</h3>
            </div>

            <div className="p-6 space-y-6">
              {}
              {normalDeposits.length > 0 && (
                <div>
                  <h4 className="text-lg font-hana-bold text-gray-900 mb-4">일반 정기예금</h4>
                  <div className="overflow-x-auto">
                    <table className="w-full">
                      <thead>
                        <tr className="bg-gray-50">
                          <th className="px-4 py-3 text-left text-sm font-hana-medium text-gray-600">만기기간</th>
                          <th className="px-4 py-3 text-right text-sm font-hana-medium text-gray-600">금리 (%)</th>
                          <th className="px-4 py-3 text-right text-sm font-hana-medium text-gray-600">1000만원 기준 이자</th>
                        </tr>
                      </thead>
                      <tbody>
                        {normalDeposits.map((rate, idx) => {
                          const interestAmount = 10000000 * rate.interestRate;
                          return (
                            <tr key={idx} className="border-t border-gray-100 hover:bg-gray-50">
                              <td className="px-4 py-3 text-sm font-hana-medium text-gray-800">{rate.maturityPeriod}</td>
                              <td className="px-4 py-3 text-right text-lg font-hana-bold text-hana-green">
                                {(rate.interestRate * 100).toFixed(2)}%
                              </td>
                              <td className="px-4 py-3 text-right text-sm font-hana-medium text-gray-600">
                                {Math.floor(interestAmount).toLocaleString()}원
                              </td>
                            </tr>
                          );
                        })}
                      </tbody>
                    </table>
                  </div>
                </div>
              )}

              {}
              {defaultOptions.length > 0 && (
                <div>
                  <h4 className="text-lg font-hana-bold text-gray-900 mb-4">
                    디폴트옵션 정기예금
                    <span className="ml-2 text-sm font-hana-regular text-gray-500">
                      (자동 만기 연장형)
                    </span>
                  </h4>
                  <div className="bg-blue-50 rounded-lg p-4">
                    {defaultOptions.map((rate, idx) => {
                      const interestAmount = 10000000 * rate.interestRate;
                      return (
                        <div key={idx} className="flex justify-between items-center">
                          <div>
                            <div className="text-sm font-hana-medium text-gray-600">{rate.maturityPeriod}</div>
                            <div className="text-xs text-gray-500 mt-1">
                              1000만원 기준: {Math.floor(interestAmount).toLocaleString()}원
                            </div>
                          </div>
                          <div className="text-2xl font-hana-bold text-blue-600">
                            {(rate.interestRate * 100).toFixed(2)}%
                          </div>
                        </div>
                      );
                    })}
                  </div>
                </div>
              )}
            </div>
          </div>
        );
      })}

      {filteredRates.length === 0 && (
        <div className="text-center py-12 text-gray-500">
          금리 정보가 없습니다.
        </div>
      )}
    </div>
  );
}

export default InterestRateComparison;