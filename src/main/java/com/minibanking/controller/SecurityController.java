package com.minibanking.controller;

import com.minibanking.security.SecureBankingService;
import com.minibanking.security.pki.CertificateAuthority;
import com.minibanking.security.hsm.HSMService;
import com.minibanking.security.encryption.DataEncryptionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * Security Controller
 * Provides security-related API endpoints
 */
@RestController
@RequestMapping("/api/security")
@CrossOrigin(origins = "*")
@Tag(name = "Security Operations", description = "Security management and monitoring")
public class SecurityController {
    
    @Autowired
    private SecureBankingService secureBankingService;
    
    @Autowired
    private CertificateAuthority certificateAuthority;
    
    @Autowired
    private HSMService hsmService;
    
    @Autowired
    private DataEncryptionService dataEncryptionService;
    
    /**
     * Initialize security system
     */
    @PostMapping("/initialize")
    @Operation(summary = "Initialize security system", description = "Initializes all security components")
    public ResponseEntity<String> initializeSecurity() {
        try {
            secureBankingService.initializeSecureBanking();
            return ResponseEntity.ok("Security system initialized successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to initialize security system: " + e.getMessage());
        }
    }
    
    /**
     * Get security status
     */
    @GetMapping("/status")
    @Operation(summary = "Get security status", description = "Returns current security system status")
    public ResponseEntity<String> getSecurityStatus() {
        try {
            String status = secureBankingService.getSecurityStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get security status: " + e.getMessage());
        }
    }
    
    /**
     * Get HSM status
     */
    @GetMapping("/hsm/status")
    @Operation(summary = "Get HSM status", description = "Returns HSM service status")
    public ResponseEntity<String> getHSMStatus() {
        try {
            String status = hsmService.getHSMStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get HSM status: " + e.getMessage());
        }
    }
    
    /**
     * List HSM keys
     */
    @GetMapping("/hsm/keys")
    @Operation(summary = "List HSM keys", description = "Returns list of all keys in HSM")
    public ResponseEntity<Map<String, String>> listHSMKeys() {
        try {
            Map<String, String> keys = hsmService.listKeys();
            return ResponseEntity.ok(keys);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(null);
        }
    }
    
    /**
     * Generate new HSM key
     */
    @PostMapping("/hsm/generate-key")
    @Operation(summary = "Generate HSM key", description = "Generates a new key in HSM")
    public ResponseEntity<String> generateHSMKey(@RequestParam String keyId, 
                                               @RequestParam String algorithm, 
                                               @RequestParam int keySize) {
        try {
            String result = hsmService.generateKey(keyId, algorithm, keySize);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to generate HSM key: " + e.getMessage());
        }
    }
    
    /**
     * Get HSM public key
     */
    @GetMapping("/hsm/public-key/{keyId}")
    @Operation(summary = "Get HSM public key", description = "Returns public key for specified key ID")
    public ResponseEntity<String> getHSMPublicKey(@PathVariable String keyId) {
        try {
            String publicKey = hsmService.getPublicKey(keyId);
            return ResponseEntity.ok(publicKey);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get HSM public key: " + e.getMessage());
        }
    }
    
    /**
     * Delete HSM key
     */
    @DeleteMapping("/hsm/keys/{keyId}")
    @Operation(summary = "Delete HSM key", description = "Deletes a key from HSM")
    public ResponseEntity<String> deleteHSMKey(@PathVariable String keyId) {
        try {
            String result = hsmService.deleteKey(keyId);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to delete HSM key: " + e.getMessage());
        }
    }
    
    /**
     * Get certificate authority status
     */
    @GetMapping("/ca/status")
    @Operation(summary = "Get CA status", description = "Returns certificate authority status")
    public ResponseEntity<String> getCAStatus() {
        try {
            int certificateCount = certificateAuthority.getIssuedCertificatesCount();
            String status = "Certificate Authority Status: " + certificateCount + " certificates issued";
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get CA status: " + e.getMessage());
        }
    }
    
    /**
     * Get encryption status
     */
    @GetMapping("/encryption/status")
    @Operation(summary = "Get encryption status", description = "Returns data encryption service status")
    public ResponseEntity<String> getEncryptionStatus() {
        try {
            String status = dataEncryptionService.getEncryptionStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Failed to get encryption status: " + e.getMessage());
        }
    }
    
    /**
     * Test encryption
     */
    @PostMapping("/encryption/test")
    @Operation(summary = "Test encryption", description = "Tests data encryption and decryption")
    public ResponseEntity<Map<String, String>> testEncryption(@RequestParam String data) {
        try {
            String encrypted = dataEncryptionService.encryptSensitiveData(data);
            String decrypted = dataEncryptionService.decryptSensitiveData(encrypted);
            
            Map<String, String> result = Map.of(
                "original", data,
                "encrypted", encrypted,
                "decrypted", decrypted,
                "success", String.valueOf(data.equals(decrypted))
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to test encryption: " + e.getMessage()));
        }
    }
    
    /**
     * Hash password
     */
    @PostMapping("/encryption/hash-password")
    @Operation(summary = "Hash password", description = "Hashes a password with salt")
    public ResponseEntity<Map<String, String>> hashPassword(@RequestParam String password) {
        try {
            String salt = dataEncryptionService.generateSalt();
            String hashedPassword = dataEncryptionService.hashPassword(password, salt);
            
            Map<String, String> result = Map.of(
                "password", password,
                "salt", salt,
                "hashedPassword", hashedPassword
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to hash password: " + e.getMessage()));
        }
    }
    
    /**
     * Verify password
     */
    @PostMapping("/encryption/verify-password")
    @Operation(summary = "Verify password", description = "Verifies a password against its hash")
    public ResponseEntity<Map<String, Object>> verifyPassword(@RequestParam String password, 
                                                            @RequestParam String salt, 
                                                            @RequestParam String hashedPassword) {
        try {
            boolean isValid = dataEncryptionService.verifyPassword(password, salt, hashedPassword);
            
            Map<String, Object> result = Map.of(
                "password", password,
                "salt", salt,
                "hashedPassword", hashedPassword,
                "isValid", isValid
            );
            
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", "Failed to verify password: " + e.getMessage()));
        }
    }
}
