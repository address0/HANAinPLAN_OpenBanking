import { useState, useEffect } from 'react'
import AlertModal from '../modal/AlertModal'
import { useNavigate } from 'react-router-dom'
import { login } from '../../api/userApi'
import type { LoginRequest, LoginResponse } from '../../api/userApi'
import { useUserStore } from '../../store/userStore'
import KakaoLogin from '../auth/KakaoLogin'

interface LoginFormProps {
  onSignUp: () => void;
}

function LoginForm({ onSignUp }: LoginFormProps) {
  const [phoneNumber, setPhoneNumber] = useState('')
  const [password, setPassword] = useState('')
  const [isPhoneFocused, setIsPhoneFocused] = useState(false)
  const [isPasswordFocused, setIsPasswordFocused] = useState(false)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [showPassword, setShowPassword] = useState(false)
  const navigate = useNavigate()
  const { setUser } = useUserStore()

  const [alertModal, setAlertModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    type: 'info' as 'success' | 'error' | 'info' | 'warning'
  })

  useEffect(() => {
    setPhoneNumber('')
    setPassword('')
    setIsSubmitting(false)
  }, [])

  const formatPhoneNumber = (value: string) => {
    const numbers = value.replace(/[^\d]/g, '')

    if (numbers.length > 11) {
      return phoneNumber
    }

    if (numbers.length <= 3) {
      return numbers
    } else if (numbers.length <= 7) {
      return `${numbers.slice(0, 3)}-${numbers.slice(3)}`
    } else {
      return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7)}`
    }
  }

  const handlePhoneNumberChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const formattedNumber = formatPhoneNumber(e.target.value)
    setPhoneNumber(formattedNumber)
  }

  const handlePasswordChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    setPassword(e.target.value)
  }

  const handleLogin = async () => {
    if (isSubmitting) return;

    if (!phoneNumber.trim()) {
      setAlertModal({
        isOpen: true,
        title: '입력 오류',
        message: '전화번호를 입력해주세요.',
        type: 'error'
      });
      return;
    }

    if (!password.trim()) {
      setAlertModal({
        isOpen: true,
        title: '입력 오류',
        message: '비밀번호를 입력해주세요.',
        type: 'error'
      });
      return;
    }

    const numbers = phoneNumber.replace(/[^\d]/g, '');
    if (numbers.length !== 11) {
      setAlertModal({
        isOpen: true,
        title: '입력 오류',
        message: '올바른 전화번호 형식을 입력해주세요.',
        type: 'error'
      });
      return;
    }

    setIsSubmitting(true);

    try {
      const loginData: LoginRequest = {
        phoneNumber: numbers,
        password: password,
        loginType: 'PHONE'
      };

      const response: LoginResponse = await login(loginData);

      if (response.success) {
        setAlertModal({
          isOpen: true,
          title: '로그인 성공',
          message: response.message,
          type: 'success'
        });

        setUser({
          id: response.userId || 0,
          userId: response.userId || 0,
          phoneNumber: response.phoneNumber || '',
          name: response.userName || '',
          userType: response.userType as 'GENERAL' | 'COUNSELOR',
          isActive: true,
          createdAt: new Date().toISOString(),
          updatedAt: new Date().toISOString()
        });

        setTimeout(() => {
          navigate('/main');
        }, 2000);
      } else {
        setAlertModal({
          isOpen: true,
          title: '로그인 실패',
          message: response.message,
          type: 'error'
        });
      }
    } catch (error) {
      setAlertModal({
        isOpen: true,
        title: '로그인 오류',
        message: '로그인 처리 중 오류가 발생했습니다. 다시 시도해주세요.',
        type: 'error'
      });
    } finally {
      setIsSubmitting(false);
    }
  };

  const handleKakaoLoginError = (error: string) => {
    setAlertModal({
      isOpen: true,
      title: '카카오 로그인 실패',
      message: error,
      type: 'error'
    });
  };

  const isFormValid = () => {
    const numbers = phoneNumber.replace(/[^\d]/g, '')
    return numbers.length === 11 && password.trim().length > 0
  }

  return (
    <form
      onSubmit={(e) => {
        e.preventDefault();
        handleLogin();
      }}
      className="w-[450px] h-full backdrop-blur-sm p-[10px] flex flex-col items-center justify-center gap-4 rounded-r-[20px] animate-slide-in border-y-2 border-r-2 border-white overflow-hidden"
    >
      {}
      <div className="h-[38px] relative flex items-center">
        <img
          src="/images/img-hana-symbol.png"
          alt="하나 심볼"
          className="h-[38px] left-0 right-[82.76%] top-0"
        />
        <span className="left-[21.98%] top-[7.89%] font-['Hana2.0_M'] text-[24px] leading-[31px]">
          하나인플랜 로그인
        </span>
      </div>

      {}
      <div className="w-[400px] h-0 border-t-2 border-[#C2C2C2]" />

      {}
      <div className={`w-[360px] h-[60px] bg-white relative rounded-[15px] flex items-center px-4 transition-all duration-200 border-2 shadow-sm ${
        isPhoneFocused
          ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]'
          : 'border-[#E0E0E0] hover:border-[#008485]/50'
      }`}>
        <input
          type="tel"
          value={phoneNumber}
          onChange={handlePhoneNumberChange}
          onFocus={() => setIsPhoneFocused(true)}
          onBlur={() => setIsPhoneFocused(false)}
          placeholder="010-0000-0000"
          className="w-full bg-transparent font-['Hana2.0_M'] text-[16px] leading-[20px] placeholder-gray-400 outline-none text-gray-700"
        />
      </div>

      {}
      <div className={`w-[360px] h-[60px] bg-white relative rounded-[15px] flex items-center px-4 transition-all duration-200 border-2 shadow-sm ${
        isPasswordFocused
          ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]'
          : 'border-[#E0E0E0] hover:border-[#008485]/50'
      }`}>
        <input
          type={showPassword ? "text" : "password"}
          value={password}
          onChange={handlePasswordChange}
          onFocus={() => setIsPasswordFocused(true)}
          onBlur={() => setIsPasswordFocused(false)}
          placeholder="비밀번호를 입력하세요"
          className="w-full pr-12 bg-transparent font-['Hana2.0_M'] text-[16px] leading-[20px] placeholder-gray-400 outline-none text-gray-700"
        />
        <button
          type="button"
          onClick={() => setShowPassword(!showPassword)}
          className="absolute right-4 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600 transition-colors duration-200"
        >
          {showPassword ? (
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13.875 18.825A10.05 10.05 0 0112 19c-4.478 0-8.268-2.943-9.543-7a9.97 9.97 0 011.563-3.029m5.858.908a3 3 0 114.243 4.243M9.878 9.878l4.242 4.242M9.878 9.878L8.464 8.464m1.414 1.414L8.464 8.464m7.071 7.071l1.414 1.414M9.878 9.878l1.414 1.414m4.242 4.242l1.414 1.414m-1.414-1.414L8.464 8.464" />
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
      <button
        onClick={handleLogin}
        disabled={!isFormValid() || isSubmitting}
        className={`w-[360px] px-4 py-3 relative rounded-lg transition-all duration-200 shadow-sm font-['Hana2.0_M'] text-[18px] leading-[20px] ${
          isFormValid() && !isSubmitting
            ? 'bg-[#008485] text-white hover:bg-[#006666] hover:shadow-md cursor-pointer border border-[#008485]'
            : 'bg-gray-300 text-gray-500 cursor-not-allowed border border-gray-300'
        }`}
      >
        {isSubmitting ? '로그인 중...' : '로그인'}
      </button>

      {}
      <KakaoLogin
        onError={handleKakaoLoginError}
      />

      {}
      <div className="font-['Hana2.0_M'] text-[16px] leading-[20px]">
        하나인플랜이 처음이신가요? <span className="text-[#008485] cursor-pointer hover:text-[#006666] transition-colors duration-200" onClick={onSignUp}>가입하기 →</span>
      </div>

      <AlertModal
        isOpen={alertModal.isOpen}
        onClose={() => setAlertModal(prev => ({ ...prev, isOpen: false }))}
        title={alertModal.title}
        message={alertModal.message}
        type={alertModal.type}
        autoClose={true}
        autoCloseDelay={2000}
      />
    </form>
  )
}

export default LoginForm