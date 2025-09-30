import { useState } from 'react';
import Layout from '../../components/layout/Layout';

interface Counselor {
  id: number;
  name: string;
  profileImage: string;
  department: string;
  specialties: string[];
  rating: number;
  totalConsultations: number;
  languages: string[];
}

interface ConsultationField {
  id: string;
  name: string;
  description: string;
  icon: string;
}

interface TimeSlot {
  time: string;
  available: boolean;
  counselors: Counselor[];
}

interface DaySchedule {
  date: string;
  day: string;
  timeSlots: TimeSlot[];
}

const consultationFields: ConsultationField[] = [
  {
    id: 'asset-management',
    name: '자산관리',
    description: '정기예금, 적금, IRP 등 자산 증식 상담',
    icon: '💰'
  },
  {
    id: 'insurance',
    name: '보험',
    description: '건강보험, 연금보험, 생명보험 등 보험 상담',
    icon: '🛡️'
  },
  {
    id: 'investment',
    name: '투자',
    description: '주식, 펀드, 채권 등 투자 상담',
    icon: '📈'
  },
  {
    id: 'loan',
    name: '대출',
    description: '주택담보대출, 신용대출 등 대출 상담',
    icon: '🏠'
  },
  {
    id: 'pension',
    name: '연금',
    description: 'IRP, 연금보험 등 노후 준비 상담',
    icon: '👴'
  },
  {
    id: 'comprehensive',
    name: '종합상담',
    description: '전체적인 금융 포트폴리오 상담',
    icon: '🎯'
  }
];

const mockCounselors: Counselor[] = [
  {
    id: 1,
    name: "김하나",
    profileImage: "/images/img-hana-symbol.png",
    department: "자산관리팀",
    specialties: ["정기예금", "정기적금", "IRP", "투자상담"],
    rating: 4.8,
    totalConsultations: 1250,
    languages: ["한국어", "영어"]
  },
  {
    id: 2,
    name: "이보험",
    profileImage: "/images/img-hana-symbol.png",
    department: "보험상품팀",
    specialties: ["건강보험", "연금보험", "생명보험", "자동차보험"],
    rating: 4.7,
    totalConsultations: 980,
    languages: ["한국어", "중국어"]
  },
  {
    id: 3,
    name: "박투자",
    profileImage: "/images/img-hana-symbol.png",
    department: "투자상담팀",
    specialties: ["주식투자", "펀드투자", "IRP", "자산배분"],
    rating: 4.9,
    totalConsultations: 2100,
    languages: ["한국어", "영어", "일본어"]
  },
  {
    id: 4,
    name: "최연금",
    profileImage: "/images/img-hana-symbol.png",
    department: "연금상품팀",
    specialties: ["연금보험", "IRP", "노후설계", "세제혜택"],
    rating: 4.6,
    totalConsultations: 750,
    languages: ["한국어"]
  },
  {
    id: 5,
    name: "정대출",
    profileImage: "/images/img-hana-symbol.png",
    department: "대출상품팀",
    specialties: ["주택담보대출", "신용대출", "전세자금대출", "자금계획"],
    rating: 4.8,
    totalConsultations: 1100,
    languages: ["한국어", "영어"]
  },
  {
    id: 6,
    name: "강종합",
    profileImage: "/images/img-hana-symbol.png",
    department: "종합금융팀",
    specialties: ["자산관리", "보험", "투자", "대출", "연금"],
    rating: 4.9,
    totalConsultations: 3500,
    languages: ["한국어", "영어", "중국어"]
  }
];

