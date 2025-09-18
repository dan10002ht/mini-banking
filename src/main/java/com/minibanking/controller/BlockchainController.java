package com.minibanking.controller;

import com.minibanking.entity.Block;
import com.minibanking.service.BlockchainService;
import com.minibanking.blockchain.BlockchainStreamConsumer;
import com.minibanking.blockchain.TransactionStreamProducer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * REST Controller for Blockchain operations
 */
@RestController
@RequestMapping("/api/blockchain")
@Tag(name = "Blockchain", description = "Blockchain management and querying")
public class BlockchainController {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainController.class);

    @Autowired
    private BlockchainService blockchainService;

    @Autowired
    private TransactionStreamProducer streamProducer;

    @Autowired
    private BlockchainStreamConsumer streamConsumer;

    /**
     * Get blockchain information
     */
    @GetMapping("/info")
    @Operation(summary = "Get blockchain information", description = "Get overall blockchain statistics and status")
    public ResponseEntity<BlockchainService.BlockchainInfo> getBlockchainInfo() {
        try {
            logger.info("Getting blockchain info");
            BlockchainService.BlockchainInfo info = blockchainService.getBlockchainInfo();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error getting blockchain info", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get block by ID
     */
    @GetMapping("/blocks/{blockId}")
    @Operation(summary = "Get block by ID", description = "Get detailed information about a specific block")
    public ResponseEntity<Block> getBlockById(
            @Parameter(description = "Block ID") @PathVariable UUID blockId) {
        try {
            logger.info("Getting block by ID: {}", blockId);
            Optional<Block> block = blockchainService.getBlock(blockId);
            return block.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting block by ID: {}", blockId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get block by block number
     */
    @GetMapping("/blocks/number/{blockNumber}")
    @Operation(summary = "Get block by number", description = "Get block information by block number")
    public ResponseEntity<Block> getBlockByNumber(
            @Parameter(description = "Block number") @PathVariable Long blockNumber) {
        try {
            logger.info("Getting block by number: {}", blockNumber);
            Optional<Block> block = blockchainService.getBlockByNumber(blockNumber);
            return block.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting block by number: {}", blockNumber, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get latest block
     */
    @GetMapping("/blocks/latest")
    @Operation(summary = "Get latest block", description = "Get the most recently created block")
    public ResponseEntity<Block> getLatestBlock() {
        try {
            logger.info("Getting latest block");
            Optional<Block> block = blockchainService.getLatestBlock();
            return block.map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (Exception e) {
            logger.error("Error getting latest block", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get blocks in range
     */
    @GetMapping("/blocks")
    @Operation(summary = "Get blocks in range", description = "Get blocks within a specified range")
    public ResponseEntity<List<Block>> getBlocksInRange(
            @Parameter(description = "Start block number") @RequestParam Long startBlock,
            @Parameter(description = "End block number") @RequestParam Long endBlock) {
        try {
            logger.info("Getting blocks in range: {} to {}", startBlock, endBlock);
            List<Block> blocks = blockchainService.getBlocksInRange(startBlock, endBlock);
            return ResponseEntity.ok(blocks);
        } catch (Exception e) {
            logger.error("Error getting blocks in range: {} to {}", startBlock, endBlock, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Verify block integrity
     */
    @PostMapping("/blocks/{blockId}/verify")
    @Operation(summary = "Verify block integrity", description = "Verify that a block is valid and properly mined")
    public ResponseEntity<Map<String, Object>> verifyBlock(
            @Parameter(description = "Block ID") @PathVariable UUID blockId) {
        try {
            logger.info("Verifying block: {}", blockId);
            boolean isValid = blockchainService.verifyBlock(blockId);

            Map<String, Object> response = Map.of(
                    "blockId", blockId,
                    "isValid", isValid,
                    "timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error verifying block: {}", blockId, e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get stream information
     */
    @GetMapping("/streams/info")
    @Operation(summary = "Get stream information", description = "Get Redis Streams statistics and status")
    public ResponseEntity<Map<String, Object>> getStreamInfo() {
        try {
            logger.info("Getting stream info");
            Map<String, Object> info = streamProducer.getStreamInfo();
            return ResponseEntity.ok(info);
        } catch (Exception e) {
            logger.error("Error getting stream info", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Get consumer status
     */
    @GetMapping("/consumer/status")
    @Operation(summary = "Get consumer status", description = "Get blockchain stream consumer status")
    public ResponseEntity<Map<String, Object>> getConsumerStatus() {
        try {
            logger.info("Getting consumer status");
            Map<String, Object> status = streamConsumer.getConsumerStatus();
            return ResponseEntity.ok(status);
        } catch (Exception e) {
            logger.error("Error getting consumer status", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Initialize consumer group
     */
    @PostMapping("/consumer/initialize")
    @Operation(summary = "Initialize consumer group", description = "Initialize Redis Streams consumer group")
    public ResponseEntity<Map<String, Object>> initializeConsumer() {
        try {
            logger.info("Initializing consumer group");
            streamConsumer.initializeConsumerGroup();

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Consumer group initialized successfully",
                    "timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error initializing consumer group", e);
            return ResponseEntity.internalServerError().build();
        }
    }

    /**
     * Start consumer
     */
    @PostMapping("/consumer/start")
    @Operation(summary = "Start consumer", description = "Start blockchain stream consumer")
    public ResponseEntity<Map<String, Object>> startConsumer() {
        try {
            logger.info("Starting consumer");
            streamConsumer.startConsuming();

            Map<String, Object> response = Map.of(
                    "status", "success",
                    "message", "Consumer started successfully",
                    "timestamp", System.currentTimeMillis());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error starting consumer", e);
            return ResponseEntity.internalServerError().build();
        }
    }
}
