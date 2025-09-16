package com.minibanking.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Test memory usage comparison between different Merkle Tree implementations
 */
public class MemoryUsageTest {
    
    @Test
    public void testMemoryUsageComparison() {
        // Create test transactions
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100 BTC",
            "TX2: Bob -> Charlie 50 BTC",
            "TX3: Charlie -> Alice 25 BTC",
            "TX4: Alice -> David 75 BTC",
            "TX5: David -> Eve 30 BTC"
        );
        
        // Test original MerkleTree (stores full tree)
        MerkleTree originalTree = new MerkleTree(transactions);
        
        // Test OptimizedMerkleTree (only stores root hash)
        OptimizedMerkleTree optimizedTree = new OptimizedMerkleTree(transactions);
        
        // Test BankingMerkleService
        BankingMerkleService bankingService = new BankingMerkleService();
        bankingService.createMerkleTree("block1", transactions);
        
        // Compare memory usage
        System.out.println("=== Memory Usage Comparison ===");
        System.out.println("Original MerkleTree:");
        System.out.println("  - Root hash: " + originalTree.getRootHash());
        System.out.println("  - Tree height: " + originalTree.getHeight());
        System.out.println("  - Estimated memory: " + estimateOriginalMemory(transactions.size()) + " bytes");
        
        System.out.println("\nOptimizedMerkleTree:");
        System.out.println("  - Root hash: " + optimizedTree.getRootHash());
        System.out.println("  - Transaction count: " + optimizedTree.getTransactionCount());
        System.out.println("  - Memory usage: " + optimizedTree.getMemoryUsage() + " bytes");
        
        System.out.println("\nBankingMerkleService:");
        System.out.println("  - Memory usage: " + bankingService.getMemoryUsage() + " bytes");
        System.out.println("  - Cache stats: " + bankingService.getCacheStats());
        
        // Verify functionality
        String testTransaction = "TX1: Alice -> Bob 100 BTC";
        
        // Original tree verification
        List<String> originalProof = originalTree.generateProof(testTransaction);
        boolean originalValid = originalTree.verifyTransaction(testTransaction, originalProof);
        
        // Optimized tree verification
        List<String> optimizedProof = optimizedTree.generateProof(testTransaction, transactions);
        boolean optimizedValid = optimizedTree.verifyTransaction(testTransaction, optimizedProof, transactions);
        
        // Banking service verification
        List<String> bankingProof = bankingService.generateProof(testTransaction, "block1", transactions);
        boolean bankingValid = bankingService.verifyTransaction(testTransaction, bankingProof, "block1", transactions);
        
        // All should be valid
        assertTrue(originalValid);
        assertTrue(optimizedValid);
        assertTrue(bankingValid);
        
        // Root hashes should be the same
        assertEquals(originalTree.getRootHash(), optimizedTree.getRootHash());
        
        System.out.println("\n=== Verification Results ===");
        System.out.println("Original tree verification: " + originalValid);
        System.out.println("Optimized tree verification: " + optimizedValid);
        System.out.println("Banking service verification: " + bankingValid);
    }
    
    @Test
    public void testLargeScaleMemoryUsage() {
        // Simulate large scale (1000 transactions)
        List<String> largeTransactions = new ArrayList<>();
        for (int i = 1; i <= 1000; i++) {
            largeTransactions.add("TX" + i + ": Account" + (i % 100) + " -> Account" + ((i + 1) % 100) + " " + (i * 10) + " BTC");
        }
        
        System.out.println("\n=== Large Scale Memory Usage (1000 transactions) ===");
        
        // Test OptimizedMerkleTree
        OptimizedMerkleTree optimizedTree = new OptimizedMerkleTree(largeTransactions);
        System.out.println("OptimizedMerkleTree:");
        System.out.println("  - Memory usage: " + optimizedTree.getMemoryUsage() + " bytes");
        System.out.println("  - Transaction count: " + optimizedTree.getTransactionCount());
        
        // Test BankingMerkleService
        BankingMerkleService bankingService = new BankingMerkleService();
        bankingService.createMerkleTree("large_block", largeTransactions);
        System.out.println("BankingMerkleService:");
        System.out.println("  - Memory usage: " + bankingService.getMemoryUsage() + " bytes");
        System.out.println("  - Cache stats: " + bankingService.getCacheStats());
        
        // Estimate original tree memory (if it existed)
        long estimatedOriginalMemory = estimateOriginalMemory(largeTransactions.size());
        System.out.println("Estimated Original MerkleTree memory: " + estimatedOriginalMemory + " bytes");
        
        // Calculate memory savings
        long savings = estimatedOriginalMemory - optimizedTree.getMemoryUsage();
        double savingsPercent = (double) savings / estimatedOriginalMemory * 100;
        
        System.out.println("Memory savings: " + savings + " bytes (" + String.format("%.2f", savingsPercent) + "%)");
        
        // Verify functionality still works
        String testTransaction = largeTransactions.get(500);
        List<String> proof = optimizedTree.generateProof(testTransaction, largeTransactions);
        boolean isValid = optimizedTree.verifyTransaction(testTransaction, proof, largeTransactions);
        
        assertTrue(isValid);
        System.out.println("Verification with 1000 transactions: " + isValid);
    }
    
    private long estimateOriginalMemory(int transactionCount) {
        // Original MerkleTree stores full tree structure
        // Each level has approximately transactionCount/2^i nodes
        // Each node is 64 bytes (32-byte hash)
        long totalNodes = 0;
        int levelCount = (int) Math.ceil(Math.log(transactionCount) / Math.log(2)) + 1;
        
        for (int i = 0; i < levelCount; i++) {
            int nodesAtLevel = (int) Math.ceil((double) transactionCount / Math.pow(2, i));
            totalNodes += nodesAtLevel;
        }
        
        return totalNodes * 64; // 64 bytes per hash
    }
}
