import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { PieChart, Pie, Cell, BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import Layout from '../components/layout/Layout';
import AlertModal from '../components/modal/AlertModal';
import { getUserInfo, updateUserInfo, deleteAccount, changePassword } from '../api/userApi';
import type { UserInfoResponse, UserInfoUpdateRequest, PasswordChangeRequest } from '../api/userApi';
import { useUserStore } from '../store/userStore';

function UserProfile() {
  const navigate = useNavigate();
  const { user, updateUser, clearUser } = useUserStore();
  const [userInfo, setUserInfo] = useState<UserInfoResponse | null>(null);
  const [isLoading, setIsLoading] = useState(true);
  const [isEditing, setIsEditing] = useState(false);
  const [editedInfo, setEditedInfo] = useState<UserInfoUpdateRequest>({});
  const [showPasswordModal, setShowPasswordModal] = useState(false);
  const [showDeleteConfirm, setShowDeleteConfirm] = useState(false);
  const [deletePassword, setDeletePassword] = useState('');
  const [passwordForm, setPasswordForm] = useState<PasswordChangeRequest>({
    currentPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [showPassword, setShowPassword] = useState({
    current: false,
    new: false,
    confirm: false
  });

  // AlertModal 상태
  const [alertModal, setAlertModal] = useState({
    isOpen: false,
    title: '',
    message: '',
    type: 'info' as 'success' | 'error' | 'info' | 'warning'
  });

  useEffect(() => {
    loadUserInfo();
  }, []);

  const loadUserInfo = async () => {
    try {
      setIsLoading(true);
      
      // zustand store에서 사용자 정보 가져오기
      if (!user) {
        setAlertModal({
          isOpen: true,
          title: '로그인 필요',
          message: '로그인이 필요합니다.',
          type: 'error'
        });
        setTimeout(() => navigate('/login'), 2000);
        return;
      }

      const response = await getUserInfo(user.userId);
      setUserInfo(response);
      
      // 편집 데이터 초기화
      setEditedInfo({
        userBasicInfo: {
          userName: response.userBasicInfo.userName,
          phoneNumber: response.userBasicInfo.phoneNumber,
          email: response.userBasicInfo.email || ''
        },
        customerDetailInfo: response.customerDetailInfo ? {
          healthInfo: response.customerDetailInfo.healthInfo,
          jobInfo: response.customerDetailInfo.jobInfo
        } : undefined
      });

    } catch (error) {
      console.error('사용자 정보 로드 오류:', error);
      setAlertModal({
        isOpen: true,
        title: '오류',
        message: '사용자 정보를 불러오는 중 오류가 발생했습니다.',
        type: 'error'
      });
    } finally {
      setIsLoading(false);
    }
  };

  const handleSave = async () => {
    if (!userInfo) return;

    try {
      const response = await updateUserInfo(userInfo.userBasicInfo.userId, editedInfo);
      setUserInfo(response);
      
      // zustand store 업데이트
      if (editedInfo.userBasicInfo) {
        updateUser({
          name: editedInfo.userBasicInfo.userName || user?.name || '',
          phoneNumber: editedInfo.userBasicInfo.phoneNumber || user?.phoneNumber || ''
        });
      }
      
      setIsEditing(false);
      
      setAlertModal({
        isOpen: true,
        title: '저장 완료',
        message: '사용자 정보가 성공적으로 업데이트되었습니다.',
        type: 'success'
      });

    } catch (error) {
      console.error('사용자 정보 업데이트 오류:', error);
      setAlertModal({
        isOpen: true,
        title: '저장 실패',
        message: '사용자 정보 업데이트 중 오류가 발생했습니다.',
        type: 'error'
      });
    }
  };

  const handleDeleteAccount = async () => {
    if (!userInfo || !deletePassword.trim()) {
      setAlertModal({
        isOpen: true,
        title: '입력 오류',
        message: '비밀번호를 입력해주세요.',
        type: 'error'
      });
      return;
    }

    try {
      const response = await deleteAccount(userInfo.userBasicInfo.userId, deletePassword);
      
      if (response.success) {
        setAlertModal({
          isOpen: true,
          title: '계정 탈퇴 완료',
          message: response.message,
          type: 'success'
        });

        // zustand store 정리 후 로그인 페이지로 이동
        clearUser();
        setTimeout(() => navigate('/login'), 2000);
      } else {
        setAlertModal({
          isOpen: true,
          title: '계정 탈퇴 실패',
          message: response.message,
          type: 'error'
        });
      }

    } catch (error) {
      console.error('계정 탈퇴 오류:', error);
      setAlertModal({
        isOpen: true,
        title: '오류',
        message: '계정 탈퇴 처리 중 오류가 발생했습니다.',
        type: 'error'
      });
    } finally {
      setShowDeleteConfirm(false);
      setDeletePassword('');
    }
  };

  const handlePasswordChange = async () => {
    if (!userInfo) return;

    // 유효성 검사
    if (!passwordForm.currentPassword || !passwordForm.newPassword || !passwordForm.confirmPassword) {
      setAlertModal({
        isOpen: true,
        title: '입력 오류',
        message: '모든 필드를 입력해주세요.',
        type: 'error'
      });
      return;
    }

    if (passwordForm.newPassword !== passwordForm.confirmPassword) {
      setAlertModal({
        isOpen: true,
        title: '입력 오류',
        message: '새 비밀번호와 확인 비밀번호가 일치하지 않습니다.',
        type: 'error'
      });
      return;
    }

    try {
      const response = await changePassword(userInfo.userBasicInfo.userId, passwordForm);
      
      if (response.success) {
        setAlertModal({
          isOpen: true,
          title: '변경 완료',
          message: '비밀번호가 성공적으로 변경되었습니다.',
          type: 'success'
        });
        setShowPasswordModal(false);
        setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
      } else {
        setAlertModal({
          isOpen: true,
          title: '변경 실패',
          message: response.message,
          type: 'error'
        });
      }
    } catch (error) {
      setAlertModal({
        isOpen: true,
        title: '오류',
        message: '비밀번호 변경 중 오류가 발생했습니다.',
        type: 'error'
      });
    }
  };

  if (isLoading) {
    return (
      <Layout>
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-hana-green mx-auto mb-4"></div>
            <p className="text-gray-600 font-hana-medium">사용자 정보를 불러오는 중...</p>
          </div>
        </div>
      </Layout>
    );
  }

  if (!userInfo) {
    return (
      <Layout>
        <div className="flex items-center justify-center min-h-screen">
          <div className="text-center">
            <p className="text-gray-600 font-hana-medium">사용자 정보를 찾을 수 없습니다.</p>
            <button 
              onClick={() => navigate('/main')}
              className="mt-4 px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-hana-green/80 transition-colors"
            >
              홈으로 돌아가기
            </button>
          </div>
        </div>
      </Layout>
    );
  }

  return (
    <Layout>
      <div className="container mx-auto px-4 py-8 max-w-6xl">
        {/* 헤더 */}
        <div className="bg-white rounded-lg shadow-md p-6 mb-6">
          <div className="flex justify-between items-center">
            <div>
              <h1 className="text-2xl font-hana-bold text-gray-800">내 정보</h1>
              <p className="text-gray-600 font-hana-medium mt-1">
                개인정보 및 설정을 관리할 수 있습니다.
              </p>
            </div>
            <div className="flex gap-3">
              {isEditing ? (
                <>
                  <button
                    onClick={() => setIsEditing(false)}
                    className="px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-hana-medium"
                  >
                    취소
                  </button>
                  <button
                    onClick={handleSave}
                    className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-hana-green/80 transition-colors font-hana-medium"
                  >
                    저장
                  </button>
                </>
              ) : (
                <>
                  <button
                    onClick={() => setIsEditing(true)}
                    className="px-4 py-2 bg-hana-green text-white rounded-lg hover:bg-hana-green/80 transition-colors font-hana-medium"
                  >
                    정보 수정
                  </button>
                  <button
                    onClick={() => setShowPasswordModal(true)}
                    className="px-4 py-2 border border-hana-green text-hana-green rounded-lg hover:bg-hana-green/10 transition-colors font-hana-medium"
                  >
                    비밀번호 변경
                  </button>
                </>
              )}
            </div>
          </div>
        </div>

        <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
          {/* 기본 정보 */}
          <div className="bg-white rounded-lg shadow-md p-6">
            <h2 className="text-xl font-hana-bold text-gray-800 mb-4">기본 정보</h2>
            
            <div className="space-y-4">
              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-1">이름</label>
                {isEditing ? (
                  <input
                    type="text"
                    value={editedInfo.userBasicInfo?.userName || ''}
                    onChange={(e) => setEditedInfo({
                      ...editedInfo,
                      userBasicInfo: {
                        ...editedInfo.userBasicInfo,
                        userName: e.target.value
                      }
                    })}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                  />
                ) : (
                  <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.userBasicInfo.userName}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-1">전화번호</label>
                {isEditing ? (
                  <input
                    type="tel"
                    value={editedInfo.userBasicInfo?.phoneNumber || ''}
                    onChange={(e) => setEditedInfo({
                      ...editedInfo,
                      userBasicInfo: {
                        ...editedInfo.userBasicInfo,
                        phoneNumber: e.target.value
                      }
                    })}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                  />
                ) : (
                  <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.userBasicInfo.phoneNumber}</p>
                )}
              </div>

              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-1">이메일</label>
                {isEditing ? (
                  <input
                    type="email"
                    value={editedInfo.userBasicInfo?.email || ''}
                    onChange={(e) => setEditedInfo({
                      ...editedInfo,
                      userBasicInfo: {
                        ...editedInfo.userBasicInfo,
                        email: e.target.value
                      }
                    })}
                    className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                  />
                ) : (
                  <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.userBasicInfo.email || '이메일 없음'}</p>
                )}
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-1">성별</label>
                  <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.userBasicInfo.gender === 'MALE' ? '남성' : '여성'}</p>
                </div>
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-1">생년월일</label>
                  <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.userBasicInfo.birthDate}</p>
                </div>
              </div>

              <div>
                <label className="block text-sm font-hana-medium text-gray-700 mb-1">가입일</label>
                <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">
                  {new Date(userInfo.userBasicInfo.createdDate).toLocaleDateString()}
                </p>
              </div>
            </div>
          </div>

          {/* 자산 정보 차트 */}
          {userInfo.customerDetailInfo && (
            <div className="bg-white rounded-lg shadow-md p-6">
              <h2 className="text-xl font-hana-bold text-gray-800 mb-6">직업 및 자산 정보</h2>
              
              {/* 기본 직업 정보 */}
              <div className="space-y-4 mb-8">
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-1">산업 분야</label>
                  {isEditing ? (
                    <input
                      type="text"
                      value={editedInfo.customerDetailInfo?.jobInfo?.industryName || ''}
                      onChange={(e) => setEditedInfo({
                        ...editedInfo,
                        customerDetailInfo: {
                          ...editedInfo.customerDetailInfo,
                          jobInfo: {
                            ...editedInfo.customerDetailInfo?.jobInfo,
                            industryName: e.target.value
                          }
                        }
                      })}
                      className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      placeholder="예: 정보통신업"
                    />
                  ) : (
                    <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.customerDetailInfo.jobInfo.industryName}</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-1">경력 연수</label>
                  {isEditing ? (
                    <input
                      type="number"
                      min="0"
                      max="50"
                      value={editedInfo.customerDetailInfo?.jobInfo?.careerYears || 0}
                      onChange={(e) => setEditedInfo({
                        ...editedInfo,
                        customerDetailInfo: {
                          ...editedInfo.customerDetailInfo,
                          jobInfo: {
                            ...editedInfo.customerDetailInfo?.jobInfo,
                            careerYears: parseInt(e.target.value) || 0
                          }
                        }
                      })}
                      className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      placeholder="경력 연수를 입력하세요"
                    />
                  ) : (
                    <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">{userInfo.customerDetailInfo.jobInfo.careerYears}년</p>
                  )}
                </div>

                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-1">자산 수준</label>
                  {isEditing ? (
                    <select
                      value={editedInfo.customerDetailInfo?.jobInfo?.assetLevel || ''}
                      onChange={(e) => setEditedInfo({
                        ...editedInfo,
                        customerDetailInfo: {
                          ...editedInfo.customerDetailInfo,
                          jobInfo: {
                            ...editedInfo.customerDetailInfo?.jobInfo,
                            assetLevel: e.target.value
                          }
                        }
                      })}
                      className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                    >
                      <option value="">선택하세요</option>
                      <option value="LOW">낮음</option>
                      <option value="MEDIUM">보통</option>
                      <option value="HIGH">높음</option>
                      <option value="VERY_HIGH">매우 높음</option>
                    </select>
                  ) : (
                    <p className="p-3 bg-gray-50 rounded-lg font-hana-medium">
                      {userInfo.customerDetailInfo.jobInfo.assetLevel === 'LOW' ? '낮음' :
                       userInfo.customerDetailInfo.jobInfo.assetLevel === 'MEDIUM' ? '보통' :
                       userInfo.customerDetailInfo.jobInfo.assetLevel === 'HIGH' ? '높음' :
                       userInfo.customerDetailInfo.jobInfo.assetLevel === 'VERY_HIGH' ? '매우 높음' : '없음'}
                    </p>
                  )}
                </div>
              </div>

              {/* Recharts 시각화 */}
              <div className="space-y-6">
                <h3 className="text-lg font-hana-bold text-gray-700">자산 및 경력 현황</h3>
                
                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                  {/* 자산 수준 파이 차트 */}
                  <div>
                    <h4 className="text-md font-hana-bold text-gray-600 mb-4">자산 수준 분포</h4>
                    <ResponsiveContainer width="100%" height={200}>
                      <PieChart>
                        <Pie
                          data={[
                            { 
                              name: userInfo.customerDetailInfo.jobInfo.assetLevel === 'LOW' ? '낮음' :
                                    userInfo.customerDetailInfo.jobInfo.assetLevel === 'MEDIUM' ? '보통' :
                                    userInfo.customerDetailInfo.jobInfo.assetLevel === 'HIGH' ? '높음' :
                                    userInfo.customerDetailInfo.jobInfo.assetLevel === 'VERY_HIGH' ? '매우 높음' : '없음',
                              value: userInfo.customerDetailInfo.jobInfo.assetLevel === 'LOW' ? 25 :
                                     userInfo.customerDetailInfo.jobInfo.assetLevel === 'MEDIUM' ? 50 :
                                     userInfo.customerDetailInfo.jobInfo.assetLevel === 'HIGH' ? 75 :
                                     userInfo.customerDetailInfo.jobInfo.assetLevel === 'VERY_HIGH' ? 100 : 0
                            },
                            { 
                              name: '나머지', 
                              value: userInfo.customerDetailInfo.jobInfo.assetLevel === 'LOW' ? 75 :
                                     userInfo.customerDetailInfo.jobInfo.assetLevel === 'MEDIUM' ? 50 :
                                     userInfo.customerDetailInfo.jobInfo.assetLevel === 'HIGH' ? 25 :
                                     userInfo.customerDetailInfo.jobInfo.assetLevel === 'VERY_HIGH' ? 0 : 100
                            }
                          ]}
                          cx="50%"
                          cy="50%"
                          outerRadius={80}
                          fill="#008485"
                          dataKey="value"
                        >
                          <Cell fill="#008485" />
                          <Cell fill="#E5E7EB" />
                        </Pie>
                        <Tooltip />
                      </PieChart>
                    </ResponsiveContainer>
                  </div>

                  {/* 경력 연수 바 차트 */}
                  <div>
                    <h4 className="text-md font-hana-bold text-gray-600 mb-4">경력 현황</h4>
                    <ResponsiveContainer width="100%" height={200}>
                      <BarChart
                        data={[
                          { name: '현재 경력', years: userInfo.customerDetailInfo.jobInfo.careerYears },
                          { name: '목표 (20년)', years: 20 }
                        ]}
                      >
                        <CartesianGrid strokeDasharray="3 3" />
                        <XAxis dataKey="name" />
                        <YAxis />
                        <Tooltip />
                        <Bar dataKey="years" fill="#008485" />
                      </BarChart>
                    </ResponsiveContainer>
                  </div>
                </div>
              </div>
            </div>
          )}
        </div>

        {/* 건강 정보 테이블 */}
        {userInfo.customerDetailInfo && (
          <div className="bg-white rounded-lg shadow-md p-6 mt-6">
            <h2 className="text-xl font-hana-bold text-gray-800 mb-6">건강 정보</h2>
            
            {/* 건강 설문 정보 */}
            <div className="mb-8">
              <h3 className="text-lg font-hana-bold text-gray-700 mb-4">건강 설문</h3>
              <div className="overflow-x-auto">
                <table className="w-full border-collapse border border-gray-200">
                  <thead>
                    <tr className="bg-gray-50">
                      <th className="border border-gray-200 px-4 py-3 text-left font-hana-bold text-gray-700">항목</th>
                      <th className="border border-gray-200 px-4 py-3 text-center font-hana-bold text-gray-700">응답</th>
                    </tr>
                  </thead>
                  <tbody>
                    {[
                      { key: 'recentMedicalAdvice', label: '최근 의료진 상담 여부', value: userInfo.customerDetailInfo.healthInfo.recentMedicalAdvice },
                      { key: 'recentHospitalization', label: '최근 입원 경험 여부', value: userInfo.customerDetailInfo.healthInfo.recentHospitalization },
                      { key: 'majorDisease', label: '주요 질병 보유 여부', value: userInfo.customerDetailInfo.healthInfo.majorDisease },
                      { key: 'longTermMedication', label: '장기 복용 약물 여부', value: userInfo.customerDetailInfo.healthInfo.longTermMedication },
                      { key: 'disabilityRegistered', label: '장애 등록 여부', value: userInfo.customerDetailInfo.healthInfo.disabilityRegistered },
                      { key: 'insuranceRejection', label: '보험 거절 이력 여부', value: userInfo.customerDetailInfo.healthInfo.insuranceRejection }
                    ].map((question) => (
                      <tr key={question.key} className="hover:bg-gray-50">
                        <td className="border border-gray-200 px-4 py-3 font-hana-medium text-gray-700">
                          {question.label}
                        </td>
                        <td className="border border-gray-200 px-4 py-3 text-center">
                          {isEditing ? (
                            <div className="flex justify-center gap-4">
                              <label className="flex items-center">
                                <input
                                  type="radio"
                                  name={question.key}
                                  checked={editedInfo.customerDetailInfo?.healthInfo?.[question.key as keyof typeof editedInfo.customerDetailInfo.healthInfo] === true}
                                  onChange={() => setEditedInfo({
                                    ...editedInfo,
                                    customerDetailInfo: {
                                      ...editedInfo.customerDetailInfo,
                                      healthInfo: {
                                        ...editedInfo.customerDetailInfo?.healthInfo,
                                        [question.key]: true
                                      }
                                    }
                                  })}
                                  className="mr-2 text-hana-green focus:ring-hana-green"
                                />
                                <span className="font-hana-medium text-sm">예</span>
                              </label>
                              <label className="flex items-center">
                                <input
                                  type="radio"
                                  name={question.key}
                                  checked={editedInfo.customerDetailInfo?.healthInfo?.[question.key as keyof typeof editedInfo.customerDetailInfo.healthInfo] === false}
                                  onChange={() => setEditedInfo({
                                    ...editedInfo,
                                    customerDetailInfo: {
                                      ...editedInfo.customerDetailInfo,
                                      healthInfo: {
                                        ...editedInfo.customerDetailInfo?.healthInfo,
                                        [question.key]: false
                                      }
                                    }
                                  })}
                                  className="mr-2 text-hana-green focus:ring-hana-green"
                                />
                                <span className="font-hana-medium text-sm">아니오</span>
                              </label>
                            </div>
                          ) : (
                            <span className={`inline-flex px-3 py-1 rounded-full text-sm font-hana-medium ${
                              question.value 
                                ? 'bg-red-100 text-red-800' 
                                : 'bg-green-100 text-green-800'
                            }`}>
                              {question.value ? '예' : '아니오'}
                            </span>
                          )}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>

            {/* 질병 상세 정보 */}
            {userInfo.customerDetailInfo.healthInfo.diseaseDetails && userInfo.customerDetailInfo.healthInfo.diseaseDetails.length > 0 ? (
              <div>
                <h3 className="text-lg font-hana-bold text-gray-700 mb-4">질병 상세 정보</h3>
                <div className="overflow-x-auto">
                  <table className="w-full border-collapse border border-gray-200">
                    <thead>
                      <tr className="bg-gray-50">
                        <th className="border border-gray-200 px-4 py-3 text-left font-hana-bold text-gray-700">질병명</th>
                        <th className="border border-gray-200 px-4 py-3 text-left font-hana-bold text-gray-700">카테고리</th>
                        <th className="border border-gray-200 px-4 py-3 text-center font-hana-bold text-gray-700">위험도</th>
                        <th className="border border-gray-200 px-4 py-3 text-center font-hana-bold text-gray-700">심각도</th>
                        <th className="border border-gray-200 px-4 py-3 text-center font-hana-bold text-gray-700">진행 기간</th>
                        <th className="border border-gray-200 px-4 py-3 text-center font-hana-bold text-gray-700">만성 여부</th>
                      </tr>
                    </thead>
                    <tbody>
                      {userInfo.customerDetailInfo.healthInfo.diseaseDetails.map((disease, index) => (
                        <tr key={index} className="hover:bg-gray-50">
                          <td className="border border-gray-200 px-4 py-3 font-hana-medium text-gray-800">
                            {disease.diseaseName}
                          </td>
                          <td className="border border-gray-200 px-4 py-3 font-hana-medium text-gray-600">
                            {disease.diseaseCategory}
                          </td>
                          <td className="border border-gray-200 px-4 py-3 text-center">
                            <span className={`inline-flex px-2 py-1 rounded-full text-xs font-hana-medium ${
                              disease.riskLevel === 'HIGH' || disease.riskLevel === '높음' ? 'text-red-600 bg-red-50' :
                              disease.riskLevel === 'MEDIUM' || disease.riskLevel === '보통' ? 'text-yellow-600 bg-yellow-50' :
                              disease.riskLevel === 'LOW' || disease.riskLevel === '낮음' ? 'text-green-600 bg-green-50' :
                              'text-gray-600 bg-gray-50'
                            }`}>
                              {disease.riskLevel}
                            </span>
                          </td>
                          <td className="border border-gray-200 px-4 py-3 text-center">
                            <span className={`inline-flex px-2 py-1 rounded-full text-xs font-hana-medium ${
                              disease.severity === 'SEVERE' || disease.severity === '심각' ? 'text-red-600 bg-red-50' :
                              disease.severity === 'MODERATE' || disease.severity === '보통' ? 'text-yellow-600 bg-yellow-50' :
                              disease.severity === 'MILD' || disease.severity === '경미' ? 'text-green-600 bg-green-50' :
                              'text-gray-600 bg-gray-50'
                            }`}>
                              {disease.severity}
                            </span>
                          </td>
                          <td className="border border-gray-200 px-4 py-3 text-center font-hana-medium text-gray-600">
                            {disease.progressPeriod === 'UNDER_1MONTH' ? '1개월 미만' :
                             disease.progressPeriod === 'FROM_1_TO_3MONTHS' ? '1~3개월' :
                             disease.progressPeriod === 'FROM_3_TO_6MONTHS' ? '3~6개월' :
                             disease.progressPeriod === 'FROM_6_TO_12MONTHS' ? '6~12개월' :
                             disease.progressPeriod === 'OVER_1YEAR' ? '1년 이상' : '없음'}
                          </td>
                          <td className="border border-gray-200 px-4 py-3 text-center">
                            <span className={`inline-flex px-2 py-1 rounded-full text-xs font-hana-medium ${
                              disease.isChronic 
                                ? 'bg-orange-100 text-orange-800' 
                                : 'bg-blue-100 text-blue-800'
                            }`}>
                              {disease.isChronic ? '만성' : '급성'}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ) : (
              <div className="text-center py-8 text-gray-500">
                <div className="mb-4">
                  <svg className="w-16 h-16 mx-auto text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
                  </svg>
                </div>
                <p className="font-hana-medium">등록된 질병 정보가 없습니다.</p>
              </div>
            )}
          </div>
        )}

        {/* 계정 관리 */}
        <div className="bg-white rounded-lg shadow-md p-6 mt-6">
          <h2 className="text-xl font-hana-bold text-gray-800 mb-4">계정 관리</h2>
          <div className="border-t pt-4">
            <button
              onClick={() => setShowDeleteConfirm(true)}
              className="px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors font-hana-medium"
            >
              계정 탈퇴
            </button>
            <p className="text-sm text-gray-500 mt-2 font-hana-medium">
              계정을 탈퇴하면 모든 데이터가 삭제되며 복구할 수 없습니다.
            </p>
          </div>
        </div>

        {/* 비밀번호 변경 모달 */}
        {showPasswordModal && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md max-h-[90vh] overflow-y-auto">
              <div className="flex justify-between items-center mb-6">
                <h2 className="text-xl font-hana-bold text-gray-800">비밀번호 변경</h2>
                <button
                  onClick={() => {
                    setShowPasswordModal(false);
                    setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
                  }}
                  className="text-gray-400 hover:text-gray-600 transition-colors"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>

              <div className="space-y-4">
                {/* 현재 비밀번호 */}
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    현재 비밀번호
                  </label>
                  <div className="relative">
                    <input
                      type={showPassword.current ? "text" : "password"}
                      value={passwordForm.currentPassword}
                      onChange={(e) => setPasswordForm({...passwordForm, currentPassword: e.target.value})}
                      className="w-full p-3 pr-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      placeholder="현재 비밀번호를 입력하세요"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword({...showPassword, current: !showPassword.current})}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showPassword.current ? '🙈' : '👁️'}
                    </button>
                  </div>
                </div>

                {/* 새 비밀번호 */}
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    새 비밀번호
                  </label>
                  <div className="relative">
                    <input
                      type={showPassword.new ? "text" : "password"}
                      value={passwordForm.newPassword}
                      onChange={(e) => setPasswordForm({...passwordForm, newPassword: e.target.value})}
                      className="w-full p-3 pr-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      placeholder="새 비밀번호를 입력하세요"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword({...showPassword, new: !showPassword.new})}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showPassword.new ? '🙈' : '👁️'}
                    </button>
                  </div>
                </div>

                {/* 새 비밀번호 확인 */}
                <div>
                  <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                    새 비밀번호 확인
                  </label>
                  <div className="relative">
                    <input
                      type={showPassword.confirm ? "text" : "password"}
                      value={passwordForm.confirmPassword}
                      onChange={(e) => setPasswordForm({...passwordForm, confirmPassword: e.target.value})}
                      className="w-full p-3 pr-12 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-hana-green font-hana-medium"
                      placeholder="새 비밀번호를 다시 입력하세요"
                    />
                    <button
                      type="button"
                      onClick={() => setShowPassword({...showPassword, confirm: !showPassword.confirm})}
                      className="absolute right-3 top-1/2 transform -translate-y-1/2 text-gray-400 hover:text-gray-600"
                    >
                      {showPassword.confirm ? '🙈' : '👁️'}
                    </button>
                  </div>
                </div>
              </div>

              {/* 버튼 */}
              <div className="flex gap-3 pt-6">
                <button
                  onClick={() => {
                    setShowPasswordModal(false);
                    setPasswordForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
                  }}
                  className="flex-1 px-4 py-3 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-hana-medium"
                >
                  취소
                </button>
                <button
                  onClick={handlePasswordChange}
                  className="flex-1 px-4 py-3 bg-hana-green text-white rounded-lg hover:bg-hana-green/80 transition-colors font-hana-medium"
                >
                  변경
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 계정 탈퇴 확인 모달 */}
        {showDeleteConfirm && (
          <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
            <div className="bg-white rounded-lg p-6 w-full max-w-md">
              <h3 className="text-lg font-hana-bold text-gray-800 mb-4">계정 탈퇴 확인</h3>
              <p className="text-gray-600 mb-4 font-hana-medium">
                정말로 계정을 탈퇴하시겠습니까? 이 작업은 되돌릴 수 없습니다.
              </p>
              <div className="mb-4">
                <label className="block text-sm font-hana-medium text-gray-700 mb-2">
                  비밀번호 확인
                </label>
                <input
                  type="password"
                  value={deletePassword}
                  onChange={(e) => setDeletePassword(e.target.value)}
                  className="w-full p-3 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-500 font-hana-medium"
                  placeholder="현재 비밀번호를 입력하세요"
                />
              </div>
              <div className="flex gap-3">
                <button
                  onClick={() => {
                    setShowDeleteConfirm(false);
                    setDeletePassword('');
                  }}
                  className="flex-1 px-4 py-2 border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 transition-colors font-hana-medium"
                >
                  취소
                </button>
                <button
                  onClick={handleDeleteAccount}
                  className="flex-1 px-4 py-2 bg-red-500 text-white rounded-lg hover:bg-red-600 transition-colors font-hana-medium"
                >
                  탈퇴
                </button>
              </div>
            </div>
          </div>
        )}

        <AlertModal
          isOpen={alertModal.isOpen}
          onClose={() => setAlertModal(prev => ({ ...prev, isOpen: false }))}
          title={alertModal.title}
          message={alertModal.message}
          type={alertModal.type}
          autoClose={true}
          autoCloseDelay={3000}
        />
      </div>
    </Layout>
  );
}

export default UserProfile;
