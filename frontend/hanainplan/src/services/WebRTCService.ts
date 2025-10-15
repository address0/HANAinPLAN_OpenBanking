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
  isScreenSharing: boolean;
  screenStream: MediaStream | null;
}

class WebRTCService {
  private peerConnection: RTCPeerConnection | null = null;
  private localStream: MediaStream | null = null;
  private remoteStream: MediaStream | null = null;
  private screenStream: MediaStream | null = null;
  private videoSender: RTCRtpSender | null = null;

  private pendingIceCandidates: ICECandidateMessage[] = [];

  private callState: CallState = {
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
  };

  private onLocalStreamCallback?: (stream: MediaStream) => void;
  private onRemoteStreamCallback?: (stream: MediaStream) => void;
  private onConnectionStateChangeCallback?: (state: RTCPeerConnectionState) => void;
  private onCallStateChangeCallback?: (state: CallState) => void;
  private onErrorCallback?: (error: Error) => void;

  private iceServers: RTCIceServer[] = [
    { urls: 'stun:stun.l.google.com:19302' },
    { urls: 'stun:stun1.l.google.com:19302' },
    { urls: 'stun:stun2.l.google.com:19302' }
  ];

  constructor() {
    this.setupWebSocketCallbacks();
  }

  private setupWebSocketCallbacks(): void {
    WebSocketService.onOffer(this.handleRemoteOffer.bind(this));
    WebSocketService.onAnswer(this.handleRemoteAnswer.bind(this));
    WebSocketService.onIceCandidate(this.handleRemoteIceCandidate.bind(this));
  }

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
      this.onErrorCallback?.(new Error('미디어 장치에 접근할 수 없습니다.'));
      throw error;
    }
  }

  private createPeerConnection(): RTCPeerConnection {
    const peerConnection = new RTCPeerConnection({
      iceServers: this.iceServers
    });

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

    peerConnection.ontrack = (event) => {
      this.remoteStream = event.streams[0];
      this.callState.remoteStream = this.remoteStream;
      this.onRemoteStreamCallback?.(this.remoteStream);
      this.notifyCallStateChange();
    };

    peerConnection.onconnectionstatechange = () => {
      this.callState.isConnected = peerConnection.connectionState === 'connected';
      this.onConnectionStateChangeCallback?.(peerConnection.connectionState);
      this.notifyCallStateChange();
    };

    peerConnection.oniceconnectionstatechange = () => {
      if (peerConnection.iceConnectionState === 'failed') {
        this.onErrorCallback?.(new Error('WebRTC 연결에 실패했습니다.'));
      }
    };

    return peerConnection;
  }

  async startCall(roomId: string, calleeId: number): Promise<void> {
    try {
      if (!this.localStream) {
        await this.initializeMedia();
      }

      this.peerConnection = this.createPeerConnection();

      this.localStream!.getTracks().forEach(track => {
        this.peerConnection!.addTrack(track, this.localStream!);
      });

      this.callState = {
        ...this.callState,
        isInCall: true,
        roomId,
        calleeId,
        isCaller: true
      };
      this.notifyCallStateChange();

    } catch (error) {
      this.onErrorCallback?.(new Error('통화를 시작할 수 없습니다.'));
      throw error;
    }
  }

  async sendOffer(roomId: string, receiverId: number): Promise<void> {
    try {
      if (!this.peerConnection) {
        this.peerConnection = this.createPeerConnection();
        if (this.localStream) {
          this.localStream.getTracks().forEach(track => {
            const sender = this.peerConnection!.addTrack(track, this.localStream!);
            if (track.kind === 'video') {
              this.videoSender = sender;
            }
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
      this.onErrorCallback?.(new Error('Offer 전송 중 오류가 발생했습니다.'));
      throw error;
    }
  }

  async acceptCall(roomId: string, callerId: number): Promise<void> {
    try {
      if (!this.localStream) {
        await this.initializeMedia();
      }

      this.peerConnection = this.createPeerConnection();

      this.localStream!.getTracks().forEach(track => {
        const sender = this.peerConnection!.addTrack(track, this.localStream!);
        if (track.kind === 'video') {
          this.videoSender = sender;
        }
      });

      this.callState = {
        ...this.callState,
        isInCall: true,
        roomId,
        callerId,
        isCaller: false
      };

      this.notifyCallStateChange();

    } catch (error) {
      this.onErrorCallback?.(new Error('통화를 수락할 수 없습니다.'));
      throw error;
    }
  }

  async handleOffer(offerMessage: SDPMessage): Promise<void> {
    await this.handleRemoteOffer(offerMessage);
  }

  private async handleRemoteOffer(offerMessage: SDPMessage): Promise<void> {
    try {
      if (!this.peerConnection) {
        if (!this.localStream) {
          await this.initializeMedia();
        }

        this.peerConnection = this.createPeerConnection();

        this.localStream!.getTracks().forEach(track => {
          const sender = this.peerConnection!.addTrack(track, this.localStream!);
          if (track.kind === 'video') {
            this.videoSender = sender;
          }
        });

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

      await this.processPendingIceCandidates();

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
      this.onErrorCallback?.(new Error('원격 Offer 처리 중 오류가 발생했습니다.'));
    }
  }

  async handleAnswer(answerMessage: SDPMessage): Promise<void> {
    await this.handleRemoteAnswer(answerMessage);
  }

  private async handleRemoteAnswer(answerMessage: SDPMessage): Promise<void> {
    try {
      if (!this.peerConnection) {
        return;
      }

      const answer = new RTCSessionDescription({
        type: 'answer',
        sdp: answerMessage.sdp
      });

      await this.peerConnection.setRemoteDescription(answer);

      await this.processPendingIceCandidates();

    } catch (error) {
      this.onErrorCallback?.(new Error('원격 Answer 처리 중 오류가 발생했습니다.'));
    }
  }

  async handleIceCandidate(iceMessage: ICECandidateMessage): Promise<void> {
    await this.handleRemoteIceCandidate(iceMessage);
  }

  private async handleRemoteIceCandidate(iceMessage: ICECandidateMessage): Promise<void> {
    try {
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
      this.onErrorCallback?.(new Error('ICE Candidate 처리 중 오류가 발생했습니다.'));
    }
  }

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
      }
    }

    this.pendingIceCandidates = [];
  }

  endCall(): void {
    if (this.screenStream) {
      this.stopScreenShare();
    }

    if (this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
    }

    if (this.peerConnection) {
      this.peerConnection.close();
      this.peerConnection = null;
    }

    this.pendingIceCandidates = [];

    this.callState = {
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
    };

    this.remoteStream = null;
    this.videoSender = null;
    this.notifyCallStateChange();
  }

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

  private notifyCallStateChange(): void {
    const newState: CallState = { ...this.callState };
    this.onCallStateChangeCallback?.(newState);
  }

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

  getCallState(): CallState {
    return { ...this.callState };
  }

  getLocalStream(): MediaStream | null {
    return this.localStream;
  }

  getRemoteStream(): MediaStream | null {
    return this.remoteStream;
  }

  stopMediaForTest(): void {
    if (!this.callState.isInCall && this.localStream) {
      this.localStream.getTracks().forEach(track => track.stop());
      this.localStream = null;
      this.callState.localStream = null;
      this.notifyCallStateChange();
    }
  }

  async startScreenShare(): Promise<void> {
    try {
      if (!this.peerConnection) {
        throw new Error('통화 중이 아닙니다.');
      }

      if (this.callState.isScreenSharing) {
        throw new Error('이미 화면 공유 중입니다.');
      }

      this.screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: {
          cursor: 'always' as any
        },
        audio: false
      });

      const screenTrack = this.screenStream.getVideoTracks()[0];

      if (this.videoSender) {
        await this.videoSender.replaceTrack(screenTrack);
      }

      screenTrack.onended = () => {
        this.stopScreenShare();
      };

      this.callState.isScreenSharing = true;
      this.callState.screenStream = this.screenStream;
      this.notifyCallStateChange();

    } catch (error) {
      this.onErrorCallback?.(new Error('화면 공유를 시작할 수 없습니다.'));
      throw error;
    }
  }

  async stopScreenShare(): Promise<void> {
    try {
      if (!this.callState.isScreenSharing || !this.screenStream) {
        return;
      }

      this.screenStream.getTracks().forEach(track => track.stop());
      this.screenStream = null;

      if (this.videoSender && this.localStream) {
        const videoTrack = this.localStream.getVideoTracks()[0];
        if (videoTrack) {
          await this.videoSender.replaceTrack(videoTrack);
        }
      }

      this.callState.isScreenSharing = false;
      this.callState.screenStream = null;
      this.notifyCallStateChange();

    } catch (error) {
      this.onErrorCallback?.(new Error('화면 공유를 중지할 수 없습니다.'));
    }
  }

  async toggleScreenShare(): Promise<boolean> {
    if (this.callState.isScreenSharing) {
      await this.stopScreenShare();
      return false;
    } else {
      await this.startScreenShare();
      return true;
    }
  }
}

export default new WebRTCService();