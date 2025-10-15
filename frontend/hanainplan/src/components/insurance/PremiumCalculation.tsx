import React, { useState, useEffect } from 'react';
import { calculatePremium } from '../../api/insuranceApi';
import type { InsuranceProduct, PremiumCalculationRequest, InsuranceDetails, PremiumCalculationResponse } from '../../types/insurance';

interface PremiumCalculationProps {
  selectedProduct: InsuranceProduct;
  onNext: (insuranceDetails: InsuranceDetails, premiumCalculation: PremiumCalculationResponse) => void;
  onPrevious: () => void;
}

const PremiumCalculation: React.FC<PremiumCalculationProps> = ({ selectedProduct, onNext, onPrevious }) => {
  const [formData, setFormData] = useState({
    age: 30,
    gender: 'M' as 'M' | 'F',
    coverageAmount: 100000000,
    paymentPeriod: 20,
    coveragePeriod: 30,
    paymentFrequency: 'MONTHLY' as 'MONTHLY' | 'QUARTERLY' | 'SEMI_ANNUAL' | 'ANNUAL',
    riders: [] as string[]
  });

  const [calculation, setCalculation] = useState<PremiumCalculationResponse | null>(null);
  const [isCalculating, setIsCalculating] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const paymentFrequencyOptions = [
    { value: 'MONTHLY', label: '월납', discount: 0 },
    { value: 'QUARTERLY', label: '분기납', discount: 0.02 },
    { value: 'SEMI_ANNUAL', label: '반기납', discount: 0.05 },
    { value: 'ANNUAL', label: '연납', discount: 0.1 }
  ];

  const riderOptions = [
    { id: 'accident', name: '상해특약', description: '상해로 인한 입원/통원 치료비', monthlyPremium: 5000 },
    { id: 'cancer', name: '암특약', description: '암 진단 시 일시금 지급', monthlyPremium: 15000 },
    { id: 'diabetes', name: '당뇨특약', description: '당뇨 관련 의료비 보장', monthlyPremium: 8000 },
    { id: 'heart', name: '심장특약', description: '심장질환 관련 의료비 보장', monthlyPremium: 12000 },
    { id: 'hospital', name: '입원특약', description: '입원 시 일당 지급', monthlyPremium: 3000 }
  ];

  useEffect(() => {
    setFormData(prev => ({
      ...prev,
      coverageAmount: Math.max(selectedProduct.minPremium, Math.min(prev.coverageAmount, selectedProduct.maxPremium))
    }));
  }, [selectedProduct]);

  const handleInputChange = (field: string, value: any) => {
    setFormData(prev => ({
      ...prev,
      [field]: value
    }));
  };

  const handleRiderToggle = (riderId: string) => {
    setFormData(prev => ({
      ...prev,
      riders: prev.riders.includes(riderId)
        ? prev.riders.filter(id => id !== riderId)
        : [...prev.riders, riderId]
    }));
  };

  const calculatePremiumAmount = async () => {
    setIsCalculating(true);
    setError(null);

    try {
      const request: PremiumCalculationRequest = {
        productId: selectedProduct.id,
        age: formData.age,
        gender: formData.gender,
        coverageAmount: formData.coverageAmount,
        paymentPeriod: formData.paymentPeriod,
        coveragePeriod: formData.coveragePeriod,
        paymentFrequency: formData.paymentFrequency,
        riders: formData.riders
      };

      const result = await calculatePremium(request);
      setCalculation(result);
    } catch (err) {
      setError('보험료 계산에 실패했습니다.');
    } finally {
      setIsCalculating(false);
    }
  };

  const handleNext = () => {
    if (!calculation) return;

    const insuranceDetails: InsuranceDetails = {
      coverageAmount: formData.coverageAmount,
      premium: calculation.finalPremium,
      paymentPeriod: formData.paymentPeriod,
      coveragePeriod: formData.coveragePeriod,
      paymentFrequency: formData.paymentFrequency,
      riders: formData.riders
    };

    onNext(insuranceDetails, calculation);
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('ko-KR', {
      style: 'currency',
      currency: 'KRW',
      minimumFractionDigits: 0
    }).format(amount);
  };

  return (
    <div className="space-y-4">
      {}
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-800 mb-2">보험료 계산</h1>
        <p className="text-gray-600">가입 조건에 따른 보험료를 계산해보세요</p>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4">
        {}
        <div className="space-y-4">
          <div className="bg-white rounded-lg shadow p-4">
            <h2 className="text-lg font-semibold text-gray-800 mb-4">가입 정보 입력</h2>

            <div className="space-y-4">
              {}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">연령</label>
                <input
                  type="number"
                  value={formData.age}
                  onChange={(e) => handleInputChange('age', parseInt(e.target.value))}
                  min={selectedProduct.minAge}
                  max={selectedProduct.maxAge}
                  className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                />
                <p className="text-xs text-gray-500 mt-1">
                  가입 가능 연령: {selectedProduct.minAge}세 - {selectedProduct.maxAge}세
                </p>
              </div>

              {}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">성별</label>
                <div className="flex space-x-4">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="gender"
                      value="M"
                      checked={formData.gender === 'M'}
                      onChange={(e) => handleInputChange('gender', e.target.value as 'M' | 'F')}
                      className="mr-1"
                    />
                    <span className="text-sm">남성</span>
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="gender"
                      value="F"
                      checked={formData.gender === 'F'}
                      onChange={(e) => handleInputChange('gender', e.target.value as 'M' | 'F')}
                      className="mr-1"
                    />
                    <span className="text-sm">여성</span>
                  </label>
                </div>
              </div>

              {}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">가입금액</label>
                <input
                  type="number"
                  value={formData.coverageAmount}
                  onChange={(e) => handleInputChange('coverageAmount', parseInt(e.target.value))}
                  min={selectedProduct.minPremium}
                  max={selectedProduct.maxPremium}
                  step="1000000"
                  className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                />
                <p className="text-xs text-gray-500 mt-1">
                  {formatCurrency(selectedProduct.minPremium)} - {formatCurrency(selectedProduct.maxPremium)}
                </p>
              </div>

              {}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">납입기간</label>
                <select
                  value={formData.paymentPeriod}
                  onChange={(e) => handleInputChange('paymentPeriod', parseInt(e.target.value))}
                  className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                >
                  <option value={10}>10년</option>
                  <option value={15}>15년</option>
                  <option value={20}>20년</option>
                  <option value={30}>30년</option>
                </select>
              </div>

              {}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">보장기간</label>
                <select
                  value={formData.coveragePeriod}
                  onChange={(e) => handleInputChange('coveragePeriod', parseInt(e.target.value))}
                  className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                >
                  <option value={10}>10년</option>
                  <option value={15}>15년</option>
                  <option value={20}>20년</option>
                  <option value={30}>30년</option>
                  <option value={80}>80세까지</option>
                  <option value={100}>100세까지</option>
                </select>
              </div>

              {}
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-2">납입주기</label>
                <div className="space-y-1">
                  {paymentFrequencyOptions.map((option) => (
                    <label key={option.value} className="flex items-center justify-between p-2 border border-gray-200 rounded-lg cursor-pointer hover:bg-gray-50">
                      <div className="flex items-center">
                        <input
                          type="radio"
                          name="paymentFrequency"
                          value={option.value}
                          checked={formData.paymentFrequency === option.value}
                          onChange={(e) => handleInputChange('paymentFrequency', e.target.value as any)}
                          className="mr-2"
                        />
                        <span className="font-medium text-sm">{option.label}</span>
                      </div>
                      {option.discount > 0 && (
                        <span className="text-xs text-green-600 font-medium">
                          {option.discount * 100}% 할인
                        </span>
                      )}
                    </label>
                  ))}
                </div>
              </div>
            </div>

            <button
              onClick={calculatePremiumAmount}
              disabled={isCalculating}
              className="w-full mt-4 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 disabled:bg-gray-300 disabled:cursor-not-allowed font-medium text-sm"
            >
              {isCalculating ? '계산 중...' : '보험료 계산하기'}
            </button>
          </div>

          {}
          <div className="bg-white rounded-lg shadow p-4">
            <h2 className="text-lg font-semibold text-gray-800 mb-3">특약 선택 (선택사항)</h2>

            <div className="space-y-3">
              {riderOptions.map((rider) => (
                <label key={rider.id} className="flex items-start space-x-2 cursor-pointer">
                  <input
                    type="checkbox"
                    checked={formData.riders.includes(rider.id)}
                    onChange={() => handleRiderToggle(rider.id)}
                    className="mt-1 w-4 h-4 text-blue-600 border-gray-300 rounded focus:ring-blue-500"
                  />
                  <div className="flex-1">
                    <div className="flex items-center justify-between">
                      <span className="font-medium text-gray-800 text-sm">{rider.name}</span>
                      <span className="text-xs text-blue-600 font-medium">
                        +{formatCurrency(rider.monthlyPremium)}/월
                      </span>
                    </div>
                    <p className="text-xs text-gray-600 mt-1">{rider.description}</p>
                  </div>
                </label>
              ))}
            </div>
          </div>
        </div>

        {}
        <div className="space-y-4">
          {calculation && (
            <div className="bg-white rounded-lg shadow p-4">
              <h2 className="text-lg font-semibold text-gray-800 mb-4">보험료 계산 결과</h2>

              <div className="space-y-3">
                <div className="flex justify-between items-center p-3 bg-blue-50 rounded-lg">
                  <span className="font-medium text-gray-800 text-sm">기본 보험료</span>
                  <span className="font-bold text-blue-600">
                    {formatCurrency(calculation.basePremium)}
                  </span>
                </div>

                {calculation.riderPremium > 0 && (
                  <div className="flex justify-between items-center p-3 bg-green-50 rounded-lg">
                    <span className="font-medium text-gray-800 text-sm">특약 보험료</span>
                    <span className="font-bold text-green-600">
                      +{formatCurrency(calculation.riderPremium)}
                    </span>
                  </div>
                )}

                {calculation.discount > 0 && (
                  <div className="flex justify-between items-center p-3 bg-orange-50 rounded-lg">
                    <span className="font-medium text-gray-800 text-sm">할인</span>
                    <span className="font-bold text-orange-600">
                      -{formatCurrency(calculation.discount)}
                    </span>
                  </div>
                )}

                <div className="border-t pt-3">
                  <div className="flex justify-between items-center p-3 bg-gray-50 rounded-lg">
                    <span className="font-bold text-gray-800">최종 보험료</span>
                    <span className="font-bold text-gray-800 text-lg">
                      {formatCurrency(calculation.finalPremium)}
                    </span>
                  </div>
                </div>
              </div>

              {}
              <div className="mt-4">
                <h3 className="font-semibold text-gray-800 mb-2 text-sm">상세 내역</h3>
                <div className="space-y-1">
                  {calculation.breakdown?.map((item: any, index: number) => (
                    <div key={index} className="flex justify-between text-xs">
                      <span className="text-gray-600">{item.description}</span>
                      <span className="font-medium">{formatCurrency(item.amount)}</span>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 rounded-lg p-3">
              <p className="text-red-600 text-sm">{error}</p>
            </div>
          )}
        </div>
      </div>

      {}
      <div className="flex justify-between">
        <button
          onClick={onPrevious}
          className="px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors text-sm"
        >
          이전 단계
        </button>
        <button
          onClick={handleNext}
          disabled={!calculation}
          className={`px-6 py-2 rounded-lg font-semibold transition-colors text-sm ${
            calculation
              ? 'bg-blue-600 text-white hover:bg-blue-700'
              : 'bg-gray-300 text-gray-500 cursor-not-allowed'
          }`}
        >
          다음 단계
        </button>
      </div>
    </div>
  );
};

export default PremiumCalculation;