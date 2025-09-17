package com.minibanking.blockchain;

import com.minibanking.entity.Transaction;
import com.minibanking.repository.TransactionRepository;
import com.minibanking.crypto.HashUtils;
import com.minibanking.crypto.IMerkleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

/**
 * Service to link transactions with blocks
 */
@Service
public class TransactionBlockLinker {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionBlockLinker.class);
    
    @Autowired
    private TransactionRepository transactionRepository;
    
    @Autowired
    private IMerkleService merkleService;
    
    /**
     * Link transactions to a block
     */
    public void linkTransactionsToBlock(Block block, List<TransactionEvent> transactionEvents) {
        try {
            logger.info("Linking {} transactions to block {}", transactionEvents.size(), block.getBlockNumber());
            
            for (TransactionEvent event : transactionEvents) {
                // Find transaction by code
                Transaction transaction = transactionRepository.findByTransactionCode(event.getTransactionCode())
                    .orElse(null);
                
                if (transaction != null) {
                    // Generate transaction hash
                    String transactionHash = generateTransactionHash(transaction);
                    
                    // Generate Merkle proof
                    List<String> merkleProof = merkleService.generateProof(
                        transactionHash,
                        String.valueOf(block.getBlockNumber()),
                        block.getTransactionHashes()
                    );
                    
                    // Link transaction to block
                    transaction.setBlockInfo(block.getBlockId());
                    transaction.setTransactionHash(transactionHash);
                    transaction.setMerkleProof(String.join(",", merkleProof));
                    transaction.confirm();
                    
                    // Save updated transaction
                    transactionRepository.save(transaction);
                    
                    logger.debug("Linked transaction {} to block {}", 
                               transaction.getTransactionCode(), block.getBlockNumber());
                } else {
                    logger.warn("Transaction not found for code: {}", event.getTransactionCode());
                }
            }
            
            logger.info("Successfully linked transactions to block {}", block.getBlockNumber());
            
        } catch (Exception e) {
            logger.error("Error linking transactions to block {}", block.getBlockNumber(), e);
            throw new RuntimeException("Failed to link transactions to block", e);
        }
    }
    
    /**
     * Generate transaction hash
     */
    private String generateTransactionHash(Transaction transaction) {
        String data = transaction.getTransactionCode() +
                     transaction.getFromAccount().getAccountNumber() +
                     transaction.getToAccount().getAccountNumber() +
                     transaction.getAmount().toString() +
                     transaction.getTransactionType().name() +
                     transaction.getCreatedAt().toString();
        
        return HashUtils.sha256(data);
    }
    
    /**
     * Verify transaction is in block
     */
    public boolean verifyTransactionInBlock(String transactionCode, Long blockNumber) {
        try {
            Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
                .orElse(null);
            
            if (transaction == null || transaction.getBlockId() == null) {
                return false;
            }
            
            // Get block info via JOIN query
            List<Transaction> transactionsInBlock = transactionRepository.findByBlockNumber(blockNumber);
            if (!transactionsInBlock.contains(transaction)) {
                return false;
            }
            
            // Verify Merkle proof
            List<String> merkleProof = List.of(transaction.getMerkleProof().split(","));
            return merkleService.verifyTransaction(
                transaction.getTransactionHash(),
                merkleProof,
                String.valueOf(blockNumber),
                List.of() // Will be loaded from block
            );
            
        } catch (Exception e) {
            logger.error("Error verifying transaction {} in block {}", transactionCode, blockNumber, e);
            return false;
        }
    }
    
    /**
     * Get transactions in a block
     */
    public List<Transaction> getTransactionsInBlock(Long blockNumber) {
        return transactionRepository.findByBlockNumber(blockNumber);
    }
    
    /**
     * Get transaction confirmation count
     */
    public int getTransactionConfirmationCount(String transactionCode) {
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
            .orElse(null);
        
        return transaction != null ? transaction.getConfirmationCount() : 0;
    }
    
    /**
     * Update transaction confirmation
     */
    public void updateTransactionConfirmation(String transactionCode) {
        Transaction transaction = transactionRepository.findByTransactionCode(transactionCode)
            .orElse(null);
        
        if (transaction != null) {
            transaction.addConfirmation();
            transactionRepository.save(transaction);
            
            logger.debug("Updated confirmation count for transaction {}: {}", 
                       transactionCode, transaction.getConfirmationCount());
        }
    }
}
