import { useState, useEffect } from 'react'
import { getIndustries, searchIndustries, type IndustryData } from '../../api/userApi'

interface JobInfo {
  industryCode: string
  industryName: string
  careerYears: number | null
  assetLevel: string
}

interface Step4Props {
  jobInfo: JobInfo
  onJobInfoChange: (jobInfo: JobInfo) => void
}

type JobSubStep = 1 | 2

function Step4JobInfo({ jobInfo, onJobInfoChange }: Step4Props) {
  const [currentSubStep, setCurrentSubStep] = useState<JobSubStep>(1)
  const [searchKeyword, setSearchKeyword] = useState('')
  const [industryList, setIndustryList] = useState<IndustryData[]>([])
  const [filteredIndustries, setFilteredIndustries] = useState<IndustryData[]>([])
  const [showDropdown, setShowDropdown] = useState(false)
  const [isInputFocused, setIsInputFocused] = useState(false)

  useEffect(() => {
    const loadIndustries = async () => {
      try {
        const data = await getIndustries()
        setIndustryList(data)
      } catch (error) {
        setIndustryList([])
      }
    }

    loadIndustries()
  }, [])

  useEffect(() => {
    if (searchKeyword.trim() === '') {
      if (isInputFocused) {
        setFilteredIndustries(industryList)
        setShowDropdown(true)
      } else {
        setFilteredIndustries([])
        setShowDropdown(false)
      }
      return
    }

    const searchIndustriesHandler = async () => {
      try {
        const data = await searchIndustries(searchKeyword)
        setFilteredIndustries(data)
        setShowDropdown(true)
      } catch (error) {
        setFilteredIndustries([])
        setShowDropdown(false)
      }
    }

    const timeoutId = setTimeout(searchIndustriesHandler, 500)
    return () => clearTimeout(timeoutId)
  }, [searchKeyword, isInputFocused, industryList])

  const isSubStep1Valid = () => {
    return jobInfo.industryCode.trim() !== '' &&
           jobInfo.industryName.trim() !== '' &&
           jobInfo.careerYears !== null
  }

  const isSubStep2Valid = () => {
    return jobInfo.assetLevel.trim() !== ''
  }

  const isCurrentSubStepValid = () => {
    switch (currentSubStep) {
      case 1: return isSubStep1Valid()
      case 2: return isSubStep2Valid()
      default: return false
    }
  }

  const handleIndustrySelect = (industry: IndustryData) => {
    onJobInfoChange({
      ...jobInfo,
      industryCode: industry.industryCode,
      industryName: industry.industryName
    })
    setSearchKeyword(industry.industryName)
    setShowDropdown(false)
    setIsInputFocused(false)
  }

  const handleInputFocus = () => {
    setIsInputFocused(true)
    if (searchKeyword.trim() === '') {
      setFilteredIndustries(industryList)
      setShowDropdown(true)
    }
  }

  const handleInputBlur = () => {
    setTimeout(() => {
      setIsInputFocused(false)
      if (searchKeyword.trim() === '') {
        setShowDropdown(false)
      }
    }, 150)
  }

  const handleCareerYearsChange = (years: number) => {
    onJobInfoChange({
      ...jobInfo,
      careerYears: years
    })
  }

  const handleAssetLevelChange = (level: string) => {
    onJobInfoChange({
      ...jobInfo,
      assetLevel: level
    })
  }

  const assetLevels = [
    { value: 'under_1', label: '1천만원 미만' },
    { value: '1_to_5', label: '1천만원 ~ 5천만원' },
    { value: '5_to_10', label: '5천만원 ~ 1억원' },
    { value: '10_to_30', label: '1억원 ~ 3억원' },
    { value: '30_to_50', label: '3억원 ~ 5억원' },
    { value: 'over_50', label: '5억원 이상' },
  ]

  const careerYearOptions = [
    { value: 0, label: '1년 미만' },
    { value: 1, label: '1년 ~ 3년' },
    { value: 3, label: '3년 ~ 5년' },
    { value: 5, label: '5년 ~ 10년' },
    { value: 10, label: '10년 ~ 20년' },
    { value: 20, label: '20년 ~ 30년' },
    { value: 30, label: '30년 이상' },
  ]

  const renderSubStep1 = () => (
    <div className="w-[480px] space-y-6 px-2 max-h-[260px] overflow-y-auto">
      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-lg text-gray-800 mb-3">
          종사 직종 선택
        </h3>
        <div className="relative">
          <input
            type="text"
            placeholder="직종명을 검색하세요 (예: 교사, 공무원, 제조업 등)"
            value={searchKeyword}
            onChange={(e) => setSearchKeyword(e.target.value)}
            onFocus={handleInputFocus}
            onBlur={handleInputBlur}
            className="w-full p-3 border border-gray-300 rounded-[8px] font-['Hana2.0_M'] text-[14px] focus:outline-none focus:border-[#008485]"
          />

          {}
          {showDropdown && filteredIndustries.length > 0 && (
            <div className="absolute top-full left-0 right-0 bg-white border border-gray-300 rounded-[8px] mt-1 max-h-[200px] overflow-y-auto z-10 shadow-lg">
              {filteredIndustries.map((industry) => (
                <div
                  key={industry.industryCode}
                  onClick={() => handleIndustrySelect(industry)}
                  className="p-3 hover:bg-gray-50 cursor-pointer border-b border-gray-100 last:border-b-0"
                >
                  <div className="flex justify-between items-center">
                    <span className="font-['Hana2.0_M'] text-[14px] text-gray-800">
                      {industry.industryName}
                    </span>
                    <span className={`px-2 py-1 rounded text-[12px] font-['Hana2.0_M'] ${
                      industry.riskLevel === '상' ? 'bg-red-100 text-red-600' :
                      industry.riskLevel === '중' ? 'bg-yellow-100 text-yellow-600' :
                      'bg-green-100 text-green-600'
                    }`}>
                      {industry.riskLevel === '상' ? '고' : industry.riskLevel === '하' ? '저' : industry.riskLevel}위험
                    </span>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        {}
        {jobInfo?.industryName && (
          <div className="mt-3 p-3 bg-[#008485]/10 rounded-[8px]">
            <span className="font-['Hana2.0_M'] text-[14px] text-[#008485]">
              선택된 직종: {jobInfo?.industryName}
            </span>
          </div>
        )}
      </div>

      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-lg text-gray-800 mb-3">
          재직 기간
        </h3>
        <div className="grid grid-cols-2 gap-2">
          {careerYearOptions.map((option) => (
            <label key={option.value} className="flex items-center cursor-pointer p-2 rounded-[6px] hover:bg-gray-50">
              <input
                type="radio"
                name="careerYears"
                checked={jobInfo?.careerYears === option.value}
                onChange={() => handleCareerYearsChange(option.value)}
                className="sr-only"
              />
              <div className={`w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center ${
                jobInfo?.careerYears === option.value
                  ? 'border-[#008485] bg-[#008485]'
                  : 'border-gray-300'
              }`}>
                {jobInfo?.careerYears === option.value && (
                  <div className="w-2 h-2 rounded-full bg-white"></div>
                )}
              </div>
              <span className="font-['Hana2.0_M'] text-[13px] text-gray-700">{option.label}</span>
            </label>
          ))}
        </div>
      </div>
    </div>
  )

  const renderSubStep2 = () => (
    <div className="w-[480px] space-y-6 px-2 max-h-[260px] overflow-y-auto">
      {}
      <div className="bg-white rounded-[12px] p-4 border border-gray-200 shadow-sm">
        <h3 className="font-['Hana2.0_M'] text-lg text-gray-800 mb-3">
          현재 자산 수준
        </h3>
        <div className="space-y-2">
          {assetLevels.map((level) => (
            <label key={level.value} className="flex items-center cursor-pointer p-2 rounded-[6px] hover:bg-gray-50">
              <input
                type="radio"
                name="assetLevel"
                checked={jobInfo?.assetLevel === level.value}
                onChange={() => handleAssetLevelChange(level.value)}
                className="sr-only"
              />
              <div className={`w-4 h-4 rounded-full border-2 mr-2 flex items-center justify-center ${
                jobInfo?.assetLevel === level.value
                  ? 'border-[#008485] bg-[#008485]'
                  : 'border-gray-300'
              }`}>
                {jobInfo?.assetLevel === level.value && (
                  <div className="w-2 h-2 rounded-full bg-white"></div>
                )}
              </div>
              <span className="font-['Hana2.0_M'] text-[13px] text-gray-700">{level.label}</span>
            </label>
          ))}
        </div>
      </div>
    </div>
  )

  return (
    <div className="w-full flex flex-col items-center gap-2">

      {}
      {currentSubStep === 1 && renderSubStep1()}
      {currentSubStep === 2 && renderSubStep2()}

      {}
      <div className="flex gap-3 mt-4">
        {currentSubStep > 1 && (
          <button
            onClick={() => setCurrentSubStep((prev) => (prev - 1) as JobSubStep)}
            className="px-4 py-2 bg-gray-100 text-gray-700 rounded-[6px] font-['Hana2.0_M'] text-[14px] hover:bg-gray-200 transition-colors duration-200 border border-gray-300"
          >
            ← 이전 문항
          </button>
        )}

        {currentSubStep < 2 && (
          <button
            onClick={() => setCurrentSubStep((prev) => (prev + 1) as JobSubStep)}
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
    </div>
  )
}

export default Step4JobInfo