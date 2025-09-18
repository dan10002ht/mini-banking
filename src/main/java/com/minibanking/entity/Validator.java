package com.minibanking.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Validator entity for Proof of Authority consensus
 */
@Entity
@Table(name = "validators")
public class Validator {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "validator_id")
    private UUID validatorId;

    @Column(name = "validator_name", unique = true, nullable = false, length = 100)
    private String validatorName;

    @Column(name = "public_key", length = 500)
    private String publicKey;

    @Column(name = "is_authorized", nullable = false)
    private Boolean isAuthorized = false;

    @Column(name = "priority", nullable = false)
    private Integer priority = 0;

    @Column(name = "last_block_time")
    private LocalDateTime lastBlockTime;

    @Column(name = "blocks_created", nullable = false)
    private Integer blocksCreated = 0;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "stake_amount")
    private Long stakeAmount = 0L; // For future PoS integration

    @Column(name = "node_url", length = 255)
    private String nodeUrl;

    @Column(name = "last_heartbeat")
    private LocalDateTime lastHeartbeat;

    @Column(name = "failed_attempts", nullable = false)
    private Integer failedAttempts = 0;

    @Column(name = "locked_until")
    private LocalDateTime lockedUntil;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // Constructors
    public Validator() {
    }

    public Validator(String validatorName, String publicKey, Integer priority) {
        this.validatorName = validatorName;
        this.publicKey = publicKey;
        this.priority = priority;
        this.isAuthorized = true;
        this.isActive = true;
    }

    // Business Methods
    public void incrementBlocksCreated() {
        this.blocksCreated++;
        this.lastBlockTime = LocalDateTime.now();
    }

    public void incrementFailedAttempts() {
        this.failedAttempts++;
        if (this.failedAttempts >= 3) {
            this.lockedUntil = LocalDateTime.now().plusMinutes(30);
        }
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.lockedUntil = null;
    }

    public boolean isLocked() {
        return lockedUntil != null && LocalDateTime.now().isBefore(lockedUntil);
    }

    public boolean canCreateBlock() {
        return isAuthorized && isActive && !isLocked();
    }

    public void updateHeartbeat() {
        this.lastHeartbeat = LocalDateTime.now();
    }

    public boolean isOnline() {
        if (lastHeartbeat == null)
            return false;
        return LocalDateTime.now().minusMinutes(5).isBefore(lastHeartbeat);
    }

    // Getters and Setters
    public UUID getValidatorId() {
        return validatorId;
    }

    public void setValidatorId(UUID validatorId) {
        this.validatorId = validatorId;
    }

    public String getValidatorName() {
        return validatorName;
    }

    public void setValidatorName(String validatorName) {
        this.validatorName = validatorName;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public Boolean getIsAuthorized() {
        return isAuthorized;
    }

    public void setIsAuthorized(Boolean isAuthorized) {
        this.isAuthorized = isAuthorized;
    }

    public Integer getPriority() {
        return priority;
    }

    public void setPriority(Integer priority) {
        this.priority = priority;
    }

    public LocalDateTime getLastBlockTime() {
        return lastBlockTime;
    }

    public void setLastBlockTime(LocalDateTime lastBlockTime) {
        this.lastBlockTime = lastBlockTime;
    }

    public Integer getBlocksCreated() {
        return blocksCreated;
    }

    public void setBlocksCreated(Integer blocksCreated) {
        this.blocksCreated = blocksCreated;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Long getStakeAmount() {
        return stakeAmount;
    }

    public void setStakeAmount(Long stakeAmount) {
        this.stakeAmount = stakeAmount;
    }

    public String getNodeUrl() {
        return nodeUrl;
    }

    public void setNodeUrl(String nodeUrl) {
        this.nodeUrl = nodeUrl;
    }

    public LocalDateTime getLastHeartbeat() {
        return lastHeartbeat;
    }

    public void setLastHeartbeat(LocalDateTime lastHeartbeat) {
        this.lastHeartbeat = lastHeartbeat;
    }

    public Integer getFailedAttempts() {
        return failedAttempts;
    }

    public void setFailedAttempts(Integer failedAttempts) {
        this.failedAttempts = failedAttempts;
    }

    public LocalDateTime getLockedUntil() {
        return lockedUntil;
    }

    public void setLockedUntil(LocalDateTime lockedUntil) {
        this.lockedUntil = lockedUntil;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    @Override
    public String toString() {
        return "Validator{" +
                "validatorId=" + validatorId +
                ", validatorName='" + validatorName + '\'' +
                ", isAuthorized=" + isAuthorized +
                ", priority=" + priority +
                ", blocksCreated=" + blocksCreated +
                ", isActive=" + isActive +
                ", isOnline=" + isOnline() +
                '}';
    }
}
