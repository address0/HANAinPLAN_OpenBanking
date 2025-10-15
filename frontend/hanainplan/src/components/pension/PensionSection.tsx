import { useState } from 'react'
import FloatingCharacter from './FloatingCharacter'
import InputField from './InputField'
import SelectField from './SelectField'
import PensionChart from './PensionChart'
import CTAButton from '../main/CTAButton'

type FormData = {
  currentAge: string
  retirementAge: string
  currentSalary: string
  currentAssets: string
  monthlyContribution: string
  expectedReturn: string
  inflationRate: string
}

type PensionData = {
  age: number
  yearlyPension: number
  monthlyPension: number
  totalAssets: number
}

function PensionSection() {
  const [formData, setFormData] = useState<FormData>({
    currentAge: '',
    retirementAge: '',
    currentSalary: '',
    currentAssets: '',
    monthlyContribution: '',
    expectedReturn: '5',
    inflationRate: '2'
  })

  const [calculationResult, setCalculationResult] = useState<PensionData[]>([])
  const [isCalculated, setIsCalculated] = useState(false)

  const handleInputChange = (field: keyof FormData) => (value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }))
  }

  const calculatePension = () => {
    const currentAge = parseInt(formData.currentAge)
    const retirementAge = parseInt(formData.retirementAge)
    const currentSalary = parseInt(formData.currentSalary) * 10000
    const currentAssets = parseInt(formData.currentAssets) * 10000
    const monthlyContribution = parseInt(formData.monthlyContribution) * 10000
    const expectedReturn = parseFloat(formData.expectedReturn) / 100
    const inflationRate = parseFloat(formData.inflationRate) / 100

    const results: PensionData[] = []

    let totalAssets = currentAssets
    const workingYears = retirementAge - currentAge

    for (let i = 0; i <= workingYears; i++) {
      const age = currentAge + i

      if (i > 0) {
        totalAssets = totalAssets * (1 + expectedReturn) + (monthlyContribution * 12)
      }

      const yearlyPension = totalAssets * 0.04
      const monthlyPension = yearlyPension / 12

      results.push({
        age,
        yearlyPension: Math.round(yearlyPension / 10000),
        monthlyPension: Math.round(monthlyPension / 10000),
        totalAssets: Math.round(totalAssets)
      })
    }

    for (let i = 1; i <= 10; i++) {
      const age = retirementAge + i
      const yearlyPension = totalAssets * 0.04
      totalAssets = (totalAssets - yearlyPension) * (1 + expectedReturn - inflationRate)

      const monthlyPension = yearlyPension / 12

      results.push({
        age,
        yearlyPension: Math.round(yearlyPension / 10000),
        monthlyPension: Math.round(monthlyPension / 10000),
        totalAssets: Math.round(totalAssets)
      })
    }

    setCalculationResult(results)
    setIsCalculated(true)
  }

  const isFormValid = () => {
    return formData.currentAge && formData.retirementAge &&
           formData.currentSalary && formData.currentAssets &&
           formData.monthlyContribution && formData.expectedReturn &&
           formData.inflationRate
  }

  return (
    <div className="min-h-screen bg-gray-50 py-20">
      <div className="container mx-auto px-4">
        {}
        <FloatingCharacter />

        {}
        <div className="text-center mb-12">
          <h1 className="text-3xl lg:text-4xl font-hana-bold text-gray-900 mb-4">
            연금 계산기
          </h1>
          <p className="text-lg text-gray-600 font-hana-medium max-w-2xl mx-auto">
            나의 자산, 연봉 정보를 기반으로 앞으로 받을 연금액과 수령 기간을 계산해보세요.
          </p>
        </div>

        <div className="max-w-4xl mx-auto space-y-8">
          {}
          <div className="bg-white rounded-xl shadow-lg p-8">
            <h2 className="text-xl font-hana-bold text-gray-900 mb-6">기본 정보 입력</h2>

            <div className="space-y-6">
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <InputField
                  label="현재 나이"
                  value={formData.currentAge}
                  onChange={handleInputChange('currentAge')}
                  type="number"
                  placeholder="35"
                  unit="세"
                  required
                />
                <InputField
                  label="은퇴 예정 나이"
                  value={formData.retirementAge}
                  onChange={handleInputChange('retirementAge')}
                  type="number"
                  placeholder="65"
                  unit="세"
                  required
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                <InputField
                  label="현재 연봉"
                  value={formData.currentSalary}
                  onChange={handleInputChange('currentSalary')}
                  type="number"
                  placeholder="5000"
                  unit="만원"
                  required
                />
                <InputField
                  label="현재 보유 자산"
                  value={formData.currentAssets}
                  onChange={handleInputChange('currentAssets')}
                  type="number"
                  placeholder="3000"
                  unit="만원"
                  required
                />
              </div>

              <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                <InputField
                  label="월 적립 금액"
                  value={formData.monthlyContribution}
                  onChange={handleInputChange('monthlyContribution')}
                  type="number"
                  placeholder="100"
                  unit="만원"
                  required
                />
                <SelectField
                  label="기대 수익률"
                  value={formData.expectedReturn}
                  onChange={handleInputChange('expectedReturn')}
                  options={[
                    { value: '3', label: '3% (안정형)' },
                    { value: '5', label: '5% (균형형)' },
                    { value: '7', label: '7% (성장형)' },
                    { value: '9', label: '9% (적극형)' }
                  ]}
                  required
                />
                <SelectField
                  label="물가상승률"
                  value={formData.inflationRate}
                  onChange={handleInputChange('inflationRate')}
                  options={[
                    { value: '1', label: '1%' },
                    { value: '2', label: '2%' },
                    { value: '3', label: '3%' },
                    { value: '4', label: '4%' }
                  ]}
                  required
                />
              </div>

              <CTAButton
                label="연금 계산하기"
                onClick={calculatePension}
                className={`w-full h-12 ${!isFormValid() ? 'opacity-50 cursor-not-allowed' : ''}`}
              />
            </div>
          </div>

          {}
          {isCalculated ? (
            <PensionChart
              data={calculationResult}
              currentAge={parseInt(formData.currentAge)}
            />
          ) : (
            <div className="bg-white rounded-xl shadow-lg p-8 flex flex-col items-center justify-center min-h-[300px]">
              <div className="text-center">
                <div className="w-24 h-24 bg-gray-100 rounded-full flex items-center justify-center mb-4 mx-auto">
                  <svg className="w-12 h-12 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 19v-6a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2a2 2 0 002-2zm0 0V9a2 2 0 012-2h2a2 2 0 012 2v10m-6 0a2 2 0 002 2h2a2 2 0 002-2m0 0V5a2 2 0 012-2h2a2 2 0 012 2v14a2 2 0 01-2 2h-2a2 2 0 01-2-2z" />
                  </svg>
                </div>
                <h3 className="text-lg font-hana-medium text-gray-900 mb-2">연금 계산 결과</h3>
                <p className="text-gray-600 font-hana-regular">
                  위 정보를 입력하고 계산하기 버튼을 눌러주세요.
                </p>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  )
}

export default PensionSection