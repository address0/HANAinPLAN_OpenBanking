import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../../store/userStore';
import NotificationIcon from '../notification/NotificationIcon';

interface MenuItem {
  label: string;
  shortLabel?: string;
  path?: string;
  submenu: { label: string; path: string; }[];
}

function Nav() {
  const navigate = useNavigate();
  const { user, clearUser } = useUserStore();
  const [activeDropdown, setActiveDropdown] = useState<string | null>(null);
  const [isUserMenuOpen, setIsUserMenuOpen] = useState(false);
  const [leaveTimer, setLeaveTimer] = useState<NodeJS.Timeout | null>(null);

  const generalMenuItems: MenuItem[] = [
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
        { label: 'IRP 펀드', path: '/funds' }
      ]
    },
    {
      label: '자산',
      shortLabel: '자산',
      submenu: [
        { label: '내 계좌', path: '/my-account' },
        { label: '포트폴리오', path: '/portfolio' },
        { label: '나의 펀드', path: '/fund/my' },
        { label: '연금 계산기', path: '/pension-calculator' }
      ]
    },
    {
      label: '상담',
      shortLabel: '상담',
      submenu: [
        { label: '상담직원 보기', path: '/consultation/staff' },
        { label: '상담 신청하기', path: '/consultation/request' },
        { label: '내 상담', path: '/consultation/my' }
      ]
    }
  ];

  const consultantMenuItems: MenuItem[] = [
    {
      label: '홈',
      path: '/main',
      submenu: [
        { label: '홈', path: '/main' }
      ]
    },
    {
      label: '금융상품 목록',
      submenu: [
        { label: 'IRP', path: '/consultant/products/irp' },
        { label: '정기예금', path: '/consultant/products/deposit' },
        { label: '펀드', path: '/consultant/products/fund' }
      ]
    },
    {
      label: '상담',
      submenu: [
        { label: '내 상담', path: '/consultant/consultations' },
        { label: '일정 관리', path: '/consultant/schedule' }
      ]
    }
  ];

  const menuItems: MenuItem[] = user?.userType === 'COUNSELOR' ? consultantMenuItems : generalMenuItems;

  const handleMouseEnter = (label: string) => {
    if (leaveTimer) {
      clearTimeout(leaveTimer);
      setLeaveTimer(null);
    }
    setActiveDropdown(label);
  };

  const handleMouseLeave = () => {
    const timer = setTimeout(() => {
      setActiveDropdown(null);
      setIsUserMenuOpen(false);
    }, 300);
    setLeaveTimer(timer);
  };

  const handleSubmenuClick = (path: string) => {
    if (leaveTimer) {
      clearTimeout(leaveTimer);
      setLeaveTimer(null);
    }
    navigate(path);
    setActiveDropdown(null);
  };

  const handleUserMenuToggle = () => {
    setIsUserMenuOpen(!isUserMenuOpen);
    setActiveDropdown(null);
  };

  const handleNotificationClick = (e: React.MouseEvent) => {
    e.stopPropagation();
    setActiveDropdown(null);
    setIsUserMenuOpen(false);
  };

  const handleUserMenuClick = (path: string) => {
    navigate(path);
    setIsUserMenuOpen(false);
  };

  const handleLogout = () => {
    localStorage.removeItem('user-storage');

    clearUser();

    setIsUserMenuOpen(false);

    navigate('/main');
  };

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      const navElement = document.querySelector('nav');
      if (navElement && !navElement.contains(event.target as Node)) {
        setActiveDropdown(null);
        setIsUserMenuOpen(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
      if (leaveTimer) {
        clearTimeout(leaveTimer);
      }
    };
  }, [leaveTimer]);

  return (
    <div
      className="fixed top-0 left-0 w-full z-50"
      onMouseLeave={handleMouseLeave}
    >
      <nav className="w-full bg-white shadow-lg">
        <div className="flex justify-between items-center px-2 sm:px-4 lg:px-6 gap-2 sm:gap-4 lg:gap-6 h-16 sm:h-20 lg:h-24">
        {}
        <div className="flex items-center gap-2 sm:gap-3 flex-shrink-0 w-32 sm:w-40 lg:w-48" onClick={() => navigate('/main')}>
          <img
            src="/images/img-hana-symbol.png"
            alt="하나은행 심볼"
            className="h-8 sm:h-10 lg:h-12 w-auto"
          />
          <span className="leading-tight text-black">
            <p className="text-sm sm:text-lg lg:text-xl font-hana-bold">하나인플랜</p>
            <p className="text-xs sm:text-sm font-hana-medium">HANAinPLAN</p>
          </span>
        </div>

        {}
        <div
          className="flex justify-center items-center gap-2 sm:gap-4 lg:gap-8 flex-1 max-w-md sm:max-w-2xl lg:max-w-4xl px-2 sm:px-4"
        >
          {menuItems.map((item, index) => (
            <button
              key={index}
              className={`relative flex-1 max-w-20 sm:max-w-24 lg:max-w-32 py-2 sm:py-3 px-1 sm:px-2 lg:px-3 rounded-lg transition-all duration-200 font-hana-medium text-sm sm:text-base lg:text-lg ${
                activeDropdown === item.label
                  ? 'text-hana-green bg-hana-green/5'
                  : 'text-gray-700 hover:text-hana-green hover:bg-hana-green/5'
              }`}
              onClick={() => item.path && navigate(item.path)}
              onMouseEnter={() => handleMouseEnter(item.label)}
            >
              {item.shortLabel || item.label}
              {activeDropdown === item.label && (
                <div className="absolute bottom-0 left-1/2 transform -translate-x-1/2 w-1 h-1 bg-hana-green rounded-full"></div>
              )}
            </button>
          ))}
        </div>

        {}
        {user ? (
          <div className="relative flex justify-center items-center flex-shrink-0 w-32 sm:w-40 lg:w-48 h-12 sm:h-16 lg:h-20">
            {}
            <div className="absolute left-0 top-1/2 transform -translate-y-1/2">
              <NotificationIcon />
            </div>

            <div
              className="flex items-center gap-1 sm:gap-2 bg-white cursor-pointer rounded-lg transition-colors px-1 sm:px-2 py-1 ml-8"
              onClick={handleUserMenuToggle}
            >
              <div className="flex flex-col justify-center">
                <span className="text-xs sm:text-sm lg:text-base leading-tight text-black hover:text-hana-green transition-colors cursor-pointer font-hana-bold">
                  {user.name}님
                </span>
              </div>
              <div className={`transition-transform duration-200 ${isUserMenuOpen ? 'rotate-180' : ''}`}>
                <svg className="w-3 h-3 sm:w-4 sm:h-4 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                </svg>
              </div>
            </div>

            {}
            {isUserMenuOpen && (
              <div className="absolute top-full right-0 mt-2 w-32 sm:w-40 lg:w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1 sm:py-2 z-50">
                <button
                  className="w-full text-left px-2 sm:px-3 lg:px-4 py-2 sm:py-3 text-xs sm:text-sm lg:text-base text-gray-700 hover:bg-gray-50 hover:text-hana-green transition-colors font-hana-medium"
                  onClick={() => handleUserMenuClick('/user-profile')}
                >
                  내 정보 보기
                </button>
                <div className="border-t border-gray-100 my-1"></div>
                <button
                  className="w-full text-left px-2 sm:px-3 lg:px-4 py-2 sm:py-3 text-xs sm:text-sm lg:text-base text-red-600 hover:bg-red-50 transition-colors font-hana-medium"
                  onClick={handleLogout}
                >
                  로그아웃
                </button>
              </div>
            )}
          </div>
        ) : (
          <div className="flex justify-center items-center flex-shrink-0 w-32 sm:w-40 lg:w-48 h-12 sm:h-16 lg:h-20">
            <button
              className="px-3 sm:px-4 lg:px-6 py-1 sm:py-2 bg-hana-green text-white font-hana-medium text-xs sm:text-sm lg:text-base rounded-lg hover:bg-green-600 transition-colors"
              onClick={() => navigate('/login')}
            >
              로그인
            </button>
          </div>
        )}
        </div>
      </nav>

      {}
      <div
        className={`w-full bg-white border-t border-gray-100 shadow-lg transition-all duration-300 ease-out overflow-hidden ${
          activeDropdown
            ? 'max-h-48 sm:max-h-64 lg:max-h-96 opacity-100 transform translate-y-0'
            : 'max-h-0 opacity-0 transform -translate-y-4'
        }`}
      >
          <div className="px-2 sm:px-4 lg:px-6 py-3 sm:py-4 lg:py-6">
            {}
            <div className="flex justify-between items-start gap-2 sm:gap-4 lg:gap-8">
              {}
              <div className="hidden sm:block sm:w-32 lg:w-48 sm:flex-shrink-0"></div>

              {}
              <div className="max-w-md sm:max-w-2xl lg:max-w-4xl flex-1 px-2 sm:px-4">
                <div className="flex justify-center items-start gap-2 sm:gap-4 lg:gap-8">
                  {menuItems.map((item, index) => (
                    <div key={index} className="flex-1 max-w-20 sm:max-w-24 lg:max-w-32">
                      <div className="space-y-1 sm:space-y-2">
                        {item.submenu.map((subItem, subIndex) => (
                          <button
                            key={subIndex}
                            className={`block w-full text-center px-1 sm:px-2 lg:px-3 py-1 sm:py-2 lg:py-3 text-xs sm:text-sm lg:text-base rounded-lg transition-all duration-200 font-hana-medium ${
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
              </div>

              {}
              <div className="sm:block sm:w-32 lg:w-48 sm:flex-shrink-0"></div>
            </div>
          </div>
        </div>
    </div>
  );
}

export default Nav;