package com.auth_service.auth_service.app.model.dto.role;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoleRequest {
    private String roleId;
    @NotBlank
    private String roleNameEn;
    @NotBlank
    private String roleNameTh;

    @NotNull
    private List<String> corporateList;

    private List<MenuAccess> menuAccessList;
}
