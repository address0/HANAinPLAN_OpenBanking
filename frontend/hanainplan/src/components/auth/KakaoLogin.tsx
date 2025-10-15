import { useState } from 'react'

interface KakaoLoginProps {
  onError: (error: string) => void;
}

function KakaoLogin({ onError }: KakaoLoginProps) {
  const [isLoading, setIsLoading] = useState(false)

  const handleKakaoLogin = async () => {
    setIsLoading(true)

    try {
      const response = await fetch('/api/auth/kakao/url', {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      })

      if (!response.ok) {
        throw new Error('카카오 로그인 URL을 가져오는데 실패했습니다.')
      }

      const data = await response.json()

      if (data.success && data.url) {
        window.location.href = data.url
      } else {
        onError(data.message || '카카오 로그인을 시작할 수 없습니다.')
      }
    } catch (error) {
      onError('카카오 로그인 처리 중 오류가 발생했습니다.')
    } finally {
      setIsLoading(false)
    }
  }

  return (
    <button
      onClick={handleKakaoLogin}
      disabled={isLoading}
      className={`w-[360px] flex items-center justify-center gap-3 px-4 py-3 rounded-lg font-['Hana2.0_M'] text-sm font-medium transition-colors duration-200 ${
        isLoading
          ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
          : 'bg-[#FEE500] hover:bg-[#FDD835] text-black cursor-pointer'
      }`}
    >
      <svg width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M9 0C4.03 0 0 3.25 0 7.25C0 10.25 2.15 12.75 5.25 14.25L4.5 16.5C4.5 16.5 4.5 17.25 5.25 17.25C5.75 17.25 6 16.75 6 16.75L6.75 14.5C7.5 14.75 8.25 14.75 9 14.75C13.97 14.75 18 11.5 18 7.5C18 3.5 13.97 0 9 0Z" fill="currentColor"/>
      </svg>
      {isLoading ? '로딩 중...' : '카카오로 로그인'}
    </button>
  )
}

export default KakaoLogin