import type {
  Consultation,
  ConsultationRequest,
  ConsultationFilters,
  ConsultationStats
} from '../types/consultation.types';

// 목업 데이터 생성
const generateMockConsultations = (): Consultation[] => {
  const today = new Date();
  const consultations: Consultation[] = [
    {
      id: '1',
      consultantId: 'consultant1',
      consultantName: '김상담',
      customerId: 'customer1',
      customerName: '박철수',
      title: '노후 준비 상담',
      description: '퇴직 후 생활비 계획에 대한 상담이 필요합니다.',
      status: 'CONFIRMED',
      priority: 'HIGH',
      consultationDate: today.toISOString().split('T')[0],
      consultationTime: '10:00',
      duration: 60,
      meetingType: 'ONLINE',
      notes: '온라인 화상 상담으로 진행 예정',
      createdAt: new Date(today.getTime() - 7 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: '2',
      consultantId: 'consultant1',
      consultantName: '김상담',
      customerId: 'customer2',
      customerName: '이영희',
      title: '펀드 투자 상담',
      description: '안정적인 펀드 상품에 대한 추천을 받고 싶습니다.',
      status: 'CONFIRMED',
      priority: 'MEDIUM',
      consultationDate: today.toISOString().split('T')[0],
      consultationTime: '14:00',
      duration: 45,
      meetingType: 'ONLINE',
      notes: '고객님의 위험 성향에 따른 맞춤 추천 필요',
      createdAt: new Date(today.getTime() - 5 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: '3',
      consultantId: 'consultant1',
      consultantName: '김상담',
      customerId: 'customer3',
      customerName: '김민수',
      title: 'IRP 계좌 개설 상담',
      description: '퇴직연금 IRP 계좌 개설에 대한 상세한 설명이 필요합니다.',
      status: 'PENDING',
      priority: 'LOW',
      consultationDate: new Date(today.getTime() + 2 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      consultationTime: '11:00',
      duration: 30,
      meetingType: 'PHONE',
      notes: '전화 상담으로 진행',
      createdAt: new Date(today.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: '4',
      consultantId: 'consultant1',
      consultantName: '김상담',
      customerId: 'customer4',
      customerName: '정수진',
      title: '보험 포트폴리오 리뷰',
      description: '현재 보유 중인 보험 상품들에 대한 종합적인 리뷰를 받고 싶습니다.',
      status: 'COMPLETED',
      priority: 'MEDIUM',
      consultationDate: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      consultationTime: '15:30',
      duration: 60,
      meetingType: 'OFFLINE',
      location: '하나은행 강남지점',
      notes: '대면 상담 완료. 추가 서류 제출 필요',
      createdAt: new Date(today.getTime() - 10 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString()
    }
  ];
  return consultations;
};

const generateMockRequests = (): ConsultationRequest[] => {
  const today = new Date();
  const requests: ConsultationRequest[] = [
    {
      id: '1',
      customerId: 'customer5',
      customerName: '오나래',
      customerPhone: '010-1234-5678',
      customerEmail: 'narae.oh@email.com',
      preferredConsultantId: 'consultant1',
      preferredDate: new Date(today.getTime() + 3 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      preferredTime: '13:00',
      consultationType: 'RETIREMENT',
      description: '퇴직 후 자산 관리에 대한 전문적인 조언이 필요합니다.',
      urgency: 'HIGH',
      status: 'WAITING',
      createdAt: new Date(today.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 2 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: '2',
      customerId: 'customer6',
      customerName: '서준호',
      customerPhone: '010-9876-5432',
      customerEmail: 'junho.seo@email.com',
      preferredConsultantId: 'consultant1',
      preferredDate: new Date(today.getTime() + 5 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      preferredTime: '10:30',
      consultationType: 'INVESTMENT',
      description: '주식과 펀드 투자에 대한 기초 교육과 포트폴리오 구성에 대해 상담하고 싶습니다.',
      urgency: 'MEDIUM',
      status: 'WAITING',
      createdAt: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString()
    },
    {
      id: '3',
      customerId: 'customer7',
      customerName: '한지민',
      customerPhone: '010-5555-6666',
      customerEmail: 'jimin.han@email.com',
      preferredConsultantId: 'consultant1',
      preferredDate: new Date(today.getTime() + 7 * 24 * 60 * 60 * 1000).toISOString().split('T')[0],
      preferredTime: '16:00',
      consultationType: 'INSURANCE',
      description: '건강보험과 암보험에 대한 비교 상담을 받고 싶습니다.',
      urgency: 'LOW',
      status: 'ASSIGNED',
      assignedConsultantId: 'consultant1',
      assignedConsultantName: '김상담',
      createdAt: new Date(today.getTime() - 3 * 24 * 60 * 60 * 1000).toISOString(),
      updatedAt: new Date(today.getTime() - 1 * 24 * 60 * 60 * 1000).toISOString()
    }
  ];
  return requests;
};

// API 함수들
export const getConsultations = async (filters?: ConsultationFilters): Promise<Consultation[]> => {
  // 목업 데이터 반환
  let consultations = generateMockConsultations();

  if (filters) {
    if (filters.search) {
      const searchTerm = filters.search.toLowerCase();
      consultations = consultations.filter(c =>
        c.title.toLowerCase().includes(searchTerm) ||
        c.customerName.toLowerCase().includes(searchTerm) ||
        c.description.toLowerCase().includes(searchTerm)
      );
    }

    if (filters.status && filters.status !== 'ALL') {
      consultations = consultations.filter(c => c.status === filters.status);
    }

    if (filters.priority && filters.priority !== 'ALL') {
      consultations = consultations.filter(c => c.priority === filters.priority);
    }
  }

  return consultations;
};

export const getConsultationRequests = async (): Promise<ConsultationRequest[]> => {
  // 목업 데이터 반환
  return generateMockRequests();
};

export const getTodayConsultations = async (): Promise<Consultation[]> => {
  const today = new Date().toISOString().split('T')[0];
  const consultations = await getConsultations();
  return consultations.filter(c => c.consultationDate === today && c.status === 'CONFIRMED');
};

export const getConsultationStats = async (): Promise<ConsultationStats> => {
  const consultations = await getConsultations();
  const requests = await getConsultationRequests();
  const today = new Date().toISOString().split('T')[0];

  return {
    totalConsultations: consultations.length,
    pendingRequests: requests.filter(r => r.status === 'WAITING').length,
    todayConsultations: consultations.filter(c => c.consultationDate === today).length,
    completedConsultations: consultations.filter(c => c.status === 'COMPLETED').length
  };
};

export const updateConsultationStatus = async (id: string, status: Consultation['status']): Promise<void> => {
  // 목업 - 실제로는 API 호출
  console.log(`상담 ${id} 상태를 ${status}로 업데이트`);
};

export const assignConsultationRequest = async (requestId: string, consultantId: string): Promise<void> => {
  // 목업 - 실제로는 API 호출
  console.log(`상담 요청 ${requestId}를 상담사 ${consultantId}에게 배정`);
};

export const rejectConsultationRequest = async (requestId: string, reason?: string): Promise<void> => {
  // 목업 - 실제로는 API 호출
  console.log(`상담 요청 ${requestId}를 거절: ${reason || '사유 없음'}`);
};
