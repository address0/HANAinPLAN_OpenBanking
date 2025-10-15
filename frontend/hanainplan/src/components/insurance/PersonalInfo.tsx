import React, { useState } from 'react';
import { validatePersonalInfo, checkResidentNumberDuplicate } from '../../api/insuranceApi';
import type { PersonalInfo } from '../../types/insurance';

interface PersonalInfoProps {
  onNext: (personalInfo: PersonalInfo) => void;
  onPrevious: () => void;
}

const PersonalInfo: React.FC<PersonalInfoProps> = ({ onNext, onPrevious }) => {
  const [formData, setFormData] = useState<PersonalInfo>({
    name: '',
    residentNumber: '',
    gender: 'M',
    birthDate: '',
    phoneNumber: '',
    email: '',
    address: {
      zipCode: '',
      address1: '',
      address2: ''
    },
    occupation: '',
    maritalStatus: 'SINGLE'
  });

  const [errors, setErrors] = useState<Record<string, string>>({});
  const [isValidating, setIsValidating] = useState(false);
  const [isDuplicateChecking, setIsDuplicateChecking] = useState(false);

  const occupations = [
    '사무직', '영업직', '기술직', '서비스업', '자영업', '공무원', '교육직', '의료직',
    '법무직', '금융업', 'IT업', '건설업', '제조업', '유통업', '농업', '어업', '기타'
  ];

  const maritalStatusOptions = [
    { value: 'SINGLE', label: '미혼' },
    { value: 'MARRIED', label: '기혼' },
    { value: 'DIVORCED', label: '이혼' },
    { value: 'WIDOWED', label: '사별' }
  ];

  const handleInputChange = (field: string, value: any) => {
    setFormData(prev => {
      if (field.includes('.')) {
        const [parent, child] = field.split('.');
        return {
          ...prev,
          [parent]: {
            ...prev[parent as keyof PersonalInfo] as any,
            [child]: value
          }
        };
      }
      return {
        ...prev,
        [field]: value
      };
    });

    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  const validateForm = async (): Promise<boolean> => {
    setIsValidating(true);
    const newErrors: Record<string, string> = {};

    if (!formData.name.trim()) {
      newErrors.name = '이름을 입력해주세요.';
    }

    if (!formData.residentNumber.trim()) {
      newErrors.residentNumber = '주민등록번호를 입력해주세요.';
    } else if (!/^\d{6}-\d{7}$/.test(formData.residentNumber)) {
      newErrors.residentNumber = '주민등록번호 형식이 올바르지 않습니다.';
    }

    if (!formData.birthDate) {
      newErrors.birthDate = '생년월일을 선택해주세요.';
    }

    if (!formData.phoneNumber.trim()) {
      newErrors.phoneNumber = '휴대폰 번호를 입력해주세요.';
    } else if (!/^010-\d{4}-\d{4}$/.test(formData.phoneNumber)) {
      newErrors.phoneNumber = '휴대폰 번호 형식이 올바르지 않습니다.';
    }

    if (!formData.email.trim()) {
      newErrors.email = '이메일을 입력해주세요.';
    } else if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(formData.email)) {
      newErrors.email = '이메일 형식이 올바르지 않습니다.';
    }

    if (!formData.address.zipCode.trim()) {
      newErrors.zipCode = '우편번호를 입력해주세요.';
    }

    if (!formData.address.address1.trim()) {
      newErrors.address1 = '주소를 입력해주세요.';
    }

    if (!formData.occupation.trim()) {
      newErrors.occupation = '직업을 선택해주세요.';
    }

    setErrors(newErrors);

    if (Object.keys(newErrors).length > 0) {
      setIsValidating(false);
      return false;
    }

    try {
      const validationResult = await validatePersonalInfo(formData);
      if (!validationResult.valid) {
        validationResult.errors.forEach(error => {
          if (error.includes('주민등록번호')) {
            newErrors.residentNumber = error;
          } else if (error.includes('이름')) {
            newErrors.name = error;
          } else {
            newErrors.general = error;
          }
        });
        setErrors(newErrors);
        setIsValidating(false);
        return false;
      }
    } catch (error) {
      newErrors.general = '개인정보 검증 중 오류가 발생했습니다.';
      setErrors(newErrors);
      setIsValidating(false);
      return false;
    }

    setIsValidating(false);
    return true;
  };

  const checkDuplicateResidentNumber = async () => {
    if (!formData.residentNumber || !/^\d{6}-\d{7}$/.test(formData.residentNumber)) {
      return;
    }

    setIsDuplicateChecking(true);
    try {
      const result = await checkResidentNumberDuplicate(formData.residentNumber);
      if (result.duplicate) {
        setErrors(prev => ({
          ...prev,
          residentNumber: '이미 가입된 주민등록번호입니다.'
        }));
      }
    } catch (error) {
    } finally {
      setIsDuplicateChecking(false);
    }
  };

  const handleNext = async () => {
    const isValid = await validateForm();
    if (isValid) {
      onNext(formData);
    }
  };

  const formatPhoneNumber = (value: string) => {
    const numbers = value.replace(/\D/g, '');
    if (numbers.length <= 3) return numbers;
    if (numbers.length <= 7) return `${numbers.slice(0, 3)}-${numbers.slice(3)}`;
    return `${numbers.slice(0, 3)}-${numbers.slice(3, 7)}-${numbers.slice(7, 11)}`;
  };

  const formatResidentNumber = (value: string) => {
    const numbers = value.replace(/\D/g, '');
    if (numbers.length <= 6) return numbers;
    return `${numbers.slice(0, 6)}-${numbers.slice(6, 13)}`;
  };

  return (
    <div className="space-y-4">
      {}
      <div className="text-center">
        <h1 className="text-2xl font-bold text-gray-800 mb-2">개인정보 입력</h1>
        <p className="text-gray-600">보험 가입에 필요한 개인정보를 입력해주세요</p>
      </div>

      {}
      {errors.general && (
        <div className="bg-red-50 border border-red-200 rounded-lg p-3">
          <p className="text-red-600 text-sm">{errors.general}</p>
        </div>
      )}

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">기본 정보</h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이름 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.name}
              onChange={(e) => handleInputChange('name', e.target.value)}
              className={`w-full p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                errors.name ? 'border-red-300' : 'border-gray-300'
              }`}
              placeholder="이름을 입력하세요"
            />
            {errors.name && <p className="text-red-500 text-xs mt-1">{errors.name}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              주민등록번호 <span className="text-red-500">*</span>
            </label>
            <div className="flex space-x-2">
              <input
                type="text"
                value={formData.residentNumber}
                onChange={(e) => handleInputChange('residentNumber', formatResidentNumber(e.target.value))}
                onBlur={checkDuplicateResidentNumber}
                className={`flex-1 p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                  errors.residentNumber ? 'border-red-300' : 'border-gray-300'
                }`}
                placeholder="000000-0000000"
                maxLength={14}
              />
              {isDuplicateChecking && (
                <div className="flex items-center px-2">
                  <div className="animate-spin rounded-full h-4 w-4 border-b-2 border-blue-600"></div>
                </div>
              )}
            </div>
            {errors.residentNumber && <p className="text-red-500 text-xs mt-1">{errors.residentNumber}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              성별 <span className="text-red-500">*</span>
            </label>
            <div className="flex space-x-4">
              <label className="flex items-center">
                <input
                  type="radio"
                  name="gender"
                  value="M"
                  checked={formData.gender === 'M'}
                  onChange={(e) => handleInputChange('gender', e.target.value as 'M' | 'F')}
                  className="mr-1"
                />
                <span className="text-sm">남성</span>
              </label>
              <label className="flex items-center">
                <input
                  type="radio"
                  name="gender"
                  value="F"
                  checked={formData.gender === 'F'}
                  onChange={(e) => handleInputChange('gender', e.target.value as 'M' | 'F')}
                  className="mr-1"
                />
                <span className="text-sm">여성</span>
              </label>
            </div>
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              생년월일 <span className="text-red-500">*</span>
            </label>
            <input
              type="date"
              value={formData.birthDate}
              onChange={(e) => handleInputChange('birthDate', e.target.value)}
              className={`w-full p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                errors.birthDate ? 'border-red-300' : 'border-gray-300'
              }`}
            />
            {errors.birthDate && <p className="text-red-500 text-xs mt-1">{errors.birthDate}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              휴대폰 번호 <span className="text-red-500">*</span>
            </label>
            <input
              type="tel"
              value={formData.phoneNumber}
              onChange={(e) => handleInputChange('phoneNumber', formatPhoneNumber(e.target.value))}
              className={`w-full p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                errors.phoneNumber ? 'border-red-300' : 'border-gray-300'
              }`}
              placeholder="010-0000-0000"
            />
            {errors.phoneNumber && <p className="text-red-500 text-xs mt-1">{errors.phoneNumber}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              이메일 <span className="text-red-500">*</span>
            </label>
            <input
              type="email"
              value={formData.email}
              onChange={(e) => handleInputChange('email', e.target.value)}
              className={`w-full p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                errors.email ? 'border-red-300' : 'border-gray-300'
              }`}
              placeholder="example@email.com"
            />
            {errors.email && <p className="text-red-500 text-xs mt-1">{errors.email}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              직업 <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.occupation}
              onChange={(e) => handleInputChange('occupation', e.target.value)}
              className={`w-full p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                errors.occupation ? 'border-red-300' : 'border-gray-300'
              }`}
            >
              <option value="">직업을 선택하세요</option>
              {occupations.map((occupation) => (
                <option key={occupation} value={occupation}>
                  {occupation}
                </option>
              ))}
            </select>
            {errors.occupation && <p className="text-red-500 text-xs mt-1">{errors.occupation}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              결혼상태 <span className="text-red-500">*</span>
            </label>
            <select
              value={formData.maritalStatus}
              onChange={(e) => handleInputChange('maritalStatus', e.target.value as any)}
              className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
            >
              {maritalStatusOptions.map((option) => (
                <option key={option.value} value={option.value}>
                  {option.label}
                </option>
              ))}
            </select>
          </div>
        </div>
      </div>

      {}
      <div className="bg-white rounded-lg shadow p-4">
        <h2 className="text-lg font-semibold text-gray-800 mb-4">주소 정보</h2>

        <div className="space-y-3">
          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              우편번호 <span className="text-red-500">*</span>
            </label>
            <div className="flex space-x-2">
              <input
                type="text"
                value={formData.address.zipCode}
                onChange={(e) => handleInputChange('address.zipCode', e.target.value)}
                className={`flex-1 p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                  errors.zipCode ? 'border-red-300' : 'border-gray-300'
                }`}
                placeholder="우편번호"
              />
              <button
                type="button"
                className="px-3 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors text-sm"
              >
                주소 검색
              </button>
            </div>
            {errors.zipCode && <p className="text-red-500 text-xs mt-1">{errors.zipCode}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              기본 주소 <span className="text-red-500">*</span>
            </label>
            <input
              type="text"
              value={formData.address.address1}
              onChange={(e) => handleInputChange('address.address1', e.target.value)}
              className={`w-full p-2 border rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm ${
                errors.address1 ? 'border-red-300' : 'border-gray-300'
              }`}
              placeholder="기본 주소"
            />
            {errors.address1 && <p className="text-red-500 text-xs mt-1">{errors.address1}</p>}
          </div>

          {}
          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">
              상세 주소
            </label>
            <input
              type="text"
              value={formData.address.address2}
              onChange={(e) => handleInputChange('address.address2', e.target.value)}
              className="w-full p-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 focus:border-blue-500 text-sm"
              placeholder="상세 주소 (선택사항)"
            />
          </div>
        </div>
      </div>

      {}
      <div className="flex justify-between">
        <button
          onClick={onPrevious}
          className="px-6 py-2 bg-gray-500 text-white rounded-lg hover:bg-gray-600 transition-colors text-sm"
        >
          이전 단계
        </button>
        <button
          onClick={handleNext}
          disabled={isValidating}
          className={`px-6 py-2 rounded-lg font-semibold transition-colors text-sm ${
            isValidating
              ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
              : 'bg-blue-600 text-white hover:bg-blue-700'
          }`}
        >
          {isValidating ? '검증 중...' : '다음 단계'}
        </button>
      </div>
    </div>
  );
};

export default PersonalInfo;