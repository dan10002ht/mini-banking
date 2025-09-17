package com.minibanking.security.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Data Encryption Service for sensitive banking data
 */
@Service
public class DataEncryptionService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataEncryptionService.class);
    
    @Value("${encryption.algorithm:AES}")
    private String algorithm;
    
    @Value("${encryption.key-size:256}")
    private int keySize;
    
    @Value("${encryption.iv-length:12}")
    private int ivLength;
    
    @Value("${encryption.tag-length:128}")
    private int tagLength;
    
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    
    /**
     * Encrypt sensitive data
     */
    public String encrypt(String plainText) {
        if (plainText == null || plainText.isEmpty()) {
            return plainText;
        }
        
        try {
            // Generate random IV
            byte[] iv = new byte[ivLength];
            new SecureRandom().nextBytes(iv);
            
            // Generate secret key (in production, use proper key management)
            SecretKey secretKey = generateSecretKey();
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, parameterSpec);
            
            // Encrypt data
            byte[] encryptedData = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            
            // Combine IV and encrypted data
            byte[] encryptedWithIv = new byte[ivLength + encryptedData.length];
            System.arraycopy(iv, 0, encryptedWithIv, 0, ivLength);
            System.arraycopy(encryptedData, 0, encryptedWithIv, ivLength, encryptedData.length);
            
            // Return base64 encoded result
            return Base64.getEncoder().encodeToString(encryptedWithIv);
            
        } catch (Exception e) {
            logger.error("Error encrypting data", e);
            throw new RuntimeException("Encryption failed", e);
        }
    }
    
    /**
     * Decrypt sensitive data
     */
    public String decrypt(String encryptedText) {
        if (encryptedText == null || encryptedText.isEmpty()) {
            return encryptedText;
        }
        
        try {
            // Decode base64
            byte[] encryptedWithIv = Base64.getDecoder().decode(encryptedText);
            
            // Extract IV and encrypted data
            byte[] iv = new byte[ivLength];
            byte[] encryptedData = new byte[encryptedWithIv.length - ivLength];
            System.arraycopy(encryptedWithIv, 0, iv, 0, ivLength);
            System.arraycopy(encryptedWithIv, ivLength, encryptedData, 0, encryptedData.length);
            
            // Generate secret key (in production, use proper key management)
            SecretKey secretKey = generateSecretKey();
            
            // Initialize cipher
            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            GCMParameterSpec parameterSpec = new GCMParameterSpec(tagLength, iv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, parameterSpec);
            
            // Decrypt data
            byte[] decryptedData = cipher.doFinal(encryptedData);
            
            return new String(decryptedData, StandardCharsets.UTF_8);
            
        } catch (Exception e) {
            logger.error("Error decrypting data", e);
            throw new RuntimeException("Decryption failed", e);
        }
    }
    
    /**
     * Encrypt BigDecimal (for financial data)
     */
    public String encryptBigDecimal(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return encrypt(value.toPlainString());
    }
    
    /**
     * Decrypt BigDecimal (for financial data)
     */
    public BigDecimal decryptBigDecimal(String encryptedValue) {
        if (encryptedValue == null || encryptedValue.isEmpty()) {
            return null;
        }
        String decryptedValue = decrypt(encryptedValue);
        return new BigDecimal(decryptedValue);
    }
    
    /**
     * Check if data is encrypted
     */
    public boolean isEncrypted(String data) {
        if (data == null || data.isEmpty()) {
            return false;
        }
        
        try {
            // Try to decode as base64
            byte[] decoded = Base64.getDecoder().decode(data);
            // Check if it has minimum length for IV + encrypted data
            return decoded.length >= ivLength + 16; // Minimum encrypted data length
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Generate secret key (in production, use HSM or proper key management)
     */
    private SecretKey generateSecretKey() {
        try {
            // In production, this should be retrieved from HSM or secure key store
            String keyString = "MySecretKey123456789012345678901234567890"; // 32 bytes for AES-256
            byte[] keyBytes = keyString.getBytes(StandardCharsets.UTF_8);
            return new SecretKeySpec(keyBytes, "AES");
        } catch (Exception e) {
            logger.error("Error generating secret key", e);
            throw new RuntimeException("Key generation failed", e);
        }
    }
    
    /**
     * Generate random secret key (for testing)
     */
    public SecretKey generateRandomKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance("AES");
            keyGenerator.init(keySize);
            return keyGenerator.generateKey();
        } catch (Exception e) {
            logger.error("Error generating random key", e);
            throw new RuntimeException("Random key generation failed", e);
        }
    }
    
    /**
     * Get encryption info
     */
    public String getEncryptionInfo() {
        return String.format("Algorithm: %s, Key Size: %d bits, IV Length: %d bytes, Tag Length: %d bits",
                algorithm, keySize, ivLength, tagLength);
    }
}