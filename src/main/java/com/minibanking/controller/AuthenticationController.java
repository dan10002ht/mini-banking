package com.minibanking.controller;

import com.minibanking.dto.LoginRequest;
import com.minibanking.dto.LoginResponse;
import com.minibanking.security.auth.AuthenticationService;
import com.minibanking.security.device.DeviceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Authentication Controller for login, logout, and device management
 */
@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
@Tag(name = "Authentication", description = "User authentication and device management")
public class AuthenticationController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);
    
    @Autowired
    private AuthenticationService authenticationService;
    
    @Autowired
    private DeviceService deviceService;
    
    /**
     * User login
     */
    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user with username/password or PIN")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Login successful",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Authentication failed"),
        @ApiResponse(responseCode = "423", description = "Device locked")
    })
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Login credentials", required = true)
            @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        
        try {
            // Extract device information from request
            extractDeviceInfo(request, httpRequest);
            
            logger.info("Login attempt for user: {} from device: {}", 
                       request.getUsername(), request.getDeviceFingerprint());
            
            // Authenticate user
            LoginResponse response = authenticationService.authenticate(request);
            
            if (response.isSuccess()) {
                logger.info("Login successful for user: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else if (response.requires2FA()) {
                logger.info("2FA required for user: {}", request.getUsername());
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Login failed for user: {} - {}", request.getUsername(), response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Login error for user: {}", request.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failed("Login failed due to server error"));
        }
    }
    
    /**
     * Verify OTP for 2FA
     */
    @PostMapping("/verify-otp")
    @Operation(summary = "Verify OTP", description = "Verify OTP for 2FA authentication")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "OTP verified successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "400", description = "Invalid OTP"),
        @ApiResponse(responseCode = "401", description = "OTP verification failed")
    })
    public ResponseEntity<LoginResponse> verifyOTP(
            @Parameter(description = "OTP ID", required = true)
            @RequestParam String otpId,
            @Parameter(description = "OTP code", required = true)
            @RequestParam String otp,
            @Parameter(description = "Device fingerprint", required = true)
            @RequestParam String deviceFingerprint) {
        
        try {
            logger.info("OTP verification attempt for OTP ID: {}", otpId);
            
            LoginResponse response = authenticationService.verifyOTP(otpId, otp, deviceFingerprint);
            
            if (response.isSuccess()) {
                logger.info("OTP verification successful");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("OTP verification failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            logger.error("OTP verification error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failed("OTP verification failed due to server error"));
        }
    }
    
    /**
     * Refresh access token
     */
    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = LoginResponse.class))),
        @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<LoginResponse> refreshToken(
            @Parameter(description = "Refresh token", required = true)
            @RequestParam String refreshToken) {
        
        try {
            logger.info("Token refresh attempt");
            
            LoginResponse response = authenticationService.refreshToken(refreshToken);
            
            if (response.isSuccess()) {
                logger.info("Token refreshed successfully");
                return ResponseEntity.ok(response);
            } else {
                logger.warn("Token refresh failed: {}", response.getMessage());
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(response);
            }
            
        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(LoginResponse.failed("Token refresh failed due to server error"));
        }
    }
    
    /**
     * User logout
     */
    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate session")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Logout successful"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<Map<String, String>> logout(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Device fingerprint", required = true)
            @RequestParam String deviceFingerprint) {
        
        try {
            authenticationService.logout(userId, deviceFingerprint);
            
            Map<String, String> response = Map.of(
                "status", "SUCCESS",
                "message", "Logout successful"
            );
            
            logger.info("User {} logged out from device {}", userId, deviceFingerprint);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Logout error for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Logout failed"));
        }
    }
    
    /**
     * Get user devices
     */
    @GetMapping("/devices")
    @Operation(summary = "Get user devices", description = "Get list of devices for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Devices retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getUserDevices(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId) {
        
        try {
            var devices = deviceService.getUserDevices(userId);
            return ResponseEntity.ok(devices);
            
        } catch (Exception e) {
            logger.error("Error getting devices for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get devices"));
        }
    }
    
    /**
     * Trust device
     */
    @PostMapping("/devices/trust")
    @Operation(summary = "Trust device", description = "Mark a device as trusted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device trusted successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<Map<String, String>> trustDevice(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Device fingerprint", required = true)
            @RequestParam String deviceFingerprint) {
        
        try {
            deviceService.trustDevice(userId, deviceFingerprint);
            
            Map<String, String> response = Map.of(
                "status", "SUCCESS",
                "message", "Device trusted successfully"
            );
            
            logger.info("Device {} trusted for user {}", deviceFingerprint, userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error trusting device for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Failed to trust device"));
        }
    }
    
    /**
     * Untrust device
     */
    @PostMapping("/devices/untrust")
    @Operation(summary = "Untrust device", description = "Mark a device as untrusted")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device untrusted successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<Map<String, String>> untrustDevice(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Device fingerprint", required = true)
            @RequestParam String deviceFingerprint) {
        
        try {
            deviceService.untrustDevice(userId, deviceFingerprint);
            
            Map<String, String> response = Map.of(
                "status", "SUCCESS",
                "message", "Device untrusted successfully"
            );
            
            logger.info("Device {} untrusted for user {}", deviceFingerprint, userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error untrusting device for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Failed to untrust device"));
        }
    }
    
    /**
     * Deactivate device
     */
    @PostMapping("/devices/deactivate")
    @Operation(summary = "Deactivate device", description = "Deactivate a device")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Device deactivated successfully"),
        @ApiResponse(responseCode = "404", description = "Device not found")
    })
    public ResponseEntity<Map<String, String>> deactivateDevice(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId,
            @Parameter(description = "Device fingerprint", required = true)
            @RequestParam String deviceFingerprint) {
        
        try {
            deviceService.deactivateDevice(userId, deviceFingerprint);
            
            Map<String, String> response = Map.of(
                "status", "SUCCESS",
                "message", "Device deactivated successfully"
            );
            
            logger.info("Device {} deactivated for user {}", deviceFingerprint, userId);
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            logger.error("Error deactivating device for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("status", "ERROR", "message", "Failed to deactivate device"));
        }
    }
    
    /**
     * Get device statistics
     */
    @GetMapping("/devices/statistics")
    @Operation(summary = "Get device statistics", description = "Get device statistics for a user")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Statistics retrieved successfully"),
        @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<?> getDeviceStatistics(
            @Parameter(description = "User ID", required = true)
            @RequestParam UUID userId) {
        
        try {
            var statistics = deviceService.getDeviceStatistics(userId);
            return ResponseEntity.ok(statistics);
            
        } catch (Exception e) {
            logger.error("Error getting device statistics for user: {}", userId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Failed to get device statistics"));
        }
    }
    
    /**
     * Extract device information from HTTP request
     */
    private void extractDeviceInfo(LoginRequest request, HttpServletRequest httpRequest) {
        if (request.getIpAddress() == null) {
            request.setIpAddress(getClientIpAddress(httpRequest));
        }
        
        if (request.getUserAgent() == null) {
            request.setUserAgent(httpRequest.getHeader("User-Agent"));
        }
        
        if (request.getDeviceName() == null) {
            request.setDeviceName("Unknown Device");
        }
        
        if (request.getDeviceType() == null) {
            request.setDeviceType("UNKNOWN");
        }
    }
    
    /**
     * Get client IP address
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
}
