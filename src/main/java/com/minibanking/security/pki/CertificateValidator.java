package com.minibanking.security.pki;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.security.cert.X509Certificate;
import java.security.cert.CertificateException;
import java.util.Date;
import java.util.List;
import java.util.ArrayList;

/**
 * Certificate Validator
 * Validates digital certificates for banking system
 */
@Component
public class CertificateValidator {
    
    private static final Logger logger = LoggerFactory.getLogger(CertificateValidator.class);
    
    @Autowired
    private CertificateAuthority certificateAuthority;
    
    /**
     * Validate certificate against CA
     * @param certificate Certificate to validate
     * @return true if certificate is valid
     */
    public boolean validateCertificate(X509Certificate certificate) {
        try {
            logger.debug("Validating certificate: {}", certificate.getSubjectDN());
            
            // 1. Verify certificate signature
            certificate.verify(certificateAuthority.getCAPublicKey());
            
            // 2. Check certificate validity period
            Date now = new Date();
            if (now.before(certificate.getNotBefore()) || now.after(certificate.getNotAfter())) {
                logger.warn("Certificate is not valid for current date");
                throw new CertificateException("Certificate is not valid for current date");
            }
            
            // 3. Check certificate revocation
            if (isCertificateRevoked(certificate)) {
                logger.warn("Certificate has been revoked");
                throw new CertificateException("Certificate has been revoked");
            }
            
            // 4. Check certificate chain (if applicable)
            if (!validateCertificateChain(certificate)) {
                logger.warn("Certificate chain validation failed");
                throw new CertificateException("Certificate chain validation failed");
            }
            
            logger.debug("Certificate validated successfully");
            return true;
            
        } catch (Exception e) {
            logger.error("Certificate validation failed", e);
            return false;
        }
    }
    
    /**
     * Validate certificate chain
     * @param certificate Certificate to validate
     * @return true if certificate chain is valid
     */
    public boolean validateCertificateChain(X509Certificate certificate) {
        try {
            // In a real implementation, this would validate the entire certificate chain
            // For now, we'll just check if the certificate is issued by our CA
            return certificate.getIssuerDN().equals(certificateAuthority.getCACertificate().getSubjectDN());
        } catch (Exception e) {
            logger.error("Error validating certificate chain", e);
            return false;
        }
    }
    
    /**
     * Check if certificate is revoked
     * @param certificate Certificate to check
     * @return true if certificate is revoked
     */
    public boolean isCertificateRevoked(X509Certificate certificate) {
        try {
            // In a real implementation, this would check CRL or OCSP
            // For now, we'll use a simplified check
            return checkRevocationList(certificate);
        } catch (Exception e) {
            logger.error("Error checking certificate revocation", e);
            return false;
        }
    }
    
    /**
     * Validate certificate for specific purpose
     * @param certificate Certificate to validate
     * @param purpose Purpose (e.g., "clientAuth", "serverAuth")
     * @return true if certificate is valid for purpose
     */
    public boolean validateCertificateForPurpose(X509Certificate certificate, String purpose) {
        try {
            // First, validate basic certificate
            if (!validateCertificate(certificate)) {
                return false;
            }
            
            // Check extended key usage
            List<String> keyUsages = getKeyUsages(certificate);
            return keyUsages.contains(purpose);
            
        } catch (Exception e) {
            logger.error("Error validating certificate for purpose: {}", purpose, e);
            return false;
        }
    }
    
    /**
     * Get certificate key usages
     * @param certificate Certificate
     * @return List of key usages
     */
    private List<String> getKeyUsages(X509Certificate certificate) {
        List<String> keyUsages = new ArrayList<>();
        
        try {
            // In a real implementation, this would extract actual key usages
            // For now, we'll return common banking purposes
            keyUsages.add("clientAuth");
            keyUsages.add("serverAuth");
            keyUsages.add("digitalSignature");
            keyUsages.add("keyEncipherment");
            
        } catch (Exception e) {
            logger.error("Error getting key usages", e);
        }
        
        return keyUsages;
    }
    
    /**
     * Check revocation list
     * @param certificate Certificate to check
     * @return true if certificate is revoked
     */
    private boolean checkRevocationList(X509Certificate certificate) {
        // Simplified revocation check
        // In real implementation, check CRL or OCSP
        try {
            // Mock implementation - in production, check actual CRL
            return false;
        } catch (Exception e) {
            logger.error("Error checking revocation list", e);
            return false;
        }
    }
    
    /**
     * Validate certificate for banking operations
     * @param certificate Certificate to validate
     * @return true if certificate is valid for banking
     */
    public boolean validateBankingCertificate(X509Certificate certificate) {
        try {
            logger.info("Validating certificate for banking operations");
            
            // 1. Basic certificate validation
            if (!validateCertificate(certificate)) {
                return false;
            }
            
            // 2. Check if certificate is valid for banking purposes
            if (!validateCertificateForPurpose(certificate, "clientAuth")) {
                logger.warn("Certificate not valid for client authentication");
                return false;
            }
            
            // 3. Check certificate strength (minimum 2048-bit RSA)
            if (certificate.getPublicKey().getAlgorithm().equals("RSA")) {
                // In real implementation, check actual key size
                logger.debug("RSA certificate detected");
            }
            
            logger.info("Certificate validated for banking operations");
            return true;
            
        } catch (Exception e) {
            logger.error("Error validating banking certificate", e);
            return false;
        }
    }
}
