package com.minibanking.service;

import com.minibanking.crypto.HashUtils;
import com.minibanking.interfaces.IMerkleService;
import com.minibanking.entity.Block;
import com.minibanking.repository.BlockRepository;
import com.minibanking.blockchain.TransactionEvent;
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
 * Blockchain Service for managing blocks and mining
 */
@Service
@Transactional
public class BlockchainService {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainService.class);

    @Autowired
    private BlockRepository blockRepository;

    @Autowired
    private IMerkleService merkleService;

    private static final String GENESIS_PREVIOUS_HASH = "0";
    private static final int BLOCK_SIZE_LIMIT = 1000; // Maximum transactions per block
    private static final int MINING_DIFFICULTY = 4; // Number of leading zeros required

    /**
     * Create a new block with transactions
     */
    public Block createBlock(List<TransactionEvent> transactions) {
        try {
            logger.info("Creating new block with {} transactions", transactions.size());

            // Get the latest block to determine block number and previous hash
            Optional<Block> latestBlock = blockRepository.findLatestBlock();
            Long blockNumber = latestBlock.map(b -> b.getBlockNumber() + 1).orElse(1L);
            String previousHash = latestBlock.map(Block::getBlockHash).orElse(GENESIS_PREVIOUS_HASH);

            // Create Merkle root from transactions
            List<String> transactionHashes = new ArrayList<>();
            for (TransactionEvent tx : transactions) {
                String txHash = HashUtils.sha256(tx.toString());
                transactionHashes.add(txHash);
            }

            String merkleRoot = merkleService.createMerkleTree(
                    "block_" + blockNumber,
                    transactionHashes);

            // Create new block
            Block block = new Block(blockNumber, previousHash, merkleRoot);
            block.setTransactionCount(transactions.size());
            block.setDifficulty(MINING_DIFFICULTY);
            block.setStatus(Block.BlockStatus.PENDING);
            block.setSizeBytes(calculateBlockSize(transactions));

            // Save block to database
            Block savedBlock = blockRepository.save(block);

            logger.info("Created block {} with hash: {}", blockNumber, savedBlock.getBlockId());
            return savedBlock;

        } catch (Exception e) {
            logger.error("Error creating block", e);
            throw new RuntimeException("Failed to create block", e);
        }
    }

    /**
     * Mine a block (Proof of Work)
     */
    public Block mineBlock(UUID blockId) {
        try {
            logger.info("Starting mining for block: {}", blockId);

            Optional<Block> blockOpt = blockRepository.findById(blockId);
            if (blockOpt.isEmpty()) {
                throw new RuntimeException("Block not found: " + blockId);
            }

            Block block = blockOpt.get();
            block.setStatus(Block.BlockStatus.MINING);
            blockRepository.save(block);

            // Mine the block (Proof of Work)
            String target = "0".repeat(block.getDifficulty());
            String blockHash;

            do {
                block.incrementNonce();
                String dataToHash = block.getBlockNumber() +
                        block.getPreviousHash() +
                        block.getMerkleRoot() +
                        block.getTimestamp() +
                        block.getNonce();
                blockHash = HashUtils.sha256(dataToHash);
            } while (!blockHash.startsWith(target));

            // Set the mined block
            block.setMined(blockHash);
            Block minedBlock = blockRepository.save(block);

            logger.info("Successfully mined block {} with nonce: {}",
                    block.getBlockNumber(), block.getNonce());

            return minedBlock;

        } catch (Exception e) {
            logger.error("Error mining block: {}", blockId, e);
            throw new RuntimeException("Failed to mine block", e);
        }
    }

    /**
     * Verify block integrity
     */
    public boolean verifyBlock(UUID blockId) {
        try {
            Optional<Block> blockOpt = blockRepository.findById(blockId);
            if (blockOpt.isEmpty()) {
                return false;
            }

            Block block = blockOpt.get();

            // Verify block hash
            String dataToHash = block.getBlockNumber() +
                    block.getPreviousHash() +
                    block.getMerkleRoot() +
                    block.getTimestamp() +
                    block.getNonce();
            String calculatedHash = HashUtils.sha256(dataToHash);

            if (!calculatedHash.equals(block.getBlockHash())) {
                logger.warn("Block {} hash verification failed", blockId);
                return false;
            }

            // Verify difficulty
            String target = "0".repeat(block.getDifficulty());
            if (!block.getBlockHash().startsWith(target)) {
                logger.warn("Block {} difficulty verification failed", blockId);
                return false;
            }

            logger.info("Block {} verification successful", blockId);
            return true;

        } catch (Exception e) {
            logger.error("Error verifying block: {}", blockId, e);
            return false;
        }
    }

    /**
     * Get blockchain info
     */
    public BlockchainInfo getBlockchainInfo() {
        try {
            Long totalBlocks = blockRepository.countTotalBlocks();
            Optional<Block> latestBlock = blockRepository.findLatestBlock();
            List<Block> pendingBlocks = blockRepository.findPendingBlocks();

            return new BlockchainInfo(
                    totalBlocks,
                    latestBlock.map(Block::getBlockNumber).orElse(0L),
                    pendingBlocks.size(),
                    MINING_DIFFICULTY);

        } catch (Exception e) {
            logger.error("Error getting blockchain info", e);
            throw new RuntimeException("Failed to get blockchain info", e);
        }
    }

    /**
     * Get block by ID
     */
    public Optional<Block> getBlock(UUID blockId) {
        return blockRepository.findById(blockId);
    }

    /**
     * Get block by block number
     */
    public Optional<Block> getBlockByNumber(Long blockNumber) {
        return blockRepository.findByBlockNumber(blockNumber);
    }

    /**
     * Get latest block
     */
    public Optional<Block> getLatestBlock() {
        return blockRepository.findLatestBlock();
    }

    /**
     * Get blocks in range
     */
    public List<Block> getBlocksInRange(Long startBlock, Long endBlock) {
        return blockRepository.findBlocksInRange(startBlock, endBlock);
    }

    /**
     * Calculate block size in bytes
     */
    private Long calculateBlockSize(List<TransactionEvent> transactions) {
        // Simple calculation - in real implementation, this would be more accurate
        return (long) (transactions.size() * 200); // Approximate 200 bytes per transaction
    }

    /**
     * Blockchain Info DTO
     */
    public static class BlockchainInfo {
        private final Long totalBlocks;
        private final Long latestBlockNumber;
        private final Integer pendingBlocks;
        private final Integer difficulty;

        public BlockchainInfo(Long totalBlocks, Long latestBlockNumber, Integer pendingBlocks, Integer difficulty) {
            this.totalBlocks = totalBlocks;
            this.latestBlockNumber = latestBlockNumber;
            this.pendingBlocks = pendingBlocks;
            this.difficulty = difficulty;
        }

        public Long getTotalBlocks() {
            return totalBlocks;
        }

        public Long getLatestBlockNumber() {
            return latestBlockNumber;
        }

        public Integer getPendingBlocks() {
            return pendingBlocks;
        }

        public Integer getDifficulty() {
            return difficulty;
        }
    }
}
