package com.hyundai.autoever.security.assignment.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;

@Slf4j
@Component
public class CryptoUtil {
    @Value("${app.security.encryption.key:MySecretKey16Byte}")
    private String secretKey;
    
    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            String adjustedKey = adjustKeyLength(secretKey);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(adjustedKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encrypted);
        } catch (Exception e) {
            log.error("암호화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("암호화 처리 중 오류가 발생했습니다", e);
        }
    }
    
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            String adjustedKey = adjustKeyLength(secretKey);
            
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            SecretKeySpec keySpec = new SecretKeySpec(adjustedKey.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            
            byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decrypted, StandardCharsets.UTF_8);
        } catch (Exception e) {
            log.error("복호화 실패: {}", e.getMessage(), e);
            throw new RuntimeException("복호화 처리 중 오류가 발생했습니다", e);
        }
    }
    
    public String hash(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }
        
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("해시 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("해시 처리 중 오류가 발생했습니다", e);
        }
    }
    
    public String maskResidentNumber(String residentNumber) {
        if (residentNumber == null || residentNumber.length() != 13) {
            return residentNumber;
        }
        return residentNumber.substring(0, 6) + "-" + 
               residentNumber.substring(6, 7) + "******";
    }
    
    private String adjustKeyLength(String key) {
        if (key.length() == 16) {
            return key;
        } else if (key.length() < 16) {
            return String.format("%-16s", key).replace(' ', '0');
        } else {
            return key.substring(0, 16);
        }
    }
}