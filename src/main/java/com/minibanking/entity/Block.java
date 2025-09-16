package com.minibanking.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Block entity for blockchain storage
 * Only stores metadata, not the full Merkle tree
 */
@Entity
@Table(name = "blocks")
public class Block {
    
    @Id
    @Column(name = "block_hash", length = 64)
    private String blockHash;
    
    @Column(name = "previous_hash", length = 64)
    private String previousHash;
    
    @Column(name = "merkle_root", length = 64)
    private String merkleRoot;
    
    @Column(name = "timestamp")
    private LocalDateTime timestamp;
    
    @Column(name = "nonce")
    private Integer nonce;
    
    @Column(name = "transaction_count")
    private Integer transactionCount;
    
    @Column(name = "block_height")
    private Long blockHeight;
    
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "block_hash")
    private List<Transaction> transactions;
    
    // Constructors
    public Block() {}
    
    public Block(String blockHash, String previousHash, String merkleRoot, 
                LocalDateTime timestamp, Integer nonce, Integer transactionCount, Long blockHeight) {
        this.blockHash = blockHash;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.timestamp = timestamp;
        this.nonce = nonce;
        this.transactionCount = transactionCount;
        this.blockHeight = blockHeight;
    }
    
    // Getters and Setters
    public String getBlockHash() {
        return blockHash;
    }
    
    public void setBlockHash(String blockHash) {
        this.blockHash = blockHash;
    }
    
    public String getPreviousHash() {
        return previousHash;
    }
    
    public void setPreviousHash(String previousHash) {
        this.previousHash = previousHash;
    }
    
    public String getMerkleRoot() {
        return merkleRoot;
    }
    
    public void setMerkleRoot(String merkleRoot) {
        this.merkleRoot = merkleRoot;
    }
    
    public LocalDateTime getTimestamp() {
        return timestamp;
    }
    
    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
    
    public Integer getNonce() {
        return nonce;
    }
    
    public void setNonce(Integer nonce) {
        this.nonce = nonce;
    }
    
    public Integer getTransactionCount() {
        return transactionCount;
    }
    
    public void setTransactionCount(Integer transactionCount) {
        this.transactionCount = transactionCount;
    }
    
    public Long getBlockHeight() {
        return blockHeight;
    }
    
    public void setBlockHeight(Long blockHeight) {
        this.blockHeight = blockHeight;
    }
    
    public List<Transaction> getTransactions() {
        return transactions;
    }
    
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
    
    @Override
    public String toString() {
        return String.format("Block{blockHash='%s', previousHash='%s', merkleRoot='%s', " +
                "timestamp=%s, nonce=%d, transactionCount=%d, blockHeight=%d}", 
                blockHash, previousHash, merkleRoot, timestamp, nonce, transactionCount, blockHeight);
    }
}
