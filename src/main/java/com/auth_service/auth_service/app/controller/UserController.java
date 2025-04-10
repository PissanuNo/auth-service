package com.auth_service.auth_service.app.controller;

import com.auth_service.auth_service.app.model.dto.user.UserRequest;
import com.auth_service.auth_service.app.model.dto.user.UserResponse;
import com.auth_service.auth_service.app.services.UserService;
import com.auth_service.auth_service.core.model.Permission;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static com.auth_service.auth_service.app.constants.Permissions.menuCode.USER_MANAGEMENT;
import static com.auth_service.auth_service.app.constants.Permissions.permissionFlag.*;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("v1")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Permission(menu = USER_MANAGEMENT, permission = CREATE)
    @PostMapping(
            path = "/s/user",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> createUser(@Valid @RequestBody UserRequest request) {
        ResponseBodyModel<String> auth = userService.createUser(request);
        return ResponseEntity.ok(auth);
    }

    @Permission(menu = USER_MANAGEMENT, permission = UPDATE)
    @PatchMapping(
            path = "/s/user",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> updateUser(@Valid @RequestBody UserRequest request) {
        ResponseBodyModel<String> auth = userService.updateUser(request);
        return ResponseEntity.ok(auth);
    }

    @Permission(menu = USER_MANAGEMENT, permission = READ)
    @GetMapping(
            path = "/s/user/{userId}",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<UserResponse>> getUserById(@PathVariable("userId") String userId) {
        ResponseBodyModel<UserResponse> auth = userService.getUserById(userId);
        return ResponseEntity.ok(auth);
    }





}
