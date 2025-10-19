import { useState, useEffect } from 'react'

function FloatingButtons() {
  const [showScrollTop, setShowScrollTop] = useState(false)
  const [showTooltip, setShowTooltip] = useState(true)

  useEffect(() => {
    const handleScroll = () => {
      setShowScrollTop(window.scrollY > 300)
    }

    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  // 말풍선을 10초 후에 숨기는 효과
  useEffect(() => {
    const timer = setTimeout(() => {
      setShowTooltip(false)
    }, 10000)

    return () => clearTimeout(timer)
  }, [])

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const openChatbot = () => {
    alert('붙여주시면 챗봇 기능 추가할게뇽!!')
  }

  return (
    <>
      {}
      <div className="fixed bottom-6 left-6 z-50">
        {/* 말풍선 */}
        {showTooltip && (
          <div className="absolute bottom-1/2 left-20 animate-fade-in">
            <div className="bg-white text-gray-800 px-4 py-2 rounded-lg shadow-lg border border-hana-green/50 max-w-xs">
              <div className="text-sm font-medium whitespace-nowrap font-hana-regular text-hana-green">
                궁금한 게 있으면 별벗에게 물어보세요!
              </div>
            </div>
          </div>
        )}
        
        {/* 챗봇 버튼 */}
        <button
          onClick={openChatbot}
          className="w-14 h-14 bg-hana-green text-white rounded-full shadow-lg hover:bg-hana-green/90 hover:shadow-xl transition-all duration-300 flex items-center justify-center group relative animate-bounce-gentle"
          aria-label="챗봇 열기"
        >
          <img src="/character/chatbot.png" alt="챗봇" className="w-12 h-12 object-contain" />
        </button>
      </div>

      {}
      {showScrollTop && (
        <button
          onClick={scrollToTop}
          className="fixed bottom-6 right-6 w-14 h-14 bg-gray-700 text-white rounded-full shadow-lg hover:bg-gray-600 hover:shadow-xl transition-all duration-300 flex items-center justify-center z-50 group animate-fade-in"
          aria-label="맨 위로 스크롤"
        >
          <svg
            className="w-6 h-6 group-hover:scale-110 transition-transform"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 10l7-7m0 0l7 7m-7-7v18" />
          </svg>
        </button>
      )}
    </>
  )
}

export default FloatingButtons