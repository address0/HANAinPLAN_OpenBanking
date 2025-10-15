package com.hanainplan.domain.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hanainplan.domain.auth.dto.KakaoUserInfoDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Slf4j
@Service
@RequiredArgsConstructor
public class KakaoAuthService {

    @Value("${kakao.restapi.key}")
    private String kakaoRestApiKey;

    @Value("${kakao.redirect.uri}")
    private String redirectUri;

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String KAKAO_AUTH_URL = "https://kauth.kakao.com/oauth/authorize";
    private static final String KAKAO_TOKEN_URL = "https://kauth.kakao.com/oauth/token";
    private static final String KAKAO_USER_INFO_URL = "https://kapi.kakao.com/v2/user/me";

    public String getKakaoAuthUrl() {
        try {
            log.info("=== 카카오 OAuth URL 생성 시작 ===");
            log.info("KAKAO_AUTH_URL: {}", KAKAO_AUTH_URL);
            log.info("kakaoRestApiKey: {}", kakaoRestApiKey.substring(0, 8) + "...");
            log.info("redirectUri: {}", redirectUri);

            String authUrl = UriComponentsBuilder.fromHttpUrl(KAKAO_AUTH_URL)
                    .queryParam("client_id", kakaoRestApiKey)
                    .queryParam("redirect_uri", URLEncoder.encode(redirectUri, StandardCharsets.UTF_8))
                    .queryParam("response_type", "code")
                    .queryParam("scope", "profile_nickname")
                    .build()
                    .toUriString();

            log.info("생성된 OAuth URL: {}", authUrl);
            return authUrl;
        } catch (Exception e) {
            log.error("카카오 OAuth URL 생성 실패", e);
            throw new RuntimeException("카카오 OAuth URL 생성에 실패했습니다.", e);
        }
    }

    public KakaoUserInfoDto handleKakaoCallback(String code) {
        log.info("=== 카카오 OAuth 콜백 처리 시작 ===");
        log.info("받은 인증 코드: {}", code);
        log.info("리다이렉트 URI: {}", redirectUri);
        log.info("REST API 키: {}", kakaoRestApiKey.substring(0, 8) + "...");
        log.info("현재 시간: {}", java.time.LocalDateTime.now());

        try {
            if (code == null || code.trim().isEmpty()) {
                log.error("인증 코드가 비어있습니다.");
                return createErrorResponse("인증 코드가 없습니다.");
            }

            log.info("액세스 토큰 요청 시작...");
            String accessToken = getAccessToken(code);
            log.info("액세스 토큰 획득 성공: {}", accessToken.substring(0, 10) + "...");

            log.info("사용자 정보 요청 시작...");
            KakaoUserInfoDto userInfo = getUserInfo(accessToken);
            log.info("사용자 정보 조회 성공: {}", userInfo.getNickname());

            return userInfo;

        } catch (HttpClientErrorException e) {
            log.error("카카오 API HTTP 오류 발생");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());

            String errorMessage = parseKakaoError(e.getResponseBodyAsString());
            return createErrorResponse(errorMessage);

        } catch (Exception e) {
            log.error("카카오 OAuth 콜백 처리 실패", e);
            return createErrorResponse("카카오 로그인 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    private KakaoUserInfoDto createErrorResponse(String message) {
        return KakaoUserInfoDto.builder()
                .success(false)
                .message(message)
                .build();
    }

    private String parseKakaoError(String errorBody) {
        try {
            JsonNode errorNode = objectMapper.readTree(errorBody);
            String error = errorNode.get("error").asText();
            String description = errorNode.get("error_description").asText();

            log.error("카카오 오류: {} - {}", error, description);

            switch (error) {
                case "invalid_grant":
                    return "인증 코드가 만료되었거나 이미 사용되었습니다. 다시 로그인해주세요.";
                case "invalid_client":
                    return "잘못된 클라이언트 정보입니다.";
                case "invalid_request":
                    return "잘못된 요청입니다.";
                default:
                    return "카카오 로그인에 실패했습니다: " + description;
            }
        } catch (Exception e) {
            log.error("카카오 오류 파싱 실패", e);
            return "카카오 로그인에 실패했습니다.";
        }
    }

    private String getAccessToken(String code) {
        log.info("=== 액세스 토큰 요청 시작 ===");
        log.info("요청 URL: {}", KAKAO_TOKEN_URL);
        log.info("인증 코드: {}", code);
        log.info("리다이렉트 URI: {}", redirectUri);

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
            params.add("grant_type", "authorization_code");
            params.add("client_id", kakaoRestApiKey);
            params.add("redirect_uri", redirectUri);
            params.add("code", code);

            log.info("요청 파라미터: {}", params);

            HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_TOKEN_URL,
                    HttpMethod.POST,
                    request,
                    String.class
            );

            log.info("응답 상태 코드: {}", response.getStatusCode());
            log.info("응답 본문: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String accessToken = jsonNode.get("access_token").asText();
                log.info("액세스 토큰 획득 성공");
                return accessToken;
            } else {
                throw new RuntimeException("액세스 토큰 요청 실패: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("액세스 토큰 요청 HTTP 오류");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("카카오 액세스 토큰 요청 실패", e);
            throw new RuntimeException("액세스 토큰을 가져오는데 실패했습니다.", e);
        }
    }

    private KakaoUserInfoDto getUserInfo(String accessToken) {
        log.info("=== 사용자 정보 요청 시작 ===");
        log.info("요청 URL: {}", KAKAO_USER_INFO_URL);
        log.info("액세스 토큰: {}", accessToken.substring(0, 10) + "...");

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            HttpEntity<String> request = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    KAKAO_USER_INFO_URL,
                    HttpMethod.GET,
                    request,
                    String.class
            );

            log.info("사용자 정보 응답 상태 코드: {}", response.getStatusCode());
            log.info("사용자 정보 응답 본문: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                JsonNode jsonNode = objectMapper.readTree(response.getBody());

                JsonNode kakaoAccount = jsonNode.get("kakao_account");
                JsonNode profile = kakaoAccount.get("profile");

                String id = jsonNode.get("id").asText();
                String nickname = profile.has("nickname") ? profile.get("nickname").asText() : "카카오사용자";
                String profileImage = profile.has("profile_image_url") ? profile.get("profile_image_url").asText() : null;

                log.info("사용자 정보 파싱 완료 - ID: {}, 닉네임: {}", id, nickname);

                return KakaoUserInfoDto.builder()
                        .success(true)
                        .message("카카오 OAuth 로그인 성공")
                        .id(id)
                        .nickname(nickname)
                        .email(null)
                        .profileImage(profileImage)
                        .accessToken(accessToken)
                        .build();
            } else {
                throw new RuntimeException("사용자 정보 요청 실패: " + response.getStatusCode());
            }
        } catch (HttpClientErrorException e) {
            log.error("사용자 정보 요청 HTTP 오류");
            log.error("상태 코드: {}", e.getStatusCode());
            log.error("응답 본문: {}", e.getResponseBodyAsString());
            throw e;
        } catch (Exception e) {
            log.error("카카오 사용자 정보 요청 실패", e);
            throw new RuntimeException("사용자 정보를 가져오는데 실패했습니다.", e);
        }
    }
}