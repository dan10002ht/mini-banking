package com.minibanking.blockchain;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.stream.StreamListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Redis Streams Consumer for Blockchain Processing
 */
@Service
public class BlockchainStreamConsumer implements StreamListener<String, MapRecord<String, String, String>> {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockchainStreamConsumer.class);
    
    private static final String STREAM_NAME = "transaction-events";
    private static final String CONSUMER_GROUP = "blockchain-processors";
    private static final String CONSUMER_NAME = "blockchain-processor-1";
    
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Autowired
    private BlockchainService blockchainService;
    
    @Autowired
    private TransactionStreamProducer streamProducer;
    
    // Buffer for collecting transactions
    private final Map<String, TransactionEvent> transactionBuffer = new ConcurrentHashMap<>();
    private static final int BATCH_SIZE = 10; // Process transactions in batches
    private static final long BATCH_TIMEOUT_MS = 5000; // 5 seconds timeout
    
    /**
     * Initialize consumer group
     */
    public void initializeConsumerGroup() {
        try {
            logger.info("Initializing consumer group: {}", CONSUMER_GROUP);
            
            // Create consumer group if it doesn't exist
            try {
                redisTemplate.opsForStream().createGroup(STREAM_NAME, CONSUMER_GROUP);
                logger.info("Created consumer group: {}", CONSUMER_GROUP);
            } catch (Exception e) {
                if (e.getMessage().contains("BUSYGROUP")) {
                    logger.info("Consumer group already exists: {}", CONSUMER_GROUP);
                } else {
                    throw e;
                }
            }
            
        } catch (Exception e) {
            logger.error("Error initializing consumer group", e);
            throw new RuntimeException("Failed to initialize consumer group", e);
        }
    }
    
    /**
     * Start consuming messages
     */
    @Async
    public void startConsuming() {
        try {
            logger.info("Starting blockchain stream consumer");
            
            while (true) {
                try {
                    // Read messages from stream
                    List<MapRecord<String, String, String>> messages = redisTemplate.opsForStream()
                        .read(Consumer.from(CONSUMER_GROUP, CONSUMER_NAME),
                              StreamOffset.create(STREAM_NAME, ReadOffset.lastConsumed()));
                    
                    if (messages.isEmpty()) {
                        Thread.sleep(1000); // Wait 1 second if no messages
                        continue;
                    }
                    
                    // Process messages
                    for (MapRecord<String, String, String> message : messages) {
                        processMessage(message);
                    }
                    
                } catch (Exception e) {
                    logger.error("Error processing messages", e);
                    Thread.sleep(5000); // Wait 5 seconds on error
                }
            }
            
        } catch (Exception e) {
            logger.error("Error in blockchain stream consumer", e);
        }
    }
    
    /**
     * Process individual message
     */
    @Override
    public void onMessage(MapRecord<String, String, String> message) {
        processMessage(message);
    }
    
    private void processMessage(MapRecord<String, String, String> message) {
        try {
            logger.debug("Processing message: {}", message.getId());
            
            // Convert message to TransactionEvent
            TransactionEvent transactionEvent = convertToTransactionEvent(message.getValue());
            
            // Add to buffer
            transactionBuffer.put(transactionEvent.getTransactionId().toString(), transactionEvent);
            
            // Check if we should process batch
            if (shouldProcessBatch()) {
                processBatch();
            }
            
            // Acknowledge message
            redisTemplate.opsForStream().acknowledge(STREAM_NAME, CONSUMER_GROUP, message.getId());
            
        } catch (Exception e) {
            logger.error("Error processing message: {}", message.getId(), e);
        }
    }
    
    /**
     * Check if we should process the current batch
     */
    private boolean shouldProcessBatch() {
        return transactionBuffer.size() >= BATCH_SIZE;
    }
    
    /**
     * Process current batch of transactions
     */
    private void processBatch() {
        try {
            if (transactionBuffer.isEmpty()) {
                return;
            }
            
            logger.info("Processing batch of {} transactions", transactionBuffer.size());
            
            // Convert buffer to list
            List<TransactionEvent> transactions = List.copyOf(transactionBuffer.values());
            
            // Create block with transactions
            Block block = blockchainService.createBlock(transactions);
            
            // Send block created event
            streamProducer.sendBlockCreatedEvent(block);
            
            // Mine the block
            Block minedBlock = blockchainService.mineBlock(block.getBlockId());
            
            // Send block mined event
            streamProducer.sendBlockMinedEvent(minedBlock);
            
            // Clear buffer
            transactionBuffer.clear();
            
            logger.info("Successfully processed batch and mined block: {}", minedBlock.getBlockNumber());
            
        } catch (Exception e) {
            logger.error("Error processing batch", e);
        }
    }
    
    /**
     * Convert message value to TransactionEvent
     */
    private TransactionEvent convertToTransactionEvent(Map<String, String> messageValue) {
        try {
            TransactionEvent event = new TransactionEvent();
            
            if (messageValue.get("transactionId") != null) {
                event.setTransactionId(java.util.UUID.fromString(messageValue.get("transactionId")));
            }
            event.setTransactionCode(messageValue.get("transactionCode"));
            
            if (messageValue.get("fromAccountId") != null && !messageValue.get("fromAccountId").isEmpty()) {
                event.setFromAccountId(java.util.UUID.fromString(messageValue.get("fromAccountId")));
            }
            event.setFromAccountNumber(messageValue.get("fromAccountNumber"));
            
            if (messageValue.get("toAccountId") != null && !messageValue.get("toAccountId").isEmpty()) {
                event.setToAccountId(java.util.UUID.fromString(messageValue.get("toAccountId")));
            }
            event.setToAccountNumber(messageValue.get("toAccountNumber"));
            
            if (messageValue.get("amount") != null) {
                event.setAmount(new java.math.BigDecimal(messageValue.get("amount")));
            }
            event.setCurrency(messageValue.get("currency"));
            event.setDescription(messageValue.get("description"));
            event.setTransactionType(messageValue.get("transactionType"));
            event.setStatus(messageValue.get("status"));
            
            if (messageValue.get("timestamp") != null) {
                event.setTimestamp(java.time.LocalDateTime.parse(messageValue.get("timestamp")));
            }
            
            event.setSignature(messageValue.get("signature"));
            event.setMerkleProof(messageValue.get("merkleProof"));
            
            return event;
            
        } catch (Exception e) {
            logger.error("Error converting message to TransactionEvent", e);
            throw new RuntimeException("Failed to convert message", e);
        }
    }
    
    /**
     * Get consumer status
     */
    public Map<String, Object> getConsumerStatus() {
        Map<String, Object> status = new ConcurrentHashMap<>();
        status.put("consumerGroup", CONSUMER_GROUP);
        status.put("consumerName", CONSUMER_NAME);
        status.put("bufferSize", transactionBuffer.size());
        status.put("batchSize", BATCH_SIZE);
        status.put("batchTimeoutMs", BATCH_TIMEOUT_MS);
        return status;
    }
}
