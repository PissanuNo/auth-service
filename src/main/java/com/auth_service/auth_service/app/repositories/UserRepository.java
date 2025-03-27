package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.UserModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserModel, String> {

    Optional<UserModel> findByEmail(String email);

    Optional<UserModel> findByEmailAndStatus(String email, Boolean status);

    Optional<UserModel> findByActivationToken(String activationToken);

    Optional<UserModel> findByUserIdAndStatus(String userId, Boolean status);
}
