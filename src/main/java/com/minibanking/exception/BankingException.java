package com.minibanking.exception;

/**
 * Base exception for banking operations
 */
public class BankingException extends RuntimeException {
    
    public BankingException(String message) {
        super(message);
    }
    
    public BankingException(String message, Throwable cause) {
        super(message, cause);
    }
}

