import React, { useState, useEffect, useRef } from 'react';
import WebSocketService from '../services/WebSocketService';
import WebRTCService from '../services/WebRTCService';
import type { CallState } from '../services/WebRTCService';
import type { CallRequestMessage, WebRTCMessage } from '../services/WebSocketService';
import InsuranceDashboard from '../components/consultation/InsuranceDashboard';

// 사용자 역할 타입 정의
type UserRole = 'counselor' | 'customer';

interface UserInfo {
  id: number;
  name: string;
  role: UserRole;
  profileImage?: string;
  department?: string;
  certification?: string;
}

interface ConsultationInfo {
  id?: string;
  type: string;
  duration: number;
  notes: string;
  status: 'scheduled' | 'in-progress' | 'completed' | 'cancelled';
}

interface HighlightInfo {
  id: string;
  text: string;
  blockId: string;
  startIndex: number;
  endIndex: number;
  color: string;
  author: 'counselor' | 'customer';
}

const VideoCall: React.FC = () => {
  // 상태 관리
  const [currentUser, setCurrentUser] = useState<UserInfo>({
    id: 1,
    name: '김상담',
    role: 'counselor',
    department: '자산관리팀',
    certification: 'AFP, CFP'
  });
  
  // 상대방 사용자 설정 (현재 사용자 역할에 따라 동적으로 결정)
  const targetUser: UserInfo = currentUser.role === 'counselor' 
    ? {
    id: 2,
    name: '이고객',
    role: 'customer'
      }
    : {
        id: 1,
        name: '김상담',
        role: 'counselor',
        department: '자산관리팀',
        certification: 'AFP, CFP'
      };
  const [consultationInfo, setConsultationInfo] = useState<ConsultationInfo>({
    type: '금융상담',
    duration: 0,
    notes: '',
    status: 'scheduled'
  });
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [callState, setCallState] = useState<CallState>({
    isInCall: false,
    roomId: null,
    isConnected: false,
    localStream: null,
    remoteStream: null,
    callerId: null,
    calleeId: null,
    isCaller: false
  });
  const [incomingCall, setIncomingCall] = useState<CallRequestMessage | null>(null);
  const [error, setError] = useState<string>('');
  const [isAudioEnabled, setIsAudioEnabled] = useState<boolean>(true);
  const [isVideoEnabled, setIsVideoEnabled] = useState<boolean>(true);
  const [isMediaInitialized, setIsMediaInitialized] = useState<boolean>(false);
  const [consultationStartTime, setConsultationStartTime] = useState<Date | null>(null);
  const [consultationNotes, setConsultationNotes] = useState<string>('');
  const [highlights, setHighlights] = useState<HighlightInfo[]>([]);
  const [currentStep, setCurrentStep] = useState<number>(0);

  // Video 요소 참조
  const localVideoRef = useRef<HTMLVideoElement>(null);
  const remoteVideoRef = useRef<HTMLVideoElement>(null);

  // 초기화 및 이벤트 리스너 설정
  useEffect(() => {
    setupEventListeners();
    return () => {
      cleanup();
    };
  }, []);

  // 로컬 비디오 스트림 설정
  useEffect(() => {
    if (callState.localStream && localVideoRef.current) {
      localVideoRef.current.srcObject = callState.localStream;
    }
  }, [callState.localStream]);

  // 원격 비디오 스트림 설정
  useEffect(() => {
    if (callState.remoteStream && remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = callState.remoteStream;
      
      // 비디오 재생 확인
      remoteVideoRef.current.play().catch(error => {
        console.error('Error playing remote video:', error);
      });
    }
  }, [callState.remoteStream]);

  // 미디어 초기화 상태 동기화
  useEffect(() => {
    setIsMediaInitialized(!!callState.localStream);
  }, [callState.localStream]);

  // 이벤트 리스너 설정
  const setupEventListeners = () => {
    // WebSocket 이벤트
    WebSocketService.onConnectionStateChange((connected) => {
      setIsConnected(connected);
    });

    WebSocketService.onCallRequest((callRequest) => {
      setIncomingCall(callRequest);
    });

    WebSocketService.onCallAccept((message) => {
      // 현재 사용자가 발신자라면(수신 알림의 receiverId가 나), 수락한 상대(senderId)에게 Offer 전송
      if (message.receiverId === currentUser.id) {
        WebRTCService.sendOffer(message.roomId, message.senderId);
      }
    });

    WebSocketService.onCallReject(() => {
      setError('통화가 거절되었습니다.');
      setTimeout(() => setError(''), 3000);
    });

    WebSocketService.onCallEnd(() => {
      handleEndCall();
    });

    // WebRTC 이벤트
    WebRTCService.onCallStateChange((state) => {
      setCallState(state);
    });

    WebRTCService.onConnectionStateChange((state) => {
      console.log('WebRTC Connection State:', state);
    });

    WebRTCService.onError((error) => {
      console.log('WebRTC Error:', error);
      setError(error.message);
      setTimeout(() => setError(''), 5000);
    });

    // 형광펜 동기화 이벤트
    WebSocketService.onHighlightSync((message) => {
      if (message.type === 'HIGHLIGHT_ADD') {
        setHighlights(prev => [...prev, message.data]);
      } else if (message.type === 'HIGHLIGHT_REMOVE') {
        setHighlights(prev => prev.filter(h => h.id !== message.data.highlightId));
      }
    });

    // 단계 동기화 이벤트
    WebSocketService.onStepSync((message) => {
      if (message.type === 'STEP_SYNC') {
        setCurrentStep(message.data.step);
      }
    });
  };

  // 정리 함수
  const cleanup = () => {
    WebSocketService.disconnect();
    WebRTCService.endCall();
  };

  // WebSocket 연결
  const handleConnect = async () => {
    try {
      await WebSocketService.connect(currentUser.id);
      setError('');
    } catch (error) {
      console.error('Connection failed:', error);
      setError('WebSocket 연결에 실패했습니다.');
    }
  };

  // WebSocket 연결 해제
  const handleDisconnect = () => {
    WebSocketService.disconnect();
    setIncomingCall(null);
  };

  // 상담 시작
  const handleStartCall = async () => {
    try {
      setConsultationInfo(prev => ({ ...prev, status: 'in-progress' }));
      
      const response = await fetch('http://localhost:8080/api/webrtc/call/request', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          callerId: currentUser.id,
          calleeId: targetUser.id,
          callerName: currentUser.name,
          calleeName: targetUser.name,
          consultationType: consultationInfo.type
        }),
      });

      const data = await response.json();
      
      if (data.success) {
        await WebRTCService.startCall(data.roomId, targetUser.id);
        setConsultationStartTime(new Date());
        setError('');
      } else {
        setError(data.message || '상담 요청에 실패했습니다.');
        setConsultationInfo(prev => ({ ...prev, status: 'scheduled' }));
      }
    } catch (error) {
      console.error('Error starting call:', error);
      setError('상담을 시작할 수 없습니다.');
      setConsultationInfo(prev => ({ ...prev, status: 'scheduled' }));
    }
  };

  // 상담 수락
  const handleAcceptCall = async () => {
    if (!incomingCall) return;

    try {
      setConsultationInfo(prev => ({ ...prev, status: 'in-progress' }));
      
      // WebSocket으로 수락 메시지 전송
      const acceptMessage: WebRTCMessage = {
        type: 'CALL_ACCEPT',
        roomId: incomingCall.roomId,
        senderId: currentUser.id,
        receiverId: incomingCall.callerId,
        data: null
      };
      WebSocketService.sendCallAccept(acceptMessage);

      // WebRTC 연결 시작
      await WebRTCService.acceptCall(incomingCall.roomId, incomingCall.callerId);
      
      setIncomingCall(null);
      setConsultationStartTime(new Date());
      setError('');
    } catch (error) {
      console.error('Error accepting call:', error);
      setError('상담 수락에 실패했습니다.');
      setConsultationInfo(prev => ({ ...prev, status: 'scheduled' }));
    }
  };

  // 상담 거절
  const handleRejectCall = () => {
    if (!incomingCall) return;

    const rejectMessage: WebRTCMessage = {
      type: 'CALL_REJECT',
      roomId: incomingCall.roomId,
      senderId: currentUser.id,
      receiverId: incomingCall.callerId,
      data: null
    };
    WebSocketService.sendCallReject(rejectMessage);
    setIncomingCall(null);
  };

  // 상담 종료
  const handleEndCall = () => {
    if (callState.roomId) {
      const endMessage: WebRTCMessage = {
        type: 'CALL_END',
        roomId: callState.roomId,
        senderId: currentUser.id,
        receiverId: callState.isCaller ? callState.calleeId! : callState.callerId!,
        data: null
      };
      WebSocketService.sendCallEnd(endMessage);
    }
    
    setConsultationInfo(prev => ({ ...prev, status: 'completed' }));
    WebRTCService.endCall();
    setIncomingCall(null);
    setConsultationStartTime(null);
  };

  // 상담 시간 계산
  const getConsultationDuration = () => {
    if (!consultationStartTime) return '00:00';
    
    const now = new Date();
    const diff = now.getTime() - consultationStartTime.getTime();
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);
    
    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  // 상담 메모 저장
  const handleSaveNotes = async () => {
    try {
    // 실제로는 서버에 저장하는 API 호출
      const response = await fetch('http://localhost:8080/api/consultation/notes', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          consultationId: callState.roomId,
          counselorId: currentUser.id,
          customerId: targetUser.id,
          consultationType: consultationInfo.type,
          notes: consultationNotes,
          duration: consultationStartTime ? Math.floor((new Date().getTime() - consultationStartTime.getTime()) / 1000) : 0,
          timestamp: new Date().toISOString()
        }),
      });

      if (response.ok) {
        console.log('상담 메모 저장 성공:', consultationNotes);
        alert('상담 기록이 성공적으로 저장되었습니다.');
      } else {
        console.error('상담 메모 저장 실패');
        alert('상담 기록 저장에 실패했습니다. 다시 시도해주세요.');
      }
    } catch (error) {
      console.error('Error saving consultation notes:', error);
      // 오프라인 모드에서는 로컬 스토리지에 저장
      localStorage.setItem(`consultation_notes_${callState.roomId}`, JSON.stringify({
        consultationId: callState.roomId,
        notes: consultationNotes,
        timestamp: new Date().toISOString(),
        consultationType: consultationInfo.type
      }));
      alert('상담 기록이 임시 저장되었습니다. (오프라인 모드)');
    }
  };

  // 마이크 토글
  const handleToggleMicrophone = () => {
    const enabled = WebRTCService.toggleMicrophone();
    setIsAudioEnabled(enabled);
  };

  // 비디오 토글
  const handleToggleVideo = () => {
    const enabled = WebRTCService.toggleVideo();
    setIsVideoEnabled(enabled);
  };

  // 미디어 테스트 (카메라/마이크 초기화)
  const handleMediaTest = async () => {
    try {
      await WebRTCService.initializeMedia(true, true);
      setError('');
    } catch (error) {
      console.error('Media test failed:', error);
      setError('카메라 또는 마이크에 접근할 수 없습니다.');
    }
  };

  // 미디어 중지
  const handleStopMedia = () => {
    WebRTCService.stopMediaForTest();
  };

  // 형광펜 추가
  const handleHighlightAdd = (highlight: Omit<HighlightInfo, 'id'>) => {
    const newHighlight: HighlightInfo = {
      ...highlight,
      id: Date.now().toString() + Math.random().toString(36).substr(2, 9)
    };
    
    setHighlights(prev => [...prev, newHighlight]);
    
    // WebSocket으로 다른 사용자에게 동기화
    if (callState.roomId) {
      const syncMessage: WebRTCMessage = {
        type: 'HIGHLIGHT_ADD',
        roomId: callState.roomId,
        senderId: currentUser.id,
        receiverId: targetUser.id,
        data: newHighlight
      };
      WebSocketService.sendHighlightSync(syncMessage);
    }
  };

  // 형광펜 제거
  const handleHighlightRemove = (highlightId: string) => {
    setHighlights(prev => prev.filter(h => h.id !== highlightId));
    
    // WebSocket으로 다른 사용자에게 동기화
    if (callState.roomId) {
      const syncMessage: WebRTCMessage = {
        type: 'HIGHLIGHT_REMOVE',
        roomId: callState.roomId,
        senderId: currentUser.id,
        receiverId: targetUser.id,
        data: { highlightId }
      };
      WebSocketService.sendHighlightSync(syncMessage);
    }
  };

  // 단계 변경
  const handleStepChange = (step: number) => {
    setCurrentStep(step);
    
    // WebSocket으로 다른 사용자에게 동기화
    if (callState.roomId) {
      const syncMessage: WebRTCMessage = {
        type: 'STEP_SYNC',
        roomId: callState.roomId,
        senderId: currentUser.id,
        receiverId: targetUser.id,
        data: { step }
      };
      WebSocketService.sendStepSync(syncMessage);
    }
  };

  return (
    <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100">
      {/* 헤더 */}
      <div className={`shadow-sm border-b ${
        currentUser.role === 'counselor' 
          ? 'bg-gradient-to-r from-blue-600 to-blue-700 text-white' 
          : 'bg-gradient-to-r from-green-600 to-green-700 text-white'
      }`}>
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-4">
                {/* 하나금융그룹 로고 */}
                <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center">
                  <img src="/images/img-hana-symbol.png" alt="하나금융그룹" className="w-8 h-8" />
                </div>
                
                <div>
                  <h1 className="text-2xl font-bold">
                    {currentUser.role === 'counselor' ? '전문상담사' : '고객상담'} 시스템
                  </h1>
                  <div className="flex items-center space-x-4 mt-1">
                    <p className="text-lg opacity-90">
                      {currentUser.role === 'counselor' ? '상담사' : '고객'}: {currentUser.name}
                    </p>
                    {currentUser.role === 'counselor' && currentUser.department && (
                      <span className="px-2 py-1 bg-white bg-opacity-20 rounded-full text-sm">
                        {currentUser.department}
                      </span>
                    )}
                    {currentUser.role === 'counselor' && currentUser.certification && (
                      <span className="px-2 py-1 bg-white bg-opacity-20 rounded-full text-sm">
                        {currentUser.certification}
                      </span>
                    )}
                  </div>
                </div>
              </div>
            </div>
            
            <div className="flex items-center space-x-6">
              {/* 상담 상태 */}
              <div className={`px-4 py-2 rounded-lg ${
                consultationInfo.status === 'in-progress' 
                  ? 'bg-green-500 bg-opacity-20 border border-green-300' 
                  : 'bg-gray-500 bg-opacity-20 border border-gray-300'
              }`}>
                <span className="text-sm font-medium">
                  {consultationInfo.status === 'in-progress' ? '상담 진행 중' : '상담 대기 중'}
                </span>
              </div>
              
              {/* 상담 시간 */}
              {consultationStartTime && (
                <div className="bg-white bg-opacity-20 px-4 py-2 rounded-lg">
                  <span className="text-sm font-medium">
                    상담 시간: {getConsultationDuration()}
                  </span>
                </div>
              )}
              
              {/* 연결 상태 */}
              <div className="flex items-center space-x-2">
                <div className={`w-3 h-3 rounded-full ${
                  isConnected ? 'bg-green-400' : 'bg-red-400'
                }`}></div>
                <span className="text-sm">
                  {isConnected ? '시스템 연결됨' : '연결 안됨'}
                </span>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="max-w-7xl mx-auto p-4">

        {/* 에러 메시지 */}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 text-red-700 px-4 py-3 mb-6 rounded-r-lg">
            <div className="flex">
              <div className="ml-3">
                <p className="text-sm font-medium">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* 상담 완료 알림 */}
        {consultationInfo.status === 'completed' && (
          <div className="bg-gradient-to-r from-green-50 to-emerald-50 border-l-4 border-green-500 text-green-800 px-6 py-6 mb-6 rounded-r-lg shadow-lg">
            <div className="flex items-center space-x-4">
              <div className="w-16 h-16 bg-green-100 rounded-full flex items-center justify-center">
                <svg className="w-8 h-8 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                </svg>
              </div>
              <div>
                <h3 className="font-bold text-xl text-green-900 mb-2">상담이 완료되었습니다</h3>
                <div className="space-y-1 text-sm">
                  <p className="text-green-700">
                    <span className="font-medium">상담 유형:</span> {consultationInfo.type}
                  </p>
                  <p className="text-green-700">
                    <span className="font-medium">상담 시간:</span> {getConsultationDuration()}
                  </p>
                  <p className="text-green-700">
                    <span className="font-medium">상담실:</span> {callState.roomId}
                  </p>
                </div>
              </div>
            </div>
          </div>
        )}

        {/* 수신 상담 알림 */}
        {incomingCall && (
          <div className="bg-gradient-to-r from-blue-50 to-indigo-50 border-l-4 border-blue-500 text-blue-800 px-6 py-6 mb-6 rounded-r-lg shadow-lg">
            <div className="flex items-center justify-between">
              <div className="flex items-center space-x-4">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center animate-pulse">
                  <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 5a2 2 0 012-2h3.28a1 1 0 01.948.684l1.498 4.493a1 1 0 01-.502 1.21l-2.257 1.13a11.042 11.042 0 005.516 5.516l1.13-2.257a1 1 0 011.21-.502l4.493 1.498a1 1 0 01.684.949V19a2 2 0 01-2 2h-1C9.716 21 3 14.284 3 6V5z" />
                  </svg>
                </div>
                <div>
                  <div className="flex items-center space-x-2 mb-1">
                    <h3 className="font-bold text-xl text-blue-900">상담 요청이 들어왔습니다</h3>
                    <span className="px-2 py-1 bg-blue-200 text-blue-800 text-xs rounded-full font-medium">
                      긴급
                    </span>
                  </div>
                  <p className="text-blue-700 mb-2">
                    {incomingCall.callerName}님께서 {consultationInfo.type} 상담을 요청하셨습니다.
                  </p>
                  <p className="text-sm text-blue-600">
                    상담실: {incomingCall.roomId} | 요청 시간: {new Date().toLocaleTimeString()}
                  </p>
                </div>
              </div>
              <div className="flex space-x-3">
                <button
                  onClick={handleAcceptCall}
                  className="bg-green-500 hover:bg-green-600 text-white px-8 py-4 rounded-lg font-semibold transition-all duration-200 shadow-lg hover:shadow-xl flex items-center space-x-2"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                  <span>상담 수락</span>
                </button>
                <button
                  onClick={handleRejectCall}
                  className="bg-gray-500 hover:bg-gray-600 text-white px-8 py-4 rounded-lg font-semibold transition-all duration-200 shadow-lg hover:shadow-xl flex items-center space-x-2"
                >
                  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                  <span>거절</span>
                </button>
              </div>
            </div>
          </div>
        )}

        {/* 새로운 레이아웃: 좌측 대시보드, 우측 비디오들 */}
        <div className="grid grid-cols-12 gap-6 h-[calc(100vh-200px)]">
          {/* 좌측: 보험상품 대시보드 (더 넓게) */}
          <div className="col-span-8">
            <InsuranceDashboard
              selectedProduct={null}
              highlights={highlights}
              onHighlightAdd={handleHighlightAdd}
              onHighlightRemove={handleHighlightRemove}
              currentStep={currentStep}
              onStepChange={handleStepChange}
              userRole={currentUser.role}
            />
          </div>

          {/* 우측: 상담사와 고객 비디오 세로 정렬 */}
          <div className="col-span-4 flex flex-col space-y-4">
            {/* 상담사 비디오 화면 */}
            <div className="bg-white rounded-xl shadow-lg p-4">
              <h3 className={`text-lg font-bold mb-4 flex items-center ${
                currentUser.role === 'counselor' ? 'text-blue-800' : 'text-green-800'
              }`}>
                <svg className={`w-5 h-5 mr-2 ${
                  currentUser.role === 'counselor' ? 'text-blue-600' : 'text-green-600'
                }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                {currentUser.role === 'counselor' ? '상담사' : '고객'} ({currentUser.name})
              </h3>
              
              <div className="relative bg-gray-900 rounded-lg overflow-hidden aspect-video">
                <video
                  ref={localVideoRef}
                  autoPlay
                  muted
                  playsInline
                  className="w-full h-full object-cover"
                />
                {!callState.localStream && (
                  <div className="absolute inset-0 flex items-center justify-center text-white bg-gray-800">
                    <div className="text-center">
                      <svg className="w-12 h-12 mx-auto mb-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 10l4.553-2.276A1 1 0 0121 8.618v6.764a1 1 0 01-1.447.894L15 14M5 18h8a2 2 0 002-2V8a2 2 0 00-2-2H5a2 2 0 00-2 2v8a2 2 0 002 2z" />
                      </svg>
                      <p className="text-sm">카메라 꺼짐</p>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* 고객 비디오 화면 */}
            <div className="bg-white rounded-xl shadow-lg p-4">
              <h3 className={`text-lg font-bold mb-4 flex items-center ${
                targetUser.role === 'counselor' ? 'text-blue-800' : 'text-green-800'
              }`}>
                <svg className={`w-5 h-5 mr-2 ${
                  targetUser.role === 'counselor' ? 'text-blue-600' : 'text-green-600'
                }`} fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                </svg>
                {targetUser.role === 'counselor' ? '상담사' : '고객'} ({targetUser.name})
              </h3>
              
              <div className="relative bg-gray-900 rounded-lg overflow-hidden aspect-video">
                <video
                  ref={remoteVideoRef}
                  autoPlay
                  playsInline
                  className="w-full h-full object-cover"
                />
                {!callState.remoteStream && (
                  <div className="absolute inset-0 flex items-center justify-center text-white bg-gray-800">
                    <div className="text-center">
                      <svg className="w-12 h-12 mx-auto mb-2 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M16 7a4 4 0 11-8 0 4 4 0 018 0zM12 14a7 7 0 00-7 7h14a7 7 0 00-7-7z" />
                      </svg>
                      <p className="text-sm">
                        {callState.isInCall ? '연결 대기 중...' : '상담이 시작되지 않았습니다'}
                      </p>
                    </div>
                  </div>
                )}
              </div>
            </div>

            {/* 상담 기록 및 제어 통합 컴포넌트 */}
            <div className="bg-gradient-to-br from-blue-50 to-white rounded-xl shadow-lg p-4 border border-blue-200 flex-1 flex flex-col">
              <h2 className="text-lg font-bold mb-4 text-blue-800 flex items-center">
                <svg className="w-5 h-5 mr-2 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                </svg>
                상담 제어 및 기록
              </h2>
              
              {/* 상담 유형 및 역할 선택 */}
              <div className="space-y-3 mb-4">
                <div>
                  <label className="block text-sm font-medium text-blue-700 mb-1">상담 유형</label>
                  <select
                    value={consultationInfo.type}
                    onChange={(e) => setConsultationInfo(prev => ({ ...prev, type: e.target.value }))}
                    className="w-full p-2 rounded-lg border-2 border-blue-300 focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
                    disabled={callState.isInCall}
                  >
                    <option value="보험상담">보험상담</option>
                    <option value="금융상담">금융상담</option>
                    <option value="투자상담">투자상담</option>
                    <option value="연금상담">연금상담</option>
                    <option value="대출상담">대출상담</option>
                  </select>
                </div>

                <div>
                  <label className="block text-sm font-medium text-blue-700 mb-1">나의 역할</label>
                  <div className="flex space-x-4">
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="userRole"
                      value="counselor"
                      checked={currentUser.role === 'counselor'}
                      onChange={async () => {
                        // 기존 연결 해제
                        if (isConnected) {
                          WebSocketService.disconnect();
                        }
                        
                        // 새로운 사용자 정보로 설정
                        const newUser = {
                          id: 1,
                          name: '김상담',
                          role: 'counselor' as UserRole,
                          department: '자산관리팀',
                          certification: 'AFP, CFP'
                        };
                        setCurrentUser(newUser);
                        
                        // 새 사용자로 WebSocket 재연결
                        if (isConnected) {
                          try {
                            await WebSocketService.connect(newUser.id);
                          } catch (error) {
                            console.error('WebSocket 재연결 실패:', error);
                          }
                        }
                      }}
                      className="mr-1"
                      disabled={isConnected}
                    />
                    <span className="text-sm">상담사</span>
                  </label>
                  <label className="flex items-center">
                    <input
                      type="radio"
                      name="userRole"
                      value="customer"
                      checked={currentUser.role === 'customer'}
                      onChange={async () => {
                        // 기존 연결 해제
                        if (isConnected) {
                          WebSocketService.disconnect();
                        }
                        
                        // 새로운 사용자 정보로 설정
                        const newUser = {
                          id: 2,
                          name: '이고객',
                          role: 'customer' as UserRole
                        };
                        setCurrentUser(newUser);
                        
                        // 새 사용자로 WebSocket 재연결
                        if (isConnected) {
                          try {
                            await WebSocketService.connect(newUser.id);
                          } catch (error) {
                            console.error('WebSocket 재연결 실패:', error);
                          }
                        }
                      }}
                      className="mr-1"
                      disabled={isConnected}
                    />
                    <span className="text-sm">고객</span>
                  </label>
                </div>
                </div>
              </div>

              {/* 연결 및 상담 버튼 */}
              <div className="space-y-2 mb-4">
                {!isConnected ? (
                  <button
                    onClick={handleConnect}
                    className={`w-full px-3 py-2 rounded-lg font-medium transition-colors text-sm ${
                      currentUser.role === 'counselor' 
                        ? 'bg-blue-600 hover:bg-blue-700 text-white' 
                        : 'bg-green-600 hover:bg-green-700 text-white'
                    }`}
                  >
                    상담 시스템 연결
                  </button>
                ) : (
                  <div className="space-y-2">
                  <button
                    onClick={handleDisconnect}
                      className="w-full bg-gray-500 hover:bg-gray-600 text-white px-3 py-2 rounded-lg font-medium transition-colors text-sm"
                  >
                    연결 해제
                  </button>

                {!isMediaInitialized ? (
                  <button
                    onClick={handleMediaTest}
                        className="w-full bg-purple-600 hover:bg-purple-700 text-white px-3 py-2 rounded-lg font-medium transition-colors text-sm"
                  >
                    카메라/마이크 확인
                  </button>
                ) : (
                  <button
                    onClick={handleStopMedia}
                    disabled={callState.isInCall}
                        className="w-full bg-orange-500 hover:bg-orange-600 disabled:bg-gray-300 text-white px-3 py-2 rounded-lg font-medium transition-colors text-sm"
                  >
                    미디어 중지
                  </button>
                )}

                {!callState.isInCall ? (
                  <button
                    onClick={handleStartCall}
                    disabled={!isConnected}
                        className={`w-full px-3 py-2 rounded-lg font-medium transition-colors text-sm disabled:bg-gray-300 ${
                          currentUser.role === 'counselor' 
                            ? 'bg-blue-600 hover:bg-blue-700 text-white' 
                            : 'bg-green-600 hover:bg-green-700 text-white'
                        }`}
                      >
                        {currentUser.role === 'counselor' ? '상담 시작' : '상담 요청'}
                  </button>
                ) : (
                  <button
                    onClick={handleEndCall}
                        className="w-full bg-red-600 hover:bg-red-700 text-white px-3 py-2 rounded-lg font-medium transition-colors text-sm"
                  >
                    상담 종료
                  </button>
                    )}
                  </div>
                )}
              </div>

              {/* 미디어 제어 버튼 (테스트 중이거나 상담 중일 때) */}
              {(isMediaInitialized || callState.isInCall) && (
                <div className="grid grid-cols-2 gap-2 mb-4">
                  <button
                    onClick={handleToggleMicrophone}
                    className={`px-3 py-2 rounded-lg font-medium transition-colors text-sm ${
                      isAudioEnabled
                        ? `${currentUser.role === 'counselor' ? 'bg-blue-600 hover:bg-blue-700' : 'bg-green-600 hover:bg-green-700'} text-white`
                        : 'bg-gray-300 hover:bg-gray-400 text-gray-700'
                    }`}
                  >
                    {isAudioEnabled ? '마이크 끄기' : '마이크 켜기'}
                  </button>
                  <button
                    onClick={handleToggleVideo}
                    className={`px-3 py-2 rounded-lg font-medium transition-colors text-sm ${
                      isVideoEnabled
                        ? `${currentUser.role === 'counselor' ? 'bg-blue-600 hover:bg-blue-700' : 'bg-green-600 hover:bg-green-700'} text-white`
                        : 'bg-gray-300 hover:bg-gray-400 text-gray-700'
                    }`}
                  >
                    {isVideoEnabled ? '카메라 끄기' : '카메라 켜기'}
                  </button>
                </div>
              )}

              {/* 상담 기록 */}
              <div className="flex-1 flex flex-col">
                <label className="block text-sm font-medium text-blue-700 mb-2">상담 기록</label>
                  <textarea
                    value={consultationNotes}
                  onChange={(e) => {
                    if (e.target.value.length <= 500) {
                      setConsultationNotes(e.target.value);
                    }
                  }}
                    placeholder="상담 내용을 기록하세요..."
                  className="flex-1 p-3 border-2 border-blue-200 rounded-lg resize-none focus:ring-2 focus:ring-blue-500 focus:border-blue-500 transition-colors text-sm"
                    disabled={!callState.isInCall}
                  />
                  
                  <button
                    onClick={handleSaveNotes}
                    disabled={!consultationNotes.trim() || !callState.isInCall}
                  className="mt-2 w-full bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white px-3 py-2 rounded-lg font-medium transition-colors text-sm"
                  >
                    메모 저장
                  </button>
                
                {callState.isInCall && (
                  <div className="mt-2 p-2 bg-green-50 rounded-lg">
                    <p className="text-xs text-green-800">
                      상담 진행 중 - 실시간 기록 가능
                    </p>
                  </div>
                )}
              </div>

              {/* 상담 상태 정보 */}
              <div className="mt-4 pt-3 border-t border-blue-200">
                <div className="grid grid-cols-3 gap-2 text-xs">
                  <div className="text-center">
                    <div className="text-gray-600">미디어</div>
                    <div className={isMediaInitialized ? 'text-green-600 font-medium' : 'text-red-600'}>
                      {isMediaInitialized ? '준비' : '미준비'}
                    </div>
                  </div>
                  <div className="text-center">
                    <div className="text-gray-600">상담</div>
                    <div className={callState.isInCall ? 'text-green-600 font-medium' : 'text-gray-500'}>
                      {consultationInfo.status === 'in-progress' ? '진행' : '대기'}
                    </div>
                  </div>
                  <div className="text-center">
                    <div className="text-gray-600">연결</div>
                    <div className={isConnected && callState.isConnected ? 'text-green-600 font-medium' : 'text-red-600'}>
                      {isConnected && callState.isConnected ? '연결' : '끊김'}
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

    </div>
  );
};

export default VideoCall; 