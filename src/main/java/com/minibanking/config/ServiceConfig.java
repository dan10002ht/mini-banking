package com.minibanking.config;

import com.minibanking.crypto.BankingMerkleService;
import com.minibanking.interfaces.IMerkleService;
import com.minibanking.service.BankingService;
import com.minibanking.interfaces.IBankingService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * Service Configuration
 * Configures different implementations based on properties
 */
@Configuration
public class ServiceConfig {
    
    // ==================== BANKING SERVICE CONFIGURATION ====================
    
    /**
     * Default Banking Service Implementation
     * Used when no specific implementation is configured
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "banking.service.type", havingValue = "standard", matchIfMissing = true)
    public IBankingService standardBankingService() {
        return new BankingService();
    }
    
    /**
     * Premium Banking Service Implementation
     * Used when banking.service.type=premium
     */
    @Bean
    @ConditionalOnProperty(name = "banking.service.type", havingValue = "premium")
    public IBankingService premiumBankingService() {
        // TODO: Implement PremiumBankingService
        // return new PremiumBankingService();
        return new BankingService(); // Fallback to standard for now
    }
    
    /**
     * Crypto Banking Service Implementation
     * Used when banking.service.type=crypto
     */
    @Bean
    @ConditionalOnProperty(name = "banking.service.type", havingValue = "crypto")
    public IBankingService cryptoBankingService() {
        // TODO: Implement CryptoBankingService
        // return new CryptoBankingService();
        return new BankingService(); // Fallback to standard for now
    }
    
    // ==================== MERKLE SERVICE CONFIGURATION ====================
    
    /**
     * Default Merkle Service Implementation
     * Used when no specific implementation is configured
     */
    @Bean
    @Primary
    @ConditionalOnProperty(name = "merkle.service.type", havingValue = "standard", matchIfMissing = true)
    public IMerkleService standardMerkleService() {
        return new BankingMerkleService();
    }
    
    /**
     * Optimized Merkle Service Implementation
     * Used when merkle.service.type=optimized
     */
    @Bean
    @ConditionalOnProperty(name = "merkle.service.type", havingValue = "optimized")
    public IMerkleService optimizedMerkleService() {
        // TODO: Implement OptimizedMerkleService with Redis cache
        // return new OptimizedMerkleService();
        return new BankingMerkleService(); // Fallback to standard for now
    }
    
    /**
     * Memory Efficient Merkle Service Implementation
     * Used when merkle.service.type=memory-efficient
     */
    @Bean
    @ConditionalOnProperty(name = "merkle.service.type", havingValue = "memory-efficient")
    public IMerkleService memoryEfficientMerkleService() {
        // TODO: Implement MemoryEfficientMerkleService
        // return new MemoryEfficientMerkleService();
        return new BankingMerkleService(); // Fallback to standard for now
    }
}
