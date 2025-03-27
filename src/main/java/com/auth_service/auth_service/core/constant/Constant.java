package com.auth_service.auth_service.core.constant;

public class Constant {
    public enum ResponseCode{
        ;
        public static final String AUTHORIZATION = "Authorization";

    }

    public enum JwtProperties{
        ;
        public static final String ALGORITHM_RSA = "RSA";
        public static final int TOKEN_EXPIRATION = 3600;
        public static final int REFRESH_EXPIRATION = 10080;
    }
}
