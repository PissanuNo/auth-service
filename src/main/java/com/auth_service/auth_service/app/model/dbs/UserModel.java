package com.auth_service.auth_service.app.model.dbs;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "portal_user")
public class UserModel {
    @Id
    private String userId;

    private String email;

    private String roleId;

    @Builder.Default
    private Boolean status = false;

    private String activationToken; // for activate account

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

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "userId", insertable = false, updatable = false)
    private  UserAccessModel userAccessModel;

}
