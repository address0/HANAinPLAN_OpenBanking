import { useEffect, useState } from 'react'
import { useNavigate, useSearchParams } from 'react-router-dom'
import { useUserStore } from '../store/userStore'

function KakaoCallback() {
  const [searchParams] = useSearchParams()
  const navigate = useNavigate()
  const { setUser } = useUserStore()
  const [isProcessing, setIsProcessing] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [success, setSuccess] = useState(false)
  const [hasProcessed, setHasProcessed] = useState(false)
  const [retryCount, setRetryCount] = useState(0)
  const MAX_RETRY_COUNT = 2

  console.log('=== KakaoCallback 컴포넌트 렌더링 ===')
  console.log('현재 URL:', window.location.href)
  console.log('searchParams:', Object.fromEntries(searchParams.entries()))
  console.log('code 파라미터:', searchParams.get('code'))
  console.log('error 파라미터:', searchParams.get('error'))

  useEffect(() => {
    // 이미 처리된 경우 중복 실행 방지
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

        // 처리 시작 표시
        setHasProcessed(true)

        // URL에서 code 파라미터 제거하여 재사용 방지
        const newUrl = new URL(window.location.href)
        newUrl.searchParams.delete('code')
        window.history.replaceState({}, '', newUrl.toString())

        // 백엔드로 코드 전송하여 사용자 정보 받기
        const apiUrl = `/api/auth/kakao/callback?code=${code}`
        console.log('백엔드 API 호출:', apiUrl)
        
        const response = await fetch(apiUrl, {
          method: 'GET',
          headers: {
            'Content-Type': 'application/json',
          },
        })

        console.log('백엔드 응답 상태:', response.status)
        console.log('백엔드 응답 헤더:', Object.fromEntries(response.headers.entries()))
        
        const data = await response.json()
        console.log('=== 카카오 로그인 응답 ===')
        console.log('응답 데이터:', data)
        console.log('성공 여부:', data.success)
        console.log('메시지:', data.message)

        if (data.success) {
          console.log('✅ 카카오 로그인 성공!', {
            id: data.id,
            nickname: data.nickname,
            email: data.email
          })
          
          // 사용자 정보를 zustand store에 저장
          setUser({
            id: parseInt(data.id),
            userId: parseInt(data.id),
            phoneNumber: data.email || `kakao_${data.id}`, // 이메일이 없으면 카카오 ID 사용
            name: data.nickname,
            userType: 'GENERAL', // 기본값
            isActive: true,
            createdAt: new Date().toISOString(),
            updatedAt: new Date().toISOString()
          })

          // 성공 상태 표시
          setSuccess(true)
          setIsProcessing(false)
          
          // 2초 후 메인 페이지로 이동
          setTimeout(() => {
            navigate('/main')
          }, 2000)
        } else {
          console.error('❌ 카카오 로그인 실패:', data.message)
          
          // 인증 코드 만료 오류인 경우 특별 처리
          if (data.message && data.message.includes('만료되었거나 이미 사용되었습니다')) {
            console.log(`🔄 인증 코드 만료 감지 (재시도 ${retryCount + 1}/${MAX_RETRY_COUNT})`)
            
            // 최대 재시도 횟수 확인
            if (retryCount < MAX_RETRY_COUNT) {
              setRetryCount(prev => prev + 1)
              
              // 3초 후 자동으로 새 카카오 로그인 시도
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
                      console.log('🔄 새 카카오 로그인 URL로 리다이렉트')
                      window.location.href = urlData.url
                      return
                    }
                  }
                } catch (error) {
                  console.error('새 로그인 URL 가져오기 실패:', error)
                }
                
                // 자동 리다이렉트 실패 시 에러 표시
                setError('인증 코드가 만료되었습니다. 다시 로그인해주세요.')
                setIsProcessing(false)
              }, 3000)
              
              setError(`인증 코드가 만료되었습니다. 자동으로 새 로그인을 시도합니다... (${retryCount + 1}/${MAX_RETRY_COUNT})`)
            } else {
              console.error('❌ 최대 재시도 횟수 초과')
              setError('인증 코드 문제가 지속되고 있습니다. 카카오 개발자 콘솔 설정을 확인해주세요.')
              setIsProcessing(false)
            }
          } else {
            setError(data.message || '카카오 로그인에 실패했습니다.')
            setIsProcessing(false)
          }
        }
      } catch (error) {
        console.error('카카오 콜백 처리 오류:', error)
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
                  // 완전히 새로운 카카오 로그인 시작
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
