import { useState } from 'react';
import Layout from '../../components/layout/Layout';

interface Counselor {
  id: number;
  name: string;
  profileImage: string;
  department: string;
  specialties: string[];
  experience: string;
  availableTimes: {
    day: string;
    times: string[];
  }[];
  rating: number;
  totalConsultations: number;
  languages: string[];
  introduction: string;
}

const mockCounselors: Counselor[] = [
  {
    id: 1,
    name: "김하나",
    profileImage: "/images/img-hana-symbol.png",
    department: "자산관리팀",
    specialties: ["정기예금", "정기적금", "IRP", "투자상담"],
    experience: "15년",
    availableTimes: [
      { day: "월요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "화요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "수요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "목요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "금요일", times: ["09:00-12:00", "14:00-17:00"] }
    ],
    rating: 4.8,
    totalConsultations: 1250,
    languages: ["한국어", "영어"],
    introduction: "하나금융그룹에서 15년간 근무하며 다양한 금융상품에 대한 전문성을 쌓아왔습니다. 고객의 목표에 맞는 맞춤형 자산관리 방안을 제시해드립니다."
  },
  {
    id: 2,
    name: "이보험",
    profileImage: "/images/img-hana-symbol.png",
    department: "보험상품팀",
    specialties: ["건강보험", "연금보험", "생명보험", "자동차보험"],
    experience: "12년",
    availableTimes: [
      { day: "월요일", times: ["10:00-13:00", "15:00-18:00"] },
      { day: "화요일", times: ["10:00-13:00", "15:00-18:00"] },
      { day: "수요일", times: ["10:00-13:00", "15:00-18:00"] },
      { day: "목요일", times: ["10:00-13:00", "15:00-18:00"] },
      { day: "금요일", times: ["10:00-13:00", "15:00-17:00"] }
    ],
    rating: 4.7,
    totalConsultations: 980,
    languages: ["한국어", "중국어"],
    introduction: "보험업계 12년 경력으로 고객의 상황에 맞는 최적의 보험 포트폴리오를 구성해드립니다. 다양한 보험 상품에 대한 깊은 이해를 바탕으로 상담해드립니다."
  },
  {
    id: 3,
    name: "박투자",
    profileImage: "/images/img-hana-symbol.png",
    department: "투자상담팀",
    specialties: ["주식투자", "펀드투자", "IRP", "자산배분"],
    experience: "18년",
    availableTimes: [
      { day: "월요일", times: ["09:00-12:00", "14:00-19:00"] },
      { day: "화요일", times: ["09:00-12:00", "14:00-19:00"] },
      { day: "수요일", times: ["09:00-12:00", "14:00-19:00"] },
      { day: "목요일", times: ["09:00-12:00", "14:00-19:00"] },
      { day: "금요일", times: ["09:00-12:00", "14:00-18:00"] }
    ],
    rating: 4.9,
    totalConsultations: 2100,
    languages: ["한국어", "영어", "일본어"],
    introduction: "18년간 투자상담 분야에서 활동하며 다양한 시장 상황에서의 투자 전략을 수립해왔습니다. 고객의 위험성향과 투자 목표에 맞는 포트폴리오를 제안해드립니다."
  },
  {
    id: 4,
    name: "최연금",
    profileImage: "/images/img-hana-symbol.png",
    department: "연금상품팀",
    specialties: ["연금보험", "IRP", "노후설계", "세제혜택"],
    experience: "10년",
    availableTimes: [
      { day: "월요일", times: ["09:30-12:30", "14:30-17:30"] },
      { day: "화요일", times: ["09:30-12:30", "14:30-17:30"] },
      { day: "수요일", times: ["09:30-12:30", "14:30-17:30"] },
      { day: "목요일", times: ["09:30-12:30", "14:30-17:30"] },
      { day: "금요일", times: ["09:30-12:30", "14:30-17:00"] }
    ],
    rating: 4.6,
    totalConsultations: 750,
    languages: ["한국어"],
    introduction: "연금상품 전문가로서 고객의 안정적인 노후 준비를 도와드립니다. 세제혜택을 최대화할 수 있는 연금 포트폴리오 구성에 특화되어 있습니다."
  },
  {
    id: 5,
    name: "정대출",
    profileImage: "/images/img-hana-symbol.png",
    department: "대출상품팀",
    specialties: ["주택담보대출", "신용대출", "전세자금대출", "자금계획"],
    experience: "14년",
    availableTimes: [
      { day: "월요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "화요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "수요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "목요일", times: ["09:00-12:00", "14:00-18:00"] },
      { day: "금요일", times: ["09:00-12:00", "14:00-17:00"] }
    ],
    rating: 4.8,
    totalConsultations: 1100,
    languages: ["한국어", "영어"],
    introduction: "대출상품 전문가로 14년간 다양한 대출 상품에 대한 상담을 진행해왔습니다. 고객의 자금 상황과 목적에 맞는 최적의 대출 방안을 제시해드립니다."
  },
  {
    id: 6,
    name: "강종합",
    profileImage: "/images/img-hana-symbol.png",
    department: "종합금융팀",
    specialties: ["자산관리", "보험", "투자", "대출", "연금"],
    experience: "20년",
    availableTimes: [
      { day: "월요일", times: ["08:30-12:00", "13:30-18:30"] },
      { day: "화요일", times: ["08:30-12:00", "13:30-18:30"] },
      { day: "수요일", times: ["08:30-12:00", "13:30-18:30"] },
      { day: "목요일", times: ["08:30-12:00", "13:30-18:30"] },
      { day: "금요일", times: ["08:30-12:00", "13:30-17:30"] }
    ],
    rating: 4.9,
    totalConsultations: 3500,
    languages: ["한국어", "영어", "중국어"],
    introduction: "20년 경력의 종합금융 전문가로 모든 금융 영역에 대한 깊은 이해를 바탕으로 고객의 종합적인 자산관리 방안을 제시해드립니다."
  }
];

function ConsultationStaff() {
  const [selectedCounselor, setSelectedCounselor] = useState<Counselor | null>(null);

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {/* Main Content */}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Introduction */}
          <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
            <h2 className="text-3xl font-hana-bold text-gray-900 mb-4">전문 상담사와 함께하세요</h2>
            <p className="text-lg text-gray-600 mb-6">
              하나금융그룹의 숙련된 전문 상담사들이 고객님의 금융 목표 달성을 위해 맞춤형 상담을 제공해드립니다.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <div className="bg-hana-green/5 p-6 rounded-lg">
                <h3 className="text-xl font-hana-bold text-hana-green mb-3">전문성</h3>
                <p className="text-gray-700">10년 이상의 경력과 전문 자격을 보유한 상담사</p>
              </div>
              <div className="bg-blue-50 p-6 rounded-lg">
                <h3 className="text-xl font-hana-bold text-blue-600 mb-3">맞춤 상담</h3>
                <p className="text-gray-700">고객의 상황과 목표에 맞는 개인별 상담</p>
              </div>
              <div className="bg-purple-50 p-6 rounded-lg">
                <h3 className="text-xl font-hana-bold text-purple-600 mb-3">언어 지원</h3>
                <p className="text-gray-700">다국어 지원으로 편리한 상담 서비스</p>
              </div>
            </div>
          </div>

          {/* Counselor List */}
          <div className="mb-6">
            <h3 className="text-2xl font-hana-bold text-gray-900 mb-4">상담사 목록</h3>
          </div>
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {mockCounselors.map((counselor) => (
              <div 
                key={counselor.id}
                className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer"
                onClick={() => setSelectedCounselor(counselor)}
              >
                <div className="flex items-center mb-4">
                  <div className="w-16 h-16 rounded-full bg-gray-200 flex items-center justify-center mr-4">
                    <img 
                      src={counselor.profileImage} 
                      alt={counselor.name}
                      className="w-16 h-16 rounded-full object-cover"
                    />
                  </div>
                  <div className="flex-1">
                    <h4 className="text-xl font-hana-bold text-gray-900">{counselor.name}</h4>
                    <p className="text-gray-600">{counselor.department}</p>
                    <div className="flex items-center mt-1">
                      <span className="text-yellow-500 text-sm">★</span>
                      <span className="text-sm text-gray-600 ml-1">{counselor.rating} ({counselor.totalConsultations}회 상담)</span>
                    </div>
                  </div>
                </div>
                
                <div className="space-y-2 mb-4">
                  <p className="text-sm text-gray-500">전문분야</p>
                  <div className="flex flex-wrap gap-2">
                    {counselor.specialties.slice(0, 3).map((specialty, index) => (
                      <span key={index} className="bg-hana-green/10 text-hana-green px-2 py-1 rounded-full text-xs font-hana-medium">
                        {specialty}
                      </span>
                    ))}
                    {counselor.specialties.length > 3 && (
                      <span className="bg-gray-100 text-gray-600 px-2 py-1 rounded-full text-xs font-hana-medium">
                        +{counselor.specialties.length - 3}
                      </span>
                    )}
                  </div>
                </div>

                <div className="space-y-2 mb-4">
                  <p className="text-sm text-gray-500">경력</p>
                  <p className="text-gray-700 font-hana-medium">{counselor.experience}</p>
                </div>

                <div className="space-y-2">
                  <p className="text-sm text-gray-500">지원 언어</p>
                  <div className="flex flex-wrap gap-2">
                    {counselor.languages.map((language, index) => (
                      <span key={index} className="bg-blue-50 text-blue-600 px-2 py-1 rounded-full text-xs font-hana-medium">
                        {language}
                      </span>
                    ))}
                  </div>
                </div>

                <button className="mt-4 w-full bg-hana-green text-white py-2 rounded-lg hover:bg-green-600 transition-colors font-hana-medium">
                  상담사 정보 보기
                </button>
              </div>
            ))}
          </div>
        </div>

        {/* Counselor Detail Modal */}
        {selectedCounselor && (
          <div 
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
            onClick={() => setSelectedCounselor(null)}
          >
            <div 
              className="bg-white rounded-xl max-w-4xl w-full max-h-[90vh] overflow-y-auto"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="p-6 border-b">
                <div className="flex items-center justify-between">
                  <div className="flex items-center">
                    <div className="w-20 h-20 rounded-full bg-gray-200 flex items-center justify-center mr-6">
                      <img 
                        src={selectedCounselor.profileImage} 
                        alt={selectedCounselor.name}
                        className="w-20 h-20 rounded-full object-cover"
                      />
                    </div>
                    <div>
                      <h2 className="text-3xl font-hana-bold text-gray-900">{selectedCounselor.name}</h2>
                      <p className="text-xl text-gray-600">{selectedCounselor.department}</p>
                      <div className="flex items-center mt-2">
                        <span className="text-yellow-500">★</span>
                        <span className="text-gray-600 ml-1">{selectedCounselor.rating} ({selectedCounselor.totalConsultations}회 상담)</span>
                      </div>
                    </div>
                  </div>
                  <button
                    onClick={() => setSelectedCounselor(null)}
                    className="text-gray-400 hover:text-gray-600 text-2xl"
                  >
                    ×
                  </button>
                </div>
              </div>

              <div className="p-6">
                <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
                  {/* Left Column */}
                  <div className="space-y-6">
                    <div>
                      <h3 className="text-xl font-hana-bold text-gray-900 mb-3">소개</h3>
                      <p className="text-gray-700 leading-relaxed">{selectedCounselor.introduction}</p>
                    </div>

                    <div>
                      <h3 className="text-xl font-hana-bold text-gray-900 mb-3">전문분야</h3>
                      <div className="flex flex-wrap gap-2">
                        {selectedCounselor.specialties.map((specialty, index) => (
                          <span key={index} className="bg-hana-green/10 text-hana-green px-3 py-2 rounded-full text-sm font-hana-medium">
                            {specialty}
                          </span>
                        ))}
                      </div>
                    </div>

                    <div>
                      <h3 className="text-xl font-hana-bold text-gray-900 mb-3">경력 정보</h3>
                      <div className="bg-gray-50 p-4 rounded-lg">
                        <div className="grid grid-cols-2 gap-4">
                          <div>
                            <p className="text-sm text-gray-500">경력</p>
                            <p className="font-hana-medium">{selectedCounselor.experience}</p>
                          </div>
                          <div>
                            <p className="text-sm text-gray-500">총 상담 횟수</p>
                            <p className="font-hana-medium">{selectedCounselor.totalConsultations.toLocaleString()}회</p>
                          </div>
                        </div>
                      </div>
                    </div>

                    <div>
                      <h3 className="text-xl font-hana-bold text-gray-900 mb-3">지원 언어</h3>
                      <div className="flex flex-wrap gap-2">
                        {selectedCounselor.languages.map((language, index) => (
                          <span key={index} className="bg-blue-50 text-blue-600 px-3 py-2 rounded-full text-sm font-hana-medium">
                            {language}
                          </span>
                        ))}
                      </div>
                    </div>
                  </div>

                  {/* Right Column */}
                  <div>
                    <h3 className="text-xl font-hana-bold text-gray-900 mb-3">상담 가능 시간</h3>
                    <div className="space-y-3">
                      {selectedCounselor.availableTimes.map((schedule, index) => (
                        <div key={index} className="bg-gray-50 p-4 rounded-lg">
                          <div className="flex items-center justify-between mb-2">
                            <span className="font-hana-medium text-gray-900">{schedule.day}</span>
                          </div>
                          <div className="flex flex-wrap gap-2">
                            {schedule.times.map((time, timeIndex) => (
                              <span key={timeIndex} className="bg-white text-gray-700 px-3 py-1 rounded-full text-sm border">
                                {time}
                              </span>
                            ))}
                          </div>
                        </div>
                      ))}
                    </div>

                    <div className="mt-6">
                      <button className="w-full bg-hana-green text-white py-3 rounded-lg hover:bg-green-600 transition-colors font-hana-medium text-lg">
                        상담 신청하기
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}

export default ConsultationStaff;
