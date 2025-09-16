package com.minibanking.crypto;

import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Production Banking Merkle Service
 * Optimized implementation with caching, lazy loading, and memory management
 * Only stores root hashes, builds tree on-demand for verification
 */
@Service
public class BankingMerkleService {
    
    // Cache for root hashes (only metadata)
    private final Map<String, String> rootHashCache = new ConcurrentHashMap<>();
    
    // Cache for Merkle proofs (only when needed)
    private final Map<String, List<String>> proofCache = new ConcurrentHashMap<>();
    
    // Cache for transaction counts per block
    private final Map<String, Integer> transactionCountCache = new ConcurrentHashMap<>();
    
    /**
     * Create Merkle Tree for a block
     * @param blockHash Block identifier
     * @param transactions List of transactions
     * @return Root hash of the Merkle tree
     */
    public String createMerkleTree(String blockHash, List<String> transactions) {
        String rootHash = calculateRootHash(transactions);
        
        // Cache metadata only
        rootHashCache.put(blockHash, rootHash);
        transactionCountCache.put(blockHash, transactions.size());
        
        return rootHash;
    }
    
    /**
     * Get Merkle root hash for a block
     * @param blockHash Block identifier
     * @param transactions List of transactions (if not cached)
     * @return Root hash
     */
    public String getMerkleRoot(String blockHash, List<String> transactions) {
        // Check cache first
        String cachedRoot = rootHashCache.get(blockHash);
        if (cachedRoot != null) {
            return cachedRoot;
        }
        
        // Calculate and cache
        String rootHash = calculateRootHash(transactions);
        rootHashCache.put(blockHash, rootHash);
        transactionCountCache.put(blockHash, transactions.size());
        
        return rootHash;
    }
    
    /**
     * Generate Merkle proof for a transaction
     * @param transactionHash Transaction to prove
     * @param blockHash Block containing the transaction
     * @param allTransactions All transactions in the block
     * @return Merkle proof
     */
    public List<String> generateProof(String transactionHash, String blockHash, List<String> allTransactions) {
        String cacheKey = blockHash + ":" + transactionHash;
        
        // Check proof cache
        List<String> cachedProof = proofCache.get(cacheKey);
        if (cachedProof != null) {
            return cachedProof;
        }
        
        // Generate proof
        List<String> proof = generateMerkleProof(transactionHash, allTransactions);
        
        // Cache proof (with size limit)
        if (proofCache.size() < 10000) { // Limit to 10K proofs
            proofCache.put(cacheKey, proof);
        }
        
        return proof;
    }
    
    /**
     * Verify a transaction using Merkle proof
     * @param transactionHash Transaction to verify
     * @param proof Merkle proof
     * @param blockHash Block containing the transaction
     * @param allTransactions All transactions in the block
     * @return true if transaction is valid
     */
    public boolean verifyTransaction(String transactionHash, List<String> proof, String blockHash, List<String> allTransactions) {
        // Get root hash from cache or calculate
        String rootHash = getMerkleRoot(blockHash, allTransactions);
        
        // Verify using proof
        return verifyMerkleProof(transactionHash, proof, rootHash);
    }
    
    /**
     * Verify that a block's Merkle root is correct
     * @param blockHash Block identifier
     * @param transactions List of transactions
     * @return true if root hash is correct
     */
    public boolean verifyBlockIntegrity(String blockHash, List<String> transactions) {
        String cachedRoot = rootHashCache.get(blockHash);
        if (cachedRoot == null) {
            return false; // Block not found in cache
        }
        
        String calculatedRoot = calculateRootHash(transactions);
        return cachedRoot.equals(calculatedRoot);
    }
    
    /**
     * Get cache statistics
     * @return Cache statistics
     */
    public Map<String, Object> getCacheStats() {
        return Map.of(
            "rootHashCacheSize", rootHashCache.size(),
            "proofCacheSize", proofCache.size(),
            "estimatedMemoryUsage", (rootHashCache.size() * 64) + (proofCache.size() * 200) + " bytes"
        );
    }
    
    /**
     * Clear caches
     */
    public void clearCaches() {
        rootHashCache.clear();
        proofCache.clear();
    }
    
