package com.minibanking.blockchain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service to initialize blockchain components on startup
 */
@Service
public class BlockchainStartupService {
    
    private static final Logger logger = LoggerFactory.getLogger(BlockchainStartupService.class);
    
    @Autowired
    private BlockchainStreamConsumer streamConsumer;
    
    /**
     * Initialize blockchain components when application starts
     */
    @EventListener(ApplicationReadyEvent.class)
    @Async
    public void initializeBlockchain() {
        try {
            logger.info("Initializing blockchain components...");
            
            // Initialize consumer group
            streamConsumer.initializeConsumerGroup();
            logger.info("Consumer group initialized successfully");
            
            // Start consuming messages
            streamConsumer.startConsuming();
            logger.info("Blockchain consumer started successfully");
            
            logger.info("Blockchain initialization completed successfully");
            
        } catch (Exception e) {
            logger.error("Error initializing blockchain components", e);
        }
    }
}
