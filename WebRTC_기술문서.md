# WebRTC 기반 비대면 상담 시스템 기술 문서

## 📑 목차
1. [전체 시스템 구조](#1-전체-시스템-구조)
2. [핵심 기술 스택](#2-핵심-기술-스택)
3. [WebRTC 화상 채팅 구현](#3-webrtc-화상-채팅-구현)
4. [실시간 동기화 기능](#4-실시간-동기화-기능)
5. [기술적 특장점](#5-기술적-특장점)

---

## 1. 전체 시스템 구조

### 1.1 아키텍처 다이어그램

```
┌─────────────────┐           ┌─────────────────┐
│  고객 (Client)   │           │ 상담원 (Client)  │
│                 │           │                 │
│  ┌───────────┐  │           │  ┌───────────┐  │
│  │ VideoCall │  │           │  │ VideoCall │  │
│  │   Page    │  │           │  │   Page    │  │
│  └─────┬─────┘  │           │  └─────┬─────┘  │
│        │        │           │        │        │
│  ┌─────┴─────┐  │           │  ┌─────┴─────┐  │
│  │ WebRTC    │◄─┼───────────┼─►│ WebRTC    │  │
│  │ Service   │  │  P2P Media │  │ Service   │  │
│  └─────┬─────┘  │  (Video/   │  └─────┬─────┘  │
│        │        │   Audio)   │        │        │
│  ┌─────┴─────┐  │           │  ┌─────┴─────┐  │
│  │WebSocket  │  │           │  │WebSocket  │  │
│  │ Service   │  │           │  │ Service   │  │
│  └─────┬─────┘  │           │  └─────┬─────┘  │
└────────┼────────┘           └────────┼────────┘
         │                             │
         │  Signaling (STOMP)          │
         └──────────┬──────────────────┘
                    │
         ┌──────────▼──────────┐
         │  Spring Backend     │
         │                     │
         │ ┌─────────────────┐ │
         │ │ WebSocket       │ │
         │ │ Config          │ │
         │ │ (STOMP)         │ │
         │ └────────┬────────┘ │
         │          │          │
         │ ┌────────▼────────┐ │
         │ │ WebRTC Signal   │ │
         │ │ Controller      │ │
         │ └────────┬────────┘ │
         │          │          │
         │ ┌────────▼────────┐ │
         │ │ WebRTC Service  │ │
         │ └────────┬────────┘ │
         │          │          │
         │ ┌────────▼────────┐ │
         │ │   MySQL DB      │ │
         │ │ (VideoCallRoom) │ │
         │ └─────────────────┘ │
         └─────────────────────┘
```

### 1.2 시스템 컴포넌트

| 컴포넌트 | 역할 | 기술 |
|---------|------|------|
| **프론트엔드** | 화상 통화 UI, 실시간 동기화 | React, TypeScript |
| **WebRTC Service** | P2P 미디어 스트림 관리 | WebRTC API |
| **WebSocket Service** | 시그널링 메시지 전송/수신 | STOMP.js, SockJS |
| **백엔드** | 시그널링 서버, 메시지 라우팅 | Spring Boot, WebSocket |
| **데이터베이스** | 통화방 정보, 상담 기록 저장 | MySQL |

---

## 2. 핵심 기술 스택

### 2.1 프론트엔드

**주요 라이브러리:**
- **WebRTC API**: 브라우저 Native API (P2P 미디어 스트림)
- **STOMP.js**: WebSocket 메시징 프로토콜
- **SockJS**: WebSocket Fallback (호환성 보장)
- **React**: UI 컴포넌트 및 상태 관리
- **TypeScript**: 타입 안정성

**파일 구조:**
```
frontend/hanainplan/src/
├── services/
│   ├── WebRTCService.ts      # WebRTC 연결 관리
│   └── WebSocketService.ts   # 시그널링 메시지 처리
├── pages/
│   └── VideoCall.tsx          # 화상 상담 페이지
└── components/
    └── consultation/
        ├── ProductConsultation.tsx  # 상품 상담
        ├── GeneralConsultation.tsx  # 일반 상담
        └── NotesTab.tsx             # 메모 동기화
```

### 2.2 백엔드

**주요 기술:**
- **Spring WebSocket**: STOMP over WebSocket 지원
- **Spring Messaging**: 메시지 브로커 및 라우팅
- **MySQL**: 통화방 정보 및 상담 기록 저장

**파일 구조:**
```
backend/hanainplan/src/main/java/com/hanainplan/
├── config/
│   └── WebSocketConfig.java           # WebSocket 설정
├── domain/webrtc/
│   ├── controller/
│   │   └── WebRTCSignalController.java  # 시그널링 컨트롤러
│   ├── service/
│   │   └── WebRTCService.java          # 비즈니스 로직
│   ├── entity/
│   │   └── VideoCallRoom.java          # 통화방 엔터티
│   └── dto/
│       ├── WebRTCMessage.java          # 메시지 DTO
│       ├── SDPMessage.java             # SDP DTO
│       └── ICECandidateMessage.java    # ICE DTO
```

### 2.3 연결 프로토콜

- **STUN Server**: NAT 통과를 위한 공인 IP 확인 (Google STUN)
- **ICE (Interactive Connectivity Establishment)**: P2P 연결 최적화

---

## 3. WebRTC 화상 채팅 구현

### 3.1 WebSocket 시그널링 채널 구축

#### Phase 1: 백엔드 WebSocket 설정

**파일:** `backend/hanainplan/src/main/java/com/hanainplan/config/WebSocketConfig.java`

```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 클라이언트 → 서버 prefix
        config.setApplicationDestinationPrefixes("/app");
        
        // 서버 → 클라이언트 브로커 (1:1, 1:다)
        config.enableSimpleBroker("/queue", "/topic");
        
        // 특정 사용자 타겟팅
        config.setUserDestinationPrefix("/user");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // WebSocket 엔드포인트
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .withSockJS();  // SockJS Fallback
    }
}
```

**핵심 개념:**
- `/app`: 클라이언트가 서버로 메시지를 보낼 때 사용하는 prefix
- `/queue`: 1대1 메시징 (개별 사용자)
- `/topic`: 1대다 메시징 (브로드캐스트)
- `/user`: 특정 사용자에게 메시지 전송

#### Phase 2: 프론트엔드 WebSocket 연결

**파일:** `frontend/hanainplan/src/services/WebSocketService.ts`

```typescript
class WebSocketService {
  private client: Client;

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });
  }

  connect(userId: number): Promise<void> {
    this.client.connectHeaders = { userId: userId.toString() };
    this.client.activate();
    
    // 메시지 구독
    this.client.subscribe('/user/queue/webrtc-offer', (message) => {
      const offer = JSON.parse(message.body);
      this.onOfferCallback?.(offer);
    });
  }
}
```

**특징:**
- **SockJS**: WebSocket 미지원 브라우저에서 HTTP Long Polling으로 자동 전환
- **자동 재연결**: 연결이 끊어지면 5초 후 재연결 시도
- **하트비트**: 4초마다 연결 상태 확인

---

### 3.2 통화 시작 플로우

#### Step 1: 통화 요청 (고객 → 상담원)

**프론트엔드 (고객):**
```typescript
// VideoCall.tsx
const startCall = async () => {
  // 1. REST API로 통화방 생성
  const response = await axios.post('/api/webrtc/call-request', {
    callerId: currentUser.id,
    calleeId: targetUser.id
  });
  
  const roomId = response.data.roomId;
  
  // 2. WebRTC 미디어 초기화
  await WebRTCService.startCall(roomId, targetUser.id);
};
```

**백엔드 (통화방 생성 및 알림):**
```java
// WebRTCService.java
public String createCallRequest(Long callerId, Long calleeId) {
    String roomId = UUID.randomUUID().toString();
    VideoCallRoom callRoom = VideoCallRoom.builder()
            .roomId(roomId)
            .callerId(callerId)
            .calleeId(calleeId)
            .status(CallStatus.WAITING)
            .build();
    
    videoCallRoomRepository.save(callRoom);
    return roomId;
}

// WebRTCSignalController.java
public void notifyCallRequest(CallRequestMessage request, String roomId) {
    request.setRoomId(roomId);
    // 상담원에게 WebSocket으로 알림 전송
    messagingTemplate.convertAndSendToUser(
        String.valueOf(request.getCalleeId()), 
        "/queue/call-request", 
        request
    );
}
```

#### Step 2: 통화 수락 (상담원 → 고객)

**프론트엔드 (상담원):**
```typescript
const acceptCall = async (callRequest: CallRequestMessage) => {
  // 미디어 장치 접근
  await WebRTCService.acceptCall(callRequest.roomId, callRequest.callerId);
  
  // 수락 메시지 전송
  WebSocketService.sendCallAccept({
    type: 'CALL_ACCEPT',
    roomId: callRequest.roomId,
    senderId: currentUser.id,
    receiverId: callRequest.callerId
  });
};
```

---

### 3.3 WebRTC 피어 연결 (Signaling)

#### Step 3: SDP Offer 생성 및 전송 (발신자)

**파일:** `frontend/hanainplan/src/services/WebRTCService.ts`

```typescript
async sendOffer(roomId: string, receiverId: number): Promise<void> {
  // 1. PeerConnection 생성
  this.peerConnection = new RTCPeerConnection({
    iceServers: [
      { urls: 'stun:stun.l.google.com:19302' }  // Google STUN
    ]
  });

  // 2. 로컬 미디어 트랙 추가
  this.localStream.getTracks().forEach(track => {
    this.peerConnection.addTrack(track, this.localStream);
  });

  // 3. ICE Candidate 이벤트 핸들러
  this.peerConnection.onicecandidate = (event) => {
    if (event.candidate) {
      WebSocketService.sendIceCandidate({
        candidate: event.candidate.candidate,
        roomId: roomId,
        senderId: this.userId,
        receiverId: receiverId
      });
    }
  };

  // 4. SDP Offer 생성
  const offer = await this.peerConnection.createOffer();
  await this.peerConnection.setLocalDescription(offer);

  // 5. Offer를 WebSocket으로 전송
  WebSocketService.sendOffer({
    type: 'offer',
    sdp: offer.sdp,
    roomId: roomId,
    senderId: this.userId,
    receiverId: receiverId
  });
}
```

**백엔드 라우팅:**
```java
@MessageMapping("/webrtc.offer")
public void handleOffer(@Payload SdpMessage offer) {
    // 수신자에게 Offer 전달
    messagingTemplate.convertAndSendToUser(
        String.valueOf(offer.getReceiverId()), 
        "/queue/webrtc-offer", 
        offer
    );
}
```

#### Step 4: SDP Answer 생성 및 전송 (수신자)

```typescript
private async handleRemoteOffer(offerMessage: SDPMessage): Promise<void> {
  // 1. PeerConnection 생성
  this.peerConnection = this.createPeerConnection();
  
  // 2. 로컬 미디어 트랙 추가
  this.localStream.getTracks().forEach(track => {
    this.peerConnection.addTrack(track, this.localStream);
  });

  // 3. Remote SDP 설정
  const offer = new RTCSessionDescription({
    type: 'offer',
    sdp: offerMessage.sdp
  });
  await this.peerConnection.setRemoteDescription(offer);

  // 4. Answer 생성
  const answer = await this.peerConnection.createAnswer();
  await this.peerConnection.setLocalDescription(answer);

  // 5. Answer 전송
  WebSocketService.sendAnswer({
    type: 'answer',
    sdp: answer.sdp,
    roomId: offerMessage.roomId,
    senderId: this.userId,
    receiverId: offerMessage.senderId
  });
}
```

#### Step 5: ICE Candidate 교환 (양방향)

```typescript
private async handleRemoteIceCandidate(iceMessage: ICECandidateMessage) {
  // Remote Description이 설정된 후에만 추가 가능
  if (!this.peerConnection.remoteDescription) {
    this.pendingIceCandidates.push(iceMessage);  // 큐에 저장
    return;
  }

  const candidate = new RTCIceCandidate({
    candidate: iceMessage.candidate,
    sdpMid: iceMessage.sdpMid,
    sdpMLineIndex: iceMessage.sdpMLineIndex
  });

  await this.peerConnection.addIceCandidate(candidate);
}
```

**핵심 개념:**
- **SDP (Session Description Protocol)**: 미디어 정보 교환 (코덱, 해상도 등)
- **ICE Candidate**: 네트워크 경로 정보 (공인 IP, 포트 등)
- **STUN Server**: NAT 뒤의 공인 IP 확인

---

### 3.4 미디어 스트림 전송 (P2P)

#### Step 6: 양방향 미디어 스트림 수신

```typescript
private createPeerConnection(): RTCPeerConnection {
  const pc = new RTCPeerConnection({ iceServers: this.iceServers });

  // 원격 스트림 수신 이벤트
  pc.ontrack = (event) => {
    this.remoteStream = event.streams[0];
    this.onRemoteStreamCallback?.(this.remoteStream);  // UI에 표시
  };

  // 연결 상태 모니터링
  pc.onconnectionstatechange = () => {
    console.log('Connection state:', pc.connectionState);
    if (pc.connectionState === 'connected') {
      this.callState.isConnected = true;
    }
  };

  return pc;
}
```

**UI에서 스트림 표시:**
```typescript
// VideoCall.tsx
<video
  ref={localVideoRef}
  autoPlay
  playsInline
  muted
  className="w-full h-full object-cover"
/>
<video
  ref={remoteVideoRef}
  autoPlay
  playsInline
  className="w-full h-full object-cover"
/>

useEffect(() => {
  if (callState.localStream && localVideoRef.current) {
    localVideoRef.current.srcObject = callState.localStream;
  }
  if (callState.remoteStream && remoteVideoRef.current) {
    remoteVideoRef.current.srcObject = callState.remoteStream;
  }
}, [callState.localStream, callState.remoteStream]);
```

---

### 3.5 화면 공유 기능

```typescript
async startScreenShare(): Promise<void> {
  // 화면 캡처 시작
  this.screenStream = await navigator.mediaDevices.getDisplayMedia({
    video: { cursor: 'always' },
    audio: false
  });

  const screenTrack = this.screenStream.getVideoTracks()[0];

  // 기존 비디오 트랙을 화면 공유 트랙으로 교체
  if (this.videoSender) {
    await this.videoSender.replaceTrack(screenTrack);
  }

  // 사용자가 브라우저에서 공유 중지 시 자동 복구
  screenTrack.onended = () => {
    this.stopScreenShare();
  };

  this.callState.isScreenSharing = true;
}

async stopScreenShare(): Promise<void> {
  // 화면 공유 중지
  this.screenStream.getTracks().forEach(track => track.stop());

  // 원래 카메라 비디오로 복구
  if (this.videoSender && this.localStream) {
    const videoTrack = this.localStream.getVideoTracks()[0];
    await this.videoSender.replaceTrack(videoTrack);
  }

  this.callState.isScreenSharing = false;
}
```

**특징:**
- `replaceTrack()` 사용으로 재연결 없이 실시간 전환
- 브라우저의 공유 중지 버튼으로도 자동 복구
- P2P 연결 유지

---

### 3.6 시그널링 메시지 타입

```typescript
enum MessageType {
  // 통화 관리
  CALL_REQUEST,      // 통화 요청
  CALL_ACCEPT,       // 통화 수락
  CALL_REJECT,       // 통화 거절
  CALL_END,          // 통화 종료
  
  // WebRTC 시그널링
  OFFER,             // SDP Offer
  ANSWER,            // SDP Answer
  ICE_CANDIDATE,     // ICE Candidate
  
  // 상담 동기화
  CONSULTATION_STEP_SYNC,  // 상담 단계 동기화
  CONSULTATION_NOTE_SYNC,  // 상담 메모 동기화
}
```

---

### 3.7 전체 시퀀스 다이어그램

```
고객                  WebSocket        백엔드         상담원
 │                      │               │              │
 │──통화 요청 (REST)────►│──────────────►│              │
 │                      │               │──알림────────►│
 │                      │               │◄─수락────────│
 │◄─────────────────────│◄──────────────│              │
 │                      │               │              │
 │──SDP Offer───────────►│──────────────►│──────────────►│
 │                      │               │◄─SDP Answer──│
 │◄─────────────────────│◄──────────────│              │
 │                      │               │              │
 │──ICE Candidate───────►│──────────────►│──────────────►│
 │◄─────────────────────│◄──────────────│◄─ICE Candidate│
 │                      │               │              │
 ╞═══════════════════════P2P Media Stream═══════════════╡
 │                   (Video/Audio)                      │
 │◄─────────────────────────────────────────────────────►│
```

---

## 4. 실시간 동기화 기능

### 4.1 상담 단계 진행 동기화

상담사가 단계를 변경하면 고객 화면이 **즉시 동일한 단계로 이동**합니다.

#### 4단계 상품 가입 프로세스

```
1단계: 신분증 확인
2단계: 상품 정보 확인 (PDF 문서 열람)
3단계: 가입 정보 입력 및 예상 수익 확인
4단계: 최종 확인 및 승인
```

#### 동기화 메커니즘

**상담사 측 (단계 변경 시):**

**파일:** `frontend/hanainplan/src/components/consultation/ProductConsultation.tsx`

```typescript
const handleNextStep = () => {
  if (canProceedToNextStep()) {
    const newStep = Math.min(currentStep + 1, 4);
    setCurrentStep(newStep);
    
    // 상담사가 단계를 변경하면 고객에게 동기화
    if (currentUserRole === 'counselor' && consultationInfo.id) {
      WebSocketService.sendConsultationStepSync({
        type: 'CONSULTATION_STEP_SYNC',
        roomId: consultationInfo.id,
        senderId: currentUserId,
        receiverId: targetUserId,
        data: { 
          step: newStep, 
          idVerified,
          depositFormData: productType === 'deposit' ? depositFormData : undefined,
          fundFormData: productType === 'fund' ? fundFormData : undefined
        }
      });
    }
  }
};
```

**백엔드 라우팅:**

**파일:** `backend/hanainplan/src/main/java/com/hanainplan/domain/webrtc/controller/WebRTCSignalController.java`

```java
@MessageMapping("/consultation.step-sync")
public void handleConsultationStepSync(@Payload WebRTCMessage message) {
    log.info("🔄 Consultation step sync from {} to {}, room {}, step: {}", 
            message.getSenderId(), message.getReceiverId(), 
            message.getRoomId(), message.getData());
    
    // 고객에게 단계 동기화 메시지 전송
    messagingTemplate.convertAndSendToUser(
            String.valueOf(message.getReceiverId()), 
            "/queue/consultation-step-sync", 
            message
    );
}
```

**고객 측 (메시지 수신):**

```typescript
useEffect(() => {
  if (currentUserRole === 'customer') {
    // 고객은 상담사로부터 단계 동기화 메시지를 받음
    WebSocketService.onConsultationStepSync((message) => {
      if (message.data) {
        // 단계 자동 업데이트
        setCurrentStep(message.data.step || currentStep);
        setIdVerified(message.data.idVerified || idVerified);
        
        // 상담사가 입력한 폼 데이터도 동기화
        if (message.data.depositFormData) {
          setDepositFormData(prev => ({
            ...prev,
            ...message.data.depositFormData
          }));
        }
        if (message.data.fundFormData) {
          setFundFormData(prev => ({
            ...prev,
            ...message.data.fundFormData
          }));
        }
      }
    });
  }
}, [currentUserRole, currentStep, idVerified]);
```

---

### 4.2 폼 데이터 실시간 동기화

상담사가 금액, 기간 등을 입력하면 고객 화면에 **즉시 반영**됩니다.

#### 예금 상품 예시

```typescript
useEffect(() => {
  if (productType === 'deposit' && 
      depositFormData.initialBalance && 
      Number(depositFormData.initialBalance) >= 1000000 &&
      depositFormData.depositPeriod &&
      productInfo) {
    
    // 가입 기간에 맞는 실제 금리 찾기
    const rate = findInterestRateForPeriod(
      depositFormData.depositPeriod, 
      productInfo.bankCode || 'HANA'
    );
    
    // 예상 수익 자동 계산
    calculateDepositProjection(
      Number(depositFormData.initialBalance),
      depositFormData.depositPeriod,
      rate
    );
    
    // 상담사가 폼을 변경하면 고객에게 실시간 동기화
    if (currentUserRole === 'counselor' && consultationInfo.id && currentStep === 3) {
      WebSocketService.sendConsultationStepSync({
        type: 'CONSULTATION_STEP_SYNC',
        roomId: consultationInfo.id,
        senderId: currentUserId,
        receiverId: targetUserId,
        data: { 
          step: currentStep,
          idVerified,
          depositFormData  // 폼 데이터 전체 전송
        }
      });
    }
  }
}, [depositFormData.initialBalance, depositFormData.depositPeriod]);
```

**고객 화면 (읽기 전용):**

```typescript
<input
  type="number"
  value={depositFormData.initialBalance}
  onChange={(e) => setDepositFormData(prev => ({ 
    ...prev, initialBalance: e.target.value 
  }))}
  disabled={currentUserRole === 'customer'}  // 고객은 읽기 전용
/>
```

---

### 4.3 PDF 문서 공유

상담사와 고객이 동일한 상품 약관, 설명서 등을 **각자의 화면에서 열람**할 수 있습니다.

#### PDF 파일 구조

```
/public/pdf/
  ├── deposit/              # 예금 상품 문서
  │   ├── HANA-DEP-001_term.pdf        # 이용 약관
  │   └── HANA-DEP-001_info.pdf        # 상품 설명서
  └── fund/                 # 펀드 상품 문서
      ├── 30810C_term.pdf              # 특약
      ├── 30810C_info.pdf              # 투자설명서
      ├── 30810C_info_simple.pdf       # 간이투자설명서
      └── 30810C_report.pdf            # 운용보고서
```

#### PDF 열람 기능

```typescript
const openPdfDocument = (docType: string) => {
  if (!productInfo) return;
  
  let pdfPath = '';
  
  if (productType === 'deposit') {
    const fileName = `${productInfo.depositCode}_${docType}.pdf`;
    pdfPath = `/pdf/deposit/${fileName}`;
  } else if (productType === 'fund') {
    const fileName = `${productInfo.childFundCd}_${docType}.pdf`;
    pdfPath = `/pdf/fund/${fileName}`;
  }
  
  // 새 창에서 PDF 열기 (상담사와 고객이 각자 열람)
  window.open(pdfPath, '_blank');
};

const downloadPdfDocument = (docType: string, docName: string) => {
  if (!productInfo) return;
  
  let pdfPath = '';
  let fileName = '';
  
  if (productType === 'deposit') {
    fileName = `${productInfo.name}_${docName}.pdf`;
    pdfPath = `/pdf/deposit/${productInfo.depositCode}_${docType}.pdf`;
  } else if (productType === 'fund') {
    fileName = `${productInfo.fundName}_${docName}.pdf`;
    pdfPath = `/pdf/fund/${productInfo.childFundCd}_${docType}.pdf`;
  }
  
  // 다운로드 트리거
  const link = document.createElement('a');
  link.href = pdfPath;
  link.download = fileName;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
};
```

#### UI 예시 (펀드 문서)

```typescript
<div className="grid grid-cols-1 md:grid-cols-2 gap-4">
  {/* 특약 */}
  <div className="bg-gradient-to-br from-purple-50 to-purple-100 border border-purple-200 rounded-lg p-4">
    <h5 className="font-bold text-gray-900 mb-1">특약</h5>
    <p className="text-xs text-gray-600">펀드 투자 관련 특별 약정 사항</p>
    <div className="flex gap-2">
      <button
        onClick={() => openPdfDocument('term')}
        className="flex-1 px-3 py-2 bg-purple-600 text-white rounded-lg hover:bg-purple-700"
      >
        📖 열람
      </button>
      <button
        onClick={() => downloadPdfDocument('term', '특약')}
        className="flex-1 px-3 py-2 bg-white border border-purple-600 text-purple-600 rounded-lg hover:bg-purple-50"
      >
        💾 다운로드
      </button>
    </div>
  </div>
  
  {/* 투자설명서, 간이투자설명서, 운용보고서 등 */}
  ...
</div>
```

---

### 4.4 상담 메모 실시간 동기화

#### 메모 종류
- **개인 메모**: 상담사/고객 각자만 볼 수 있는 메모 (동기화 X)
- **공유 메모**: 상담사가 작성하면 고객에게 실시간 전송 (동기화 O)

#### Markdown 에디터 사용

**파일:** `frontend/hanainplan/src/components/consultation/NotesTab.tsx`

```typescript
import MDEditor from '@uiw/react-md-editor';  // 마크다운 편집기
import ReactMarkdown from 'react-markdown';   // 마크다운 렌더러
```

#### 공유 메모 저장 & 동기화

```typescript
const handleSaveShared = async () => {
  // 1. DB에 저장
  await saveNote('SHARED', tabState.sharedNote);
  
  // 2. WebSocket으로 고객에게 실시간 동기화 (상담사만)
  if (currentUserRole === 'counselor' && targetUserId) {
    WebSocketService.sendConsultationNoteSync({
      type: 'CONSULTATION_NOTE_SYNC',
      roomId: consultationId,
      senderId: currentUserId,
      receiverId: targetUserId,
      data: { 
        noteType: 'SHARED', 
        content: tabState.sharedNote 
      }
    });
  }
};
```

**백엔드 라우팅:**

```java
@MessageMapping("/consultation.note-sync")
public void handleConsultationNoteSync(@Payload WebRTCMessage message) {
    log.info("📝 Consultation note sync from {} to {}, room {}", 
            message.getSenderId(), message.getReceiverId(), message.getRoomId());
    
    // 고객에게 메모 동기화 메시지 전송
    messagingTemplate.convertAndSendToUser(
            String.valueOf(message.getReceiverId()), 
            "/queue/consultation-note-sync", 
            message
    );
}
```

**고객 측 수신 & 화면 갱신:**

```typescript
useEffect(() => {
  if (currentUserRole === 'customer') {
    // 고객은 상담사로부터 메모 동기화 메시지를 받음
    WebSocketService.onConsultationNoteSync(async (message) => {
      console.log('📩 공유 메모 동기화 메시지 수신:', message);
      
      if (message.data && message.data.noteType === 'SHARED') {
        console.log('🔄 공유 메모 갱신 중...');
        
        // 공유 메모만 DB에서 다시 로드
        try {
          const sharedNote = await getSharedNote(consultationId);
          setTabState(prev => ({
            ...prev,
            sharedNote: sharedNote?.content || ''
          }));
          console.log('✅ 공유 메모 갱신 완료');
        } catch (error) {
          console.error('공유 메모 갱신 실패:', error);
        }
      }
    });
  }
}, [currentUserRole, consultationId]);
```

**고객 화면 렌더링 (읽기 전용):**

```typescript
{currentUserRole === 'counselor' ? (
  // 상담사: 편집 가능한 마크다운 에디터
  <MDEditor
    value={tabState.sharedNote}
    onChange={handleSharedNoteChange}
    height={400}
    preview="edit"
  />
) : (
  // 고객: 읽기 전용 마크다운 뷰어
  <div className="p-4 min-h-[400px] bg-gray-50">
    {tabState.sharedNote ? (
      <div className="prose max-w-none">
        <ReactMarkdown>{tabState.sharedNote}</ReactMarkdown>
      </div>
    ) : (
      <p>상담사가 작성한 공유 메모가 없습니다.</p>
    )}
  </div>
)}
```

---

### 4.5 비밀번호 입력 상태 동기화 (보안 처리)

고객이 IRP 계좌 비밀번호를 입력할 때, **비밀번호 자체는 전송하지 않고** 입력 완료 여부만 상담사에게 알립니다.

#### 고객 측 (비밀번호 입력)

```typescript
useEffect(() => {
  if (currentUserRole === 'customer' && 
      consultationInfo.id && 
      currentStep === 3) {
    
    // 4자리 숫자 입력 완료 여부 확인
    const isCompleted = irpPassword.password.length === 4 && 
                       /^\d{4}$/.test(irpPassword.password);
    
    if (isCompleted !== passwordCompleted) {
      setPasswordCompleted(isCompleted);
      
      // 상담사에게 비밀번호 입력 완료 상태만 전송 (비밀번호 자체는 전송하지 않음)
      WebSocketService.sendConsultationStepSync({
        type: 'CONSULTATION_STEP_SYNC',
        roomId: consultationInfo.id,
        senderId: currentUserId,
        receiverId: targetUserId,
        data: { 
          step: currentStep,
          passwordCompleted: isCompleted  // boolean 값만 전송
        }
      });
    }
  }
}, [irpPassword.password, currentUserRole, consultationInfo.id, currentStep]);
```

#### 상담사 화면 (입력 대기 표시)

```typescript
<div className="bg-white rounded-lg p-6 text-center">
  <div className="text-4xl mb-3">🔒</div>
  <p className="text-gray-700 font-medium mb-2">
    고객이 IRP 계좌 비밀번호를 입력 중입니다
  </p>
  <p className="text-sm text-gray-500">
    보안을 위해 상담사는 비밀번호를 확인할 수 없습니다
  </p>
  
  {passwordCompleted ? (
    <div className="mt-4 p-3 bg-green-100 border border-green-300 rounded-lg">
      <p className="text-green-800 font-medium">
        ✅ 고객이 비밀번호 입력을 완료했습니다
      </p>
    </div>
  ) : (
    <div className="mt-4 p-3 bg-gray-100 border border-gray-300 rounded-lg">
      <p className="text-gray-600">
        ⏳ 고객의 비밀번호 입력을 기다리고 있습니다...
      </p>
    </div>
  )}
</div>
```

---

### 4.6 전체 동기화 시퀀스 다이어그램

```
┌──────────┐                 ┌──────────┐                 ┌──────────┐
│ 상담사    │                 │  서버     │                 │  고객     │
└─────┬────┘                 └─────┬────┘                 └─────┬────┘
      │                            │                            │
      │ 1️⃣ 단계 변경 (Step 2→3)      │                            │
      ├──────────────────────────►│                            │
      │ CONSULTATION_STEP_SYNC     │                            │
      │ { step: 3, depositFormData }│                            │
      │                            │ 2️⃣ 라우팅                   │
      │                            ├──────────────────────────►│
      │                            │ /user/123/queue/...        │
      │                            │                            │
      │                            │              3️⃣ 화면 업데이트│
      │                            │                   setCurrentStep(3)
      │                            │                   setDepositFormData(...)
      │                            │                            │
      │ 4️⃣ 금액 입력 (1000만원)       │                            │
      ├──────────────────────────►│                            │
      │ (실시간 동기화)                │                            │
      │                            ├──────────────────────────►│
      │                            │              5️⃣ 금액 표시   │
      │                            │                   10,000,000원
      │                            │                            │
      │                            │   6️⃣ 비밀번호 입력 (••••)    │
      │                            │◄──────────────────────────┤
      │                            │ passwordCompleted: true    │
      │ 7️⃣ 입력 완료 알림             │                            │
      │◄───────────────────────────┤                            │
      │ "✅ 고객 비밀번호 입력 완료"      │                            │
      │                            │                            │
      │ 8️⃣ 공유 메모 저장               │                            │
      ├──────────────────────────►│                            │
      │ CONSULTATION_NOTE_SYNC     │                            │
      │ { noteType: 'SHARED', ... }│                            │
      │                            ├──────────────────────────►│
      │                            │              9️⃣ 메모 표시   │
      │                            │                   (Markdown)
      │                            │                            │
```

---

## 5. 기술적 특장점

### 5.1 동기화 기능 비교표

| 기능 | 동기화 방식 | 전송 데이터 | 특징 |
|------|------------|-----------|------|
| **단계 진행** | WebSocket (STOMP) | 단계 번호, 폼 데이터 | 상담사 변경 → 고객 즉시 반영 |
| **폼 데이터** | WebSocket (실시간) | 금액, 기간, 선택값 | `useEffect` 감지 → 자동 전송 |
| **PDF 문서** | 독립적 열람 | 없음 | 각자 브라우저에서 새 창 열기 |
| **공유 메모** | WebSocket + DB | Markdown 텍스트 | 상담사 저장 → 고객 자동 갱신 |
| **개인 메모** | DB만 저장 | Markdown 텍스트 | 동기화 없음 (개인용) |
| **비밀번호 상태** | WebSocket | boolean | 비밀번호 자체는 미전송 (보안) |

### 5.2 WebRTC 핵심 특징

✅ **P2P 직접 연결**
- 서버를 거치지 않고 클라이언트 간 직접 미디어 전송
- 저지연 (Low Latency)
- 서버 부하 최소화

✅ **시그널링 서버 분리**
- WebSocket은 연결 설정(SDP, ICE)에만 사용
- 실제 미디어는 P2P로 전송
- 효율적인 리소스 활용

✅ **ICE Candidate 큐잉**
- Remote Description 설정 전 도착한 candidate 임시 저장
- 순서 보장으로 안정적인 연결

✅ **실시간 트랙 교체**
- `replaceTrack()`으로 재연결 없이 화면 공유 전환
- 끊김 없는 사용자 경험

### 5.3 동기화 최적화

✅ **STOMP 프로토콜**
- 메시지 타입별 라우팅
- 특정 사용자 타겟팅 지원
- 구독/발행 패턴

✅ **SockJS Fallback**
- WebSocket 미지원 환경에서 자동으로 HTTP 폴링 전환
- 최대 호환성 보장

✅ **상담 동기화**
- 문서 공유, 단계 진행, 메모 등을 WebSocket으로 실시간 동기화
- 상담사와 고객이 동일한 화면을 보며 상담

### 5.4 보안 및 UX

✅ **보안**
- 비밀번호는 클라이언트에만 저장, 서버 미전송
- 입력 완료 여부만 boolean으로 전달
- 고객은 상담사 입력 필드 수정 불가 (`disabled`)

✅ **UX**
- 실시간 예상 수익 자동 계산 및 표시
- 마크다운 에디터로 서식 있는 메모 작성
- PDF 새 창 열기로 화상 채팅 유지
- 단계별 진행 표시기로 현재 위치 시각화

✅ **성능**
- `useEffect` 의존성 배열로 불필요한 전송 방지
- DB 조회는 필요 시에만 (메모 동기화 시)
- WebSocket 재연결 자동 처리 (`reconnectDelay: 5000`)
- 하트비트로 연결 상태 지속 모니터링

---

## 6. 결론

본 시스템은 **WebRTC를 활용한 P2P 화상 통화**와 **WebSocket 기반 실시간 동기화**를 결합하여, 상담사와 고객이 마치 같은 공간에서 상담하는 것과 같은 경험을 제공합니다.

### 주요 성과

1. **저지연 화상 통화**: P2P 연결로 서버 부하 없이 고품질 통화 제공
2. **실시간 화면 동기화**: 상담사의 모든 작업이 고객 화면에 즉시 반영
3. **보안 강화**: 민감 정보(비밀번호)는 절대 전송하지 않는 설계
4. **끊김 없는 UX**: 화면 공유 전환, 문서 열람 등 자연스러운 흐름

### 기술적 차별점

- **Hybrid Architecture**: WebRTC(미디어) + WebSocket(시그널링/동기화) 분리
- **상태 관리 최적화**: React의 `useEffect`를 활용한 자동 동기화
- **Fallback 메커니즘**: SockJS로 모든 환경에서 작동 보장
- **확장 가능한 구조**: 새로운 동기화 기능 추가 용이

---

**작성일**: 2025년 10월 13일  
**작성자**: HANAinPLAN 개발팀  
**기술 스택**: React, TypeScript, WebRTC, STOMP, Spring Boot, MySQL


