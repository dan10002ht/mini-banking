package com.minibanking.repository;

import com.minibanking.entity.Device;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Device entities
 */
@Repository
public interface DeviceRepository extends JpaRepository<Device, UUID> {
    
    /**
     * Find device by user ID and device fingerprint
     */
    Optional<Device> findByUserIdAndDeviceFingerprint(UUID userId, String deviceFingerprint);
    
    /**
     * Find device by device fingerprint
     */
    Optional<Device> findByDeviceFingerprint(String deviceFingerprint);
    
    /**
     * Find all devices for a user
     */
    List<Device> findByUserIdOrderByLastLoginDesc(UUID userId);
    
    /**
     * Find trusted devices for a user
     */
    List<Device> findByUserIdAndIsTrustedTrueAndIsActiveTrueOrderByLastLoginDesc(UUID userId);
    
    /**
     * Find untrusted devices for a user
     */
    List<Device> findByUserIdAndIsTrustedFalseAndIsActiveTrueOrderByCreatedAtDesc(UUID userId);
    
    /**
     * Find devices by user ID and active status
     */
    List<Device> findByUserIdAndIsActiveTrueOrderByLastLoginDesc(UUID userId);
    
    /**
     * Find locked devices
     */
    @Query("SELECT d FROM Device d WHERE d.lockedUntil IS NOT NULL AND d.lockedUntil > :now")
    List<Device> findLockedDevices(@Param("now") LocalDateTime now);
    
    /**
     * Find devices with failed login attempts
     */
    @Query("SELECT d FROM Device d WHERE d.failedLoginAttempts >= :threshold")
    List<Device> findDevicesWithFailedAttempts(@Param("threshold") Integer threshold);
    
    /**
     * Count devices for a user
     */
    Long countByUserId(UUID userId);
    
    /**
     * Count trusted devices for a user
     */
    Long countByUserIdAndIsTrustedTrueAndIsActiveTrue(UUID userId);
    
    /**
     * Find devices by IP address
     */
    List<Device> findByIpAddress(String ipAddress);
    
    /**
     * Find devices by user agent
     */
    @Query("SELECT d FROM Device d WHERE d.userAgent LIKE %:userAgent%")
    List<Device> findByUserAgentContaining(@Param("userAgent") String userAgent);
    
    /**
     * Find inactive devices (not logged in for specified days)
     */
    @Query("SELECT d FROM Device d WHERE d.lastLogin < :cutoffDate AND d.isActive = true")
    List<Device> findInactiveDevices(@Param("cutoffDate") LocalDateTime cutoffDate);
    
    /**
     * Delete devices by user ID
     */
    void deleteByUserId(UUID userId);
    
    /**
     * Delete inactive devices
     */
    @Query("DELETE FROM Device d WHERE d.lastLogin < :cutoffDate AND d.isActive = false")
    int deleteInactiveDevices(@Param("cutoffDate") LocalDateTime cutoffDate);
}
