import { useState } from 'react'

interface ProfessionalInfo {
  specialty: string;
  position: string;
  workPhoneNumber: string;
  workEmail: string;
}

interface Step3CounselorProfessionalInfoProps {
  professionalInfo: ProfessionalInfo;
  onProfessionalInfoChange: (professionalInfo: ProfessionalInfo) => void;
}

const SPECIALTIES = [
  { value: 'PENSION', label: '연금(IRP)' },
  { value: 'FUND', label: '펀드' },
  { value: 'DEPOSIT', label: '예금' },
  { value: 'ASSET', label: '자산관리(종합)' }
]

const POSITIONS = [
  { value: 'JUNIOR', label: '사원' },
  { value: 'SENIOR', label: '대리' },
  { value: 'MANAGER', label: '과장' },
  { value: 'DEPUTY_DIRECTOR', label: '차장' },
  { value: 'DIRECTOR', label: '부장' },
  { value: 'OTHER', label: '기타' }
]

function Step3CounselorProfessionalInfo({ professionalInfo, onProfessionalInfoChange }: Step3CounselorProfessionalInfoProps) {
  const [errors, setErrors] = useState<Partial<ProfessionalInfo>>({})

  const handleInputChange = (field: keyof ProfessionalInfo, value: string) => {
    let formattedValue = value

    if (field === 'workPhoneNumber') {
      formattedValue = formatPhoneNumber(value)
    }

    onProfessionalInfoChange({
      ...professionalInfo,
      [field]: formattedValue
    })

    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }

  const formatPhoneNumber = (value: string) => {
    const numbers = value.replace(/[^\d]/g, '')

    if (numbers.length <= 2) {
      return numbers
    } else if (numbers.length <= 6) {
      return `${numbers.slice(0, 2)}-${numbers.slice(2)}`
    } else if (numbers.length <= 10) {
      return `${numbers.slice(0, 2)}-${numbers.slice(2, 6)}-${numbers.slice(6)}`
    } else {
      return `${numbers.slice(0, 2)}-${numbers.slice(2, 6)}-${numbers.slice(6, 10)}`
    }
  }

  const validateField = (field: keyof ProfessionalInfo, value: string) => {
    let error = ''

    switch (field) {
      case 'specialty':
        if (!value.trim()) error = '전문직종을 선택해주세요.'
        break
      case 'position':
        if (!value.trim()) error = '직책을 선택해주세요.'
        break
      case 'workPhoneNumber':
        if (!value.trim()) {
          error = '업무용 연락처를 입력해주세요.'
        } else if (!/^[0-9-]+$/.test(value) || value.replace(/[^\d]/g, '').length < 9) {
          error = '올바른 전화번호 형식을 입력해주세요.'
        }
        break
      case 'workEmail':
        if (value.trim() && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(value)) {
          error = '올바른 이메일 형식을 입력해주세요.'
        }
        break
    }

    setErrors(prev => ({ ...prev, [field]: error }))
    return !error
  }

  const handleBlur = (field: keyof ProfessionalInfo) => {
    validateField(field, professionalInfo[field])
  }

  return (
    <div className="w-full max-w-md mx-auto p-6">
      <div className="mb-8">
        <h2 className="text-2xl font-['Hana2.0_M'] text-gray-800 mb-2">
          전문직 정보
        </h2>
        <p className="text-gray-600 font-['Hana2.0_M'] text-sm">
          상담사로서의 전문직 정보를 입력해주세요.
        </p>
      </div>

      <div className="space-y-6">
        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            전문직종 <span className="text-red-500">*</span>
          </label>
          <select
            value={professionalInfo.specialty}
            onChange={(e) => handleInputChange('specialty', e.target.value)}
            onBlur={() => handleBlur('specialty')}
            className={`w-full px-4 py-3 border rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485] ${
              errors.specialty ? 'border-red-500' : 'border-gray-300'
            }`}
          >
            <option value="">전문직종을 선택해주세요</option>
            {SPECIALTIES.map((specialty) => (
              <option key={specialty.value} value={specialty.value}>
                {specialty.label}
              </option>
            ))}
          </select>
          {errors.specialty && (
            <p className="mt-1 text-sm text-red-500 font-['Hana2.0_M']">
              {errors.specialty}
            </p>
          )}
        </div>

        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            직책 <span className="text-red-500">*</span>
          </label>
          <select
            value={professionalInfo.position}
            onChange={(e) => handleInputChange('position', e.target.value)}
            onBlur={() => handleBlur('position')}
            className={`w-full px-4 py-3 border rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485] ${
              errors.position ? 'border-red-500' : 'border-gray-300'
            }`}
          >
            <option value="">직책을 선택해주세요</option>
            {POSITIONS.map((position) => (
              <option key={position.value} value={position.value}>
                {position.label}
              </option>
            ))}
          </select>
          {errors.position && (
            <p className="mt-1 text-sm text-red-500 font-['Hana2.0_M']">
              {errors.position}
            </p>
          )}
        </div>

        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            업무용 연락처 <span className="text-red-500">*</span>
          </label>
          <input
            type="tel"
            value={professionalInfo.workPhoneNumber}
            onChange={(e) => handleInputChange('workPhoneNumber', e.target.value)}
            onBlur={() => handleBlur('workPhoneNumber')}
            placeholder="예: 02-1234-5678"
            className={`w-full px-4 py-3 border rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485] ${
              errors.workPhoneNumber ? 'border-red-500' : 'border-gray-300'
            }`}
          />
          {errors.workPhoneNumber && (
            <p className="mt-1 text-sm text-red-500 font-['Hana2.0_M']">
              {errors.workPhoneNumber}
            </p>
          )}
        </div>

        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            업무용 이메일
          </label>
          <input
            type="email"
            value={professionalInfo.workEmail}
            onChange={(e) => handleInputChange('workEmail', e.target.value)}
            onBlur={() => handleBlur('workEmail')}
            placeholder="예: counselor@hanabank.com (선택사항)"
            className={`w-full px-4 py-3 border rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485] ${
              errors.workEmail ? 'border-red-500' : 'border-gray-300'
            }`}
          />
          {errors.workEmail && (
            <p className="mt-1 text-sm text-red-500 font-['Hana2.0_M']">
              {errors.workEmail}
            </p>
          )}
        </div>
      </div>

      {}
      <div className="mt-6 p-4 bg-blue-50 rounded-lg">
        <div className="flex items-start">
          <div className="flex-shrink-0">
            <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
              <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
            </svg>
          </div>
          <div className="ml-3">
            <p className="text-sm text-blue-700 font-['Hana2.0_M']">
              업무용 연락처와 이메일은 고객 상담 시 사용될 정보입니다.
              정확한 정보를 입력해주세요.
            </p>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Step3CounselorProfessionalInfo