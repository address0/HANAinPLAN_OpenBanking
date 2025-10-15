import { Client } from '@stomp/stompjs';
import type { Message } from '@stomp/stompjs';
import SockJS from 'sockjs-client';

export interface WebRTCMessage {
  type: MessageType;
  roomId: string;
  senderId: number;
  receiverId: number;
  data?: any;
}

export const MessageType = {
  CALL_REQUEST: 'CALL_REQUEST',
  CALL_ACCEPT: 'CALL_ACCEPT',
  CALL_REJECT: 'CALL_REJECT',
  CALL_END: 'CALL_END',
  CONSULTATION_START: 'CONSULTATION_START',
  OFFER: 'OFFER',
  ANSWER: 'ANSWER',
  ICE_CANDIDATE: 'ICE_CANDIDATE',
  HIGHLIGHT_ADD: 'HIGHLIGHT_ADD',
  HIGHLIGHT_REMOVE: 'HIGHLIGHT_REMOVE',
  STEP_SYNC: 'STEP_SYNC',
  CONSULTATION_STEP_SYNC: 'CONSULTATION_STEP_SYNC',
  CONSULTATION_NOTE_SYNC: 'CONSULTATION_NOTE_SYNC',
  USER_JOINED: 'USER_JOINED',
  USER_LEFT: 'USER_LEFT',
  ERROR: 'ERROR'
} as const;

export type MessageType = typeof MessageType[keyof typeof MessageType];

export interface SDPMessage {
  type: string;
  sdp: string;
  roomId: string;
  senderId: number;
  receiverId: number;
}

export interface ICECandidateMessage {
  candidate: string;
  sdpMid: string;
  sdpMLineIndex: number;
  roomId: string;
  senderId: number;
  receiverId: number;
}

export interface CallRequestMessage {
  roomId: string;
  callerId: number;
  calleeId: number;
  callerName: string;
  calleeName: string;
}

class WebSocketService {
  private client: Client | null = null;
  private userId: number | null = null;
  private isConnected = false;

  private onCallRequestCallback?: (message: CallRequestMessage) => void;
  private onCallAcceptCallback?: (message: WebRTCMessage) => void;
  private onCallRejectCallback?: (message: WebRTCMessage) => void;
  private onCallEndCallback?: (message: WebRTCMessage) => void;
  private onConsultationStartCallback?: (message: WebRTCMessage) => void;
  private onOfferCallback?: (message: SDPMessage) => void;
  private onAnswerCallback?: (message: SDPMessage) => void;
  private onIceCandidateCallback?: (message: ICECandidateMessage) => void;
  private onHighlightSyncCallback?: (message: WebRTCMessage) => void;
  private onStepSyncCallback?: (message: WebRTCMessage) => void;
  private onConsultationStepSyncCallback?: (message: WebRTCMessage) => void;
  private onConsultationNoteSyncCallback?: (message: WebRTCMessage) => void;
  private onConnectionStateChangeCallback?: (connected: boolean) => void;

