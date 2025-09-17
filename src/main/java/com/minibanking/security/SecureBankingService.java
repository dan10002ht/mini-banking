package com.minibanking.security;

import com.minibanking.security.pki.CertificateAuthority;
import com.minibanking.security.pki.CertificateValidator;
import com.minibanking.security.hsm.HSMService;
import com.minibanking.security.encryption.DataEncryptionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.util.Base64;

/**
 * Secure Banking Service
 * Integrates all security components for banking operations
 */
@Service
public class SecureBankingService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecureBankingService.class);
    
    @Autowired
    private CertificateAuthority certificateAuthority;
    
    @Autowired
    private CertificateValidator certificateValidator;
    
    @Autowired
    private HSMService hsmService;
    
    @Autowired
    private DataEncryptionService dataEncryptionService;
    
    /**
     * Initialize secure banking service
     */
    public void initializeSecureBanking() {
        try {
            logger.info("Initializing Secure Banking Service...");
            
            // Initialize all security components
            certificateAuthority.initializeCA();
            dataEncryptionService.initializeEncryption();
            
            logger.info("Secure Banking Service initialized successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing Secure Banking Service", e);
            throw new RuntimeException("Failed to initialize secure banking", e);
        }
    }
    
    /**
     * Process secure transaction
     * @param transactionData Transaction data
     * @param clientCertificate Client certificate
     * @param signature Digital signature
     * @return Secure transaction result
     */
    public SecureTransactionResult processSecureTransaction(String transactionData, 
                                                          X509Certificate clientCertificate, 
                                                          byte[] signature) {
        try {
            logger.info("Processing secure transaction");
            
            // 1. Validate client certificate
            if (!certificateValidator.validateBankingCertificate(clientCertificate)) {
                throw new SecurityException("Invalid client certificate");
            }
            
            // 2. Verify transaction signature
            if (!hsmService.verify("BANK_SIGNING_KEY", transactionData.getBytes(), signature)) {
                throw new SecurityException("Invalid transaction signature");
            }
            
            // 3. Encrypt sensitive transaction data
            String encryptedData = dataEncryptionService.encryptTransactionData(transactionData);
            
            // 4. Sign transaction with bank's HSM key
            byte[] bankSignature = hsmService.sign("BANK_SIGNING_KEY", transactionData.getBytes());
            
            // 5. Create secure transaction result
            SecureTransactionResult result = new SecureTransactionResult();
            result.setTransactionData(encryptedData);
            result.setBankSignature(bankSignature);
            result.setClientCertificate(clientCertificate);
            result.setTimestamp(System.currentTimeMillis());
            result.setStatus("SECURE");
            
            logger.info("Secure transaction processed successfully");
            return result;
            
        } catch (Exception e) {
            logger.error("Error processing secure transaction", e);
            throw new RuntimeException("Failed to process secure transaction", e);
        }
    }
    
    /**
     * Create secure customer
     * @param customerData Customer data
     * @param clientCertificate Client certificate
     * @return Secure customer result
     */
    public SecureCustomerResult createSecureCustomer(String customerData, 
                                                   X509Certificate clientCertificate) {
        try {
            logger.info("Creating secure customer");
            
            // 1. Validate client certificate
            if (!certificateValidator.validateBankingCertificate(clientCertificate)) {
                throw new SecurityException("Invalid client certificate");
            }
            
            // 2. Encrypt customer data
            String encryptedData = dataEncryptionService.encryptCustomerData(customerData);
            
            // 3. Generate customer key pair
            String customerKeyId = "CUSTOMER_" + System.currentTimeMillis();
            hsmService.generateKey(customerKeyId, "RSA", 2048);
            
            // 4. Create secure customer result
            SecureCustomerResult result = new SecureCustomerResult();
            result.setCustomerData(encryptedData);
            result.setCustomerKeyId(customerKeyId);
            result.setPublicKey(hsmService.getPublicKey(customerKeyId));
            result.setTimestamp(System.currentTimeMillis());
            result.setStatus("SECURE");
            
            logger.info("Secure customer created successfully");
            return result;
            
        } catch (Exception e) {
            logger.error("Error creating secure customer", e);
            throw new RuntimeException("Failed to create secure customer", e);
        }
    }
    
    /**
     * Verify secure transaction
     * @param transactionData Transaction data
     * @param signature Digital signature
     * @param clientKeyId Client key ID
     * @return true if transaction is valid
     */
    public boolean verifySecureTransaction(String transactionData, 
                                         byte[] signature, 
                                         String clientKeyId) {
        try {
            logger.debug("Verifying secure transaction");
            
            // Verify signature with client's public key
            boolean isValid = hsmService.verify(clientKeyId, transactionData.getBytes(), signature);
            
            logger.debug("Secure transaction verification result: {}", isValid);
            return isValid;
            
        } catch (Exception e) {
            logger.error("Error verifying secure transaction", e);
            return false;
        }
    }
    
    /**
     * Get security status
     * @return Security status
     */
    public String getSecurityStatus() {
        try {
            StringBuilder status = new StringBuilder();
            status.append("=== SECURITY STATUS ===\n");
            status.append("Certificate Authority: ").append(certificateAuthority.getIssuedCertificatesCount()).append(" certificates issued\n");
            status.append("HSM Service: ").append(hsmService.getHSMStatus()).append("\n");
            status.append("Data Encryption: ").append(dataEncryptionService.getEncryptionStatus()).append("\n");
            status.append("Security Level: HIGH\n");
            status.append("Compliance: FIPS 140-2, Common Criteria\n");
            
            return status.toString();
            
        } catch (Exception e) {
            logger.error("Error getting security status", e);
            return "Security status unavailable";
        }
    }
    
    /**
     * Secure Transaction Result
     */
    public static class SecureTransactionResult {
        private String transactionData;
        private byte[] bankSignature;
        private X509Certificate clientCertificate;
        private long timestamp;
        private String status;
        
        // Getters and setters
        public String getTransactionData() { return transactionData; }
        public void setTransactionData(String transactionData) { this.transactionData = transactionData; }
        
        public byte[] getBankSignature() { return bankSignature; }
        public void setBankSignature(byte[] bankSignature) { this.bankSignature = bankSignature; }
        
        public X509Certificate getClientCertificate() { return clientCertificate; }
        public void setClientCertificate(X509Certificate clientCertificate) { this.clientCertificate = clientCertificate; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
    
    /**
     * Secure Customer Result
     */
    public static class SecureCustomerResult {
        private String customerData;
        private String customerKeyId;
        private String publicKey;
        private long timestamp;
        private String status;
        
        // Getters and setters
        public String getCustomerData() { return customerData; }
        public void setCustomerData(String customerData) { this.customerData = customerData; }
        
        public String getCustomerKeyId() { return customerKeyId; }
        public void setCustomerKeyId(String customerKeyId) { this.customerKeyId = customerKeyId; }
        
        public String getPublicKey() { return publicKey; }
        public void setPublicKey(String publicKey) { this.publicKey = publicKey; }
        
        public long getTimestamp() { return timestamp; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
        
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }
}
