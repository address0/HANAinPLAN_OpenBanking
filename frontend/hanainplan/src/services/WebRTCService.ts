import WebSocketService from './WebSocketService';
import type { SDPMessage, ICECandidateMessage } from './WebSocketService';

export interface CallState {
  isInCall: boolean;
  roomId: string | null;
  isConnected: boolean;
  localStream: MediaStream | null;
  remoteStream: MediaStream | null;
  callerId: number | null;
  calleeId: number | null;
  isCaller: boolean;
}

class WebRTCService {
  private peerConnection: RTCPeerConnection | null = null;
  private localStream: MediaStream | null = null;
  private remoteStream: MediaStream | null = null;
  
  // ICE candidate 큐 (remote description 설정 전에 도착한 candidate들 저장)
  private pendingIceCandidates: ICECandidateMessage[] = [];
  
  private callState: CallState = {
    isInCall: false,
    roomId: null,
    isConnected: false,
    localStream: null,
    remoteStream: null,
    callerId: null,
    calleeId: null,
    isCaller: false
  };

  // 콜백 함수들
  private onLocalStreamCallback?: (stream: MediaStream) => void;
  private onRemoteStreamCallback?: (stream: MediaStream) => void;
  private onConnectionStateChangeCallback?: (state: RTCPeerConnectionState) => void;
  private onCallStateChangeCallback?: (state: CallState) => void;
  private onErrorCallback?: (error: Error) => void;

