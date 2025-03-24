package com.auth_service.auth_service.core.security;

import com.auth_service.auth_service.core.model.JwtClaim;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;

public class AuthenFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Access-Control-Allow-Methods", "POST, GET, PUT, OPTIONS, DELETE");
        response.setHeader("Access-Control-Max-Age", "3600");
        response.setHeader("Access-Control-Allow-Headers",
                "X-PINGOTHER,Content-Type,X-Requested-With,accept,Origin,Access-Control-Request-Method,Access-Control-Request-Headers,Authorization");
        response.setHeader("Access-Control-Expose-Headers", "xsrf-token");

        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            try {
                String token = request.getHeader("Authorization");
                //  claims from JWT token (after Bearer)
                String jwtToken = token.split(" ")[1];
                // convert claimsString to Map
                Map<String, Object> claimsMap = new ObjectMapper().readValue(JwtHelper.decode(jwtToken).getClaims(), Map.class);

                // create and set JwtClaim
                JwtClaim jwtClaim = JwtClaim.builder()
                        .token(jwtToken)
                        .expire(Long.valueOf(claimsMap.get("exp").toString()))
                        .iat(Long.valueOf(claimsMap.get("iat").toString()))
                        .attrs(claimsMap)
                        .build();

                Authentication auth = new JwtAuthenticationToken(jwtClaim, token);
                SecurityContextHolder.getContext().setAuthentication(auth);
            } catch (Exception ex) {
                if (request.getHeader("Authorization") != null) { //if token exists & error then log token
                    logger.error("error set claim token for:" + request.getHeader("Authorization"), ex);
                }
            } finally {
                filterChain.doFilter(request, response);
            }
        }
    }
}
