import { useState } from 'react'

interface Step5PasswordSetupProps {
  signUpData: {
    password: string;
    confirmPassword: string;
  };
  onDataChange: (data: { password: string; confirmPassword: string }) => void;
}

function Step5PasswordSetup({ signUpData, onDataChange }: Step5PasswordSetupProps) {
  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)
  const [focusedField, setFocusedField] = useState<string | null>(null)

  const handlePasswordChange = (password: string) => {
    onDataChange({ ...signUpData, password })
  }

  const handleConfirmPasswordChange = (confirmPassword: string) => {
    onDataChange({ ...signUpData, confirmPassword })
  }

  const handleKakaoLogin = () => {
  }

  const getPasswordValidation = () => {
    const { password } = signUpData
    const validations = {
      length: password.length >= 8 && password.length <= 20,
      hasNumber: /\d/.test(password),
      hasLetter: /[a-zA-Z]/.test(password),
      hasSpecial: /[!@#$%^&*()_+\-=\[\]{};':"\\|,.<>\/?]/.test(password),
    }

    return validations
  }

  const getConfirmPasswordValidation = () => {
    const { password, confirmPassword } = signUpData
    return {
      matches: password === confirmPassword && password.length > 0,
      notEmpty: confirmPassword.length > 0
    }
  }

  const validation = getPasswordValidation()
  const confirmValidation = getConfirmPasswordValidation()
  const isPasswordValid = Object.values(validation).every(Boolean)
  const isConfirmPasswordValid = confirmValidation.matches

  return (
    <div className="w-full flex flex-col items-center gap-[20px]">

      <div className="w-full max-w-[400px] flex flex-col gap-[20px]">
        {}
        <div className="flex flex-col gap-2">
          <label className="font-['Hana2.0_M'] text-[14px] leading-[18px] text-gray-700">
            비밀번호
          </label>
          <div className="relative">
            <input
              type={showPassword ? 'text' : 'password'}
              value={signUpData.password}
              onChange={(e) => handlePasswordChange(e.target.value)}
              onFocus={() => setFocusedField('password')}
              onBlur={() => setFocusedField(null)}
              placeholder="8-20자, 영문, 숫자, 특수문자 조합"
              className={`w-full h-[50px] px-4 pr-12 border-2 rounded-[10px] font-['Hana2.0_M'] text-[16px] transition-all duration-200 ${
                focusedField === 'password'
                  ? 'border-[#008485] bg-white'
                  : signUpData.password.length > 0
                    ? isPasswordValid
                      ? 'border-green-500 bg-green-50'
                      : 'border-red-500 bg-red-50'
                    : 'border-gray-300 bg-gray-50'
              }`}
            />
            <button
              type="button"
              onClick={() => setShowPassword(!showPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors duration-200"
            >
              {showPassword ? (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              )}
            </button>
          </div>

          {}
          {signUpData.password.length > 0 && (
            <div className="flex flex-col gap-1 text-[12px] font-['Hana2.0_M'] grid grid-cols-2">
              <div className={`flex items-center gap-1 col-span-1 ${validation.length ? 'text-green-600' : 'text-red-500'}`}>
                <span>{validation.length ? '✓' : '✗'}</span>
                <span>8-20자 길이</span>
              </div>
              <div className={`flex items-center gap-1 col-span-1 ${validation.hasLetter ? 'text-green-600' : 'text-red-500'}`}>
                <span>{validation.hasLetter ? '✓' : '✗'}</span>
                <span>영문자 포함</span>
              </div>
              <div className={`flex items-center gap-1 ${validation.hasNumber ? 'text-green-600' : 'text-red-500'}`}>
                <span>{validation.hasNumber ? '✓' : '✗'}</span>
                <span>숫자 포함</span>
              </div>
              <div className={`flex items-center gap-1 ${validation.hasSpecial ? 'text-green-600' : 'text-red-500'}`}>
                <span>{validation.hasSpecial ? '✓' : '✗'}</span>
                <span>특수문자 포함</span>
              </div>
            </div>
          )}
        </div>

        {}
        <div className="flex flex-col gap-2">
          <label className="font-['Hana2.0_M'] text-[14px] leading-[18px] text-gray-700">
            비밀번호 확인
          </label>
          <div className="relative">
            <input
              type={showConfirmPassword ? 'text' : 'password'}
              value={signUpData.confirmPassword}
              onChange={(e) => handleConfirmPasswordChange(e.target.value)}
              onFocus={() => setFocusedField('confirmPassword')}
              onBlur={() => setFocusedField(null)}
              placeholder="비밀번호를 다시 입력하세요"
              className={`w-full h-[50px] px-4 pr-12 border-2 rounded-[10px] font-['Hana2.0_M'] text-[16px] transition-all duration-200 ${
                focusedField === 'confirmPassword'
                  ? 'border-[#008485] bg-white'
                  : signUpData.confirmPassword.length > 0
                    ? isConfirmPasswordValid
                      ? 'border-green-500 bg-green-50'
                      : 'border-red-500 bg-red-50'
                    : 'border-gray-300 bg-gray-50'
              }`}
            />
            <button
              type="button"
              onClick={() => setShowConfirmPassword(!showConfirmPassword)}
              className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-500 hover:text-gray-700 transition-colors duration-200"
            >
              {showConfirmPassword ? (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L3 3m6.878 6.878L21 21" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                </svg>
              )}
            </button>
          </div>

          {}
          {signUpData.confirmPassword.length > 0 && (
            <div className={`text-[12px] font-['Hana2.0_M'] flex items-center gap-1 ${
              isConfirmPasswordValid ? 'text-green-600' : 'text-red-500'
            }`}>
              <span>{isConfirmPasswordValid ? '✓' : '✗'}</span>
              <span>{isConfirmPasswordValid ? '비밀번호가 일치합니다' : '비밀번호가 일치하지 않습니다'}</span>
            </div>
          )}
        </div>

        {}
        <div className="flex items-center gap-4 my-2">
          <div className="flex-1 h-px bg-gray-300"></div>
          <span className="font-['Hana2.0_M'] text-[14px] text-gray-500">또는</span>
          <div className="flex-1 h-px bg-gray-300"></div>
        </div>

        {}
        <button
          onClick={handleKakaoLogin}
          className="w-full h-[50px] bg-[#FEE500] hover:bg-[#FDD835] text-black rounded-[10px] font-['Hana2.0_M'] text-[16px] flex items-center justify-center gap-3 transition-all duration-200 border-2 border-[#FEE500] hover:border-[#FDD835]"
        >
          <img
            src="/images/KakaoTalk_logo.png"
            alt="카카오톡 로고"
            className="w-5 h-5"
          />
          <span>카카오로 간편 로그인</span>
        </button>
      </div>
    </div>
  )
}

export default Step5PasswordSetup