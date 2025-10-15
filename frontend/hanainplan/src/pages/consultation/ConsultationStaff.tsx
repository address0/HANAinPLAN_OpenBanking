import { useState, useEffect } from 'react';
import { useQuery } from '@tanstack/react-query';
import Layout from '../../components/layout/Layout';
import { fetchAvailableConsultantsAtTime } from '../../api/scheduleApi';
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

const parseSpecialization = (specialization: string | null): string[] => {
  if (!specialization) return [];
  try {
    const parsed = JSON.parse(specialization);
    return Array.isArray(parsed) ? parsed : [];
  } catch {
    return [];
  }
};

function ConsultationStaff() {
  const [selectedCounselor, setSelectedCounselor] = useState<Counselor | null>(null);
  const [selectedDate, setSelectedDate] = useState<string>('');
  const [selectedTime, setSelectedTime] = useState<string>('');
  const [availableConsultantIds, setAvailableConsultantIds] = useState<number[]>([]);
  const [isFilterActive, setIsFilterActive] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const { data: consultants = [], isLoading: consultantsLoading } = useQuery({
    queryKey: ['consultants'],
    queryFn: async () => {
      const response = await axios.get<Counselor[]>(`${API_BASE_URL}/api/consultants`);
      return response.data;
    }
  });

  const handleSearchAvailability = async () => {
    if (!selectedDate || !selectedTime) {
      alert('날짜와 시간을 선택해주세요.');
      return;
    }

    setIsLoading(true);
    try {
      const startTime = `${selectedDate}T${selectedTime}:00`;

      const [hours, minutes] = selectedTime.split(':').map(Number);
      const endHours = hours + 1;
      const endTime = `${selectedDate}T${endHours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}:00`;

      const availableIds = await fetchAvailableConsultantsAtTime(startTime, endTime);
      setAvailableConsultantIds(availableIds);
      setIsFilterActive(true);
    } catch (error) {
      alert('상담사 조회에 실패했습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleResetFilter = () => {
    setSelectedDate('');
    setSelectedTime('');
    setAvailableConsultantIds([]);
    setIsFilterActive(false);
  };

  const filteredCounselors = isFilterActive
    ? consultants.filter(counselor => availableConsultantIds.includes(counselor.consultantId))
    : consultants;

  if (consultantsLoading) {
    return (
      <Layout>
        <div className="min-h-screen bg-gray-50 flex items-center justify-center">
          <div className="text-center">
            <div className="animate-spin rounded-full h-16 w-16 border-b-4 border-hana-green mx-auto"></div>
            <p className="mt-4 text-gray-600 font-hana-regular">상담사 목록을 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="min-h-screen bg-gray-50">

        {}
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {}
          <div className="bg-white rounded-xl shadow-lg p-8 mb-8">
            <h2 className="text-3xl font-hana-bold text-gray-900 mb-4">전문 상담사와 함께하세요</h2>
            <p className="text-lg text-gray-600 mb-6">
              하나금융그룹의 숙련된 전문 상담사들이 고객님의 금융 목표 달성을 위해 맞춤형 상담을 제공해드립니다.
            </p>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="bg-hana-green/5 p-6 rounded-lg">
                <h3 className="text-xl font-hana-bold text-hana-green mb-3">전문성</h3>
                <p className="text-gray-700">풍부한 경력과 전문 자격을 보유한 상담사</p>
              </div>
              <div className="bg-blue-50 p-6 rounded-lg">
                <h3 className="text-xl font-hana-bold text-blue-600 mb-3">맞춤 상담</h3>
                <p className="text-gray-700">고객의 상황과 목표에 맞는 개인별 상담</p>
              </div>
            </div>
          </div>

          {}
          <div className="bg-white rounded-xl shadow-lg p-6 mb-8">
            <h3 className="text-2xl font-hana-bold text-gray-900 mb-4">상담 희망 시간 선택</h3>
            <p className="text-gray-600 mb-4">원하시는 날짜와 시간을 선택하시면 해당 시간에 가능한 상담사만 표시됩니다.</p>

            <div className="flex flex-wrap gap-4 items-end">
              <div className="flex-1 min-w-[200px]">
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  날짜 선택
                </label>
                <input
                  type="date"
                  value={selectedDate}
                  onChange={(e) => setSelectedDate(e.target.value)}
                  min={new Date().toISOString().split('T')[0]}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                />
              </div>

              <div className="flex-1 min-w-[200px]">
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  시간 선택
                </label>
                <select
                  value={selectedTime}
                  onChange={(e) => setSelectedTime(e.target.value)}
                  className="w-full px-4 py-3 border border-gray-300 rounded-lg focus:ring-2 focus:ring-hana-green focus:border-transparent font-hana-regular"
                >
                  <option value="">시간을 선택하세요</option>
                  <option value="09:00">오전 9:00</option>
                  <option value="09:30">오전 9:30</option>
                  <option value="10:00">오전 10:00</option>
                  <option value="10:30">오전 10:30</option>
                  <option value="11:00">오전 11:00</option>
                  <option value="11:30">오전 11:30</option>
                  <option value="12:00">오후 12:00</option>
                  <option value="12:30">오후 12:30</option>
                  <option value="13:00">오후 1:00</option>
                  <option value="13:30">오후 1:30</option>
                  <option value="14:00">오후 2:00</option>
                  <option value="14:30">오후 2:30</option>
                  <option value="15:00">오후 3:00</option>
                  <option value="15:30">오후 3:30</option>
                  <option value="16:00">오후 4:00</option>
                  <option value="16:30">오후 4:30</option>
                  <option value="17:00">오후 5:00</option>
                  <option value="17:30">오후 5:30</option>
                  <option value="18:00">오후 6:00</option>
                </select>
              </div>

              <div className="flex gap-2">
                <button
                  onClick={handleSearchAvailability}
                  disabled={isLoading || !selectedDate || !selectedTime}
                  className="px-6 py-3 bg-hana-green text-white rounded-lg hover:bg-green-600 transition-colors font-hana-medium disabled:opacity-50 disabled:cursor-not-allowed"
                >
                  {isLoading ? '조회 중...' : '가능한 상담사 조회'}
                </button>

                {isFilterActive && (
                  <button
                    onClick={handleResetFilter}
                    className="px-6 py-3 bg-gray-200 text-gray-700 rounded-lg hover:bg-gray-300 transition-colors font-hana-medium"
                  >
                    필터 초기화
                  </button>
                )}
              </div>
            </div>

            {isFilterActive && (
              <div className="mt-4 p-4 bg-green-50 border border-green-200 rounded-lg">
                <p className="text-green-800 font-hana-medium">
                  ✓ {selectedDate} {selectedTime}에 가능한 상담사 {filteredCounselors.length}명
                </p>
              </div>
            )}
          </div>

          {}
          <div className="mb-6">
            <h3 className="text-2xl font-hana-bold text-gray-900 mb-4">
              상담사 목록 {isFilterActive && `(${filteredCounselors.length})`}
            </h3>
          </div>

          {filteredCounselors.length === 0 ? (
            <div className="bg-white rounded-xl shadow-lg p-12 text-center">
              <p className="text-gray-600 text-lg font-hana-regular">
                선택한 시간에 가능한 상담사가 없습니다.
              </p>
              <p className="text-gray-500 text-sm font-hana-regular mt-2">
                다른 날짜나 시간을 선택해 주세요.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
              {filteredCounselors.map((counselor) => {
                const specialties = parseSpecialization(counselor.specialization);
                return (
                  <div
                    key={counselor.consultantId}
                    className="bg-white rounded-xl shadow-lg p-6 hover:shadow-xl transition-shadow cursor-pointer"
                    onClick={() => setSelectedCounselor(counselor)}
                  >
                    <div className="flex items-center mb-4">
                      <div className="w-16 h-16 rounded-full bg-hana-green/10 flex items-center justify-center mr-4">
                        <span className="text-2xl font-hana-bold text-hana-green">
                          {counselor.userName?.charAt(0) || '상'}
                        </span>
                      </div>
                      <div className="flex-1">
                        <h4 className="text-xl font-hana-bold text-gray-900">{counselor.userName || '상담사'}</h4>
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

                    {specialties.length > 0 && (
                      <div className="space-y-2 mb-4">
                        <p className="text-sm text-gray-500">전문분야</p>
                        <div className="flex flex-wrap gap-2">
                          {specialties.slice(0, 3).map((specialty, index) => (
                            <span key={index} className="bg-hana-green/10 text-hana-green px-2 py-1 rounded-full text-xs font-hana-medium">
                              {specialty}
                            </span>
                          ))}
                          {specialties.length > 3 && (
                            <span className="bg-gray-100 text-gray-600 px-2 py-1 rounded-full text-xs font-hana-medium">
                              +{specialties.length - 3}
                            </span>
                          )}
                        </div>
                      </div>
                    )}

                    <div className="grid grid-cols-2 gap-4 mb-4">
                      {counselor.branchName && (
                        <div className="space-y-1">
                          <p className="text-sm text-gray-500">소속 지점</p>
                          <p className="text-gray-700 font-hana-medium">{counselor.branchName}</p>
                        </div>
                      )}
                      {counselor.experienceYears && (
                        <div className="space-y-1">
                          <p className="text-sm text-gray-500">경력</p>
                          <p className="text-gray-700 font-hana-medium">{counselor.experienceYears}</p>
                        </div>
                      )}
                    </div>

                    <button className="mt-4 w-full bg-hana-green text-white py-2 rounded-lg hover:bg-green-600 transition-colors font-hana-medium">
                      상담사 정보 보기
                    </button>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {}
        {selectedCounselor && (() => {
          const specialties = parseSpecialization(selectedCounselor.specialization);
          return (
            <div
              className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4"
              onClick={() => setSelectedCounselor(null)}
            >
              <div
                className="bg-white rounded-xl max-w-3xl w-full max-h-[90vh] overflow-y-auto"
                onClick={(e) => e.stopPropagation()}
              >
                <div className="p-6 border-b">
                  <div className="flex items-center justify-between">
                    <div className="flex items-center">
                      <div className="w-20 h-20 rounded-full bg-hana-green/10 flex items-center justify-center mr-6">
                        <span className="text-3xl font-hana-bold text-hana-green">
                          {selectedCounselor.userName?.charAt(0) || '상'}
                        </span>
                      </div>
                      <div>
                        <h2 className="text-3xl font-hana-bold text-gray-900">{selectedCounselor.userName || '상담사'}</h2>
                        <p className="text-xl text-gray-600">{selectedCounselor.department || '일반상담팀'}</p>
                        <p className="text-lg text-gray-500">{selectedCounselor.position || '상담사'}</p>
                        <div className="flex items-center mt-2">
                          <span className="text-yellow-500">★</span>
                          <span className="text-gray-600 ml-1">
                            {selectedCounselor.consultationRating?.toFixed(1) || '0.0'} ({selectedCounselor.totalConsultations || 0}회 상담)
                          </span>
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
                  <div className="space-y-6">
                    {specialties.length > 0 && (
                      <div>
                        <h3 className="text-xl font-hana-bold text-gray-900 mb-3">전문분야</h3>
                        <div className="flex flex-wrap gap-2">
                          {specialties.map((specialty, index) => (
                            <span key={index} className="bg-hana-green/10 text-hana-green px-3 py-2 rounded-full text-sm font-hana-medium">
                              {specialty}
                            </span>
                          ))}
                        </div>
                      </div>
                    )}

                    <div>
                      <h3 className="text-xl font-hana-bold text-gray-900 mb-3">상담 정보</h3>
                      <div className="bg-gray-50 p-4 rounded-lg space-y-3">
                        {selectedCounselor.branchName && (
                          <div>
                            <p className="text-sm text-gray-500">소속 지점</p>
                            <p className="font-hana-medium">{selectedCounselor.branchName}</p>
                          </div>
                        )}
                        {selectedCounselor.experienceYears && (
                          <div>
                            <p className="text-sm text-gray-500">경력</p>
                            <p className="font-hana-medium">{selectedCounselor.experienceYears}</p>
                          </div>
                        )}
                        <div>
                          <p className="text-sm text-gray-500">총 상담 횟수</p>
                          <p className="font-hana-medium">{(selectedCounselor.totalConsultations || 0).toLocaleString()}회</p>
                        </div>
                        <div>
                          <p className="text-sm text-gray-500">평점</p>
                          <p className="font-hana-medium">{selectedCounselor.consultationRating?.toFixed(1) || '0.0'} / 5.0</p>
                        </div>
                        {selectedCounselor.consultationStatus && (
                          <div>
                            <p className="text-sm text-gray-500">상담 상태</p>
                            <p className="font-hana-medium">{selectedCounselor.consultationStatus}</p>
                          </div>
                        )}
                      </div>
                    </div>

                    {(selectedCounselor.workEmail || selectedCounselor.phoneNumber) && (
                      <div>
                        <h3 className="text-xl font-hana-bold text-gray-900 mb-3">연락처</h3>
                        <div className="bg-gray-50 p-4 rounded-lg space-y-2">
                          {selectedCounselor.phoneNumber && (
                            <div>
                              <p className="text-sm text-gray-500">전화번호</p>
                              <p className="font-hana-medium">{selectedCounselor.phoneNumber}</p>
                            </div>
                          )}
                          {selectedCounselor.workEmail && (
                            <div>
                              <p className="text-sm text-gray-500">이메일</p>
                              <p className="font-hana-medium">{selectedCounselor.workEmail}</p>
                            </div>
                          )}
                        </div>
                      </div>
                    )}

                    <div className="pt-4">
                      <button className="w-full bg-hana-green text-white py-3 rounded-lg hover:bg-green-600 transition-colors font-hana-medium text-lg">
                        상담 신청하기
                      </button>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          );
        })()}
      </div>
    </Layout>
  );
}

export default ConsultationStaff;