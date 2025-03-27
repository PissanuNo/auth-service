package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.RoleCorporateModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RoleCorporateRepository extends JpaRepository<RoleCorporateModel, String> {

    List<RoleCorporateModel> findByRoleId(String roleId);

    void deleteByRoleId(String roleId);
}
