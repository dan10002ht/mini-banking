package com.minibanking.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class RSAUtilsTest {
    
    private KeyPair keyPair;
    private String testMessage;
    
    @BeforeEach
    void setUp() {
        keyPair = RSAUtils.generateKeyPair();
        testMessage = "Hello, RSA encryption!";
    }
    
    @Test
    void testKeyGeneration() {
        assertNotNull(keyPair);
        assertNotNull(keyPair.getPrivate());
        assertNotNull(keyPair.getPublic());
        
        int keySize = RSAUtils.getKeySize(keyPair.getPrivate());
        assertEquals(2048, keySize);
    }
    
    @Test
    void testEncryptionDecryption() {
        // Encrypt with public key
        String encrypted = RSAUtils.encrypt(testMessage, keyPair.getPublic());
        assertNotNull(encrypted);
        assertNotEquals(testMessage, encrypted);
        
        // Decrypt with private key
        String decrypted = RSAUtils.decrypt(encrypted, keyPair.getPrivate());
        assertEquals(testMessage, decrypted);
    }
    
    @Test
    void testDigitalSignature() {
        // Sign with private key
        String signature = RSAUtils.sign(testMessage, keyPair.getPrivate());
        assertNotNull(signature);
        
        // Verify with public key
        boolean isValid = RSAUtils.verify(testMessage, signature, keyPair.getPublic());
        assertTrue(isValid);
    }
    
    @Test
    void testSignatureVerificationWithWrongMessage() {
        String signature = RSAUtils.sign(testMessage, keyPair.getPrivate());
        String wrongMessage = "Wrong message";
        
        boolean isValid = RSAUtils.verify(wrongMessage, signature, keyPair.getPublic());
        assertFalse(isValid);
    }
    
    @Test
    void testKeySerialization() {
        // Convert to Base64
        String publicKeyBase64 = RSAUtils.publicKeyToBase64(keyPair.getPublic());
        String privateKeyBase64 = RSAUtils.privateKeyToBase64(keyPair.getPrivate());
        
        assertNotNull(publicKeyBase64);
        assertNotNull(privateKeyBase64);
        
        // Convert back from Base64
        PublicKey restoredPublicKey = RSAUtils.base64ToPublicKey(publicKeyBase64);
        PrivateKey restoredPrivateKey = RSAUtils.base64ToPrivateKey(privateKeyBase64);
        
        assertNotNull(restoredPublicKey);
        assertNotNull(restoredPrivateKey);
        
        // Test encryption with restored keys
        String encrypted = RSAUtils.encrypt(testMessage, restoredPublicKey);
        String decrypted = RSAUtils.decrypt(encrypted, restoredPrivateKey);
        assertEquals(testMessage, decrypted);
    }
    
    @Test
    void testDataSizeValidation() {
        int keySize = RSAUtils.getKeySize(keyPair.getPrivate());
        int maxDataSize = RSAUtils.getMaxDataSize(keySize);
        
        // Test valid data size
        byte[] validData = new byte[maxDataSize];
        assertTrue(RSAUtils.isDataSizeValid(validData, keySize));
        
        // Test invalid data size
        byte[] invalidData = new byte[maxDataSize + 1];
        assertFalse(RSAUtils.isDataSizeValid(invalidData, keySize));
    }
    
    @Test
    void testDifferentKeySizes() {
        // Test 1024-bit key
        KeyPair keyPair1024 = RSAUtils.generateKeyPair(1024);
        assertEquals(1024, RSAUtils.getKeySize(keyPair1024.getPrivate()));
        
        // Test 4096-bit key
        KeyPair keyPair4096 = RSAUtils.generateKeyPair(4096);
        assertEquals(4096, RSAUtils.getKeySize(keyPair4096.getPrivate()));
    }
    
    @Test
    void testCrossKeyEncryption() {
        // Generate two different key pairs
        KeyPair keyPair1 = RSAUtils.generateKeyPair();
        KeyPair keyPair2 = RSAUtils.generateKeyPair();
        
        // Encrypt with keyPair1, try to decrypt with keyPair2 (should fail)
        String encrypted = RSAUtils.encrypt(testMessage, keyPair1.getPublic());
        
        assertThrows(RuntimeException.class, () -> {
            RSAUtils.decrypt(encrypted, keyPair2.getPrivate());
        });
    }
}
