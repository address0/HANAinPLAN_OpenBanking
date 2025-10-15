package com.hanainplan.domain.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KakaoUserInfoDto {
    private boolean success;
    private String message;
    private String id;
    private String nickname;
    private String email;
    private String profileImage;
    private String accessToken;
}