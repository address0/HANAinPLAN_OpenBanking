import { useState } from 'react'
import Step2BasicInfo from './Step2BasicInfo'
import Step3HealthInfo from './Step3HealthInfo'
import Step4JobInfo from './Step4JobInfo'
import Step5PasswordSetup from './Step5PasswordSetup'
import Step6Complete from './Step6Complete'
import { signUp, type SignUpRequest } from '../../api/userApi'
import AlertModal from '../modal/AlertModal'
import MyDataConsentModal from '../modal/MyDataConsentModal'

interface GeneralSignUpProps {
  onBackToLogin: () => void;
  onBackToUserTypeSelection: () => void;
}

type SignUpStep = 1 | 2 | 3 | 4 | 5 | 6

interface SignUpData {
  name: string;
  socialNumber: string;
  phoneNumber: string;
  verificationCode: string;
  email?: string;
  ci?: string;
  isPhoneVerified?: boolean;
  password: string;
  confirmPassword: string;
  healthInfo: {
    recentMedicalAdvice: boolean | null;
    recentHospitalization: boolean | null;
    majorDisease: boolean | null;
    diseaseDetails: Array<{
      diseaseCode: string;
      diseaseName: string;
      diseaseCategory: string;
      riskLevel: string;
      severity: 'mild' | 'moderate' | 'severe' | null;
      progressPeriod: 'under_1month' | '1_3months' | '3_6months' | '6_12months' | 'over_1year' | null;
      isChronic: boolean | null;
      description?: string;
    }>;
    longTermMedication: boolean | null;
    disabilityRegistered: boolean | null;
    insuranceRejection: boolean | null;
  };
  jobInfo: {
    industryCode: string;
    industryName: string;
    careerYears: number | null;
    assetLevel: string;
  };
}

