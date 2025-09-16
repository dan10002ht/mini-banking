package com.minibanking.crypto;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.DHParameterSpec;
import java.security.*;
import java.security.spec.AlgorithmParameterSpec;
import java.util.Base64;

/**
 * Diffie-Hellman Key Exchange implementation
 * Used for secure key exchange between two parties
 */
public class DiffieHellmanUtils {
    
    private static final String ALGORITHM = "DH";
    private static final int DEFAULT_KEY_SIZE = 2048;
    
    /**
     * Generate Diffie-Hellman key pair with default parameters
     * @return DH key pair
     */
    public static KeyPair generateKeyPair() {
        return generateKeyPair(DEFAULT_KEY_SIZE);
    }
    
    /**
     * Generate Diffie-Hellman key pair with specified key size
     * @param keySize Key size in bits
     * @return DH key pair
     */
    public static KeyPair generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(keySize);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generating DH key pair", e);
        }
    }
    
    /**
     * Generate Diffie-Hellman key pair with custom parameters
     * @param spec DH parameter specification
     * @return DH key pair
     */
    public static KeyPair generateKeyPair(AlgorithmParameterSpec spec) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            keyGen.initialize(spec);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generating DH key pair with custom parameters", e);
        }
    }
    
    /**
     * Perform key agreement between two parties
     * @param privateKey Local private key
     * @param publicKey Remote public key
     * @return Shared secret key
     */
    public static byte[] performKeyAgreement(PrivateKey privateKey, PublicKey publicKey) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance(ALGORITHM);
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            return keyAgreement.generateSecret();
        } catch (Exception e) {
            throw new RuntimeException("Error performing key agreement", e);
        }
    }
    
    /**
     * Perform key agreement and generate shared secret as Base64 string
     * @param privateKey Local private key
     * @param publicKey Remote public key
     * @return Shared secret as Base64 string
     */
    public static String performKeyAgreementBase64(PrivateKey privateKey, PublicKey publicKey) {
        byte[] sharedSecret = performKeyAgreement(privateKey, publicKey);
        return Base64.getEncoder().encodeToString(sharedSecret);
    }
    
    /**
     * Generate shared secret key with specified algorithm
     * @param privateKey Local private key
     * @param publicKey Remote public key
     * @param algorithm Hash algorithm for key derivation
     * @param keyLength Desired key length in bits
     * @return Derived shared secret key
     */
    public static byte[] generateSharedSecret(PrivateKey privateKey, PublicKey publicKey, 
                                           String algorithm, int keyLength) {
        try {
            KeyAgreement keyAgreement = KeyAgreement.getInstance(ALGORITHM);
            keyAgreement.init(privateKey);
            keyAgreement.doPhase(publicKey, true);
            
            // Generate shared secret
            byte[] sharedSecret = keyAgreement.generateSecret();
            
            // Derive key using hash function
            MessageDigest digest = MessageDigest.getInstance(algorithm);
            digest.update(sharedSecret);
            
            // Truncate or pad to desired length
            byte[] derivedKey = digest.digest();
            if (derivedKey.length * 8 > keyLength) {
                byte[] truncated = new byte[keyLength / 8];
                System.arraycopy(derivedKey, 0, truncated, 0, truncated.length);
                return truncated;
            } else if (derivedKey.length * 8 < keyLength) {
                // Pad with zeros (in real implementation, use proper key derivation)
                byte[] padded = new byte[keyLength / 8];
                System.arraycopy(derivedKey, 0, padded, 0, derivedKey.length);
                return padded;
            }
            
            return derivedKey;
        } catch (Exception e) {
            throw new RuntimeException("Error generating shared secret", e);
        }
    }
    
    /**
     * Generate shared secret key using SHA-256
     * @param privateKey Local private key
     * @param publicKey Remote public key
     * @return 256-bit shared secret key
     */
    public static byte[] generateSharedSecret256(PrivateKey privateKey, PublicKey publicKey) {
        return generateSharedSecret(privateKey, publicKey, "SHA-256", 256);
    }
    
    /**
     * Generate shared secret key using SHA-256 as Base64 string
     * @param privateKey Local private key
     * @param publicKey Remote public key
     * @return 256-bit shared secret key as Base64 string
     */
    public static String generateSharedSecret256Base64(PrivateKey privateKey, PublicKey publicKey) {
        byte[] sharedSecret = generateSharedSecret256(privateKey, publicKey);
        return Base64.getEncoder().encodeToString(sharedSecret);
    }
    
    /**
     * Convert public key to Base64 string
     * @param publicKey Public key
     * @return Base64 encoded public key
     */
    public static String publicKeyToBase64(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }
    
    /**
     * Convert private key to Base64 string
     * @param privateKey Private key
     * @return Base64 encoded private key
     */
    public static String privateKeyToBase64(PrivateKey privateKey) {
        return Base64.getEncoder().encodeToString(privateKey.getEncoded());
    }
    
    /**
     * Convert Base64 string to public key
     * @param publicKeyBase64 Base64 encoded public key
     * @return Public key
     */
    public static PublicKey base64ToPublicKey(String publicKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(publicKeyBase64);
            java.security.spec.X509EncodedKeySpec spec = new java.security.spec.X509EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePublic(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error converting Base64 to public key", e);
        }
    }
    
    /**
     * Convert Base64 string to private key
     * @param privateKeyBase64 Base64 encoded private key
     * @return Private key
     */
    public static PrivateKey base64ToPrivateKey(String privateKeyBase64) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(privateKeyBase64);
            java.security.spec.PKCS8EncodedKeySpec spec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance(ALGORITHM);
            return keyFactory.generatePrivate(spec);
        } catch (Exception e) {
            throw new RuntimeException("Error converting Base64 to private key", e);
        }
    }
    
    /**
     * Get key size in bits
     * @param key Key to measure
     * @return Key size in bits
     */
    public static int getKeySize(Key key) {
        if (key instanceof javax.crypto.interfaces.DHPrivateKey) {
            return ((javax.crypto.interfaces.DHPrivateKey) key).getParams().getP().bitLength();
        } else if (key instanceof javax.crypto.interfaces.DHPublicKey) {
            return ((javax.crypto.interfaces.DHPublicKey) key).getParams().getP().bitLength();
        }
        return -1;
    }
    
    /**
     * Demo: Complete key exchange between two parties
     * @return Demo result with shared secret
     */
    public static String demoKeyExchange() {
        try {
            // Party A generates key pair
            KeyPair keyPairA = generateKeyPair();
            String publicKeyABase64 = publicKeyToBase64(keyPairA.getPublic());
            
            // Party B generates key pair
            KeyPair keyPairB = generateKeyPair();
            String publicKeyBBase64 = publicKeyToBase64(keyPairB.getPublic());
            
            // Party A generates shared secret
            byte[] sharedSecretA = performKeyAgreement(keyPairA.getPrivate(), keyPairB.getPublic());
            String sharedSecretABase64 = Base64.getEncoder().encodeToString(sharedSecretA);
            
            // Party B generates shared secret
            byte[] sharedSecretB = performKeyAgreement(keyPairB.getPrivate(), keyPairA.getPublic());
            String sharedSecretBBase64 = Base64.getEncoder().encodeToString(sharedSecretB);
            
            // Verify both parties have the same shared secret
            boolean secretsMatch = java.util.Arrays.equals(sharedSecretA, sharedSecretB);
            
            return String.format(
                "Key Exchange Demo:\n" +
                "Party A Public Key: %s\n" +
                "Party B Public Key: %s\n" +
                "Shared Secret A: %s\n" +
                "Shared Secret B: %s\n" +
                "Secrets Match: %s",
                publicKeyABase64.substring(0, 50) + "...",
                publicKeyBBase64.substring(0, 50) + "...",
                sharedSecretABase64.substring(0, 50) + "...",
                sharedSecretBBase64.substring(0, 50) + "...",
                secretsMatch
            );
        } catch (Exception e) {
            return "Demo failed: " + e.getMessage();
        }
    }
}
