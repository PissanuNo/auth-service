package com.auth_service.auth_service.app.model.dto.menu;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MenuResponse {
    private String menuId;

    private String menuNameEn;

    private String menuNameTh;

    private String parentMenuId;

    private Integer menuOrder;

    private String iconMenuId;

    private String menuCode;

    private List<MenuResponse> children;

}
