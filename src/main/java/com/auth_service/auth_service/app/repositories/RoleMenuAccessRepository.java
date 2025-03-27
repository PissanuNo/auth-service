package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.RoleMenuAccessModel;
import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoleMenuAccessRepository extends JpaRepository<RoleMenuAccessModel, String> {

    Optional<RoleMenuAccessModel> findByRoleIdAndMenuId(String roleId, String menuId);

    void deleteByRoleId(String roleId);

    @Modifying
    @Query("DELETE FROM RoleMenuAccessModel r WHERE r.accessId IN :ids")
    void deleteByAccessIds(@Param("ids") List<String> ids);


}
