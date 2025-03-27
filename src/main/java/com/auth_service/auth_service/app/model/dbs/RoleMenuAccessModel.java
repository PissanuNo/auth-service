package com.auth_service.auth_service.app.model.dbs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_role_menu_access")
public class RoleMenuAccessModel {
    @Id
    private String accessId;

    private String roleId;

    private String menuId;

    private Integer accessFlag;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "menuId", insertable = false, updatable = false)
    private MenuModel menu;
}
