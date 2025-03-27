package com.auth_service.auth_service.core.utils;

import com.auth_service.auth_service.core.service.JwtUtilService;
import com.azure.security.keyvault.secrets.SecretClient;

import io.jsonwebtoken.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static com.auth_service.auth_service.core.constant.Constant.JwtProperties.REFRESH_EXPIRATION;
import static com.auth_service.auth_service.core.constant.Constant.JwtProperties.TOKEN_EXPIRATION;


@Service
public class JwtUtilServiceImpl implements JwtUtilService {

    private static final Logger log = LoggerFactory.getLogger(JwtUtilServiceImpl.class);

    @Value("${keyvault.public.key}")
    private String publicKeyName;

    @Value("${keyvault.private.key}")
    private String privateKeyName;

    private SecretClient secretClient;

    @Autowired
    public void keyVaultService(SecretClient secretClient) {
        this.secretClient = secretClient;
    }

    public String getSecretValue(String secretName) {
        return secretClient.getSecret(secretName).getValue();
    }

    @Override
    public String enCode(Map<String, Object> mapData, String type) {
        String token = null;
        int expire = switch (type) {
            case "access" -> TOKEN_EXPIRATION;
            case "refresh" -> REFRESH_EXPIRATION;
            default -> TOKEN_EXPIRATION;
        };
        Date exp = KeyUtil.getExpirationTime(expire);
        Map<String, Object> claims = new HashMap<>(mapData);
        try {
            String base64PrivateKey = getSecretValue(privateKeyName);
            // convert Base64 to byte array
            byte[] decodedKey = Base64.getDecoder().decode(base64PrivateKey);

            // convert byte array to RSAPrivateKey
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPrivateKey privateKey = (RSAPrivateKey) keyFactory.generatePrivate(keySpec);
            token = Jwts.builder()
                    .claims(claims)
                    .issuedAt(new Date(System.currentTimeMillis()))
                    .expiration(exp)
                    .signWith(privateKey)
                    .compact();
        } catch (Exception ex) {
            log.error("ERROR JWT encode message : {}", ex.getMessage());
        }
        return token;
    }

    @Override
    public Claims deCode(String token) throws NoSuchAlgorithmException, InvalidKeySpecException {
        Claims claims;
        try {
            String publicKeyString = getSecretValue(publicKeyName);
            // if public key is Base64 encoded, then decode
            byte[] decodedKey = Base64.getDecoder().decode(publicKeyString);

            // convert decoded key to RSAPublicKey
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            RSAPublicKey publicKey = (RSAPublicKey) keyFactory.generatePublic(keySpec);

            claims = Jwts.parser()
                    .verifyWith(publicKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (ExpiredJwtException expired) {
            throw expired;
        } catch (MalformedJwtException malformedJwt) {
            throw malformedJwt;
        } catch (SignatureException signature) {
            throw signature;
        } catch (Exception e) {
            throw e;
        }
        return claims;
    }
}