function GeneralSignUp({ onBackToLogin, onBackToUserTypeSelection }: GeneralSignUpProps) {
  const [currentStep, setCurrentStep] = useState<SignUpStep>(2)
  const [isSubmitting, setIsSubmitting] = useState(false)

  const [alertModal, setAlertModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    type: 'info' as 'success' | 'error' | 'info' | 'warning'
  })

  const [myDataModal, setMyDataModal] = useState({
    isOpen: false,
    consent: false,
    bankAccountInfo: [] as any[]
  })
  const [signUpData, setSignUpData] = useState<SignUpData>({
    name: '',
    socialNumber: '',
    phoneNumber: '',
    verificationCode: '',
    email: '',
    password: '',
    confirmPassword: '',
    healthInfo: {
      recentMedicalAdvice: null,
      recentHospitalization: null,
      majorDisease: null,
      diseaseDetails: [],
      longTermMedication: null,
      disabilityRegistered: null,
      insuranceRejection: null
    },
    jobInfo: {
      industryCode: '',
      industryName: '',
      careerYears: null,
      assetLevel: ''
    }
  })

  const handleBasicInfoChange = (data: Partial<SignUpData>) => {
    setSignUpData(prev => ({ ...prev, ...data }))
  }

  const handleHealthInfoChange = (field: keyof SignUpData['healthInfo'], value: any) => {
    setSignUpData(prev => ({
      ...prev,
      healthInfo: {
        ...prev.healthInfo,
        [field]: value
      }
    }))
  }

  const handleJobInfoChange = (jobInfo: SignUpData['jobInfo']) => {
    setSignUpData(prev => ({ ...prev, jobInfo }))
  }

  const isStep2Valid = () => {
    const isNameValid = signUpData.name.trim().length >= 2
    const isSocialNumberValid = signUpData.socialNumber.replace(/[^\d]/g, '').length === 13
    const isCiVerified = !!signUpData.ci
    const isPhoneVerified = signUpData.isPhoneVerified === true
    return isNameValid && isSocialNumberValid && isCiVerified && isPhoneVerified
  }

  const isStep3Valid = () => {
    const { healthInfo } = signUpData
    const requiredFields = [
      healthInfo.recentMedicalAdvice,
      healthInfo.recentHospitalization,
      healthInfo.majorDisease,
      healthInfo.longTermMedication,
      healthInfo.disabilityRegistered,
      healthInfo.insuranceRejection
    ]

    const basicFieldsValid = requiredFields.every(value => value !== null)

    if (healthInfo.majorDisease === true) {
      return basicFieldsValid && healthInfo.diseaseDetails.length > 0
    }

    return basicFieldsValid
  }

  const isStep4Valid = () => {
    const { jobInfo } = signUpData
    return jobInfo.industryCode.trim() !== '' &&
           jobInfo.industryName.trim() !== '' &&
           jobInfo.careerYears !== null &&
           jobInfo.assetLevel.trim() !== ''
  }

  const isStep5Valid = () => {
    const { password, confirmPassword } = signUpData
    const isPasswordValid = password.length >= 8 && password.length <= 20 &&
                           /\d/.test(password) && /[a-zA-Z]/.test(password) &&
                           /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password)
    const isConfirmValid = password === confirmPassword && password.length > 0
    return isPasswordValid && isConfirmValid
  }

  const handleSignUp = async () => {
    if (!isCurrentStepValid()) return

    setIsSubmitting(true)
    try {
      const request: SignUpRequest = {
        userType: 'GENERAL',
        name: signUpData.name,
        socialNumber: signUpData.socialNumber,
        phoneNumber: signUpData.phoneNumber,
        verificationCode: signUpData.verificationCode,
        ci: signUpData.ci,
        email: signUpData.email,
        password: signUpData.password,
        confirmPassword: signUpData.confirmPassword,
        healthInfo: {
          recentMedicalAdvice: signUpData.healthInfo.recentMedicalAdvice!,
          recentHospitalization: signUpData.healthInfo.recentHospitalization!,
          majorDisease: signUpData.healthInfo.majorDisease!,
          diseaseDetails: signUpData.healthInfo.diseaseDetails.map(detail => ({
            ...detail,
            severity: detail.severity || 'mild',
            progressPeriod: detail.progressPeriod || 'under_1month',
            isChronic: detail.isChronic || false
          })),
          longTermMedication: signUpData.healthInfo.longTermMedication!,
          disabilityRegistered: signUpData.healthInfo.disabilityRegistered!,
          insuranceRejection: signUpData.healthInfo.insuranceRejection!
        },
        jobInfo: {
          industryCode: signUpData.jobInfo.industryCode,
          industryName: signUpData.jobInfo.industryName,
          careerYears: signUpData.jobInfo.careerYears!,
          assetLevel: signUpData.jobInfo.assetLevel
        }
      }

      const response = await signUp(request)

      if (response.userId) {
        setCurrentStep(6)
        setAlertModal({
          isOpen: true,
          title: '회원가입 완료',
          message: '하나인플랜에 가입해주셔서 감사합니다!',
          type: 'success'
        })

        if (myDataModal.consent && myDataModal.bankAccountInfo.length > 0) {
          try {
            const accountResponse = await fetch(`/api/user/account/${response.userId}/save-accounts`, {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({
                userId: response.userId,
                bankAccountInfo: myDataModal.bankAccountInfo
              }),
            });

            if (accountResponse.ok) {
            } else {
            }
          } catch (error) {
          }
        }
      } else {
        setAlertModal({
          isOpen: true,
          title: '가입 실패',
          message: response.message || '회원가입에 실패했습니다.',
          type: 'error'
        })
      }
    } catch (error) {
      setAlertModal({
        isOpen: true,
        title: '가입 실패',
        message: '회원가입 처리 중 오류가 발생했습니다.',
        type: 'error'
      })
    } finally {
      setIsSubmitting(false)
    }
  }

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
              {}
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
                {}
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

                {}
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

  const handleMyDataConsent = async (consent: boolean, bankAccountInfo?: any[]) => {
    setMyDataModal(prev => ({
      ...prev,
      isOpen: false,
      consent: consent,
      bankAccountInfo: bankAccountInfo || []
    }))

    setCurrentStep(3)
  }

  const handleNextStep = () => {
    if (currentStep === 2) {
      setMyDataModal(prev => ({ ...prev, isOpen: true }))
    } else {
      setCurrentStep((prev) => (prev + 1) as SignUpStep)
    }
  }

  return (
    <div className="w-[600px] h-full backdrop-blur-sm p-[10px] flex flex-col items-center justify-center gap-4 rounded-r-[20px] shadow-[4px_0px_20px_rgba(0,0,0,0.25)] animate-slide-in border-y-2 border-r-2 border-white overflow-hidden">
      {}
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

      {}
      {renderArrowProgressBar()}

      {}
      <div className="w-full h-[400px] overflow-y-auto">
        {currentStep === 2 && (
          <Step2BasicInfo
            signUpData={signUpData}
            onDataChange={handleBasicInfoChange}
            isValid={isStep2Valid()}
          />
        )}
        {currentStep === 3 && (
          <Step3HealthInfo
            healthInfo={signUpData.healthInfo}
            onHealthInfoChange={handleHealthInfoChange}
          />
        )}
        {currentStep === 4 && (
          <Step4JobInfo
            jobInfo={signUpData.jobInfo}
            onJobInfoChange={handleJobInfoChange}
          />
        )}
        {currentStep === 5 && (
          <Step5PasswordSetup
            signUpData={{
              password: signUpData.password,
              confirmPassword: signUpData.confirmPassword
            }}
            onDataChange={handleBasicInfoChange}
          />
        )}
        {currentStep === 6 && (
          <Step6Complete
            userType="general"
            userName={signUpData.name}
          />
        )}
      </div>

      {}
      {currentStep < 6 && (
        <div className="flex gap-4 mt-2">
          {}
          <button
            onClick={currentStep === 2 ? onBackToUserTypeSelection : () => setCurrentStep((prev) => (prev - 1) as SignUpStep)}
            className="px-6 py-3 bg-gray-500 text-white rounded-[10px] font-['Hana2.0_M'] text-[16px] hover:bg-gray-600 transition-colors duration-200"
          >
            이전 단계
          </button>
          <button
            onClick={currentStep === 5 ? handleSignUp : handleNextStep}
            disabled={!isCurrentStepValid() || isSubmitting}
            className={`px-6 py-3 rounded-[10px] font-['Hana2.0_M'] text-[16px] transition-all duration-200 ${
              isCurrentStepValid() && !isSubmitting
                ? 'bg-[#008485] text-white hover:bg-[#006666] cursor-pointer'
                : 'bg-gray-300 text-gray-500 cursor-not-allowed'
            }`}
          >
            {isSubmitting ? '처리중...' : currentStep === 5 ? '가입 완료' : '다음 단계'}
          </button>
        </div>
      )}

      {}
      <div className="font-['Hana2.0_M'] text-[16px] leading-[20px] mt-2">
        이미 계정이 있으신가요? <span className="text-[#008485] cursor-pointer hover:text-[#006666] transition-colors duration-200" onClick={onBackToLogin}>로그인하기 →</span>
      </div>

      {}
      <AlertModal
        isOpen={alertModal.isOpen}
        onClose={() => setAlertModal(prev => ({ ...prev, isOpen: false }))}
        title={alertModal.title}
        message={alertModal.message}
        type={alertModal.type}
        autoClose={alertModal.type === 'success'}
        autoCloseDelay={3000}
      />

      {}
      <MyDataConsentModal
        isOpen={myDataModal.isOpen}
        onClose={() => setMyDataModal(prev => ({ ...prev, isOpen: false }))}
        onConsent={handleMyDataConsent}
        personalInfo={{
          phoneNumber: signUpData.phoneNumber,
          socialNumber: signUpData.socialNumber,
          name: signUpData.name
        }}
      />
    </div>
  )
}

export default GeneralSignUp