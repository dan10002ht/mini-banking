package com.minibanking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Session entity for managing user sessions
 */
@Entity
@Table(name = "sessions")
public class Session {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "session_id")
    private UUID sessionId;
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "device_fingerprint", length = 255)
    private String deviceFingerprint;
    
    @Column(name = "token_hash", length = 255, nullable = false)
    private String tokenHash;
    
    @Column(name = "refresh_token_hash", length = 255)
    private String refreshTokenHash;
    
    @Column(name = "ip_address", length = 45)
    private String ipAddress;
    
    @Column(name = "user_agent", length = 500)
    private String userAgent;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
    
    @Column(name = "is_temporary", nullable = false)
    private Boolean isTemporary = false; // For 2FA sessions
    
    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;
    
    @Column(name = "last_activity")
    private LocalDateTime lastActivity;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    // Constructors
    public Session() {}
    
    public Session(UUID userId, String deviceFingerprint, String tokenHash, LocalDateTime expiresAt) {
        this.userId = userId;
        this.deviceFingerprint = deviceFingerprint;
        this.tokenHash = tokenHash;
        this.expiresAt = expiresAt;
        this.lastActivity = LocalDateTime.now();
    }
    
    // Business Methods
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiresAt);
    }
    
    public void refreshActivity() {
        this.lastActivity = LocalDateTime.now();
    }
    
    public void invalidate() {
        this.isActive = false;
    }
    
    public void extendExpiration(LocalDateTime newExpiresAt) {
        this.expiresAt = newExpiresAt;
    }
    
    public boolean isTemporarySession() {
        return this.isTemporary;
    }
    
    public void markAsTemporary() {
        this.isTemporary = true;
        // Temporary sessions expire in 10 minutes
        this.expiresAt = LocalDateTime.now().plusMinutes(10);
    }
    
    // Getters and Setters
    public UUID getSessionId() { return sessionId; }
    public void setSessionId(UUID sessionId) { this.sessionId = sessionId; }
    
    public UUID getUserId() { return userId; }
    public void setUserId(UUID userId) { this.userId = userId; }
    
    public String getDeviceFingerprint() { return deviceFingerprint; }
    public void setDeviceFingerprint(String deviceFingerprint) { this.deviceFingerprint = deviceFingerprint; }
    
    public String getTokenHash() { return tokenHash; }
    public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }
    
    public String getRefreshTokenHash() { return refreshTokenHash; }
    public void setRefreshTokenHash(String refreshTokenHash) { this.refreshTokenHash = refreshTokenHash; }
    
    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }
    
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    
    public Boolean getIsActive() { return isActive; }
    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
    
    public Boolean getIsTemporary() { return isTemporary; }
    public void setIsTemporary(Boolean isTemporary) { this.isTemporary = isTemporary; }
    
    public LocalDateTime getExpiresAt() { return expiresAt; }
    public void setExpiresAt(LocalDateTime expiresAt) { this.expiresAt = expiresAt; }
    
    public LocalDateTime getLastActivity() { return lastActivity; }
    public void setLastActivity(LocalDateTime lastActivity) { this.lastActivity = lastActivity; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    @Override
    public String toString() {
        return "Session{" +
                "sessionId=" + sessionId +
                ", userId=" + userId +
                ", deviceFingerprint='" + deviceFingerprint + '\'' +
                ", isActive=" + isActive +
                ", isTemporary=" + isTemporary +
                ", expiresAt=" + expiresAt +
                ", lastActivity=" + lastActivity +
                '}';
    }
}
