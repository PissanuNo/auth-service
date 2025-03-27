package com.auth_service.auth_service.app.model.dbs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_menus")
public class MenuModel {
    @Id
    private String menuId;

    private String menuNameEn;

    private String menuNameTh;

    private String parentMenuId;

    private Integer menuOrder;

    private Boolean isActive;

    private String iconMenuId;

    private String menuCode;

}
