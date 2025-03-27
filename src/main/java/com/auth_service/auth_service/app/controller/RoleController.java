package com.auth_service.auth_service.app.controller;

import com.auth_service.auth_service.app.model.dto.role.RoleRequest;
import com.auth_service.auth_service.app.model.dto.role.RoleResponse;
import com.auth_service.auth_service.app.services.RoleAccessService;
import com.auth_service.auth_service.core.model.Permission;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.auth_service.auth_service.app.constants.Permissions.menuCode.ROLE_PERMISSION;
import static com.auth_service.auth_service.app.constants.Permissions.permissionFlag.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("v1")
@RequiredArgsConstructor
public class RoleController {

    private final RoleAccessService roleAccessService;

    @Permission(menu = ROLE_PERMISSION, permission = READ)
    @GetMapping(
            path = "/s/role/{roleId}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<RoleResponse>> getRoleById(@PathVariable("roleId") String roleId) {
        ResponseBodyModel<RoleResponse> response = roleAccessService.getRoleById(roleId);
        return ResponseEntity.ok(response);
    }

    @Permission(menu = ROLE_PERMISSION, permission = CREATE)
    @PostMapping(
            path = "/s/role",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> createRole(@Valid @RequestBody RoleRequest request) {
        ResponseBodyModel<String> response = roleAccessService.createRole(request);
        return ResponseEntity.ok(response);
    }

    @Permission(menu = ROLE_PERMISSION, permission = UPDATE)
    @PatchMapping(
            path = "/s/role",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> updateRole(@Valid @RequestBody RoleRequest request) {
        ResponseBodyModel<String> response = roleAccessService.updateRole(request);
        return ResponseEntity.ok(response);
    }

    @Permission(menu = ROLE_PERMISSION, permission = DELETE)
    @DeleteMapping(
            path = "/s/role/{roleId}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> deleteRole(@PathVariable("roleId") String roleId) {
        ResponseBodyModel<String> response = roleAccessService.deleteRole(roleId);
        return ResponseEntity.ok(response);
    }

}
