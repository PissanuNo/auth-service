package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.RoleModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoleRepository extends JpaRepository<RoleModel, String> {

    Boolean existsByRoleNameEnAndRoleNameTh(String roleNameEn, String roleNameTh);

}
