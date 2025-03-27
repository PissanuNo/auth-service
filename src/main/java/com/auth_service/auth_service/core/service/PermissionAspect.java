package com.auth_service.auth_service.core.service;


import com.auth_service.auth_service.app.model.dto.auth.CheckPermissionRequest;
import com.auth_service.auth_service.app.model.dto.auth.CheckPermissionResponse;
import com.auth_service.auth_service.app.services.AuthService;
import com.auth_service.auth_service.core.model.JwtClaim;
import com.auth_service.auth_service.core.model.Permission;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Aspect
@Component
public class PermissionAspect {


    private final AuthService authService;

    public PermissionAspect(AuthService authService) {
        this.authService = authService;
    }

    @Before("@annotation(permission)")  // Catch existing methods @Permission
    public void checkPermission(Permission permission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String token = null;
        if (authentication.getPrincipal() instanceof JwtClaim jwtClaim) {
            token = jwtClaim.getToken();
        }

            ResponseBodyModel<CheckPermissionResponse> checkPermission = authService
                .checkPermission(CheckPermissionRequest.builder()
                        .token(token)
                        .menu(permission.menu())
                        .permission(permission.permission())
                        .build());

        // Check if the permissionFlag allows access
        if ((!checkPermission.isStatus()) || (!checkPermission.getObjectValue().isCheckPermission())) {
            ResponseBodyModel<Object> result = new ResponseBodyModel<>();
            // Send results that are ResponseEntity
            Map<String, String> errors = new HashMap<>();
            errors.put("permission", "You don't have permission to access this resource.");
            result.setOperationError("ERROR_CODE_FORBIDDEN", "FORBIDDEN", errors);

            // Send ResponseEntity containing error information and HttpStatus.FORBIDDEN
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access Denied");
        }
    }

}
