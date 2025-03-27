package com.auth_service.auth_service.app.services;

import com.auth_service.auth_service.app.model.dto.menu.MenuResponse;
import com.auth_service.auth_service.core.model.ResponseBodyModel;

import java.util.List;

public interface MenuService {

    ResponseBodyModel<List<MenuResponse>> getAllMenu();
}