  constructor() {
    this.client = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: {},
      debug: (str) => {
      },
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
    });

    this.client.onConnect = (frame) => {
      this.isConnected = true;
      this.onConnectionStateChangeCallback?.(true);
      this.subscribeToMessages();
    };

    this.client.onDisconnect = () => {
      this.isConnected = false;
      this.onConnectionStateChangeCallback?.(false);
    };

    this.client.onStompError = (frame) => {
      this.isConnected = false;
      this.onConnectionStateChangeCallback?.(false);
    };
  }

  connect(userId: number): Promise<void> {
    return new Promise((resolve, reject) => {
      if (this.isConnected) {
        resolve();
        return;
      }

      this.userId = userId;
      this.client!.connectHeaders = { userId: userId.toString() };

      const originalOnConnect = this.client!.onConnect;
      this.client!.onConnect = (frame) => {
        originalOnConnect?.(frame);
        resolve();
      };

      const originalOnStompError = this.client!.onStompError;
      this.client!.onStompError = (frame) => {
        originalOnStompError?.(frame);
        reject(new Error(`WebSocket connection failed: ${frame.headers['message'] || 'Unknown error'}`));
      };

      this.client!.activate();
    });
  }

  disconnect(): void {
    if (this.client && this.isConnected) {
      this.client.deactivate();
      this.isConnected = false;
      this.userId = null;
    }
  }

  private subscribeToMessages(): void {
    if (!this.client || !this.isConnected) return;

    this.client.subscribe('/user/queue/call-request', (message: Message) => {
      const callRequest: CallRequestMessage = JSON.parse(message.body);
      this.onCallRequestCallback?.(callRequest);
    });

    this.client.subscribe('/user/queue/call-accept', (message: Message) => {
      const acceptMessage: WebRTCMessage = JSON.parse(message.body);
      this.onCallAcceptCallback?.(acceptMessage);
    });

    this.client.subscribe('/user/queue/call-reject', (message: Message) => {
      const rejectMessage: WebRTCMessage = JSON.parse(message.body);
      this.onCallRejectCallback?.(rejectMessage);
    });

    this.client.subscribe('/user/queue/call-end', (message: Message) => {
      const endMessage: WebRTCMessage = JSON.parse(message.body);
      this.onCallEndCallback?.(endMessage);
    });

    this.client.subscribe('/user/queue/consultation-start', (message: Message) => {
      const startMessage: WebRTCMessage = JSON.parse(message.body);
      this.onConsultationStartCallback?.(startMessage);
    });

    this.client.subscribe('/user/queue/webrtc-offer', (message: Message) => {
      const offer: SDPMessage = JSON.parse(message.body);
      this.onOfferCallback?.(offer);
    });

    this.client.subscribe('/user/queue/webrtc-answer', (message: Message) => {
      const answer: SDPMessage = JSON.parse(message.body);
      this.onAnswerCallback?.(answer);
    });

    this.client.subscribe('/user/queue/webrtc-ice', (message: Message) => {
      const iceCandidate: ICECandidateMessage = JSON.parse(message.body);
      this.onIceCandidateCallback?.(iceCandidate);
    });

    this.client.subscribe('/user/queue/step-sync', (message: Message) => {
      const syncMessage: WebRTCMessage = JSON.parse(message.body);
      this.onStepSyncCallback?.(syncMessage);
    });

    this.client.subscribe('/user/queue/consultation-step-sync', (message: Message) => {
      const syncMessage: WebRTCMessage = JSON.parse(message.body);
      this.onConsultationStepSyncCallback?.(syncMessage);
    });

    this.client.subscribe('/user/queue/consultation-note-sync', (message: Message) => {
      const syncMessage: WebRTCMessage = JSON.parse(message.body);
      this.onConsultationNoteSyncCallback?.(syncMessage);
    });
  }

  sendCallRequest(callRequest: CallRequestMessage): void {
    if (!this.client || !this.isConnected) {
      return;
    }
    this.client.publish({
      destination: '/app/call.request',
      body: JSON.stringify(callRequest)
    });
  }

  sendCallAccept(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/call.accept',
      body: JSON.stringify(message)
    });
  }

  sendCallReject(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/call.reject',
      body: JSON.stringify(message)
    });
  }

  sendCallEnd(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/call.end',
      body: JSON.stringify(message)
    });
  }

  sendConsultationStart(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/consultation.start',
      body: JSON.stringify(message)
    });
  }

  sendOffer(offer: SDPMessage): void {
    if (!this.client || !this.isConnected) return;

    this.client.publish({
      destination: '/app/webrtc.offer',
      body: JSON.stringify(offer)
    });
  }

  sendAnswer(answer: SDPMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/webrtc.answer',
      body: JSON.stringify(answer)
    });
  }

  sendIceCandidate(iceCandidate: ICECandidateMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/webrtc.ice',
      body: JSON.stringify(iceCandidate)
    });
  }

  sendHighlightSync(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/highlight.sync',
      body: JSON.stringify(message)
    });
  }

  sendStepSync(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/step.sync',
      body: JSON.stringify(message)
    });
  }

  sendConsultationStepSync(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/consultation.step-sync',
      body: JSON.stringify(message)
    });
  }

  sendConsultationNoteSync(message: WebRTCMessage): void {
    if (!this.client || !this.isConnected) return;
    this.client.publish({
      destination: '/app/consultation.note-sync',
      body: JSON.stringify(message)
    });
  }

  onCallRequest(callback: (message: CallRequestMessage) => void): void {
    this.onCallRequestCallback = callback;
  }

  onCallAccept(callback: (message: WebRTCMessage) => void): void {
    this.onCallAcceptCallback = callback;
  }

  onCallReject(callback: (message: WebRTCMessage) => void): void {
    this.onCallRejectCallback = callback;
  }

  onCallEnd(callback: (message: WebRTCMessage) => void): void {
    this.onCallEndCallback = callback;
  }

  onConsultationStart(callback: (message: WebRTCMessage) => void): void {
    this.onConsultationStartCallback = callback;
  }

  onOffer(callback: (message: SDPMessage) => void): void {
    this.onOfferCallback = callback;
  }

  onAnswer(callback: (message: SDPMessage) => void): void {
    this.onAnswerCallback = callback;
  }

  onIceCandidate(callback: (message: ICECandidateMessage) => void): void {
    this.onIceCandidateCallback = callback;
  }

  onConnectionStateChange(callback: (connected: boolean) => void): void {
    this.onConnectionStateChangeCallback = callback;
  }

  onHighlightSync(callback: (message: WebRTCMessage) => void): void {
    this.onHighlightSyncCallback = callback;
  }

  onStepSync(callback: (message: WebRTCMessage) => void): void {
    this.onStepSyncCallback = callback;
  }

  onConsultationStepSync(callback: (message: WebRTCMessage) => void): void {
    this.onConsultationStepSyncCallback = callback;
  }

  onConsultationNoteSync(callback: (message: WebRTCMessage) => void): void {
    this.onConsultationNoteSyncCallback = callback;
  }

  getConnectionStatus(): boolean {
    return this.isConnected;
  }

  getUserId(): number | null {
    return this.userId;
  }
}

export default new WebSocketService();