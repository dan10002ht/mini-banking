package com.minibanking.dto;

import com.minibanking.entity.Account;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for creating a new account
 * Only includes necessary fields for account creation
 */
@Schema(description = "Request to create a new account")
public class CreateAccountRequest {
    
    @NotBlank(message = "Account number is required")
    @Schema(description = "Unique account number", example = "ACC001", required = true)
    private String accountNumber;
    
    @NotNull(message = "Customer ID is required")
    @Schema(description = "Customer ID who owns this account", example = "123e4567-e89b-12d3-a456-426614174000", required = true)
    private UUID customerId;
    
    @NotNull(message = "Account type is required")
    @Schema(description = "Type of account", example = "SAVINGS", required = true)
    private Account.AccountType accountType;
    
    @Schema(description = "Currency code", example = "VND", defaultValue = "VND")
    private String currency = "VND";
    
    @Schema(description = "Initial balance", example = "1000.00", defaultValue = "0.00")
    private BigDecimal initialBalance = BigDecimal.ZERO;
    
    @Schema(description = "Credit limit for credit accounts", example = "5000.00", defaultValue = "0.00")
    private BigDecimal creditLimit = BigDecimal.ZERO;
    
    @Schema(description = "Interest rate", example = "0.05", defaultValue = "0.00")
    private BigDecimal interestRate = BigDecimal.ZERO;
    
    // Constructors
    public CreateAccountRequest() {}
    
    public CreateAccountRequest(String accountNumber, UUID customerId, Account.AccountType accountType) {
        this.accountNumber = accountNumber;
        this.customerId = customerId;
        this.accountType = accountType;
    }
    
    // Getters and Setters
    public String getAccountNumber() { return accountNumber; }
    public void setAccountNumber(String accountNumber) { this.accountNumber = accountNumber; }
    
    public UUID getCustomerId() { return customerId; }
    public void setCustomerId(UUID customerId) { this.customerId = customerId; }
    
    public Account.AccountType getAccountType() { return accountType; }
    public void setAccountType(Account.AccountType accountType) { this.accountType = accountType; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public BigDecimal getInitialBalance() { return initialBalance; }
    public void setInitialBalance(BigDecimal initialBalance) { this.initialBalance = initialBalance; }
    
    public BigDecimal getCreditLimit() { return creditLimit; }
    public void setCreditLimit(BigDecimal creditLimit) { this.creditLimit = creditLimit; }
    
    public BigDecimal getInterestRate() { return interestRate; }
    public void setInterestRate(BigDecimal interestRate) { this.interestRate = interestRate; }
}
