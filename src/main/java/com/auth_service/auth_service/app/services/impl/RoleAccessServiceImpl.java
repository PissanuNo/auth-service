package com.auth_service.auth_service.app.services.impl;

import com.auth_service.auth_service.app.model.dbs.RoleCorporateModel;
import com.auth_service.auth_service.app.model.dbs.RoleMenuAccessModel;
import com.auth_service.auth_service.app.model.dbs.RoleModel;
import com.auth_service.auth_service.app.model.dto.role.MenuAccess;
import com.auth_service.auth_service.app.model.dto.role.RoleRequest;
import com.auth_service.auth_service.app.model.dto.role.RoleResponse;
import com.auth_service.auth_service.app.repositories.MenuRepository;
import com.auth_service.auth_service.app.repositories.RoleCorporateRepository;
import com.auth_service.auth_service.app.repositories.RoleMenuAccessRepository;
import com.auth_service.auth_service.app.repositories.RoleRepository;
import com.auth_service.auth_service.app.services.RoleAccessService;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import com.auth_service.auth_service.core.service.PrincipalService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

import static com.auth_service.auth_service.app.constants.Constant.ResponseCode.*;
import static com.auth_service.auth_service.app.constants.Constant.ResponseMessage.*;


@Service
@RequiredArgsConstructor
public class RoleAccessServiceImpl implements RoleAccessService {

    Logger log = LoggerFactory.getLogger("RoleAccessService");
    private final RoleRepository roleRepository;
    private final RoleCorporateRepository roleCorporateRepository;
    private final RoleMenuAccessRepository roleMenuAccessRepository;
    private final MenuRepository menuRepository;
    private final PrincipalService principalService;

