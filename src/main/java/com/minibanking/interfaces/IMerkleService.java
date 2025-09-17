package com.minibanking.interfaces;

import java.util.List;
import java.util.Map;

/**
 * Merkle Service Interface
 * Defines contract for Merkle tree operations in banking system
 */
public interface IMerkleService {
    
    // ==================== MERKLE TREE CREATION ====================
    
    /**
     * Create Merkle Tree for a block
     * @param blockHash Block identifier
     * @param transactions List of transactions
     * @return Root hash of the Merkle tree
     */
    String createMerkleTree(String blockHash, List<String> transactions);
    
    /**
     * Get Merkle root hash for a block
     * @param blockHash Block identifier
     * @param transactions List of transactions (if not cached)
     * @return Root hash
     */
    String getMerkleRoot(String blockHash, List<String> transactions);
    
    // ==================== MERKLE PROOF OPERATIONS ====================
    
    /**
     * Generate Merkle proof for a transaction
     * @param transactionHash Transaction to prove
     * @param blockHash Block containing the transaction
     * @param allTransactions All transactions in the block
     * @return Merkle proof
     */
    List<String> generateProof(String transactionHash, String blockHash, List<String> allTransactions);
    
    /**
     * Verify a transaction using Merkle proof
     * @param transactionHash Transaction to verify
     * @param proof Merkle proof
     * @param blockHash Block containing the transaction
     * @param allTransactions All transactions in the block
     * @return true if transaction is valid
     */
    boolean verifyTransaction(String transactionHash, List<String> proof, String blockHash, List<String> allTransactions);
    
    // ==================== BLOCK INTEGRITY ====================
    
    /**
     * Verify that a block's Merkle root is correct
     * @param blockHash Block identifier
     * @param transactions List of transactions
     * @return true if root hash is correct
     */
    boolean verifyBlockIntegrity(String blockHash, List<String> transactions);
    
    // ==================== CACHE MANAGEMENT ====================
    
    /**
     * Get cache statistics
     * @return Cache statistics
     */
    Map<String, Object> getCacheStats();
    
    /**
     * Get memory usage estimate
     * @return Memory usage in bytes
     */
    long getMemoryUsage();
    
    /**
     * Clear caches
     */
    void clearCaches();
    
    // ==================== BLOCK METADATA ====================
    
    /**
     * Get transaction count for a block
     * @param blockHash Block identifier
     * @return Number of transactions
     */
    int getTransactionCount(String blockHash);
    
    /**
     * Get tree height for a block
     * @param blockHash Block identifier
     * @return Height of the tree
     */
    int getTreeHeight(String blockHash);
}
