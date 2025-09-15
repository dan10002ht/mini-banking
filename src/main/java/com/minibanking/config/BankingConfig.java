package com.minibanking.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;

@Configuration
@EnableTransactionManagement
public class BankingConfig {
    
    /**
     * Banking Transaction Manager Configuration
     * Optimized for high-concurrency banking operations
     */
    @Bean
    public PlatformTransactionManager transactionManager(DataSource dataSource) {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setDataSource(dataSource);
        
        // Banking-specific transaction settings
        transactionManager.setDefaultTimeout(30); // 30 seconds timeout
        transactionManager.setFailEarlyOnGlobalRollbackOnly(true);
        transactionManager.setRollbackOnCommitFailure(true);
        
        return transactionManager;
    }
}

