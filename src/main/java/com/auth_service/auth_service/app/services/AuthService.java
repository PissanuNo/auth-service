package com.auth_service.auth_service.app.services;

import com.auth_service.auth_service.app.model.dto.auth.*;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import jakarta.transaction.Transactional;

public interface AuthService {

    ResponseBodyModel<AuthenResponse> login(AuthenRequest request);

    ResponseBodyModel<String> activateUser(String token);

    @Transactional
    ResponseBodyModel<String> forgetPassword(String email);

    @Transactional
    ResponseBodyModel<String> resetPassword(ResetPasswordRequest request);

    @Transactional
    ResponseBodyModel<String> changePassword(ChangePasswordRequest request);

    ResponseBodyModel<CheckPermissionResponse> checkPermission(CheckPermissionRequest request);
}
