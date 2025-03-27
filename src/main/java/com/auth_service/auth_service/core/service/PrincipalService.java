package com.auth_service.auth_service.core.service;


import com.auth_service.auth_service.core.model.JwtClaim;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class PrincipalService {

    public String getRole() {
        Object role = getClaimAttr("role");
        return role == null ? "no-role" : role.toString();
    }

    public String getUserId() {
        Object userId = getClaimAttr("userId");
        return userId == null ? "no-user" : userId.toString();
    }

    public String getUserName() { // if no username claims return userId
        Object userName = getClaimAttr("userName");
        return userName == null ? getUserId() : userName.toString();
    }

    public Map<String, Object> getClaimAttrs() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null ? new HashMap<>() :
                ((JwtClaim) authentication.getPrincipal()).getAttrs();
    }

    public Object getClaimAttr(String attrName) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication == null || authentication.getPrincipal().equals("anonymousUser") ? null :
                ((JwtClaim) authentication.getPrincipal()).getAttrs() == null ? null : ((JwtClaim) authentication.getPrincipal()).getAttrs().getOrDefault(attrName, null);
    }

}

