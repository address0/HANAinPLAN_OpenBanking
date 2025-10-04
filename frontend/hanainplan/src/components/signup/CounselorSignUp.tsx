import { useState } from 'react'
import Step2BasicInfo from './Step2BasicInfo'
import Step3CounselorProfessionalInfo from './Step3CounselorProfessionalInfo.tsx'
import Step4CounselorBranchInfo from './Step4CounselorBranchInfo.tsx'
import Step5CounselorVerification from './Step5CounselorVerification.tsx'
import Step6Complete from './Step6Complete'

interface CounselorSignUpProps {
  onBackToLogin: () => void;
  onBackToUserTypeSelection: () => void;
}

type SignUpStep = 2 | 3 | 4 | 5 | 6

interface SignUpData {
  name: string;
  socialNumber: string;
  phoneNumber: string;
  verificationCode: string;
  ci?: string;
  isPhoneVerified?: boolean;
  password: string;
  confirmPassword: string;
  professionalInfo: {
    specialty: string;
    position: string;
    workPhoneNumber: string;
    workEmail: string;
  };
  branchInfo: {
    branchCode: string;
    branchName: string;
    address: string;
    coordinates: {
      latitude: number;
      longitude: number;
    };
  };
  verificationInfo: {
    employeeId: string;
    verificationDocuments: File[];
    additionalNotes: string;
  };
}

