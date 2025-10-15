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
            String[] parsedInfo = parseSocialNumber(request.getSocialNumber());
            if (parsedInfo == null) {
                MyDataConsentResponseDto errorResponse = new MyDataConsentResponseDto();
                errorResponse.setMessage("주민등록번호 형식이 올바르지 않습니다.");
                return errorResponse;
            }

            MyDataConsentRequestDto enhancedRequest = MyDataConsentRequestDto.builder()
                .phoneNumber(request.getPhoneNumber())
                .socialNumber(request.getSocialNumber())
                .name(request.getName())
                .consentToMyDataCollection(request.isConsentToMyDataCollection())
                .birthDate(parsedInfo[0])
                .gender(parsedInfo[1])
                .build();

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

    private String[] parseSocialNumber(String socialNumber) {
        try {
            if (socialNumber == null || socialNumber.length() != 13) {
                return null;
            }

            String birthPart = socialNumber.substring(0, 6);
            char genderCode = socialNumber.charAt(6);

            String year = birthPart.substring(0, 2);
            String month = birthPart.substring(2, 4);
            String day = birthPart.substring(4, 6);

            String fullYear;
            String gender;

            switch (genderCode) {
                case '1':
                    fullYear = "19" + year;
                    gender = "M";
                    break;
                case '2':
                    fullYear = "19" + year;
                    gender = "F";
                    break;
                case '3':
                    fullYear = "20" + year;
                    gender = "M";
                    break;
                case '4':
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