interface Step1Props {
  selectedUserType: 'general' | 'counselor' | null
  onUserTypeSelect: (userType: 'general' | 'counselor') => void
}

function Step1UserTypeSelection({ selectedUserType, onUserTypeSelect }: Step1Props) {
  return (
    <div className="w-full flex flex-col items-center gap-[30px]">

      <p className="font-['Hana2.0_M'] text-lg leading-[18px] text-gray-600">
        서비스 가입 유형을 선택해 주세요
      </p>

      {}
      <div className="flex gap-[20px]">
        {}
        <div
          onClick={() => onUserTypeSelect('general')}
          className={`w-[160px] h-[200px] bg-white rounded-[15px] border-2 cursor-pointer transition-all duration-300 hover:scale-105 hover:shadow-lg group relative ${
            selectedUserType === 'general'
              ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]'
              : 'border-gray-200 hover:border-[#008485]/50'
          }`}
        >
          <div className="flex flex-col items-center justify-center h-full p-4 gap-4">
            {}
            <div className={`w-16 h-16 rounded-full flex items-center justify-center transition-colors duration-300 ${
              selectedUserType === 'general'
                ? 'bg-[#008485] text-white'
                : 'bg-gray-100 text-gray-600 group-hover:bg-[#008485]/10 group-hover:text-[#008485]'
            }`}>
              <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 20 20">
                <path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd" />
              </svg>
            </div>

            {}
            <div className="text-center">
              <h3 className={`font-['Hana2.0_M'] text-lg leading-[20px] mb-1 transition-colors duration-300 ${
                selectedUserType === 'general'
                  ? 'text-[#008485]'
                  : 'text-gray-700 group-hover:text-[#008485]'
              }`}>
                일반고객
              </h3>
              <p className="font-['Hana2.0_M'] text-sm text-gray-500 mt-1">
                보험 상품을 찾고<br/>계시는 고객님
              </p>
            </div>

            {}
            {selectedUserType === 'general' && (
              <div className="absolute top-3 right-3 w-6 h-6 bg-[#008485] text-white rounded-full flex items-center justify-center transition-all duration-200 animate-pulse z-10">
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </div>
            )}
          </div>
        </div>

        {}
        <div
          onClick={() => onUserTypeSelect('counselor')}
          className={`w-[160px] h-[200px] bg-white rounded-[15px] border-2 cursor-pointer transition-all duration-300 hover:scale-105 hover:shadow-lg group relative ${
            selectedUserType === 'counselor'
              ? 'border-[#008485] shadow-[0_0_0_3px_rgba(0,132,133,0.1)]'
              : 'border-gray-200 hover:border-[#008485]/50'
          }`}
        >
          <div className="flex flex-col items-center justify-center h-full p-4 gap-4">
            {}
            <div className={`w-16 h-16 rounded-full flex items-center justify-center transition-colors duration-300 ${
              selectedUserType === 'counselor'
                ? 'bg-[#008485] text-white'
                : 'bg-gray-100 text-gray-600 group-hover:bg-[#008485]/10 group-hover:text-[#008485]'
            }`}>
              <svg className="w-8 h-8" fill="currentColor" viewBox="0 0 20 20">
                <path d="M13 6a3 3 0 11-6 0 3 3 0 016 0zM18 8a2 2 0 11-4 0 2 2 0 014 0zM14 15a4 4 0 00-8 0v3h8v-3zM6 8a2 2 0 11-4 0 2 2 0 014 0zM16 18v-3a5.972 5.972 0 00-.75-2.906A3.005 3.005 0 0119 15v3h-3zM4.75 12.094A5.973 5.973 0 004 15v3H1v-3a3 3 0 013.75-2.906z"/>
              </svg>
            </div>

            {}
            <div className="text-center">
              <h3 className={`font-['Hana2.0_M'] text-[16px] leading-[20px] mb-1 transition-colors duration-300 ${
                selectedUserType === 'counselor'
                  ? 'text-[#008485]'
                  : 'text-gray-700 group-hover:text-[#008485]'
              }`}>
                상담직원
              </h3>
              <p className="font-['Hana2.0_M'] text-sm text-gray-500 mt-1">
                고객 상담을 담당하는<br/>직원분
              </p>
            </div>

            {}
            {selectedUserType === 'counselor' && (
              <div className="absolute top-3 right-3 w-6 h-6 bg-[#008485] text-white rounded-full flex items-center justify-center transition-all duration-200 animate-pulse z-10">
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clipRule="evenodd" />
                </svg>
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  )
}

export default Step1UserTypeSelection