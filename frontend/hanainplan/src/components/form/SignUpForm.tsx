import { useState } from 'react'
import Step1UserTypeSelection from '../signup/Step1UserTypeSelection'
import GeneralSignUp from '../signup/GeneralSignUp'
import CounselorSignUp from '../signup/CounselorSignUp'

interface SignUpFormProps {
  onBackToLogin: () => void;
}

type UserType = 'general' | 'counselor' | null

function SignUpForm({ onBackToLogin }: SignUpFormProps) {
  const [selectedUserType, setSelectedUserType] = useState<UserType>(null)

  const handleUserTypeSelect = (userType: UserType) => {
    setSelectedUserType(userType)
  }

  const handleBackToUserTypeSelection = () => {
    setSelectedUserType(null)
  }

  if (!selectedUserType) {
    return (
      <div className="w-[550px] h-full backdrop-blur-sm p-[10px] flex flex-col items-center justify-center gap-4 rounded-r-[20px] animate-slide-in border-y-2 border-r-2 border-white">
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
        <div className="flex items-center justify-center w-full max-w-[600px] mb-6">
          <div className="flex items-center flex-1">
            {}
            <div className="relative flex items-center justify-center h-12 flex-1 transition-all duration-300 bg-[#008485] text-white rounded-l-lg">
              {}
              <div
                className="absolute right-0 top-1/2 transform translate-x-1/2 -translate-y-1/2 z-10"
                style={{
                  width: 0,
                  height: 0,
                  borderTop: '24px solid transparent',
                  borderBottom: '24px solid transparent',
                  borderLeft: '24px solid #008485'
                }}
              />

              {}
              <div className="flex items-center gap-2 px-3 py-2 relative z-20">
                <div className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold bg-white text-[#008485]">
                  01
                </div>
                <span className="font-['Hana2.0_M'] text-sm font-medium whitespace-nowrap">
                  가입 유형 선택
                </span>
              </div>
            </div>

            {}
            <div className="relative flex items-center justify-center h-12 flex-1 transition-all duration-300 bg-gray-200 text-gray-600">
              {}
              <div
                className="absolute right-0 top-1/2 transform translate-x-1/2 -translate-y-1/2 z-10"
                style={{
                  width: 0,
                  height: 0,
                  borderTop: '24px solid transparent',
                  borderBottom: '24px solid transparent',
                  borderLeft: '24px solid #E5E7EB'
                }}
              />

              {}
              <div className="flex items-center gap-2 px-3 py-2 relative z-20">
                <div className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold bg-gray-300 text-gray-600">
                  02
                </div>
                <span className="font-['Hana2.0_M'] text-sm font-medium whitespace-nowrap">
                  기본 정보
                </span>
              </div>
            </div>

            {}
            <div className="relative flex items-center justify-center h-12 flex-1 transition-all duration-300 bg-gray-200 text-gray-600">
              {}
              <div
                className="absolute right-0 top-1/2 transform translate-x-1/2 -translate-y-1/2 z-10"
                style={{
                  width: 0,
                  height: 0,
                  borderTop: '24px solid transparent',
                  borderBottom: '24px solid transparent',
                  borderLeft: '24px solid #E5E7EB'
                }}
              />

              {}
              <div className="flex items-center gap-2 px-3 py-2 relative z-20">
                <div className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold bg-gray-300 text-gray-600">
                  03
                </div>
                <span className="font-['Hana2.0_M'] text-sm font-medium whitespace-nowrap">
                  추가 정보
                </span>
              </div>
            </div>

            {}
            <div className="relative flex items-center justify-center h-12 flex-1 transition-all duration-300 bg-gray-200 text-gray-600 rounded-r-lg">
              {}
              <div className="flex items-center gap-2 px-3 py-2 relative z-20">
                <div className="w-6 h-6 rounded-full flex items-center justify-center text-xs font-bold bg-gray-300 text-gray-600">
                  04
                </div>
                <span className="font-['Hana2.0_M'] text-sm font-medium whitespace-nowrap">
                  가입 완료
                </span>
              </div>
            </div>
          </div>
        </div>

        {}
        <Step1UserTypeSelection
          selectedUserType={selectedUserType}
          onUserTypeSelect={handleUserTypeSelect}
        />

        {}
        <div className="font-['Hana2.0_M'] text-[16px] leading-[20px] mt-2">
          이미 계정이 있으신가요? <span className="text-[#008485] cursor-pointer hover:text-[#006666] transition-colors duration-200" onClick={onBackToLogin}>로그인하기 →</span>
        </div>
      </div>
    )
  }

  if (selectedUserType === 'general') {
    return <GeneralSignUp onBackToLogin={onBackToLogin} onBackToUserTypeSelection={handleBackToUserTypeSelection} />
  }

  if (selectedUserType === 'counselor') {
    return <CounselorSignUp onBackToLogin={onBackToLogin} onBackToUserTypeSelection={handleBackToUserTypeSelection} />
  }

  return null
}

export default SignUpForm