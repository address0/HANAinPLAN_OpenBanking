import { useState } from 'react';
import { useQuery } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import { fetchAvailableConsultantsAtTime } from '../../api/scheduleApi';
import { createConsultation } from '../../api/consultationApi';
import { useUserStore } from '../../store/userStore';
import { getAllDepositProducts } from '../../api/productApi';
import { fundProductApi } from '../../api/fundApi';
import axios from 'axios';

const API_BASE_URL = import.meta.env.VITE_API_URL || 'http://localhost:8080';

interface Counselor {
  consultantId: number;
  userName: string;
  department: string;
  position: string;
  branchName: string;
  specialization: string;
  consultationRating: number;
  totalConsultations: number;
  workEmail: string;
  phoneNumber: string;
  experienceYears: string;
  consultationStatus: string;
  workStatus: string;
}

interface ConsultationField {
  id: string;
  name: string;
  description: string;
  icon: string;
}

interface ProductOption {
  id: string;
  name: string;
  description: string;
  icon: string;
  type: 'deposit' | 'fund';
}

const consultationFields: ConsultationField[] = [
  {
    id: 'general',
    name: '일반',
    description: '상담사를 통한 금융 정보 제공',
    icon: '💬'
  },
  {
    id: 'product',
    name: '상품가입',
    description: '정기예금 및 펀드가입 상담',
    icon: '📋'
  },
  {
    id: 'asset-management',
    name: '자산관리',
    description: '내 자산 포트폴리오 관리',
    icon: '💰'
  }
];

const getFundIcon = (fundName: string) => {
  if (fundName.includes('글로벌') || fundName.includes('해외')) return '🌍';
  if (fundName.includes('테크') || fundName.includes('기술')) return '💻';
  if (fundName.includes('배당')) return '📈';
  if (fundName.includes('ESG') || fundName.includes('친환경')) return '🌱';
  if (fundName.includes('균형')) return '⚖️';
  if (fundName.includes('성장')) return '📊';
  return '💰';
};

const assetManagementOptions = [
  {
    id: 'rebalancing',
    name: '자산 리밸런싱',
    description: '현재 포트폴리오의 자산 배분 조정',
    icon: '⚖️'
  },
  {
    id: 'fund-portfolio',
    name: '펀드 포트폴리오',
    description: '펀드 중심의 새로운 포트폴리오 구성',
    icon: '📊'
  }
];

