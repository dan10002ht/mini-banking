package com.minibanking.blockchain;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Block entity for Blockchain
 */
@Entity
@Table(name = "blocks")
public class Block {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "block_id")
    private UUID blockId;
    
    @Column(name = "block_number", nullable = false, unique = true)
    private Long blockNumber;
    
    @Column(name = "previous_hash", length = 64)
    private String previousHash;
    
    @Column(name = "merkle_root", length = 64, nullable = false)
    private String merkleRoot;
    
    @Column(name = "block_hash", length = 64, nullable = false, unique = true)
    private String blockHash;
    
    @Column(name = "nonce")
    private Long nonce = 0L;
    
    @Column(name = "difficulty")
    private Integer difficulty = 4; // Number of leading zeros required
    
    @Column(name = "transaction_count")
    private Integer transactionCount = 0;
    
    @CreationTimestamp
    @Column(name = "timestamp", nullable = false)
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime timestamp;
    
    @Column(name = "size_bytes")
    private Long sizeBytes = 0L;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20)
    private BlockStatus status = BlockStatus.PENDING;
    
    // Constructors
    public Block() {}
    
    public Block(Long blockNumber, String previousHash, String merkleRoot) {
        this.blockNumber = blockNumber;
        this.previousHash = previousHash;
        this.merkleRoot = merkleRoot;
        this.timestamp = LocalDateTime.now();
    }
    
    // Business Methods
    public void incrementNonce() {
        this.nonce++;
    }
    
    public void setMined(String blockHash) {
        this.blockHash = blockHash;
        this.status = BlockStatus.MINED;
    }
    
    public boolean isValid() {
        return blockHash != null && 
               blockHash.startsWith("0".repeat(difficulty)) &&
               status == BlockStatus.MINED;
    }
    
    // Getters and Setters
    public UUID getBlockId() { return blockId; }
    public void setBlockId(UUID blockId) { this.blockId = blockId; }
    
    public Long getBlockNumber() { return blockNumber; }
    public void setBlockNumber(Long blockNumber) { this.blockNumber = blockNumber; }
    
    public String getPreviousHash() { return previousHash; }
    public void setPreviousHash(String previousHash) { this.previousHash = previousHash; }
    
    public String getMerkleRoot() { return merkleRoot; }
    public void setMerkleRoot(String merkleRoot) { this.merkleRoot = merkleRoot; }
    
    public String getBlockHash() { return blockHash; }
    public void setBlockHash(String blockHash) { this.blockHash = blockHash; }
    
    public Long getNonce() { return nonce; }
    public void setNonce(Long nonce) { this.nonce = nonce; }
    
    public Integer getDifficulty() { return difficulty; }
    public void setDifficulty(Integer difficulty) { this.difficulty = difficulty; }
    
    public Integer getTransactionCount() { return transactionCount; }
    public void setTransactionCount(Integer transactionCount) { this.transactionCount = transactionCount; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }
    
    public Long getSizeBytes() { return sizeBytes; }
    public void setSizeBytes(Long sizeBytes) { this.sizeBytes = sizeBytes; }
    
    public BlockStatus getStatus() { return status; }
    public void setStatus(BlockStatus status) { this.status = status; }
    
    // Enums
    public enum BlockStatus {
        PENDING,    // Block is being created
        MINING,     // Block is being mined
        MINED,      // Block has been successfully mined
        INVALID     // Block is invalid
    }
    
    @Override
    public String toString() {
        return "Block{" +
                "blockId=" + blockId +
                ", blockNumber=" + blockNumber +
                ", previousHash='" + previousHash + '\'' +
                ", merkleRoot='" + merkleRoot + '\'' +
                ", blockHash='" + blockHash + '\'' +
                ", nonce=" + nonce +
                ", difficulty=" + difficulty +
                ", transactionCount=" + transactionCount +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }
}
