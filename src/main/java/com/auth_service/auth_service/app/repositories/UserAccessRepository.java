package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.UserAccessModel;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserAccessRepository extends JpaRepository<UserAccessModel, String> {


}