    /**
     * Get memory usage estimate
     * @return Memory usage in bytes
     */
    public long getMemoryUsage() {
        // Root hash cache: 64 bytes per entry (blockHash + rootHash)
        long rootHashMemory = rootHashCache.size() * 64L;
        
        // Proof cache: ~200 bytes per proof (transactionHash + proof path)
        long proofMemory = proofCache.size() * 200L;
        
        return rootHashMemory + proofMemory;
    }
    
    // ==================== CORE MERKLE TREE IMPLEMENTATION ====================
    
    /**
     * Calculate root hash without storing the entire tree
     * @param transactions List of transactions
     * @return Root hash
     */
    private String calculateRootHash(List<String> transactions) {
        if (transactions.isEmpty()) {
            return "";
        }
        
        // Start with leaf nodes (hashed transactions)
        List<String> currentLevel = transactions.stream()
            .map(HashUtils::sha256)
            .collect(Collectors.toList());
        
        // Build tree bottom-up
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size()) 
                    ? currentLevel.get(i + 1) 
                    : left; // Duplicate last element if odd number
                
                nextLevel.add(HashUtils.sha256(left + right));
            }
            
            currentLevel = nextLevel;
        }
        
        return currentLevel.get(0);
    }
    
    /**
     * Generate Merkle proof for a transaction
     * @param transactionHash Transaction to prove
     * @param allTransactions All transactions in the block
     * @return Merkle proof
     */
    private List<String> generateMerkleProof(String transactionHash, List<String> allTransactions) {
        if (allTransactions.isEmpty()) {
            return new ArrayList<>();
        }
        
        // Find transaction index
        int transactionIndex = -1;
        for (int i = 0; i < allTransactions.size(); i++) {
            if (HashUtils.sha256(allTransactions.get(i)).equals(transactionHash)) {
                transactionIndex = i;
                break;
            }
        }
        
        if (transactionIndex == -1) {
            return new ArrayList<>(); // Transaction not found
        }
        
        // Build proof path
        List<String> proof = new ArrayList<>();
        List<String> currentLevel = allTransactions.stream()
            .map(HashUtils::sha256)
            .collect(Collectors.toList());
        
        int currentIndex = transactionIndex;
        
        while (currentLevel.size() > 1) {
            List<String> nextLevel = new ArrayList<>();
            
            for (int i = 0; i < currentLevel.size(); i += 2) {
                String left = currentLevel.get(i);
                String right = (i + 1 < currentLevel.size()) 
                    ? currentLevel.get(i + 1) 
                    : left;
                
                // Add sibling to proof if current node is part of this pair
                if (i == currentIndex) {
                    proof.add(right);
                } else if (i + 1 == currentIndex) {
                    proof.add(left);
                }
                
                nextLevel.add(HashUtils.sha256(left + right));
            }
            
            currentLevel = nextLevel;
            currentIndex = currentIndex / 2;
        }
        
        return proof;
    }
    
    /**
     * Verify transaction using Merkle proof
     * @param transactionHash Transaction to verify
     * @param proof Merkle proof
     * @param rootHash Expected root hash
     * @return true if transaction is valid
     */
    private boolean verifyMerkleProof(String transactionHash, List<String> proof, String rootHash) {
        if (proof.isEmpty()) {
            return false;
        }
        
        // Start with transaction hash
        String currentHash = transactionHash;
        
        // Rebuild hash up the tree using proof
        for (String siblingHash : proof) {
            currentHash = HashUtils.sha256(currentHash + siblingHash);
        }
        
        // Compare with root hash
        return currentHash.equals(rootHash);
    }
    
    /**
     * Get transaction count for a block
     * @param blockHash Block identifier
     * @return Number of transactions
     */
    public int getTransactionCount(String blockHash) {
        return transactionCountCache.getOrDefault(blockHash, 0);
    }
    
    /**
     * Get tree height for a block
     * @param blockHash Block identifier
     * @return Height of the tree
     */
    public int getTreeHeight(String blockHash) {
        int count = getTransactionCount(blockHash);
        if (count == 0) return 0;
        return (int) Math.ceil(Math.log(count) / Math.log(2)) + 1;
    }
}
