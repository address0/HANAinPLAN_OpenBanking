import { useState, useEffect } from 'react'

function FloatingButtons() {
  const [showScrollTop, setShowScrollTop] = useState(false)

  useEffect(() => {
    const handleScroll = () => {
      setShowScrollTop(window.scrollY > 300)
    }

    window.addEventListener('scroll', handleScroll)
    return () => window.removeEventListener('scroll', handleScroll)
  }, [])

  const scrollToTop = () => {
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  const openChatbot = () => {
    alert('챗봇 기능이 곧 추가될 예정입니다!')
  }

  return (
    <>
      {}
      <button
        onClick={openChatbot}
        className="fixed bottom-6 left-6 w-14 h-14 bg-hana-green text-white rounded-full shadow-lg hover:bg-hana-green/90 hover:shadow-xl transition-all duration-300 flex items-center justify-center z-50 group"
        aria-label="챗봇 열기"
      >
        <svg
          className="w-6 h-6 group-hover:scale-110 transition-transform"
          fill="currentColor"
          viewBox="0 0 24 24"
        >
          <path d="M12 2C6.48 2 2 6.48 2 12c0 1.54.36 2.98.97 4.29L1 23l6.71-1.97C9.02 21.64 10.46 22 12 22c5.52 0 10-4.48 10-10S17.52 2 12 2zm-1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/>
        </svg>
      </button>

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