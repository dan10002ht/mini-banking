package com.minibanking.security.device;

import com.minibanking.entity.Device;
import com.minibanking.repository.DeviceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Device Service for device management and trust
 */
@Service
public class DeviceService {
    
    private static final Logger logger = LoggerFactory.getLogger(DeviceService.class);
    
    @Autowired
    private DeviceRepository deviceRepository;
    
    /**
     * Check if device is trusted
     */
    public boolean isTrustedDevice(UUID userId, String deviceFingerprint) {
        Optional<Device> device = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        return device.isPresent() && 
               device.get().getIsTrusted() && 
               device.get().getIsActive() && 
               !device.get().isLocked();
    }
    
    /**
     * Get device trust level
     */
    public DeviceTrustLevel getDeviceTrustLevel(UUID userId, String deviceFingerprint) {
        Optional<Device> device = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        
        if (device.isEmpty()) {
            return DeviceTrustLevel.UNKNOWN;
        }
        
        Device deviceEntity = device.get();
        
        if (!deviceEntity.getIsActive()) {
            return DeviceTrustLevel.UNKNOWN;
        }
        
        if (deviceEntity.isLocked()) {
            return DeviceTrustLevel.LOCKED;
        }
        
        if (deviceEntity.getIsTrusted()) {
            return DeviceTrustLevel.TRUSTED;
        }
        
        return DeviceTrustLevel.UNTRUSTED;
    }
    
    /**
     * Register new device
     */
    public Device registerDevice(UUID userId, String deviceFingerprint, String deviceName, 
                                String deviceType, String osName, String osVersion, 
                                String browserName, String browserVersion, String ipAddress, 
                                String userAgent) {
        
        // Check if device already exists
        Optional<Device> existingDevice = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (existingDevice.isPresent()) {
            Device device = existingDevice.get();
            device.setIsActive(true);
            device.setLastActivity(LocalDateTime.now());
            return deviceRepository.save(device);
        }
        
        // Create new device
        Device device = new Device();
        device.setUserId(userId);
        device.setDeviceFingerprint(deviceFingerprint);
        device.setDeviceName(deviceName);
        device.setDeviceType(deviceType);
        device.setOsName(osName);
        device.setOsVersion(osVersion);
        device.setBrowserName(browserName);
        device.setBrowserVersion(browserVersion);
        device.setIpAddress(ipAddress);
        device.setUserAgent(userAgent);
        device.setIsTrusted(false);
        device.setIsActive(true);
        
        Device savedDevice = deviceRepository.save(device);
        logger.info("New device registered for user: {} with fingerprint: {}", userId, deviceFingerprint);
        
        return savedDevice;
    }
    
    /**
     * Trust device
     */
    public Device trustDevice(UUID userId, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (deviceOpt.isEmpty()) {
            throw new RuntimeException("Device not found");
        }
        
        Device device = deviceOpt.get();
        device.markAsTrusted();
        device.recordSuccessfulLogin();
        
        Device savedDevice = deviceRepository.save(device);
        logger.info("Device trusted for user: {} with fingerprint: {}", userId, deviceFingerprint);
        
        return savedDevice;
    }
    
    /**
     * Untrust device
     */
    public Device untrustDevice(UUID userId, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (deviceOpt.isEmpty()) {
            throw new RuntimeException("Device not found");
        }
        
        Device device = deviceOpt.get();
        device.markAsUntrusted();
        
        Device savedDevice = deviceRepository.save(device);
        logger.info("Device untrusted for user: {} with fingerprint: {}", userId, deviceFingerprint);
        
        return savedDevice;
    }
    
    /**
     * Record successful login
     */
    public void recordSuccessfulLogin(UUID userId, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.recordSuccessfulLogin();
            deviceRepository.save(device);
        }
    }
    
    /**
     * Record failed login
     */
    public void recordFailedLogin(UUID userId, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.recordFailedLogin();
            deviceRepository.save(device);
        }
    }
    
    /**
     * Get user devices
     */
    public List<Device> getUserDevices(UUID userId) {
        return deviceRepository.findByUserIdAndIsActiveTrueOrderByLastLoginDesc(userId);
    }
    
    /**
     * Get trusted devices
     */
    public List<Device> getTrustedDevices(UUID userId) {
        return deviceRepository.findByUserIdAndIsTrustedTrueAndIsActiveTrueOrderByLastLoginDesc(userId);
    }
    
    /**
     * Get untrusted devices
     */
    public List<Device> getUntrustedDevices(UUID userId) {
        return deviceRepository.findByUserIdAndIsTrustedFalseAndIsActiveTrueOrderByCreatedAtDesc(userId);
    }
    
    /**
     * Deactivate device
     */
    public void deactivateDevice(UUID userId, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.deactivate();
            deviceRepository.save(device);
            logger.info("Device deactivated for user: {} with fingerprint: {}", userId, deviceFingerprint);
        }
    }
    
    /**
     * Unlock device
     */
    public void unlockDevice(UUID userId, String deviceFingerprint) {
        Optional<Device> deviceOpt = deviceRepository.findByUserIdAndDeviceFingerprint(userId, deviceFingerprint);
        if (deviceOpt.isPresent()) {
            Device device = deviceOpt.get();
            device.unlock();
            deviceRepository.save(device);
            logger.info("Device unlocked for user: {} with fingerprint: {}", userId, deviceFingerprint);
        }
    }
    
    /**
     * Get device by fingerprint
     */
    public Optional<Device> getDeviceByFingerprint(String deviceFingerprint) {
        return deviceRepository.findByDeviceFingerprint(deviceFingerprint);
    }
    
    /**
     * Cleanup inactive devices
     */
    public void cleanupInactiveDevices(int daysInactive) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysInactive);
        List<Device> inactiveDevices = deviceRepository.findInactiveDevices(cutoffDate);
        
        for (Device device : inactiveDevices) {
            device.deactivate();
            deviceRepository.save(device);
        }
        
        logger.info("Cleaned up {} inactive devices", inactiveDevices.size());
    }
    
    /**
     * Get device statistics
     */
    public DeviceStatistics getDeviceStatistics(UUID userId) {
        Long totalDevices = deviceRepository.countByUserId(userId);
        Long trustedDevices = deviceRepository.countByUserIdAndIsTrustedTrueAndIsActiveTrue(userId);
        
        return new DeviceStatistics(totalDevices, trustedDevices);
    }
    
    /**
     * Device Trust Level enum
     */
    public enum DeviceTrustLevel {
        UNKNOWN,    // Device not found or inactive
        TRUSTED,    // Device is trusted
        UNTRUSTED,  // Device is not trusted
        LOCKED      // Device is locked due to failed attempts
    }
    
    /**
     * Device Statistics class
     */
    public static class DeviceStatistics {
        private final Long totalDevices;
        private final Long trustedDevices;
        
        public DeviceStatistics(Long totalDevices, Long trustedDevices) {
            this.totalDevices = totalDevices;
            this.trustedDevices = trustedDevices;
        }
        
        public Long getTotalDevices() { return totalDevices; }
        public Long getTrustedDevices() { return trustedDevices; }
        public Long getUntrustedDevices() { return totalDevices - trustedDevices; }
    }
}
