package com.minibanking.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.PublicKey;

/**
 * Test cases for DigitalSignature
 */
public class DigitalSignatureTest {
    
    @Test
    public void testKeyGeneration() {
        DigitalSignature ds = new DigitalSignature();
        
        assertNotNull(ds.getPublicKey());
        assertNotNull(ds.getPrivateKey());
        assertNotNull(ds.getKeyPair());
        
        // Public and private keys should be different
        assertNotEquals(ds.getPublicKey(), ds.getPrivateKey());
    }
    
    @Test
    public void testSignAndVerify() {
        DigitalSignature ds = new DigitalSignature();
        String message = "Hello, World!";
        
        // Sign the message
        byte[] signature = ds.sign(message);
        assertNotNull(signature);
        assertTrue(signature.length > 0);
        
        // Verify the signature
        assertTrue(ds.verify(message, signature));
    }
    
    @Test
    public void testSignAndVerifyWithBytes() {
        DigitalSignature ds = new DigitalSignature();
        byte[] message = "Hello, World!".getBytes();
        
        // Sign the message
        byte[] signature = ds.sign(message);
        assertNotNull(signature);
        assertTrue(signature.length > 0);
        
        // Verify the signature
        assertTrue(ds.verify(message, signature));
    }
    
    @Test
    public void testWrongMessageVerification() {
        DigitalSignature ds = new DigitalSignature();
        String originalMessage = "Hello, World!";
        String wrongMessage = "Goodbye, World!";
        
        // Sign the original message
        byte[] signature = ds.sign(originalMessage);
        
        // Verify with wrong message should fail
        assertFalse(ds.verify(wrongMessage, signature));
    }
    
    @Test
    public void testWrongSignatureVerification() {
        DigitalSignature ds = new DigitalSignature();
        String message = "Hello, World!";
        
        // Sign the message
        byte[] signature = ds.sign(message);
        
        // Create wrong signature
        byte[] wrongSignature = "wrongsignature".getBytes();
        
        // Verify with wrong signature should fail
        assertFalse(ds.verify(message, wrongSignature));
    }
    
    @Test
    public void testDifferentKeys() {
        DigitalSignature ds1 = new DigitalSignature();
        DigitalSignature ds2 = new DigitalSignature();
        String message = "Hello, World!";
        
        // Sign with first key pair
        byte[] signature1 = ds1.sign(message);
        
        // Sign with second key pair
        byte[] signature2 = ds2.sign(message);
        
        // Signatures should be different
        assertNotEquals(signature1, signature2);
        
        // Verify with correct key should work
        assertTrue(ds1.verify(message, signature1));
        assertTrue(ds2.verify(message, signature2));
        
        // Verify with wrong key should fail
        assertFalse(ds1.verify(message, signature2));
        assertFalse(ds2.verify(message, signature1));
    }
    
    @Test
    public void testCrossVerification() {
        DigitalSignature ds1 = new DigitalSignature();
        DigitalSignature ds2 = new DigitalSignature();
        String message = "Hello, World!";
        
        // Sign with first key pair
        byte[] signature = ds1.sign(message);
        
        // Verify with second key pair should fail
        assertFalse(ds2.verify(message, signature));
        
        // Verify with first key pair should work
        assertTrue(ds1.verify(message, signature));
    }
    
    @Test
    public void testFromKeyPair() {
        DigitalSignature ds1 = new DigitalSignature();
        KeyPair keyPair = ds1.getKeyPair();
        
        // Create new DigitalSignature from existing key pair
        DigitalSignature ds2 = DigitalSignature.fromKeyPair(keyPair);
        
        String message = "Hello, World!";
        
        // Sign with first instance
        byte[] signature = ds1.sign(message);
        
        // Verify with second instance should work
        assertTrue(ds2.verify(message, signature));
    }
    
    @Test
    public void testEmptyMessage() {
        DigitalSignature ds = new DigitalSignature();
        String message = "";
        
        // Sign empty message
        byte[] signature = ds.sign(message);
        assertNotNull(signature);
        
        // Verify empty message
        assertTrue(ds.verify(message, signature));
    }
    
    @Test
    public void testLongMessage() {
        DigitalSignature ds = new DigitalSignature();
        StringBuilder longMessage = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longMessage.append("This is a very long message. ");
        }
        
        String message = longMessage.toString();
        
        // Sign long message
        byte[] signature = ds.sign(message);
        assertNotNull(signature);
        
        // Verify long message
        assertTrue(ds.verify(message, signature));
    }
    
    @Test
    public void testHexRepresentation() {
        DigitalSignature ds = new DigitalSignature();
        
        String publicKeyHex = ds.getPublicKeyHex();
        String privateKeyHex = ds.getPrivateKeyHex();
        
        assertNotNull(publicKeyHex);
        assertNotNull(privateKeyHex);
        
        // Should be valid hex strings
        assertTrue(publicKeyHex.matches("[0-9a-f]+"));
        assertTrue(privateKeyHex.matches("[0-9a-f]+"));
        
        // Should not be empty
        assertFalse(publicKeyHex.isEmpty());
        assertFalse(privateKeyHex.isEmpty());
    }
    
    @Test
    public void testDeterministicSigning() {
        DigitalSignature ds = new DigitalSignature();
        String message = "Hello, World!";
        
        // Sign the same message multiple times
        byte[] signature1 = ds.sign(message);
        byte[] signature2 = ds.sign(message);
        
        // ECDSA signatures are non-deterministic due to random k
        // So signatures should be different
        assertNotEquals(signature1, signature2);
        
        // But both should verify
        assertTrue(ds.verify(message, signature1));
        assertTrue(ds.verify(message, signature2));
    }
}
