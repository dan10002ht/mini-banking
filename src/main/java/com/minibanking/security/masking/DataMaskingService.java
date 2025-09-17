package com.minibanking.security.masking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.regex.Pattern;

/**
 * Data Masking Service for logging and error messages
 */
@Service
public class DataMaskingService {
    
    private static final Logger logger = LoggerFactory.getLogger(DataMaskingService.class);
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile("(^.{2}).*(@.*)");
    private static final Pattern PHONE_PATTERN = Pattern.compile("(^.{3}).*(.{3}$)");
    
    /**
     * Mask account number
     */
    public String maskAccountNumber(String accountNumber) {
        if (accountNumber == null || accountNumber.length() < 4) {
            return "****";
        }
        
        if (accountNumber.length() <= 8) {
            return "****" + accountNumber.substring(accountNumber.length() - 4);
        }
        
        return accountNumber.substring(0, 4) + "****" + accountNumber.substring(accountNumber.length() - 4);
    }
    
    /**
     * Mask customer name
     */
    public String maskCustomerName(String firstName, String lastName) {
        if (firstName == null && lastName == null) {
            return "****";
        }
        
        String maskedFirstName = maskName(firstName);
        String maskedLastName = maskName(lastName);
        
        return maskedFirstName + " " + maskedLastName;
    }
    
    /**
     * Mask individual name
     */
    private String maskName(String name) {
        if (name == null || name.isEmpty()) {
            return "****";
        }
        
        if (name.length() <= 2) {
            return "**";
        }
        
        return name.charAt(0) + "*".repeat(name.length() - 2) + name.charAt(name.length() - 1);
    }
    
    /**
     * Mask email address
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "****@****";
        }
        
        String[] parts = email.split("@");
        String username = parts[0];
        String domain = parts[1];
        
        if (username.length() <= 2) {
            return "**@" + domain;
        }
        
        return username.charAt(0) + "*".repeat(username.length() - 2) + username.charAt(username.length() - 1) + "@" + domain;
    }
    
    /**
     * Mask phone number
     */
    public String maskPhone(String phone) {
        if (phone == null || phone.length() < 6) {
            return "****";
        }
        
        if (phone.length() <= 10) {
            return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 3);
        }
        
        return phone.substring(0, 4) + "****" + phone.substring(phone.length() - 4);
    }
    
    /**
     * Mask amount (for logging)
     */
    public String maskAmount(BigDecimal amount) {
        if (amount == null) {
            return "****";
        }
        
        // Show range instead of exact amount
        if (amount.compareTo(new BigDecimal("10000000")) >= 0) {
            return "10M+";
        } else if (amount.compareTo(new BigDecimal("1000000")) >= 0) {
            return "1M-10M";
        } else if (amount.compareTo(new BigDecimal("100000")) >= 0) {
            return "100K-1M";
        } else {
            return "<100K";
        }
    }
    
    /**
     * Mask transaction description
     */
    public String maskDescription(String description) {
        if (description == null || description.isEmpty()) {
            return "****";
        }
        
        if (description.length() <= 10) {
            return "****";
        }
        
        return description.substring(0, 4) + "****" + description.substring(description.length() - 4);
    }
    
    /**
     * Mask sensitive data in log message
     */
    public String maskLogMessage(String message) {
        if (message == null) {
            return null;
        }
        
        // Mask account numbers (10+ digits)
        message = message.replaceAll("\\b\\d{10,}\\b", this::maskAccountNumber);
        
        // Mask email addresses
        message = message.replaceAll("\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b", 
                                   match -> maskEmail(match.group()));
        
        // Mask phone numbers
        message = message.replaceAll("\\b\\d{10,11}\\b", 
                                   match -> maskPhone(match.group()));
        
        return message;
    }
    
    /**
     * Create masked customer info for logging
     */
    public String createMaskedCustomerInfo(String firstName, String lastName, String email, String phone) {
        return String.format("Customer: %s, Email: %s, Phone: %s",
                           maskCustomerName(firstName, lastName),
                           maskEmail(email),
                           maskPhone(phone));
    }
    
    /**
     * Create masked transaction info for logging
     */
    public String createMaskedTransactionInfo(String fromAccount, String toAccount, BigDecimal amount, String description) {
        return String.format("Transaction: %s -> %s, Amount: %s, Desc: %s",
                           maskAccountNumber(fromAccount),
                           maskAccountNumber(toAccount),
                           maskAmount(amount),
                           maskDescription(description));
    }
}
