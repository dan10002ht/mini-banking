package com.minibanking.blockchain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.minibanking.entity.Transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Transaction Event for Redis Streams
 */
public class TransactionEvent {
    
    private UUID transactionId;
    private String transactionCode;
    private UUID fromAccountId;
    private String fromAccountNumber;
    private UUID toAccountId;
    private String toAccountNumber;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String transactionType;
    private String status;
    
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    private String signature; // Digital signature for verification
    private String merkleProof; // Merkle proof for inclusion verification
    
    // Constructors
    public TransactionEvent() {}
    
    public TransactionEvent(Transaction transaction) {
        this.transactionId = transaction.getTransactionId();
        this.transactionCode = transaction.getTransactionCode();
        this.fromAccountId = transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountId() : null;
        this.fromAccountNumber = transaction.getFromAccount() != null ? transaction.getFromAccount().getAccountNumber() : null;
        this.toAccountId = transaction.getToAccount() != null ? transaction.getToAccount().getAccountId() : null;
        this.toAccountNumber = transaction.getToAccount() != null ? transaction.getToAccount().getAccountNumber() : null;
        this.amount = transaction.getAmount();
        this.currency = transaction.getCurrency();
        this.description = transaction.getDescription();
        this.transactionType = transaction.getTransactionType().name();
        this.status = transaction.getStatus().name();
        this.timestamp = transaction.getCreatedAt();
    }
    
    // Getters and Setters
    public UUID getTransactionId() { return transactionId; }
    public void setTransactionId(UUID transactionId) { this.transactionId = transactionId; }
    
    public String getTransactionCode() { return transactionCode; }
    public void setTransactionCode(String transactionCode) { this.transactionCode = transactionCode; }
    
    public UUID getFromAccountId() { return fromAccountId; }
    public void setFromAccountId(UUID fromAccountId) { this.fromAccountId = fromAccountId; }
    
    public String getFromAccountNumber() { return fromAccountNumber; }
    public void setFromAccountNumber(String fromAccountNumber) { this.fromAccountNumber = fromAccountNumber; }
    
    public UUID getToAccountId() { return toAccountId; }
    public void setToAccountId(UUID toAccountId) { this.toAccountId = toAccountId; }
    
    public String getToAccountNumber() { return toAccountNumber; }
    public void setToAccountNumber(String toAccountNumber) { this.toAccountNumber = toAccountNumber; }
    
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    
    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }
    
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    
    public String getTransactionType() { return transactionType; }
    public void setTransactionType(String transactionType) { this.transactionType = transactionType; }
    
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public String getSignature() { return signature; }
    public void setSignature(String signature) { this.signature = signature; }
    
    public String getMerkleProof() { return merkleProof; }
    public void setMerkleProof(String merkleProof) { this.merkleProof = merkleProof; }
    
    @Override
    public String toString() {
        return "TransactionEvent{" +
                "transactionId=" + transactionId +
                ", transactionCode='" + transactionCode + '\'' +
                ", fromAccountNumber='" + fromAccountNumber + '\'' +
                ", toAccountNumber='" + toAccountNumber + '\'' +
                ", amount=" + amount +
                ", currency='" + currency + '\'' +
                ", description='" + description + '\'' +
                ", transactionType='" + transactionType + '\'' +
                ", status='" + status + '\'' +
                ", timestamp=" + timestamp +
                '}';
    }
}
