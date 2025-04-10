package com.auth_service.auth_service.app.services.impl;

import com.auth_service.auth_service.app.model.dbs.UserAccessModel;
import com.auth_service.auth_service.app.model.dbs.UserModel;
import com.auth_service.auth_service.app.model.dto.client.EmailRequest;
import com.auth_service.auth_service.app.model.dto.user.UserRequest;
import com.auth_service.auth_service.app.model.dto.user.UserResponse;
import com.auth_service.auth_service.app.repositories.RoleRepository;
import com.auth_service.auth_service.app.repositories.UserAccessRepository;
import com.auth_service.auth_service.app.repositories.UserRepository;
import com.auth_service.auth_service.app.services.UserService;
import com.auth_service.auth_service.app.services.UtilService;
import com.auth_service.auth_service.app.services.client.CommuticationClient;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import com.auth_service.auth_service.core.service.PrincipalService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static com.auth_service.auth_service.app.constants.Constant.ResponseCode.*;
import static com.auth_service.auth_service.app.constants.Constant.ResponseMessage.*;
import static com.auth_service.auth_service.app.constants.Constant.activities.ENCRYPT;


@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    Logger log = LoggerFactory.getLogger("UserService");
    private final UserRepository userRepository;
    private final UserAccessRepository userAccessRepository;
    private final RoleRepository roleRepository;
    private final PrincipalService principalService;
    private final UtilService utilService;
    private final CommuticationClient commuticationClient;

    @Value("${web.portal.endpoint}")
    private String webPortalEndpoint;

    @Transactional
    @Override
    public ResponseBodyModel<String> createUser(UserRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            String userId = principalService.getUserId();
            if (!utilService.isEmailValid(request.getEmail())){
                response.setOperationError(ERROR_CODE_BUSINESS, INVALID_EMAIL_FORMAT_MSG, null);
                return response;
            }
            //check duplicate email
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_DUPLICATE, null);
                return response;
            }

            if (!roleRepository.existsById(request.getRoleId())) {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
                return response;
            }

            //create user
            String newUserId = UUID.randomUUID().toString();
            String activationToken = utilService.generateToken();
            userRepository.saveAndFlush(UserModel.builder()
                    .userId(newUserId)
                    .email(request.getEmail())
                    .roleId(request.getRoleId())
                    .activationToken(activationToken)
                    .createBy(userId)
                    .modifyBy(userId)
                    .build());

            //create user access
            String pwd = utilService.generatePassword(8);
            String wrapPwd = utilService.textValueEncAndDec(newUserId,
                    pwd,
                    ENCRYPT);
            userAccessRepository.saveAndFlush(UserAccessModel
                    .builder()
                    .userId(newUserId)
                    .password(wrapPwd)
                    .build());

            log.info(String.format("Created user with password {%s}, {%s}", pwd, activationToken));

            //send email
            Map<String, Object> variables = new HashMap<>();
            variables.put("password", pwd);
            variables.put("email", request.getEmail());
            variables.put("link", webPortalEndpoint + "/auth/activate?token=" + activationToken);
            commuticationClient.sendEmail(EmailRequest.builder()
                            .recipientEmail(request.getEmail())
                            .templateName("activate_account")
                            .variables(variables)
                    .build());

            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
        } catch (Exception ex) {
            log.error("Error creating user: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> updateUser(UserRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try{
            String userId = principalService.getUserId();
            Optional<UserModel> userModel = userRepository.findByUserIdAndStatus(request.getUserId(), true);
            if (userModel.isPresent()) {
                userModel.get().setRoleId(request.getRoleId());
                userModel.get().setModifyBy(userId);
                userModel.get().setModifyDate(new Timestamp(System.currentTimeMillis()));
                userRepository.saveAndFlush(userModel.get());
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
            }else {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
            }
        }catch (Exception ex){
            log.error("Error updating user: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }


    @Override
    public ResponseBodyModel<UserResponse> getUserById(String userId) {
        ResponseBodyModel<UserResponse> response = new ResponseBodyModel<>();
        try {
            Optional<UserModel> user = userRepository.findById(userId);
            if (user.isPresent()) {
                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                UserResponse result = objectMapper.convertValue(user.get(), UserResponse.class);
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS, result);

            } else {
                response.setOperationError(ERROR_CODE_DATA_NOT_FOUND, DATA_NOT_FOUND, null);
            }
        } catch (Exception ex) {
            log.error("Error getting user", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> changeUserStatus(String userId, boolean status) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<UserModel> userModel = userRepository.findById(userId);
            if (userModel.isPresent()) {
                userModel.get().setStatus(status);
                userModel.get().setModifyBy(principalService.getUserId());
                userModel.get().setModifyDate(new Timestamp(System.currentTimeMillis()));
                userRepository.saveAndFlush(userModel.get());
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
            } else {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_NOT_FOUND, null);
            }

        } catch (Exception ex) {
            log.error("Error changing user status", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }
}
