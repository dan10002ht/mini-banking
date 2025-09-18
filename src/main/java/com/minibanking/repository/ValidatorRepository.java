package com.minibanking.repository;

import com.minibanking.entity.Validator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for Validator entities
 */
@Repository
public interface ValidatorRepository extends JpaRepository<Validator, UUID> {

    /**
     * Find validator by name
     */
    Optional<Validator> findByValidatorName(String validatorName);

    /**
     * Find authorized validators ordered by priority
     */
    @Query("SELECT v FROM Validator v WHERE v.isAuthorized = true AND v.isActive = true ORDER BY v.priority DESC")
    List<Validator> findAuthorizedValidatorsOrderByPriority();

    /**
     * Find active validators
     */
    List<Validator> findByIsActiveTrue();

    /**
     * Find validators by authorization status
     */
    List<Validator> findByIsAuthorized(Boolean isAuthorized);

    /**
     * Find validators that can create blocks
     */
    @Query("SELECT v FROM Validator v WHERE v.isAuthorized = true AND v.isActive = true " +
            "AND (v.lockedUntil IS NULL OR v.lockedUntil < :now) " +
            "ORDER BY v.priority DESC")
    List<Validator> findValidatorsCanCreateBlock(@Param("now") LocalDateTime now);

    /**
     * Find online validators
     */
    @Query("SELECT v FROM Validator v WHERE v.lastHeartbeat > :threshold")
    List<Validator> findOnlineValidators(@Param("threshold") LocalDateTime threshold);

    /**
     * Find validators by priority range
     */
    @Query("SELECT v FROM Validator v WHERE v.priority BETWEEN :minPriority AND :maxPriority")
    List<Validator> findByPriorityRange(@Param("minPriority") Integer minPriority,
            @Param("maxPriority") Integer maxPriority);

    /**
     * Count authorized validators
     */
    @Query("SELECT COUNT(v) FROM Validator v WHERE v.isAuthorized = true AND v.isActive = true")
    Long countAuthorizedValidators();

    /**
     * Find validators with most blocks created
     */
    @Query("SELECT v FROM Validator v ORDER BY v.blocksCreated DESC")
    List<Validator> findValidatorsByBlocksCreated();

    /**
     * Find validators that haven't created blocks recently
     */
    @Query("SELECT v FROM Validator v WHERE v.lastBlockTime IS NULL OR v.lastBlockTime < :threshold")
    List<Validator> findInactiveValidators(@Param("threshold") LocalDateTime threshold);

    /**
     * Find validators by node URL
     */
    Optional<Validator> findByNodeUrl(String nodeUrl);

    /**
     * Find validators with failed attempts
     */
    @Query("SELECT v FROM Validator v WHERE v.failedAttempts > 0")
    List<Validator> findValidatorsWithFailedAttempts();
}
