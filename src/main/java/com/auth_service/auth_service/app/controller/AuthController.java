package com.auth_service.auth_service.app.controller;

import com.auth_service.auth_service.app.model.dto.auth.*;
import com.auth_service.auth_service.app.services.AuthService;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("v1")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping(
            path = "/auth/login",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<AuthenResponse>> auth(@Valid @RequestBody AuthenRequest request) {
        ResponseBodyModel<AuthenResponse> response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping(
            path = "/auth/activate",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> activateUser(@RequestParam("token") String token) {
        ResponseBodyModel<String> response = authService.activateUser(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            path = "/auth/forgot-password",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> forgotPassword(@RequestParam("email") String email) {
        ResponseBodyModel<String> response = authService.forgetPassword(email);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            path = "/auth/reset-password",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        ResponseBodyModel<String> response = authService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            path = "/s/auth/change-password",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<String>> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        ResponseBodyModel<String> response = authService.changePassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping(
            path = "/auth/check-permission",
            consumes = APPLICATION_JSON_VALUE,
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<CheckPermissionResponse>> checkPermission(@Valid @RequestBody CheckPermissionRequest request) {
        ResponseBodyModel<CheckPermissionResponse> response = authService.checkPermission(request);
        return ResponseEntity.ok(response);
    }



}
