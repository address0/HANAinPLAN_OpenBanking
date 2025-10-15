import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams } from 'react-router-dom';
import WebSocketService from '../services/WebSocketService';
import WebRTCService from '../services/WebRTCService';
import { useUserStore } from '../store/userStore';
import type { CallState } from '../services/WebRTCService';
import type { CallRequestMessage, WebRTCMessage } from '../services/WebSocketService';
import { getConsultationDetails, joinConsultationRoom } from '../api/consultationApi';
import GeneralConsultation from '../components/consultation/GeneralConsultation';
import ProductConsultation from '../components/consultation/ProductConsultation';
import AssetConsultation from '../components/consultation/AssetConsultation';

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
  detail?: string;
  status: 'scheduled' | 'in-progress' | 'completed' | 'cancelled';
}

const VideoCall: React.FC = () => {
  const [searchParams] = useSearchParams();
  const consultationId = searchParams.get('consultationId');

  const { user } = useUserStore();

  if (!user) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg text-center">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">로그인이 필요합니다</h2>
          <p className="text-gray-600 mb-6">상담 서비스를 이용하려면 로그인해주세요.</p>
          <button
            onClick={() => window.location.href = '/login'}
            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium"
          >
            로그인 페이지로 이동
          </button>
        </div>
      </div>
    );
  }

  const userRole: UserRole = user.userType === 'COUNSELOR' ? 'counselor' : 'customer';

  const [currentUser] = useState<UserInfo>({
    id: user.userId,
    name: user.name,
    role: userRole,
    department: userRole === 'counselor' ? '자산관리팀' : undefined,
    certification: userRole === 'counselor' ? 'AFP, CFP' : undefined
  });

  const [targetUser, setTargetUser] = useState<UserInfo | null>(null);
  const [consultationInfo, setConsultationInfo] = useState<ConsultationInfo>({
    type: '금융상담',
    status: 'scheduled'
  });
  const [isLoading, setIsLoading] = useState<boolean>(true);
  const [isConnected, setIsConnected] = useState<boolean>(false);
  const [showWaitingRoomModal, setShowWaitingRoomModal] = useState<boolean>(false);
  const [callState, setCallState] = useState<CallState>({
    isInCall: false,
    roomId: null,
    isConnected: false,
    localStream: null,
    remoteStream: null,
    callerId: null,
    calleeId: null,
    isCaller: false,
    isScreenSharing: false,
    screenStream: null
  });
  const [incomingCall, setIncomingCall] = useState<CallRequestMessage | null>(null);
  const [error, setError] = useState<string>('');
  const [isAudioEnabled, setIsAudioEnabled] = useState<boolean>(true);
  const [isVideoEnabled, setIsVideoEnabled] = useState<boolean>(true);
  const [isMediaInitialized, setIsMediaInitialized] = useState<boolean>(false);
  const [isScreenSharing, setIsScreenSharing] = useState<boolean>(false);
  const [consultationStartTime, setConsultationStartTime] = useState<Date | null>(null);
  const [isWaitingForConsultant, setIsWaitingForConsultant] = useState<boolean>(false);

  const localVideoRef = useRef<HTMLVideoElement>(null);
  const remoteVideoRef = useRef<HTMLVideoElement>(null);

  const getConsultationType = () => {
    if (!consultationId) return consultationInfo.type;

    const prefix = consultationId.substring(0, 3);
    switch (prefix) {
      case 'COM': return 'general';
      case 'PRO': return 'product';
      case 'ASM': return 'asset-management';
      default: return consultationInfo.type;
    }
  };

  const consultationTypeFromId = getConsultationType();

  const getThemeColors = () => {
    const type = consultationTypeFromId;

    switch (type) {
      case 'general':
        return {
          primary: 'blue',
          gradient: 'from-blue-600 to-blue-700',
          bg: 'bg-blue-600',
          bgHover: 'bg-blue-700',
          text: 'text-blue-800',
          border: 'border-blue-200',
          bgLight: 'bg-blue-50',
          icon: '💬'
        };
      case 'product':
        return {
          primary: 'green',
          gradient: 'from-green-600 to-green-700',
          bg: 'bg-green-600',
          bgHover: 'bg-green-700',
          text: 'text-green-800',
          border: 'border-green-200',
          bgLight: 'bg-green-50',
          icon: '📋'
        };
      case 'asset-management':
        return {
          primary: 'purple',
          gradient: 'from-purple-600 to-purple-700',
          bg: 'bg-purple-600',
          bgHover: 'bg-purple-700',
          text: 'text-purple-800',
          border: 'border-purple-200',
          bgLight: 'bg-purple-50',
          icon: '💰'
        };
      default:
        return {
          primary: 'blue',
          gradient: 'from-blue-600 to-blue-700',
          bg: 'bg-blue-600',
          bgHover: 'bg-blue-700',
          text: 'text-blue-800',
          border: 'border-blue-200',
          bgLight: 'bg-blue-50',
          icon: '💼'
        };
    }
  };

  const themeColors = getThemeColors();

  useEffect(() => {
    setupEventListeners();
    return () => {
      cleanup();
    };
  }, []);

  useEffect(() => {
    if (consultationId) {
      loadConsultationDetails(consultationId);
    } else {
      setIsLoading(false);
    }
  }, [consultationId]);

  useEffect(() => {
    if (callState.localStream && localVideoRef.current) {
      localVideoRef.current.srcObject = callState.localStream;
    }
  }, [callState.localStream]);

  useEffect(() => {
    if (callState.remoteStream && remoteVideoRef.current) {
      remoteVideoRef.current.srcObject = callState.remoteStream;

      remoteVideoRef.current.play().catch(error => {
      });
    }
  }, [callState.remoteStream]);

  useEffect(() => {
    setIsMediaInitialized(!!callState.localStream);
  }, [callState.localStream]);

  useEffect(() => {
    setIsScreenSharing(callState.isScreenSharing);
  }, [callState.isScreenSharing]);

  const setupEventListeners = () => {
    WebSocketService.onConnectionStateChange((connected) => {
      setIsConnected(connected);
    });

    WebSocketService.onCallRequest((callRequest) => {
      setIncomingCall(callRequest);
    });

    WebSocketService.onCallAccept((message) => {
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

    WebSocketService.onConsultationStart(async () => {
      if (currentUser.role === 'customer') {
        setIsWaitingForConsultant(false);
        setShowWaitingRoomModal(false);
        setConsultationInfo(prev => ({ ...prev, status: 'in-progress' }));
        setError('✅ 상담이 시작되었습니다! 상담사와 연결 중입니다...');
        setTimeout(() => setError(''), 3000);
      }
    });

    WebRTCService.onCallStateChange((state) => {
      setCallState(state);
    });

    WebRTCService.onConnectionStateChange((state) => {
    });

    WebRTCService.onError((error) => {
      setError(error.message);
      setTimeout(() => setError(''), 5000);
    });

    WebSocketService.onOffer(async (offer) => {
      try {
        if (!callState.localStream) {
          await WebRTCService.initializeMedia();
        }

        await WebRTCService.handleOffer(offer);
      } catch (error) {
        setError('연결에 실패했습니다.');
      }
    });

    WebSocketService.onAnswer(async (answer) => {
      try {
        await WebRTCService.handleAnswer(answer);
      } catch (error) {
        setError('연결에 실패했습니다.');
      }
    });

    WebSocketService.onIceCandidate(async (candidate) => {
      try {
        await WebRTCService.handleIceCandidate(candidate);
      } catch (error) {
      }
    });
  };

  const cleanup = () => {
    WebSocketService.disconnect();
    WebRTCService.endCall();
  };

  const handleConnect = async () => {
    try {
      await WebSocketService.connect(currentUser.id);
      setError('');
    } catch (error) {
      setError('WebSocket 연결에 실패했습니다.');
    }
  };

  const loadConsultationDetails = async (consultId: string) => {
    setIsLoading(true);
    try {
      const details = await getConsultationDetails(consultId);

      setConsultationInfo({
        id: details.consultId,
        type: details.consultType,
        detail: details.detail,
        status: 'scheduled'
      });

      if (currentUser.role === 'counselor') {
        setTargetUser({
          id: Number(details.customerId),
          name: details.customerName || '고객',
          role: 'customer'
        });
      } else {
        setTargetUser({
          id: Number(details.consultantId),
          name: details.consultantName || '상담사',
          role: 'counselor',
          department: details.consultantDepartment || '상담팀'
        });
      }

    } catch (error) {
      setError('상담 정보를 불러올 수 없습니다.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStartCall = async () => {
    try {
      if (currentUser.role === 'customer' && consultationId) {
        setIsWaitingForConsultant(true);
        setShowWaitingRoomModal(true);
        return;
      }

      setConsultationInfo(prev => ({ ...prev, status: 'in-progress' }));

      if (consultationId) {
        if (!targetUser) {
          throw new Error('상담 대상 정보를 찾을 수 없습니다.');
        }

        const result = await joinConsultationRoom(consultationId, currentUser.id);

        if (result.success) {
          await WebRTCService.startCall(consultationId, targetUser.id);
          setConsultationStartTime(new Date());

          const startMessage: WebRTCMessage = {
            type: 'CONSULTATION_START',
            roomId: consultationId,
            senderId: currentUser.id,
            receiverId: targetUser.id,
            data: { consultationId }
          };

          WebSocketService.sendConsultationStart(startMessage);

          await WebRTCService.sendOffer(consultationId, targetUser.id);

          setError('상담이 시작되었습니다. 고객에게 시작 알림을 전송했습니다.');
          setTimeout(() => setError(''), 3000);
        } else {
          throw new Error(result.message || '상담 방 입장에 실패했습니다.');
        }
      } else {
        const apiUrl = currentUser.role === 'customer'
          ? 'http://localhost:8080/api/webrtc/consultation/request'
          : 'http://localhost:8080/api/webrtc/call/request';

        const response = await fetch(apiUrl, {
          method: 'POST',
          headers: {
            'Content-Type': 'application/json',
          },
          body: JSON.stringify({
            callerId: currentUser.id,
            calleeId: currentUser.role === 'customer' ? undefined : targetUser?.id,
            callerName: currentUser.name,
            calleeName: currentUser.role === 'customer' ? undefined : targetUser?.name,
            consultationType: consultationInfo.type
          }),
        });

        const data = await response.json();

        if (data.success) {
          if (currentUser.role === 'customer' && data.consultantId) {
            setError(`상담원과 연결되었습니다. (${data.consultantName || data.consultantId})`);
            setTimeout(() => setError(''), 3000);
          }

          await WebRTCService.startCall(data.roomId, data.consultantId || targetUser?.id || 0);
          setConsultationStartTime(new Date());
        } else {
          setError(data.message || '상담 요청에 실패했습니다.');
          setConsultationInfo(prev => ({ ...prev, status: 'scheduled' }));

          if (data.message && data.message.includes('대기열')) {
            setTimeout(() => setError(''), 5000);
          }
        }
      }
    } catch (error: any) {
      setError(error.message || '상담을 시작할 수 없습니다.');
      setConsultationInfo(prev => ({ ...prev, status: 'scheduled' }));
    }
  };

  const handleAcceptCall = async () => {
    if (!incomingCall) return;

    try {
      setConsultationInfo(prev => ({ ...prev, status: 'in-progress' }));

      const acceptMessage: WebRTCMessage = {
        type: 'CALL_ACCEPT',
        roomId: incomingCall.roomId,
        senderId: currentUser.id,
        receiverId: incomingCall.callerId,
        data: null
      };
      WebSocketService.sendCallAccept(acceptMessage);

      await WebRTCService.acceptCall(incomingCall.roomId, incomingCall.callerId);

      setIncomingCall(null);
      setConsultationStartTime(new Date());
      setError('');
    } catch (error) {
      setError('상담 수락에 실패했습니다.');
      setConsultationInfo(prev => ({ ...prev, status: 'scheduled' }));
    }
  };

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

  const getConsultationDuration = () => {
    if (!consultationStartTime) return '00:00';

    const now = new Date();
    const diff = now.getTime() - consultationStartTime.getTime();
    const minutes = Math.floor(diff / 60000);
    const seconds = Math.floor((diff % 60000) / 1000);

    return `${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}`;
  };

  const handleToggleMicrophone = () => {
    const enabled = WebRTCService.toggleMicrophone();
    setIsAudioEnabled(enabled);
  };

  const handleToggleVideo = () => {
    const enabled = WebRTCService.toggleVideo();
    setIsVideoEnabled(enabled);
  };

  const handleMediaTest = async () => {
    try {
      await WebRTCService.initializeMedia(true, true);
      setError('');
    } catch (error) {
      setError('카메라 또는 마이크에 접근할 수 없습니다.');
    }
  };

  const handleToggleScreenShare = async () => {
    try {
      await WebRTCService.toggleScreenShare();
    } catch (error) {
      setError('화면 공유 전환에 실패했습니다.');
      setTimeout(() => setError(''), 3000);
    }
  };

  if (isLoading) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg text-center">
          <div className="w-16 h-16 border-4 border-blue-600 border-t-transparent rounded-full animate-spin mx-auto mb-4"></div>
          <h2 className="text-2xl font-bold text-gray-800 mb-2">상담 정보 로딩 중</h2>
          <p className="text-gray-600">잠시만 기다려주세요...</p>
        </div>
      </div>
    );
  }

  if (consultationId && !targetUser) {
    return (
      <div className="min-h-screen bg-gradient-to-br from-blue-50 to-indigo-100 flex items-center justify-center">
        <div className="bg-white p-8 rounded-xl shadow-lg text-center">
          <h2 className="text-2xl font-bold text-gray-800 mb-4">상담 정보를 찾을 수 없습니다</h2>
          <p className="text-gray-600 mb-6">올바른 상담 ID로 다시 시도해주세요.</p>
          <button
            onClick={() => window.history.back()}
            className="bg-blue-600 hover:bg-blue-700 text-white px-6 py-3 rounded-lg font-medium"
          >
            돌아가기
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="bg-gray-50">
      {}
      <div className={`shadow-sm border-b bg-gradient-to-r ${themeColors.gradient} text-white`}>
        <div className="max-w-7xl mx-auto px-4 py-4">
          <div className="flex items-center justify-between">
            <div className="flex items-center space-x-4">
              <div className="flex items-center space-x-4">
                {}
                <div className="w-12 h-12 bg-white rounded-full flex items-center justify-center">
                  <img src="/images/img-hana-symbol.png" alt="하나금융그룹" className="w-8 h-8" />
                </div>

                <div>
                  <h1 className="text-2xl font-bold flex items-center gap-2">
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
              {}
              <div className={`px-4 py-2 rounded-lg ${
                consultationInfo.status === 'in-progress'
                  ? 'bg-green-500 bg-opacity-20 border border-green-300'
                  : 'bg-gray-500 bg-opacity-20 border border-gray-300'
              }`}>
                <span className="text-sm font-medium">
                  {consultationInfo.status === 'in-progress' ? '상담 진행 중' : '상담 대기 중'}
                </span>
              </div>

              {}
              {consultationStartTime && (
                <div className="bg-white bg-opacity-20 px-4 py-2 rounded-lg">
                  <span className="text-sm font-medium">
                    상담 시간: {getConsultationDuration()}
                  </span>
                </div>
              )}

              {}
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

        {}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 text-red-700 px-4 py-3 mb-6 rounded-r-lg">
            <div className="flex">
              <div className="ml-3">
                <p className="text-sm font-medium">{error}</p>
              </div>
            </div>
          </div>
        )}

        {}
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

        {}
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

        {}
        <div className="grid grid-cols-12 gap-6 h-[calc(100vh-180px)] min-h-[600px]">
          {}
          <div className="col-span-5 flex flex-col space-y-4">
            {}
            <div className="bg-white rounded-xl shadow-lg p-4 flex-1">
              <h3 className={`text-sm font-bold mb-3 flex items-center ${themeColors.text}`}>
                {currentUser.role === 'counselor' ? '상담사' : '고객'} ({currentUser.name})
              </h3>

              <div className="relative bg-gray-900 rounded-lg overflow-hidden h-[calc(100%-2rem)]">
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

            {}
            {targetUser && (
              <div className="bg-white rounded-xl shadow-lg p-4 flex-1">
                <h3 className={`text-sm font-bold mb-3 flex items-center ${themeColors.text}`}>
                  {targetUser.role === 'counselor' ? '상담사' : '고객'} ({targetUser.name})
                </h3>

                <div className="relative bg-gray-900 rounded-lg overflow-hidden h-[calc(100%-2rem)]">
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
            )}

            {}
            <div className="bg-white rounded-xl shadow-lg p-4">
              <div className="space-y-2">
                {!isConnected ? (
                  <button
                    onClick={handleConnect}
                    className={`w-full px-4 py-3 rounded-lg font-medium transition-colors ${themeColors.bg} hover:${themeColors.bgHover} text-white`}
                  >
                    {currentUser.role === 'counselor' ? '화상 상담 시작' : '상담 대기실 입장'}
                  </button>
                ) : (
                  <>

                    {!callState.isInCall && !isWaitingForConsultant ? (
                      <>
                        {currentUser.role === 'counselor' ? (
                          <button
                            onClick={handleStartCall}
                            disabled={!isMediaInitialized}
                            className={`w-full px-4 py-3 rounded-lg font-medium transition-colors disabled:bg-gray-300 ${themeColors.bg} hover:${themeColors.bgHover} text-white`}
                          >
                            상담 시작
                          </button>
                        ) : (
                          <button
                            onClick={handleStartCall}
                            disabled={!isMediaInitialized}
                            className={`w-full px-4 py-3 rounded-lg font-medium transition-colors disabled:bg-gray-300 ${themeColors.bg} hover:${themeColors.bgHover} text-white`}
                          >
                            준비 완료
                          </button>
                        )}
                      </>
                    ) : callState.isInCall ? (
                      <button
                        onClick={handleEndCall}
                        className="w-full px-4 py-3 bg-red-600 hover:bg-red-700 text-white rounded-lg font-medium transition-colors"
                      >
                        상담 종료
                      </button>
                    ) : null}

                    {!isMediaInitialized && !callState.isInCall && !isWaitingForConsultant && (
                      <button
                        onClick={handleMediaTest}
                        className="w-full px-4 py-3 bg-purple-600 hover:bg-purple-700 text-white rounded-lg font-medium transition-colors"
                      >
                        {currentUser.role === 'counselor' ? '카메라/마이크 테스트' : '카메라/마이크 준비'}
                      </button>
                    )}

                    {(isMediaInitialized || callState.isInCall) && (
                      <div className="grid grid-cols-2 gap-2">
                        <button
                          onClick={handleToggleMicrophone}
                          className={`px-3 py-2 rounded-lg font-medium transition-colors text-sm ${
                            isAudioEnabled
                              ? `${themeColors.bg} hover:${themeColors.bgHover} text-white`
                              : 'bg-gray-300 hover:bg-gray-400 text-gray-700'
                          }`}
                        >
                          {isAudioEnabled ? '🎤 ON' : '🎤 OFF'}
                        </button>
                        <button
                          onClick={handleToggleVideo}
                          className={`px-3 py-2 rounded-lg font-medium transition-colors text-sm ${
                            isVideoEnabled
                              ? `${themeColors.bg} hover:${themeColors.bgHover} text-white`
                              : 'bg-gray-300 hover:bg-gray-400 text-gray-700'
                          }`}
                        >
                          {isVideoEnabled ? '📹 ON' : '📹 OFF'}
                        </button>
                      </div>
                    )}

                    {}
                    {callState.isInCall && currentUser.role === 'counselor' && (
                      <button
                        onClick={handleToggleScreenShare}
                        className={`w-full px-3 py-2 rounded-lg font-medium transition-colors text-sm flex items-center justify-center space-x-2 ${
                          isScreenSharing
                            ? 'bg-orange-600 hover:bg-orange-700 text-white'
                            : `${themeColors.bg} hover:${themeColors.bgHover} text-white`
                        }`}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d={
                            isScreenSharing
                              ? "M6 18L18 6M6 6l12 12"
                              : "M9.75 17L9 20l-1 1h8l-1-1-.75-3M3 13h18M5 17h14a2 2 0 002-2V5a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z"
                          } />
                        </svg>
                        <span>{isScreenSharing ? '화면 공유 중지' : '화면 공유'}</span>
                      </button>
                    )}
                  </>
                )}
              </div>
            </div>
          </div>

          {}
          <div className="col-span-7 h-full overflow-hidden">
            <div className="h-full overflow-y-auto scrollbar-thin scrollbar-thumb-gray-300 scrollbar-track-gray-100 hover:scrollbar-thumb-gray-400">
              {targetUser && consultationTypeFromId === 'general' && (
                <GeneralConsultation
                  consultationInfo={consultationInfo}
                  currentUserId={currentUser.id}
                  currentUserRole={currentUser.role}
                  targetUserId={targetUser.id}
                  isInCall={callState.isInCall}
                />
              )}

              {targetUser && consultationTypeFromId === 'product' && (
                <ProductConsultation
                  consultationInfo={consultationInfo}
                  currentUserId={currentUser.id}
                  currentUserRole={currentUser.role}
                  targetUserId={targetUser.id}
                  isInCall={callState.isInCall}
                />
              )}

              {targetUser && consultationTypeFromId === 'asset-management' && (
                <AssetConsultation
                  consultationInfo={consultationInfo}
                  customerId={currentUser.role === 'counselor' ? targetUser.id : currentUser.id}
                  currentUserId={currentUser.id}
                  currentUserRole={currentUser.role}
                  targetUserId={targetUser.id}
                  isInCall={callState.isInCall}
                />
              )}
            </div>
          </div>
        </div>
      </div>

      {}
      {showWaitingRoomModal && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50">
          <div className="bg-white rounded-xl p-8 max-w-md w-full mx-4">
            <div className="text-center">
              <div className="mb-6">
                <div className="w-16 h-16 bg-blue-100 rounded-full flex items-center justify-center mx-auto mb-4">
                  <svg className="w-8 h-8 text-blue-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                  </svg>
                </div>
                <h3 className="text-xl font-bold text-gray-900 mb-2">상담 대기실</h3>
                <p className="text-gray-600">
                  준비가 완료되었습니다.<br />
                  상담사가 시작할 때까지 잠시만 기다려주세요.
                </p>
              </div>

              <div className="bg-yellow-50 border border-yellow-200 rounded-lg p-4 mb-6">
                <div className="flex items-center justify-center gap-2">
                  <div className="animate-pulse">
                    <svg className="w-5 h-5 text-yellow-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                  </div>
                  <p className="text-sm font-medium text-yellow-800">
                    상담사가 곧 상담을 시작합니다...
                  </p>
                </div>
              </div>

              <div className="flex space-x-3">
                <button
                  onClick={() => setShowWaitingRoomModal(false)}
                  className="flex-1 px-4 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
                >
                  대기실 나가기
                </button>
                <button
                  onClick={handleMediaTest}
                  className="flex-1 px-4 py-2 bg-blue-600 hover:bg-blue-700 text-white rounded-lg transition-colors"
                >
                  카메라/마이크 테스트
                </button>
              </div>
            </div>
          </div>
        </div>
      )}

    </div>
  );
};

export default VideoCall;