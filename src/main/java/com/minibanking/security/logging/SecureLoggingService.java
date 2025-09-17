package com.minibanking.security.logging;

import com.minibanking.security.masking.DataMaskingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Secure Logging Service for sensitive data
 */
@Service
public class SecureLoggingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureLoggingService.class);
    
    @Autowired
    private DataMaskingService dataMaskingService;
    
    /**
     * Log transfer operation securely
     */
    public void logTransfer(String fromAccount, String toAccount, BigDecimal amount, String description, String status) {
        String maskedFromAccount = dataMaskingService.maskAccountNumber(fromAccount);
        String maskedToAccount = dataMaskingService.maskAccountNumber(toAccount);
        String maskedAmount = dataMaskingService.maskAmount(amount);
        String maskedDescription = dataMaskingService.maskDescription(description);
        
        logger.info("Transfer {}: {} -> {}, Amount: {}, Desc: {}", 
                   status, maskedFromAccount, maskedToAccount, maskedAmount, maskedDescription);
    }
    
    /**
     * Log customer operation securely
     */
    public void logCustomerOperation(String operation, String firstName, String lastName, String email, String phone) {
        String maskedName = dataMaskingService.maskCustomerName(firstName, lastName);
        String maskedEmail = dataMaskingService.maskEmail(email);
        String maskedPhone = dataMaskingService.maskPhone(phone);
        
        logger.info("Customer {}: Name: {}, Email: {}, Phone: {}", 
                   operation, maskedName, maskedEmail, maskedPhone);
    }
    
    /**
     * Log account operation securely
     */
    public void logAccountOperation(String operation, String accountNumber, BigDecimal balance, String accountType) {
        String maskedAccount = dataMaskingService.maskAccountNumber(accountNumber);
        String maskedBalance = dataMaskingService.maskAmount(balance);
        
        logger.info("Account {}: Number: {}, Balance: {}, Type: {}", 
                   operation, maskedAccount, maskedBalance, accountType);
    }
    
    /**
     * Log authentication securely
     */
    public void logAuthentication(String username, String deviceFingerprint, String status, String ipAddress) {
        String maskedUsername = maskUsername(username);
        String maskedDevice = maskDeviceFingerprint(deviceFingerprint);
        String maskedIp = maskIpAddress(ipAddress);
        
        logger.info("Authentication {}: User: {}, Device: {}, IP: {}", 
                   status, maskedUsername, maskedDevice, maskedIp);
    }
    
    /**
     * Log error securely
     */
    public void logError(String operation, String errorMessage, String... sensitiveData) {
        String maskedMessage = dataMaskingService.maskLogMessage(errorMessage);
        
        if (sensitiveData.length > 0) {
            String maskedSensitiveData = String.join(", ", sensitiveData);
            maskedSensitiveData = dataMaskingService.maskLogMessage(maskedSensitiveData);
            logger.error("Error in {}: {} - Sensitive Data: {}", operation, maskedMessage, maskedSensitiveData);
        } else {
            logger.error("Error in {}: {}", operation, maskedMessage);
        }
    }
    
    /**
     * Log security event
     */
    public void logSecurityEvent(String event, String userId, String details) {
        String maskedUserId = maskUserId(userId);
        String maskedDetails = dataMaskingService.maskLogMessage(details);
        
        logger.warn("Security Event: {} - User: {}, Details: {}", event, maskedUserId, maskedDetails);
    }
    
    /**
     * Mask username
     */
    private String maskUsername(String username) {
        if (username == null || username.length() <= 2) {
            return "****";
        }
        return username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1);
    }
    
    /**
     * Mask device fingerprint
     */
    private String maskDeviceFingerprint(String deviceFingerprint) {
        if (deviceFingerprint == null || deviceFingerprint.length() <= 8) {
            return "****";
        }
        return deviceFingerprint.substring(0, 4) + "****" + deviceFingerprint.substring(deviceFingerprint.length() - 4);
    }
    
    /**
     * Mask IP address
     */
    private String maskIpAddress(String ipAddress) {
        if (ipAddress == null || !ipAddress.contains(".")) {
            return "****";
        }
        String[] parts = ipAddress.split("\\.");
        if (parts.length == 4) {
            return parts[0] + "." + parts[1] + ".***.***";
        }
        return "****";
    }
    
    /**
     * Mask user ID
     */
    private String maskUserId(String userId) {
        if (userId == null || userId.length() <= 8) {
            return "****";
        }
        return userId.substring(0, 4) + "****" + userId.substring(userId.length() - 4);
    }
}
