package com.auth_service.auth_service.app.services.impl;

import com.auth_service.auth_service.app.model.dbs.RoleCorporateModel;
import com.auth_service.auth_service.app.model.dbs.UserModel;
import com.auth_service.auth_service.app.model.dbs.UserResetPasswordTokenModel;
import com.auth_service.auth_service.app.model.dto.auth.*;
import com.auth_service.auth_service.app.repositories.*;
import com.auth_service.auth_service.app.services.AuthService;
import com.auth_service.auth_service.app.services.UtilService;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import com.auth_service.auth_service.core.service.JwtUtilService;
import com.auth_service.auth_service.core.service.PrincipalService;
import io.jsonwebtoken.Claims;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

import static com.auth_service.auth_service.app.constants.Constant.ResponseCode.*;
import static com.auth_service.auth_service.app.constants.Constant.ResponseMessage.*;
import static com.auth_service.auth_service.app.constants.Constant.activities.ENCRYPT;


@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final RoleRepository roleRepository;
    Logger log = LoggerFactory.getLogger("AuthService");
    private final UserRepository userRepository;
    private final UserAccessRepository userAccessRepository;
    private final UtilService utilService;
    private final JwtUtilService jwtUtilService;
    private final RoleCorporateRepository roleCorporateRepository;
    private final UserResetPasswordTokenRepository userResetPasswordTokenRepository;
    private final PrincipalService principalService;

    @Transactional
    @Override
    public ResponseBodyModel<AuthenResponse> login(AuthenRequest request) {
        ResponseBodyModel<AuthenResponse> response = new ResponseBodyModel<>();
        try {

            Optional<UserModel> userModel = userRepository
                    .findByEmailAndStatus(request.getUsername(),
                            true);
            if (userModel.isEmpty()) {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
                return response;
            }
            String pwd = utilService.textValueEncAndDec(userModel.get().getUserId(),
                    request.getPassword(),
                    ENCRYPT);
            String sysPwd = userModel.get().getUserAccessModel().getPassword();
            if (pwd.equals(sysPwd)) {

                List<String> roleCorporateModels = roleCorporateRepository
                        .findByRoleId(userModel.get().getRoleId())
                        .stream()
                        .map(RoleCorporateModel::getRoleId)
                        .toList();

                Map<String, Object> mapUser = new HashMap<>();
                mapUser.put("userId", userModel.get().getUserId());
                mapUser.put("role", userModel.get().getRoleId());
                mapUser.put("userName", userModel.get().getEmail());
                mapUser.put("roleCorporate", roleCorporateModels);

                String accessToken = jwtUtilService.enCode(mapUser, "access");
                String refreshToken = jwtUtilService.enCode(mapUser, "refresh");
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS,
                        AuthenResponse.builder()
                                .userId(userModel.get().getUserId())
                                .accessToken(accessToken)
                                .refreshToken(refreshToken)
                                .expired(new Date(System.currentTimeMillis()))
                                .build());
                userModel.get().getUserAccessModel().setLastAccessDate(new Timestamp(System.currentTimeMillis()));
                userAccessRepository.saveAndFlush(userModel.get().getUserAccessModel());

            } else {
                response.setOperationError(ERROR_CODE_BUSINESS, ERROR_PASSWORD_INCORRECT, null);
                return response;
            }

        } catch (Exception ex) {
            log.error("Error login ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }


    @Transactional
    @Override
    public ResponseBodyModel<String> activateUser(String token) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<UserModel> userModel = userRepository.findByActivationToken(token);
            if (userModel.isEmpty()) {
                log.error("Invalid activation token");
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
                return response;
            }

            userModel.get().setStatus(true);
            userModel.get().setActivationToken(null);
            userRepository.saveAndFlush(userModel.get());
            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
        } catch (Exception ex) {
            log.error("Error activating user ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> forgetPassword(String email) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<UserModel> userModel = userRepository.findByEmail(email);
            if (userModel.isEmpty()) {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
                return response;
            }
            //save token for send link
            String resetPwdToken = utilService.generateToken();
            Date expireLocalDateTime = Timestamp.valueOf(LocalDateTime.now().plusMinutes(30));

            userResetPasswordTokenRepository.saveAndFlush(UserResetPasswordTokenModel.builder()
                    .tranId(UUID.randomUUID().toString())
                    .userId(userModel.get().getUserId())
                    .token(resetPwdToken)
                    .createDate(new Timestamp(System.currentTimeMillis()))
                    .expireDate(expireLocalDateTime)
                    .build());

            //TODO send reset password email

            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
        } catch (Exception ex) {
            log.error("Error forgetting password ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> resetPassword(ResetPasswordRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<UserResetPasswordTokenModel> resetPwdToken = userResetPasswordTokenRepository
                    .findTopByTokenOrderByCreateDateDesc(request.getToken());
            if (resetPwdToken.isEmpty()) {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
                return response;
            }

            //check expire
            if (new Timestamp(new Date().getTime()).after(resetPwdToken.get().getExpireDate())) {
                response.setOperationError(ERROR_CODE_BUSINESS, PASSWORD_RESET_EXP, null);
                return response;
            }

            Optional<UserModel> userModel = userRepository.findById(resetPwdToken.get().getUserId());
            if (userModel.isEmpty()) {
                response.setOperationError(ERROR_CODE_BUSINESS, USER_NOT_FOUND, null);
                return response;
            }

            if (request.getNewPassword().equals(request.getConfirmPassword())) {
                if (!utilService.isValidPassword(request.getNewPassword())) {
                    response.setOperationError(ERROR_CODE_BUSINESS, PASSWORD_NOT_STRONG, null);
                    return response;
                }
                String newPwd = utilService.textValueEncAndDec(userModel.get().getUserId(),
                        request.getNewPassword(),
                        ENCRYPT);
                userModel.get().getUserAccessModel().setPassword(newPwd);
                userModel.get().getUserAccessModel().setLastPasswordChangeDate(new Timestamp(System.currentTimeMillis()));
                userAccessRepository.saveAndFlush(userModel.get().getUserAccessModel());
                userResetPasswordTokenRepository.deleteById(resetPwdToken.get().getTranId());
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
            } else {
                response.setOperationError(ERROR_CODE_BUSINESS, PASSWORD_NOT_MATCH, null);
            }

        } catch (Exception ex) {
            log.error("Error reset password ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> changePassword(ChangePasswordRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            String userId = principalService.getUserId();
            Optional<UserModel> userModel = userRepository.findById(userId);
            if (userModel.isEmpty()) {
                response.setOperationError(ERROR_CODE_BUSINESS, USER_NOT_FOUND, null);
                return response;
            }

            String oldPwd = utilService.textValueEncAndDec(userId, request.getOldPassword(), ENCRYPT);
            if (oldPwd.equals(userModel.get().getUserAccessModel().getPassword())) {
                if (request.getNewPassword().equals(request.getConfirmPassword())) {
                    if (!utilService.isValidPassword(request.getNewPassword())) {
                        response.setOperationError(ERROR_CODE_BUSINESS, PASSWORD_NOT_STRONG, null);
                        return response;
                    }
                    String newPwd = utilService.textValueEncAndDec(userId, request.getNewPassword(), ENCRYPT);
                    userModel.get().getUserAccessModel().setPassword(newPwd);
                    userModel.get().getUserAccessModel().setLastPasswordChangeDate(new Timestamp(System.currentTimeMillis()));
                    userAccessRepository.saveAndFlush(userModel.get().getUserAccessModel());
                    response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
                } else {
                    response.setOperationError(ERROR_CODE_BUSINESS, PASSWORD_NOT_MATCH, null);
                }
            } else {
                response.setOperationError(ERROR_CODE_BUSINESS, ERROR_PASSWORD_INCORRECT, null);
            }

        } catch (Exception ex) {
            log.error("Error changing password ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Override
    public ResponseBodyModel<CheckPermissionResponse> checkPermission(CheckPermissionRequest request) {
        ResponseBodyModel<CheckPermissionResponse> response = new ResponseBodyModel<>();
        try {

            Claims claims = jwtUtilService.deCode(request.getToken());
            String roleId = claims.get("role", String.class);

            boolean checked = roleRepository.findById(roleId)
                    .map(role -> role.getRoleMenuAccessModel().stream()
                            .anyMatch(roleMenuAccessModel ->
                                    roleMenuAccessModel.getMenu().getMenuCode().equals(request.getMenu()) &&
                                            (roleMenuAccessModel.getAccessFlag() & request.getPermission()) != 0
                            )
                    )
                    .orElse(false);

            response.setOperationSuccess(SUCCESS_CODE, SUCCESS,
                    CheckPermissionResponse.builder()
                            .checkPermission(checked)
                            .build());

        } catch (Exception ex) {
            log.error("Error checking permission ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }
}
