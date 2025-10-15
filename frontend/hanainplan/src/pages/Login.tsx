import Lottie from 'lottie-react'
import { useEffect, useState } from 'react'
import LoginForm from '../components/form/LoginForm'
import SignUpForm from '../components/form/SignUpForm'

function Login() {
  const [animationData, setAnimationData] = useState(null)
  const [isSignUp, setIsSignUp] = useState(false)

  useEffect(() => {
    fetch('/lottie/login-background.json')
      .then(response => response.json())
      .then(data => setAnimationData(data))
  }, [])

  return (
    <div className="w-full h-full min-h-screen relative">
      {}
      <div className="absolute inset-0 z-0">
        {animationData && (
          <Lottie
            animationData={animationData}
            loop={true}
            autoplay={true}
            style={{ width: '100%', height: '100%' }}
          />
        )}
      </div>

      {}
      <div className="relative z-10 w-full h-full flex items-center justify-center">
        {}
        <div className={`flex rounded-[20px] transition-all duration-500 ease-in-out ${
          isSignUp
            ? 'w-[1000px] h-[700px]'
            : 'w-[800px] h-[500px]'
        }`}>
          {}
          <div className={`h-full bg-[#008485] relative rounded-l-[20px] shadow-[4px_0px_20px_rgba(0,0,0,0.25)] transition-all duration-500 ease-in-out flex items-center justify-center ${
            isSignUp
              ? 'w-[400px]'
              : 'w-[350px]'
          }`}>
            <img
              src="/character/title.png"
              alt="HANAinPLAN"
              className={`drop-shadow-[4px_4px_10px_rgba(0,0,0,0.25)] transition-all duration-500 ease-in-out animate-float ${
                isSignUp
                  ? 'w-[280px] h-[280px]'
                  : 'w-[240px] h-[240px]'
              }`}
            />
          </div>

          {}
          {isSignUp ? (
            <SignUpForm onBackToLogin={() => setIsSignUp(false)} />
          ) : (
            <LoginForm onSignUp={() => setIsSignUp(true)} />
          )}
        </div>
      </div>
    </div>
  )
}

export default Login