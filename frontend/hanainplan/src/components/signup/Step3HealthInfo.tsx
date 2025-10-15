import { useState, useEffect } from 'react'
import { getDiseaseCodes, searchDiseaseCodes, type DiseaseCodeData } from '../../api/userApi'

interface DiseaseDetail {
  diseaseCode: string;
  diseaseName: string;
  diseaseCategory: string;
  riskLevel: string;
  severity: 'mild' | 'moderate' | 'severe' | null;
  progressPeriod: 'under_1month' | '1_3months' | '3_6months' | '6_12months' | 'over_1year' | null;
  isChronic: boolean | null;
  description?: string;
}

interface HealthInfo {
  recentMedicalAdvice: boolean | null;
  recentHospitalization: boolean | null;
  majorDisease: boolean | null;
  diseaseDetails: DiseaseDetail[];
  longTermMedication: boolean | null;
  disabilityRegistered: boolean | null;
  insuranceRejection: boolean | null;
}

interface Step3Props {
  healthInfo: HealthInfo
  onHealthInfoChange: (field: keyof HealthInfo, value: any) => void
}

type HealthSubStep = 1 | 2 | 3

function Step3HealthInfo({ healthInfo, onHealthInfoChange }: Step3Props) {
  const [currentSubStep, setCurrentSubStep] = useState<HealthSubStep>(1)
  const [diseaseSearchKeyword, setDiseaseSearchKeyword] = useState('')
  const [diseaseList, setDiseaseList] = useState<DiseaseCodeData[]>([])
  const [filteredDiseases, setFilteredDiseases] = useState<DiseaseCodeData[]>([])
  const [showDiseaseDropdown, setShowDiseaseDropdown] = useState(false)
  const [isDiseaseInputFocused, setIsDiseaseInputFocused] = useState(false)
  const [selectedDiseaseForDetails, setSelectedDiseaseForDetails] = useState<DiseaseCodeData | null>(null)
  const [showDiseaseDetailsForm, setShowDiseaseDetailsForm] = useState(false)
  const [severity, setSeverity] = useState<DiseaseDetail['severity']>(null)
  const [progressPeriod, setProgressPeriod] = useState<DiseaseDetail['progressPeriod']>(null)
  const [isChronic, setIsChronic] = useState<boolean | null>(null)

  useEffect(() => {
    const loadDiseaseCodes = async () => {
      try {
        const data = await getDiseaseCodes()
        setDiseaseList(data)
      } catch (error) {
        setDiseaseList([])
      }
    }

    loadDiseaseCodes()
  }, [])

  useEffect(() => {
    if (diseaseSearchKeyword.trim() === '') {
      if (isDiseaseInputFocused) {
        setFilteredDiseases(diseaseList)
        setShowDiseaseDropdown(true)
      } else {
        setFilteredDiseases([])
        setShowDiseaseDropdown(false)
      }
      return
    }

    const searchDiseaseHandler = async () => {
      try {
        const data = await searchDiseaseCodes(diseaseSearchKeyword)
        setFilteredDiseases(data)
        setShowDiseaseDropdown(true)
      } catch (error) {
        setFilteredDiseases([])
        setShowDiseaseDropdown(false)
      }
    }

    const timeoutId = setTimeout(searchDiseaseHandler, 500)
    return () => clearTimeout(timeoutId)
  }, [diseaseSearchKeyword, isDiseaseInputFocused, diseaseList])

  const handleDiseaseSelect = (disease: DiseaseCodeData) => {
    setSelectedDiseaseForDetails(disease)
    setShowDiseaseDetailsForm(true)
    setShowDiseaseDropdown(false)
    setIsDiseaseInputFocused(false)
    setDiseaseSearchKeyword(disease.diseaseName)
    setSeverity(null)
    setProgressPeriod(null)
    setIsChronic(null)
  }

  const handleDiseaseDetailsSubmit = (diseaseDetail: DiseaseDetail) => {
    const updatedDiseaseDetails = [...(healthInfo.diseaseDetails || []), diseaseDetail]
    onHealthInfoChange('diseaseDetails', updatedDiseaseDetails)
    setShowDiseaseDetailsForm(false)
    setSelectedDiseaseForDetails(null)
    setDiseaseSearchKeyword('')
    setSeverity(null)
    setProgressPeriod(null)
    setIsChronic(null)
  }

  const handleRemoveDisease = (index: number) => {
    const updatedDiseaseDetails = healthInfo.diseaseDetails.filter((_, i) => i !== index)
    onHealthInfoChange('diseaseDetails', updatedDiseaseDetails)
  }

  const handleDiseaseInputFocus = () => {
    setIsDiseaseInputFocused(true)
    if (diseaseSearchKeyword.trim() === '') {
      setFilteredDiseases(diseaseList)
      setShowDiseaseDropdown(true)
    }
  }

  const handleDiseaseInputBlur = () => {
    setTimeout(() => {
      setIsDiseaseInputFocused(false)
      if (diseaseSearchKeyword.trim() === '') {
        setShowDiseaseDropdown(false)
      }
    }, 150)
  }

  const isSubStep1Valid = () => {
    return healthInfo.recentMedicalAdvice !== null && healthInfo.recentHospitalization !== null
  }

  const isSubStep2Valid = () => {
    if (healthInfo.majorDisease === null) return false
    if (healthInfo.majorDisease === true) {
      return healthInfo.diseaseDetails && healthInfo.diseaseDetails.length > 0
    }
    return true
  }

  const isSubStep3Valid = () => {
    return healthInfo.longTermMedication !== null &&
           healthInfo.disabilityRegistered !== null &&
           healthInfo.insuranceRejection !== null
  }

  const isCurrentSubStepValid = () => {
    switch (currentSubStep) {
      case 1: return isSubStep1Valid()
      case 2: return isSubStep2Valid()
      case 3: return isSubStep3Valid()
      default: return false
    }
  }
  const renderDiseaseDetailsForm = () => {
    if (!selectedDiseaseForDetails || !showDiseaseDetailsForm) return null

    const handleSubmit = () => {
      if (severity && progressPeriod && isChronic !== null) {
        const diseaseDetail: DiseaseDetail = {
          diseaseCode: selectedDiseaseForDetails.diseaseCode,
          diseaseName: selectedDiseaseForDetails.diseaseName,
          diseaseCategory: selectedDiseaseForDetails.diseaseCategory,
          riskLevel: selectedDiseaseForDetails.riskLevel,
          severity,
          progressPeriod,
          isChronic,
          description: selectedDiseaseForDetails.description
        }
        handleDiseaseDetailsSubmit(diseaseDetail)
      }
    }

    const isFormValid = severity && progressPeriod && isChronic !== null

    return (
      <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
        <div className="bg-white rounded-[12px] p-6 max-w-md w-full mx-4 max-h-[90vh] overflow-y-auto">
          <h3 className="font-['Hana2.0_M'] text-[16px] text-gray-800 mb-4">
            질병 상세 정보 입력하기
          </h3>

          {}
          <div className="bg-gray-50 rounded-[8px] p-3 mb-4">
            <div className="flex justify-between items-start mb-2">
              <span className="font-['Hana2.0_M'] text-[14px] text-gray-800">
                {selectedDiseaseForDetails.diseaseName}
              </span>
              <span className={`px-2 py-1 rounded text-[12px] font-['Hana2.0_M'] ${
                selectedDiseaseForDetails.riskLevel === '높음' ? 'bg-red-100 text-red-600' :
                selectedDiseaseForDetails.riskLevel === '중간' ? 'bg-yellow-100 text-yellow-600' :
                'bg-green-100 text-green-600'
              }`}>
                위험도: {selectedDiseaseForDetails.riskLevel}
              </span>
            </div>
            <p className="font-['Hana2.0_M'] text-[12px] text-gray-600">
              분류: {selectedDiseaseForDetails.diseaseCategory}
            </p>
            {selectedDiseaseForDetails.description && (
              <p className="font-['Hana2.0_M'] text-[12px] text-gray-600 mt-1">
                {selectedDiseaseForDetails.description}
              </p>
            )}
          </div>

          {}
          <div className="mb-4">
            <h4 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-2">중증도</h4>
            <div className="space-y-2">
              {[
                { value: 'mild', label: '경증 (일상생활에 지장 없음)' },
                { value: 'moderate', label: '중등도 (일부 제한 있음)' },
                { value: 'severe', label: '중증 (일상생활에 큰 지장)' }
              ].map((option) => (
                <label key={option.value} className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="severity"
                    checked={severity === option.value}
                    onChange={() => setSeverity(option.value as DiseaseDetail['severity'])}
                    className="sr-only"
                  />
                  <div className={`w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center ${
                    severity === option.value ? 'border-[#008485] bg-[#008485]' : 'border-gray-300'
                  }`}>
                    {severity === option.value && (
                      <div className="w-2 h-2 rounded-full bg-white"></div>
                    )}
                  </div>
                  <span className="font-['Hana2.0_M'] text-[13px] text-gray-700">{option.label}</span>
                </label>
              ))}
            </div>
          </div>

          {}
          <div className="mb-4">
            <h4 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-2">진단 후 경과 기간</h4>
            <div className="space-y-2">
              {[
                { value: 'under_1month', label: '1개월 미만' },
                { value: '1_3months', label: '1개월 ~ 3개월' },
                { value: '3_6months', label: '3개월 ~ 6개월' },
                { value: '6_12months', label: '6개월 ~ 1년' },
                { value: 'over_1year', label: '1년 이상' }
              ].map((option) => (
                <label key={option.value} className="flex items-center cursor-pointer">
                  <input
                    type="radio"
                    name="progressPeriod"
                    checked={progressPeriod === option.value}
                    onChange={() => setProgressPeriod(option.value as DiseaseDetail['progressPeriod'])}
                    className="sr-only"
                  />
                  <div className={`w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center ${
                    progressPeriod === option.value ? 'border-[#008485] bg-[#008485]' : 'border-gray-300'
                  }`}>
                    {progressPeriod === option.value && (
                      <div className="w-2 h-2 rounded-full bg-white"></div>
                    )}
                  </div>
                  <span className="font-['Hana2.0_M'] text-[13px] text-gray-700">{option.label}</span>
                </label>
              ))}
            </div>
          </div>

          {}
          <div className="mb-6">
            <h4 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-2">만성 질환 여부</h4>
            <div className="flex gap-4">
              <label className="flex items-center cursor-pointer">
                <input
                  type="radio"
                  name="isChronic"
                  checked={isChronic === true}
                  onChange={() => setIsChronic(true)}
                  className="sr-only"
                />
                <div className={`w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center ${
                  isChronic === true ? 'border-[#008485] bg-[#008485]' : 'border-gray-300'
                }`}>
                  {isChronic === true && (
                    <div className="w-2 h-2 rounded-full bg-white"></div>
                  )}
                </div>
                <span className="font-['Hana2.0_M'] text-[13px] text-gray-700">만성</span>
              </label>
              <label className="flex items-center cursor-pointer">
                <input
                  type="radio"
                  name="isChronic"
                  checked={isChronic === false}
                  onChange={() => setIsChronic(false)}
                  className="sr-only"
                />
                <div className={`w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center ${
                  isChronic === false ? 'border-[#008485] bg-[#008485]' : 'border-gray-300'
                }`}>
                  {isChronic === false && (
                    <div className="w-2 h-2 rounded-full bg-white"></div>
                  )}
                </div>
                <span className="font-['Hana2.0_M'] text-[13px] text-gray-700">급성</span>
              </label>
            </div>
          </div>

          {}
          <div className="flex gap-2">
            <button
              onClick={() => {
                setShowDiseaseDetailsForm(false)
                setSelectedDiseaseForDetails(null)
                setDiseaseSearchKeyword('')
                setSeverity(null)
                setProgressPeriod(null)
                setIsChronic(null)
              }}
              className="flex-1 px-4 py-2 bg-gray-100 text-gray-700 rounded-[6px] font-['Hana2.0_M'] text-[14px] hover:bg-gray-200 transition-colors duration-200"
            >
              취소
            </button>
            <button
              onClick={handleSubmit}
              disabled={!isFormValid}
              className={`flex-1 px-4 py-2 rounded-[6px] font-['Hana2.0_M'] text-[14px] transition-all duration-200 ${
                isFormValid
                  ? 'bg-[#008485] text-white hover:bg-[#006666] cursor-pointer'
                  : 'bg-gray-100 text-gray-400 cursor-not-allowed'
              }`}
            >
              저장
            </button>
          </div>
        </div>
      </div>
    )
  }

  const renderRadioOption = (field: keyof HealthInfo, value: boolean, label: string) => (
    <label className="flex items-center cursor-pointer">
      <input
        type="radio"
        name={field}
        checked={healthInfo[field] === value}
        onChange={() => onHealthInfoChange(field, value)}
        className="sr-only"
      />
      <div className={`w-5 h-5 rounded-full border-2 mr-2 flex items-center justify-center ${
        healthInfo[field] === value
          ? 'border-[#008485] bg-[#008485]'
          : 'border-gray-300'
      }`}>
        {healthInfo[field] === value && (
          <div className="w-2 h-2 rounded-full bg-white"></div>
        )}
      </div>
      <span className="font-['Hana2.0_M'] text-[14px] text-gray-700">{label}</span>
    </label>
  )

  const renderSubStep1 = () => (
    <div className="w-[480px] space-y-4 px-2 max-h-[400px] overflow-y-auto">
      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-2">
          1. 최근 3개월 이내에 의사로부터 입원, 수술, 추가검사(재검 포함)를 권유받은 사실이 있습니까?
        </h3>
        <p className="font-['Hana2.0_M'] text-[12px] text-gray-500 mb-3">
          (예: 위내시경 재검, 종양 재검 등도 포함)
        </p>
        <div className="flex gap-4">
          {renderRadioOption('recentMedicalAdvice', true, '예')}
          {renderRadioOption('recentMedicalAdvice', false, '아니오')}
        </div>
      </div>

      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-2">
          2. 최근 2년 이내에 입원 또는 수술을 받은 적이 있습니까?
        </h3>
        <p className="font-['Hana2.0_M'] text-[12px] text-gray-500 mb-3">
          (단순 외래 치료나 처방은 제외)
        </p>
        <div className="flex gap-4">
          {renderRadioOption('recentHospitalization', true, '예')}
          {renderRadioOption('recentHospitalization', false, '아니오')}
        </div>
      </div>
    </div>
  )

  const renderSubStep2 = () => (
    <div className="w-[480px] space-y-4 px-2 max-h-[400px] overflow-y-auto">
      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-2">
          3. 최근 5년 이내에 질병으로 진단, 입원, 수술을 받은 적이 있습니까?
        </h3>
        <div className="flex gap-4 mb-4">
          {renderRadioOption('majorDisease', true, '예')}
          {renderRadioOption('majorDisease', false, '아니오')}
        </div>

        {}
        {healthInfo.majorDisease === true && (
          <div className="border-t border-gray-200 pt-4">
            <h4 className="font-['Hana2.0_M'] text-md text-gray-700 mb-2">질병명</h4>
            <div className="relative mb-4">
              <input
                type="text"
                placeholder="질병명을 검색하세요 (예: 당뇨병, 고혈압 등)"
                value={diseaseSearchKeyword}
                onChange={(e) => setDiseaseSearchKeyword(e.target.value)}
                onFocus={handleDiseaseInputFocus}
                onBlur={handleDiseaseInputBlur}
                className="w-full p-3 border border-gray-300 rounded-[8px] font-['Hana2.0_M'] text-[14px] focus:outline-none focus:border-[#008485]"
              />

              {}
              {showDiseaseDropdown && filteredDiseases.length > 0 && (
                <div className="absolute top-full left-0 right-0 bg-white border border-gray-300 rounded-[8px] mt-1 max-h-[200px] overflow-y-auto z-10 shadow-lg">
                  {filteredDiseases.map((disease) => (
                    <div
                      key={disease.diseaseCode}
                      onClick={() => handleDiseaseSelect(disease)}
                      className="p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                    >
                      <div className="flex justify-between items-center">
                        <div>
                          <span className="font-['Hana2.0_M'] text-[14px] text-gray-800 block">
                            {disease.diseaseName}
                          </span>
                          <span className="font-['Hana2.0_M'] text-[12px] text-gray-500">
                            {disease.diseaseCategory}
                          </span>
                        </div>
                        <span className={`px-2 py-1 rounded text-[12px] font-['Hana2.0_M'] ${
                          disease.riskLevel === '높음' ? 'bg-red-100 text-red-600' :
                          disease.riskLevel === '중간' ? 'bg-yellow-100 text-yellow-600' :
                          'bg-green-100 text-green-600'
                        }`}>
                          위험도: {disease.riskLevel}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}
            </div>

              {healthInfo.diseaseDetails && healthInfo.diseaseDetails.length > 0 ? (
                <div className="space-y-2">
                  <h4 className="font-['Hana2.0_M'] text-[13px] text-gray-700 mb-2">선택된 질병 목록</h4>
                {healthInfo.diseaseDetails.map((disease, index) => (
                  <div key={index} className="bg-gray-50 rounded-[8px] p-3">
                    <div className="flex justify-between items-start mb-2">
                      <div className="flex-1">
                        <div className="flex items-center gap-2 mb-1">
                          <span className="font-['Hana2.0_M'] text-[14px] text-gray-800">
                            {disease.diseaseName}
                          </span>
                          <span className={`px-2 py-1 rounded text-[11px] font-['Hana2.0_M'] ${
                            disease.riskLevel === '높음' ? 'bg-red-100 text-red-600' :
                            disease.riskLevel === '중간' ? 'bg-yellow-100 text-yellow-600' :
                            'bg-green-100 text-green-600'
                          }`}>
                            위험도: {disease.riskLevel}
                          </span>
                        </div>
                        <div className="font-['Hana2.0_M'] text-[12px] text-gray-600 space-y-1">
                          <div>분류: {disease.diseaseCategory}</div>
                          <div>중증도: {
                            disease.severity === 'mild' ? '경증' :
                            disease.severity === 'moderate' ? '중등도' : '중증'
                          }</div>
                          <div>경과기간: {
                            disease.progressPeriod === 'under_1month' ? '1개월 미만' :
                            disease.progressPeriod === '1_3months' ? '1-3개월' :
                            disease.progressPeriod === '3_6months' ? '3-6개월' :
                            disease.progressPeriod === '6_12months' ? '6개월-1년' : '1년 이상'
                          }</div>
                          <div>만성여부: {disease.isChronic ? '만성' : '급성'}</div>
                        </div>
                      </div>
                      <button
                        onClick={() => handleRemoveDisease(index)}
                        className="text-red-500 hover:text-red-700 font-['Hana2.0_M'] text-[12px]"
                      >
                        삭제
                      </button>
                    </div>
                  </div>
                ))}
                </div>
            ) : (
              <div className="w-full h-full flex flex-col gap-2 justify-center items-center">
                <img src="/images/noDisease.png" alt="empty" className="w-1/3" />
                <p className="font-['Hana2.0_M'] text-sm text-gray-500">선택된 질병이 없습니다.</p>
              </div>
            )}

          </div>
        )}
      </div>
    </div>
  )

  const renderSubStep3 = () => (
    <div className="w-[480px] space-y-4 px-2 max-h-[400px] overflow-y-auto">
      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-3">
          4. 장기복용 중인 약물이 있습니까?
        </h3>
        <div className="flex gap-4">
          {renderRadioOption('longTermMedication', true, '예')}
          {renderRadioOption('longTermMedication', false, '아니오')}
        </div>
      </div>

      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-3">
          5. 장애진단을 받은 적이 있거나 현재 장애 등록이 되어 있습니까?
        </h3>
        <div className="flex gap-4">
          {renderRadioOption('disabilityRegistered', true, '예')}
          {renderRadioOption('disabilityRegistered', false, '아니오')}
        </div>
      </div>

      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-[14px] text-gray-800 mb-3">
          6. 과거 보험 가입이 거절되거나 보험금 지급 거절을 받은 적이 있습니까?
        </h3>
        <div className="flex gap-4">
          {renderRadioOption('insuranceRejection', true, '예')}
          {renderRadioOption('insuranceRejection', false, '아니오')}
        </div>
      </div>
    </div>
  )

  return (
    <div className="w-full flex flex-col items-center gap-2">

      {}
      {currentSubStep === 1 && renderSubStep1()}
      {currentSubStep === 2 && renderSubStep2()}
      {currentSubStep === 3 && renderSubStep3()}

      {}
      <div className="flex gap-3 mt-4">
        {currentSubStep > 1 && (
          <button
            onClick={() => setCurrentSubStep((prev) => (prev - 1) as HealthSubStep)}
            className="px-4 py-2 bg-gray-100 text-gray-700 rounded-[6px] font-['Hana2.0_M'] text-[14px] hover:bg-gray-200 transition-colors duration-200 border border-gray-300"
          >
            ← 이전 문항
          </button>
        )}

        {currentSubStep < 3 && (
          <button
            onClick={() => setCurrentSubStep((prev) => (prev + 1) as HealthSubStep)}
            disabled={!isCurrentSubStepValid()}
            className={`px-4 py-2 rounded-[6px] font-['Hana2.0_M'] text-[14px] transition-all duration-200 border ${
              isCurrentSubStepValid()
                ? 'bg-[#008485] text-white hover:bg-[#006666] cursor-pointer border-[#008485]'
                : 'bg-gray-100 text-gray-400 cursor-not-allowed border-gray-300'
            }`}
          >
            다음 문항 →
          </button>
        )}
      </div>

      {}
      {renderDiseaseDetailsForm()}
    </div>
  )
}

export default Step3HealthInfo