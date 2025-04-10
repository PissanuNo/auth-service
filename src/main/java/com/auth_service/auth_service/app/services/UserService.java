package com.auth_service.auth_service.app.services;

import com.auth_service.auth_service.app.model.dto.user.UserRequest;
import com.auth_service.auth_service.app.model.dto.user.UserResponse;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import jakarta.transaction.Transactional;

public interface UserService {

    @Transactional
    ResponseBodyModel<String> createUser(UserRequest request);

    ResponseBodyModel<String> updateUser(UserRequest request);

    ResponseBodyModel<UserResponse> getUserById(String userId);

    @Transactional
    ResponseBodyModel<String> changeUserStatus(String userId, boolean status);
}
