import { useState, useRef } from 'react'

interface VerificationInfo {
  employeeId: string;
  verificationDocuments: File[];
  additionalNotes: string;
}

interface Step5CounselorVerificationProps {
  verificationInfo: VerificationInfo;
  onVerificationInfoChange: (verificationInfo: VerificationInfo) => void;
}

const REQUIRED_DOCUMENTS = [
  { id: 'employee_id', name: '직원증', description: '하나은행 직원증 사본' },
  { id: 'employment_contract', name: '근로계약서', description: '근로계약서 또는 재직증명서' },
  { id: 'identity_verification', name: '신분증명서', description: '주민등록증 또는 운전면허증' },
  { id: 'qualification_cert', name: '자격증명서', description: '금융상품 관련 자격증 (선택)' }
]

function Step5CounselorVerification({ verificationInfo, onVerificationInfoChange }: Step5CounselorVerificationProps) {
  const [errors, setErrors] = useState<Partial<VerificationInfo>>({})
  const [uploadedDocs, setUploadedDocs] = useState<{[key: string]: File}>({})
  const fileInputRefs = useRef<{[key: string]: HTMLInputElement | null}>({})

  const handleInputChange = (field: keyof VerificationInfo, value: string | File[]) => {
    onVerificationInfoChange({
      ...verificationInfo,
      [field]: value
    })
    
    // 에러 메시지 초기화
    if (errors[field]) {
      setErrors(prev => ({ ...prev, [field]: undefined }))
    }
  }

  const handleFileUpload = (docId: string, file: File) => {
    // 파일 크기 검증 (10MB 제한)
    if (file.size > 10 * 1024 * 1024) {
      alert('파일 크기는 10MB를 초과할 수 없습니다.')
      return
    }

    // PDF 파일만 허용
    if (file.type !== 'application/pdf') {
      alert('PDF 파일만 업로드 가능합니다.')
      return
    }

    const newUploadedDocs = { ...uploadedDocs, [docId]: file }
    setUploadedDocs(newUploadedDocs)

    // verificationDocuments 배열 업데이트
    const allFiles = Object.values(newUploadedDocs)
    handleInputChange('verificationDocuments', allFiles)
  }

  const handleFileRemove = (docId: string) => {
    const newUploadedDocs = { ...uploadedDocs }
    delete newUploadedDocs[docId]
    setUploadedDocs(newUploadedDocs)

    // verificationDocuments 배열 업데이트
    const allFiles = Object.values(newUploadedDocs)
    handleInputChange('verificationDocuments', allFiles)
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
          검증 정보 및 문서
        </h2>
        <p className="text-gray-600 font-['Hana2.0_M'] text-sm">
          상담사 신원 확인을 위한 정보와 문서를 제출해주세요.
        </p>
      </div>

      <div className="space-y-8">
        {/* 직원번호 입력 */}
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

        {/* 검증 문서 업로드 */}
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
                      {doc.name}
                    </h3>
                    <p className="text-xs text-gray-500 font-['Hana2.0_M']">
                      {doc.description}
                    </p>
                  </div>
                  {doc.id === 'qualification_cert' && (
                    <span className="text-xs text-gray-400 font-['Hana2.0_M']">
                      선택
                    </span>
                  )}
                </div>

                {uploadedDocs[doc.id] ? (
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
                    >
                      <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                      </svg>
                    </button>
                  </div>
                ) : (
                  <div className="border-2 border-dashed border-gray-300 rounded-lg p-4 text-center hover:border-[#008485] transition-colors">
                    <input
                      ref={(el) => fileInputRefs.current[doc.id] = el}
                      type="file"
                      accept=".pdf"
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
                      <p>PDF 파일 업로드</p>
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
        </div>

        {/* 추가 메모 */}
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

      {/* 안내 메시지 */}
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
                  <li>PDF 파일만 업로드 가능합니다.</li>
                  <li>파일 크기는 10MB를 초과할 수 없습니다.</li>
                  <li>문서는 명확하게 읽을 수 있도록 스캔해주세요.</li>
                  <li>개인정보 보호를 위해 민감한 정보는 마스킹 처리해주세요.</li>
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
                제출된 문서는 상담사 신원 확인 및 자격 검증 목적으로만 사용되며, 
                보안이 유지됩니다. 검증 완료까지 1-2 영업일이 소요될 수 있습니다.
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}

export default Step5CounselorVerification

