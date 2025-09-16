package com.minibanking.crypto;

import java.security.*;
import java.security.spec.RSAKeyGenParameterSpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

/**
 * RSA (Rivest-Shamir-Adleman) implementation
 * Used for encryption, decryption, and digital signatures
 */
public class RSAUtils {
    
    private static final String ALGORITHM = "RSA";
    private static final String SIGNATURE_ALGORITHM = "SHA256withRSA";
    private static final int DEFAULT_KEY_SIZE = 2048;
    
    /**
     * Generate RSA key pair
     * @param keySize Key size in bits (1024, 2048, 4096)
     * @return RSA key pair
     */
    public static KeyPair generateKeyPair(int keySize) {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance(ALGORITHM);
            RSAKeyGenParameterSpec spec = new RSAKeyGenParameterSpec(keySize, RSAKeyGenParameterSpec.F4);
            keyGen.initialize(spec);
            return keyGen.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }
    
    /**
     * Generate RSA key pair with default key size (2048 bits)
     * @return RSA key pair
     */
    public static KeyPair generateKeyPair() {
        return generateKeyPair(DEFAULT_KEY_SIZE);
    }
    
    /**
     * Encrypt data with RSA public key
     * @param data Data to encrypt
     * @param publicKey Public key for encryption
     * @return Encrypted data
     */
    public static byte[] encrypt(byte[] data, PublicKey publicKey) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
            cipher.init(javax.crypto.Cipher.ENCRYPT_MODE, publicKey);
            return cipher.doFinal(data);
        } catch (Exception e) {
            throw new RuntimeException("Error encrypting data", e);
        }
    }
    
    /**
     * Encrypt string with RSA public key
     * @param data String to encrypt
     * @param publicKey Public key for encryption
     * @return Encrypted data as Base64 string
     */
    public static String encrypt(String data, PublicKey publicKey) {
        byte[] encrypted = encrypt(data.getBytes(), publicKey);
        return Base64.getEncoder().encodeToString(encrypted);
    }
    
    /**
     * Decrypt data with RSA private key
     * @param encryptedData Encrypted data
     * @param privateKey Private key for decryption
     * @return Decrypted data
     */
    public static byte[] decrypt(byte[] encryptedData, PrivateKey privateKey) {
        try {
            javax.crypto.Cipher cipher = javax.crypto.Cipher.getInstance(ALGORITHM);
            cipher.init(javax.crypto.Cipher.DECRYPT_MODE, privateKey);
            return cipher.doFinal(encryptedData);
        } catch (Exception e) {
            throw new RuntimeException("Error decrypting data", e);
        }
    }
    
    /**
     * Decrypt Base64 string with RSA private key
     * @param encryptedData Encrypted data as Base64 string
     * @param privateKey Private key for decryption
     * @return Decrypted string
     */
    public static String decrypt(String encryptedData, PrivateKey privateKey) {
        byte[] encrypted = Base64.getDecoder().decode(encryptedData);
        byte[] decrypted = decrypt(encrypted, privateKey);
        return new String(decrypted);
    }
    
    /**
     * Sign data with RSA private key
     * @param data Data to sign
     * @param privateKey Private key for signing
     * @return Digital signature
     */
    public static byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance(SIGNATURE_ALGORITHM);
            signature.initSign(privateKey);
            signature.update(data);
            return signature.sign();
        } catch (Exception e) {
            throw new RuntimeException("Error signing data", e);
        }
    }
    
    /**
     * Sign string with RSA private key
     * @param data String to sign
     * @param privateKey Private key for signing
     * @return Digital signature as Base64 string
     */
    public static String sign(String data, PrivateKey privateKey) {
        byte[] signature = sign(data.getBytes(), privateKey);
        return Base64.getEncoder().encodeToString(signature);
    }
    
    /**
     * Verify signature with RSA public key
     * @param data Original data
     * @param signature Digital signature
     * @param publicKey Public key for verification
     * @return true if signature is valid
     */
    public static boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            Signature sig = Signature.getInstance(SIGNATURE_ALGORITHM);
            sig.initVerify(publicKey);
            sig.update(data);
            return sig.verify(signature);
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Verify Base64 signature with RSA public key
     * @param data Original data
     * @param signature Digital signature as Base64 string
     * @param publicKey Public key for verification
     * @return true if signature is valid
     */
    public static boolean verify(String data, String signature, PublicKey publicKey) {
        byte[] sigBytes = Base64.getDecoder().decode(signature);
        return verify(data.getBytes(), sigBytes, publicKey);
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
            X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
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
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(keyBytes);
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
        if (key instanceof java.security.interfaces.RSAPrivateKey) {
            return ((java.security.interfaces.RSAPrivateKey) key).getModulus().bitLength();
        } else if (key instanceof java.security.interfaces.RSAPublicKey) {
            return ((java.security.interfaces.RSAPublicKey) key).getModulus().bitLength();
        }
        return -1;
    }
    
    /**
     * Get maximum data size that can be encrypted with this key
     * @param keySize Key size in bits
     * @return Maximum data size in bytes
     */
    public static int getMaxDataSize(int keySize) {
        return (keySize / 8) - 42; // PKCS1 padding overhead
    }
    
    /**
     * Check if data size is within encryption limits
     * @param data Data to check
     * @param keySize Key size in bits
     * @return true if data can be encrypted
     */
    public static boolean isDataSizeValid(byte[] data, int keySize) {
        return data.length <= getMaxDataSize(keySize);
    }
}
