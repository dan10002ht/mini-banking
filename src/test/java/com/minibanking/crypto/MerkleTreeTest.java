package com.minibanking.crypto;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * Test cases for MerkleTree
 */
public class MerkleTreeTest {
    
    @Test
    public void testEmptyTree() {
        MerkleTree tree = new MerkleTree(new ArrayList<>());
        assertTrue(tree.isEmpty());
        assertEquals("", tree.getRootHash());
    }
    
    @Test
    public void testSingleTransaction() {
        List<String> transactions = Arrays.asList("TX1: Alice -> Bob 100");
        MerkleTree tree = new MerkleTree(transactions);
        
        assertFalse(tree.isEmpty());
        assertNotNull(tree.getRootHash());
        assertEquals(64, tree.getRootHash().length()); // 256-bit hash = 64 hex chars
        assertEquals(1, tree.getHeight());
    }
    
    @Test
    public void testTwoTransactions() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        assertNotNull(tree.getRootHash());
        assertEquals(64, tree.getRootHash().length());
        assertEquals(2, tree.getHeight());
    }
    
    @Test
    public void testFourTransactions() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50",
            "TX3: Charlie -> Alice 25",
            "TX4: Alice -> David 75"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        assertNotNull(tree.getRootHash());
        assertEquals(64, tree.getRootHash().length());
        assertEquals(3, tree.getHeight());
    }
    
    @Test
    public void testFiveTransactions() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50",
            "TX3: Charlie -> Alice 25",
            "TX4: Alice -> David 75",
            "TX5: David -> Eve 30"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        assertNotNull(tree.getRootHash());
        assertEquals(64, tree.getRootHash().length());
        assertEquals(4, tree.getHeight());
    }
    
    @Test
    public void testVerifyTransaction() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50",
            "TX3: Charlie -> Alice 25",
            "TX4: Alice -> David 75"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        // Generate proof for TX1
        List<String> proof = tree.generateProof("TX1: Alice -> Bob 100");
        
        // Verify TX1 is in the tree
        assertTrue(tree.verifyTransaction("TX1: Alice -> Bob 100", proof));
        
        // Verify TX2 is in the tree
        List<String> proof2 = tree.generateProof("TX2: Bob -> Charlie 50");
        assertTrue(tree.verifyTransaction("TX2: Bob -> Charlie 50", proof2));
        
        // Verify TX3 is in the tree
        List<String> proof3 = tree.generateProof("TX3: Charlie -> Alice 25");
        assertTrue(tree.verifyTransaction("TX3: Charlie -> Alice 25", proof3));
        
        // Verify TX4 is in the tree
        List<String> proof4 = tree.generateProof("TX4: Alice -> David 75");
        assertTrue(tree.verifyTransaction("TX4: Alice -> David 75", proof4));
    }
    
    @Test
    public void testVerifyNonExistentTransaction() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        // Try to verify non-existent transaction
        List<String> proof = tree.generateProof("TX3: Non-existent");
        assertTrue(proof.isEmpty());
        
        // Verify should fail
        assertFalse(tree.verifyTransaction("TX3: Non-existent", proof));
    }
    
    @Test
    public void testWrongProof() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        // Generate correct proof
        List<String> correctProof = tree.generateProof("TX1: Alice -> Bob 100");
        
        // Create wrong proof
        List<String> wrongProof = Arrays.asList("wronghash1", "wronghash2");
        
        // Correct proof should work
        assertTrue(tree.verifyTransaction("TX1: Alice -> Bob 100", correctProof));
        
        // Wrong proof should fail
        assertFalse(tree.verifyTransaction("TX1: Alice -> Bob 100", wrongProof));
    }
    
    @Test
    public void testDeterministicRoot() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50",
            "TX3: Charlie -> Alice 25"
        );
        
        MerkleTree tree1 = new MerkleTree(transactions);
        MerkleTree tree2 = new MerkleTree(transactions);
        
        // Same transactions should produce same root hash
        assertEquals(tree1.getRootHash(), tree2.getRootHash());
    }
    
    @Test
    public void testOrderMatters() {
        List<String> transactions1 = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50"
        );
        List<String> transactions2 = Arrays.asList(
            "TX2: Bob -> Charlie 50",
            "TX1: Alice -> Bob 100"
        );
        
        MerkleTree tree1 = new MerkleTree(transactions1);
        MerkleTree tree2 = new MerkleTree(transactions2);
        
        // Different order should produce different root hashes
        assertNotEquals(tree1.getRootHash(), tree2.getRootHash());
    }
    
    @Test
    public void testGetTransactions() {
        List<String> transactions = Arrays.asList(
            "TX1: Alice -> Bob 100",
            "TX2: Bob -> Charlie 50"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        List<String> retrievedTransactions = tree.getTransactions();
        
        // Should return same transactions
        assertEquals(transactions.size(), retrievedTransactions.size());
        assertTrue(retrievedTransactions.containsAll(transactions));
    }
    
    @Test
    public void testTreeStructure() {
        List<String> transactions = Arrays.asList(
            "TX1", "TX2", "TX3", "TX4"
        );
        MerkleTree tree = new MerkleTree(transactions);
        
        List<List<String>> treeStructure = tree.getTree();
        
        // Should have 3 levels for 4 transactions
        assertEquals(3, treeStructure.size());
        
        // First level should have 4 hashes (leaf nodes)
        assertEquals(4, treeStructure.get(0).size());
        
        // Second level should have 2 hashes
        assertEquals(2, treeStructure.get(1).size());
        
        // Third level should have 1 hash (root)
        assertEquals(1, treeStructure.get(2).size());
    }
}
