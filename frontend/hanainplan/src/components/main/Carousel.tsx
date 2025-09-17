import { useState, useEffect } from 'react';

interface CarouselProps {
  className?: string;
}

function Carousel({ className = '' }: CarouselProps) {
  const [currentSlide, setCurrentSlide] = useState(0);
  const [isPlaying, setIsPlaying] = useState(true);

  // 캐러셀 데이터 배열
  const slides = [
    {
      id: 1,
      backgroundImage: '/character/work.png',
      backgroundColor: '#F6EFE9',
      overlayColor: 'rgba(0, 0, 0, 0.6)',
      title: '열심히 달려온 당신의\n다음 챕터를 위해,\n이제 나를 위한 계획',
      titleColor: '#FFFFFF',
      logoPosition: 'bottom-left'
    },
    {
      id: 2,
      backgroundImage: '/character/rest.png',
      backgroundColor: '#F6EFE9',
      overlayColor: 'rgba(0, 71, 71, 0.6)',
      title: '지금은 나를 위한 시간!\n여유로운 인생 2막의 시작',
      titleColor: '#FFFFFF',
      logoPosition: 'bottom-left'
    }
  ];

  // 자동 슬라이드 기능
  useEffect(() => {
    if (!isPlaying) return;

    const interval = setInterval(() => {
      setCurrentSlide((prev) => (prev + 1) % slides.length);
    }, 4000); // 4초마다 자동 슬라이드

    return () => clearInterval(interval);
  }, [isPlaying, slides.length]);

  // 이전 슬라이드로 이동
  const goToPrevious = () => {
    setCurrentSlide((prev) => (prev - 1 + slides.length) % slides.length);
  };

  // 다음 슬라이드로 이동
  const goToNext = () => {
    setCurrentSlide((prev) => (prev + 1) % slides.length);
  };

  // 특정 슬라이드로 이동
  const goToSlide = (index: number) => {
    setCurrentSlide(index);
  };

  // 재생/일시정지 토글
  const togglePlayPause = () => {
    setIsPlaying(!isPlaying);
  };

  return (
    <div className={`relative w-full h-[600px] overflow-hidden ${className}`}>
      {/* 슬라이드 컨테이너 */}
      <div 
        className="flex transition-transform duration-500 ease-in-out h-full"
        style={{ transform: `translateX(-${currentSlide * 100}%)` }}
      >
        {slides.map((slide, index) => (
          <div 
            key={slide.id} 
            className="w-full h-full flex-shrink-0 relative"
            style={{ backgroundColor: slide.backgroundColor }}
          >
            {/* 배경 이미지 */}
            <div className="absolute right-0 top-1/2 transform -translate-y-1/2 w-[480px] h-[420px] mr-[40px]">
              <img
                src={slide.backgroundImage}
                alt={`캐러셀 배경 이미지 ${index + 1}`}
                className="w-full h-full object-cover"
                onError={(e) => {
                  console.error(`이미지 로드 실패: ${slide.backgroundImage}`);
                  e.currentTarget.style.display = 'none';
                }}
              />
            </div>

            {/* 오버레이 */}
            <div 
              className="absolute inset-0 w-full h-full"
              style={{ backgroundColor: slide.overlayColor }}
            />

            {/* 콘텐츠 영역 */}
            <div className="absolute inset-0 flex flex-col justify-center pl-16 pb-20">
              {/* 메인 텍스트 */}
              <div className="mb-12">
                {slide.title.split('\n').map((line, lineIndex) => (
                  <div key={lineIndex} className="mb-2">
                    <span 
                      className={`${index === 0 ? 'font-hana-light text-[24px] leading-[32px]' : 'font-hana-medium text-[32px] leading-[41px]'} drop-shadow-lg`}
                      style={{ 
                        color: slide.titleColor,
                        textShadow: '4px 4px 4px rgba(0, 0, 0, 0.25)'
                      }}
                    >
                      {line}
                    </span>
                  </div>
                ))}
              </div>

              {/* 하나 로고와 HANAinPLAN 텍스트 */}
              <div className="flex items-center gap-6">
                <img
                  src="/images/img-hana-symbol-white.svg"
                  alt="하나은행 심볼"
                  className="w-[60px] h-[55px]"
                  style={{ 
                    filter: 'drop-shadow(4px 4px 4px rgba(0, 0, 0, 0.25))'
                  }}
                />
                <span 
                  className="font-hana-heavy text-[48px] leading-[32px] flex items-center"
                  style={{ 
                    color: '#FFFFFF',
                    textShadow: '4px 4px 4px rgba(0, 0, 0, 0.25)'
                  }}
                >
                  HANAinPLAN
                </span>
              </div>
            </div>
          </div>
        ))}
      </div>

      {/* 컨트롤 버튼 (좌측 하단) */}
      <div className="absolute bottom-4 left-4 flex items-center bg-black bg-opacity-50 rounded-full px-3 py-2 gap-2">
        {/* 재생/일시정지 버튼 */}
        <button
          onClick={togglePlayPause}
          className="text-white hover:text-hana-green transition-colors p-1"
          aria-label={isPlaying ? '일시정지' : '재생'}
        >
          {isPlaying ? (
            // 일시정지 아이콘 (두 개의 세로 막대)
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M6 4h4v16H6V4zm8 0h4v16h-4V4z"/>
            </svg>
          ) : (
            // 재생 아이콘 (삼각형)
            <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
              <path d="M8 5v14l11-7z"/>
            </svg>
          )}
        </button>

        {/* 이전 버튼 */}
        <button
          onClick={goToPrevious}
          className="text-white hover:text-hana-green transition-colors p-1"
          aria-label="이전 슬라이드"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M15.41 7.41L14 6l-6 6 6 6 1.41-1.41L10.83 12z"/>
          </svg>
        </button>

        {/* 페이지 표시 */}
        <span className="text-white text-sm font-hana-medium px-2">
          {currentSlide + 1} / {slides.length}
        </span>

        {/* 다음 버튼 */}
        <button
          onClick={goToNext}
          className="text-white hover:text-hana-green transition-colors p-1"
          aria-label="다음 슬라이드"
        >
          <svg width="16" height="16" viewBox="0 0 24 24" fill="currentColor">
            <path d="M8.59 16.59L10 18l6-6-6-6-1.41 1.41L13.17 12z"/>
          </svg>
        </button>
      </div>

      {/* 인디케이터 점들 (우측 하단) */}
      <div className="absolute bottom-4 right-4 flex gap-2">
        {slides.map((_, index) => (
          <button
            key={index}
            onClick={() => goToSlide(index)}
            className={`w-3 h-3 rounded-full transition-colors ${
              currentSlide === index 
                ? 'bg-white' 
                : 'bg-white bg-opacity-50 hover:bg-opacity-75'
            }`}
            aria-label={`${index + 1}번째 슬라이드로 이동`}
          />
        ))}
      </div>

      {/* 키보드 접근성을 위한 숨겨진 버튼들 */}
      <div className="sr-only">
        <button onClick={goToPrevious}>이전 슬라이드</button>
        <button onClick={goToNext}>다음 슬라이드</button>
      </div>
    </div>
  );
}

export default Carousel;