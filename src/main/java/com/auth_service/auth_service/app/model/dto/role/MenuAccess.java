package com.auth_service.auth_service.app.model.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuAccess {
    private String accessId;
    @NotBlank
    private String menuId;
    @NotNull
    private int accessFlag;
}
