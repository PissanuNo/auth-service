package com.auth_service.auth_service.app.model.dto.client;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EmailRequest {
    @NotBlank
    private String recipientEmail;
    @NotBlank
    private String templateName;

    private Map<String, Object> variables;
}
