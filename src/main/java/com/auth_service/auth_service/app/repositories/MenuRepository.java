package com.auth_service.auth_service.app.repositories;

import com.auth_service.auth_service.app.model.dbs.MenuModel;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuModel, String> {

    List<MenuModel> findAllByIsActive(Boolean isActive, Sort sort);

    Boolean existsByMenuIdIn(List<String> menuIds);
}
