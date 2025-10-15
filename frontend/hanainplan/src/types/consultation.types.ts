
export interface Consultation {
  id: string;
  consultantId: string;
  consultantName: string;
  customerId: string;
  customerName: string;
  title: string;
  description: string;
  status: 'PENDING' | 'CONFIRMED' | 'COMPLETED' | 'CANCELLED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH';
  consultationDate: string;
  consultationTime: string;
  duration: number;
  meetingType: 'ONLINE' | 'OFFLINE' | 'PHONE';
  location?: string;
  notes?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConsultationRequest {
  id: string;
  customerId: string;
  customerName: string;
  customerPhone: string;
  customerEmail?: string;
  preferredConsultantId?: string;
  preferredDate: string;
  preferredTime: string;
  consultationType: 'GENERAL' | 'RETIREMENT' | 'INVESTMENT' | 'INSURANCE' | 'TAX';
  description: string;
  urgency: 'LOW' | 'MEDIUM' | 'HIGH';
  status: 'WAITING' | 'ASSIGNED' | 'CONFIRMED' | 'REJECTED';
  assignedConsultantId?: string;
  assignedConsultantName?: string;
  createdAt: string;
  updatedAt: string;
}

export interface ConsultationFilters {
  status?: string;
  priority?: string;
  consultationType?: string;
  dateRange?: {
    start: string;
    end: string;
  };
  search?: string;
}

export interface ConsultationStats {
  totalConsultations: number;
  pendingRequests: number;
  todayConsultations: number;
  completedConsultations: number;
}