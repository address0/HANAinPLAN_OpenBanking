package com.hanainplan.domain.user.service;

import com.hanainplan.domain.user.dto.CustomerAccountInfoDto;
import com.hanainplan.domain.user.dto.MyDataConsentRequestDto;
import com.hanainplan.domain.user.dto.MyDataConsentResponseDto;
import com.hanainplan.domain.user.service.UserAccountTransactionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MyDataConsentService {
    
    private final RestTemplate restTemplate;
    private final UserAccountTransactionService userAccountTransactionService;
    
    private static final String CI_SERVER_MYDATA_URL = "http://localhost:8084/api/user/mydata/consent";
    
    public MyDataConsentResponseDto processMyDataConsent(MyDataConsentRequestDto request) {
        try {
            // 주민등록번호에서 생년월일과 성별 추출
            String[] parsedInfo = parseSocialNumber(request.getSocialNumber());
            if (parsedInfo == null) {
                MyDataConsentResponseDto errorResponse = new MyDataConsentResponseDto();
                errorResponse.setMessage("주민등록번호 형식이 올바르지 않습니다.");
                return errorResponse;
            }
            
            // 추출된 정보를 포함한 요청 객체 생성
            MyDataConsentRequestDto enhancedRequest = MyDataConsentRequestDto.builder()
                .phoneNumber(request.getPhoneNumber())
                .socialNumber(request.getSocialNumber())
                .name(request.getName())
                .consentToMyDataCollection(request.isConsentToMyDataCollection())
                .birthDate(parsedInfo[0]) // YYYYMMDD
                .gender(parsedInfo[1])    // M 또는 F
                .build();
            
            // 실명인증 서버(8084)에 마이데이터 수집 동의 요청 전달
            String url = CI_SERVER_MYDATA_URL;
            
            MyDataConsentResponseDto response = null;
            try {
                response = restTemplate.postForObject(
                    url, 
                    enhancedRequest, 
                    MyDataConsentResponseDto.class
                );
                
            } catch (Exception e) {
                throw e;
            }
            
            if (response != null) {
                return response;
            } else {
                return MyDataConsentResponseDto.success("마이데이터 수집 처리 중 오류가 발생했습니다.", new ArrayList<>(), 0, 0);
            }
            
        } catch (Exception e) {
            return MyDataConsentResponseDto.success("마이데이터 수집 서버 연결 실패: " + e.getMessage(), new ArrayList<>(), 0, 0);
        }
    }
    
    /**
     * 주민등록번호에서 생년월일과 성별 추출
     * @param socialNumber 주민등록번호 (13자리)
     * @return [생년월일(YYYYMMDD), 성별(M/F)] 또는 null
     */
    private String[] parseSocialNumber(String socialNumber) {
        try {
            if (socialNumber == null || socialNumber.length() != 13) {
                return null;
            }
            
            // 앞 6자리: 생년월일 (YYMMDD)
            String birthPart = socialNumber.substring(0, 6);
            // 7번째 자리: 성별 코드 (1,2,3,4)
            char genderCode = socialNumber.charAt(6);
            
            // 생년월일 변환 (YYMMDD -> YYYYMMDD)
            String year = birthPart.substring(0, 2);
            String month = birthPart.substring(2, 4);
            String day = birthPart.substring(4, 6);
            
            // 성별 코드에 따른 세기 결정
            String fullYear;
            String gender;
            
            switch (genderCode) {
                case '1': // 1900년대 남성
                    fullYear = "19" + year;
                    gender = "M";
                    break;
                case '2': // 1900년대 여성
                    fullYear = "19" + year;
                    gender = "F";
                    break;
                case '3': // 2000년대 남성
                    fullYear = "20" + year;
                    gender = "M";
                    break;
                case '4': // 2000년대 여성
                    fullYear = "20" + year;
                    gender = "F";
                    break;
                default:
                    return null;
            }
            
            String birthDate = fullYear + month + day;
            return new String[]{birthDate, gender};
            
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
