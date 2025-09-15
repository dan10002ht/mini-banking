package com.minibanking.exception;

/**
 * Exception thrown when account has insufficient funds
 */
public class InsufficientFundsException extends BankingException {
    
    public InsufficientFundsException(String message) {
        super(message);
    }
    
    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }
}

