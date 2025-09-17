package com.minibanking.dto;

import com.minibanking.entity.Customer;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Login Response DTO
 */
public class LoginResponse {
    
    private String status;
    private String message;
    private String accessToken;
    private String refreshToken;
    private String tempToken; // For 2FA
    private String otpId; // For 2FA
    private Customer user;
    private boolean isNewDevice;
    private LocalDateTime expiresAt;
    private UUID userId;
    
    // Constructors
    public LoginResponse() {}
    
    public LoginResponse(String status, String message) {
        this.status = status;
        this.message = message;
    }
    
    // Static factory methods
    public static LoginResponse success(String accessToken, String refreshToken, Customer user, boolean isNewDevice) {
        LoginResponse response = new LoginResponse();
        response.status = "SUCCESS";
        response.message = "Login successful";
        response.accessToken = accessToken;
        response.refreshToken = refreshToken;
        response.user = user;
        response.isNewDevice = isNewDevice;
        response.expiresAt = LocalDateTime.now().plusHours(24);
        response.userId = user != null ? user.getCustomerId() : null;
        return response;
    }
    
    public static LoginResponse requires2FA(String otpId, String tempToken, Customer user) {
        LoginResponse response = new LoginResponse();
        response.status = "REQUIRES_2FA";
        response.message = "Please verify with OTP sent to your phone";
        response.otpId = otpId;
        response.tempToken = tempToken;
        response.user = user;
        response.expiresAt = LocalDateTime.now().plusMinutes(10);
        response.userId = user != null ? user.getCustomerId() : null;
        return response;
    }
    
    public static LoginResponse failed(String message) {
        LoginResponse response = new LoginResponse();
        response.status = "FAILED";
        response.message = message;
        return response;
    }
    
    // Getters and Setters
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    
    public String getTempToken() { return tempToken; }
    public void setTempToken(String tempToken) { this.tempToken = tempToken; }
    
    public String getOtpId() { return otpId; }
    public void setOtpId(String otpId) { this.otpId = otpId; }
    
    public Customer getUser() { return user; }
    public void setUser(Customer user) { this.user = user; }
    
    public boolean getIsNewDevice() { return isNewDevice; }
    public void setIsNewDevice(boolean isNewDevice) { this.isNewDevice = isNewDevice; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    // Helper methods
    public boolean isSuccess() {
        return "SUCCESS".equals(status);
    }
    
    public boolean requires2FA() {
        return "REQUIRES_2FA".equals(status);
    }
    
    public boolean isFailed() {
        return "FAILED".equals(status);
    }
    
    @Override
    public String toString() {
        return "LoginResponse{" +
                "status='" + status + '\'' +
                ", message='" + message + '\'' +
                ", isNewDevice=" + isNewDevice +
                ", expiresAt=" + expiresAt +
                '}';
    }
}
