package com.minibanking.crypto;

import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPublicKeySpec;
import java.math.BigInteger;

/**
 * Digital Signature implementation using ECDSA
 * Provides key generation, signing, and verification
 */
public class DigitalSignature {
    
    private KeyPair keyPair;
    private static final String ALGORITHM = "SHA256withECDSA";
    private static final String CURVE = "secp256r1"; // P-256 curve (supported by default)
    
    public DigitalSignature() {
        generateKeyPair();
    }
    
    public DigitalSignature(KeyPair keyPair) {
        this.keyPair = keyPair;
    }
    
    /**
     * Generate new ECDSA key pair
     */
    private void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("EC");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec(CURVE);
            keyGen.initialize(ecSpec);
            this.keyPair = keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generating key pair", e);
        }
    }
    
    /**
     * Sign a message with private key
     * @param message Message to sign
     * @return Digital signature as byte array
     */
    public byte[] sign(String message) {
        return sign(message.getBytes());
    }
    
    /**
     * Sign a message with private key
     * @param message Message to sign as byte array
     * @return Digital signature as byte array
     */
    public byte[] sign(byte[] message) {
        try {
            Signature signature = Signature.getInstance(ALGORITHM);
            signature.initSign(keyPair.getPrivate());
            signature.update(message);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("Error signing message", e);
        }
    }
    
    /**
     * Verify a signature with public key
     * @param message Original message
     * @param signature Signature to verify
     * @return true if signature is valid
     */
    public boolean verify(String message, byte[] signature) {
        return verify(message.getBytes(), signature);
    }
    
    /**
     * Verify a signature with public key
     * @param message Original message as byte array
     * @param signature Signature to verify
     * @return true if signature is valid
     */
    public boolean verify(byte[] message, byte[] signature) {
        try {
            Signature sig = Signature.getInstance(ALGORITHM);
            sig.initVerify(keyPair.getPublic());
            sig.update(message);
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verify signature with external public key
     * @param message Original message
     * @param signature Signature to verify
     * @param publicKey Public key to use for verification
     * @return true if signature is valid
     */
    public boolean verify(String message, byte[] signature, PublicKey publicKey) {
        return verify(message.getBytes(), signature, publicKey);
    }
    
    /**
     * Verify signature with external public key
     * @param message Original message as byte array
     * @param signature Signature to verify
     * @param publicKey Public key to use for verification
     * @return true if signature is valid
     */
    public boolean verify(byte[] message, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance(ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(message);
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Get public key
     * @return Public key
     */
    public PublicKey getPublicKey() {
        return keyPair.getPublic();
    }
    
    /**
     * Get private key
     * @return Private key
     */
    public PrivateKey getPrivateKey() {
        return keyPair.getPrivate();
    }
    
    /**
     * Get key pair
     * @return Key pair
     */
    public KeyPair getKeyPair() {
        return keyPair;
    }
    
    /**
     * Get public key as hex string
     * @return Public key as hex string
     */
    public String getPublicKeyHex() {
        return bytesToHex(keyPair.getPublic().getEncoded());
    }
    
    /**
     * Get private key as hex string
     * @return Private key as hex string
     */
    public String getPrivateKeyHex() {
        return bytesToHex(keyPair.getPrivate().getEncoded());
    }
    
    /**
     * Convert byte array to hex string
     * @param bytes Byte array to convert
     * @return Hex string representation
     */
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
    
    /**
     * Create signature from existing key pair
     * @param keyPair Existing key pair
     * @return New DigitalSignature instance
     */
    public static DigitalSignature fromKeyPair(KeyPair keyPair) {
        return new DigitalSignature(keyPair);
    }
}
