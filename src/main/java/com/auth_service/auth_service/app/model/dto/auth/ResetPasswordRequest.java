package com.auth_service.auth_service.app.model.dto.auth;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ResetPasswordRequest {
    @NotBlank
    private String newPassword;
    @NotBlank
    private String confirmPassword;
    @NotBlank
    private String token;
}
