package com.auth_service.auth_service.app.services;

import com.auth_service.auth_service.app.model.dto.role.RoleRequest;
import com.auth_service.auth_service.app.model.dto.role.RoleResponse;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import jakarta.transaction.Transactional;

public interface RoleAccessService {

    @Transactional
    ResponseBodyModel<String> createRole(RoleRequest request);

    ResponseBodyModel<String> updateRole(RoleRequest request);


    @Transactional
    ResponseBodyModel<String> deleteRole(String roleId);

    ResponseBodyModel<RoleResponse> getRoleById(String roleId);
}
