package com.minibanking.security.otp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * OTP Service for 2FA authentication
 */
@Service
public class OTPService {
    
    private static final Logger logger = LoggerFactory.getLogger(OTPService.class);
    
    @Value("${otp.length:6}")
    private int otpLength;
    
    @Value("${otp.expiration:300}") // 5 minutes in seconds
    private int otpExpirationSeconds;
    
    @Value("${otp.max-attempts:3}")
    private int maxAttempts;
    
    // In-memory storage for OTPs (in production, use Redis)
    private final Map<String, OTPData> otpStorage = new ConcurrentHashMap<>();
    
    /**
     * Generate OTP for user
     */
    public String generateOTP(UUID userId, String phoneNumber) {
        String otp = generateRandomOTP();
        String otpId = UUID.randomUUID().toString();
        
        OTPData otpData = new OTPData();
        otpData.setOtpId(otpId);
        otpData.setUserId(userId);
        otpData.setPhoneNumber(phoneNumber);
        otpData.setOtp(otp);
        otpData.setCreatedAt(LocalDateTime.now());
        otpData.setExpiresAt(LocalDateTime.now().plusSeconds(otpExpirationSeconds));
        otpData.setAttempts(0);
        otpData.setIsUsed(false);
        
        otpStorage.put(otpId, otpData);
        
        logger.info("OTP generated for user: {} with ID: {}", userId, otpId);
        return otpId;
    }
    
    /**
     * Verify OTP
     */
    public OTPVerificationResult verifyOTP(String otpId, String otp) {
        OTPData otpData = otpStorage.get(otpId);
        
        if (otpData == null) {
            return OTPVerificationResult.invalid("OTP not found");
        }
        
        if (otpData.getIsUsed()) {
            return OTPVerificationResult.invalid("OTP already used");
        }
        
        if (otpData.getExpiresAt().isBefore(LocalDateTime.now())) {
            otpStorage.remove(otpId);
            return OTPVerificationResult.invalid("OTP expired");
        }
        
        if (otpData.getAttempts() >= maxAttempts) {
            otpStorage.remove(otpId);
            return OTPVerificationResult.invalid("Maximum attempts exceeded");
        }
        
        otpData.setAttempts(otpData.getAttempts() + 1);
        
        if (!otpData.getOtp().equals(otp)) {
            if (otpData.getAttempts() >= maxAttempts) {
                otpStorage.remove(otpId);
                return OTPVerificationResult.invalid("Maximum attempts exceeded");
            }
            return OTPVerificationResult.invalid("Invalid OTP");
        }
        
        // OTP is valid
        otpData.setIsUsed(true);
        otpStorage.remove(otpId);
        
        logger.info("OTP verified successfully for user: {}", otpData.getUserId());
        return OTPVerificationResult.valid(otpData.getUserId(), otpData.getPhoneNumber());
    }
    
    /**
     * Check if OTP exists and is valid
     */
    public boolean isOTPValid(String otpId) {
        OTPData otpData = otpStorage.get(otpId);
        return otpData != null && 
               !otpData.getIsUsed() && 
               otpData.getExpiresAt().isAfter(LocalDateTime.now()) &&
               otpData.getAttempts() < maxAttempts;
    }
    
    /**
     * Get OTP data
     */
    public OTPData getOTPData(String otpId) {
        return otpStorage.get(otpId);
    }
    
    /**
     * Cleanup expired OTPs
     */
    public void cleanupExpiredOTPs() {
        LocalDateTime now = LocalDateTime.now();
        otpStorage.entrySet().removeIf(entry -> 
            entry.getValue().getExpiresAt().isBefore(now) || 
            entry.getValue().getIsUsed()
        );
        logger.info("Cleaned up expired OTPs");
    }
    
    /**
     * Generate random OTP
     */
    private String generateRandomOTP() {
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        
        for (int i = 0; i < otpLength; i++) {
            otp.append(random.nextInt(10));
        }
        
        return otp.toString();
    }
    
    /**
     * OTP Data class
     */
    public static class OTPData {
        private String otpId;
        private UUID userId;
        private String phoneNumber;
        private String otp;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
        private int attempts;
        private boolean isUsed;
        
        // Getters and Setters
        public String getOtpId() { return otpId; }
        public void setOtpId(String otpId) { this.otpId = otpId; }
        
        public UUID getUserId() { return userId; }
        public void setUserId(UUID userId) { this.userId = userId; }
        
        public String getPhoneNumber() { return phoneNumber; }
        public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }
        
        public String getOtp() { return otp; }
        public void setOtp(String otp) { this.otp = otp; }
        
        public LocalDateTime getCreatedAt() { return createdAt; }
        public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
        
        public LocalDateTime getExpiresAt() { return expiresAt; }
        public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
        
        public int getAttempts() { return attempts; }
        public void setAttempts(int attempts) { this.attempts = attempts; }
        
        public boolean getIsUsed() { return isUsed; }
        public void setIsUsed(boolean isUsed) { this.isUsed = isUsed; }
    }
    
    /**
     * OTP Verification Result
     */
    public static class OTPVerificationResult {
        private boolean valid;
        private String message;
        private UUID userId;
        private String phoneNumber;
        
        private OTPVerificationResult(boolean valid, String message, UUID userId, String phoneNumber) {
            this.valid = valid;
            this.message = message;
            this.userId = userId;
            this.phoneNumber = phoneNumber;
        }
        
        public static OTPVerificationResult valid(UUID userId, String phoneNumber) {
            return new OTPVerificationResult(true, "OTP verified successfully", userId, phoneNumber);
        }
        
        public static OTPVerificationResult invalid(String message) {
            return new OTPVerificationResult(false, message, null, null);
        }
        
        // Getters
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public UUID getUserId() { return userId; }
        public String getPhoneNumber() { return phoneNumber; }
    }
}
