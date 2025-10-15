function Footer() {
  return (
    <footer className="mt-16 border-t border-gray-200 bg-white">
      <div className="container mx-auto px-4 py-10 grid grid-cols-1 md:grid-cols-3 gap-8 text-sm text-gray-600">
        <div>
          <div className="flex items-center gap-2">
            <img src="/images/img-hana-symbol.png" alt="하나은행 심볼" className="h-6 w-auto" />
            <span className="font-hana-bold text-gray-900">하나인플랜</span>
          </div>
          <p className="mt-3 leading-6 font-hana-regular">
            금융 라이프를 더 편리하고 안전하게 <br />
            하나인플랜은 맞춤형 보험과 노후 설계를 제공합니다.
          </p>
        </div>

        <div>
          <h4 className="font-hana-medium text-gray-900 mb-3">바로가기</h4>
          <ul className="space-y-2 font-hana-regular">
            <li><a className="hover:text-hana-green" href="#">메인</a></li>
            <li><a className="hover:text-hana-green" href="#consult">상담 신청</a></li>
            <li><a className="hover:text-hana-green" href="#pension-calculator">연금 계산기</a></li>
          </ul>
        </div>

        <div>
          <h4 className="font-hana-medium text-gray-900 mb-3">고객센터</h4>
          <ul className="space-y-2 font-hana-regular">
            <li>평일 09:00–18:00</li>
            <li>이메일: support@hanainplan.example</li>
            <li>주소: 서울특별시 중구 을지로</li>
          </ul>
        </div>
      </div>

      <div className="border-t border-gray-100 font-hana-regular">
        <div className="container mx-auto px-4 py-4 flex flex-col sm:flex-row items-center justify-between text-xs text-gray-500">
          <p>© {new Date().getFullYear()} HANAinPLAN. All rights reserved.</p>
          <div className="flex gap-4 mt-2 sm:mt-0">
            <a href="#" className="hover:text-hana-green">이용약관</a>
            <a href="#" className="hover:text-hana-green">개인정보처리방침</a>
          </div>
        </div>
      </div>
    </footer>
  )
}

export default Footer