import { useState } from 'react';
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import FullCalendar from '@fullcalendar/react';
import dayGridPlugin from '@fullcalendar/daygrid';
import timeGridPlugin from '@fullcalendar/timegrid';
import interactionPlugin from '@fullcalendar/interaction';
import koLocale from '@fullcalendar/core/locales/ko';
import { fetchSchedules, createSchedule, deleteSchedule, type ScheduleEvent } from '../../api/scheduleApi';
import './schedule.css';

interface DateSelectArg {
  start: Date;
  end: Date;
  startStr: string;
  endStr: string;
  allDay: boolean;
  view: any;
}

interface EventClickArg {
  el: HTMLElement;
  event: {
    id: string;
    title: string;
    start: Date | null;
    end: Date | null;
    allDay: boolean;
    [key: string]: any;
  };
  jsEvent: MouseEvent;
  view: any;
}

interface EventContentArg {
  event: {
    id: string;
    title: string;
    start: Date | null;
    end: Date | null;
    [key: string]: any;
  };
  timeText: string;
  backgroundColor: string;
  borderColor: string;
  textColor: string;
  view: any;
}

function ConsultantSchedule() {
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedDate, setSelectedDate] = useState<DateSelectArg | null>(null);
  const [selectedEvent, setSelectedEvent] = useState<ScheduleEvent | null>(null);
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    clientName: '',
    type: 'consultation' as 'consultation' | 'meeting' | 'other'
  });

  const queryClient = useQueryClient();

  const { data: schedules = [], isLoading } = useQuery({
    queryKey: ['schedules'],
    queryFn: fetchSchedules
  });

  const createMutation = useMutation({
    mutationFn: createSchedule,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] });
      closeModal();
      alert('일정이 생성되었습니다.');
    },
    onError: () => {
      alert('일정 생성에 실패했습니다.');
    }
  });

  const deleteMutation = useMutation({
    mutationFn: deleteSchedule,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['schedules'] });
      setSelectedEvent(null);
      alert('일정이 삭제되었습니다.');
    },
    onError: () => {
      alert('일정 삭제에 실패했습니다.');
    }
  });

  const handleDateSelect = (selectInfo: DateSelectArg) => {
    setSelectedDate(selectInfo);
    setIsModalOpen(true);
    setFormData({
      title: '',
      description: '',
      clientName: '',
      type: 'consultation'
    });
  };

  const handleEventClick = (clickInfo: EventClickArg) => {
    const event = schedules.find(e => e.id === clickInfo.event.id);
    if (event) {
      setSelectedEvent(event);
    }
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setSelectedDate(null);
    setFormData({
      title: '',
      description: '',
      clientName: '',
      type: 'consultation'
    });
  };

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();

    if (!selectedDate || !formData.title.trim()) {
      alert('제목을 입력해주세요.');
      return;
    }

    const newEvent: Omit<ScheduleEvent, 'id'> = {
      title: formData.title,
      start: selectedDate.startStr,
      end: selectedDate.endStr,
      description: formData.description,
      clientName: formData.clientName,
      type: formData.type
    };

    createMutation.mutate(newEvent);
  };

  const handleDelete = () => {
    if (selectedEvent && confirm('정말 삭제하시겠습니까?')) {
      deleteMutation.mutate(selectedEvent.id);
    }
  };

  const getEventColor = (type: string) => {
    switch (type.toLowerCase()) {
      case 'consultation':
        return {
          backgroundColor: '#008485',
          borderColor: '#008490',
        };
      case 'meeting':
        return {
          backgroundColor: '#3b82f6',
          borderColor: '#2563eb',
        };
      case 'other':
        return {
          backgroundColor: '#6b7280',
          borderColor: '#4b5563',
        };
      default:
        return {
          backgroundColor: '#6b7280',
          borderColor: '#4b5563',
        };
    }
  };

  const scheduleEvents = schedules.map(schedule => ({
    ...schedule,
    ...getEventColor(schedule.type || 'other')
  }));

  const formatTime24Hour = (date: Date | null) => {
    if (!date) return '';
    return date.toLocaleTimeString('ko-KR', {
      hour: '2-digit',
      minute: '2-digit',
      hour12: false
    });
  };

  const renderEventContent = (eventContent: EventContentArg) => {
    const startTime = formatTime24Hour(eventContent.event.start);
    const endTime = formatTime24Hour(eventContent.event.end);

    return (
      <div className="p-1">
        <div className="font-hana-medium text-xs text-white">{startTime}{endTime ? ` - ${endTime}` : ''}</div>
        <div className="font-hana-bold text-sm truncate text-white">{eventContent.event.title}</div>
      </div>
    );
  };

  if (isLoading) {
    return (
      <Layout>
        <div className="flex justify-center items-center min-h-[calc(100vh-240px)]">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto"></div>
            <p className="mt-4 text-gray-600 font-hana-regular">일정 정보를 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
          <h1 className="text-3xl font-hana-bold text-gray-900 mb-4">위클리 일정 관리</h1>

          {}
          <div className="bg-white rounded-xl shadow-lg p-6">
            <FullCalendar
              plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
              initialView="timeGridWeek"
              locale={koLocale}
              headerToolbar={{
                left: 'prev,next today',
                center: 'title',
                right: 'dayGridMonth,timeGridWeek,timeGridDay'
              }}
              slotMinTime="08:00:00"
              slotMaxTime="20:00:00"
              allDaySlot={false}
              expandRows={true}
              height="auto"
              selectable={true}
              selectMirror={true}
              dayMaxEvents={true}
              weekends={true}
              events={scheduleEvents}
              select={handleDateSelect}
              eventClick={handleEventClick}
              eventContent={renderEventContent}
              businessHours={{
                daysOfWeek: [1, 2, 3, 4, 5],
                startTime: '09:00',
                endTime: '18:00'
              }}
              slotDuration="00:30:00"
              snapDuration="00:15:00"
            />
          </div>

          {}
          {isModalOpen && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
                <h2 className="text-2xl font-hana-bold text-gray-900 mb-4">새 일정 추가</h2>
                <form onSubmit={handleSubmit}>
                  <div className="space-y-4">
                    {}
                    <div>
                      <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                        일정 유형
                      </label>
                      <select
                        value={formData.type}
                        onChange={(e) => setFormData({ ...formData, type: e.target.value as any })}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                      >
                        <option value="consultation">고객 상담</option>
                        <option value="meeting">회의</option>
                        <option value="other">기타</option>
                      </select>
                    </div>

                    {}
                    <div>
                      <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                        제목 *
                      </label>
                      <input
                        type="text"
                        value={formData.title}
                        onChange={(e) => setFormData({ ...formData, title: e.target.value })}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                        placeholder="일정 제목을 입력하세요"
                        required
                      />
                    </div>

                    {}
                    {formData.type === 'consultation' && (
                      <div>
                        <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                          고객명
                        </label>
                        <input
                          type="text"
                          value={formData.clientName}
                          onChange={(e) => setFormData({ ...formData, clientName: e.target.value })}
                          className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                          placeholder="고객명을 입력하세요"
                        />
                      </div>
                    )}

                    {}
                    <div>
                      <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                        설명
                      </label>
                      <textarea
                        value={formData.description}
                        onChange={(e) => setFormData({ ...formData, description: e.target.value })}
                        className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                        rows={3}
                        placeholder="일정 설명을 입력하세요"
                      />
                    </div>

                    {}
                    <div className="bg-gray-50 p-3 rounded-lg">
                      <p className="text-sm font-hana-medium text-gray-700">
                        선택된 시간: {selectedDate?.startStr ? new Date(selectedDate.startStr).toLocaleString('ko-KR') : ''} ~ {selectedDate?.endStr ? new Date(selectedDate.endStr).toLocaleString('ko-KR') : ''}
                      </p>
                    </div>
                  </div>

                  {}
                  <div className="flex gap-3 mt-6">
                    <button
                      type="button"
                      onClick={closeModal}
                      className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-hana-medium"
                    >
                      취소
                    </button>
                    <button
                      type="submit"
                      disabled={createMutation.isPending}
                      className="flex-1 px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium disabled:opacity-50"
                    >
                      {createMutation.isPending ? '생성 중...' : '생성'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}

          {}
          {selectedEvent && (
            <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
              <div className="bg-white rounded-xl shadow-xl p-6 w-full max-w-md">
                <h2 className="text-2xl font-hana-bold text-gray-900 mb-4">일정 상세</h2>
                <div className="space-y-3">
                  <div>
                    <p className="text-sm font-hana-medium text-gray-600">제목</p>
                    <p className="text-lg font-hana-bold text-gray-900">{selectedEvent.title}</p>
                  </div>
                  <div>
                    <p className="text-sm font-hana-medium text-gray-600">시간</p>
                    <p className="font-hana-regular text-gray-900">
                      {new Date(selectedEvent.start).toLocaleString('ko-KR')} ~ {new Date(selectedEvent.end).toLocaleString('ko-KR')}
                    </p>
                  </div>
                  {selectedEvent.clientName && (
                    <div>
                      <p className="text-sm font-hana-medium text-gray-600">고객명</p>
                      <p className="font-hana-regular text-gray-900">{selectedEvent.clientName}</p>
                    </div>
                  )}
                  {selectedEvent.description && (
                    <div>
                      <p className="text-sm font-hana-medium text-gray-600">설명</p>
                      <p className="font-hana-regular text-gray-900">{selectedEvent.description}</p>
                    </div>
                  )}
                  <div>
                    <p className="text-sm font-hana-medium text-gray-600">유형</p>
                    <p className="font-hana-regular text-gray-900">
                      {selectedEvent.type === 'consultation' ? '고객 상담' : selectedEvent.type === 'meeting' ? '회의' : '기타'}
                    </p>
                  </div>
                </div>
                <div className="flex gap-3 mt-6">
                  <button
                    onClick={() => setSelectedEvent(null)}
                    className="flex-1 px-4 py-2 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-hana-medium"
                  >
                    닫기
                  </button>
                  <button
                    onClick={handleDelete}
                    disabled={deleteMutation.isPending}
                    className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors font-hana-medium disabled:opacity-50"
                  >
                    {deleteMutation.isPending ? '삭제 중...' : '삭제'}
                  </button>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </Layout>
  );
}

export default ConsultantSchedule;