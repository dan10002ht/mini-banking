package com.minibanking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

/**
 * Secure Transfer Request with encrypted sensitive data
 */
public class SecureTransferRequest {
    
    @NotBlank(message = "Encrypted data is required")
    private String encryptedData;
    
    @NotBlank(message = "Encryption key is required")
    private String encryptionKey;
    
    @NotBlank(message = "Signature is required")
    private String signature;
    
    // Non-sensitive data (plain text)
    private String requestId;
    private String timestamp;
    private String clientType;
    
    // Constructors
    public SecureTransferRequest() {}
    
    public SecureTransferRequest(String encryptedData, String encryptionKey, String signature) {
        this.encryptedData = encryptedData;
        this.encryptionKey = encryptionKey;
        this.signature = signature;
    }
    
    // Getters and Setters
    public String getEncryptedData() { return encryptedData; }
    public void setEncryptedData(String encryptedData) { this.encryptedData = encryptedData; }
    
    public String getEncryptionKey() { return encryptionKey; }
    public void setEncryptionKey(String encryptionKey) { this.encryptionKey = encryptionKey; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }
    
    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
    
    public String getClientType() { return clientType; }
    public void setClientType(String clientType) { this.clientType = clientType; }
}
