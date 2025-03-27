package com.auth_service.auth_service.app.model.dbs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_role")
public class RoleModel {
    @Id
    private String roleId;

    private String roleNameEn;

    private String roleNameTh;

    @Column(columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    @Builder.Default
    private Date createDate = new Timestamp(System.currentTimeMillis());

    private String createBy;

    @Column(columnDefinition = "DATETIME")
    @Temporal(TemporalType.TIMESTAMP)
    @Builder.Default
    private Date modifyDate = new Timestamp(System.currentTimeMillis());

    private String modifyBy;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "roleId", insertable = false, updatable = false)
    private List<RoleMenuAccessModel> roleMenuAccessModel;


    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "roleId", insertable = false, updatable = false)
    private List<RoleCorporateModel> roleCorporateModel;


}
