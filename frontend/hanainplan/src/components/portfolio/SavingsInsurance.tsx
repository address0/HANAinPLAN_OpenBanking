
interface SavingsInsuranceProps {
  savings: {
    general: number;
    irp: number;
  };
  insurance: {
    total: number;
    monthly: number;
  };
}

function SavingsInsurance({ savings, insurance }: SavingsInsuranceProps) {
  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0,
      maximumFractionDigits: 0,
    }).format(amount);
  };

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
      {}
      <div className="bg-white rounded-2xl shadow-lg p-8">
        <div className="mb-6">
          <h2 className="text-2xl font-hana-bold text-gray-900">예적금 & IRP</h2>
        </div>

        {}
        <div className="mb-6">
          <div className="bg-gradient-to-r from-hana-green/10 to-hana-green/20 rounded-xl p-6 border border-hana-green/30">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-hana-bold text-gray-900">일반 금융상품</h3>
              <span className="text-sm text-hana-green font-hana-medium">연 3.2%</span>
            </div>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-gray-600 font-hana-medium">현재 잔액</span>
                <span className="text-2xl font-hana-bold text-gray-900">
                  {formatCurrency(savings.general)}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600 font-hana-medium">예상 수익률</span>
                <span className="text-hana-green font-hana-bold">+₩1,120,000</span>
              </div>
              <div className="w-full bg-gray-200 rounded-full h-2">
                <div
                  className="bg-hana-green h-2 rounded-full"
                  style={{ width: '75%' }}
                ></div>
              </div>
              <p className="text-xs text-gray-500">목표 달성률: 75%</p>
            </div>
          </div>
        </div>

        {}
        <div>
          <div className="bg-gradient-to-r from-hana-green/15 to-hana-green/25 rounded-xl p-6 border border-hana-green/40">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-hana-bold text-gray-900">IRP 계좌</h3>
              <span className="text-sm text-hana-green font-hana-medium">세제혜택</span>
            </div>
            {savings.irp > 0 ? (
              <div className="space-y-3">
                <div className="flex justify-between items-center">
                  <span className="text-gray-600 font-hana-medium">현재 잔액</span>
                  <span className="text-2xl font-hana-bold text-gray-900">
                    {formatCurrency(savings.irp)}
                  </span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600 font-hana-medium">세제 혜택</span>
                  <span className="text-hana-green font-hana-bold">₩450,000/년</span>
                </div>
                <div className="flex justify-between items-center">
                  <span className="text-gray-600 font-hana-medium">예상 수익률</span>
                  <span className="text-hana-green font-hana-bold">+₩480,000</span>
                </div>
                <div className="w-full bg-gray-200 rounded-full h-2">
                  <div
                    className="bg-hana-green h-2 rounded-full"
                    style={{ width: '60%' }}
                  ></div>
                </div>
                <p className="text-xs text-gray-500">목표 달성률: 60%</p>
              </div>
            ) : (
              <div className="text-center py-4">
                <p className="text-gray-500 font-hana-medium mb-3">개설된 IRP 계좌가 없습니다</p>
                <button
                  onClick={() => window.location.href = '/products/irp'}
                  className="bg-hana-green text-white px-4 py-2 rounded-lg font-hana-medium hover:bg-hana-green/90 transition-colors"
                >
                  IRP 계좌 개설하기
                </button>
              </div>
            )}
          </div>
        </div>

        {}
        <div className="mt-6 p-4 bg-hana-green/5 rounded-xl border border-hana-green/20">
          <h4 className="font-hana-bold text-gray-900 mb-2">추천 상품</h4>
          <div className="space-y-2">
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">하나 정기예금 (12개월)</span>
              <span className="text-sm font-hana-bold text-hana-green">연 3.5%</span>
            </div>
            <div className="flex justify-between items-center">
              <span className="text-sm text-gray-600">하나 적금 (24개월)</span>
              <span className="text-sm font-hana-bold text-hana-green">연 4.2%</span>
            </div>
          </div>
        </div>
      </div>

      {}
      <div className="bg-white rounded-2xl shadow-lg p-8">
        <div className="mb-6">
          <h2 className="text-2xl font-hana-bold text-gray-900">보험 현황</h2>
        </div>

        {}
        <div className="mb-6">
          <div className="bg-gradient-to-r from-hana-green/10 to-hana-green/20 rounded-xl p-6 border border-hana-green/30">
            <div className="flex items-center justify-between mb-4">
              <h3 className="text-lg font-hana-bold text-gray-900">총 보험료</h3>
              <span className="text-sm text-hana-green font-hana-medium">월 납입</span>
            </div>
            <div className="space-y-3">
              <div className="flex justify-between items-center">
                <span className="text-gray-600 font-hana-medium">월 납입액</span>
                <span className="text-2xl font-hana-bold text-gray-900">
                  {formatCurrency(insurance.monthly)}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600 font-hana-medium">총 보험금액</span>
                <span className="text-lg font-hana-bold text-gray-900">
                  {formatCurrency(insurance.total)}
                </span>
              </div>
              <div className="flex justify-between items-center">
                <span className="text-gray-600 font-hana-medium">연간 납입액</span>
                <span className="text-lg font-hana-bold text-gray-900">
                  {formatCurrency(insurance.monthly * 12)}
                </span>
              </div>
            </div>
          </div>
        </div>

        {}
        <div className="space-y-4">
          <h3 className="text-lg font-hana-bold text-gray-900">가입 보험 상품</h3>

          <div className="space-y-3">
            {}
            <div className="bg-hana-green/10 rounded-xl p-4 border border-hana-green/30">
              <div className="flex justify-between items-start mb-2">
                <div>
                  <h4 className="font-hana-bold text-gray-900">하나 생명보험</h4>
                  <p className="text-sm text-gray-600">종신보험</p>
                </div>
                <span className="text-sm font-hana-bold text-hana-green">월 ₩80,000</span>
              </div>
              <div className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">보험금액:</span>
                  <span className="font-hana-medium">{formatCurrency(100000000)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">주요 혜택:</span>
                  <span className="font-hana-medium text-hana-green">사망/재해</span>
                </div>
              </div>
            </div>

            {}
            <div className="bg-hana-green/10 rounded-xl p-4 border border-hana-green/30">
              <div className="flex justify-between items-start mb-2">
                <div>
                  <h4 className="font-hana-bold text-gray-900">하나 건강보험</h4>
                  <p className="text-sm text-gray-600">실손보험</p>
                </div>
                <span className="text-sm font-hana-bold text-hana-green">월 ₩45,000</span>
              </div>
              <div className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">보험금액:</span>
                  <span className="font-hana-medium">{formatCurrency(50000000)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">주요 혜택:</span>
                  <span className="font-hana-medium text-hana-green">질병/상해</span>
                </div>
              </div>
            </div>

            {}
            <div className="bg-hana-green/10 rounded-xl p-4 border border-hana-green/30">
              <div className="flex justify-between items-start mb-2">
                <div>
                  <h4 className="font-hana-bold text-gray-900">하나 연금보험</h4>
                  <p className="text-sm text-gray-600">변액연금</p>
                </div>
                <span className="text-sm font-hana-bold text-hana-green">월 ₩25,000</span>
              </div>
              <div className="space-y-1">
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">보험금액:</span>
                  <span className="font-hana-medium">{formatCurrency(50000000)}</span>
                </div>
                <div className="flex justify-between text-sm">
                  <span className="text-gray-600">주요 혜택:</span>
                  <span className="font-hana-medium text-hana-green">노후준비</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        {}
        <div className="mt-6 p-4 bg-gray-50 rounded-xl">
          <h4 className="font-hana-bold text-gray-900 mb-2">보험 혜택 요약</h4>
          <div className="grid grid-cols-2 gap-4 text-sm">
            <div className="text-center">
              <p className="text-gray-600">총 보장액</p>
              <p className="font-hana-bold text-lg">{formatCurrency(200000000)}</p>
            </div>
            <div className="text-center">
              <p className="text-gray-600">월 보험료</p>
              <p className="font-hana-bold text-lg">{formatCurrency(150000)}</p>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
}

export default SavingsInsurance;