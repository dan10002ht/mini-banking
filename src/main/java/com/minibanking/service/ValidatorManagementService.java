package com.minibanking.service;

import com.minibanking.entity.Validator;
import com.minibanking.repository.ValidatorRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Validator Management Service
 * 
 * Manages validators for Proof of Authority consensus
 */
@Service
@Transactional
public class ValidatorManagementService {

    private static final Logger logger = LoggerFactory.getLogger(ValidatorManagementService.class);

    @Autowired
    private ValidatorRepository validatorRepository;

    /**
     * Add a new authorized validator
     */
    public Validator addAuthorizedValidator(String validatorName, String publicKey, Integer priority, String nodeUrl) {
        logger.info("Adding authorized validator: {} with priority: {}", validatorName, priority);

        // Check if validator name already exists
        if (validatorRepository.findByValidatorName(validatorName).isPresent()) {
            throw new IllegalArgumentException("Validator name already exists: " + validatorName);
        }

        // Check if node URL already exists
        if (nodeUrl != null && validatorRepository.findByNodeUrl(nodeUrl).isPresent()) {
            throw new IllegalArgumentException("Node URL already exists: " + nodeUrl);
        }

        Validator validator = new Validator();
        validator.setValidatorName(validatorName);
        validator.setPublicKey(publicKey);
        validator.setPriority(priority);
        validator.setNodeUrl(nodeUrl);
        validator.setIsAuthorized(true);
        validator.setIsActive(true);
        validator.setBlocksCreated(0);
        validator.setFailedAttempts(0);

        validator = validatorRepository.save(validator);

        logger.info("Successfully added validator: {} with ID: {}", validatorName, validator.getValidatorId());
        return validator;
    }

    /**
     * Revoke validator authority
     */
    public void revokeValidatorAuthority(UUID validatorId) {
        logger.info("Revoking authority for validator: {}", validatorId);

        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + validatorId));

        validator.setIsAuthorized(false);
        validator.setIsActive(false);
        validatorRepository.save(validator);

        logger.info("Successfully revoked authority for validator: {}", validator.getValidatorName());
    }

    /**
     * Activate validator
     */
    public void activateValidator(UUID validatorId) {
        logger.info("Activating validator: {}", validatorId);

        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + validatorId));

        validator.setIsActive(true);
        validator.resetFailedAttempts();
        validatorRepository.save(validator);

        logger.info("Successfully activated validator: {}", validator.getValidatorName());
    }

    /**
     * Deactivate validator
     */
    public void deactivateValidator(UUID validatorId) {
        logger.info("Deactivating validator: {}", validatorId);

        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + validatorId));

        validator.setIsActive(false);
        validatorRepository.save(validator);

        logger.info("Successfully deactivated validator: {}", validator.getValidatorName());
    }

    /**
     * Update validator priority
     */
    public void updateValidatorPriority(UUID validatorId, Integer newPriority) {
        logger.info("Updating priority for validator: {} to {}", validatorId, newPriority);

        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + validatorId));

        validator.setPriority(newPriority);
        validatorRepository.save(validator);

        logger.info("Successfully updated priority for validator: {} to {}",
                validator.getValidatorName(), newPriority);
    }

    /**
     * Update validator heartbeat
     */
    public void updateValidatorHeartbeat(UUID validatorId) {
        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + validatorId));

        validator.updateHeartbeat();
        validator.resetFailedAttempts(); // Reset failed attempts on successful heartbeat
        validatorRepository.save(validator);
    }

    /**
     * Record validator failure
     */
    public void recordValidatorFailure(UUID validatorId) {
        logger.warn("Recording failure for validator: {}", validatorId);

        Validator validator = validatorRepository.findById(validatorId)
                .orElseThrow(() -> new IllegalArgumentException("Validator not found: " + validatorId));

        validator.incrementFailedAttempts();
        validatorRepository.save(validator);

        if (validator.isLocked()) {
            logger.warn("Validator {} is now locked until: {}",
                    validator.getValidatorName(), validator.getLockedUntil());
        }
    }

    /**
     * Get all authorized validators
     */
    public List<Validator> getAuthorizedValidators() {
        return validatorRepository.findAuthorizedValidatorsOrderByPriority();
    }

    /**
     * Get all active validators
     */
    public List<Validator> getActiveValidators() {
        return validatorRepository.findByIsActiveTrue();
    }

    /**
     * Get online validators
     */
    public List<Validator> getOnlineValidators() {
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(5);
        return validatorRepository.findOnlineValidators(threshold);
    }

    /**
     * Get validator by ID
     */
    public Optional<Validator> getValidatorById(UUID validatorId) {
        return validatorRepository.findById(validatorId);
    }

    /**
     * Get validator by name
     */
    public Optional<Validator> getValidatorByName(String validatorName) {
        return validatorRepository.findByValidatorName(validatorName);
    }

    /**
     * Get validators that can create blocks
     */
    public List<Validator> getValidatorsCanCreateBlock() {
        return validatorRepository.findValidatorsCanCreateBlock(LocalDateTime.now());
    }

    /**
     * Get validators with failed attempts
     */
    public List<Validator> getValidatorsWithFailedAttempts() {
        return validatorRepository.findValidatorsWithFailedAttempts();
    }

    /**
     * Get validator statistics
     */
    public ValidatorStatistics getValidatorStatistics() {
        ValidatorStatistics stats = new ValidatorStatistics();

        stats.setTotalValidators(validatorRepository.count());
        stats.setAuthorizedValidators(validatorRepository.countAuthorizedValidators());
        stats.setActiveValidators(validatorRepository.findByIsActiveTrue().size());
        stats.setOnlineValidators(getOnlineValidators().size());
        stats.setValidatorsCanCreateBlock(getValidatorsCanCreateBlock().size());
        stats.setValidatorsWithFailedAttempts(getValidatorsWithFailedAttempts().size());

        return stats;
    }

    /**
     * Validator Statistics DTO
     */
    public static class ValidatorStatistics {
        private Long totalValidators;
        private Long authorizedValidators;
        private Integer activeValidators;
        private Integer onlineValidators;
        private Integer validatorsCanCreateBlock;
        private Integer validatorsWithFailedAttempts;

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

        public Integer getValidatorsCanCreateBlock() {
            return validatorsCanCreateBlock;
        }

        public void setValidatorsCanCreateBlock(Integer validatorsCanCreateBlock) {
            this.validatorsCanCreateBlock = validatorsCanCreateBlock;
        }

        public Integer getValidatorsWithFailedAttempts() {
            return validatorsWithFailedAttempts;
        }

        public void setValidatorsWithFailedAttempts(Integer validatorsWithFailedAttempts) {
            this.validatorsWithFailedAttempts = validatorsWithFailedAttempts;
        }
    }
}
