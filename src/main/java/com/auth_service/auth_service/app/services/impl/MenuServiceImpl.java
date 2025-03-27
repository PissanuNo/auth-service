package com.auth_service.auth_service.app.services.impl;

import com.auth_service.auth_service.app.model.dbs.MenuModel;
import com.auth_service.auth_service.app.model.dbs.RoleMenuAccessModel;
import com.auth_service.auth_service.app.model.dto.menu.MenuResponse;
import com.auth_service.auth_service.app.repositories.MenuRepository;
import com.auth_service.auth_service.app.repositories.RoleRepository;
import com.auth_service.auth_service.app.services.MenuService;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import com.auth_service.auth_service.core.service.PrincipalService;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.auth_service.auth_service.app.constants.Constant.ResponseCode.INTERNAL_SERVER_ERROR;
import static com.auth_service.auth_service.app.constants.Constant.ResponseCode.SUCCESS_CODE;
import static com.auth_service.auth_service.app.constants.Constant.ResponseMessage.INTERNAL_SERVER_ERROR_MSG;
import static com.auth_service.auth_service.app.constants.Constant.ResponseMessage.SUCCESS;


@Service
@RequiredArgsConstructor
public class MenuServiceImpl implements MenuService {
    Logger log = LoggerFactory.getLogger("MenuService");
    private final MenuRepository menuRepository;
    private final PrincipalService principalService;
    private final RoleRepository roleRepository;

    @Override
    public ResponseBodyModel<List<MenuResponse>> getAllMenu() {
        ResponseBodyModel<List<MenuResponse>> response = new ResponseBodyModel<>();
        try {
            String roleId = principalService.getRole();

            // get all menu (order by menu order)
            List<MenuModel> menuModels = menuRepository.findAllByIsActive(true ,
                    Sort.by(Sort.Direction.ASC, "menuOrder"));

            // get Permission Map by Role ID
            Map<String, Integer> permissionMaps = roleRepository.findById(roleId)
                    .map(role -> role.getRoleMenuAccessModel().stream()
                            .collect(Collectors.toMap(RoleMenuAccessModel::getMenuId, RoleMenuAccessModel::getAccessFlag))
                    )
                    .orElseGet(HashMap::new); // if no Role -> Map empty

            // filter by (permissionFlag != 0)
            List<MenuModel> filteredMenuModels = menuModels.stream()
                    .filter(menu -> {
                        Integer permissionFlag = permissionMaps.get(menu.getMenuId());
                        return permissionFlag != null && permissionFlag != 0;
                    })
                    .toList();

            List<MenuResponse> result = new ArrayList<>();
            for (MenuModel object : filteredMenuModels) {
                ObjectMapper mapper = new ObjectMapper();
                mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
                MenuResponse objMapper = mapper.convertValue(object, MenuResponse.class);
                result.add(objMapper);
            }

            result = buildRootMenu(result);

            response.setOperationSuccess(SUCCESS_CODE, SUCCESS, result);
        } catch (Exception ex) {
            log.error("Error while getting menu", ex);
            response.setOperationError(INTERNAL_SERVER_ERROR, INTERNAL_SERVER_ERROR_MSG, null);
        }
        return response;
    }


    private List<MenuResponse> buildRootMenu(List<MenuResponse> menuList) {
        List<MenuResponse> rootMenus = new ArrayList<>();

        for (MenuResponse menu : menuList) {
            if (menu.getParentMenuId() == null) {
                rootMenus.add(menu); // Menus without parentId are root menus.
            }
        }

        // Create a parent-child structure by searching for menu children later.
        for (MenuResponse rootMenu : rootMenus) {
            // Find the menu child of the rootMenu.
            List<MenuResponse> children = findChildren(rootMenu.getMenuId(), menuList);
            rootMenu.setChildren(children);

            // Search for menu items of menu items (recursive), if any.
            buildMenuHierarchy(children, menuList);
        }
        return rootMenus;
    }

    // Function to search for menu items of a menu
    private List<MenuResponse> findChildren(String parentId, List<MenuResponse> allMenus) {
        List<MenuResponse> children = new ArrayList<>();
        for (MenuResponse menu : allMenus) {
            if (parentId.equals(menu.getParentMenuId())) {
                children.add(menu);
            }
        }
        return children;
    }

    // Function used to search for children's children (recursive)
    private void buildMenuHierarchy(List<MenuResponse> children, List<MenuResponse> allMenus) {
        for (MenuResponse child : children) {
            List<MenuResponse> grandChildren = findChildren(child.getMenuId(), allMenus);
            if (!grandChildren.isEmpty()) {
                child.setChildren(grandChildren);
                buildMenuHierarchy(grandChildren, allMenus);
            }
        }
    }
}
