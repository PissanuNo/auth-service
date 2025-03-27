package com.auth_service.auth_service.app.controller;

import com.auth_service.auth_service.app.model.dto.menu.MenuResponse;
import com.auth_service.auth_service.app.services.MenuService;
import com.auth_service.auth_service.core.model.ResponseBodyModel;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Controller
@RequestMapping("v1")
@RequiredArgsConstructor
public class MenuController {

    private final MenuService menuService;

    @GetMapping(
            path = "/s/menu",
            produces = APPLICATION_JSON_VALUE
    )
    public ResponseEntity<ResponseBodyModel<List<MenuResponse>>> getAllMenu() {
        ResponseBodyModel<List<MenuResponse>> response = menuService.getAllMenu();
        return ResponseEntity.ok(response);
    }
}
