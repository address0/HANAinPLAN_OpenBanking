import { useState, useEffect } from 'react'
import AlertModal from '../modal/AlertModal'
import { sendVerificationCode, verifyCode } from '../../api/userApi'

interface SignUpData {
  name: string;
  socialNumber: string;
  phoneNumber: string;
  verificationCode: string;
}

interface Step2Props {
  signUpData: SignUpData
  onDataChange: (data: Partial<SignUpData>) => void
  isValid: boolean
}

function Step2BasicInfo({ signUpData, onDataChange, isValid }: Step2Props) {
  const [isCodeSent, setIsCodeSent] = useState(false)
  const [isPhoneFocused, setIsPhoneFocused] = useState(false)
  const [isCodeFocused, setIsCodeFocused] = useState(false)
  const [isNameFocused, setIsNameFocused] = useState(false)
  const [isSocialFocused, setIsSocialFocused] = useState(false)
  const [timeLeft, setTimeLeft] = useState(0)
  const [isVerified, setIsVerified] = useState(false)
  
  // AlertModal 상태
  const [alertModal, setAlertModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    type: 'info' as 'success' | 'error' | 'info' | 'warning'
  })

  // 전화번호 포맷팅 함수
  const formatPhoneNumber = (value: string) => {
    const numbers = value.replace(/[^\d]/g, '')
    if (numbers.length > 11) {
      return signUpData.phoneNumber
    }
    if (numbers.length <= 3) {
      return numbers
    } else if (numbers.length <= 7) {
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`
    } else {
      return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7)}`
    }
  }

  // 주민번호 포맷팅 함수 (앞자리 6자리 + 뒷자리 1자리)
  const formatSocialNumber = (value: string) => {
    const numbers = value.replace(/[^\d]/g, '')
    if (numbers.length > 7) {
      return signUpData.socialNumber
    }
    if (numbers.length <= 6) {
      return numbers
    } else {
      return `${numbers.slice(0, 6)}-${numbers.slice(6, 7)}`
    }
  }

  // 입력 핸들러들
  const handleNameChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onDataChange({ name: e.target.value })
  }

  const handleSocialNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formatted = formatSocialNumber(e.target.value)
    onDataChange({ socialNumber: formatted })
  }

  const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formattedNumber = formatPhoneNumber(e.target.value)
    onDataChange({ phoneNumber: formattedNumber })
    setIsVerified(false)
  }

  const handleVerificationCodeChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    onDataChange({ verificationCode: e.target.value })
  }

  // 인증번호 전송
  const handleSendCode = async () => {
    const numbers = signUpData.phoneNumber.replace(/[^\d]/g, '')
    if (numbers.length === 11) {
      try {
        const response = await sendVerificationCode(signUpData.phoneNumber)
        
        if (response.success) {
          setIsCodeSent(true)
          setTimeLeft(180) // 3분
          onDataChange({ verificationCode: '' })
          setIsVerified(false)
          
          setAlertModal({
            isOpen: true,
            title: '인증번호 전송',
            message: `인증번호가 전송되었습니다. (개발용: ${response.verificationCode})`,
            type: 'success'
          })
        } else {
          setAlertModal({
            isOpen: true,
            title: '전송 실패',
            message: response.message || '인증번호 전송에 실패했습니다.',
            type: 'error'
          })
        }
      } catch (error) {
        console.error('인증번호 전송 오류:', error)
        setAlertModal({
          isOpen: true,
          title: '전송 실패',
          message: '인증번호 전송 중 오류가 발생했습니다.',
          type: 'error'
        })
      }
    }
  }

  // 인증번호 확인
  const handleVerifyCode = async () => {
    if (signUpData.verificationCode.trim()) {
      try {
        const response = await verifyCode(signUpData.phoneNumber, signUpData.verificationCode)
        
        if (response.success) {
          setIsVerified(true)
          setTimeLeft(0) // 타이머 중지
          
          setAlertModal({
            isOpen: true,
            title: '인증 완료',
            message: '전화번호 인증이 성공적으로 완료되었습니다.',
            type: 'success'
          })
        } else {
          setAlertModal({
            isOpen: true,
            title: '인증 실패',
            message: response.message || '인증번호가 올바르지 않습니다.',
            type: 'error'
          })
        }
      } catch (error) {
        console.error('인증번호 확인 오류:', error)
        setAlertModal({
          isOpen: true,
          title: '인증 실패',
          message: '인증번호 확인 중 오류가 발생했습니다.',
          type: 'error'
        })
      }
    }
  }

  // 유효성 검사 함수들
  const isPhoneNumberValid = () => {
    const numbers = signUpData.phoneNumber.replace(/[^\d]/g, '')
    return numbers.length === 11
  }

  const isNameValid = () => signUpData.name.trim().length >= 2

  const isSocialNumberValid = () => {
    const numbers = signUpData.socialNumber.replace(/[^\d]/g, '')
    return numbers.length === 7
  }

  // 타이머 useEffect
  useEffect(() => {
    let interval: number | null = null
    
    if (isCodeSent && timeLeft > 0) {
      interval = setInterval(() => {
        setTimeLeft((time) => {
          if (time <= 1) {
            setIsCodeSent(false)
            return 0
          }
          return time - 1
        })
      }, 1000)
    }

    return () => {
      if (interval) clearInterval(interval)
    }
  }, [isCodeSent, timeLeft])

  const formatTime = (seconds: number) => {
    const minutes = Math.floor(seconds / 60)
    const remainingSeconds = seconds % 60
    return `${minutes}:${remainingSeconds.toString().padStart(2, '0')}`
  }

  return (
    <div className="w-full flex flex-col items-center gap-3">

      {/* 이름 입력 필드 */}
      <div className="w-[400px] flex items-center gap-4">
        <label className="font-['Hana2.0_M'] text-[14px] text-gray-700 w-[100px] text-left flex-shrink-0">
          이름 <span className="text-red-500">*</span>
        </label>
        <div className={`w-[280px] h-[60px] bg-white relative rounded-[15px] flex items-center px-4 transition-all duration-200 border-2 shadow-sm ${
          isNameFocused 
            ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]' 
            : 'border-[#E0E0E0] hover:border-[#008485]/50'
        }`}>
          <input
            type="text"
            value={signUpData.name}
            onChange={handleNameChange}
            onFocus={() => setIsNameFocused(true)}
            onBlur={() => setIsNameFocused(false)}
            placeholder="이름을 입력하세요"
            className="w-full bg-transparent font-['Hana2.0_M'] text-[16px] leading-[20px] placeholder-gray-400 outline-none text-gray-700"
          />
          {isNameValid() && (
            <div className="text-green-600 text-sm">
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
            </div>
          )}
        </div>
      </div>

      {/* 주민번호 입력 필드 */}
      <div className="w-[400px] flex items-center gap-4">
        <label className="font-['Hana2.0_M'] text-[14px] text-gray-700 w-[100px] text-left flex-shrink-0">
          주민번호 <span className="text-red-500">*</span>
        </label>
        <div className={`w-[280px] h-[60px] bg-white relative rounded-[15px] flex items-center px-4 transition-all duration-200 border-2 shadow-sm ${
          isSocialFocused 
            ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]' 
            : 'border-[#E0E0E0] hover:border-[#008485]/50'
        }`}>
          <input
            type="text"
            value={signUpData.socialNumber}
            onChange={handleSocialNumberChange}
            onFocus={() => setIsSocialFocused(true)}
            onBlur={() => setIsSocialFocused(false)}
            placeholder="주민번호 7자리 (000000-0)"
            className="w-full bg-transparent font-['Hana2.0_M'] text-[16px] leading-[20px] placeholder-gray-400 outline-none text-gray-700"
            maxLength={8}
          />
          {isSocialNumberValid() && (
            <div className="text-green-600 text-sm">
              <svg className="w-5 h-5" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
              </svg>
            </div>
          )}
        </div>
      </div>

      {/* 전화번호 입력 필드 */}
      <div className="w-[400px] flex items-center gap-4">
        <label className="font-['Hana2.0_M'] text-[14px] text-gray-700 w-[100px] text-left flex-shrink-0">
          휴대폰 <span className="text-red-500">*</span>
        </label>
        <div className={`w-[280px] h-[60px] bg-white relative rounded-[15px] flex items-center px-4 transition-all duration-200 border-2 shadow-sm ${
          isPhoneFocused 
            ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]' 
            : 'border-[#E0E0E0] hover:border-[#008485]/50'
        }`}>
          <input
            type="tel"
            value={signUpData.phoneNumber}
            onChange={handlePhoneNumberChange}
            onFocus={() => setIsPhoneFocused(true)}
            onBlur={() => setIsPhoneFocused(false)}
            placeholder="010-0000-0000"
            className="w-[180px] bg-transparent font-['Hana2.0_M'] text-[16px] leading-[20px] placeholder-gray-400 outline-none text-gray-700"
          />
          {signUpData.phoneNumber && !isCodeSent && (
            <button
              onClick={handleSendCode}
              disabled={!isPhoneNumberValid()}
              className={`absolute right-4 top-1/2 transform -translate-y-1/2 px-4 py-1.5 rounded-[8px] font-['Hana2.0_M'] text-[13px] transition-all duration-200 shadow-sm ${
                isPhoneNumberValid()
                  ? 'bg-[#008485] text-white hover:bg-[#006666] hover:shadow-md cursor-pointer'
                  : 'bg-gray-300 text-gray-500 cursor-not-allowed'
              }`}
            >
              인증
            </button>
          )}
          {isCodeSent && timeLeft > 0 && (
            <div className="absolute right-4 top-1/2 transform -translate-y-1/2 px-3 py-1 bg-green-100 text-green-600 rounded-[6px] font-['Hana2.0_M'] text-[12px] whitespace-nowrap">
              ✓ 전송완료
            </div>
          )}
        </div>
      </div>

      {/* 인증 안내 메시지 */}
      {isCodeSent && timeLeft > 0 && (
        <div className="w-[400px] text-center">
          <p className="font-['Hana2.0_M'] text-[14px] text-[#008485] mb-1">
            인증번호가 전송되었습니다.
          </p>
          <p className="font-['Hana2.0_M'] text-[12px] text-gray-500">
            3분 이내에 입력해주세요.
          </p>
        </div>
      )}

      {/* 인증번호 입력 필드 */}
      <div className="w-[400px] flex items-center gap-4">
        <label className="font-['Hana2.0_M'] text-[14px] text-gray-700 w-[100px] text-left flex-shrink-0">
          인증번호 <span className="text-red-500">*</span>
        </label>
        <div className={`w-[280px] h-[60px] bg-white relative rounded-[15px] flex items-center px-4 transition-all duration-200 border-2 shadow-sm ${
          !isCodeSent || timeLeft === 0
            ? 'border-[#E0E0E0] opacity-60' 
            : isCodeFocused 
              ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]' 
              : 'border-[#E0E0E0] hover:border-[#008485]/50'
        }`}>
          <input
            type="text"
            value={signUpData.verificationCode}
            onChange={handleVerificationCodeChange}
            onFocus={() => setIsCodeFocused(true)}
            onBlur={() => setIsCodeFocused(false)}
            placeholder={
              timeLeft === 0 && !isCodeSent 
                ? "인증시간이 만료되었습니다" 
                : isCodeSent && timeLeft > 0
                  ? "인증번호를 입력하세요" 
                  : "먼저 전화번호 인증을 진행하세요"
            }
            disabled={!isCodeSent || timeLeft === 0}
            className="w-[180px] bg-transparent font-['Hana2.0_M'] text-[16px] leading-[20px] placeholder-gray-400 outline-none text-gray-700 disabled:text-gray-400"
            maxLength={6}
          />
          {isCodeSent && timeLeft > 0 && (
            <>
              <span className={`absolute top-1/2 transform -translate-y-1/2 font-['Hana2.0_M'] text-[14px] text-[#FF6B6B] whitespace-nowrap ${
                signUpData.verificationCode ? 'right-[75px]' : 'right-4'
              }`}>
                {formatTime(timeLeft)}
              </span>
              {signUpData.verificationCode && (
                <button
                  onClick={handleVerifyCode}
                  className="absolute right-4 top-1/2 transform -translate-y-1/2 px-4 py-1.5 bg-[#008485] text-white rounded-[8px] font-['Hana2.0_M'] text-[13px] hover:bg-[#006666] transition-all duration-200 shadow-sm hover:shadow-md"
                >
                  확인
                </button>
              )}
            </>
          )}
        </div>
      </div>

      {/* 재전송 버튼 */}
      {timeLeft === 0 && isCodeSent && (
        <button
          onClick={handleSendCode}
          className="font-['Hana2.0_M'] text-[14px] text-[#008485] hover:text-[#006666] underline transition-colors duration-200"
        >
          인증번호 재전송
        </button>
      )}

      {/* AlertModal */}
      <AlertModal
        isOpen={alertModal.isOpen}
        onClose={() => setAlertModal(prev => ({ ...prev, isOpen: false }))}
        title={alertModal.title}
        message={alertModal.message}
        type={alertModal.type}
        autoClose={true}
        autoCloseDelay={2000}
      />
    </div>
  )
}

export default Step2BasicInfo 