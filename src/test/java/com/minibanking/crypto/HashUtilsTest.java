package com.minibanking.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test cases for HashUtils
 */
public class HashUtilsTest {
    
    @Test
    public void testSHA256Basic() {
        String input = "Hello World";
        String expected = "a591a6d40bf420404a011733cfb7b190d62c65bf0bcda32b57b277d9ad9f146e";
        String actual = HashUtils.sha256(input);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testSHA256EmptyString() {
        String input = "";
        String expected = "e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855";
        String actual = HashUtils.sha256(input);
        assertEquals(expected, actual);
    }
    
    @Test
    public void testAvalancheEffect() {
        String input1 = "Hello";
        String input2 = "Hello!";
        
        String hash1 = HashUtils.sha256(input1);
        String hash2 = HashUtils.sha256(input2);
        
        // Hashes should be completely different
        assertNotEquals(hash1, hash2);
        
        // Both should be 64 characters (256 bits = 64 hex chars)
        assertEquals(64, hash1.length());
        assertEquals(64, hash2.length());
    }
    
    @Test
    public void testDeterministic() {
        String input = "Test message";
        String hash1 = HashUtils.sha256(input);
        String hash2 = HashUtils.sha256(input);
        
        // Same input should always produce same hash
        assertEquals(hash1, hash2);
    }
    
    @Test
    public void testDoubleSHA256() {
        String input = "Test message";
        String singleHash = HashUtils.sha256(input);
        String doubleHash = HashUtils.doubleSha256(input);
        
        // Double hash should be different from single hash
        assertNotEquals(singleHash, doubleHash);
        
        // Both should be 64 characters
        assertEquals(64, singleHash.length());
        assertEquals(64, doubleHash.length());
    }
    
    @Test
    public void testVerifyHash() {
        String input = "Test message";
        String hash = HashUtils.sha256(input);
        
        // Correct hash should verify
        assertTrue(HashUtils.verifyHash(input, hash));
        
        // Wrong hash should not verify
        assertFalse(HashUtils.verifyHash(input, "wronghash"));
        
        // Wrong input should not verify
        assertFalse(HashUtils.verifyHash("wrong input", hash));
    }
    
    @Test
    public void testSHA256WithSalt() {
        String input = "password";
        String salt = "randomSalt123";
        
        String hash1 = HashUtils.sha256WithSalt(input, salt);
        String hash2 = HashUtils.sha256WithSalt(input, salt);
        String hash3 = HashUtils.sha256WithSalt(input, "differentSalt");
        
        // Same input + salt should produce same hash
        assertEquals(hash1, hash2);
        
        // Different salt should produce different hash
        assertNotEquals(hash1, hash3);
        
        // All hashes should be 64 characters
        assertEquals(64, hash1.length());
        assertEquals(64, hash2.length());
        assertEquals(64, hash3.length());
    }
    
    @Test
    public void testSHA256ByteArray() {
        String input = "Test message";
        byte[] inputBytes = input.getBytes();
        
        String hashFromString = HashUtils.sha256(input);
        String hashFromBytes = HashUtils.sha256(inputBytes);
        
        // Both methods should produce same result
        assertEquals(hashFromString, hashFromBytes);
    }
    
    @Test
    public void testLongInput() {
        StringBuilder longInput = new StringBuilder();
        for (int i = 0; i < 1000; i++) {
            longInput.append("This is a very long input string. ");
        }
        
        String hash = HashUtils.sha256(longInput.toString());
        
        // Should still produce 64-character hash
        assertEquals(64, hash.length());
        assertNotNull(hash);
    }
}