function CounselorSignUp({ onBackToLogin, onBackToUserTypeSelection }: CounselorSignUpProps) {
  const [currentStep, setCurrentStep] = useState<SignUpStep>(2) // 유형 선택 후부터 시작
  const [signUpData, setSignUpData] = useState<SignUpData>({
    name: '',
    socialNumber: '',
    phoneNumber: '',
    verificationCode: '',
    password: '',
    confirmPassword: '',
    professionalInfo: {
      specialty: '',
      position: '',
      workPhoneNumber: '',
      workEmail: ''
    },
    branchInfo: {
      branchCode: '',
      branchName: '',
      address: '',
      coordinates: {
        latitude: 0,
        longitude: 0
      }
    },
    verificationInfo: {
      employeeId: '',
      verificationDocuments: [],
      additionalNotes: ''
    }
  })

  // 데이터 변경 핸들러
  const handleBasicInfoChange = (data: Partial<SignUpData>) => {
    setSignUpData(prev => ({ ...prev, ...data }))
  }

  const handleProfessionalInfoChange = (professionalInfo: SignUpData['professionalInfo']) => {
    setSignUpData(prev => ({ ...prev, professionalInfo }))
  }

  const handleBranchInfoChange = (branchInfo: SignUpData['branchInfo']) => {
    setSignUpData(prev => ({ ...prev, branchInfo }))
  }

  const handleVerificationInfoChange = (verificationInfo: SignUpData['verificationInfo']) => {
    setSignUpData(prev => ({ ...prev, verificationInfo }))
  }

  // 유효성 검사
  const isStep2Valid = () => {
    const isNameValid = signUpData.name.trim().length >= 2
    const isSocialNumberValid = signUpData.socialNumber.replace(/[^\d]/g, '').length === 13
    const isCiVerified = !!signUpData.ci
    const isPhoneVerified = signUpData.isPhoneVerified === true
    return isNameValid && isSocialNumberValid && isCiVerified && isPhoneVerified
  }

  const isStep3Valid = () => {
    const { professionalInfo } = signUpData
    return professionalInfo.specialty.trim() !== '' && 
           professionalInfo.position.trim() !== '' && 
           professionalInfo.workPhoneNumber.trim() !== ''
  }

  const isStep4Valid = () => {
    const { branchInfo } = signUpData
    return branchInfo.branchCode.trim() !== '' && 
           branchInfo.branchName.trim() !== '' && 
           branchInfo.address.trim() !== ''
  }

  const isStep5Valid = () => {
    const { verificationInfo } = signUpData
    return verificationInfo.employeeId.trim() !== '' && 
           verificationInfo.verificationDocuments.length > 0
  }

  // 화살표 형태 진행 상태 표시기
  const renderArrowProgressBar = () => {
    const steps = [
      { number: 1, name: '가입 유형 선택' },
      { number: 2, name: '기본 정보' },
      { number: 3, name: '추가 정보' },
      { number: 4, name: '가입 완료' }
    ]

    const getCurrentStepIndex = () => {
      if (currentStep === 2) return 1
      if (currentStep >= 3 && currentStep <= 5) return 2
      if (currentStep === 6) return 3
      return 0
    }

    return (
      <div className="flex items-center justify-center w-full max-w-[500px] mb-6">
        {steps.map((step, index) => {
          const isActive = index === getCurrentStepIndex()
          const isCompleted = index < getCurrentStepIndex()
          
          return (
            <div key={step.number} className="flex items-center flex-1">
              {/* 화살표 단계 컴포넌트 */}
              <div className={`relative flex items-center justify-center h-12 flex-1 transition-all duration-300 ${
                isActive 
                  ? 'bg-[#008485] text-white' 
                  : isCompleted 
                    ? 'bg-gray-400 text-white'
                    : 'bg-gray-200 text-gray-600'
              } ${
                index === 0 
                  ? 'rounded-l-lg' 
                  : index === steps.length - 1 
                    ? 'rounded-r-lg'
                    : ''
              }`}>
                {/* 화살표 모양 생성 */}
                {index < steps.length - 1 && (
                  <div 
                    className={`absolute right-0 top-1/2 transform translate-x-1/2 -translate-y-1/2 z-10`}
                    style={{
                      width: 0,
                      height: 0,
                      borderTop: '24px solid transparent',
                      borderBottom: '24px solid transparent',
                      borderLeft: `24px solid ${
                        isActive 
                          ? '#008485' 
                          : isCompleted 
                            ? '#9CA3AF'
                            : '#E5E7EB'
                      }`
                    }}
                  />
                )}
                
                {/* 단계 번호와 이름 */}
                <div className="flex items-center gap-2 px-4 py-2 relative z-20">
                  <div className={`w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold ${
                    isActive || isCompleted ? 'bg-white text-[#008485]' : 'bg-gray-300 text-gray-600'
                  }`}>
                    {isCompleted ? '✓' : step.number.toString().padStart(2, '0')}
                  </div>
                  <span className="font-['Hana2.0_M'] text-sm font-medium whitespace-nowrap">
                    {step.name}
                  </span>
                </div>
              </div>
            </div>
          )
        })}
      </div>
    )
  }

  // 현재 단계 유효성 검사
  const isCurrentStepValid = () => {
    switch (currentStep) {
      case 2: return isStep2Valid()
      case 3: return isStep3Valid()
      case 4: return isStep4Valid()
      case 5: return isStep5Valid()
      case 6: return true
      default: return false
    }
  }

  return (
    <div className="w-[600px] h-full backdrop-blur-sm p-[10px] flex flex-col items-center justify-center gap-4 rounded-r-[20px] shadow-[4px_0px_20px_rgba(0,0,0,0.25)] animate-slide-in border-y-2 border-r-2 border-white overflow-hidden">
      {/* 공통 헤더 */}
      <div className="h-[38px] relative flex items-center">
        <img 
          src="/images/img-hana-symbol.png"
          alt="하나 심볼"
          className="h-[38px] left-0 right-[82.76%] top-0"
        />
        <span className="ml-2 top-[7.89%] font-['Hana2.0_M'] text-[24px] leading-[31px]">
          하나인플랜 회원가입
        </span>
      </div>

      {/* 화살표 단계 표시기 */}
      {renderArrowProgressBar()}

      {/* 단계별 컴포넌트 */}
      <div className="w-full h-[400px] overflow-y-auto">
        {currentStep === 2 && (
          <Step2BasicInfo
            signUpData={signUpData}
            onDataChange={handleBasicInfoChange}
            isValid={isStep2Valid()}
          />
        )}
        {currentStep === 3 && (
          <Step3CounselorProfessionalInfo
            professionalInfo={signUpData.professionalInfo}
            onProfessionalInfoChange={handleProfessionalInfoChange}
          />
        )}
        {currentStep === 4 && (
          <Step4CounselorBranchInfo
            branchInfo={signUpData.branchInfo}
            onBranchInfoChange={handleBranchInfoChange}
          />
        )}
        {currentStep === 5 && (
          <Step5CounselorVerification
            verificationInfo={signUpData.verificationInfo}
            onVerificationInfoChange={handleVerificationInfoChange}
          />
        )}
        {currentStep === 6 && (
          <Step6Complete
            userType="counselor"
            userName={signUpData.name}
          />
        )}
      </div>

      {/* 네비게이션 버튼들 */}
      {currentStep < 6 && (
        <div className="flex gap-4 mt-2">
          {/* 2단계일 때는 유형 선택으로, 그 이후엔 이전 단계로 */}
          <button 
            onClick={currentStep === 2 ? onBackToUserTypeSelection : () => setCurrentStep((prev) => (prev - 1) as SignUpStep)}
            className="px-6 py-3 bg-gray-500 text-white rounded-[10px] font-['Hana2.0_M'] text-[16px] hover:bg-gray-600 transition-colors duration-200"
          >
            이전 단계
          </button>
          <button 
            onClick={() => setCurrentStep((prev) => (prev + 1) as SignUpStep)}
            disabled={!isCurrentStepValid()}
            className={`px-6 py-3 rounded-[10px] font-['Hana2.0_M'] text-[16px] transition-all duration-200 ${
              isCurrentStepValid()
                ? 'bg-[#008485] text-white hover:bg-[#006666] cursor-pointer'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            다음 단계
          </button>
        </div>
      )}

      {/* 로그인 링크 */}
      <div className="font-['Hana2.0_M'] text-[16px] leading-[20px] mt-2">
        이미 계정이 있으신가요? <span className="text-[#008485] cursor-pointer hover:text-[#006666] transition-colors duration-200" onClick={onBackToLogin}>로그인하기 →</span>
      </div>
    </div>
  )
}

export default CounselorSignUp 