package com.hanainplan.user.service;

import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Base64;

@Service
public class CiConversionService {

    // 공인기관의 시크릿 키 (실제 운영에서는 환경변수나 설정 파일에서 관리)
    private static final String SECRET_KEY = "HANAINPLAN_SECRET_KEY_2024";
    private static final String ALGORITHM = "HmacSHA256";
    private static final int CI_LENGTH = 32; // CI 길이 제한

    /**
     * CI 변환 (주민번호만으로)
     * @param residentNumber 주민번호
     * @return CI 값
     */
    public String convertToCi(String residentNumber) {
        try {
            // 주민번호에서 생년월일과 성별 추출
            String birthDate = extractBirthDateFromResidentNumber(residentNumber);
            String gender = extractGenderFromResidentNumber(residentNumber);
            
            // 이름은 주민번호로부터 추출할 수 없으므로 빈 문자열로 처리
            String normalizedName = "";
            String normalizedBirthDate = normalizeBirthDate(birthDate);
            String normalizedGender = normalizeGender(gender);
            String normalizedResidentNumber = normalizeResidentNumber(residentNumber);

            // 결합
            String combinedData = combineData(normalizedName, normalizedBirthDate, normalizedGender, normalizedResidentNumber);

            // 키 결합 및 일방향 변환 (HMAC-SHA256)
            String hashedData = generateHmac(combinedData, SECRET_KEY);

            // 인코딩/포맷팅 (Base64 URL-safe)
            String encodedData = Base64.getUrlEncoder().withoutPadding().encodeToString(hashedData.getBytes(StandardCharsets.UTF_8));

            // 길이 제한 적용
            String ci = encodedData.substring(0, Math.min(CI_LENGTH, encodedData.length()));

            return ci;

        } catch (Exception e) {
            throw new RuntimeException("CI 변환 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 주민번호를 CI로 변환 (기존 메서드 - 사용자 생성 시 사용)
     * @param name 실명
     * @param birthDate 생년월일 (YYYYMMDD 형식)
     * @param gender 성별 (M/F)
     * @param residentNumber 주민번호 (13자리)
     * @return CI 값
     */
    public String convertToCi(String name, String birthDate, String gender, String residentNumber) {
        try {
            // 1. 입력 데이터 정규화
            String normalizedName = normalizeName(name);
            String normalizedBirthDate = normalizeBirthDate(birthDate);
            String normalizedGender = normalizeGender(gender);
            String normalizedResidentNumber = normalizeResidentNumber(residentNumber);

            // 2. 결합 (정해진 순서로 필드 결합)
            String combinedData = combineData(normalizedName, normalizedBirthDate, normalizedGender, normalizedResidentNumber);

            // 3. 키 결합 및 일방향 변환 (HMAC-SHA256)
            String hashedData = generateHmac(combinedData, SECRET_KEY);

            // 4. 인코딩/포맷팅 (Base64 URL-safe)
            String encodedData = Base64.getUrlEncoder().withoutPadding().encodeToString(hashedData.getBytes(StandardCharsets.UTF_8));

            // 5. 길이 제한 적용
            String ci = encodedData.substring(0, Math.min(CI_LENGTH, encodedData.length()));

            return ci;

        } catch (Exception e) {
            throw new RuntimeException("CI 변환 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * CI 검증 (4개 파라미터로)
     * @param name 이름
     * @param birthDate 생년월일
     * @param gender 성별
     * @param residentNumber 주민번호
     * @return 검증 결과 (사용자 존재 여부)
     */
    public boolean verifyCi(String name, String birthDate, String gender, String residentNumber) {
        try {
            // 입력된 개인정보를 CI로 변환
            String ci = convertToCi(name, birthDate, gender, residentNumber);
            
            // CI로 사용자 존재 여부 확인 (이 메서드는 UserService에서 구현)
            // 여기서는 CI 변환만 수행하고, 실제 사용자 존재 여부는 Controller에서 확인
            return true; // CI 변환 성공
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 주민번호에서 생년월일 추출
     * @param residentNumber 주민번호
     * @return 생년월일 (YYYYMMDD)
     */
    public String extractBirthDateFromResidentNumber(String residentNumber) {
        if (residentNumber == null || residentNumber.length() != 13) {
            throw new IllegalArgumentException("주민번호는 13자리여야 합니다.");
        }
        
        String year = residentNumber.substring(0, 2);
        String month = residentNumber.substring(2, 4);
        String day = residentNumber.substring(4, 6);
        
        // 7번째 자리로 1900년대/2000년대 구분
        int genderCode = Integer.parseInt(residentNumber.substring(6, 7));
        if (genderCode >= 1 && genderCode <= 2) {
            year = "19" + year;
        } else if (genderCode >= 3 && genderCode <= 4) {
            year = "20" + year;
        } else {
            throw new IllegalArgumentException("유효하지 않은 주민번호입니다.");
        }
        
        return year + month + day;
    }

    /**
     * 주민번호에서 성별 추출
     * @param residentNumber 주민번호
     * @return 성별 (M/F)
     */
    public String extractGenderFromResidentNumber(String residentNumber) {
        if (residentNumber == null || residentNumber.length() != 13) {
            throw new IllegalArgumentException("주민번호는 13자리여야 합니다.");
        }
        
        int genderCode = Integer.parseInt(residentNumber.substring(6, 7));
        if (genderCode == 1 || genderCode == 3) {
            return "M";
        } else if (genderCode == 2 || genderCode == 4) {
            return "F";
        } else {
            throw new IllegalArgumentException("유효하지 않은 주민번호입니다.");
        }
    }

    /**
     * 주민번호 유효성 검증
     * @param name 이름
     * @param birthDate 생년월일 (YYYYMMDD)
     * @param gender 성별 (M/F)
     * @param residentNumber 주민번호 (13자리)
     * @throws IllegalArgumentException 검증 실패 시
     */
    public void validateResidentNumber(String name, String birthDate, String gender, String residentNumber) {
        // 1. 기본 형식 검증
        if (residentNumber == null || residentNumber.length() != 13) {
            throw new IllegalArgumentException("주민번호는 13자리 숫자여야 합니다.");
        }
        
        if (!residentNumber.matches("\\d{13}")) {
            throw new IllegalArgumentException("주민번호는 숫자만 입력 가능합니다.");
        }
        
        // 2. 생년월일과 주민번호 앞 6자리 일치 검증
        String expectedBirthPart = birthDate.substring(2); // YYYYMMDD -> YYMMDD
        String actualBirthPart = residentNumber.substring(0, 6);
        
        if (!expectedBirthPart.equals(actualBirthPart)) {
            throw new IllegalArgumentException("생년월일과 주민번호 앞 6자리가 일치하지 않습니다.");
        }
        
        // 3. 성별 코드 검증
        int genderCode = Integer.parseInt(residentNumber.substring(6, 7));
        int birthYear = Integer.parseInt(birthDate.substring(0, 4));
        
        // 성별 입력값 검증
        if (gender == null || (!gender.equals("M") && !gender.equals("F"))) {
            throw new IllegalArgumentException("성별은 M 또는 F만 입력 가능합니다.");
        }
        
        // 연도대별 성별 코드 검증
        if (birthYear >= 1900 && birthYear <= 1999) {
            // 1900년대
            if (gender.equals("M") && genderCode != 1) {
                throw new IllegalArgumentException("1900년대 남성의 주민번호 7번째 자리는 1이어야 합니다.");
            }
            if (gender.equals("F") && genderCode != 2) {
                throw new IllegalArgumentException("1900년대 여성의 주민번호 7번째 자리는 2이어야 합니다.");
            }
        } else if (birthYear >= 2000) {
            // 2000년대 이상
            if (gender.equals("M") && genderCode != 3) {
                throw new IllegalArgumentException("2000년대 이상 남성의 주민번호 7번째 자리는 3이어야 합니다.");
            }
            if (gender.equals("F") && genderCode != 4) {
                throw new IllegalArgumentException("2000년대 이상 여성의 주민번호 7번째 자리는 4이어야 합니다.");
            }
        } else {
            throw new IllegalArgumentException("유효하지 않은 출생연도입니다. 1900년 이후만 가능합니다.");
        }
        
        // 4. 체크섬 검증 (주민번호 마지막 자리)
        if (!validateChecksum(residentNumber)) {
            throw new IllegalArgumentException("주민번호의 체크섬이 올바르지 않습니다.");
        }
    }

    /**
     * 주민번호 체크섬 검증
     * @param residentNumber 주민번호
     * @return 검증 결과
     */
    private boolean validateChecksum(String residentNumber) {
        int[] multipliers = {2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5};
        int sum = 0;
        
        for (int i = 0; i < 12; i++) {
            sum += Character.getNumericValue(residentNumber.charAt(i)) * multipliers[i];
        }
        
        int remainder = sum % 11;
        int checkDigit = (11 - remainder) % 10;
        int actualCheckDigit = Character.getNumericValue(residentNumber.charAt(12));
        
        return checkDigit == actualCheckDigit;
    }

    /**
     * 이름 정규화
     */
    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("이름은 필수입니다.");
        }
        return name.trim().toUpperCase();
    }

    /**
     * 생년월일 정규화
     */
    private String normalizeBirthDate(String birthDate) {
        if (birthDate == null || birthDate.trim().isEmpty()) {
            throw new IllegalArgumentException("생년월일은 필수입니다.");
        }

        String cleaned = birthDate.replaceAll("[^0-9]", "");
        
        // YYYYMMDD 형식으로 변환
        if (cleaned.length() == 8) {
            return cleaned;
        } else if (cleaned.length() == 6) {
            // YYMMDD 형식인 경우 YYYYMMDD로 변환
            int year = Integer.parseInt(cleaned.substring(0, 2));
            if (year >= 0 && year <= 30) {
                return "20" + cleaned;
            } else {
                return "19" + cleaned;
            }
        } else {
            throw new IllegalArgumentException("생년월일 형식이 올바르지 않습니다. (YYYYMMDD 또는 YYMMDD)");
        }
    }

    /**
     * 성별 정규화
     */
    private String normalizeGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            throw new IllegalArgumentException("성별은 필수입니다.");
        }
        
        String normalized = gender.trim().toUpperCase();
        if ("M".equals(normalized) || "MALE".equals(normalized) || "남".equals(normalized) || "남성".equals(normalized)) {
            return "M";
        } else if ("F".equals(normalized) || "FEMALE".equals(normalized) || "여".equals(normalized) || "여성".equals(normalized)) {
            return "F";
        } else {
            throw new IllegalArgumentException("성별은 M(남성) 또는 F(여성)이어야 합니다.");
        }
    }

    /**
     * 주민번호 정규화
     */
    private String normalizeResidentNumber(String residentNumber) {
        if (residentNumber == null || residentNumber.trim().isEmpty()) {
            throw new IllegalArgumentException("주민번호는 필수입니다.");
        }

        String cleaned = residentNumber.replaceAll("[^0-9]", "");
        
        if (cleaned.length() != 13) {
            throw new IllegalArgumentException("주민번호는 13자리여야 합니다.");
        }

        // 주민번호 유효성 검증 (간단한 체크)
        if (!isValidResidentNumber(cleaned)) {
            throw new IllegalArgumentException("유효하지 않은 주민번호입니다.");
        }

        return cleaned;
    }

    /**
     * 주민번호 유효성 검증
     */
    private boolean isValidResidentNumber(String residentNumber) {
        try {
            // 생년월일 부분 검증
            int year = Integer.parseInt(residentNumber.substring(0, 2));
            int month = Integer.parseInt(residentNumber.substring(2, 4));
            int day = Integer.parseInt(residentNumber.substring(4, 6));
            
            if (month < 1 || month > 12) return false;
            if (day < 1 || day > 31) return false;
            
            // 성별 코드 검증 (7번째 자리)
            int genderCode = Integer.parseInt(residentNumber.substring(6, 7));
            if (genderCode < 1 || genderCode > 4) return false;
            
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * 데이터 결합
     */
    private String combineData(String name, String birthDate, String gender, String residentNumber) {
        // 정해진 순서로 필드 결합 (구분자 사용)
        return String.join("|", name, birthDate, gender, residentNumber);
    }

    /**
     * HMAC-SHA256 생성
     */
    private String generateHmac(String data, String key) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance(ALGORITHM);
        SecretKeySpec secretKeySpec = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), ALGORITHM);
        mac.init(secretKeySpec);
        
        byte[] hmacData = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
        return Base64.getEncoder().encodeToString(hmacData);
    }
}
