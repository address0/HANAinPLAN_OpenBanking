import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import CTAButton from './CTAButton'
import ProductCard from './ProductCard'

function MainSection() {
  const navigate = useNavigate()
  const [isVisible, setIsVisible] = useState<{ intro: boolean; category: boolean; pension: boolean; cards: boolean[] }>({
    intro: false,
    category: false,
    pension: false,
    cards: new Array(6).fill(false)
  })
  const introRef = useRef<HTMLElement>(null)
  const categoryRef = useRef<HTMLElement>(null)
  const pensionRef = useRef<HTMLElement>(null)
  const cardRefs = useRef<(HTMLDivElement | null)[]>([])

  useEffect(() => {
    const observer = new IntersectionObserver(
      (entries) => {
        entries.forEach((entry) => {
          const target = entry.target

          if (entry.isIntersecting) {
            if (target === introRef.current) {
              setIsVisible(prev => ({ ...prev, intro: true }))
            } else if (target === categoryRef.current) {
              setIsVisible(prev => ({ ...prev, category: true }))
            } else if (target === pensionRef.current) {
              setIsVisible(prev => ({ ...prev, pension: true }))
            }

            const cardIndex = cardRefs.current.findIndex(ref => ref === target)
            if (cardIndex !== -1) {
              setIsVisible(prev => {
                const newCards = [...prev.cards]
                newCards[cardIndex] = true
                return { ...prev, cards: newCards }
              })
            }
          } else {
            if (target === introRef.current) {
              setIsVisible(prev => ({ ...prev, intro: false }))
            } else if (target === categoryRef.current) {
              setIsVisible(prev => ({ ...prev, category: false }))
            } else if (target === pensionRef.current) {
              setIsVisible(prev => ({ ...prev, pension: false }))
            }

            const cardIndex = cardRefs.current.findIndex(ref => ref === target)
            if (cardIndex !== -1) {
              setIsVisible(prev => {
                const newCards = [...prev.cards]
                newCards[cardIndex] = false
                return { ...prev, cards: newCards }
              })
            }
          }
        })
      },
      { threshold: 0.3 }
    )

    const timer = setTimeout(() => {
      const allRefs = [introRef.current, categoryRef.current, pensionRef.current, ...cardRefs.current]
      allRefs.forEach(ref => ref && observer.observe(ref))
    }, 100)

    return () => {
      clearTimeout(timer)
      observer.disconnect()
    }
  }, [])

  return (
    <>

    {}
    <section
        ref={categoryRef}
        className="container mx-auto px-4 py-20 overflow-hidden"
      >
        <div className="grid grid-cols-1 lg:grid-cols-2 gap-12 items-center">
          {}
          <div className={`flex justify-center transition-all duration-1000 ease-out ${
            isVisible.category ? 'translate-x-0 opacity-100' : '-translate-x-20 opacity-0'
          }`}>
            <img src="/character/irp.png" alt="IRP 캐릭터" className="w-[360px] h-[360px] max-w-full object-contain" />
          </div>

          {}
          <div className={`flex flex-col items-start transition-all duration-1000 ease-out ${
            isVisible.category ? 'translate-x-0 opacity-100' : 'translate-x-20 opacity-0'
          }`}>
            <div className="max-w-lg">
              <h3 className="text-2xl sm:text-3xl font-hana-bold text-gray-900 text-left">
                복잡한 연금관리, 이제는 하나의 계좌로
              </h3>
              <div className="mt-8 text-gray-600 font-hana-medium text-base sm:text-lg leading-6 text-left">
                <p>IRP 계좌를 개설하고 자동이체를 설정하면,</p>
                <p>연 900만원까지 소득에 대한 세제혜택을 받을 수 있어요.</p>
                <br />
                <p>IRP 계좌 안에서 정기예금, 펀드를 가입하고</p>
                <p>HANAinPLAN에서 내 자산을 관리받아 보세요.</p>
              </div>
            </div>
          </div>
        </div>

        {}
        <div className="mt-16 grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6 place-items-center">
          {[
            {
              imageSrc: "/images/pension-insurance.jpg",
              alt: "연금/투자보험 이미지",
              title: "연금/투자보험",
              description: "노후 생활자금 마련과 자산 증식을 위한 장기 투자형 보험"
            },
            {
              imageSrc: "/images/savings-insurance.png",
              alt: "저축보험 이미지",
              title: "저축보험",
              description: "안정적인 이자와 목표 마련을 위한 장기 저축형 보험"
            },
            {
              imageSrc: "/images/term-insurance.png",
              alt: "정기보험 이미지",
              title: "정기보험",
              description: "특정 기간 동안 사망·재해 등 위험을 보장하는 기간 한정형 보험"
            },
            {
              imageSrc: "/images/life-insurance.jpg",
              alt: "종신보험 이미지",
              title: "종신보험",
              description: "평생 동안 사망 보장을 제공하는 종신 보장성 보험"
            },
            {
              imageSrc: "/images/CI-insurance.jpg",
              alt: "암/CI/간병보험 이미지",
              title: "암/CI/간병보험",
              description: "암·중대한 질병·간병 비용을 보장하는 건강 보장성 보험"
            },
            {
              imageSrc: "/images/medical-insurance.jpg",
              alt: "진병/실손의료보험 이미지",
              title: "질병/실손의료보험",
              description: "질병·사고로 인한 의료비 실손 보장을 제공하는 의료 보험"
            }
          ].map((card, index) => (
            <div
              key={index}
              ref={(el) => { cardRefs.current[index] = el }}
              className={`transition-all duration-700 ease-out ${
                isVisible.cards[index]
                  ? 'translate-y-0 opacity-100'
                  : 'translate-y-10 opacity-0'
              }`}
              style={{ transitionDelay: `${(index % 3) * 150}ms` }}
            >
              <ProductCard
                imageSrc={card.imageSrc}
                alt={card.alt}
                title={card.title}
                description={card.description}
              />
            </div>
          ))}
        </div>
      </section>

      {}
      <section
        ref={introRef}
        className="container mx-auto px-4 py-20 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center overflow-hidden"
      >
        {}
        <div className={`transition-all duration-1000 ease-out ${
          isVisible.intro ? 'translate-x-0 opacity-100' : '-translate-x-20 opacity-0'
        }`}>
          <h2 className="text-2xl sm:text-3xl lg:text-4xl font-hana-bold text-gray-900 text-left">
            전문가와의 상담, 이제 집에서 편하게
          </h2>
          <div className="mt-8 text-gray-600 font-hana-medium text-base sm:text-lg leading-6 flex flex-col gap-2 text-left max-w-md">
            <p>전문 상담사와 실시간 화상 상담을 통해,</p>
            <p>내 상황에 맞춘 자산관리 솔루션을 편리하게 받아보세요.</p>
          </div>
          <div className="mt-6">
            <CTAButton label="상담 신청하러 가기 →" href="/consultation/request" className="w-full sm:w-[320px] h-[56px]" />
          </div>
        </div>

        {}
        <div className={`flex justify-center transition-all duration-1000 ease-out ${
          isVisible.intro ? 'translate-x-0 opacity-100' : 'translate-x-20 opacity-0'
        }`}>
          <img
            src="/character/counselor.png"
            alt="상담사 일러스트"
            className="w-[360px] h-[360px] max-w-full object-contain"
          />
        </div>
      </section>

      {}
      <section
        ref={pensionRef}
        className="container mx-auto px-4 py-20 grid grid-cols-1 lg:grid-cols-2 gap-12 items-center overflow-hidden"
      >
        {}
        <div className={`transition-all duration-1000 ease-out ${
          isVisible.pension ? 'translate-x-0 opacity-100' : '-translate-x-20 opacity-0'
        }`}>
          <h3 className="text-2xl sm:text-3xl lg:text-4xl font-hana-bold text-gray-900 text-left">
            은퇴 후, 내가 받을 연금은 얼마일까요?
          </h3>
          <div className="mt-8 text-gray-600 font-hana-medium text-base sm:text-lg leading-6 max-w-md text-left">
            <p>나의 자산, 연봉 정보를 기반으로</p>
            <p>앞으로 받을 연금액과 수령 기간을 계산해 드립니다.</p>
            <p>간단한 입력만으로 은퇴 후 자산 여정을 미리 확인하세요.</p>
          </div>
          <div className="mt-6">
            <CTAButton
              label="내 연금 계산하러 가기 →"
              onClick={() => navigate('/pension-calculator')}
              className="w-full sm:w-[360px] h-[56px]"
            />
          </div>
        </div>

        {}
        <div className={`flex justify-center transition-all duration-1000 ease-out ${
          isVisible.pension ? 'translate-x-0 opacity-100' : 'translate-x-20 opacity-0'
        }`}>
          <img src="/character/plan.png" alt="연금 관련 캐릭터" className="w-[360px] h-[360px] max-w-full object-contain" />
        </div>
      </section>
    </>
  )
}

export default MainSection