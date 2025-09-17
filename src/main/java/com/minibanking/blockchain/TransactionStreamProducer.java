package com.minibanking.blockchain;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Redis Streams Producer for Transaction Events
 */
@Service
public class TransactionStreamProducer {
    
    private static final Logger logger = LoggerFactory.getLogger(TransactionStreamProducer.class);
    
    private static final String STREAM_NAME = "transaction-events";
    private static final String BLOCKCHAIN_STREAM_NAME = "blockchain-events";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    /**
     * Send transaction event to Redis Stream
     */
    public void sendTransactionEvent(TransactionEvent transactionEvent) {
        try {
            logger.info("Sending transaction event to stream: {}", transactionEvent.getTransactionId());
            
            Map<String, Object> fields = new HashMap<>();
            fields.put("transactionId", transactionEvent.getTransactionId().toString());
            fields.put("transactionCode", transactionEvent.getTransactionCode());
            fields.put("fromAccountId", transactionEvent.getFromAccountId() != null ? transactionEvent.getFromAccountId().toString() : "");
            fields.put("fromAccountNumber", transactionEvent.getFromAccountNumber() != null ? transactionEvent.getFromAccountNumber() : "");
            fields.put("toAccountId", transactionEvent.getToAccountId() != null ? transactionEvent.getToAccountId().toString() : "");
            fields.put("toAccountNumber", transactionEvent.getToAccountNumber() != null ? transactionEvent.getToAccountNumber() : "");
            fields.put("amount", transactionEvent.getAmount().toString());
            fields.put("currency", transactionEvent.getCurrency());
            fields.put("description", transactionEvent.getDescription() != null ? transactionEvent.getDescription() : "");
            fields.put("transactionType", transactionEvent.getTransactionType());
            fields.put("status", transactionEvent.getStatus());
            fields.put("timestamp", transactionEvent.getTimestamp().toString());
            fields.put("signature", transactionEvent.getSignature() != null ? transactionEvent.getSignature() : "");
            fields.put("merkleProof", transactionEvent.getMerkleProof() != null ? transactionEvent.getMerkleProof() : "");
            
            // Add to Redis Stream
            String messageId = redisTemplate.opsForStream().add(STREAM_NAME, fields);
            
            logger.info("Transaction event sent successfully with message ID: {}", messageId);
            
        } catch (Exception e) {
            logger.error("Error sending transaction event to stream", e);
            throw new RuntimeException("Failed to send transaction event", e);
        }
    }
    
    /**
     * Send blockchain event to Redis Stream
     */
    public void sendBlockchainEvent(String eventType, Object eventData) {
        try {
            logger.info("Sending blockchain event: {}", eventType);
            
            Map<String, Object> fields = new HashMap<>();
            fields.put("eventType", eventType);
            fields.put("timestamp", System.currentTimeMillis());
            fields.put("data", objectMapper.writeValueAsString(eventData));
            
            // Add to Redis Stream
            String messageId = redisTemplate.opsForStream().add(BLOCKCHAIN_STREAM_NAME, fields);
            
            logger.info("Blockchain event sent successfully with message ID: {}", messageId);
            
        } catch (Exception e) {
            logger.error("Error sending blockchain event to stream", e);
            throw new RuntimeException("Failed to send blockchain event", e);
        }
    }
    
    /**
     * Send block created event
     */
    public void sendBlockCreatedEvent(Block block) {
        try {
            Map<String, Object> blockData = new HashMap<>();
            blockData.put("blockId", block.getBlockId().toString());
            blockData.put("blockNumber", block.getBlockNumber());
            blockData.put("previousHash", block.getPreviousHash());
            blockData.put("merkleRoot", block.getMerkleRoot());
            blockData.put("transactionCount", block.getTransactionCount());
            blockData.put("difficulty", block.getDifficulty());
            blockData.put("timestamp", block.getTimestamp().toString());
            blockData.put("status", block.getStatus().name());
            
            sendBlockchainEvent("BLOCK_CREATED", blockData);
            
        } catch (Exception e) {
            logger.error("Error sending block created event", e);
        }
    }
    
    /**
     * Send block mined event
     */
    public void sendBlockMinedEvent(Block block) {
        try {
            Map<String, Object> blockData = new HashMap<>();
            blockData.put("blockId", block.getBlockId().toString());
            blockData.put("blockNumber", block.getBlockNumber());
            blockData.put("blockHash", block.getBlockHash());
            blockData.put("nonce", block.getNonce());
            blockData.put("difficulty", block.getDifficulty());
            blockData.put("timestamp", block.getTimestamp().toString());
            blockData.put("status", block.getStatus().name());
            
            sendBlockchainEvent("BLOCK_MINED", blockData);
            
        } catch (Exception e) {
            logger.error("Error sending block mined event", e);
        }
    }
    
    /**
     * Get stream info
     */
    public Map<String, Object> getStreamInfo() {
        try {
            Map<String, Object> info = new HashMap<>();
            
            // Get transaction stream info
            var transactionStreamInfo = redisTemplate.opsForStream().info(STREAM_NAME);
            info.put("transactionStream", transactionStreamInfo);
            
            // Get blockchain stream info
            var blockchainStreamInfo = redisTemplate.opsForStream().info(BLOCKCHAIN_STREAM_NAME);
            info.put("blockchainStream", blockchainStreamInfo);
            
            return info;
            
        } catch (Exception e) {
            logger.error("Error getting stream info", e);
            return new HashMap<>();
        }
    }
}
