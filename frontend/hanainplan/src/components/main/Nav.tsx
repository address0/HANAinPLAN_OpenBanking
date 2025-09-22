import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../../store/userStore';

function Nav() {
  const navigate = useNavigate();
  const { user } = useUserStore();
  const [activeDropdown, setActiveDropdown] = useState<string | null>(null);

  // 메뉴 데이터 정의
  const menuItems = [
    {
      label: '홈',
      path: '/main',
      submenu: [
        { label: '홈', path: '/main' }
      ]
    },
    {
      label: '상품 보기',
      submenu: [
        { label: '정기예금', path: '/products/deposit' },
        { label: '정기적금', path: '/products/savings' },
        { label: 'IRP', path: '/products/irp' },
        { label: '보험', path: '/products/insurance' }
      ]
    },
    {
      label: '자산 관리',
      submenu: [
        { label: '내 계좌', path: '/my-account' },
        { label: '포트폴리오', path: '/portfolio' },
        { label: '연금 계산기', path: '/pension-calculator' }
      ]
    },
    {
      label: '비대면 상담',
      submenu: [
        { label: '상담직원 보기', path: '/consultation/staff' },
        { label: '상담 신청하기', path: '/consultation/request' },
        { label: '내 상담', path: '/consultation/my' }
      ]
    },
    {
      label: '라이프',
      submenu: [
        { label: '여행', path: '/life/travel' },
        { label: '부동산', path: '/life/real-estate' }
      ]
    }
  ];

  const handleMouseEnter = (label: string) => {
    setActiveDropdown(label);
  };

  const handleMouseLeave = () => {
    setActiveDropdown(null);
  };


  const handleSubmenuClick = (path: string) => {
    navigate(path);
    setActiveDropdown(null);
  };

  return (
    <div 
      className="fixed top-0 left-0 w-full z-50"
      onMouseLeave={handleMouseLeave}
    >
      <nav className="w-full bg-white shadow-lg">
        <div className="flex justify-between items-center px-[10px] gap-[10px] h-[80px]">
        {/* Main Logo Section */}
        <div className="flex items-center gap-[10px] w-[200px] h-[42px]" onClick={() => navigate('/main')}>
          <img 
            src="/images/img-hana-symbol.png" 
            alt="하나은행 심볼" 
            className="h-[38px] w-auto"
          />
          <span className="leading-[20px] text-black">
            <p className="text-[20px] font-hana-bold">하나인플랜</p>
            <p className="text-[14px] font-hana-medium">HANAinPLAN</p>
          </span>
        </div>

        {/* Navigation Menu */}
        <div 
          className="flex justify-center items-center gap-[40px] w-[900px] h-[40px] px-[20px]"
        >
          {menuItems.map((item, index) => (
            <button
              key={index}
              className={`relative w-[150px] py-3 px-4 rounded-lg transition-all duration-200 font-hana-medium text-lg ${
                activeDropdown === item.label 
                  ? 'text-hana-green bg-hana-green/5' 
                  : 'text-gray-700 hover:text-hana-green hover:bg-hana-green/5'
              }`}
              onClick={() => item.path && navigate(item.path)}
              onMouseEnter={() => handleMouseEnter(item.label)}
            >
              {item.label}
              {activeDropdown === item.label && (
                <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-hana-green rounded-full"></div>
              )}
            </button>
          ))}
        </div>

        {/* User Profile Section */}
      {user ? (
        <div 
          className="flex justify-center items-center gap-[10px] w-[180px] h-[60px] bg-white cursor-pointer hover:bg-gray-50 rounded-lg transition-colors"
          onClick={() => navigate('/user-profile')}
        >
          <div className="w-[40px] h-[40px] rounded-full bg-gray-200 flex items-center justify-center">
            <img className="w-[40px] h-[40px]" src="/images/profile.png" alt="프로필 이미지" />
          </div>
          <div className="flex flex-col justify-center">
            <span className="font-hana-medium text-[14px] leading-[18px] text-black">
              {user.name}님
            </span>
            <span className="font-hana-medium text-[12px] leading-[14px] text-gray-600 hover:text-hana-green transition-colors">
              내 정보 보기
            </span>
          </div>
        </div>
      ) : (
        <div className="flex justify-center items-center w-[180px] h-[60px]">
          <button 
            className="px-6 py-2 bg-hana-green text-white font-hana-medium text-[14px] rounded-lg hover:bg-green-600 transition-colors"
            onClick={() => navigate('/login')}
          >
            로그인
          </button>
        </div>
        )}
        </div>
      </nav>

      {/* 통합 드롭다운 메뉴 */}
      <div 
        className={`w-full bg-white border-t border-gray-100 shadow-lg transition-all duration-300 ease-out overflow-hidden ${
          activeDropdown 
            ? 'max-h-96 opacity-100 transform translate-y-0' 
            : 'max-h-0 opacity-0 transform -translate-y-4'
        }`}
      >
          <div className="flex justify-between items-start px-[10px] gap-[10px] py-6">
            {/* 좌측 빈 공간 (로고 영역과 동일) */}
            <div className="w-[200px]"></div>
            
            {/* 중앙 메뉴 영역 (네비게이션 바와 동일한 구조) */}
            <div className="w-[900px] px-[20px] flex items-start gap-[40px]">
              {menuItems.map((item, index) => (
                <div key={index} className="w-[150px] flex flex-col items-center">
                  <div className="space-y-2">
                    {item.submenu.map((subItem, subIndex) => (
                      <button
                        key={subIndex}
                        className={`block w-full text-center px-4 py-3 text-md rounded-lg transition-all duration-200 font-hana-medium ${
                          activeDropdown === item.label 
                            ? 'text-gray-600 hover:text-hana-green hover:bg-hana-green/8' 
                            : 'text-gray-400 hover:text-hana-green hover:bg-hana-green/5'
                        }`}
                        onClick={() => handleSubmenuClick(subItem.path)}
                      >
                        {subItem.label}
                      </button>
                    ))}
                  </div>
                </div>
              ))}
            </div>
            
            {/* 우측 빈 공간 (사용자 정보 영역과 동일) */}
            <div className="w-[180px]"></div>
          </div>
        </div>
    </div>
  );
}

export default Nav;