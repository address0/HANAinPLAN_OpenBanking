import { useState, useEffect } from 'react'
import { BrowserRouter as Router, Routes, Route, useNavigate, Navigate } from 'react-router-dom'
import './App.css'
import Login from './pages/Login'
import VideoCall from './pages/VideoCall'
import Main from './pages/Main'
import Portfolio from './pages/Portfolio'
import PensionCalculator from './pages/PensionCalculator'
import MyAccount from './pages/MyAccount'
import UserProfile from './pages/UserProfile'
import KakaoCallback from './pages/KakaoCallback'
import DepositProducts from './pages/products/DepositProducts'
import IrpProducts from './pages/products/IrpProducts'
import InsuranceProducts from './pages/products/InsuranceProducts'
import ConsultationStaff from './pages/consultation/ConsultationStaff'
import ConsultationRequest from './pages/consultation/ConsultationRequest'
import FundList from './pages/FundList'
import FundDetail from './pages/FundDetail'
import FundMy from './pages/FundMy'

// 상담사용자용 페이지들
import ConsultantIrpProducts from './pages/consultant/IrpProducts'
import ConsultantDepositProducts from './pages/consultant/DepositProducts'

function Landing() {
  const [isLoading, setIsLoading] = useState(true)
  const [isAnimating, setIsAnimating] = useState(false)
  const navigate = useNavigate()

  useEffect(() => {
    const timer = setTimeout(() => {
      setIsLoading(false)
    }, 1000)

    return () => clearTimeout(timer)
  }, [])

  const handleLogin = () => {
    setIsAnimating(true)
    setTimeout(() => {
      navigate('/login')
    }, 1000)
  }

  return (
    <div className="w-full h-full min-h-screen flex items-center justify-center">
      <div className={`fixed ${isAnimating ? 'absolute' : 'inset-0'} bg-[#008485] ${isAnimating ? 'animate-shrink-bg' : ''}`} />
      <div className="flex items-center gap-20">
        <div className={`w-[380px] font-['Hana2.0_M'] text-2xl leading-[42px] text-white flex flex-col ${isAnimating ? 'animate-fade-out' : ''}`}>
          <div className="animate-fadeInUp-1">
            내 삶의 2막을 설계하다
          </div>
          <div className="animate-fadeInUp-2 -mt-2">
            Next Chapter in
          </div>
          <div className="animate-fadeInUp-3 mt-4">
            <span className="text-5xl font-['Hana2.0_H']">HANAinPLAN</span>
          </div>
          
          <div className="h-[60px] mt-4">
            {!isLoading && (
              <div className="animate-fadeInUp-button">
                <button 
                  onClick={handleLogin}
                  className="w-full bg-white text-[#008485] py-3 rounded-lg text-xl 
                  hover:bg-opacity-95 transition-all group border-2 border-transparent
                  hover:border-[#006D6E] hover:shadow-[0_0_20px_rgba(0,109,110,0.4)]"
                >
                  <div className="flex items-center justify-center gap-2">
                    원클릭으로 시작하기
                    <span className="opacity-0 group-hover:opacity-100 transition-opacity">
                      →
                    </span>
                  </div>
                </button>
              </div>
            )}
          </div>
        </div>
        
        <div className={`w-[350px] h-[500px] relative ${isAnimating ? 'animate-move-image' : ''}`}>
          <img 
            src="/character/title.png"
            alt="HANAinPLAN" 
            className="absolute w-[240px] h-[240px] left-[55px] top-[130px] drop-shadow-[4px_4px_10px_rgba(0,0,0,0.25)] animate-fadeInUp-image"
          />
        </div>
      </div>
    </div>
  )
}

function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Landing />} />
        <Route path="/login" element={<Login />} />
        <Route path="/auth/kakao/callback" element={<KakaoCallback />} />
        <Route path="/auth/kakao/callback/*" element={<KakaoCallback />} />
        <Route path="/test-callback" element={<KakaoCallback />} />
        <Route path="/webrtc" element={<VideoCall />} />
        <Route path="/main" element={<Main />} />
        <Route path="/portfolio" element={<Portfolio />} />
        <Route path="/pension-calculator" element={<PensionCalculator />} />
        <Route path="/my-account" element={<MyAccount />} />
        <Route path="/user-profile" element={<UserProfile />} />
        <Route path="/products/deposit" element={<DepositProducts />} />
        <Route path="/products/irp" element={<IrpProducts />} />
        <Route path="/products/insurance" element={<InsuranceProducts />} />
        <Route path="/consultation/staff" element={<ConsultationStaff />} />
        <Route path="/consultation/request" element={<ConsultationRequest />} />
        
        {/* 펀드 라우트 */}
        <Route path="/funds" element={<FundList />} />
        <Route path="/fund/:fundCode" element={<FundDetail />} />
        <Route path="/fund/my" element={<FundMy />} />

        {/* 상담사용자용 라우트 */}
        <Route path="/consultant/products/irp" element={<ConsultantIrpProducts />} />
        <Route path="/consultant/products/deposit" element={<ConsultantDepositProducts />} />
        {/* 펀드는 일반 고객과 동일한 페이지 사용 (매수/매도 버튼만 조건부 표시) */}
        <Route path="/consultant/products/fund" element={<FundList />} />

        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Router>
  )
}

export default App