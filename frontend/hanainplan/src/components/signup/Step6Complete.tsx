import { useEffect, useState } from 'react'

interface Step6CompleteProps {
  userType: 'general' | 'counselor';
  userName?: string;
}

function Step6Complete({ userType, userName }: Step6CompleteProps) {
  const [showAnimation, setShowAnimation] = useState(false)

  useEffect(() => {
    const timer = setTimeout(() => {
      setShowAnimation(true)
    }, 100)

    return () => clearTimeout(timer)
  }, [])

  const handleGoToLogin = () => {
    window.location.href = '/login'
  }

  return (
    <div className="w-full flex flex-col items-center gap-[30px]">
      {}
      <div className={`relative transition-all duration-1000 transform ${
        showAnimation ? 'scale-100 opacity-100' : 'scale-50 opacity-0'
      }`}>
        <div className="w-24 h-24 bg-gradient-to-br from-[#008485] to-[#006666] rounded-full flex items-center justify-center shadow-lg">
          <svg className="w-12 h-12 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={3} d="M5 13l4 4L19 7" />
          </svg>
        </div>

        {}
        <div className={`absolute inset-0 w-24 h-24 rounded-full border-4 border-[#008485] transition-all duration-2000 ${
          showAnimation ? 'scale-150 opacity-0' : 'scale-100 opacity-100'
        }`}></div>
      </div>

      {}
      <div className={`text-center transition-all duration-1000 delay-300 transform ${
        showAnimation ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'
      }`}>
        <h2 className="font-['Hana2.0_M'] text-[24px] leading-[31px] text-gray-800 mb-2">
          🎉 회원가입이 완료되었습니다!
        </h2>
        <p className="font-['Hana2.0_M'] text-[16px] leading-[20px] text-gray-600 mb-4">
          {userName ? `${userName}님, ` : ''}하나인플랜에 오신 것을 환영합니다
        </p>
      </div>

      {}
      <div className={`flex flex-col items-center gap-4 transition-all duration-1000 delay-500 transform ${
        showAnimation ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'
      }`}>
        <img
          src="/images/img-hana-symbol.png"
          alt="하나 심볼"
          className="h-16 opacity-80"
        />
        <div className="text-center">
          <h3 className="font-['Hana2.0_M'] text-[18px] leading-[23px] text-[#008485] mb-1">
            하나인플랜
          </h3>
          <p className="font-['Hana2.0_M'] text-[12px] leading-[16px] text-gray-500">
            당신의 미래를 함께 계획합니다
          </p>
        </div>
      </div>

      {}
      <div className={`flex flex-col sm:flex-row gap-4 w-full max-w-[400px] transition-all duration-1000 delay-1000 transform ${
        showAnimation ? 'translate-y-0 opacity-100' : 'translate-y-4 opacity-0'
      }`}>
        <button
          onClick={handleGoToLogin}
          className="flex-1 h-[50px] bg-[#008485] hover:bg-[#006666] text-white rounded-[10px] font-['Hana2.0_M'] text-[16px] transition-all duration-200 hover:shadow-lg transform hover:scale-105"
        >
          로그인하기
        </button>
      </div>
    </div>
  )
}

export default Step6Complete