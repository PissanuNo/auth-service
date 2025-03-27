package com.auth_service.auth_service.app.model.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthenResponse {
    private String userId;
    private String accessToken;
    private String refreshToken;
    private Date expired;
}
