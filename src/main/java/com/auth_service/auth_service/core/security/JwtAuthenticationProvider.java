package com.auth_service.auth_service.core.security;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationProvider implements AuthenticationProvider {

	@Override
	public Authentication authenticate(Authentication authentication) throws AuthenticationException {
		AbstractAuthenticationToken jwtAuthentication = (AbstractAuthenticationToken) authentication;
		return jwtAuthentication;
	}

	@Override
	public boolean supports(Class<?> authentication) {
		return AbstractAuthenticationToken.class.isAssignableFrom(authentication);
	}
	
}
