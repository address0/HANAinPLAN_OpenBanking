import React, { useState, useEffect } from 'react';
import MDEditor from '@uiw/react-md-editor';
import ReactMarkdown from 'react-markdown';
import { saveConsultationNote, getUserNote, getSharedNote } from '../../api/consultationNotesApi';
import type { ConsultationNote } from '../../api/consultationNotesApi';
import WebSocketService from '../../services/WebSocketService';

interface NotesTabProps {
  consultationId: string;
  currentUserId: number;
  currentUserRole: 'customer' | 'counselor';
  targetUserId?: number; // 상담 상대방 ID (동기화용)
}

interface TabState {
  personalNote: string;
  sharedNote: string;
  personalNoteId?: number;
  sharedNoteId?: number;
  isLoading: boolean;
  isSaving: boolean;
}

const NotesTab: React.FC<NotesTabProps> = ({ consultationId, currentUserId, currentUserRole, targetUserId }) => {
  const [activeTab, setActiveTab] = useState<'personal' | 'shared'>('personal');
  const [tabState, setTabState] = useState<TabState>({
    personalNote: '',
    sharedNote: '',
    isLoading: true,
    isSaving: false
  });

  // 메모 로드
  useEffect(() => {
    loadNotes();
  }, [consultationId, currentUserId]);

  // WebSocket 이벤트 리스너 설정 (메모 동기화)
  useEffect(() => {
    if (currentUserRole === 'customer') {
      // 고객은 상담사로부터 메모 동기화 메시지를 받음
      WebSocketService.onConsultationNoteSync(async (message) => {
        console.log('📩 공유 메모 동기화 메시지 수신:', message);
        if (message.data && message.data.noteType === 'SHARED') {
          console.log('🔄 공유 메모 갱신 중...');
          
          // 공유 메모만 다시 로드
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

    return () => {
      // cleanup (필요시)
    };
  }, [currentUserRole, consultationId]);

  const loadNotes = async () => {
    // consultationId가 없으면 로드하지 않음
    if (!consultationId || consultationId.trim() === '') {
      setTabState(prev => ({ ...prev, isLoading: false }));
      return;
    }

    setTabState(prev => ({ ...prev, isLoading: true }));
    
    try {
      // 개인 메모 로드
      const personalNote = await getUserNote(consultationId, currentUserId, 'PERSONAL');
      
      // 공유 메모 로드
      let sharedNote: ConsultationNote | null = null;
      if (currentUserRole === 'counselor') {
        // 상담사는 자신이 작성한 공유 메모를 조회
        sharedNote = await getUserNote(consultationId, currentUserId, 'SHARED');
      } else {
        // 고객은 상담사가 작성한 공유 메모를 조회
        sharedNote = await getSharedNote(consultationId);
      }

      setTabState(prev => ({
        ...prev,
        personalNote: personalNote?.content || '',
        sharedNote: sharedNote?.content || '',
        personalNoteId: personalNote?.noteId,
        sharedNoteId: sharedNote?.noteId,
        isLoading: false
      }));
    } catch (error) {
      console.error('메모 로드 실패:', error);
      setTabState(prev => ({ ...prev, isLoading: false }));
    }
  };

  // 메모 저장
  const saveNote = async (noteType: 'PERSONAL' | 'SHARED', content: string) => {
    // consultationId가 없으면 저장하지 않음
    if (!consultationId || consultationId.trim() === '') {
      console.warn('consultationId가 없어 메모를 저장할 수 없습니다');
      return;
    }

    setTabState(prev => ({ ...prev, isSaving: true }));
    
    try {
      await saveConsultationNote({
        consultId: consultationId,
        userId: currentUserId,
        noteType,
        content
      });
      
      console.log(`${noteType === 'PERSONAL' ? '개인' : '공유'} 메모 저장 완료`);
    } catch (error) {
      console.error(`${noteType === 'PERSONAL' ? '개인' : '공유'} 메모 저장 실패:`, error);
    } finally {
      setTabState(prev => ({ ...prev, isSaving: false }));
    }
  };

  // 개인 메모 변경 핸들러
  const handlePersonalNoteChange = (value: string | undefined) => {
    const content = value || '';
    setTabState(prev => ({ ...prev, personalNote: content }));
  };

  // 공유 메모 변경 핸들러
  const handleSharedNoteChange = (value: string | undefined) => {
    const content = value || '';
    setTabState(prev => ({ ...prev, sharedNote: content }));
  };

  // 수동 저장 핸들러
  const handleSavePersonal = () => {
    saveNote('PERSONAL', tabState.personalNote);
  };

  const handleSaveShared = async () => {
    await saveNote('SHARED', tabState.sharedNote);
    
    // 공유 메모 저장 후 WebSocket으로 동기화 (상담사만)
    if (currentUserRole === 'counselor' && targetUserId) {
      WebSocketService.sendConsultationNoteSync({
        type: 'CONSULTATION_NOTE_SYNC',
        roomId: consultationId,
        senderId: currentUserId,
        receiverId: targetUserId,
        data: { noteType: 'SHARED', content: tabState.sharedNote }
      });
    }
  };

  if (tabState.isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-blue-600"></div>
      </div>
    );
  }

  return (
    <div className="bg-white rounded-lg shadow-sm border">
      {/* 탭 헤더 */}
      <div className="border-b border-gray-200">
        <nav className="flex space-x-8 px-6">
          <button
            onClick={() => setActiveTab('personal')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'personal'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            📝 개인 메모장
          </button>
          <button
            onClick={() => setActiveTab('shared')}
            className={`py-4 px-1 border-b-2 font-medium text-sm ${
              activeTab === 'shared'
                ? 'border-blue-500 text-blue-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`}
          >
            🤝 공유 메모장
          </button>
        </nav>
      </div>

      {/* 탭 내용 */}
      <div className="p-6">
        {activeTab === 'personal' && (
          <div>
            <div className="mb-4">
              <h3 className="text-lg font-medium text-gray-900 mb-2">개인 메모장</h3>
              <p className="text-sm text-gray-600">
                개인적으로 작성하는 메모입니다. 상담사와 공유되지 않습니다.
              </p>
            </div>
            
            <div className="border rounded-lg overflow-hidden">
              <MDEditor
                value={tabState.personalNote}
                onChange={handlePersonalNoteChange}
                height={400}
                preview="edit"
                data-color-mode="light"
              />
            </div>
            
            <div className="flex justify-end mt-3">
              <button
                onClick={handleSavePersonal}
                disabled={tabState.isSaving}
                className="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white rounded-lg font-medium transition-colors"
              >
                {tabState.isSaving ? '저장 중...' : '저장'}
              </button>
            </div>
          </div>
        )}

        {activeTab === 'shared' && (
          <div>
            <div className="mb-4">
              <h3 className="text-lg font-medium text-gray-900 mb-2">공유 메모장</h3>
              <p className="text-sm text-gray-600">
                {currentUserRole === 'counselor' 
                  ? '고객과 공유되는 메모입니다. 상담 내용이나 추천 사항을 작성해주세요.'
                  : '상담사가 작성한 공유 메모입니다. 상담 내용과 추천 사항을 확인할 수 있습니다.'
                }
              </p>
            </div>
            
            <div className="border rounded-lg overflow-hidden">
              {currentUserRole === 'counselor' ? (
                <MDEditor
                  value={tabState.sharedNote}
                  onChange={handleSharedNoteChange}
                  height={400}
                  preview="edit"
                  data-color-mode="light"
                />
              ) : (
                <div className="p-4 min-h-[400px] bg-gray-50">
                  {tabState.sharedNote ? (
                    <div className="prose max-w-none">
                      <ReactMarkdown>
                        {tabState.sharedNote}
                      </ReactMarkdown>
                    </div>
                  ) : (
                    <div className="flex items-center justify-center h-full text-gray-500">
                      <p>상담사가 작성한 공유 메모가 없습니다.</p>
                    </div>
                  )}
                </div>
              )}
            </div>
            
            {currentUserRole === 'counselor' && (
              <div className="flex justify-end mt-3">
                <button
                  onClick={handleSaveShared}
                  disabled={tabState.isSaving}
                  className="px-4 py-2 bg-blue-600 hover:bg-blue-700 disabled:bg-gray-300 text-white rounded-lg font-medium transition-colors"
                >
                  {tabState.isSaving ? '저장 중...' : '저장'}
                </button>
              </div>
            )}
          </div>
        )}
      </div>
    </div>
  );
};

export default NotesTab;
