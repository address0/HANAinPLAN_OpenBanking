import React, { useState } from 'react';
import MyDataConsentModal from '../modal/MyDataConsentModal';

interface SignupWithMyDataProps {
  onSignupComplete: (bankAccountInfo: any) => void;
}

const SignupWithMyData: React.FC<SignupWithMyDataProps> = ({
  onSignupComplete
}) => {
  const [showMyDataModal, setShowMyDataModal] = useState(false);
  const [signupStep, setSignupStep] = useState<'phone' | 'identity' | 'mydata' | 'complete'>('phone');
  const [phoneNumber, setPhoneNumber] = useState('');
  const [verificationCode, setVerificationCode] = useState('');
  const [isPhoneVerified, setIsPhoneVerified] = useState(false);
  const [isCodeSent, setIsCodeSent] = useState(false);
  const [ci, setCi] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [socialNumber, setSocialNumber] = useState('');
  const [name, setName] = useState('');

  const handleSendVerificationCode = async () => {
    if (!phoneNumber) {
      alert('휴대폰 번호를 입력해주세요.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch('/api/user/phone/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phoneNumber: phoneNumber
        }),
      });

      if (response.ok) {
        setIsCodeSent(true);
        alert('인증번호가 발송되었습니다.');
      } else {
        alert('인증번호 발송에 실패했습니다.');
      }
    } catch (error) {
      alert('인증번호 발송에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleVerifyCode = async () => {
    if (!verificationCode) {
      alert('인증번호를 입력해주세요.');
      return;
    }

    setIsLoading(true);
    try {
      const response = await fetch('/api/user/phone/verify-code', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phoneNumber: phoneNumber,
          code: verificationCode
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setIsPhoneVerified(true);
        setSignupStep('identity');
        alert('휴대폰 인증이 완료되었습니다.');
      } else {
        alert('인증번호가 올바르지 않습니다.');
      }
    } catch (error) {
      alert('인증번호 검증에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleIdentityVerification = async () => {
    setIsLoading(true);
    try {
      const response = await fetch('/api/user/ci/verify', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          phoneNumber: phoneNumber
        }),
      });

      if (response.ok) {
        const data = await response.json();
        setCi(data.ci);
        setSignupStep('mydata');
        setShowMyDataModal(true);
      } else {
        alert('실명인증에 실패했습니다.');
      }
    } catch (error) {
      alert('실명인증에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleMyDataConsent = (consent: boolean, bankAccountInfo?: any[]) => {
    setShowMyDataModal(false);

    if (consent) {
      setSignupStep('complete');
      onSignupComplete({
        ci: ci,
        myDataConsent: true,
        bankAccountInfo: bankAccountInfo || [],
        timestamp: new Date().toISOString()
      });
    } else {
      setSignupStep('complete');
      onSignupComplete({
        ci: ci,
        myDataConsent: false,
        bankAccountInfo: [],
        timestamp: new Date().toISOString()
      });
    }
  };

  return (
    <div className="max-w-md mx-auto p-6 bg-white rounded-lg shadow-lg">
      <h2 className="text-2xl font-bold text-center mb-6">회원가입</h2>

      {}
      {signupStep === 'phone' && (
        <div className="space-y-4">
          <div className="text-center">
            <h3 className="text-lg font-semibold mb-2">휴대폰 인증</h3>
            <p className="text-gray-600 text-sm">
              휴대폰 번호를 입력하고 인증을 완료해주세요.
            </p>
          </div>

          <div className="space-y-3">
            <input
              type="tel"
              placeholder="휴대폰 번호 (예: 010-1234-5678)"
              value={phoneNumber}
              onChange={(e) => setPhoneNumber(e.target.value)}
              disabled={isPhoneVerified}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
            />

            {!isCodeSent ? (
              <button
                onClick={handleSendVerificationCode}
                disabled={isLoading || !phoneNumber}
                className="w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
              >
                {isLoading && (
                  <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                )}
                인증번호 발송
              </button>
            ) : (
              <div className="space-y-3">
                <input
                  type="text"
                  placeholder="인증번호 6자리"
                  value={verificationCode}
                  onChange={(e) => setVerificationCode(e.target.value)}
                  className="w-full px-3 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
                <button
                  onClick={handleVerifyCode}
                  disabled={isLoading || !verificationCode}
                  className="w-full py-2 bg-green-600 text-white rounded-lg hover:bg-green-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
                >
                  {isLoading && (
                    <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
                  )}
                  인증번호 확인
                </button>
              </div>
            )}
          </div>
        </div>
      )}

      {}
      {signupStep === 'identity' && (
        <div className="space-y-4">
          <div className="text-center">
            <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
              <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
              </svg>
            </div>
            <h3 className="text-lg font-semibold mb-2">휴대폰 인증 완료</h3>
            <p className="text-gray-600 text-sm mb-4">
              이제 실명인증을 진행합니다.
            </p>
            <button
              onClick={handleIdentityVerification}
              disabled={isLoading}
              className="w-full py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              {isLoading && (
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />
              )}
              실명인증 진행
            </button>
          </div>
        </div>
      )}

      {}
      {signupStep === 'mydata' && (
        <div className="text-center">
          <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600 mx-auto mb-4"></div>
          <p className="text-gray-600">마이데이터 수집 동의를 처리하고 있습니다...</p>
        </div>
      )}

      {}
      {signupStep === 'complete' && (
        <div className="text-center">
          <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h3 className="text-lg font-semibold mb-2">회원가입 완료!</h3>
          <p className="text-gray-600 text-sm">
            회원가입이 성공적으로 완료되었습니다.
          </p>
        </div>
      )}

      {}
      <MyDataConsentModal
        isOpen={showMyDataModal}
        onClose={() => setShowMyDataModal(false)}
        onConsent={handleMyDataConsent}
        personalInfo={{
          phoneNumber: phoneNumber,
          socialNumber: socialNumber,
          name: name,
          ci: ci
        }}
      />
    </div>
  );
};

export default SignupWithMyData;