    @Transactional
    @Override
    public ResponseBodyModel<String> createRole(RoleRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            //check duplicate role name
            if (existsRoleName(request.getRoleNameEn(), request.getRoleNameTh())) {
                response.setOperationError(ERROR_CODE_BUSINESS, DATA_DUPLICATE, null);
                return response;
            }

            //validate menu
            if (!validateMenu(request.getMenuAccessList())) {
                log.error("Error create role some menu not found");
                response.setOperationError(ERROR_CODE_DATA_NOT_FOUND, DATA_NOT_FOUND, null);
                return response;
            }
            String roleId = UUID.randomUUID().toString();
            String userId = principalService.getUserId();

            saveCorporateList(request.getCorporateList(), roleId);
            saveMenuAccess(request.getMenuAccessList(), roleId);

            roleRepository.saveAndFlush(RoleModel.builder()
                    .roleId(roleId)
                    .roleNameEn(request.getRoleNameEn())
                    .roleNameTh(request.getRoleNameTh())
                    .createBy(userId)
                    .modifyBy(userId)
                    .build());

            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
        } catch (Exception ex) {
            log.error("Error creating role: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    public boolean validateMenu(List<MenuAccess> menuAccess) {
        return menuRepository.existsByMenuIdIn(menuAccess.stream()
                .map(MenuAccess::getMenuId)
                .toList());
    }

    public void saveCorporateList(List<String> corporateIds, String roleId) {
        List<RoleCorporateModel> corporateModels = new ArrayList<>();
        corporateIds.forEach(c -> corporateModels.add(RoleCorporateModel.builder()
                .tranIds(UUID.randomUUID().toString())
                .corporateId(c)
                .roleId(roleId)
                .build()));
        roleCorporateRepository.saveAllAndFlush(corporateModels);
    }

    private void saveMenuAccess(List<MenuAccess> menuAccesses, String roleId) {
        List<RoleMenuAccessModel> menuAccessModels = new ArrayList<>();
        menuAccesses.forEach(m -> menuAccessModels.add(
                RoleMenuAccessModel.builder()
                        .accessId(UUID.randomUUID().toString())
                        .menuId(m.getMenuId())
                        .roleId(roleId)
                        .accessFlag(m.getAccessFlag())
                        .build()
        ));
        roleMenuAccessRepository.saveAllAndFlush(menuAccessModels);
    }


    @Override
    @Transactional
    public ResponseBodyModel<String> updateRole(RoleRequest request) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<RoleModel> roleModel = roleRepository.findById(request.getRoleId());
            if (roleModel.isPresent()) {

                if (roleModel.get().getRoleNameEn().contains("System Admin")) {
                    response.setOperationError(ERROR_CODE_BUSINESS, NO_PERMISSION_SYS_ADMIN, null);
                    return response;
                }

                //check duplicate name
                if (existsRoleName(request.getRoleNameEn(), request.getRoleNameTh())
                        && !request.getRoleNameTh().equals(roleModel.get().getRoleNameTh())
                        && !request.getRoleNameEn().equals(roleModel.get().getRoleNameEn())) {
                    response.setOperationError(ERROR_CODE_BUSINESS, DATA_DUPLICATE, null);
                    return response;
                }
                roleModel.get().setRoleNameEn(request.getRoleNameEn());
                roleModel.get().setRoleNameTh(request.getRoleNameTh());
                roleModel.get().setModifyBy(principalService.getUserId());
                roleModel.get().setModifyDate(new Timestamp(System.currentTimeMillis()));
                roleRepository.saveAndFlush(roleModel.get());

                //update role corporate
                updateCorporate(request.getCorporateList(),
                        roleModel.get().getRoleCorporateModel(),
                        roleModel.get().getRoleId());

                //update role access menu
                updateRoleAccessMenu(request.getMenuAccessList(),
                        roleModel.get().getRoleMenuAccessModel(),
                        roleModel.get().getRoleId());

                response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
            } else {
                response.setOperationError(ERROR_CODE_DATA_NOT_FOUND, DATA_NOT_FOUND, null);
            }
        } catch (Exception ex) {
            log.error("Error updating role: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Transactional
    public void updateCorporate(List<String> newCorporates, List<RoleCorporateModel> currentCorporates, String roleId) {
        Set<String> newCorporateIds = new HashSet<>(newCorporates);

        Set<String> currentCorporateIds = currentCorporates.stream()
                .map(RoleCorporateModel::getCorporateId)
                .collect(Collectors.toSet());

        // 1. Delete any corporates that do not exist in newCorporates (that are missing).
        List<String> corporateToDel = currentCorporates.stream()
                .filter(c -> !newCorporateIds.contains(c.getCorporateId()))
                .map(RoleCorporateModel::getTranIds)
                .toList();

        //2. Create a new corporate that does not exist in currentCorporates.
        List<String> corporateToAdd = currentCorporates.stream()
                .map(RoleCorporateModel::getCorporateId)
                .filter(corporateId -> !currentCorporateIds.contains(corporateId))
                .toList();

        if (!corporateToDel.isEmpty()) {
            roleCorporateRepository.deleteAllById(corporateToDel);
        }

        if (!corporateToAdd.isEmpty()) {
            corporateToAdd.forEach(c -> saveCorporateList(corporateToAdd, roleId));
        }
    }

    @Transactional
    public void updateRoleAccessMenu(List<MenuAccess> newMenu, List<RoleMenuAccessModel> currentMenu, String roleId) {
        Set<String> newMenus = newMenu.stream()
                .map(MenuAccess::getMenuId)
                .collect(Collectors.toSet());

        Set<String> currentMenus = currentMenu.stream()
                .map(RoleMenuAccessModel::getMenuId)
                .collect(Collectors.toSet());

        List<String> menuToDel = currentMenu.stream()
                .filter(menu -> !newMenus.contains(menu.getMenuId()))
                .map(RoleMenuAccessModel::getAccessId)
                .toList();

        List<MenuAccess> menuToAdd = newMenu.stream()
                .filter(m -> !currentMenus.contains(m.getMenuId()))
                .toList();

        if (!menuToDel.isEmpty()) {
           roleMenuAccessRepository.deleteByAccessIds(menuToDel);
        }

        if (!menuToAdd.isEmpty()) {
            menuToAdd.forEach(m -> saveMenuAccess(menuToAdd, roleId));
        }
    }

    private boolean existsRoleName(String roleNameEn, String roleNameTh) {
        return roleRepository.existsByRoleNameEnAndRoleNameTh(roleNameEn, roleNameTh);
    }

    @Transactional
    @Override
    public ResponseBodyModel<String> deleteRole(String roleId) {
        ResponseBodyModel<String> response = new ResponseBodyModel<>();
        try {
            Optional<RoleModel> roleModel = roleRepository.findById(roleId);
            if (roleModel.isPresent()) {
                if(roleModel.get().getRoleNameEn().contains("System Admin")){
                    response.setOperationError(ERROR_CODE_BUSINESS, NO_PERMISSION_SYS_ADMIN, null);
                    return response;
                }
                //TODO has user assigned to role

                //delete role by id , role corporate, role access menu
//                roleMenuAccessRepository.deleteByRoleId(request.getRoleId());
//                roleCorporateRepository.deleteByRoleId(request.getRoleId());
//                roleRepository.deleteById(request.getRoleId());
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS, null);
            } else {
                response.setOperationError(ERROR_CODE_DATA_NOT_FOUND, DATA_NOT_FOUND, null);
            }

        } catch (Exception ex) {
            log.error("Error deleting role: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

    @Override
    public ResponseBodyModel<RoleResponse> getRoleById(String roleId) {
        ResponseBodyModel<RoleResponse> response = new ResponseBodyModel<>();
        try {
            Optional<RoleModel> roleModel = roleRepository.findById(roleId);
            if (roleModel.isPresent()) {
                response.setOperationSuccess(SUCCESS_CODE, SUCCESS_CODE,
                        RoleResponse.builder()
                        .roleId(roleModel.get().getRoleId())
                        .roleNameEn(roleModel.get().getRoleNameEn())
                        .roleNameTh(roleModel.get().getRoleNameTh())
                        .corporateList(roleModel.get().getRoleCorporateModel()
                                .stream()
                                .map(RoleCorporateModel::getCorporateId)
                                .toList())
                        .menuAccessList(roleModel.get().getRoleMenuAccessModel().stream()
                                .map(m -> MenuAccess.builder()
                                        .accessId(m.getAccessId())
                                        .menuId(m.getMenuId())
                                        .accessFlag(m.getAccessFlag())
                                        .build())
                                .toList())
                        .build());
            } else {
                response.setOperationError(ERROR_CODE_DATA_NOT_FOUND, DATA_NOT_FOUND, null);
            }
        } catch (Exception ex) {
            log.error("Error getting role: ", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }

}
