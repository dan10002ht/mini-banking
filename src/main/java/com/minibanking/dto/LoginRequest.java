package com.minibanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Login Request DTO
 */
public class LoginRequest {
    
    @NotBlank(message = "Username is required")
    private String username;
    
    private String password; // Required for untrusted devices
    
    private String pin; // Required for trusted devices
    
    @NotBlank(message = "Device fingerprint is required")
    private String deviceFingerprint;
    
    private String deviceName;
    private String deviceType;
    private String osName;
    private String osVersion;
    private String browserName;
    private String browserVersion;
    private String ipAddress;
    private String userAgent;
    
    // Constructors
    public LoginRequest() {}
    
    public LoginRequest(String username, String password, String deviceFingerprint) {
        this.username = username;
        this.password = password;
        this.deviceFingerprint = deviceFingerprint;
    }
    
    // Getters and Setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    
    public String getPin() { return pin; }
    public void setPin(String pin) { this.pin = pin; }
    
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    
    public String getDeviceName() { return deviceName; }
    public void setDeviceName(String deviceName) { this.deviceName = deviceName; }
    
    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }
    
    public String getOsName() { return osName; }
    public void setOsName(String osName) { this.osName = osName; }
    
    public String getOsVersion() { return osVersion; }
    public void setOsVersion(String osVersion) { this.osVersion = osVersion; }
    
    public String getBrowserName() { return browserName; }
    public void setBrowserName(String browserName) { this.browserName = browserName; }
    
    public String getBrowserVersion() { return browserVersion; }
    public void setBrowserVersion(String browserVersion) { this.browserVersion = browserVersion; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    @Override
    public String toString() {
        return "LoginRequest{" +
                "username='" + username + '\'' +
                ", deviceFingerprint='" + deviceFingerprint + '\'' +
                ", deviceName='" + deviceName + '\'' +
                ", deviceType='" + deviceType + '\'' +
                '}';
    }
}
