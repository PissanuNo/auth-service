package com.auth_service.auth_service.core.service;

import io.jsonwebtoken.Claims;

import java.text.ParseException;
import java.util.Map;

public interface JwtUtilService {
    String enCode(Map<String, Object> mapData ,String type) throws ParseException;
    Claims deCode(String token) throws Exception;
}
