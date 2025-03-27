package com.auth_service.auth_service.app.model.dbs;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_role_corporate")
public class RoleCorporateModel {
    @Id
    private String tranIds;

    private String roleId;

    private String corporateId;


}
