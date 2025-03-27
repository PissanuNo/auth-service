package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.RoleCorporateModel;
import com.auth_service.auth_service.app.model.dbs.UserResetPasswordTokenModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserResetPasswordTokenRepository extends JpaRepository<UserResetPasswordTokenModel, String> {

    Optional<UserResetPasswordTokenModel> findTopByTokenOrderByCreateDateDesc(String token);


}
