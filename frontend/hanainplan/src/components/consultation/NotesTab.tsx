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
  targetUserId?: number;
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

  useEffect(() => {
    loadNotes();
  }, [consultationId, currentUserId]);

  useEffect(() => {
    if (currentUserRole === 'customer') {
      WebSocketService.onConsultationNoteSync(async (message) => {
        if (message.data && message.data.noteType === 'SHARED') {

          try {
            const sharedNote = await getSharedNote(consultationId);
            setTabState(prev => ({
              ...prev,
              sharedNote: sharedNote?.content || ''
            }));
          } catch (error) {
          }
        }
      });
    }

    return () => {
    };
  }, [currentUserRole, consultationId]);

  const loadNotes = async () => {
    if (!consultationId || consultationId.trim() === '') {
      setTabState(prev => ({ ...prev, isLoading: false }));
      return;
    }

    setTabState(prev => ({ ...prev, isLoading: true }));

    try {
      const personalNote = await getUserNote(consultationId, currentUserId, 'PERSONAL');

      let sharedNote: ConsultationNote | null = null;
      if (currentUserRole === 'counselor') {
        sharedNote = await getUserNote(consultationId, currentUserId, 'SHARED');
      } else {
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
      setTabState(prev => ({ ...prev, isLoading: false }));
    }
  };

  const saveNote = async (noteType: 'PERSONAL' | 'SHARED', content: string) => {
    if (!consultationId || consultationId.trim() === '') {
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

    } catch (error) {
    } finally {
      setTabState(prev => ({ ...prev, isSaving: false }));
    }
  };

  const handlePersonalNoteChange = (value: string | undefined) => {
    const content = value || '';
    setTabState(prev => ({ ...prev, personalNote: content }));
  };

  const handleSharedNoteChange = (value: string | undefined) => {
    const content = value || '';
    setTabState(prev => ({ ...prev, sharedNote: content }));
  };

  const handleSavePersonal = () => {
    saveNote('PERSONAL', tabState.personalNote);
  };

  const handleSaveShared = async () => {
    await saveNote('SHARED', tabState.sharedNote);

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
      {}
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

      {}
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