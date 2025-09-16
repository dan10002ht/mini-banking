package com.minibanking.crypto;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

public class DiffieHellmanUtilsTest {
    
    private KeyPair keyPairA;
    private KeyPair keyPairB;
    
    @BeforeEach
    void setUp() {
        keyPairA = DiffieHellmanUtils.generateKeyPair();
        keyPairB = DiffieHellmanUtils.generateKeyPair();
    }
    
    @Test
    void testKeyGeneration() {
        assertNotNull(keyPairA);
        assertNotNull(keyPairA.getPrivate());
        assertNotNull(keyPairA.getPublic());
        
        assertNotNull(keyPairB);
        assertNotNull(keyPairB.getPrivate());
        assertNotNull(keyPairB.getPublic());
    }
    
    @Test
    void testKeyAgreement() {
        // Party A generates shared secret
        byte[] sharedSecretA = DiffieHellmanUtils.performKeyAgreement(
            keyPairA.getPrivate(), keyPairB.getPublic());
        
        // Party B generates shared secret
        byte[] sharedSecretB = DiffieHellmanUtils.performKeyAgreement(
            keyPairB.getPrivate(), keyPairA.getPublic());
        
        // Both parties should have the same shared secret
        assertArrayEquals(sharedSecretA, sharedSecretB);
        assertNotNull(sharedSecretA);
        assertTrue(sharedSecretA.length > 0);
    }
    
    @Test
    void testKeyAgreementBase64() {
        // Test Base64 encoding/decoding
        String sharedSecretA = DiffieHellmanUtils.performKeyAgreementBase64(
            keyPairA.getPrivate(), keyPairB.getPublic());
        
        String sharedSecretB = DiffieHellmanUtils.performKeyAgreementBase64(
            keyPairB.getPrivate(), keyPairA.getPublic());
        
        assertEquals(sharedSecretA, sharedSecretB);
        assertNotNull(sharedSecretA);
    }
    
    @Test
    void testSharedSecretGeneration() {
        // Generate 256-bit shared secret
        byte[] sharedSecretA = DiffieHellmanUtils.generateSharedSecret256(
            keyPairA.getPrivate(), keyPairB.getPublic());
        
        byte[] sharedSecretB = DiffieHellmanUtils.generateSharedSecret256(
            keyPairB.getPrivate(), keyPairA.getPublic());
        
        assertArrayEquals(sharedSecretA, sharedSecretB);
        assertEquals(32, sharedSecretA.length); // 256 bits = 32 bytes
    }
    
    @Test
    void testKeySerialization() {
        // Convert to Base64
        String publicKeyABase64 = DiffieHellmanUtils.publicKeyToBase64(keyPairA.getPublic());
        String privateKeyABase64 = DiffieHellmanUtils.privateKeyToBase64(keyPairA.getPrivate());
        
        assertNotNull(publicKeyABase64);
        assertNotNull(privateKeyABase64);
        
        // Convert back from Base64
        PublicKey restoredPublicKey = DiffieHellmanUtils.base64ToPublicKey(publicKeyABase64);
        PrivateKey restoredPrivateKey = DiffieHellmanUtils.base64ToPrivateKey(privateKeyABase64);
        
        assertNotNull(restoredPublicKey);
        assertNotNull(restoredPrivateKey);
        
        // Test key agreement with restored keys
        byte[] originalSharedSecret = DiffieHellmanUtils.performKeyAgreement(
            keyPairA.getPrivate(), keyPairB.getPublic());
        
        byte[] restoredSharedSecret = DiffieHellmanUtils.performKeyAgreement(
            restoredPrivateKey, keyPairB.getPublic());
        
        assertArrayEquals(originalSharedSecret, restoredSharedSecret);
    }
    
    @Test
    void testKeySize() {
        int keySizeA = DiffieHellmanUtils.getKeySize(keyPairA.getPrivate());
        int keySizeB = DiffieHellmanUtils.getKeySize(keyPairB.getPrivate());
        
        assertTrue(keySizeA > 0);
        assertTrue(keySizeB > 0);
        assertEquals(keySizeA, keySizeB); // Same key size
    }
    
    @Test
    void testDifferentKeySizes() {
        // Test 1024-bit key
        KeyPair keyPair1024 = DiffieHellmanUtils.generateKeyPair(1024);
        assertEquals(1024, DiffieHellmanUtils.getKeySize(keyPair1024.getPrivate()));
        
        // Test 4096-bit key
        KeyPair keyPair4096 = DiffieHellmanUtils.generateKeyPair(4096);
        assertEquals(4096, DiffieHellmanUtils.getKeySize(keyPair4096.getPrivate()));
    }
    
    @Test
    void testCrossKeyAgreement() {
        // Generate third key pair
        KeyPair keyPairC = DiffieHellmanUtils.generateKeyPair();
        
        // A and B should have same shared secret
        byte[] sharedSecretAB = DiffieHellmanUtils.performKeyAgreement(
            keyPairA.getPrivate(), keyPairB.getPublic());
        
        byte[] sharedSecretBA = DiffieHellmanUtils.performKeyAgreement(
            keyPairB.getPrivate(), keyPairA.getPublic());
        
        assertArrayEquals(sharedSecretAB, sharedSecretBA);
        
        // A and C should have different shared secret
        byte[] sharedSecretAC = DiffieHellmanUtils.performKeyAgreement(
            keyPairA.getPrivate(), keyPairC.getPublic());
        
        assertFalse(java.util.Arrays.equals(sharedSecretAB, sharedSecretAC));
    }
    
    @Test
    void testDemoKeyExchange() {
        String demoResult = DiffieHellmanUtils.demoKeyExchange();
        assertNotNull(demoResult);
        assertTrue(demoResult.contains("Key Exchange Demo"));
        assertTrue(demoResult.contains("Secrets Match: true"));
    }
}
