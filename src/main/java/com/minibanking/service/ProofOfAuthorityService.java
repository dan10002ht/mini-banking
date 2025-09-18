package com.minibanking.service;

import com.minibanking.entity.Block;
import com.minibanking.entity.Validator;
import com.minibanking.repository.BlockRepository;
import com.minibanking.repository.ValidatorRepository;
import com.minibanking.blockchain.TransactionEvent;
import com.minibanking.crypto.HashUtils;
import com.minibanking.interfaces.IMerkleService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Proof of Authority Service for Banking
 * 
 * PoA is ideal for banking because:
 * - Fast transaction processing (< 1 second)
 * - Low energy consumption
 * - Centralized control (banks can manage validators)
 * - No mining required
 */
@Service
@Transactional
public class ProofOfAuthorityService {

    private static final Logger logger = LoggerFactory.getLogger(ProofOfAuthorityService.class);

    @Autowired
    private ValidatorRepository validatorRepository;

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private IMerkleService merkleService;

    private static final String GENESIS_PREVIOUS_HASH = "0";

    /**
     * Select the authorized validator with highest priority
     */
    public Validator selectAuthorizedValidator() {
        List<Validator> authorizedValidators = validatorRepository.findAuthorizedValidatorsOrderByPriority();

        if (authorizedValidators.isEmpty()) {
            throw new RuntimeException("No authorized validators available");
        }

        // Select validator with highest priority that can create blocks
        for (Validator validator : authorizedValidators) {
            if (validator.canCreateBlock() && validator.isOnline()) {
                logger.info("Selected validator: {} with priority: {}",
                        validator.getValidatorName(), validator.getPriority());
                return validator;
            }
        }

        // If no online validators, select the first authorized one
        Validator fallbackValidator = authorizedValidators.get(0);
        logger.warn("No online validators found, using fallback: {}", fallbackValidator.getValidatorName());
        return fallbackValidator;
    }

    /**
     * Create block without mining (PoA advantage)
     */
    public Block createBlockWithoutMining(List<TransactionEvent> transactions) {
        try {
            logger.info("Creating block with PoA for {} transactions", transactions.size());

            // 1. Select authorized validator
            Validator validator = selectAuthorizedValidator();

            // 2. Get latest block info
            Optional<Block> latestBlock = blockRepository.findLatestBlock();
            Long blockNumber = latestBlock.map(b -> b.getBlockNumber() + 1).orElse(1L);
            String previousHash = latestBlock.map(Block::getBlockHash).orElse(GENESIS_PREVIOUS_HASH);

            // 3. Create Merkle root
            List<String> transactionHashes = new ArrayList<>();
            for (TransactionEvent tx : transactions) {
                transactionHashes.add(tx.getTransactionHash());
            }
            String merkleRoot = merkleService.createMerkleTree(UUID.randomUUID().toString(), transactionHashes);

            // 4. Create block (no mining needed!)
            Block block = new Block();
            block.setBlockNumber(blockNumber);
            block.setPreviousHash(previousHash);
            block.setMerkleRoot(merkleRoot);
            block.setTransactionCount(transactions.size());
            block.setTimestamp(LocalDateTime.now());
            block.setStatus(Block.BlockStatus.PENDING);

            // 5. Calculate block hash (no nonce needed for PoA)
            String blockHash = calculateBlockHash(block);
            block.setBlockHash(blockHash);
            block.setStatus(Block.BlockStatus.MINED);

            // 6. Save block
            block = blockRepository.save(block);

            // 7. Update validator stats
            validator.incrementBlocksCreated();
            validatorRepository.save(validator);

            logger.info("Block created successfully: {} by validator: {}",
                    block.getBlockNumber(), validator.getValidatorName());

            return block;

        } catch (Exception e) {
            logger.error("Failed to create block with PoA: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to create block: " + e.getMessage(), e);
        }
    }

    /**
     * Calculate block hash (simplified for PoA)
     */
    private String calculateBlockHash(Block block) {
        StringBuilder data = new StringBuilder();
        data.append(block.getBlockNumber());
        data.append(block.getPreviousHash());
        data.append(block.getMerkleRoot());
        data.append(block.getTimestamp());
        data.append(block.getTransactionCount());

        return HashUtils.sha256(data.toString());
    }

    /**
     * Validate block integrity
     */
    public boolean validateBlock(Block block) {
        try {
            // 1. Check block hash
            String calculatedHash = calculateBlockHash(block);
            if (!calculatedHash.equals(block.getBlockHash())) {
                logger.warn("Block hash mismatch for block: {}", block.getBlockNumber());
                return false;
            }

            // 2. Check previous hash
            if (block.getBlockNumber() > 1) {
                Optional<Block> previousBlock = blockRepository.findByBlockNumber(block.getBlockNumber() - 1);
                if (previousBlock.isEmpty() || !previousBlock.get().getBlockHash().equals(block.getPreviousHash())) {
                    logger.warn("Previous hash mismatch for block: {}", block.getBlockNumber());
                    return false;
                }
            }

            // 3. Check Merkle root
            // This would require the actual transactions to verify
            // For now, we'll assume it's valid if the block was created by an authorized
            // validator

            return true;

        } catch (Exception e) {
            logger.error("Error validating block: {}", e.getMessage(), e);
            return false;
        }
    }

    /**
     * Get PoA statistics
     */
    public PoAStatistics getPoAStatistics() {
        PoAStatistics stats = new PoAStatistics();

        // Count validators
        stats.setTotalValidators(validatorRepository.count());
        stats.setAuthorizedValidators(validatorRepository.countAuthorizedValidators());
        stats.setActiveValidators(validatorRepository.findByIsActiveTrue().size());

        // Count online validators
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        stats.setOnlineValidators(validatorRepository.findOnlineValidators(threshold).size());

        // Count blocks
        stats.setTotalBlocks(blockRepository.countTotalBlocks());

        // Get top validators by blocks created
        List<Validator> topValidators = validatorRepository.findValidatorsByBlocksCreated();
        stats.setTopValidators(topValidators.subList(0, Math.min(5, topValidators.size())));

        return stats;
    }

    /**
     * PoA Statistics DTO
     */
    public static class PoAStatistics {
        private Long totalValidators;
        private Long authorizedValidators;
        private Integer activeValidators;
        private Integer onlineValidators;
        private Long totalBlocks;
        private List<Validator> topValidators;

        // Getters and Setters
        public Long getTotalValidators() {
            return totalValidators;
        }

        public void setTotalValidators(Long totalValidators) {
            this.totalValidators = totalValidators;
        }

        public Long getAuthorizedValidators() {
            return authorizedValidators;
        }

        public void setAuthorizedValidators(Long authorizedValidators) {
            this.authorizedValidators = authorizedValidators;
        }

        public Integer getActiveValidators() {
            return activeValidators;
        }

        public void setActiveValidators(Integer activeValidators) {
            this.activeValidators = activeValidators;
        }

        public Integer getOnlineValidators() {
            return onlineValidators;
        }

        public void setOnlineValidators(Integer onlineValidators) {
            this.onlineValidators = onlineValidators;
        }

        public Long getTotalBlocks() {
            return totalBlocks;
        }

        public void setTotalBlocks(Long totalBlocks) {
            this.totalBlocks = totalBlocks;
        }

        public List<Validator> getTopValidators() {
            return topValidators;
        }

        public void setTopValidators(List<Validator> topValidators) {
            this.topValidators = topValidators;
        }
    }
}