  // ICE 서버 설정
  private iceServers: RTCIceServer[] = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' }
  ];

  constructor() {
    this.setupWebSocketCallbacks();
  }

  // WebSocket 콜백 설정
  private setupWebSocketCallbacks(): void {
    WebSocketService.onOffer(this.handleRemoteOffer.bind(this));
    WebSocketService.onAnswer(this.handleRemoteAnswer.bind(this));
    WebSocketService.onIceCandidate(this.handleRemoteIceCandidate.bind(this));
  }

  // 로컬 미디어 스트림 초기화
  async initializeMedia(audioEnabled = true, videoEnabled = true): Promise<MediaStream> {
    try {
      this.localStream = await navigator.mediaDevices.getUserMedia({
        audio: audioEnabled,
        video: videoEnabled
      });

      this.callState.localStream = this.localStream;
      this.onLocalStreamCallback?.(this.localStream);
      this.notifyCallStateChange();

      return this.localStream;
    } catch (error) {
      console.error('Error accessing media devices:', error);
      this.onErrorCallback?.(new Error('미디어 장치에 접근할 수 없습니다.'));
      throw error;
    }
  }

  // PeerConnection 생성
  private createPeerConnection(): RTCPeerConnection {
    const peerConnection = new RTCPeerConnection({
      iceServers: this.iceServers
    });

    // ICE candidate 이벤트 처리
    peerConnection.onicecandidate = (event) => {
      if (event.candidate && this.callState.roomId) {
        const iceMessage: ICECandidateMessage = {
          candidate: event.candidate.candidate,
          sdpMid: event.candidate.sdpMid || '',
          sdpMLineIndex: event.candidate.sdpMLineIndex || 0,
          roomId: this.callState.roomId,
          senderId: WebSocketService.getUserId() || 0,
          receiverId: this.callState.isCaller ? this.callState.calleeId! : this.callState.callerId!
        };
        WebSocketService.sendIceCandidate(iceMessage);
      }
    };

    // 원격 스트림 수신
    peerConnection.ontrack = (event) => {
      this.remoteStream = event.streams[0];
      this.callState.remoteStream = this.remoteStream;
      this.onRemoteStreamCallback?.(this.remoteStream);
      this.notifyCallStateChange();
    };

    // 연결 상태 변경
    peerConnection.onconnectionstatechange = () => {
      console.log('Connection state changed:', peerConnection.connectionState);
      this.callState.isConnected = peerConnection.connectionState === 'connected';
      this.onConnectionStateChangeCallback?.(peerConnection.connectionState);
      this.notifyCallStateChange();
    };

    // ICE 연결 상태 변경
    peerConnection.oniceconnectionstatechange = () => {
      console.log('ICE connection state changed:', peerConnection.iceConnectionState);
      if (peerConnection.iceConnectionState === 'failed') {
        this.onErrorCallback?.(new Error('WebRTC 연결에 실패했습니다.'));
      }
    };

    return peerConnection;
  }

  // 통화 시작 (발신자)
  async startCall(roomId: string, calleeId: number): Promise<void> {
    try {
      if (!this.localStream) {
        await this.initializeMedia();
      }

      this.peerConnection = this.createPeerConnection();
      
      // 로컬 스트림을 PeerConnection에 추가
      this.localStream!.getTracks().forEach(track => {
        this.peerConnection!.addTrack(track, this.localStream!);
      });

      // 통화 상태 설정
      this.callState = {
        ...this.callState,
        isInCall: true,
        roomId,
        calleeId,
        isCaller: true
      };
      this.notifyCallStateChange();

    } catch (error) {
      console.error('Error starting call:', error);
      this.onErrorCallback?.(new Error('통화를 시작할 수 없습니다.'));
      throw error;
    }
  }

  // 발신자: 수신자 수락 이후 Offer 생성 및 전송
  async sendOffer(roomId: string, receiverId: number): Promise<void> {
    try {
      if (!this.peerConnection) {
        this.peerConnection = this.createPeerConnection();
        if (this.localStream) {
          this.localStream.getTracks().forEach(track => {
            this.peerConnection!.addTrack(track, this.localStream!);
          });
        }
      }

      const offer = await this.peerConnection.createOffer();
      await this.peerConnection.setLocalDescription(offer);

      const offerMessage: SDPMessage = {
        type: 'offer',
        sdp: offer.sdp!,
        roomId,
        senderId: WebSocketService.getUserId() || 0,
        receiverId
      };

      WebSocketService.sendOffer(offerMessage);
    } catch (error) {
      console.error('Error sending offer:', error);
      this.onErrorCallback?.(new Error('Offer 전송 중 오류가 발생했습니다.'));
      throw error;
    }
  }

  // 통화 수락 (수신자)
  async acceptCall(roomId: string, callerId: number): Promise<void> {
    try {
      if (!this.localStream) {
        await this.initializeMedia();
      }

      this.peerConnection = this.createPeerConnection();
      
      // 로컬 스트림을 PeerConnection에 추가
      this.localStream!.getTracks().forEach(track => {
        this.peerConnection!.addTrack(track, this.localStream!);
      });

      // 통화 상태 설정
      this.callState = {
        ...this.callState,
        isInCall: true,
        roomId,
        callerId,
        isCaller: false
      };

      this.notifyCallStateChange();

    } catch (error) {
      console.error('Error accepting call:', error);
      this.onErrorCallback?.(new Error('통화를 수락할 수 없습니다.'));
      throw error;
    }
  }

  // 원격 Offer 처리
  private async handleRemoteOffer(offerMessage: SDPMessage): Promise<void> {
    try {
      if (!this.peerConnection) {
        // 미디어 스트림이 없으면 초기화
        if (!this.localStream) {
          await this.initializeMedia();
        }
        
        // PeerConnection 생성
        this.peerConnection = this.createPeerConnection();
        
        // 로컬 스트림 추가
        this.localStream!.getTracks().forEach(track => {
          this.peerConnection!.addTrack(track, this.localStream!);
        });
        
        // 통화 상태 업데이트 (수신자로 설정)
        this.callState = {
          ...this.callState,
          isInCall: true,
          roomId: offerMessage.roomId,
          callerId: offerMessage.senderId,
          isCaller: false
        };
        
        this.notifyCallStateChange();
      }

      const offer = new RTCSessionDescription({
        type: 'offer',
        sdp: offerMessage.sdp
      });

      await this.peerConnection.setRemoteDescription(offer);

      // Remote description이 설정되었으므로 대기 중인 ICE candidate들 처리
      await this.processPendingIceCandidates();

      // Answer 생성 및 전송
      const answer = await this.peerConnection.createAnswer();
      await this.peerConnection.setLocalDescription(answer);

      const answerMessage: SDPMessage = {
        type: 'answer',
        sdp: answer.sdp!,
        roomId: offerMessage.roomId,
        senderId: WebSocketService.getUserId() || 0,
        receiverId: offerMessage.senderId
      };

      WebSocketService.sendAnswer(answerMessage);

    } catch (error) {
      console.error('Error handling remote offer:', error);
      this.onErrorCallback?.(new Error('원격 Offer 처리 중 오류가 발생했습니다.'));
    }
  }

  // 원격 Answer 처리
  private async handleRemoteAnswer(answerMessage: SDPMessage): Promise<void> {
    try {
      if (!this.peerConnection) {
        console.error('PeerConnection not initialized when receiving answer');
        return;
      }

      const answer = new RTCSessionDescription({
        type: 'answer',
        sdp: answerMessage.sdp
      });

      await this.peerConnection.setRemoteDescription(answer);

      // Remote description이 설정되었으므로 대기 중인 ICE candidate들 처리
      await this.processPendingIceCandidates();

    } catch (error) {
      console.error('Error handling remote answer:', error);
      this.onErrorCallback?.(new Error('원격 Answer 처리 중 오류가 발생했습니다.'));
    }
  }

  // 원격 ICE Candidate 처리
  private async handleRemoteIceCandidate(iceMessage: ICECandidateMessage): Promise<void> {
    try {
      // PeerConnection이 없거나 remote description이 없으면 큐에 저장
      if (!this.peerConnection || !this.peerConnection.remoteDescription) {
        this.pendingIceCandidates.push(iceMessage);
        return;
      }

      const candidate = new RTCIceCandidate({
        candidate: iceMessage.candidate,
        sdpMid: iceMessage.sdpMid,
        sdpMLineIndex: iceMessage.sdpMLineIndex
      });

      await this.peerConnection.addIceCandidate(candidate);

    } catch (error) {
      console.error('Error handling remote ICE candidate:', error);
      this.onErrorCallback?.(new Error('ICE Candidate 처리 중 오류가 발생했습니다.'));
    }
  }

  // 대기 중인 ICE candidate들 처리
  private async processPendingIceCandidates(): Promise<void> {
    if (!this.peerConnection || this.pendingIceCandidates.length === 0) {
      return;
    }
    
    for (const iceMessage of this.pendingIceCandidates) {
      try {
        const candidate = new RTCIceCandidate({
          candidate: iceMessage.candidate,
          sdpMid: iceMessage.sdpMid,
          sdpMLineIndex: iceMessage.sdpMLineIndex
        });

        await this.peerConnection.addIceCandidate(candidate);
      } catch (error) {
        console.error('Error adding pending ICE candidate:', error);
      }
    }

    // 처리 완료된 candidate들 제거
    this.pendingIceCandidates = [];
  }

  // 통화 종료
  endCall(): void {
    // 로컬 스트림 정리
    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }

    // PeerConnection 정리
    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = null;
    }

    // 대기 중인 ICE candidate들 제거
    this.pendingIceCandidates = [];

    // 통화 상태 초기화
    this.callState = {
      isInCall: false,
      roomId: null,
      isConnected: false,
      localStream: null,
      remoteStream: null,
      callerId: null,
      calleeId: null,
      isCaller: false
    };

    this.remoteStream = null;
    this.notifyCallStateChange();
  }

  // 마이크 음소거/해제
  toggleMicrophone(): boolean {
    if (this.localStream) {
      const audioTrack = this.localStream.getAudioTracks()[0];
      if (audioTrack) {
        audioTrack.enabled = !audioTrack.enabled;
        return audioTrack.enabled;
      }
    }
    return false;
  }

  // 비디오 켜기/끄기
  toggleVideo(): boolean {
    if (this.localStream) {
      const videoTrack = this.localStream.getVideoTracks()[0];
      if (videoTrack) {
        videoTrack.enabled = !videoTrack.enabled;
        return videoTrack.enabled;
      }
    }
    return false;
  }

  // 상태 변경 알림
  private notifyCallStateChange(): void {
    // 새로운 객체를 생성해서 전달 (React 상태 업데이트 보장)
    const newState: CallState = { ...this.callState };
    this.onCallStateChangeCallback?.(newState);
  }

  // 콜백 등록 메소드들
  onLocalStream(callback: (stream: MediaStream) => void): void {
    this.onLocalStreamCallback = callback;
  }

  onRemoteStream(callback: (stream: MediaStream) => void): void {
    this.onRemoteStreamCallback = callback;
  }

  onConnectionStateChange(callback: (state: RTCPeerConnectionState) => void): void {
    this.onConnectionStateChangeCallback = callback;
  }

  onCallStateChange(callback: (state: CallState) => void): void {
    this.onCallStateChangeCallback = callback;
  }

  onError(callback: (error: Error) => void): void {
    this.onErrorCallback = callback;
  }

  // 현재 통화 상태 반환
  getCallState(): CallState {
    return { ...this.callState };
  }

  // 미디어 스트림 반환
  getLocalStream(): MediaStream | null {
    return this.localStream;
  }

  getRemoteStream(): MediaStream | null {
    return this.remoteStream;
  }

  // 테스트용 미디어 중지 (통화 중이 아닐 때만)
  stopMediaForTest(): void {
    if (!this.callState.isInCall && this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
      this.callState.localStream = null;
      this.notifyCallStateChange();
    }
  }
}

export default new WebRTCService(); 