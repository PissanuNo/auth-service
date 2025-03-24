package com.auth_service.auth_service.core.security;


import com.auth_service.auth_service.core.model.JwtClaim;
import com.google.common.base.Strings;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Arrays;

public class JwtAuthenticationToken extends AbstractAuthenticationToken {

    private static final long serialVersionUID = 6673630888624971991L;

    private JwtClaim claim;
    private String token;

    public JwtAuthenticationToken(JwtClaim claims, String token) {
        super(Arrays.asList());
        this.claim = claims;
        this.token = token;
    }

    @Override
    public Object getCredentials() {
        return token;
    }

    @Override
    public Object getPrincipal() {
        if (!Strings.isNullOrEmpty(claim.getToken())) {
            return claim;
        } else {
            return null;
        }
    }

}
