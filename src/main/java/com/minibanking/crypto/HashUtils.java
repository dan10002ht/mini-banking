package com.minibanking.crypto;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.UnsupportedEncodingException;

/**
 * Utility class for hash functions
 * Implements SHA256 and other cryptographic hash functions
 */
public class HashUtils {
    
    /**
     * Generate SHA256 hash of input string
     * @param input String to hash
     * @return SHA256 hash as hex string
     */
    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes("UTF-8"));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            throw new RuntimeException("Error generating SHA256 hash", e);
        }
    }
    
    /**
     * Generate SHA256 hash of byte array
     * @param input Byte array to hash
     * @return SHA256 hash as hex string
     */
    public static String sha256(byte[] input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input);
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error generating SHA256 hash", e);
        }
    }
    
    /**
     * Generate double SHA256 hash (used in Bitcoin)
     * @param input String to hash
     * @return Double SHA256 hash as hex string
     */
    public static String doubleSha256(String input) {
        String firstHash = sha256(input);
        return sha256(firstHash);
    }
    
    /**
     * Convert byte array to hex string
     * @param bytes Byte array to convert
     * @return Hex string representation
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Verify if a string matches its hash
     * @param input Original string
     * @param hash Hash to verify against
     * @return true if hash matches
     */
    public static boolean verifyHash(String input, String hash) {
        return sha256(input).equals(hash);
    }
    
    /**
     * Generate hash with salt for additional security
     * @param input String to hash
     * @param salt Salt to add
     * @return SHA256 hash with salt
     */
    public static String sha256WithSalt(String input, String salt) {
        return sha256(input + salt);
    }
}
