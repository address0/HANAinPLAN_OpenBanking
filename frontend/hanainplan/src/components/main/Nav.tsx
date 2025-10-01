import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../../store/userStore';

function Nav() {
  const navigate = useNavigate();
  const { user, clearUser } = useUserStore();
  const [activeDropdown, setActiveDropdown] = useState<string | null>(null);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);

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
      label: 'IRP',
      submenu: [
        { label: 'IRP 상품소개', path: '/products/irp' },
        { label: 'IRP 정기예금', path: '/products/deposit' },
        { label: '나의 퇴직연금', path: '/products/insurance' }
      ]
    },
    {
      label: '보험',
      submenu: [
        { label: '보험상품 목록', path: '/my-account' },
        { label: '나의 보험', path: '/portfolio' }
      ]
    },
    {
      label: '자산 관리',
      submenu: [
        { label: '내 계좌', path: '/life/travel' },
        { label: '포트폴리오', path: '/life/real-estate' },
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
    }
  ];

  const handleMouseEnter = (label: string) => {
    setActiveDropdown(label);
  };

  const handleMouseLeave = () => {
    setActiveDropdown(null);
    setIsUserMenuOpen(false);
  };


  const handleSubmenuClick = (path: string) => {
    navigate(path);
    setActiveDropdown(null);
  };

  const handleUserMenuToggle = () => {
    setIsUserMenuOpen(!isUserMenuOpen);
    setActiveDropdown(null); // 다른 드롭다운이 열려있으면 닫기
  };

  const handleUserMenuClick = (path: string) => {
    navigate(path);
    setIsUserMenuOpen(false);
  };


  const handleLogout = () => {
    // 로컬 스토리지에서 사용자 데이터 제거
    localStorage.removeItem('user-storage');
    
    // 사용자 스토어 비우기
    clearUser();
    
    // 사용자 메뉴 닫기
    setIsUserMenuOpen(false);
    
    // 메인 페이지로 이동
    navigate('/main');
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
          <div className="relative flex justify-center items-center w-[180px] h-[60px]">
            <div
              className="flex items-center gap-[10px] bg-white cursor-pointer rounded-lg transition-colors px-2 py-1"
              onClick={handleUserMenuToggle}
            >
              <div className="flex flex-col justify-center">
                <span className="text-[16px] leading-[20px] text-black hover:text-hana-green transition-colors cursor-pointer font-hana-bold">
                  {user.name}님
                </span>
              </div>
              <div className={`transition-transform duration-200 ${isUserMenuOpen ? 'rotate-180' : ''}`}>
                <svg className="w-4 h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>
            </div>

            {/* 사용자 드롭다운 메뉴 */}
            {isUserMenuOpen && (
              <div className="absolute top-full right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-2 z-50">
                <button
                  className="w-full text-left px-4 py-3 text-gray-700 hover:bg-gray-50 hover:text-hana-green transition-colors font-hana-medium"
                  onClick={() => handleUserMenuClick('/user-profile')}
                >
                  내 정보 보기
                </button>
                <div className="border-t border-gray-100 my-1"></div>
                <button
                  className="w-full text-left px-4 py-3 text-red-600 hover:bg-red-50 transition-colors font-hana-medium"
                  onClick={handleLogout}
                >
                  로그아웃
                </button>
              </div>
            )}
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