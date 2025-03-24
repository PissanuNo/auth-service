package com.auth_service.auth_service.app.services;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public interface LoggingService {
    void displayReq(HttpServletRequest request, Object body);
    void displayResp(HttpServletRequest request, HttpServletResponse response, Object body);
    void logStamp(String code, String message, Class<?> logClass);
}
