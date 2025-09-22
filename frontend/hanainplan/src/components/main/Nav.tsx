import { useNavigate } from 'react-router-dom';
import { useUserStore } from '../../store/userStore';

function Nav() {
  const navigate = useNavigate();
  const { user } = useUserStore();

  return (
    <nav className="fixed top-0 left-0 w-full h-[80px] bg-white flex justify-between items-center px-[10px] gap-[10px] z-50 shadow-lg">
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
      <div className="flex justify-center items-center gap-[80px] w-[800px] h-[40px] bg-white px-[10px] font-hana-medium text-[20px] leading-[20px] text-black">
        <button className="hover:text-hana-green transition-colors" onClick={() => navigate('/main')}>
          홈
        </button>
        <button 
          onClick={() => navigate('/portfolio')}
          className="hover:text-hana-green transition-colors"
        >
          포트폴리오
        </button>
        <button 
          onClick={() => navigate('/my-account')}
          className="hover:text-hana-green transition-colors"
        >
          내 계좌
        </button>
        <button className="hover:text-hana-green transition-colors">
          상담
        </button>
        <button 
          onClick={() => navigate('/pension-calculator')}
          className="hover:text-hana-green transition-colors"
        >
          연금 계산기
        </button>
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
    </nav>
  );
}

export default Nav;