function ConsultationRequest() {
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedField, setSelectedField] = useState<string>('');
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<string>('');
  const [selectedCounselor, setSelectedCounselor] = useState<Counselor | null>(null);

  // 다음 7일간의 날짜 생성
  const generateDates = () => {
    const dates: DaySchedule[] = [];
    const today = new Date();
    
    for (let i = 1; i <= 7; i++) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);
      
      const dayNames = ['일', '월', '화', '수', '목', '금', '토'];
      const dayName = dayNames[date.getDay()];
      
      // 주말은 제외
      if (date.getDay() !== 0 && date.getDay() !== 6) {
        const timeSlots: TimeSlot[] = [
          { time: '09:00-10:00', available: true, counselors: [mockCounselors[0], mockCounselors[2]] },
          { time: '10:00-11:00', available: true, counselors: [mockCounselors[1], mockCounselors[4]] },
          { time: '11:00-12:00', available: true, counselors: [mockCounselors[3], mockCounselors[5]] },
          { time: '14:00-15:00', available: true, counselors: [mockCounselors[0], mockCounselors[1]] },
          { time: '15:00-16:00', available: true, counselors: [mockCounselors[2], mockCounselors[3]] },
          { time: '16:00-17:00', available: true, counselors: [mockCounselors[4], mockCounselors[5]] },
          { time: '17:00-18:00', available: true, counselors: [mockCounselors[0], mockCounselors[2]] }
        ];
        
        dates.push({
          date: date.toISOString().split('T')[0],
          day: dayName,
          timeSlots
        });
      }
    }
    
    return dates;
  };

  const dates = generateDates();

  const getAvailableCounselors = () => {
    if (!selectedDate || !selectedTimeSlot) return [];
    
    const selectedDay = dates.find(d => d.date === selectedDate);
    if (!selectedDay) return [];
    
    const selectedTime = selectedDay.timeSlots.find(t => t.time === selectedTimeSlot);
    if (!selectedTime) return [];
    
    // 선택된 분야와 관련된 상담사만 필터링
    return selectedTime.counselors.filter(counselor => {
      if (selectedField === 'comprehensive') return true;
      return counselor.specialties.some(specialty => {
        switch (selectedField) {
          case 'asset-management':
            return ['정기예금', '정기적금', 'IRP', '투자상담'].includes(specialty);
          case 'insurance':
            return ['건강보험', '연금보험', '생명보험', '자동차보험'].includes(specialty);
          case 'investment':
            return ['주식투자', '펀드투자', 'IRP', '자산배분'].includes(specialty);
          case 'loan':
            return ['주택담보대출', '신용대출', '전세자금대출', '자금계획'].includes(specialty);
          case 'pension':
            return ['연금보험', 'IRP', '노후설계', '세제혜택'].includes(specialty);
          default:
            return true;
        }
      });
    });
  };

  const handleNext = () => {
    if (currentStep < 3) {
      setCurrentStep(currentStep + 1);
    }
  };

  const handlePrev = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleSubmit = () => {
    // 상담 신청 완료 처리
    alert(`상담 신청이 완료되었습니다!\n상담사: ${selectedCounselor?.name}\n일시: ${selectedDate} ${selectedTimeSlot}`);
  };

  const renderStep1 = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">상담 분야를 선택해주세요</h2>
        <p className="text-gray-600">어떤 분야의 상담을 받고 싶으신가요?</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        {consultationFields.map((field) => (
          <div
            key={field.id}
            className={`p-6 rounded-xl border-2 cursor-pointer transition-all ${
              selectedField === field.id
                ? 'border-hana-green bg-hana-green/5'
                : 'border-gray-200 hover:border-gray-300'
            }`}
            onClick={() => setSelectedField(field.id)}
          >
            <div className="text-4xl mb-3">{field.icon}</div>
            <h3 className="text-lg font-hana-bold text-gray-900 mb-2">{field.name}</h3>
            <p className="text-sm text-gray-600">{field.description}</p>
          </div>
        ))}
      </div>
    </div>
  );

  const renderStep2 = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">상담 일시를 선택해주세요</h2>
        <p className="text-gray-600">원하는 날짜와 시간을 선택해주세요</p>
      </div>
      
      <div className="space-y-6">
        {/* 날짜 선택 */}
        <div>
          <h3 className="text-lg font-hana-bold text-gray-900 mb-3">날짜 선택</h3>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
            {dates.map((day) => (
              <button
                key={day.date}
                className={`p-4 rounded-lg border-2 transition-all ${
                  selectedDate === day.date
                    ? 'border-hana-green bg-hana-green/5 text-hana-green'
                    : 'border-gray-200 hover:border-gray-300'
                }`}
                onClick={() => setSelectedDate(day.date)}
              >
                <div className="font-hana-medium">{day.day}</div>
                <div className="text-sm text-gray-600">
                  {new Date(day.date).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                </div>
              </button>
            ))}
          </div>
        </div>

        {/* 시간 선택 */}
        {selectedDate && (
          <div>
            <h3 className="text-lg font-hana-bold text-gray-900 mb-3">시간 선택</h3>
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
              {dates.find(d => d.date === selectedDate)?.timeSlots.map((slot) => (
                <button
                  key={slot.time}
                  className={`p-3 rounded-lg border-2 transition-all ${
                    selectedTimeSlot === slot.time
                      ? 'border-hana-green bg-hana-green/5 text-hana-green'
                      : slot.available
                        ? 'border-gray-200 hover:border-gray-300'
                        : 'border-gray-100 bg-gray-50 text-gray-400 cursor-not-allowed'
                  }`}
                  onClick={() => slot.available && setSelectedTimeSlot(slot.time)}
                  disabled={!slot.available}
                >
                  <div className="font-hana-medium">{slot.time}</div>
                  <div className="text-xs text-gray-500">
                    {slot.available ? '예약 가능' : '예약 불가'}
                  </div>
                </button>
              ))}
            </div>
          </div>
        )}
      </div>
    </div>
  );

  const renderStep3 = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">상담사를 선택해주세요</h2>
        <p className="text-gray-600">선택하신 시간에 가능한 전문 상담사입니다</p>
      </div>
      
      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {getAvailableCounselors().map((counselor) => (
          <div
            key={counselor.id}
            className={`p-6 rounded-xl border-2 cursor-pointer transition-all ${
              selectedCounselor?.id === counselor.id
                ? 'border-hana-green bg-hana-green/5'
                : 'border-gray-200 hover:border-gray-300'
            }`}
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
                <h3 className="text-xl font-hana-bold text-gray-900">{counselor.name}</h3>
                <p className="text-gray-600">{counselor.department}</p>
                <div className="flex items-center mt-1">
                  <span className="text-yellow-500 text-sm">★</span>
                  <span className="text-sm text-gray-600 ml-1">{counselor.rating} ({counselor.totalConsultations}회 상담)</span>
                </div>
              </div>
            </div>
            
            <div className="space-y-2">
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
          </div>
        ))}
      </div>
    </div>
  );

  const getStepTitle = () => {
    switch (currentStep) {
      case 1: return '1단계: 상담 분야 선택';
      case 2: return '2단계: 상담 일시 선택';
      case 3: return '3단계: 상담사 선택';
      default: return '';
    }
  };

  const canProceed = () => {
    switch (currentStep) {
      case 1: return selectedField !== '';
      case 2: return selectedDate !== '' && selectedTimeSlot !== '';
      case 3: return selectedCounselor !== null;
      default: return false;
    }
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        {/* Progress Bar */}
        <div className="bg-white shadow-sm border-b">
          <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-2xl font-hana-bold text-gray-900">{getStepTitle()}</h1>
              <span className="text-sm text-gray-500">{currentStep}/3 단계</span>
            </div>
            
            <div className="flex items-center space-x-4">
              {[1, 2, 3].map((step) => (
                <div key={step} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-hana-medium ${
                    step <= currentStep 
                      ? 'bg-hana-green text-white' 
                      : 'bg-gray-200 text-gray-500'
                  }`}>
                    {step}
                  </div>
                  {step < 3 && (
                    <div className={`w-12 h-1 ml-2 ${
                      step < currentStep ? 'bg-hana-green' : 'bg-gray-200'
                    }`} />
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        {/* Main Content */}
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {currentStep === 1 && renderStep1()}
          {currentStep === 2 && renderStep2()}
          {currentStep === 3 && renderStep3()}

          {/* Navigation Buttons */}
          <div className="flex justify-between mt-8">
            <button
              onClick={handlePrev}
              disabled={currentStep === 1}
              className={`px-6 py-3 rounded-lg font-hana-medium transition-colors ${
                currentStep === 1
                  ? 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  : 'bg-gray-200 text-gray-700 hover:bg-gray-300'
              }`}
            >
              이전
            </button>

            <div className="flex space-x-4">
              {currentStep < 3 ? (
                <button
                  onClick={handleNext}
                  disabled={!canProceed()}
                  className={`px-6 py-3 rounded-lg font-hana-medium transition-colors ${
                    canProceed()
                      ? 'bg-hana-green text-white hover:bg-green-600'
                      : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  }`}
                >
                  다음
                </button>
              ) : (
                <button
                  onClick={handleSubmit}
                  disabled={!canProceed()}
                  className={`px-6 py-3 rounded-lg font-hana-medium transition-colors ${
                    canProceed()
                      ? 'bg-hana-green text-white hover:bg-green-600'
                      : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  }`}
                >
                  상담 신청하기
                </button>
              )}
            </div>
          </div>
        </div>
      </div>
    </Layout>
  );
}

export default ConsultationRequest;
