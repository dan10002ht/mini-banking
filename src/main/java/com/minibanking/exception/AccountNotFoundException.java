package com.minibanking.exception;

/**
 * Exception thrown when account is not found
 */
public class AccountNotFoundException extends BankingException {
    
    public AccountNotFoundException(String message) {
        super(message);
    }
    
    public AccountNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

