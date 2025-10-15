import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useUserStore } from '../store/userStore'

function KakaoCallback(): JSX.Element | null {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { setUser } = useUserStore()
  const [isProcessing, setIsProcessing] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [hasProcessed, setHasProcessed] = useState(false)
  const [retryCount, setRetryCount] = useState(0)
  const MAX_RETRY_COUNT = 2

  useEffect(() => {
    if (hasProcessed) {
      return
    }

    const handleKakaoCallback = async () => {
      try {
        const code = searchParams.get('code')

        if (!code) {
          setError('인증 코드가 없습니다.')
          setIsProcessing(false)
          setHasProcessed(true)
          return
        }

        setHasProcessed(true)

        const newUrl = new URL(window.location.href)
        newUrl.searchParams.delete('code')
        window.history.replaceState({}, '', newUrl.toString())

        const apiUrl = `/api/auth/kakao/callback?code=${code}`

        const response = await fetch(apiUrl, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        })

        const data = await response.json()

        if (data.success) {
          setUser({
            id: parseInt(data.id),
            userId: parseInt(data.id),
            phoneNumber: data.email || `kakao_${data.id}`,
            name: data.nickname,
            userType: 'GENERAL',
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          })

          setSuccess(true)
          setIsProcessing(false)

          setTimeout(() => {
            navigate('/main')
          }, 2000)
        } else {

          if (data.message && data.message.includes('만료되었거나 이미 사용되었습니다')) {

            if (retryCount < MAX_RETRY_COUNT) {
              setRetryCount(prev => prev + 1)

              setTimeout(async () => {
                try {
                  const response = await fetch('/api/auth/kakao/url', {
                    method: 'GET',
                    headers: {
                      'Content-Type': 'application/json',
                    },
                  })

                  if (response.ok) {
                    const urlData = await response.json()
                    if (urlData.success && urlData.url) {
                      window.location.href = urlData.url
                      return
                    }
                  }
                } catch (error) {
                }

                setError('인증 코드가 만료되었습니다. 다시 로그인해주세요.')
                setIsProcessing(false)
              }, 3000)

              setError(`인증 코드가 만료되었습니다. 자동으로 새 로그인을 시도합니다... (${retryCount + 1}/${MAX_RETRY_COUNT})`)
            } else {
              setError('인증 코드 문제가 지속되고 있습니다. 카카오 개발자 콘솔 설정을 확인해주세요.')
              setIsProcessing(false)
            }
          } else {
            setError(data.message || '카카오 로그인에 실패했습니다.')
            setIsProcessing(false)
          }
        }
      } catch (error) {
        setError('로그인 처리 중 오류가 발생했습니다.')
        setIsProcessing(false)
      }
    }

    handleKakaoCallback()
  }, [searchParams, navigate, setUser])

  if (isProcessing) {
    return (
      <div className="w-full h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="w-16 h-16 border-4 border-[#008485] border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <p className="text-gray-600 font-['Hana2.0_M'] text-lg">카카오 로그인 처리 중...</p>
        </div>
      </div>
    )
  }

  if (success) {
    return (
      <div className="w-full h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <p className="text-green-600 font-['Hana2.0_M'] text-lg mb-2">✅ 카카오 로그인 성공!</p>
          <p className="text-gray-600 font-['Hana2.0_M'] text-sm">메인 페이지로 이동합니다...</p>
        </div>
      </div>
    )
  }

  if (error) {
    return (
      <div className="w-full h-screen flex items-center justify-center bg-gray-50">
        <div className="text-center">
          <div className="w-16 h-16 bg-red-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </div>
          <p className="text-red-600 font-['Hana2.0_M'] text-lg mb-4">{error}</p>
          <div className="flex gap-3">
            <button
              onClick={() => navigate('/login')}
              className="px-6 py-3 bg-[#008485] text-white rounded-lg font-['Hana2.0_M'] hover:bg-[#006666] transition-colors"
            >
              로그인 페이지로 돌아가기
            </button>
            {error.includes('만료되었습니다') && (
              <button
                onClick={() => {
                  window.location.href = '/login'
                }}
                className="px-6 py-3 bg-yellow-500 text-white rounded-lg font-['Hana2.0_M'] hover:bg-yellow-600 transition-colors"
              >
                새로 로그인하기
              </button>
            )}
          </div>
        </div>
      </div>
    )
  }

  return null
}

export default KakaoCallback