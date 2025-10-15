import { useState, useRef } from 'react'
import { extractDocumentInfo, verifyAllDocuments } from '../../services/ocrService'
import type { ExtractedDocumentInfo, MergedCounselorInfo } from '../../services/ocrService'

interface VerificationInfo {
  employeeId: string;
  verificationDocuments: File[];
  additionalNotes: string;
  extractedInfo?: MergedCounselorInfo;
}

interface Step5CounselorVerificationProps {
  verificationInfo: VerificationInfo;
  onVerificationInfoChange: (verificationInfo: VerificationInfo) => void;
}

interface ValidationErrors {
  employeeId?: string;
  verificationDocuments?: string;
  additionalNotes?: string;
  extractedInfo?: string;
}

const REQUIRED_DOCUMENTS = [
  { id: 'employment_contract', name: '재직증명서', description: '하나은행 재직증명서' },
  { id: 'identity_verification', name: '신분증', description: '주민등록증 또는 운전면허증' }
]

function Step5CounselorVerification({ verificationInfo, onVerificationInfoChange }: Step5CounselorVerificationProps) {
  const [errors, setErrors] = useState<ValidationErrors>({})
  const [uploadedDocs, setUploadedDocs] = useState<{[key: string]: File}>({})
  const [extractedInfos, setExtractedInfos] = useState<{[key: string]: ExtractedDocumentInfo}>({})
  const [maskedImages, setMaskedImages] = useState<{[key: string]: string[]}>({})
  const [isProcessing, setIsProcessing] = useState(false)
  const [processingDoc, setProcessingDoc] = useState<string | null>(null)
  const [showModal, setShowModal] = useState(false)
  const [currentModalImage, setCurrentModalImage] = useState<string | null>(null)
  const [isEditMode, setIsEditMode] = useState(false)
  const [editedInfos, setEditedInfos] = useState<{[key: string]: ExtractedDocumentInfo}>({})
  const fileInputRefs = useRef<{[key: string]: HTMLInputElement | null}>({})

  const handleInputChange = (field: keyof VerificationInfo, value: string | File[]) => {
    onVerificationInfoChange({
      ...verificationInfo,
      [field]: value
    })

    if (field in errors && errors[field as keyof ValidationErrors]) {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }

  const handleFileUpload = async (docId: string, file: File) => {
    if (file.size > 10 * 1024 * 1024) {
      alert('파일 크기는 10MB를 초과할 수 없습니다.')
      return
    }

    const allowedTypes = ['application/pdf', 'image/jpeg', 'image/jpg', 'image/png', 'image/webp']
    if (!allowedTypes.includes(file.type)) {
      alert('PDF 또는 이미지 파일(JPG, PNG)만 업로드 가능합니다.')
      return
    }

    const newUploadedDocs = { ...uploadedDocs, [docId]: file }
    setUploadedDocs(newUploadedDocs)

    const allFiles = Object.values(newUploadedDocs)
    handleInputChange('verificationDocuments', allFiles)

    setIsProcessing(true)
    setProcessingDoc(docId)

    try {
      const result = await extractDocumentInfo(file)

      if (result.success && result.extracted_info.length > 0) {
        const info = result.extracted_info[0]

        setExtractedInfos(prev => {
          const newInfos = { ...prev, [docId]: info }
          setEditedInfos(newInfos)
          return newInfos
        })

        if (result.masked_images && result.masked_images.length > 0) {
          setMaskedImages(prev => ({ ...prev, [docId]: result.masked_images }))
        }

        if (result.masked_images && result.masked_images.length > 0) {
          setCurrentModalImage(result.masked_images[0])
          setIsEditMode(false)
          setShowModal(true)
        } else {
          alert(`✅ 문서 분석 완료!\n\n추출된 정보:\n- 이름: ${info.name || 'N/A'}\n- 지점: ${info.branch_name || 'N/A'}\n- 직급: ${info.position || 'N/A'}`)
        }
      }
    } catch (error) {
      alert('문서 분석 중 오류가 발생했습니다. 수동으로 정보를 입력해주세요.')
    } finally {
      setIsProcessing(false)
      setProcessingDoc(null)
    }
  }

  const handleFileRemove = (docId: string) => {
    const newUploadedDocs = { ...uploadedDocs }
    delete newUploadedDocs[docId]
    setUploadedDocs(newUploadedDocs)

    const newExtractedInfos = { ...extractedInfos }
    delete newExtractedInfos[docId]
    setExtractedInfos(newExtractedInfos)

    const newMaskedImages = { ...maskedImages }
    delete newMaskedImages[docId]
    setMaskedImages(newMaskedImages)

    const allFiles = Object.values(newUploadedDocs)
    handleInputChange('verificationDocuments', allFiles)
  }

  const handleViewMaskedImage = (docId: string) => {
    if (maskedImages[docId] && maskedImages[docId].length > 0) {
      setCurrentModalImage(maskedImages[docId][0])
      setIsEditMode(false)
      setEditedInfos({ ...extractedInfos })
      setShowModal(true)

    }
  }

  const toggleEditMode = () => {
    if (!isEditMode) {
      setEditedInfos({ ...extractedInfos })
    }
    setIsEditMode(!isEditMode)
  }

  const handleEditFieldChange = (docId: string, field: keyof ExtractedDocumentInfo, value: string) => {
    setEditedInfos(prev => ({
      ...prev,
      [docId]: {
        ...prev[docId],
        [field]: value
      }
    }))
  }

  const handleSaveEdit = () => {
    setExtractedInfos({ ...editedInfos })
    setIsEditMode(false)
    alert('수정사항이 저장되었습니다.')
  }

  const handleCancelEdit = () => {
    setEditedInfos({ ...extractedInfos })
    setIsEditMode(false)
  }

  const handleVerifyAllDocuments = async () => {
    if (Object.keys(uploadedDocs).length < 2) {
      alert('재직증명서와 신분증을 모두 업로드해주세요.')
      return
    }

    if (!uploadedDocs['employment_contract']) {
      alert('재직증명서를 업로드해주세요.')
      return
    }

    if (!uploadedDocs['identity_verification']) {
      alert('신분증을 업로드해주세요.')
      return
    }

    setIsProcessing(true)

    try {
      const result = await verifyAllDocuments({
        employeeId: uploadedDocs['employment_contract'],
        employmentContract: uploadedDocs['employment_contract'],
        identityVerification: uploadedDocs['identity_verification'],
        qualificationCert: undefined
      })

      if (result.success) {
        const merged = result.merged_info

        onVerificationInfoChange({
          ...verificationInfo,
          extractedInfo: merged
        })

        if (merged.employee_id) {
          handleInputChange('employeeId', merged.employee_id)
        }

        alert(`✅ 전체 문서 검증 완료!\n\n추출된 정보:\n- 이름: ${merged.name || 'N/A'}\n- 성별: ${merged.gender || 'N/A'}\n- 주민번호 앞자리: ${merged.social_number_front || 'N/A'}\n- 직원번호: ${merged.employee_id || 'N/A'}\n- 지점: ${merged.branch_name || 'N/A'}\n- 부서: ${merged.department || 'N/A'}\n- 직급: ${merged.position || 'N/A'}`)
      }
    } catch (error) {
      alert('문서 검증 중 오류가 발생했습니다.')
    } finally {
      setIsProcessing(false)
    }
  }

  const validateField = (field: keyof VerificationInfo, value: any) => {
    let error = ''

    switch (field) {
      case 'employeeId':
        if (!value || !value.trim()) {
          error = '직원번호를 입력해주세요.'
        } else if (!/^[A-Z0-9]{6,12}$/.test(value.trim())) {
          error = '올바른 직원번호 형식을 입력해주세요. (6-12자 영문, 숫자)'
        }
        break
      case 'verificationDocuments':
        if (!value || value.length === 0) {
          error = '최소 1개 이상의 검증 문서를 업로드해주세요.'
        }
        break
    }

    setErrors(prev => ({ ...prev, [field]: error }))
    return !error
  }

  const handleBlur = (field: keyof VerificationInfo) => {
    validateField(field, verificationInfo[field])
  }

  const formatFileSize = (bytes: number) => {
    if (bytes === 0) return '0 Bytes'
    const k = 1024
    const sizes = ['Bytes', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  return (
    <div className="w-full max-w-4xl mx-auto p-6">
      <div className="mb-8">
        <h2 className="text-2xl font-['Hana2.0_M'] text-gray-800 mb-2">
          직원 증빙서류 제출
        </h2>
      </div>

      <div className="space-y-8">
        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            직원번호 <span className="text-red-500">*</span>
          </label>
          <input
            type="text"
            value={verificationInfo.employeeId}
            onChange={(e) => handleInputChange('employeeId', e.target.value)}
            onBlur={() => handleBlur('employeeId')}
            placeholder="예: HANA001234"
            className={`w-full px-4 py-3 border rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485] ${
              errors.employeeId ? 'border-red-500' : 'border-gray-300'
            }`}
          />
          {errors.employeeId && (
            <p className="mt-1 text-sm text-red-500 font-['Hana2.0_M']">
              {errors.employeeId}
            </p>
          )}
          <p className="mt-1 text-xs text-gray-500 font-['Hana2.0_M']">
            6-12자 영문, 숫자 조합으로 입력해주세요.
          </p>
        </div>

        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-4">
            검증 문서 <span className="text-red-500">*</span>
          </label>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            {REQUIRED_DOCUMENTS.map((doc) => (
              <div key={doc.id} className="border border-gray-200 rounded-lg p-4">
                <div className="flex items-start justify-between mb-2">
                  <div>
                    <h3 className="font-['Hana2.0_M'] text-sm font-medium text-gray-900">
                      {doc.name} <span className="text-red-500">*</span>
                    </h3>
                    {doc.description && (
                      <p className="text-xs text-gray-500 font-['Hana2.0_M']">
                        {doc.description}
                      </p>
                    )}
                  </div>
                </div>

                {uploadedDocs[doc.id] ? (
                  <div className="space-y-2">
                    <div className="flex items-center justify-between p-2 bg-green-50 rounded border border-green-200">
                      <div className="flex items-center space-x-2">
                        <svg className="w-4 h-4 text-green-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                        </svg>
                        <span className="text-sm text-green-700 font-['Hana2.0_M']">
                          {uploadedDocs[doc.id].name}
                        </span>
                        <span className="text-xs text-green-600 font-['Hana2.0_M']">
                          ({formatFileSize(uploadedDocs[doc.id].size)})
                        </span>
                      </div>
                      <button
                        type="button"
                        onClick={() => handleFileRemove(doc.id)}
                        className="text-red-500 hover:text-red-700"
                        disabled={isProcessing}
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                    </div>

                    {}
                    {isProcessing && processingDoc === doc.id && (
                      <div className="flex items-center space-x-2 p-2 bg-blue-50 rounded">
                        <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-500"></div>
                        <span className="text-xs text-blue-700 font-['Hana2.0_M']">
                          문서 분석 중...
                        </span>
                      </div>
                    )}

                    {}
                    {extractedInfos[doc.id] && (
                      <div className="space-y-2">
                        <div className="p-2 bg-blue-50 rounded border border-blue-200">
                          <div className="text-xs text-blue-700 font-['Hana2.0_M'] space-y-1">
                            <p className="font-semibold">✓ 분석 완료</p>
                            {extractedInfos[doc.id].name && <p>• 이름: {extractedInfos[doc.id].name}</p>}
                            {extractedInfos[doc.id].branch_name && <p>• 지점: {extractedInfos[doc.id].branch_name}</p>}
                            {extractedInfos[doc.id].position && <p>• 직급: {extractedInfos[doc.id].position}</p>}
                          </div>
                        </div>

                        {}
                        {maskedImages[doc.id] && (
                          <button
                            type="button"
                            onClick={() => handleViewMaskedImage(doc.id)}
                            className="w-full px-3 py-2 bg-indigo-50 text-indigo-700 rounded border border-indigo-200 hover:bg-indigo-100 text-xs font-['Hana2.0_M'] flex items-center justify-center space-x-1"
                          >
                            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M15 12a3 3 0 11-6 0 3 3 0 016 0z" />
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z" />
                            </svg>
                            <span>마스킹된 문서 보기</span>
                          </button>
                        )}
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-[#008485] transition-colors">
                    <input
                      ref={(el) => {
                        fileInputRefs.current[doc.id] = el;
                        return;
                      }}
                      type="file"
                      accept=".pdf,.jpg,.jpeg,.png"
                      onChange={(e) => {
                        const file = e.target.files?.[0]
                        if (file) handleFileUpload(doc.id, file)
                      }}
                      className="hidden"
                    />
                    <button
                      type="button"
                      onClick={() => fileInputRefs.current[doc.id]?.click()}
                      className="text-sm text-[#008485] hover:text-[#006666] font-['Hana2.0_M']"
                    >
                      <svg className="w-8 h-8 mx-auto mb-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M7 16a4 4 0 01-.88-7.903A5 5 0 1115.9 6L16 6a5 5 0 011 9.9M15 13l-3-3m0 0l-3 3m3-3v12" />
                      </svg>
                      <p>PDF 또는 이미지 업로드</p>
                      <p className="text-xs text-gray-400 mt-1">(JPG, PNG, PDF)</p>
                    </button>
                  </div>
                )}
              </div>
            ))}
          </div>

          {errors.verificationDocuments && (
            <p className="mt-2 text-sm text-red-500 font-['Hana2.0_M']">
              {errors.verificationDocuments}
            </p>
          )}

          {}
          {Object.keys(uploadedDocs).length >= 3 && (
            <div className="mt-4 flex justify-center">
              <button
                type="button"
                onClick={handleVerifyAllDocuments}
                disabled={isProcessing}
                className="px-6 py-3 bg-[#008485] text-white rounded-lg font-['Hana2.0_M'] hover:bg-[#006666] disabled:opacity-50 disabled:cursor-not-allowed flex items-center space-x-2"
              >
                {isProcessing ? (
                  <>
                    <div className="animate-spin rounded-full h-5 w-5 border-b-2 border-white"></div>
                    <span>전체 문서 검증 중...</span>
                  </>
                ) : (
                  <>
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z" />
                    </svg>
                    <span>전체 문서 일괄 검증하기</span>
                  </>
                )}
              </button>
            </div>
          )}
        </div>

        {}
        <div>
          <label className="block text-sm font-['Hana2.0_M'] text-gray-700 mb-2">
            추가 정보 (선택)
          </label>
          <textarea
            value={verificationInfo.additionalNotes}
            onChange={(e) => handleInputChange('additionalNotes', e.target.value)}
            placeholder="기타 참고사항이나 특이사항이 있다면 입력해주세요..."
            rows={4}
            className="w-full px-4 py-3 border border-gray-300 rounded-lg font-['Hana2.0_M'] text-sm focus:outline-none focus:ring-2 focus:ring-[#008485] resize-none"
          />
        </div>
      </div>

      {}
      <div className="mt-8 space-y-4">
        <div className="p-4 bg-yellow-50 rounded-lg">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-yellow-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M8.257 3.099c.765-1.36 2.722-1.36 3.486 0l5.58 9.92c.75 1.334-.213 2.98-1.742 2.98H4.42c-1.53 0-2.493-1.646-1.743-2.98l5.58-9.92zM11 13a1 1 0 11-2 0 1 1 0 012 0zm-1-8a1 1 0 00-1 1v3a1 1 0 002 0V6a1 1 0 00-1-1z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <h3 className="text-sm font-medium text-yellow-800 font-['Hana2.0_M']">
                문서 업로드 안내
              </h3>
              <div className="mt-2 text-sm text-yellow-700 font-['Hana2.0_M']">
                <ul className="list-disc list-inside space-y-1">
                  <li>재직증명서와 주민등록증(또는 운전면허증) 2개 문서가 필수입니다.</li>
                  <li>PDF 또는 이미지 파일(JPG, PNG)을 업로드할 수 있습니다.</li>
                  <li>파일 크기는 10MB를 초과할 수 없습니다.</li>
                  <li>문서는 명확하게 읽을 수 있도록 스캔하거나 촬영해주세요.</li>
                  <li>민감 정보는 이미지에서 자동으로 블러 처리됩니다.</li>
                </ul>
              </div>
            </div>
          </div>
        </div>

        <div className="p-4 bg-blue-50 rounded-lg">
          <div className="flex items-start">
            <div className="flex-shrink-0">
              <svg className="h-5 w-5 text-blue-400" viewBox="0 0 20 20" fill="currentColor">
                <path fillRule="evenodd" d="M18 10a8 8 0 11-16 0 8 8 0 0116 0zm-7-4a1 1 0 11-2 0 1 1 0 012 0zM9 9a1 1 0 000 2v3a1 1 0 001 1h1a1 1 0 100-2v-3a1 1 0 00-1-1H9z" clipRule="evenodd" />
              </svg>
            </div>
            <div className="ml-3">
              <p className="text-sm text-blue-700 font-['Hana2.0_M']">
                제출된 문서는 상담사 신원 확인 목적으로만 사용되며 보안이 유지됩니다.
                OCR로 자동 추출된 정보는 수정 가능하며, 검증 완료까지 1-2 영업일이 소요될 수 있습니다.
              </p>
            </div>
          </div>
        </div>
      </div>

      {}
      {showModal && currentModalImage && (
        <div
          className="fixed inset-0 bg-black bg-opacity-50 z-50 flex items-center justify-center p-4"
          onClick={() => setShowModal(false)}
        >
          <div
            className="bg-white rounded-lg max-w-4xl max-h-[90vh] overflow-auto"
            onClick={(e) => e.stopPropagation()}
          >
            <div className="sticky top-0 bg-white border-b p-4 flex items-center justify-between">
              <h3 className="text-lg font-['Hana2.0_M'] text-gray-800">
                마스킹된 문서
              </h3>
              <div className="flex items-center gap-2">
                {isEditMode ? (
                  <>
                    <button
                      onClick={handleSaveEdit}
                      className="px-3 py-1 bg-hana-green text-white rounded-md text-sm font-['Hana2.0_M'] hover:bg-green-600"
                    >
                      저장
                    </button>
                    <button
                      onClick={handleCancelEdit}
                      className="px-3 py-1 bg-gray-200 text-gray-700 rounded-md text-sm font-['Hana2.0_M'] hover:bg-gray-300"
                    >
                      취소
                    </button>
                  </>
                ) : (
                  <button
                    onClick={toggleEditMode}
                    className="px-3 py-1 bg-blue-500 text-white rounded-md text-sm font-['Hana2.0_M'] hover:bg-blue-600"
                  >
                    ✏️ 수정
                  </button>
                )}
                <button
                  onClick={() => setShowModal(false)}
                  className="text-gray-400 hover:text-gray-600"
                >
                  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                  </svg>
                </button>
              </div>
            </div>

            <div className="p-4">
              <img
                src={`data:image/png;base64,${currentModalImage}`}
                alt="마스킹된 문서"
                className="w-full h-auto"
              />

              {}
              {Object.entries(isEditMode ? editedInfos : extractedInfos).map(([docId, info]) =>
                maskedImages[docId]?.[0] === currentModalImage && (
                  <div key={docId} className="mt-4 space-y-3">
                    {}
                    <div className="p-4 bg-blue-50 rounded-lg border border-blue-200">
                      <h4 className="text-sm font-semibold text-blue-900 font-['Hana2.0_M'] mb-3">
                        📋 추출된 정보 {isEditMode && <span className="text-xs text-blue-600">(수정 모드)</span>}
                      </h4>
                      <div className="grid grid-cols-2 gap-3 text-sm font-['Hana2.0_M']">
                        {}
                        <div className="flex flex-col space-y-1">
                          <label className="text-blue-700">이름:</label>
                          {isEditMode ? (
                            <input
                              type="text"
                              value={info.name || ''}
                              onChange={(e) => handleEditFieldChange(docId, 'name', e.target.value)}
                              className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                            />
                          ) : (
                            <span className="text-blue-900 font-medium">{info.name || '-'}</span>
                          )}
                        </div>

                        {}
                        {docId === 'employment_contract' && (
                          <>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">지점:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.branch_name || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'branch_name', e.target.value)}
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.branch_name || '-'}</span>
                              )}
                            </div>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">부서:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.department || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'department', e.target.value)}
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.department || '-'}</span>
                              )}
                            </div>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">직급:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.position || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'position', e.target.value)}
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.position || '-'}</span>
                              )}
                            </div>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">입사일:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.hire_date || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'hire_date', e.target.value)}
                                  placeholder="2021년 3월 22일"
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.hire_date || '-'}</span>
                              )}
                            </div>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">발급일:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.issue_date || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'issue_date', e.target.value)}
                                  placeholder="2025년 10월 4일"
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.issue_date || '-'}</span>
                              )}
                            </div>
                          </>
                        )}

                        {}
                        {docId === 'identity_verification' && (
                          <>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">주민등록번호:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.social_number_front || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'social_number_front', e.target.value)}
                                  placeholder="900101-1"
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">
                                  {info.social_number_front ? `${info.social_number_front}******` : '-'}
                                </span>
                              )}
                            </div>
                            <div className="flex flex-col space-y-1 col-span-2">
                              <label className="text-blue-700">주소:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.address || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'address', e.target.value)}
                                  placeholder="서울특별시 중구 을지로 35"
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.address || '-'}</span>
                              )}
                            </div>
                            <div className="flex flex-col space-y-1">
                              <label className="text-blue-700">발급일:</label>
                              {isEditMode ? (
                                <input
                                  type="text"
                                  value={info.issue_date || ''}
                                  onChange={(e) => handleEditFieldChange(docId, 'issue_date', e.target.value)}
                                  placeholder="2025년 10월 4일"
                                  className="px-2 py-1 border border-blue-300 rounded-md text-blue-900 font-medium focus:outline-none focus:ring-2 focus:ring-blue-500"
                                />
                              ) : (
                                <span className="text-blue-900 font-medium">{info.issue_date || '-'}</span>
                              )}
                            </div>
                          </>
                        )}
                      </div>
                    </div>

                    {}
                    <div className="p-3 bg-yellow-50 rounded-lg">
                      <p className="text-sm text-yellow-800 font-['Hana2.0_M']">
                        {docId === 'identity_verification'
                          ? 'ℹ️ 이미지에서 주민번호 뒷자리와 주소 상세 정보가 블러 처리되었습니다.'
                          : 'ℹ️ 민감한 정보는 이미지에서 자동으로 블러 처리되었습니다.'}
                      </p>
                    </div>
                  </div>
                )
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default Step5CounselorVerification