package com.auth_service.auth_service.app.services.impl;


import com.auth_service.auth_service.app.services.UtilService;
import com.auth_service.auth_service.core.model.PageBodyModel;
import com.google.common.base.Strings;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import javax.crypto.*;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.auth_service.auth_service.app.constants.Constant.activities.DECRYPT;
import static com.auth_service.auth_service.app.constants.Constant.activities.ENCRYPT;

@Service
public class UtilServiceImpl implements UtilService {

    @Value("${text.iv.spec}")
    private String ivSpecKey;

    @Value("${text.salt.key}")
    private String saltKey;

    private static final String LOWERCASE = "abcdefghijklmnopqrstuvwxyz";
    private static final String UPPERCASE = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final String DIGITS = "0123456789";
    private static final String SPECIAL_CHARACTERS = "!@#$%^&*()-_=+<>?";
    private static final SecureRandom random = new SecureRandom();

    @Override
    public String textValueEncAndDec(String secretKey, String text, String action) throws InvalidKeySpecException
            , NoSuchAlgorithmException, NoSuchPaddingException
            , InvalidKeyException, InvalidAlgorithmParameterException
            , IllegalBlockSizeException, BadPaddingException {
        byte[] iv = Base64.getDecoder().decode(ivSpecKey);
        IvParameterSpec ivSpec = new IvParameterSpec(iv);
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(secretKey.toCharArray(), saltKey.getBytes(), 65536, 256);
        SecretKey tmp = factory.generateSecret(spec);
        SecretKeySpec generateSecretKey = new SecretKeySpec(tmp.getEncoded(), "AES");
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING");
        return switch (action) {
            case DECRYPT -> {
                cipher.init(Cipher.DECRYPT_MODE, generateSecretKey, ivSpec);
                yield new String(cipher.doFinal(Base64.getDecoder().decode(text)));
            }
            case ENCRYPT -> {
                cipher.init(Cipher.ENCRYPT_MODE, generateSecretKey, ivSpec);
                yield Base64.getEncoder()
                        .encodeToString(cipher.doFinal(text.getBytes(StandardCharsets.UTF_8)));
            }
            default -> null;
        };
    }

    @Override
    public boolean isEmailValid(@NotNull String email) {
        return Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$")
                .matcher(email)
                .matches();
    }

    @Override
    public boolean isTimeExceeded(Date givenTime) {
        return givenTime.before(new Date());
    }


    @Override
    public Pageable pageBodyconvertToPageable(PageBodyModel pageBodyModel) {
        return PageRequest.of(
                Strings.isNullOrEmpty(pageBodyModel.getPage()) ? 0 : Integer.parseInt(pageBodyModel.getPage()),
                Strings.isNullOrEmpty(pageBodyModel.getPageSize()) ? 10 : Integer.parseInt(pageBodyModel.getPageSize()),
                Sort.by(Strings.isNullOrEmpty(pageBodyModel.getSortDirection())
                                ? Sort.Direction.ASC
                                : Sort.Direction.valueOf(pageBodyModel.getSortDirection()),
                        Strings.isNullOrEmpty(pageBodyModel.getSortBy())
                                ? ""
                                : pageBodyModel.getSortBy())
        );
    }

    @Override
    public PageBodyModel pageableConvertToPageBodyModel(Pageable pageable, long totalElements, int totalPages) {
        return PageBodyModel
                .builder()
                .page(String.valueOf(pageable.getPageNumber()))
                .pageSize(String.valueOf(pageable.getPageSize()))
                .total(String.valueOf(totalElements))
                .totalPage(String.valueOf(totalPages))
                .sortDirection(pageable.getSort().iterator().next().getDirection().name())
                .sortBy(pageable.getSort().iterator().next().getProperty())
                .build();
    }

    @Override
    public String generatePassword(int length) {
        String allCharacters = LOWERCASE + UPPERCASE + DIGITS + SPECIAL_CHARACTERS;
        SecureRandom rd = new SecureRandom();
        StringBuilder password = new StringBuilder(length);

        // Ensure that the password contains at least one character from each category
        password.append(LOWERCASE.charAt(rd.nextInt(LOWERCASE.length())));
        password.append(UPPERCASE.charAt(rd.nextInt(UPPERCASE.length())));
        password.append(DIGITS.charAt(rd.nextInt(DIGITS.length())));
        password.append(SPECIAL_CHARACTERS.charAt(rd.nextInt(SPECIAL_CHARACTERS.length())));

        // Fill the remaining length with secureRandom characters
        for (int i = password.length(); i < length; i++) {
            password.append(allCharacters.charAt(rd.nextInt(allCharacters.length())));
        }

        // Shuffle the password to ensure randomness
        StringBuilder shuffledPassword = new StringBuilder(password.length());
        while (!password.isEmpty()) {
            int randomIndex = rd.nextInt(password.length());
            shuffledPassword.append(password.charAt(randomIndex));
            password.deleteCharAt(randomIndex);
        }

        return shuffledPassword.toString();
    }

    @Override
    public boolean isValidPassword(String password) {
        // create Pattern and Matcher
        Pattern pattern = Pattern.compile("^(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
        Matcher matcher = pattern.matcher(password);

        // check password match regex
        return matcher.matches();
    }

    @Override
    public String generateToken() {
        byte[] token = new byte[24]; // create token size 24 bytes
        random.nextBytes(token);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(token); // convert to Base64
    }

}
