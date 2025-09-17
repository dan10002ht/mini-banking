package com.minibanking.security.auth;

import com.minibanking.entity.Customer;
import com.minibanking.entity.Device;
import com.minibanking.entity.Session;
import com.minibanking.interfaces.IBankingService;
import com.minibanking.repository.SessionRepository;
import com.minibanking.security.device.DeviceService;
import com.minibanking.security.jwt.JwtService;
import com.minibanking.security.otp.OTPService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Authentication Service for login and device management
 */
@Service
public class AuthenticationService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);
    
    @Autowired
    private IBankingService bankingService;
    
    @Autowired
    private DeviceService deviceService;
    
    @Autowired
    private JwtService jwtService;
    
    @Autowired
    private OTPService otpService;
    
    @Autowired
    private SessionRepository sessionRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * Authenticate user login
     */
    public LoginResponse authenticate(LoginRequest request) {
        try {
            // 1. Get user by username
            Optional<Customer> customerOpt = bankingService.getCustomerByCode(request.getUsername());
            if (customerOpt.isEmpty()) {
                return LoginResponse.failed("Invalid username or password");
            }
            
            Customer customer = customerOpt.get();
            
            // 2. Check device trust level
            DeviceService.DeviceTrustLevel trustLevel = deviceService.getDeviceTrustLevel(
                customer.getCustomerId(), 
                request.getDeviceFingerprint()
            );
            
            // 3. Handle different trust levels
            switch (trustLevel) {
                case TRUSTED:
                    return authenticateWithPin(customer, request);
                case UNTRUSTED:
                case UNKNOWN:
                    return authenticateWithPassword(customer, request);
                case LOCKED:
                    return LoginResponse.failed("Device is locked due to multiple failed attempts");
                default:
                    return LoginResponse.failed("Authentication failed");
            }
            
        } catch (Exception e) {
            logger.error("Authentication error", e);
            return LoginResponse.failed("Authentication failed");
        }
    }
    
    /**
     * Authenticate with PIN (trusted device)
     */
    private LoginResponse authenticateWithPin(Customer customer, LoginRequest request) {
        // 1. Verify PIN
        if (!verifyPin(customer, request.getPin())) {
            deviceService.recordFailedLogin(customer.getCustomerId(), request.getDeviceFingerprint());
            return LoginResponse.failed("Invalid PIN");
        }
        
        // 2. Generate tokens
        String accessToken = jwtService.generateToken(
            customer.getCustomerId(), 
            customer.getCustomerCode(), 
            request.getDeviceFingerprint()
        );
        
        String refreshToken = jwtService.generateRefreshToken(
            customer.getCustomerId(), 
            customer.getCustomerCode(), 
            request.getDeviceFingerprint()
        );
        
        // 3. Create session
        createSession(customer.getCustomerId(), request.getDeviceFingerprint(), accessToken, refreshToken);
        
        // 4. Record successful login
        deviceService.recordSuccessfulLogin(customer.getCustomerId(), request.getDeviceFingerprint());
        
        logger.info("User {} authenticated with PIN on trusted device", customer.getCustomerCode());
        
        return LoginResponse.success(accessToken, refreshToken, customer, false);
    }
    
    /**
     * Authenticate with password (untrusted/unknown device)
     */
    private LoginResponse authenticateWithPassword(Customer customer, LoginRequest request) {
        // 1. Verify password
        if (!verifyPassword(customer, request.getPassword())) {
            deviceService.recordFailedLogin(customer.getCustomerId(), request.getDeviceFingerprint());
            return LoginResponse.failed("Invalid username or password");
        }
        
        // 2. Register device
        Device device = deviceService.registerDevice(
            customer.getCustomerId(),
            request.getDeviceFingerprint(),
            request.getDeviceName(),
            request.getDeviceType(),
            request.getOsName(),
            request.getOsVersion(),
            request.getBrowserName(),
            request.getBrowserVersion(),
            request.getIpAddress(),
            request.getUserAgent()
        );
        
        // 3. Generate OTP
        String otpId = otpService.generateOTP(customer.getCustomerId(), customer.getPhone());
        
        // 4. Create temporary session
        String tempToken = jwtService.generateTemporaryToken(
            customer.getCustomerId(), 
            customer.getCustomerCode(), 
            request.getDeviceFingerprint()
        );
        
        createTemporarySession(customer.getCustomerId(), request.getDeviceFingerprint(), tempToken);
        
        logger.info("User {} requires 2FA verification", customer.getCustomerCode());
        
        return LoginResponse.requires2FA(otpId, tempToken, customer);
    }
    
    /**
     * Verify OTP and complete authentication
     */
    public LoginResponse verifyOTP(String otpId, String otp, String deviceFingerprint) {
        try {
            // 1. Verify OTP
            OTPService.OTPVerificationResult result = otpService.verifyOTP(otpId, otp);
            if (!result.isValid()) {
                return LoginResponse.failed(result.getMessage());
            }
            
            // 2. Get customer
            Optional<Customer> customerOpt = bankingService.getCustomerById(result.getUserId());
            if (customerOpt.isEmpty()) {
                return LoginResponse.failed("User not found");
            }
            
            Customer customer = customerOpt.get();
            
            // 3. Trust device
            deviceService.trustDevice(customer.getCustomerId(), deviceFingerprint);
            
            // 4. Generate tokens
            String accessToken = jwtService.generateToken(
                customer.getCustomerId(), 
                customer.getCustomerCode(), 
                deviceFingerprint
            );
            
            String refreshToken = jwtService.generateRefreshToken(
                customer.getCustomerId(), 
                customer.getCustomerCode(), 
                deviceFingerprint
            );
            
            // 5. Create session
            createSession(customer.getCustomerId(), deviceFingerprint, accessToken, refreshToken);
            
            // 6. Record successful login
            deviceService.recordSuccessfulLogin(customer.getCustomerId(), deviceFingerprint);
            
            logger.info("User {} completed 2FA authentication", customer.getCustomerCode());
            
            return LoginResponse.success(accessToken, refreshToken, customer, true);
            
        } catch (Exception e) {
            logger.error("OTP verification error", e);
            return LoginResponse.failed("OTP verification failed");
        }
    }
    
    /**
     * Refresh access token
     */
    public LoginResponse refreshToken(String refreshToken) {
        try {
            // 1. Validate refresh token
            if (!jwtService.isRefreshToken(refreshToken) || !jwtService.validateToken(refreshToken)) {
                return LoginResponse.failed("Invalid refresh token");
            }
            
            // 2. Get user info from token
            UUID userId = jwtService.getUserIdFromToken(refreshToken);
            String username = jwtService.getUsernameFromToken(refreshToken);
            String deviceFingerprint = jwtService.getDeviceFingerprintFromToken(refreshToken);
            
            // 3. Verify session exists
            Optional<Session> sessionOpt = sessionRepository.findByUserIdAndDeviceFingerprint(
                userId, deviceFingerprint, LocalDateTime.now()
            );
            
            if (sessionOpt.isEmpty()) {
                return LoginResponse.failed("Session not found");
            }
            
            // 4. Generate new access token
            String newAccessToken = jwtService.generateToken(userId, username, deviceFingerprint);
            
            // 5. Update session
            Session session = sessionOpt.get();
            session.setTokenHash(jwtService.getTokenHash(newAccessToken));
            session.refreshActivity();
            sessionRepository.save(session);
            
            return LoginResponse.success(newAccessToken, refreshToken, null, false);
            
        } catch (Exception e) {
            logger.error("Token refresh error", e);
            return LoginResponse.failed("Token refresh failed");
        }
    }
    
    /**
     * Logout user
     */
    public void logout(UUID userId, String deviceFingerprint) {
        try {
            // Invalidate sessions for this user and device
            sessionRepository.invalidateSessionsByUserIdAndDevice(userId, deviceFingerprint);
            logger.info("User {} logged out from device {}", userId, deviceFingerprint);
        } catch (Exception e) {
            logger.error("Logout error", e);
        }
    }
    
    /**
     * Verify PIN
     */
    private boolean verifyPin(Customer customer, String pin) {
        // In a real system, PIN would be hashed and stored
        // For demo purposes, we'll use a simple check
        return pin != null && pin.length() >= 4;
    }
    
    /**
     * Verify password
     */
    private boolean verifyPassword(Customer customer, String password) {
        // In a real system, password would be hashed and compared
        // For demo purposes, we'll use a simple check
        return password != null && password.length() >= 6;
    }
    
    /**
     * Create session
     */
    private void createSession(UUID userId, String deviceFingerprint, String accessToken, String refreshToken) {
        // Invalidate old sessions for this device
        sessionRepository.invalidateSessionsByUserIdAndDevice(userId, deviceFingerprint);
        
        // Create new session
        Session session = new Session();
        session.setUserId(userId);
        session.setDeviceFingerprint(deviceFingerprint);
        session.setTokenHash(jwtService.getTokenHash(accessToken));
        session.setRefreshTokenHash(jwtService.getTokenHash(refreshToken));
        session.setExpiresAt(LocalDateTime.now().plusHours(24));
        session.setIsActive(true);
        session.setIsTemporary(false);
        
        sessionRepository.save(session);
    }
    
    /**
     * Create temporary session for 2FA
     */
    private void createTemporarySession(UUID userId, String deviceFingerprint, String tempToken) {
        Session session = new Session();
        session.setUserId(userId);
        session.setDeviceFingerprint(deviceFingerprint);
        session.setTokenHash(jwtService.getTokenHash(tempToken));
        session.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        session.setIsActive(true);
        session.setIsTemporary(true);
        
        sessionRepository.save(session);
    }
    
    /**
     * Get token hash (simplified)
     */
    private String getTokenHash(String token) {
        return String.valueOf(token.hashCode());
    }
}