const parseSpecialization = (specialization: string | null): string[] => {
  if (!specialization) return [];
  try {
    const parsed = JSON.parse(specialization);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

function ConsultationRequest() {
  const [currentStep, setCurrentStep] = useState(1);
  const [selectedField, setSelectedField] = useState<string>('');
  const [selectedSubOption, setSelectedSubOption] = useState<string>('');
  const [generalRequest, setGeneralRequest] = useState<string>('');
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [selectedTimeSlot, setSelectedTimeSlot] = useState<string>('');
  const [selectedCounselor, setSelectedCounselor] = useState<Counselor | null>(null);
  const [availableConsultants, setAvailableConsultants] = useState<Counselor[]>([]);
  const [isLoadingConsultants, setIsLoadingConsultants] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [showSuccessModal, setShowSuccessModal] = useState(false);
  const [showProductNoticeModal, setShowProductNoticeModal] = useState(false);
  const [consultationId, setConsultationId] = useState<string>('');

  const { user } = useUserStore();

  const { data: allConsultants = [] } = useQuery({
    queryKey: ['consultants'],
    queryFn: async () => {
      const response = await axios.get<Counselor[]>(`${API_BASE_URL}/api/consultants`);
      return response.data;
    }
  });

  const { data: depositProducts = [] } = useQuery({
    queryKey: ['depositProducts'],
    queryFn: async () => {
      const response = await getAllDepositProducts();
      return response.products || [];
    }
  });

  const { data: fundClasses = [] } = useQuery({
    queryKey: ['fundClasses'],
    queryFn: async () => {
      return await fundProductApi.getAllFundClasses();
    }
  });

  const productOptions: ProductOption[] = [
    ...depositProducts
      .filter(product => product.bankCode === 'HANA')
      .map(product => ({
        id: product.depositCode,
        name: product.name,
        description: product.description || '안정적인 수익을 위한 정기예금 상품',
        icon: '🏦',
        type: 'deposit' as const
      })),
    ...fundClasses.map(fund => ({
      id: fund.childFundCd,
      name: fund.fundMaster.fundName,
      description: `${fund.fundMaster.assetType} | ${fund.classCode}클래스`,
      icon: getFundIcon(fund.fundMaster.fundName),
      type: 'fund' as const
    }))
  ];

  const generateDates = () => {
    const dates: string[] = [];
    const today = new Date();

    let count = 0;
    let i = 1;
    while (count < 7) {
      const date = new Date(today);
      date.setDate(today.getDate() + i);

      if (date.getDay() !== 0 && date.getDay() !== 6) {
        dates.push(date.toISOString().split('T')[0]);
        count++;
      }
      i++;
    }

    return dates;
  };

  const dates = generateDates();

  const timeSlots = [
    '09:00', '09:30', '10:00', '10:30', '11:00', '11:30',
    '14:00', '14:30', '15:00', '15:30', '16:00', '16:30', '17:00', '17:30'
  ];

  const loadAvailableConsultants = async (date: string, time: string) => {
    setIsLoadingConsultants(true);
    try {
      const startTime = `${date}T${time}:00`;
      const [hours, minutes] = time.split(':').map(Number);
      const endHours = hours + 1;
      const endTime = `${date}T${endHours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:00`;

      const availableIds = await fetchAvailableConsultantsAtTime(startTime, endTime);
      const available = allConsultants.filter(c => availableIds.includes(c.consultantId));
      setAvailableConsultants(available);
    } catch (error) {
      setAvailableConsultants([]);
    } finally {
      setIsLoadingConsultants(false);
    }
  };

  const getSubOptions = () => {
    if (selectedField === 'product') return productOptions;
    if (selectedField === 'asset-management') return assetManagementOptions;
    return [];
  };

  const getSelectedOptionName = () => {
    if (selectedField === 'product') {
      return productOptions.find(option => option.id === selectedSubOption)?.name || '';
    }
    if (selectedField === 'asset-management') {
      return assetManagementOptions.find(option => option.id === selectedSubOption)?.name || '';
    }
    return '';
  };

  const handleNext = async () => {
    if (currentStep < getTotalSteps()) {
      if (currentStep === 1 && selectedField === 'product') {
        setShowProductNoticeModal(true);
        return;
      }

      if (currentStep === 3 && selectedDate && selectedTimeSlot) {
        await loadAvailableConsultants(selectedDate, selectedTimeSlot);
      }

      setCurrentStep(currentStep + 1);
    }
  };

  const handleProductNoticeConfirm = () => {
    setShowProductNoticeModal(false);
    setCurrentStep(currentStep + 1);
  };

  const handlePrev = () => {
    if (currentStep > 1) {
      setCurrentStep(currentStep - 1);
    }
  };

  const handleSubmit = async () => {
    if (!user?.userId || !selectedCounselor || !selectedDate || !selectedTimeSlot) {
      alert('모든 정보를 선택해주세요.');
      return;
    }

    if (selectedField === 'product' && !selectedSubOption) {
      alert('상품을 선택해주세요.');
      return;
    }
    if (selectedField === 'asset-management' && !selectedSubOption) {
      alert('상담 유형을 선택해주세요.');
      return;
    }
    if (selectedField === 'general' && !generalRequest.trim()) {
      alert('요청 사항을 작성해주세요.');
      return;
    }

    setIsSubmitting(true);
    try {
      const reservationDatetime = `${selectedDate}T${selectedTimeSlot}:00`;

      let detail = '';
      if (selectedField === 'product') {
        detail = getSelectedOptionName();
      } else if (selectedField === 'asset-management') {
        detail = getSelectedOptionName();
      } else if (selectedField === 'general') {
        detail = generalRequest;
      } else {
        detail = `${consultationFields.find(f => f.id === selectedField)?.name} 상담 신청`;
      }

      const response = await createConsultation({
        customerId: user.userId,
        consultantId: selectedCounselor.consultantId,
        consultationType: selectedField,
        reservationDatetime: reservationDatetime,
        detail: detail
      });

      setConsultationId(response.consultId);
      setShowSuccessModal(true);
    } catch (error: any) {
      alert(error.message || '상담 신청에 실패했습니다.');
    } finally {
      setIsSubmitting(false);
    }
  };

  const formatDate = (dateStr: string) => {
    const date = new Date(dateStr);
    const dayNames = ['일', '월', '화', '수', '목', '금', '토'];
    return `${date.getMonth() + 1}월 ${date.getDate()}일 (${dayNames[date.getDay()]})`;
  };

  const getStepTitle = () => {
    switch (currentStep) {
      case 1: return '1단계: 상담 분야 선택';
      case 2:
        if (selectedField === 'product') return '2단계: 상품 선택';
        if (selectedField === 'asset-management') return '2단계: 상담 유형 선택';
        if (selectedField === 'general') return '2단계: 요청 사항 작성';
        return '2단계';
      case 3: return '3단계: 상담 일시 선택';
      case 4: return '4단계: 상담사 선택';
      default: return '';
    }
  };

  const canProceed = () => {
    switch (currentStep) {
      case 1: return selectedField !== '';
      case 2:
        if (selectedField === 'product') return selectedSubOption !== '';
        if (selectedField === 'asset-management') return selectedSubOption !== '';
        if (selectedField === 'general') return generalRequest.trim() !== '';
        return false;
      case 3: return selectedDate !== '' && selectedTimeSlot !== '';
      case 4: return selectedCounselor !== null;
      default: return false;
    }
  };

  const getTotalSteps = () => {
    return 4;
  };

  const renderStep1 = () => (
    <div className="space-y-6">
      <div className="text-center">
        <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">상담 분야를 선택해주세요</h2>
        <p className="text-gray-600">어떤 분야의 상담을 받고 싶으신가요?</p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-4 max-w-4xl mx-auto">
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
            <div className="text-4xl mb-3 text-center">{field.icon}</div>
            <h3 className="text-lg font-hana-bold text-gray-900 mb-2 text-center">{field.name}</h3>
            <p className="text-sm text-gray-600 text-center">{field.description}</p>
          </div>
        ))}
      </div>
    </div>
  );

  const renderStep2 = () => {
    if (selectedField === 'general') {
      return (
        <div className="space-y-6">
          <div className="text-center">
            <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">요청 사항을 작성해주세요</h2>
            <p className="text-gray-600">상담받고 싶은 내용을 자유롭게 작성해주세요</p>
          </div>

          <div className="max-w-2xl mx-auto">
            <textarea
              value={generalRequest}
              onChange={(e) => setGeneralRequest(e.target.value)}
              placeholder="예) 퇴직 후 노후 준비를 위한 재무 설계 상담을 받고 싶습니다.&#10;현재 자산 현황과 향후 필요한 자금에 대해 조언을 구하고 싶습니다."
              className="w-full h-48 p-4 border-2 border-gray-300 rounded-xl focus:ring-2 focus:ring-hana-green focus:border-hana-green resize-none font-hana-regular"
              maxLength={500}
            />
            <div className="flex justify-between mt-2 text-sm text-gray-500">
              <span>상담사가 사전에 준비할 수 있도록 구체적으로 작성해주세요</span>
              <span>{generalRequest.length}/500</span>
            </div>
          </div>
        </div>
      );
    }

    return (
      <div className="space-y-6">
        <div className="text-center">
          <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">
            {selectedField === 'product' ? '상품을 선택해주세요' : '상담 유형을 선택해주세요'}
          </h2>
          <p className="text-gray-600">
            {selectedField === 'product'
              ? '가입을 원하는 상품을 선택해주세요'
              : '원하는 자산관리 상담 유형을 선택해주세요'}
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4 max-w-4xl mx-auto">
          {getSubOptions().map((option) => (
            <div
              key={option.id}
              className={`p-6 rounded-xl border-2 cursor-pointer transition-all ${
                selectedSubOption === option.id
                  ? 'border-hana-green bg-hana-green/5'
                  : 'border-gray-200 hover:border-gray-300'
              }`}
              onClick={() => setSelectedSubOption(option.id)}
            >
              <div className="text-4xl mb-3 text-center">{option.icon}</div>
              <h3 className="text-lg font-hana-bold text-gray-900 mb-2 text-center">{option.name}</h3>
              <p className="text-sm text-gray-600 text-center">{option.description}</p>
            </div>
          ))}
        </div>
      </div>
    );
  };

  const renderStep3 = () => {
    return (
        <div className="space-y-6">
          <div className="text-center">
            <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">상담 일시를 선택해주세요</h2>
            <p className="text-gray-600">원하는 날짜와 시간을 선택해주세요</p>
          </div>

          <div className="space-y-6">
            {}
            <div>
              <h3 className="text-lg font-hana-bold text-gray-900 mb-3">날짜 선택</h3>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                {dates.map((date) => {
                  const dateObj = new Date(date);
                  const dayNames = ['일', '월', '화', '수', '목', '금', '토'];
                  const dayName = dayNames[dateObj.getDay()];

                  return (
                    <button
                      key={date}
                      className={`p-4 rounded-lg border-2 transition-all ${
                        selectedDate === date
                          ? 'border-hana-green bg-hana-green/5 text-hana-green'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                      onClick={() => {
                        setSelectedDate(date);
                        setSelectedTimeSlot('');
                        setSelectedCounselor(null);
                      }}
                    >
                      <div className="font-hana-medium">{dayName}</div>
                      <div className="text-sm text-gray-600">
                        {dateObj.toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })}
                      </div>
                    </button>
                  );
                })}
              </div>
            </div>

            {}
            {selectedDate && (
              <div>
                <h3 className="text-lg font-hana-bold text-gray-900 mb-3">시간 선택</h3>
                <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-3">
                  {timeSlots.map((time) => (
                    <button
                      key={time}
                      className={`p-3 rounded-lg border-2 transition-all ${
                        selectedTimeSlot === time
                          ? 'border-hana-green bg-hana-green/5 text-hana-green'
                          : 'border-gray-200 hover:border-gray-300'
                      }`}
                      onClick={() => {
                        setSelectedTimeSlot(time);
                        setSelectedCounselor(null);
                      }}
                    >
                      <div className="font-hana-medium">{time}</div>
                      <div className="text-xs text-gray-500">예약 가능</div>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>
        </div>
    );
  };

  const renderStep4 = () => {
    if (isLoadingConsultants) {
      return (
        <div className="flex justify-center items-center py-12">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
            <p className="mt-4 text-gray-600 font-hana-regular">가능한 상담사를 조회하는 중...</p>
          </div>
        </div>
      );
    }

    return (
      <div className="space-y-6">
        <div className="text-center">
          <h2 className="text-2xl font-hana-bold text-gray-900 mb-2">상담사를 선택해주세요</h2>
          <p className="text-gray-600">선택하신 시간에 가능한 전문 상담사입니다 ({availableConsultants.length}명)</p>
        </div>

        {availableConsultants.length === 0 ? (
          <div className="text-center py-12">
            <p className="text-gray-600">선택한 시간에 가능한 상담사가 없습니다.</p>
            <p className="text-sm text-gray-500 mt-2">다른 시간을 선택해 주세요.</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            {availableConsultants.map((counselor) => {
              const specialties = parseSpecialization(counselor.specialization);
              return (
                <div
                  key={counselor.consultantId}
                  className={`p-6 rounded-xl border-2 cursor-pointer transition-all ${
                    selectedCounselor?.consultantId === counselor.consultantId
                      ? 'border-hana-green bg-hana-green/5'
                      : 'border-gray-200 hover:border-gray-300'
                  }`}
                  onClick={() => setSelectedCounselor(counselor)}
                >
                  <div className="flex items-center mb-4">
                    <div className="w-16 h-16 rounded-full bg-hana-green/10 flex items-center justify-center mr-4">
                      <span className="text-2xl font-hana-bold text-hana-green">
                        {counselor.userName?.charAt(0) || '상'}
                      </span>
                    </div>
                    <div className="flex-1">
                      <h3 className="text-xl font-hana-bold text-gray-900">{counselor.userName || '상담사'}</h3>
                      <p className="text-gray-600">{counselor.department || '일반상담팀'}</p>
                      <p className="text-sm text-gray-500">{counselor.position || '상담사'}</p>
                      <div className="flex items-center mt-1">
                        <span className="text-yellow-500 text-sm">★</span>
                        <span className="text-sm text-gray-600 ml-1">
                          {counselor.consultationRating?.toFixed(1) || '0.0'} ({counselor.totalConsultations || 0}회 상담)
                        </span>
                      </div>
                    </div>
                  </div>

                  <div className="space-y-3">
                    {specialties.length > 0 && (
                      <div>
                        <p className="text-sm text-gray-500">전문분야</p>
                        <div className="flex flex-wrap gap-2">
                          {specialties.slice(0, 4).map((specialty, index) => (
                            <span key={index} className="bg-hana-green/10 text-hana-green px-2 py-1 rounded-full text-xs font-hana-medium">
                              {specialty}
                            </span>
                          ))}
                          {specialties.length > 4 && (
                            <span className="bg-gray-100 text-gray-600 px-2 py-1 rounded-full text-xs font-hana-medium">
                              +{specialties.length - 4}
                            </span>
                          )}
                        </div>
                      </div>
                    )}

                    {counselor.experienceYears && (
                      <div>
                        <p className="text-sm text-gray-500">경력</p>
                        <p className="text-gray-700 font-hana-medium">{counselor.experienceYears}</p>
                      </div>
                    )}

                    {counselor.branchName && (
                      <div>
                        <p className="text-sm text-gray-500">소속 지점</p>
                        <p className="text-gray-700 font-hana-medium">{counselor.branchName}</p>
                      </div>
                    )}
                  </div>
                </div>
              );
            })}
          </div>
        )}
      </div>
    );
  };

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        {}
        <div className="bg-white shadow-sm border-b">
          <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
            <div className="flex items-center justify-between mb-4">
              <h1 className="text-2xl font-hana-bold text-gray-900">{getStepTitle()}</h1>
              <span className="text-sm text-gray-500">{currentStep}/{getTotalSteps()} 단계</span>
            </div>

            <div className="flex items-center space-x-4">
              {Array.from({ length: getTotalSteps() }, (_, i) => i + 1).map((step) => (
                <div key={step} className="flex items-center">
                  <div className={`w-8 h-8 rounded-full flex items-center justify-center text-sm font-hana-medium ${
                    step <= currentStep
                      ? 'bg-hana-green text-white'
                      : 'bg-gray-200 text-gray-500'
                  }`}>
                    {step}
                  </div>
                  {step < getTotalSteps() && (
                    <div className={`w-12 h-1 ml-2 ${
                      step < currentStep ? 'bg-hana-green' : 'bg-gray-200'
                    }`} />
                  )}
                </div>
              ))}
            </div>
          </div>
        </div>

        {}
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {currentStep === 1 && renderStep1()}
          {currentStep === 2 && renderStep2()}
          {currentStep === 3 && renderStep3()}
          {currentStep === 4 && renderStep4()}

          {}
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
              {currentStep < getTotalSteps() ? (
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
                  disabled={!canProceed() || isSubmitting}
                  className={`px-6 py-3 rounded-lg font-hana-medium transition-colors ${
                    canProceed() && !isSubmitting
                      ? 'bg-hana-green text-white hover:bg-green-600'
                      : 'bg-gray-100 text-gray-400 cursor-not-allowed'
                  }`}
                >
                  {isSubmitting ? '신청 중...' : '상담 신청하기'}
                </button>
              )}
            </div>
          </div>
        </div>

        {}
        {showProductNoticeModal && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={() => setShowProductNoticeModal(false)}
          >
            <div
              className="bg-white rounded-xl p-8 max-w-md w-full mx-4"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="text-center">
                <div className="w-16 h-16 bg-yellow-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-10 h-10 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                  </svg>
                </div>

                <h2 className="text-2xl font-hana-bold text-gray-900 mb-4">
                  상품 가입 상담 안내
                </h2>

                <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6 text-left space-y-3">
                  <div className="flex items-start">
                    <span className="text-yellow-600 mr-2 mt-0.5">📋</span>
                    <div>
                      <p className="font-hana-medium text-gray-900 mb-1">본인 확인 필수</p>
                      <p className="text-sm text-gray-700">
                        상품 가입 상담은 <span className="font-hana-bold text-hana-green">본인이 직접</span> 상담을 받으셔야 합니다.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <span className="text-yellow-600 mr-2 mt-0.5">🪪</span>
                    <div>
                      <p className="font-hana-medium text-gray-900 mb-1">신분증 준비</p>
                      <p className="text-sm text-gray-700">
                        상담 시 <span className="font-hana-bold text-hana-green">신분증(주민등록증, 운전면허증 등)</span>을 반드시 지참해 주세요.
                      </p>
                    </div>
                  </div>

                  <div className="flex items-start">
                    <span className="text-yellow-600 mr-2 mt-0.5">ℹ️</span>
                    <div>
                      <p className="text-sm text-gray-700">
                        대리인을 통한 상담은 불가능합니다.
                      </p>
                    </div>
                  </div>
                </div>

                <div className="flex space-x-3">
                  <button
                    onClick={() => setShowProductNoticeModal(false)}
                    className="flex-1 px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-hana-medium"
                  >
                    취소
                  </button>
                  <button
                    onClick={handleProductNoticeConfirm}
                    className="flex-1 px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                  >
                    확인했습니다
                  </button>
                </div>
              </div>
            </div>
          </div>
        )}

        {}
        {showSuccessModal && (
          <div
            className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50"
            onClick={() => {
              setShowSuccessModal(false);
              window.location.href = '/';
            }}
          >
            <div
              className="bg-white rounded-xl p-8 max-w-md w-full mx-4"
              onClick={(e) => e.stopPropagation()}
            >
              <div className="text-center">
                <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                </div>

                <h2 className="text-2xl font-hana-bold text-gray-900 mb-3">
                  상담 신청이 완료되었습니다!
                </h2>

                <div className="bg-gray-50 rounded-lg p-4 mb-6 text-left space-y-2">
                  <div className="flex justify-between">
                    <span className="text-gray-600">상담 번호</span>
                    <span className="font-hana-medium">{consultationId}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">상담 분야</span>
                    <span className="font-hana-medium">
                      {consultationFields.find(f => f.id === selectedField)?.name}
                    </span>
                  </div>
                  {selectedField === 'product' && selectedSubOption && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">선택 상품</span>
                      <span className="font-hana-medium">{getSelectedOptionName()}</span>
                    </div>
                  )}
                  {selectedField === 'asset-management' && selectedSubOption && (
                    <div className="flex justify-between">
                      <span className="text-gray-600">상담 유형</span>
                      <span className="font-hana-medium">{getSelectedOptionName()}</span>
                    </div>
                  )}
                  {selectedField === 'general' && generalRequest && (
                    <div className="flex flex-col">
                      <span className="text-gray-600 mb-1">요청 사항</span>
                      <span className="font-hana-regular text-sm bg-gray-50 p-2 rounded">{generalRequest}</span>
                    </div>
                  )}
                  <div className="flex justify-between">
                    <span className="text-gray-600">상담사</span>
                    <span className="font-hana-medium">{selectedCounselor?.userName}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-gray-600">예약 일시</span>
                    <span className="font-hana-medium">
                      {formatDate(selectedDate)} {selectedTimeSlot}
                    </span>
                  </div>
                </div>

                <p className="text-gray-600 mb-6">
                  예약 확인 후 확정 시 등록된 메일로 안내드리겠습니다.
                </p>

                <button
                  onClick={() => {
                    setShowSuccessModal(false);
                    window.location.href = '/main';
                  }}
                  className="w-full px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium"
                >
                  확인
                </button>
              </div>
            </div>
          </div>
        )}
      </div>
    </Layout>
  );
}

export default ConsultationRequest;