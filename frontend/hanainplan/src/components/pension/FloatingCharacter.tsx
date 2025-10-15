import { useEffect, useState } from 'react'

function FloatingCharacter() {
  const [isVisible, setIsVisible] = useState(false)

  useEffect(() => {
    setIsVisible(true)
  }, [])

  return (
    <div className="flex justify-center mb-8">
      <div
        className={`transition-all duration-1000 ease-out animate-float ${
          isVisible ? 'translate-y-0 opacity-100' : 'translate-y-10 opacity-0'
        }`}
      >
        <img
          src="/character/plan.png"
          alt="연금 계산 캐릭터"
          className="w-32 h-32 object-contain"
        />
      </div>
    </div>
  )
}

export default FloatingCharacter