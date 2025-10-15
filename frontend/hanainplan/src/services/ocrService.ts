
const OCR_API_BASE_URL = 'http://localhost:8090/api/ocr';

export interface ExtractedDocumentInfo {
  document_type: string;
  name?: string;
  social_number_front?: string;
  gender?: 'M' | 'F';
  employee_id?: string;
  branch_code?: string;
  branch_name?: string;
  department?: string;
  position?: string;
  license_type?: string;
  license_number?: string;
  license_issue_date?: string;
  hire_date?: string;
  phone_number?: string;
  address?: string;
  issue_date?: string;
  raw_text: string;
}

export interface ExtractResponse {
  success: boolean;
  masked_text: string;
  masked_pdf_base64: string;
  masked_images: string[];
  extracted_info: ExtractedDocumentInfo[];
  pages: number;
  message?: string;
}

export interface MergedCounselorInfo {
  name?: string;
  social_number_front?: string;
  gender?: string;
  employee_id?: string;
  branch_name?: string;
  department?: string;
  position?: string;
  license_type?: string;
  license_number?: string;
  hire_date?: string;
}

export interface VerifyDocumentsResponse {
  success: boolean;
  merged_info: MergedCounselorInfo;
  individual_documents: Record<string, ExtractedDocumentInfo>;
  message?: string;
}

export interface CounselorRegistrationData {
  name: string;
  social_number: string;
  gender: string;
  phone_number: string;
  employee_id: string;
  branch_code?: string;
  branch_name?: string;
  department?: string;
  position?: string;
  license_type?: string;
  license_number?: string;
  license_issue_date?: string;
  hire_date?: string;
}

export interface RegisterResponse {
  success: boolean;
  user_id?: number;
  message: string;
}

export const extractDocumentInfo = async (file: File): Promise<ExtractResponse> => {
  const formData = new FormData();
  formData.append('file', file);

  const response = await fetch(`${OCR_API_BASE_URL}/extract`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error('문서 처리 중 오류가 발생했습니다.');
  }

  return await response.json();
};

export const verifyAllDocuments = async (documents: {
  employeeId: File;
  employmentContract: File;
  identityVerification: File;
  qualificationCert?: File;
}): Promise<VerifyDocumentsResponse> => {
  const formData = new FormData();
  formData.append('employee_id_doc', documents.employeeId);
  formData.append('employment_contract', documents.employmentContract);
  formData.append('identity_doc', documents.identityVerification);

  if (documents.qualificationCert) {
    formData.append('qualification_cert', documents.qualificationCert);
  }

  const response = await fetch(`${OCR_API_BASE_URL}/verify-documents`, {
    method: 'POST',
    body: formData,
  });

  if (!response.ok) {
    throw new Error('문서 검증 중 오류가 발생했습니다.');
  }

  return await response.json();
};

export const registerCounselor = async (
  data: CounselorRegistrationData
): Promise<RegisterResponse> => {
  const response = await fetch(`${OCR_API_BASE_URL}/register-counselor`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
    },
    body: JSON.stringify(data),
  });

  const result = await response.json();

  if (!response.ok) {
    throw new Error(result.message || '상담사 등록 중 오류가 발생했습니다.');
  }

  return result;
};

export const downloadMaskedPDF = (base64PDF: string, filename: string = 'masked_document.pdf') => {
  const byteCharacters = atob(base64PDF);
  const byteNumbers = new Array(byteCharacters.length);

  for (let i = 0; i < byteCharacters.length; i++) {
    byteNumbers[i] = byteCharacters.charCodeAt(i);
  }

  const byteArray = new Uint8Array(byteNumbers);
  const blob = new Blob([byteArray], { type: 'application/pdf' });

  const link = document.createElement('a');
  link.href = window.URL.createObjectURL(blob);
  link.download = filename;
  link.click();

  window.URL.revokeObjectURL(link.href);
};