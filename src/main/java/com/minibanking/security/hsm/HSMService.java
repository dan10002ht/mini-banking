package com.minibanking.security.hsm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.security.*;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

/**
 * HSM Service Implementation
 * Simulates Hardware Security Module for banking system
 */
@Service
public class HSMService {
    
    private static final Logger logger = LoggerFactory.getLogger(HSMService.class);
    
    // Simulated HSM key store
    private Map<String, KeyPair> keyStore;
    private Map<String, String> keyMetadata;
    
    public HSMService() {
        this.keyStore = new HashMap<>();
        this.keyMetadata = new HashMap<>();
        initializeHSM();
    }
    
    /**
     * Initialize HSM
     */
    private void initializeHSM() {
        try {
            logger.info("Initializing HSM Service...");
            
            // Generate master key for HSM
            generateKey("HSM_MASTER_KEY", "RSA", 2048);
            
            // Generate banking keys
            generateKey("BANK_SIGNING_KEY", "RSA", 2048);
            generateKey("BANK_ENCRYPTION_KEY", "RSA", 2048);
            generateKey("CUSTOMER_KEY_PREFIX", "RSA", 2048);
            
            logger.info("HSM Service initialized successfully");
            logger.info("Generated keys: {}", keyStore.size());
            
        } catch (Exception e) {
            logger.error("Error initializing HSM Service", e);
            throw new RuntimeException("Failed to initialize HSM", e);
        }
    }
    
    /**
     * Generate key pair
     * @param keyId Key identifier
     * @param algorithm Key algorithm
     * @param keySize Key size
     * @return Key generation result
     */
    public String generateKey(String keyId, String algorithm, int keySize) {
        try {
            logger.info("Generating key: {} with algorithm: {} and size: {}", keyId, algorithm, keySize);
            
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(algorithm);
            keyGen.initialize(keySize);
            KeyPair keyPair = keyGen.generateKeyPair();
            
            // Store key in HSM
            keyStore.put(keyId, keyPair);
            keyMetadata.put(keyId, algorithm + "_" + keySize);
            
            logger.info("Key generated successfully: {}", keyId);
            return "Key generated successfully: " + keyId;
            
        } catch (Exception e) {
            logger.error("Error generating key: {}", keyId, e);
            throw new RuntimeException("Failed to generate key", e);
        }
    }
    
    /**
     * Get public key
     * @param keyId Key identifier
     * @return Public key as Base64 string
     */
    public String getPublicKey(String keyId) {
        try {
            KeyPair keyPair = keyStore.get(keyId);
            if (keyPair == null) {
                throw new RuntimeException("Key not found: " + keyId);
            }
            
            return Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded());
            
        } catch (Exception e) {
            logger.error("Error getting public key: {}", keyId, e);
            throw new RuntimeException("Failed to get public key", e);
        }
    }
    
    /**
     * Sign data with HSM key
     * @param keyId Key identifier
     * @param data Data to sign
     * @return Digital signature
     */
    public byte[] sign(String keyId, byte[] data) {
        try {
            logger.debug("Signing data with key: {}", keyId);
            
            KeyPair keyPair = keyStore.get(keyId);
            if (keyPair == null) {
                throw new RuntimeException("Key not found: " + keyId);
            }
            
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(keyPair.getPrivate());
            signature.update(data);
            
            byte[] signatureBytes = signature.sign();
            
            logger.debug("Data signed successfully with key: {}", keyId);
            return signatureBytes;
            
        } catch (Exception e) {
            logger.error("Error signing data with key: {}", keyId, e);
            throw new RuntimeException("Failed to sign data", e);
        }
    }
    
    /**
     * Verify signature with HSM key
     * @param keyId Key identifier
     * @param data Original data
     * @param signature Digital signature
     * @return true if signature is valid
     */
    public boolean verify(String keyId, byte[] data, byte[] signature) {
        try {
            logger.debug("Verifying signature with key: {}", keyId);
            
            KeyPair keyPair = keyStore.get(keyId);
            if (keyPair == null) {
                logger.warn("Key not found: {}", keyId);
                return false;
            }
            
            Signature sig = Signature.getInstance("SHA256withRSA");
            sig.initVerify(keyPair.getPublic());
            sig.update(data);
            
            boolean isValid = sig.verify(signature);
            
            logger.debug("Signature verification result: {} for key: {}", isValid, keyId);
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying signature with key: {}", keyId, e);
            return false;
        }
    }
    
    /**
     * Encrypt data with HSM key
     * @param keyId Key identifier
     * @param data Data to encrypt
     * @return Encrypted data
     */
    public byte[] encrypt(String keyId, byte[] data) {
        try {
            logger.debug("Encrypting data with key: {}", keyId);
            
            KeyPair keyPair = keyStore.get(keyId);
            if (keyPair == null) {
                throw new RuntimeException("Key not found: " + keyId);
            }
            
            // Simplified encryption - in production, use proper encryption
            // For demo purposes, we'll just return the data as-is
            logger.warn("Using mock encryption - implement proper encryption");
            return data;
            
        } catch (Exception e) {
            logger.error("Error encrypting data with key: {}", keyId, e);
            throw new RuntimeException("Failed to encrypt data", e);
        }
    }
    
    /**
     * Decrypt data with HSM key
     * @param keyId Key identifier
     * @param encryptedData Encrypted data
     * @return Decrypted data
     */
    public byte[] decrypt(String keyId, byte[] encryptedData) {
        try {
            logger.debug("Decrypting data with key: {}", keyId);
            
            KeyPair keyPair = keyStore.get(keyId);
            if (keyPair == null) {
                throw new RuntimeException("Key not found: " + keyId);
            }
            
            // Simplified decryption - in production, use proper decryption
            // For demo purposes, we'll just return the data as-is
            logger.warn("Using mock decryption - implement proper decryption");
            return encryptedData;
            
        } catch (Exception e) {
            logger.error("Error decrypting data with key: {}", keyId, e);
            throw new RuntimeException("Failed to decrypt data", e);
        }
    }
    
    /**
     * Delete key from HSM
     * @param keyId Key identifier
     * @return Deletion result
     */
    public String deleteKey(String keyId) {
        try {
            logger.info("Deleting key: {}", keyId);
            
            if (keyStore.containsKey(keyId)) {
                keyStore.remove(keyId);
                keyMetadata.remove(keyId);
                logger.info("Key deleted successfully: {}", keyId);
                return "Key deleted successfully: " + keyId;
            } else {
                logger.warn("Key not found for deletion: {}", keyId);
                return "Key not found: " + keyId;
            }
            
        } catch (Exception e) {
            logger.error("Error deleting key: {}", keyId, e);
            throw new RuntimeException("Failed to delete key", e);
        }
    }
    
    /**
     * List all keys in HSM
     * @return Map of key IDs and metadata
     */
    public Map<String, String> listKeys() {
        logger.debug("Listing all keys in HSM");
        return new HashMap<>(keyMetadata);
    }
    
    /**
     * Get key metadata
     * @param keyId Key identifier
     * @return Key metadata
     */
    public String getKeyMetadata(String keyId) {
        return keyMetadata.get(keyId);
    }
    
    /**
     * Check if key exists
     * @param keyId Key identifier
     * @return true if key exists
     */
    public boolean keyExists(String keyId) {
        return keyStore.containsKey(keyId);
    }
    
    /**
     * Get HSM status
     * @return HSM status
     */
    public String getHSMStatus() {
        return String.format("HSM Status: %d keys stored, %d metadata entries", 
            keyStore.size(), keyMetadata.size());
    }
}
